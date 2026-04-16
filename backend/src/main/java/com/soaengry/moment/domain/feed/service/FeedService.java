package com.soaengry.moment.domain.feed.service;

import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.feed.dto.request.CommentRequest;
import com.soaengry.moment.domain.feed.dto.request.PostRequest;
import com.soaengry.moment.domain.feed.dto.response.CommentResponse;
import com.soaengry.moment.domain.feed.dto.response.PostResponse;
import com.soaengry.moment.domain.feed.entity.*;
import com.soaengry.moment.domain.feed.exception.FeedErrorCode;
import com.soaengry.moment.domain.feed.exception.FeedException;
import com.soaengry.moment.domain.feed.repository.*;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeedService {

    private final PostRepository postRepository;
    private final PostImageRepository postImageRepository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final BookmarkRepository bookmarkRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final AttendanceRepository attendanceRepository;

    // ==================== Post ====================

    @Transactional
    public PostResponse createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS));

        Post post = Post.create(user, request.content());
        postRepository.save(post);
        attachImages(post, request.imageUrls());

        return PostResponse.from(post, false, false);
    }

    public Page<PostResponse> getFeed(Long userId, Pageable pageable) {
        Page<Long> postIds = postRepository.findVisiblePostIds(userId, pageable);
        if (postIds.isEmpty()) {
            return postIds.map(id -> null);
        }
        List<Long> ids = postIds.getContent();
        Map<Long, Post> postMap = postRepository.findWithUserAndImagesByIds(ids)
                .stream().collect(Collectors.toMap(Post::getId, p -> p));

        Set<Long> likedSet = Collections.emptySet();
        Set<Long> bookmarkedSet = Collections.emptySet();
        if (userId != null) {
            likedSet = toLikedPostIds(userId, ids);
            bookmarkedSet = toBookmarkedPostIds(userId, ids);
        }
        final Set<Long> finalLiked = likedSet;
        final Set<Long> finalBookmarked = bookmarkedSet;

        return postIds.map(id -> {
            Post post = postMap.get(id);
            if (post == null) return null;
            return PostResponse.from(post, finalLiked.contains(id), finalBookmarked.contains(id));
        });
    }

    public Page<PostResponse> getUserPosts(Long userId, Long targetUserId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdWithImages(targetUserId, pageable);
        return enrichPostResponses(posts, userId);
    }

    public PostResponse getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.POST_NOT_FOUND));

        boolean isLiked = userId != null && postLikeRepository.existsByPostIdAndUserId(postId, userId);
        boolean isBookmarked = userId != null && bookmarkRepository.existsByPostIdAndUserId(postId, userId);

        return PostResponse.from(post, isLiked, isBookmarked);
    }

    @Transactional
    public PostResponse updatePost(Long userId, Long postId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS);
        }

        post.update(request.content());
        post.clearImages();
        attachImages(post, request.imageUrls());

        boolean isLiked = postLikeRepository.existsByPostIdAndUserId(postId, userId);
        boolean isBookmarked = bookmarkRepository.existsByPostIdAndUserId(postId, userId);

        return PostResponse.from(post, isLiked, isBookmarked);
    }

    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS);
        }

        postRepository.delete(post);
    }

    // ==================== Like ====================

    @Transactional
    public boolean toggleLike(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS));

        Optional<PostLike> existing = postLikeRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            postLikeRepository.delete(existing.get());
            post.decrementLikeCount();
            return false;
        } else {
            postLikeRepository.save(PostLike.create(post, user));
            post.incrementLikeCount();
            return true;
        }
    }

    // ==================== Bookmark ====================

    @Transactional
    public boolean toggleBookmark(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS));

        Optional<Bookmark> existing = bookmarkRepository.findByPostIdAndUserId(postId, userId);
        if (existing.isPresent()) {
            bookmarkRepository.delete(existing.get());
            return false;
        } else {
            bookmarkRepository.save(Bookmark.create(post, user));
            return true;
        }
    }

    public Page<PostResponse> getBookmarkedPosts(Long userId, Pageable pageable) {
        Page<Long> postIds = bookmarkRepository.findBookmarkedPostIdsByUserId(userId, pageable);
        return mapBookmarkedPostIds(postIds, userId);
    }

    // ==================== Comment ====================

    @Transactional
    public CommentResponse createComment(Long userId, Long postId, CommentRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.POST_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS));

        Comment comment = Comment.create(post, user, request.content());
        commentRepository.save(comment);
        post.incrementCommentCount();

        return CommentResponse.from(comment);
    }

    public Page<CommentResponse> getComments(Long postId, Pageable pageable) {
        return commentRepository.findByPostIdWithUser(postId, pageable)
                .map(CommentResponse::from);
    }

    @Transactional
    public CommentResponse updateComment(Long userId, Long commentId, CommentRequest request) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS);
        }

        comment.update(request.content());
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUser().getId().equals(userId)) {
            throw new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS);
        }

        Post post = comment.getPost();
        commentRepository.delete(comment);
        post.decrementCommentCount();
    }

    // ==================== Wedding Feed ====================

    @Transactional
    public PostResponse createEventPost(Long userId, Long eventId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS));

        Post post = Post.create(user, request.content(), eventId);
        postRepository.save(post);
        attachImages(post, request.imageUrls());

        return PostResponse.from(post, false, false);
    }

    public Page<PostResponse> getEventFeed(Long eventId, Long userId, Pageable pageable) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.isPublic()) {
            if (userId == null
                    || (!event.getUser().getId().equals(userId) && !attendanceRepository.existsByUserIdAndEventId(userId, eventId))) {
                throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
            }
        }
        Page<Post> posts = postRepository.findByEventIdWithUserAndImages(eventId, pageable);
        return enrichPostResponses(posts, userId);
    }

    // ==================== My Page ====================

    public Page<PostResponse> getMyPosts(Long userId, Long eventId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdAndOptionalEventId(userId, eventId, pageable);
        return enrichPostResponses(posts, userId);
    }

    public Page<PostResponse> getMyBookmarks(Long userId, Long eventId, Pageable pageable) {
        Page<Long> postIds = bookmarkRepository.findBookmarkedPostIdsByUserIdAndOptionalEventId(userId, eventId, pageable);
        return mapBookmarkedPostIds(postIds, userId);
    }

    public Page<PostResponse> getMyLikes(Long userId, Long eventId, Pageable pageable) {
        Page<Long> postIds = postLikeRepository.findLikedPostIdsByUserIdAndOptionalEventId(userId, eventId, pageable);
        List<Post> posts = postRepository.findAllById(postIds.getContent());

        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Set<Long> bookmarkedPostIds = toBookmarkedPostIds(userId, postIds.getContent());

        return postIds.map(id -> {
            Post post = postMap.get(id);
            if (post == null) return null;
            return PostResponse.from(post, true, bookmarkedPostIds.contains(id));
        });
    }

    public Page<CommentResponse> getMyComments(Long userId, Long eventId, Pageable pageable) {
        return commentRepository.findByUserIdAndOptionalEventId(userId, eventId, pageable)
                .map(CommentResponse::from);
    }

    // ==================== Helper ====================

    private Set<Long> toLikedPostIds(Long userId, List<Long> postIds) {
        return postLikeRepository.findByUserIdAndPostIdIn(userId, postIds)
                .stream().map(l -> l.getPost().getId()).collect(Collectors.toSet());
    }

    private Set<Long> toBookmarkedPostIds(Long userId, List<Long> postIds) {
        return bookmarkRepository.findByUserIdAndPostIdIn(userId, postIds)
                .stream().map(b -> b.getPost().getId()).collect(Collectors.toSet());
    }

    private void attachImages(Post post, List<String> imageUrls) {
        if (imageUrls == null) return;
        for (int i = 0; i < imageUrls.size(); i++) {
            post.addImage(PostImage.create(post, imageUrls.get(i), i));
        }
    }

    private Page<PostResponse> mapBookmarkedPostIds(Page<Long> postIds, Long userId) {
        List<Post> posts = postRepository.findAllById(postIds.getContent());
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Set<Long> likedPostIds = toLikedPostIds(userId, postIds.getContent());
        return postIds.map(id -> {
            Post post = postMap.get(id);
            if (post == null) return null;
            return PostResponse.from(post, likedPostIds.contains(id), true);
        });
    }

    private Page<PostResponse> enrichPostResponses(Page<Post> posts, Long userId) {
        if (userId == null) {
            return posts.map(p -> PostResponse.from(p, false, false));
        }

        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Set<Long> likedPostIds = toLikedPostIds(userId, postIds);
        Set<Long> bookmarkedPostIds = toBookmarkedPostIds(userId, postIds);

        return posts.map(post -> PostResponse.from(
                post,
                likedPostIds.contains(post.getId()),
                bookmarkedPostIds.contains(post.getId())
        ));
    }
}

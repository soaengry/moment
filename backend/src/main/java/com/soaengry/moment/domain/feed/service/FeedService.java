package com.soaengry.moment.domain.feed.service;

import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.feed.dto.request.CommentRequest;
import com.soaengry.moment.domain.feed.dto.request.PostRequest;
import com.soaengry.moment.domain.feed.dto.response.CommentResponse;
import com.soaengry.moment.domain.feed.dto.response.PostResponse;
import com.soaengry.moment.domain.feed.entity.*;
import com.soaengry.moment.domain.feed.exception.FeedErrorCode;
import com.soaengry.moment.domain.feed.exception.FeedException;
import com.soaengry.moment.domain.feed.repository.*;
import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.InvitationRepository;
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
    private final InvitationRepository invitationRepository;
    private final AttendanceRepository attendanceRepository;

    // ==================== Post ====================

    @Transactional
    public PostResponse createPost(Long userId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS));

        Post post = Post.create(user, request.content());
        postRepository.save(post);

        if (request.imageUrls() != null) {
            for (int i = 0; i < request.imageUrls().size(); i++) {
                PostImage image = PostImage.create(post, request.imageUrls().get(i), i);
                post.addImage(image);
            }
        }

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

        List<Long> likedPostIds = Collections.emptyList();
        List<Long> bookmarkedPostIds = Collections.emptyList();
        if (userId != null) {
            likedPostIds = postLikeRepository.findByUserIdAndPostIdIn(userId, ids)
                    .stream().map(l -> l.getPost().getId()).toList();
            bookmarkedPostIds = bookmarkRepository.findByUserIdAndPostIdIn(userId, ids)
                    .stream().map(b -> b.getPost().getId()).toList();
        }
        final Set<Long> likedSet = new HashSet<>(likedPostIds);
        final Set<Long> bookmarkedSet = new HashSet<>(bookmarkedPostIds);

        return postIds.map(id -> {
            Post post = postMap.get(id);
            if (post == null) return null;
            return PostResponse.from(post, likedSet.contains(id), bookmarkedSet.contains(id));
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

        if (request.imageUrls() != null) {
            for (int i = 0; i < request.imageUrls().size(); i++) {
                PostImage image = PostImage.create(post, request.imageUrls().get(i), i);
                post.addImage(image);
            }
        }

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
        List<Post> posts = postRepository.findAllById(postIds.getContent());

        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Set<Long> likedPostIds = postLikeRepository.findByUserIdAndPostIdIn(userId, postIds.getContent())
                .stream().map(l -> l.getPost().getId()).collect(Collectors.toSet());

        return postIds.map(id -> {
            Post post = postMap.get(id);
            if (post == null) return null;
            return PostResponse.from(post, likedPostIds.contains(id), true);
        });
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
    public PostResponse createWeddingPost(Long userId, Long weddingId, PostRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.UNAUTHORIZED_ACCESS));

        Post post = Post.create(user, request.content(), weddingId);
        postRepository.save(post);

        if (request.imageUrls() != null) {
            for (int i = 0; i < request.imageUrls().size(); i++) {
                PostImage image = PostImage.create(post, request.imageUrls().get(i), i);
                post.addImage(image);
            }
        }

        return PostResponse.from(post, false, false);
    }

    public Page<PostResponse> getWeddingFeed(Long weddingId, Long userId, Pageable pageable) {
        Invitation invitation = invitationRepository.findById(weddingId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));
        if (!invitation.isPublic()) {
            if (userId == null) throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);
            User viewer = userRepository.findById(userId)
                    .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED));
            if (viewer.getRole() != User.Role.ADMIN
                    && !attendanceRepository.existsByUserIdAndWeddingId(userId, weddingId)) {
                throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);
            }
        }
        Page<Post> posts = postRepository.findByWeddingIdWithUserAndImages(weddingId, pageable);
        return enrichPostResponses(posts, userId);
    }

    // ==================== My Page ====================

    public Page<PostResponse> getMyPosts(Long userId, Long weddingId, Pageable pageable) {
        Page<Post> posts = postRepository.findByUserIdAndOptionalWeddingId(userId, weddingId, pageable);
        return enrichPostResponses(posts, userId);
    }

    public Page<PostResponse> getMyBookmarks(Long userId, Long weddingId, Pageable pageable) {
        Page<Long> postIds = bookmarkRepository.findBookmarkedPostIdsByUserIdAndOptionalWeddingId(userId, weddingId, pageable);
        List<Post> posts = postRepository.findAllById(postIds.getContent());

        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Set<Long> likedPostIds = postLikeRepository.findByUserIdAndPostIdIn(userId, postIds.getContent())
                .stream().map(l -> l.getPost().getId()).collect(Collectors.toSet());

        return postIds.map(id -> {
            Post post = postMap.get(id);
            if (post == null) return null;
            return PostResponse.from(post, likedPostIds.contains(id), true);
        });
    }

    public Page<PostResponse> getMyLikes(Long userId, Long weddingId, Pageable pageable) {
        Page<Long> postIds = postLikeRepository.findLikedPostIdsByUserIdAndOptionalWeddingId(userId, weddingId, pageable);
        List<Post> posts = postRepository.findAllById(postIds.getContent());

        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Set<Long> bookmarkedPostIds = bookmarkRepository.findByUserIdAndPostIdIn(userId, postIds.getContent())
                .stream().map(b -> b.getPost().getId()).collect(Collectors.toSet());

        return postIds.map(id -> {
            Post post = postMap.get(id);
            if (post == null) return null;
            return PostResponse.from(post, true, bookmarkedPostIds.contains(id));
        });
    }

    public Page<CommentResponse> getMyComments(Long userId, Long weddingId, Pageable pageable) {
        return commentRepository.findByUserIdAndOptionalWeddingId(userId, weddingId, pageable)
                .map(CommentResponse::from);
    }

    // ==================== Helper ====================

    private Page<PostResponse> enrichPostResponses(Page<Post> posts, Long userId) {
        if (userId == null) {
            return posts.map(p -> PostResponse.from(p, false, false));
        }

        List<Long> postIds = posts.getContent().stream().map(Post::getId).toList();
        Set<Long> likedPostIds = postLikeRepository.findByUserIdAndPostIdIn(userId, postIds)
                .stream().map(l -> l.getPost().getId()).collect(Collectors.toSet());
        Set<Long> bookmarkedPostIds = bookmarkRepository.findByUserIdAndPostIdIn(userId, postIds)
                .stream().map(b -> b.getPost().getId()).collect(Collectors.toSet());

        return posts.map(post -> PostResponse.from(
                post,
                likedPostIds.contains(post.getId()),
                bookmarkedPostIds.contains(post.getId())
        ));
    }
}

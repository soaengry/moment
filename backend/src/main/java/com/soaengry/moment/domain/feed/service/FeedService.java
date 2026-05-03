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
import com.soaengry.moment.global.common.CursorPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
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

    public CursorPageResponse<PostResponse> getFeed(Long userId, Long cursor, int size) {
        List<Long> postIds = postRepository.findVisiblePostIdsCursor(userId, cursor, PageRequest.of(0, size + 1));
        boolean hasNext = postIds.size() > size;
        List<Long> ids = hasNext ? postIds.subList(0, size) : postIds;
        Long nextCursor = hasNext ? ids.get(ids.size() - 1) : null;

        if (ids.isEmpty()) {
            return new CursorPageResponse<>(Collections.emptyList(), null, false);
        }

        List<Post> posts = postRepository.findWithUserAndImagesByIds(ids);
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Map<Long, String> slugMap = fetchEventSlugMap(posts);

        Set<Long> likedSet = Collections.emptySet();
        Set<Long> bookmarkedSet = Collections.emptySet();
        if (userId != null) {
            likedSet = toLikedPostIds(userId, ids);
            bookmarkedSet = toBookmarkedPostIds(userId, ids);
        }
        final Set<Long> finalLiked = likedSet;
        final Set<Long> finalBookmarked = bookmarkedSet;

        List<PostResponse> content = ids.stream()
                .map(id -> {
                    Post post = postMap.get(id);
                    if (post == null) return null;
                    return PostResponse.from(post, finalLiked.contains(id), finalBookmarked.contains(id),
                            post.getEventId() != null ? slugMap.get(post.getEventId()) : null);
                })
                .filter(Objects::nonNull)
                .toList();

        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    @Cacheable(cacheNames = "posts", key = "#postId + ':' + (#userId ?: 'anon')")
    public PostResponse getPost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new FeedException(FeedErrorCode.POST_NOT_FOUND));

        boolean isLiked = userId != null && postLikeRepository.existsByPostIdAndUserId(postId, userId);
        boolean isBookmarked = userId != null && bookmarkRepository.existsByPostIdAndUserId(postId, userId);

        return PostResponse.from(post, isLiked, isBookmarked);
    }

    @CacheEvict(cacheNames = "posts", key = "#postId + ':' + (#userId ?: 'anon')")
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

    @CacheEvict(cacheNames = "posts", allEntries = true)
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

    @CacheEvict(cacheNames = "posts", key = "#postId + ':' + (#userId ?: 'anon')")
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

    @CacheEvict(cacheNames = "posts", key = "#postId + ':' + (#userId ?: 'anon')")
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

    public CursorPageResponse<PostResponse> getBookmarkedPosts(Long userId, Long cursor, int size) {
        List<Long> postIds = bookmarkRepository.findBookmarkedPostIdsByUserIdCursor(userId, cursor, PageRequest.of(0, size + 1));
        return mapBookmarkedPostIds(postIds, userId, size);
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

    public CursorPageResponse<CommentResponse> getComments(Long postId, Long cursor, int size) {
        List<Comment> comments = commentRepository.findByPostIdCursor(postId, cursor, PageRequest.of(0, size + 1));
        return toCursorPage(comments, size, c -> c.getId(), CommentResponse::from);
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

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.getUser().getId().equals(userId) && !attendanceRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }

        Post post = Post.create(user, request.content(), eventId);
        postRepository.save(post);
        attachImages(post, request.imageUrls());

        return PostResponse.from(post, false, false);
    }

    public CursorPageResponse<PostResponse> getEventFeed(Long eventId, Long userId, Long cursor, int size) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.isPublic()) {
            if (userId == null
                    || (!event.getUser().getId().equals(userId) && !attendanceRepository.existsByUserIdAndEventId(userId, eventId))) {
                throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
            }
        }
        List<Post> posts = postRepository.findByEventIdCursor(eventId, cursor, PageRequest.of(0, size + 1));
        return enrichPostResponsesCursor(posts, userId, size);
    }

    // ==================== My Page ====================

    public CursorPageResponse<PostResponse> getMyPosts(Long userId, Long eventId, Long cursor, int size) {
        List<Post> posts = postRepository.findByUserIdAndOptionalEventIdCursor(userId, eventId, cursor, PageRequest.of(0, size + 1));
        return enrichPostResponsesCursor(posts, userId, size);
    }

    public CursorPageResponse<PostResponse> getMyBookmarks(Long userId, Long eventId, Long cursor, int size) {
        List<Long> postIds = bookmarkRepository.findBookmarkedPostIdsByUserIdAndOptionalEventIdCursor(userId, eventId, cursor, PageRequest.of(0, size + 1));
        return mapBookmarkedPostIds(postIds, userId, size);
    }

    public CursorPageResponse<PostResponse> getMyLikes(Long userId, Long eventId, Long cursor, int size) {
        List<Long> postIds = postLikeRepository.findLikedPostIdsByUserIdAndOptionalEventIdCursor(userId, eventId, cursor, PageRequest.of(0, size + 1));
        boolean hasNext = postIds.size() > size;
        List<Long> ids = hasNext ? postIds.subList(0, size) : postIds;
        Long nextCursor = hasNext ? ids.get(ids.size() - 1) : null;

        List<Post> posts = postRepository.findAllById(ids);
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Map<Long, String> slugMap = fetchEventSlugMap(posts);
        Set<Long> bookmarkedPostIds = toBookmarkedPostIds(userId, ids);

        List<PostResponse> content = ids.stream()
                .map(id -> {
                    Post post = postMap.get(id);
                    if (post == null) return null;
                    return PostResponse.from(post, true, bookmarkedPostIds.contains(id),
                            post.getEventId() != null ? slugMap.get(post.getEventId()) : null);
                })
                .filter(Objects::nonNull)
                .toList();

        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    public CursorPageResponse<CommentResponse> getMyComments(Long userId, Long eventId, Long cursor, int size) {
        List<Comment> comments = commentRepository.findByUserIdAndOptionalEventIdCursor(userId, eventId, cursor, PageRequest.of(0, size + 1));
        return toCursorPage(comments, size, c -> c.getId(), CommentResponse::from);
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

    private CursorPageResponse<PostResponse> mapBookmarkedPostIds(List<Long> postIds, Long userId, int size) {
        boolean hasNext = postIds.size() > size;
        List<Long> ids = hasNext ? postIds.subList(0, size) : postIds;
        Long nextCursor = hasNext ? ids.get(ids.size() - 1) : null;

        if (ids.isEmpty()) {
            return new CursorPageResponse<>(Collections.emptyList(), null, false);
        }

        List<Post> posts = postRepository.findAllById(ids);
        Map<Long, Post> postMap = posts.stream().collect(Collectors.toMap(Post::getId, p -> p));
        Map<Long, String> slugMap = fetchEventSlugMap(posts);
        Set<Long> likedPostIds = toLikedPostIds(userId, ids);

        List<PostResponse> content = ids.stream()
                .map(id -> {
                    Post post = postMap.get(id);
                    if (post == null) return null;
                    return PostResponse.from(post, likedPostIds.contains(id), true,
                            post.getEventId() != null ? slugMap.get(post.getEventId()) : null);
                })
                .filter(Objects::nonNull)
                .toList();

        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private CursorPageResponse<PostResponse> enrichPostResponsesCursor(List<Post> posts, Long userId, int size) {
        boolean hasNext = posts.size() > size;
        List<Post> page = hasNext ? posts.subList(0, size) : posts;
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;

        if (page.isEmpty()) {
            return new CursorPageResponse<>(Collections.emptyList(), null, false);
        }

        Map<Long, String> slugMap = fetchEventSlugMap(page);
        List<PostResponse> content;
        if (userId == null) {
            content = page.stream().map(p -> PostResponse.from(p, false, false,
                    p.getEventId() != null ? slugMap.get(p.getEventId()) : null)).toList();
        } else {
            List<Long> postIds = page.stream().map(Post::getId).toList();
            Set<Long> likedPostIds = toLikedPostIds(userId, postIds);
            Set<Long> bookmarkedPostIds = toBookmarkedPostIds(userId, postIds);
            content = page.stream().map(post -> PostResponse.from(
                    post,
                    likedPostIds.contains(post.getId()),
                    bookmarkedPostIds.contains(post.getId()),
                    post.getEventId() != null ? slugMap.get(post.getEventId()) : null
            )).toList();
        }

        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }

    private Map<Long, String> fetchEventSlugMap(List<Post> posts) {
        List<Long> eventIds = posts.stream()
                .map(Post::getEventId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (eventIds.isEmpty()) return Collections.emptyMap();
        return eventRepository.findAllById(eventIds).stream()
                .collect(Collectors.toMap(Event::getId, Event::getSlug));
    }

    private <E, R> CursorPageResponse<R> toCursorPage(List<E> items, int size,
                                                       Function<E, Long> idExtractor,
                                                       Function<E, R> mapper) {
        boolean hasNext = items.size() > size;
        List<E> page = hasNext ? items.subList(0, size) : items;
        Long nextCursor = hasNext ? idExtractor.apply(page.get(page.size() - 1)) : null;
        List<R> content = page.stream().map(mapper).toList();
        return new CursorPageResponse<>(content, nextCursor, hasNext);
    }
}

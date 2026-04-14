package com.soaengry.moment.domain.feed.controller;

import com.soaengry.moment.domain.feed.dto.request.CommentRequest;
import com.soaengry.moment.domain.feed.dto.request.PostRequest;
import com.soaengry.moment.domain.feed.dto.response.CommentResponse;
import com.soaengry.moment.domain.feed.dto.response.PostResponse;
import com.soaengry.moment.domain.feed.service.FeedService;
import org.springframework.lang.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    // ==================== Post ====================

    @PostMapping("/api/feed")
    public ResponseEntity<PostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody PostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = feedService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/feed")
    public ResponseEntity<Page<PostResponse>> getFeed(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<PostResponse> responses = feedService.getFeed(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/feed/{postId}")
    public ResponseEntity<PostResponse> getPost(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        PostResponse response = feedService.getPost(postId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/api/feed/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = feedService.updatePost(userId, postId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/feed/{postId}")
    public ResponseEntity<Void> deletePost(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = (Long) authentication.getPrincipal();
        feedService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Like ====================

    @PostMapping("/api/feed/{postId}/like")
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = (Long) authentication.getPrincipal();
        boolean liked = feedService.toggleLike(userId, postId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    // ==================== Bookmark ====================

    @PostMapping("/api/feed/{postId}/bookmark")
    public ResponseEntity<Map<String, Boolean>> toggleBookmark(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = (Long) authentication.getPrincipal();
        boolean bookmarked = feedService.toggleBookmark(userId, postId);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @GetMapping("/api/feed/bookmarks")
    public ResponseEntity<Page<PostResponse>> getBookmarkedPosts(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getBookmarkedPosts(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    // ==================== My Page ====================

    @GetMapping("/api/feed/my/posts")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long eventId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getMyPosts(userId, eventId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/feed/my/bookmarks")
    public ResponseEntity<Page<PostResponse>> getMyBookmarks(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long eventId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getMyBookmarks(userId, eventId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/feed/my/likes")
    public ResponseEntity<Page<PostResponse>> getMyLikes(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long eventId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getMyLikes(userId, eventId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/api/feed/my/comments")
    public ResponseEntity<Page<CommentResponse>> getMyComments(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long eventId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<CommentResponse> responses = feedService.getMyComments(userId, eventId, pageable);
        return ResponseEntity.ok(responses);
    }

    // ==================== Comment ====================

    @PostMapping("/api/feed/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = feedService.createComment(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/api/feed/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<CommentResponse> responses = feedService.getComments(postId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/api/feed/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            Authentication authentication,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = feedService.updateComment(userId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api/feed/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            Authentication authentication,
            @PathVariable Long commentId) {
        Long userId = (Long) authentication.getPrincipal();
        feedService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Event Feed ====================

    @GetMapping("/api/events/{eventId}/feed/posts")
    public ResponseEntity<Page<PostResponse>> getEventFeed(
            @PathVariable Long eventId,
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<PostResponse> responses = feedService.getEventFeed(eventId, userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/api/events/{eventId}/feed/posts")
    public ResponseEntity<PostResponse> createEventPost(
            @PathVariable Long eventId,
            Authentication authentication,
            @Valid @RequestBody PostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = feedService.createEventPost(userId, eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}

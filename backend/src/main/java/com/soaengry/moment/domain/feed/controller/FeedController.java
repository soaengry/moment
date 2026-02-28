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
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final FeedService feedService;

    // ==================== Post ====================

    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createPost(
            Authentication authentication,
            @Valid @RequestBody PostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = feedService.createPost(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponse>> getFeed(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<PostResponse> responses = feedService.getFeed(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        PostResponse response = feedService.getPost(postId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{targetUserId}/posts")
    public ResponseEntity<Page<PostResponse>> getUserPosts(
            Authentication authentication,
            @PathVariable Long targetUserId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<PostResponse> responses = feedService.getUserPosts(userId, targetUserId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = feedService.updatePost(userId, postId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = (Long) authentication.getPrincipal();
        feedService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Like ====================

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = (Long) authentication.getPrincipal();
        boolean liked = feedService.toggleLike(userId, postId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    // ==================== Bookmark ====================

    @PostMapping("/posts/{postId}/bookmark")
    public ResponseEntity<Map<String, Boolean>> toggleBookmark(
            Authentication authentication,
            @PathVariable Long postId) {
        Long userId = (Long) authentication.getPrincipal();
        boolean bookmarked = feedService.toggleBookmark(userId, postId);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    @GetMapping("/bookmarks")
    public ResponseEntity<Page<PostResponse>> getBookmarkedPosts(
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getBookmarkedPosts(userId, pageable);
        return ResponseEntity.ok(responses);
    }

    // ==================== My Page ====================

    @GetMapping("/my/posts")
    public ResponseEntity<Page<PostResponse>> getMyPosts(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long weddingId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getMyPosts(userId, weddingId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my/bookmarks")
    public ResponseEntity<Page<PostResponse>> getMyBookmarks(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long weddingId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getMyBookmarks(userId, weddingId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my/likes")
    public ResponseEntity<Page<PostResponse>> getMyLikes(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long weddingId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<PostResponse> responses = feedService.getMyLikes(userId, weddingId, pageable);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/my/comments")
    public ResponseEntity<Page<CommentResponse>> getMyComments(
            Authentication authentication,
            @RequestParam(required = false) @Nullable Long weddingId,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = (Long) authentication.getPrincipal();
        Page<CommentResponse> responses = feedService.getMyComments(userId, weddingId, pageable);
        return ResponseEntity.ok(responses);
    }

    // ==================== Comment ====================

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            Authentication authentication,
            @PathVariable Long postId,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = feedService.createComment(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<CommentResponse> responses = feedService.getComments(postId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            Authentication authentication,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = feedService.updateComment(userId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            Authentication authentication,
            @PathVariable Long commentId) {
        Long userId = (Long) authentication.getPrincipal();
        feedService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}

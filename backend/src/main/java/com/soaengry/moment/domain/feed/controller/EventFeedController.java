package com.soaengry.moment.domain.feed.controller;

import com.soaengry.moment.domain.feed.dto.request.CommentRequest;
import com.soaengry.moment.domain.feed.dto.request.PostRequest;
import com.soaengry.moment.domain.feed.dto.response.CommentResponse;
import com.soaengry.moment.domain.feed.dto.response.PostResponse;
import com.soaengry.moment.domain.feed.service.FeedService;
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
@RequestMapping("/api/events/{eventId}/feed")
@RequiredArgsConstructor
public class EventFeedController {

    private final FeedService feedService;

    // ==================== Post ====================

    @GetMapping("/posts")
    public ResponseEntity<Page<PostResponse>> getEventFeed(
            @PathVariable Long eventId,
            Authentication authentication,
            @PageableDefault(size = 20) Pageable pageable) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        Page<PostResponse> responses = feedService.getEventFeed(eventId, userId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/posts")
    public ResponseEntity<PostResponse> createEventPost(
            @PathVariable Long eventId,
            Authentication authentication,
            @Valid @RequestBody PostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = feedService.createEventPost(userId, eventId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        PostResponse response = feedService.getPost(postId, userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponse> updatePost(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody PostRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        PostResponse response = feedService.updatePost(userId, postId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        feedService.deletePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Like ====================

    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<Map<String, Boolean>> toggleLike(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean liked = feedService.toggleLike(userId, postId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    // ==================== Bookmark ====================

    @PostMapping("/posts/{postId}/bookmark")
    public ResponseEntity<Map<String, Boolean>> toggleBookmark(
            @PathVariable Long postId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        boolean bookmarked = feedService.toggleBookmark(userId, postId);
        return ResponseEntity.ok(Map.of("bookmarked", bookmarked));
    }

    // ==================== Comment ====================

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long postId,
            @PageableDefault(size = 30) Pageable pageable) {
        Page<CommentResponse> responses = feedService.getComments(postId, pageable);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long postId,
            Authentication authentication,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = feedService.createComment(userId, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long commentId,
            Authentication authentication,
            @Valid @RequestBody CommentRequest request) {
        Long userId = (Long) authentication.getPrincipal();
        CommentResponse response = feedService.updateComment(userId, commentId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long commentId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        feedService.deleteComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}

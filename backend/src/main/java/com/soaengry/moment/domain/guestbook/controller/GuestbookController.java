package com.soaengry.moment.domain.guestbook.controller;

import com.soaengry.moment.domain.guestbook.dto.request.GuestbookRequest;
import com.soaengry.moment.domain.guestbook.dto.response.GuestbookResponse;
import com.soaengry.moment.domain.guestbook.service.GuestbookService;
import com.soaengry.moment.global.common.ApiResponse;
import com.soaengry.moment.global.common.CursorPageResponse;
import com.soaengry.moment.global.common.SuccessCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weddings/{weddingId}/guestbook")
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;

    @PostMapping
    public ResponseEntity<GuestbookResponse> createEntry(
            @PathVariable Long weddingId,
            @Valid @RequestBody GuestbookRequest request,
            Authentication authentication) {
        GuestbookResponse response = guestbookService.createEntry(weddingId, request, extractUserId(authentication));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<GuestbookResponse>> getEntries(
            @PathVariable Long weddingId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size,
            Authentication authentication) {
        return ResponseEntity.ok(guestbookService.getEntries(
                weddingId, extractUserId(authentication), isAdmin(authentication), cursor, size));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<GuestbookResponse> updateEntry(
            @PathVariable Long weddingId,
            @PathVariable Long entryId,
            @Valid @RequestBody GuestbookRequest request,
            Authentication authentication) {
        GuestbookResponse response = guestbookService.updateEntry(
                weddingId, entryId, request, extractUserId(authentication), isAdmin(authentication));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{entryId}/verify-password")
    public ResponseEntity<ApiResponse<?>> verifyPassword(
            @PathVariable Long weddingId,
            @PathVariable Long entryId,
            @RequestBody java.util.Map<String, String> body) {
        guestbookService.verifyPassword(entryId, body.get("password"));
        return ResponseEntity.ok(ApiResponse.ok(SuccessCode.OK, null));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<Void> deleteEntry(
            @PathVariable Long weddingId,
            @PathVariable Long entryId,
            @RequestParam(required = false) String password,
            Authentication authentication) {
        guestbookService.deleteEntry(weddingId, entryId, password, extractUserId(authentication), isAdmin(authentication));
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) return null;
        Object principal = authentication.getPrincipal();
        return principal instanceof Long ? (Long) principal : null;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}

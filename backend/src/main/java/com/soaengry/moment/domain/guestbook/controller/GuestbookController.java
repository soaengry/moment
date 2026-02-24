package com.soaengry.moment.domain.guestbook.controller;

import com.soaengry.moment.domain.guestbook.dto.request.GuestbookRequest;
import com.soaengry.moment.domain.guestbook.dto.response.GuestbookResponse;
import com.soaengry.moment.domain.guestbook.service.GuestbookService;
import com.soaengry.moment.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weddings/{weddingId}/guestbook")
@RequiredArgsConstructor
public class GuestbookController {

    private final GuestbookService guestbookService;

    @PostMapping
    public ResponseEntity<ApiResponse<GuestbookResponse>> createEntry(
            @PathVariable Long weddingId,
            @Valid @RequestBody GuestbookRequest request) {
        GuestbookResponse response = guestbookService.createEntry(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<GuestbookResponse>>> getEntries(
            @PathVariable Long weddingId,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<GuestbookResponse> responses = guestbookService.getEntries(weddingId, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{entryId}")
    public ResponseEntity<ApiResponse<GuestbookResponse>> updateEntry(
            @PathVariable Long weddingId,
            @PathVariable Long entryId,
            @Valid @RequestBody GuestbookRequest request) {
        GuestbookResponse response = guestbookService.updateEntry(weddingId, entryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{entryId}")
    public ResponseEntity<ApiResponse<Void>> deleteEntry(
            @PathVariable Long weddingId,
            @PathVariable Long entryId,
            @RequestParam(required = false) String password) {
        guestbookService.deleteEntry(weddingId, entryId, password);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

package com.soaengry.moment.domain.rsvp.controller;

import com.soaengry.moment.domain.rsvp.dto.request.RsvpCreateRequest;
import com.soaengry.moment.domain.rsvp.dto.request.RsvpUpdateRequest;
import com.soaengry.moment.domain.rsvp.dto.response.RsvpListResponse;
import com.soaengry.moment.domain.rsvp.dto.response.RsvpResponse;
import com.soaengry.moment.domain.rsvp.dto.response.RsvpStatsResponse;
import com.soaengry.moment.domain.rsvp.service.RsvpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rsvp")
@RequiredArgsConstructor
public class RsvpController {

    private final RsvpService rsvpService;

    @PostMapping
    public ResponseEntity<RsvpResponse> create(
            @Valid @RequestBody RsvpCreateRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rsvpService.create(request, userId));
    }

    @GetMapping("/me")
    public ResponseEntity<RsvpResponse> getMyRsvp(
            @RequestParam Long weddingId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(rsvpService.getMyRsvp(weddingId, userId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RsvpResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody RsvpUpdateRequest request,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(rsvpService.update(id, request, userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal Long userId) {
        rsvpService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<RsvpStatsResponse> getStats(
            @RequestParam Long weddingId,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(rsvpService.getStats(weddingId, userId));
    }

    @GetMapping("/list")
    public ResponseEntity<RsvpListResponse> getList(
            @RequestParam Long weddingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Long userId) {
        return ResponseEntity.ok(rsvpService.getList(weddingId, page, size, userId));
    }
}

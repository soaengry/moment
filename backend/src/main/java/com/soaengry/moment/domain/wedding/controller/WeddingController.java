package com.soaengry.moment.domain.wedding.controller;

import com.soaengry.moment.domain.wedding.dto.request.WeddingRequest;
import com.soaengry.moment.domain.wedding.dto.response.WeddingResponse;
import com.soaengry.moment.domain.wedding.service.WeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weddings")
@RequiredArgsConstructor
public class WeddingController {

    private final WeddingService weddingService;

    // ─── Wedding ───

    @PostMapping
    public ResponseEntity<WeddingResponse> createWedding(
            @RequestBody WeddingRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(weddingService.createWedding(userId, request));
    }

    @GetMapping("/{weddingId}")
    public ResponseEntity<WeddingResponse> getWedding(@PathVariable Long weddingId) {
        return ResponseEntity.ok(weddingService.getWedding(weddingId));
    }

    @PutMapping("/{weddingId}")
    public ResponseEntity<WeddingResponse> updateWedding(
            @PathVariable Long weddingId,
            @RequestBody WeddingRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(weddingService.updateWedding(weddingId, userId, request));
    }
}

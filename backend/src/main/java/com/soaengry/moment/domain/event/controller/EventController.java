package com.soaengry.moment.domain.event.controller;

import com.soaengry.moment.domain.event.dto.request.CheckSlugRequest;
import com.soaengry.moment.domain.event.dto.request.EventCreateRequest;
import com.soaengry.moment.domain.event.dto.response.CheckSlugResponse;
import com.soaengry.moment.domain.event.dto.response.EventInfoResponse;
import com.soaengry.moment.domain.event.service.EventService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @PostMapping
    public ResponseEntity<EventInfoResponse> createEventWithDetails(
            @Valid @RequestBody EventCreateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(eventService.createEventWithDetails(userId, request));
    }

    @PutMapping("/{eventId}/details")
    public ResponseEntity<EventInfoResponse> updateEventWithDetails(
            @PathVariable Long eventId,
            @Valid @RequestBody EventCreateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(eventService.updateEventWithDetails(eventId, userId, request));
    }

    @DeleteMapping("/{eventId}")
    public ResponseEntity<Void> deleteEvent(
            @PathVariable Long eventId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        eventService.deleteEvent(eventId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-slug")
    public ResponseEntity<CheckSlugResponse> checkSlug(@Valid @RequestBody CheckSlugRequest request) {
        return ResponseEntity.ok(new CheckSlugResponse(eventService.checkSlugExists(request.slug())));
    }

    @GetMapping("/{slug:[a-zA-Z0-9\\-]+}")
    public ResponseEntity<EventInfoResponse> getEventInfo(
            @PathVariable String slug,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(eventService.getEventInfoBySlug(slug, userId));
    }
}

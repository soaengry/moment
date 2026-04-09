package com.soaengry.moment.domain.event.controller;

import com.soaengry.moment.domain.event.dto.request.*;
import com.soaengry.moment.domain.event.dto.response.*;
import com.soaengry.moment.domain.event.service.EventService;
import com.soaengry.moment.domain.wedding.dto.request.HostRequest;
import com.soaengry.moment.domain.wedding.dto.response.HostResponse;
import com.soaengry.moment.domain.wedding.service.WeddingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final WeddingService weddingService;

    // ─── Event CRUD ───

    @PostMapping
    public ResponseEntity<EventResponse> createEvent(
            @RequestBody EventRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(userId, request));
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponse> getEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEvent(eventId));
    }

    @PutMapping("/{eventId}")
    public ResponseEntity<EventResponse> updateEvent(
            @PathVariable Long eventId,
            @RequestBody EventRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(eventService.updateEvent(eventId, userId, request));
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

    @GetMapping("/{slug}/info")
    public ResponseEntity<EventInfoResponse> getEventInfo(
            @PathVariable String slug,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(eventService.getEventInfo(slug, userId));
    }

    // ─── HeroImages ───

    @PostMapping("/{eventId}/hero-images")
    public ResponseEntity<HeroImageResponse> addHeroImage(
            @PathVariable Long eventId,
            @RequestBody HeroImageRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addHeroImage(eventId, userId, request));
    }

    @GetMapping("/{eventId}/hero-images")
    public ResponseEntity<List<HeroImageResponse>> getHeroImages(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getHeroImages(eventId));
    }

    @DeleteMapping("/hero-images/{imageId}")
    public ResponseEntity<Void> deleteHeroImage(
            @PathVariable Long imageId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        eventService.deleteHeroImage(imageId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── Transportation ───

    @PostMapping("/{eventId}/transportation")
    public ResponseEntity<TransportationResponse> addTransportation(
            @PathVariable Long eventId,
            @RequestBody TransportationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addTransportation(eventId, userId, request));
    }

    @GetMapping("/{eventId}/transportation")
    public ResponseEntity<List<TransportationResponse>> getTransportations(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getTransportations(eventId));
    }

    @PutMapping("/transportation/{transportId}")
    public ResponseEntity<TransportationResponse> updateTransportation(
            @PathVariable Long transportId,
            @RequestBody TransportationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(eventService.updateTransportation(transportId, userId, request));
    }

    @DeleteMapping("/transportation/{transportId}")
    public ResponseEntity<Void> deleteTransportation(
            @PathVariable Long transportId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        eventService.deleteTransportation(transportId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── Hosts ───

    @PostMapping("/{eventId}/hosts")
    public ResponseEntity<HostResponse> createHost(
            @PathVariable Long eventId,
            @RequestBody HostRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(weddingService.createHost(eventId, userId, request));
    }

    @GetMapping("/{eventId}/hosts")
    public ResponseEntity<List<HostResponse>> getHosts(@PathVariable Long eventId) {
        return ResponseEntity.ok(weddingService.getHosts(eventId));
    }

    @PutMapping("/hosts/{hostId}")
    public ResponseEntity<HostResponse> updateHost(
            @PathVariable Long hostId,
            @RequestBody HostRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(weddingService.updateHost(hostId, userId, request));
    }

    @DeleteMapping("/hosts/{hostId}")
    public ResponseEntity<Void> deleteHost(
            @PathVariable Long hostId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteHost(hostId, userId);
        return ResponseEntity.noContent().build();
    }

    // ─── Announcements ───

    @PostMapping("/{eventId}/announcements")
    public ResponseEntity<AnnouncementResponse> addAnnouncement(
            @PathVariable Long eventId,
            @RequestBody AnnouncementRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.addAnnouncement(eventId, userId, request));
    }

    @GetMapping("/{eventId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getAnnouncements(eventId));
    }

    @PutMapping("/announcements/{announcementId}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(eventService.updateAnnouncement(announcementId, userId, request));
    }

    @DeleteMapping("/announcements/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long announcementId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        eventService.deleteAnnouncement(announcementId, userId);
        return ResponseEntity.noContent().build();
    }
}

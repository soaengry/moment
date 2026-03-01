package com.soaengry.moment.domain.wedding.controller;

import com.soaengry.moment.domain.wedding.dto.request.*;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.service.WeddingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weddings")
@RequiredArgsConstructor
public class WeddingController {

    private final WeddingService weddingService;

    // ==================== Wedding ====================

    @PostMapping
    public ResponseEntity<WeddingResponse> createWedding(@RequestBody WeddingRequest request) {
        WeddingResponse response = weddingService.createWedding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}")
    public ResponseEntity<WeddingResponse> getWedding(@PathVariable Long weddingId) {
        WeddingResponse response = weddingService.getWedding(weddingId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{weddingId}")
    public ResponseEntity<WeddingResponse> updateWedding(
            @PathVariable Long weddingId,
            @RequestBody WeddingRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        WeddingResponse response = weddingService.updateWedding(weddingId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{weddingId}")
    public ResponseEntity<Void> deleteWedding(
            @PathVariable Long weddingId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteWedding(weddingId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-invitation")
    public ResponseEntity<CheckInvitationResponse> checkNickname(
            @Valid @RequestBody CheckInvitationRequest request
    ) {
        boolean exists = weddingService.checkInvitationIdExists(request.invitationId());
        return ResponseEntity.ok(new CheckInvitationResponse(exists));
    }

    // ==================== 전체 정보 조회 ====================

    @GetMapping("/{invitationId}/info")
    public ResponseEntity<WeddingInfoResponse> getWeddingInfo(@PathVariable String invitationId) {
        WeddingInfoResponse response = weddingService.getWeddingInfo(invitationId);
        return ResponseEntity.ok(response);
    }

    // ==================== Couple ====================

    @PostMapping("/{weddingId}/couples")
    public ResponseEntity<CoupleResponse> createCouple(
            @PathVariable Long weddingId,
            @RequestBody CoupleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CoupleResponse response = weddingService.createCouple(weddingId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}/couples")
    public ResponseEntity<List<CoupleResponse>> getCouplesByWedding(@PathVariable Long weddingId) {
        List<CoupleResponse> responses = weddingService.getCouplesByWedding(weddingId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/couples/{coupleId}")
    public ResponseEntity<CoupleResponse> updateCouple(
            @PathVariable Long coupleId,
            @RequestBody CoupleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        CoupleResponse response = weddingService.updateCouple(coupleId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/couples/{coupleId}")
    public ResponseEntity<Void> deleteCouple(
            @PathVariable Long coupleId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteCouple(coupleId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Schedule ====================

    @PostMapping("/{weddingId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @PathVariable Long weddingId,
            @RequestBody ScheduleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ScheduleResponse response = weddingService.createSchedule(weddingId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedulesByWedding(@PathVariable Long weddingId) {
        List<ScheduleResponse> responses = weddingService.getSchedulesByWedding(weddingId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        ScheduleResponse response = weddingService.updateSchedule(scheduleId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== AccountGroup ====================

    @PostMapping("/{weddingId}/account-groups")
    public ResponseEntity<AccountGroupResponse> createAccountGroup(
            @PathVariable Long weddingId,
            @RequestBody AccountGroupRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AccountGroupResponse response = weddingService.createAccountGroup(weddingId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}/account-groups")
    public ResponseEntity<List<AccountGroupResponse>> getAccountGroupsByWedding(@PathVariable Long weddingId) {
        List<AccountGroupResponse> responses = weddingService.getAccountGroupsByWedding(weddingId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/account-groups/{accountGroupId}")
    public ResponseEntity<AccountGroupResponse> updateAccountGroup(
            @PathVariable Long accountGroupId,
            @RequestBody AccountGroupRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AccountGroupResponse response = weddingService.updateAccountGroup(accountGroupId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/account-groups/{accountGroupId}")
    public ResponseEntity<Void> deleteAccountGroup(
            @PathVariable Long accountGroupId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteAccountGroup(accountGroupId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Account ====================

    @PostMapping("/account-groups/{accountGroupId}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable Long accountGroupId,
            @RequestBody AccountRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AccountResponse response = weddingService.createAccount(accountGroupId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account-groups/{accountGroupId}/accounts")
    public ResponseEntity<List<AccountResponse>> getAccountsByGroup(@PathVariable Long accountGroupId) {
        List<AccountResponse> responses = weddingService.getAccountsByGroup(accountGroupId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AccountResponse response = weddingService.updateAccount(accountId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteAccount(accountId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Gallery ====================

    @PostMapping("/{weddingId}/galleries")
    public ResponseEntity<GalleryResponse> createGallery(
            @PathVariable Long weddingId,
            @RequestBody GalleryRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        GalleryResponse response = weddingService.createGallery(weddingId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}/galleries")
    public ResponseEntity<List<GalleryResponse>> getGalleriesByWedding(@PathVariable Long weddingId) {
        List<GalleryResponse> responses = weddingService.getGalleriesByWedding(weddingId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/galleries/{galleryId}")
    public ResponseEntity<GalleryResponse> updateGallery(
            @PathVariable Long galleryId,
            @RequestBody GalleryRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        GalleryResponse response = weddingService.updateGallery(galleryId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/galleries/{galleryId}")
    public ResponseEntity<Void> deleteGallery(
            @PathVariable Long galleryId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteGallery(galleryId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Transportation ====================

    @PostMapping("/{weddingId}/transportation")
    public ResponseEntity<TransportationResponse> createTransportation(
            @PathVariable Long weddingId,
            @RequestBody TransportationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        TransportationResponse response = weddingService.createTransportation(weddingId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}/transportation")
    public ResponseEntity<List<TransportationResponse>> getTransportationsByWedding(@PathVariable Long weddingId) {
        List<TransportationResponse> responses = weddingService.getTransportationsByWedding(weddingId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/transportation/{transportationId}")
    public ResponseEntity<TransportationResponse> updateTransportation(
            @PathVariable Long transportationId,
            @RequestBody TransportationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        TransportationResponse response = weddingService.updateTransportation(transportationId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/transportation/{transportationId}")
    public ResponseEntity<Void> deleteTransportation(
            @PathVariable Long transportationId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteTransportation(transportationId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Accommodation ====================

    @PostMapping("/{weddingId}/accommodations")
    public ResponseEntity<AccommodationResponse> createAccommodation(
            @PathVariable Long weddingId,
            @RequestBody AccommodationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AccommodationResponse response = weddingService.createAccommodation(weddingId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}/accommodations")
    public ResponseEntity<List<AccommodationResponse>> getAccommodationsByWedding(@PathVariable Long weddingId) {
        List<AccommodationResponse> responses = weddingService.getAccommodationsByWedding(weddingId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/accommodations/{accommodationId}")
    public ResponseEntity<AccommodationResponse> updateAccommodation(
            @PathVariable Long accommodationId,
            @RequestBody AccommodationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AccommodationResponse response = weddingService.updateAccommodation(accommodationId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/accommodations/{accommodationId}")
    public ResponseEntity<Void> deleteAccommodation(
            @PathVariable Long accommodationId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteAccommodation(accommodationId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Announcement ====================

    @PostMapping("/{weddingId}/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @PathVariable Long weddingId,
            @RequestBody AnnouncementRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AnnouncementResponse response = weddingService.createAnnouncement(weddingId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{weddingId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncementsByWedding(@PathVariable Long weddingId) {
        List<AnnouncementResponse> responses = weddingService.getAnnouncementsByWedding(weddingId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/announcements/{announcementId}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        AnnouncementResponse response = weddingService.updateAnnouncement(announcementId, userId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/announcements/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long announcementId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        weddingService.deleteAnnouncement(announcementId, userId);
        return ResponseEntity.noContent().build();
    }
}

package com.soaengry.moment.domain.wedding.controller;

import com.soaengry.moment.domain.wedding.dto.request.*;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.service.WeddingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
            @RequestBody WeddingRequest request) {
        WeddingResponse response = weddingService.updateWedding(weddingId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{weddingId}")
    public ResponseEntity<Void> deleteWedding(@PathVariable Long weddingId) {
        weddingService.deleteWedding(weddingId);
        return ResponseEntity.noContent().build();
    }

    // ==================== 전체 정보 조회 ====================

    @GetMapping("/{weddingId}/info")
    public ResponseEntity<WeddingInfoResponse> getWeddingInfo(@PathVariable Long weddingId) {
        WeddingInfoResponse response = weddingService.getWeddingInfo(weddingId);
        return ResponseEntity.ok(response);
    }

    // ==================== Couple ====================

    @PostMapping("/{weddingId}/couples")
    public ResponseEntity<CoupleResponse> createCouple(
            @PathVariable Long weddingId,
            @RequestBody CoupleRequest request) {
        CoupleResponse response = weddingService.createCouple(weddingId, request);
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
            @RequestBody CoupleRequest request) {
        CoupleResponse response = weddingService.updateCouple(coupleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/couples/{coupleId}")
    public ResponseEntity<Void> deleteCouple(@PathVariable Long coupleId) {
        weddingService.deleteCouple(coupleId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Schedule ====================

    @PostMapping("/{weddingId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @PathVariable Long weddingId,
            @RequestBody ScheduleRequest request) {
        ScheduleResponse response = weddingService.createSchedule(weddingId, request);
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
            @RequestBody ScheduleRequest request) {
        ScheduleResponse response = weddingService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(@PathVariable Long scheduleId) {
        weddingService.deleteSchedule(scheduleId);
        return ResponseEntity.noContent().build();
    }

    // ==================== AccountGroup ====================

    @PostMapping("/{weddingId}/account-groups")
    public ResponseEntity<AccountGroupResponse> createAccountGroup(
            @PathVariable Long weddingId,
            @RequestBody AccountGroupRequest request) {
        AccountGroupResponse response = weddingService.createAccountGroup(weddingId, request);
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
            @RequestBody AccountGroupRequest request) {
        AccountGroupResponse response = weddingService.updateAccountGroup(accountGroupId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/account-groups/{accountGroupId}")
    public ResponseEntity<Void> deleteAccountGroup(@PathVariable Long accountGroupId) {
        weddingService.deleteAccountGroup(accountGroupId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Account ====================

    @PostMapping("/account-groups/{accountGroupId}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable Long accountGroupId,
            @RequestBody AccountRequest request) {
        AccountResponse response = weddingService.createAccount(accountGroupId, request);
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
            @RequestBody AccountRequest request) {
        AccountResponse response = weddingService.updateAccount(accountId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        weddingService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Gallery ====================

    @PostMapping("/{weddingId}/galleries")
    public ResponseEntity<GalleryResponse> createGallery(
            @PathVariable Long weddingId,
            @RequestBody GalleryRequest request) {
        GalleryResponse response = weddingService.createGallery(weddingId, request);
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
            @RequestBody GalleryRequest request) {
        GalleryResponse response = weddingService.updateGallery(galleryId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/galleries/{galleryId}")
    public ResponseEntity<Void> deleteGallery(@PathVariable Long galleryId) {
        weddingService.deleteGallery(galleryId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Transportation ====================

    @PostMapping("/{weddingId}/transportation")
    public ResponseEntity<TransportationResponse> createTransportation(
            @PathVariable Long weddingId,
            @RequestBody TransportationRequest request) {
        TransportationResponse response = weddingService.createTransportation(weddingId, request);
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
            @RequestBody TransportationRequest request) {
        TransportationResponse response = weddingService.updateTransportation(transportationId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/transportation/{transportationId}")
    public ResponseEntity<Void> deleteTransportation(@PathVariable Long transportationId) {
        weddingService.deleteTransportation(transportationId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Accommodation ====================

    @PostMapping("/{weddingId}/accommodations")
    public ResponseEntity<AccommodationResponse> createAccommodation(
            @PathVariable Long weddingId,
            @RequestBody AccommodationRequest request) {
        AccommodationResponse response = weddingService.createAccommodation(weddingId, request);
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
            @RequestBody AccommodationRequest request) {
        AccommodationResponse response = weddingService.updateAccommodation(accommodationId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/accommodations/{accommodationId}")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long accommodationId) {
        weddingService.deleteAccommodation(accommodationId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Announcement ====================

    @PostMapping("/{weddingId}/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @PathVariable Long weddingId,
            @RequestBody AnnouncementRequest request) {
        AnnouncementResponse response = weddingService.createAnnouncement(weddingId, request);
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
            @RequestBody AnnouncementRequest request) {
        AnnouncementResponse response = weddingService.updateAnnouncement(announcementId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/announcements/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(@PathVariable Long announcementId) {
        weddingService.deleteAnnouncement(announcementId);
        return ResponseEntity.noContent().build();
    }
}
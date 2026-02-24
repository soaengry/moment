package com.soaengry.moment.domain.wedding.controller;

import com.soaengry.moment.domain.wedding.dto.request.*;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.service.WeddingService;
import com.soaengry.moment.global.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
    public ResponseEntity<ApiResponse<WeddingResponse>> createWedding(@RequestBody WeddingRequest request) {
        WeddingResponse response = weddingService.createWedding(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}")
    public ResponseEntity<ApiResponse<WeddingResponse>> getWedding(@PathVariable Long weddingId) {
        WeddingResponse response = weddingService.getWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{weddingId}")
    public ResponseEntity<ApiResponse<WeddingResponse>> updateWedding(
            @PathVariable Long weddingId,
            @RequestBody WeddingRequest request) {
        WeddingResponse response = weddingService.updateWedding(weddingId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{weddingId}")
    public ResponseEntity<ApiResponse<Void>> deleteWedding(@PathVariable Long weddingId) {
        weddingService.deleteWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/check-invitation")
    public ResponseEntity<ApiResponse<CheckInvitationResponse>> checkNickname(
            @Valid @RequestBody CheckInvitationRequest request
    ) {
        boolean exists = weddingService.checkInvitationIdExists(request.invitationId());
        return ResponseEntity.ok(ApiResponse.success(new CheckInvitationResponse(exists)));
    }

    // ==================== 전체 정보 조회 ====================

    @GetMapping("/{invitationId}/info")
    public ResponseEntity<ApiResponse<WeddingInfoResponse>> getWeddingInfo(@PathVariable String invitationId) {
        WeddingInfoResponse response = weddingService.getWeddingInfo(invitationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ==================== Couple ====================

    @PostMapping("/{weddingId}/couples")
    public ResponseEntity<ApiResponse<CoupleResponse>> createCouple(
            @PathVariable Long weddingId,
            @RequestBody CoupleRequest request) {
        CoupleResponse response = weddingService.createCouple(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}/couples")
    public ResponseEntity<ApiResponse<List<CoupleResponse>>> getCouplesByWedding(@PathVariable Long weddingId) {
        List<CoupleResponse> responses = weddingService.getCouplesByWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/couples/{coupleId}")
    public ResponseEntity<ApiResponse<CoupleResponse>> updateCouple(
            @PathVariable Long coupleId,
            @RequestBody CoupleRequest request) {
        CoupleResponse response = weddingService.updateCouple(coupleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/couples/{coupleId}")
    public ResponseEntity<ApiResponse<Void>> deleteCouple(@PathVariable Long coupleId) {
        weddingService.deleteCouple(coupleId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== Schedule ====================

    @PostMapping("/{weddingId}/schedules")
    public ResponseEntity<ApiResponse<ScheduleResponse>> createSchedule(
            @PathVariable Long weddingId,
            @RequestBody ScheduleRequest request) {
        ScheduleResponse response = weddingService.createSchedule(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}/schedules")
    public ResponseEntity<ApiResponse<List<ScheduleResponse>>> getSchedulesByWedding(@PathVariable Long weddingId) {
        List<ScheduleResponse> responses = weddingService.getSchedulesByWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<ScheduleResponse>> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequest request) {
        ScheduleResponse response = weddingService.updateSchedule(scheduleId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable Long scheduleId) {
        weddingService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== AccountGroup ====================

    @PostMapping("/{weddingId}/account-groups")
    public ResponseEntity<ApiResponse<AccountGroupResponse>> createAccountGroup(
            @PathVariable Long weddingId,
            @RequestBody AccountGroupRequest request) {
        AccountGroupResponse response = weddingService.createAccountGroup(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}/account-groups")
    public ResponseEntity<ApiResponse<List<AccountGroupResponse>>> getAccountGroupsByWedding(@PathVariable Long weddingId) {
        List<AccountGroupResponse> responses = weddingService.getAccountGroupsByWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/account-groups/{accountGroupId}")
    public ResponseEntity<ApiResponse<AccountGroupResponse>> updateAccountGroup(
            @PathVariable Long accountGroupId,
            @RequestBody AccountGroupRequest request) {
        AccountGroupResponse response = weddingService.updateAccountGroup(accountGroupId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/account-groups/{accountGroupId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccountGroup(@PathVariable Long accountGroupId) {
        weddingService.deleteAccountGroup(accountGroupId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== Account ====================

    @PostMapping("/account-groups/{accountGroupId}/accounts")
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @PathVariable Long accountGroupId,
            @RequestBody AccountRequest request) {
        AccountResponse response = weddingService.createAccount(accountGroupId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/account-groups/{accountGroupId}/accounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByGroup(@PathVariable Long accountGroupId) {
        List<AccountResponse> responses = weddingService.getAccountsByGroup(accountGroupId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<AccountResponse>> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountRequest request) {
        AccountResponse response = weddingService.updateAccount(accountId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long accountId) {
        weddingService.deleteAccount(accountId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== Gallery ====================

    @PostMapping("/{weddingId}/galleries")
    public ResponseEntity<ApiResponse<GalleryResponse>> createGallery(
            @PathVariable Long weddingId,
            @RequestBody GalleryRequest request) {
        GalleryResponse response = weddingService.createGallery(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}/galleries")
    public ResponseEntity<ApiResponse<List<GalleryResponse>>> getGalleriesByWedding(@PathVariable Long weddingId) {
        List<GalleryResponse> responses = weddingService.getGalleriesByWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/galleries/{galleryId}")
    public ResponseEntity<ApiResponse<GalleryResponse>> updateGallery(
            @PathVariable Long galleryId,
            @RequestBody GalleryRequest request) {
        GalleryResponse response = weddingService.updateGallery(galleryId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/galleries/{galleryId}")
    public ResponseEntity<ApiResponse<Void>> deleteGallery(@PathVariable Long galleryId) {
        weddingService.deleteGallery(galleryId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== Transportation ====================

    @PostMapping("/{weddingId}/transportation")
    public ResponseEntity<ApiResponse<TransportationResponse>> createTransportation(
            @PathVariable Long weddingId,
            @RequestBody TransportationRequest request) {
        TransportationResponse response = weddingService.createTransportation(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}/transportation")
    public ResponseEntity<ApiResponse<List<TransportationResponse>>> getTransportationsByWedding(@PathVariable Long weddingId) {
        List<TransportationResponse> responses = weddingService.getTransportationsByWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/transportation/{transportationId}")
    public ResponseEntity<ApiResponse<TransportationResponse>> updateTransportation(
            @PathVariable Long transportationId,
            @RequestBody TransportationRequest request) {
        TransportationResponse response = weddingService.updateTransportation(transportationId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/transportation/{transportationId}")
    public ResponseEntity<ApiResponse<Void>> deleteTransportation(@PathVariable Long transportationId) {
        weddingService.deleteTransportation(transportationId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== Accommodation ====================

    @PostMapping("/{weddingId}/accommodations")
    public ResponseEntity<ApiResponse<AccommodationResponse>> createAccommodation(
            @PathVariable Long weddingId,
            @RequestBody AccommodationRequest request) {
        AccommodationResponse response = weddingService.createAccommodation(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}/accommodations")
    public ResponseEntity<ApiResponse<List<AccommodationResponse>>> getAccommodationsByWedding(@PathVariable Long weddingId) {
        List<AccommodationResponse> responses = weddingService.getAccommodationsByWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/accommodations/{accommodationId}")
    public ResponseEntity<ApiResponse<AccommodationResponse>> updateAccommodation(
            @PathVariable Long accommodationId,
            @RequestBody AccommodationRequest request) {
        AccommodationResponse response = weddingService.updateAccommodation(accommodationId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/accommodations/{accommodationId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccommodation(@PathVariable Long accommodationId) {
        weddingService.deleteAccommodation(accommodationId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // ==================== Announcement ====================

    @PostMapping("/{weddingId}/announcements")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> createAnnouncement(
            @PathVariable Long weddingId,
            @RequestBody AnnouncementRequest request) {
        AnnouncementResponse response = weddingService.createAnnouncement(weddingId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @GetMapping("/{weddingId}/announcements")
    public ResponseEntity<ApiResponse<List<AnnouncementResponse>>> getAnnouncementsByWedding(@PathVariable Long weddingId) {
        List<AnnouncementResponse> responses = weddingService.getAnnouncementsByWedding(weddingId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/announcements/{announcementId}")
    public ResponseEntity<ApiResponse<AnnouncementResponse>> updateAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementRequest request) {
        AnnouncementResponse response = weddingService.updateAnnouncement(announcementId, request);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/announcements/{announcementId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnnouncement(@PathVariable Long announcementId) {
        weddingService.deleteAnnouncement(announcementId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}


record CheckInvitationResponse(boolean exists) {
}


record CheckInvitationRequest(
        @NotBlank
        String invitationId
) {
}
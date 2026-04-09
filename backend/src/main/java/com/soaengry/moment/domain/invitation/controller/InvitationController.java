package com.soaengry.moment.domain.invitation.controller;

import com.soaengry.moment.domain.invitation.dto.request.*;
import com.soaengry.moment.domain.invitation.dto.response.*;
import com.soaengry.moment.domain.invitation.entity.TemplateType;
import com.soaengry.moment.domain.invitation.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/invitations")
@RequiredArgsConstructor
public class InvitationController {

    private final InvitationService invitationService;
    private final TemplateService templateService;
    private final CoupleService coupleService;
    private final ScheduleService scheduleService;
    private final AccountService accountService;
    private final GalleryService galleryService;
    private final TransportationService transportationService;
    private final AccommodationService accommodationService;
    private final AnnouncementService announcementService;

    // ==================== Templates ====================

    @GetMapping("/templates")
    public ResponseEntity<List<TemplateResponse>> getAllTemplates() {
        return ResponseEntity.ok(templateService.getAllTemplates());
    }

    @GetMapping("/templates/{type}")
    public ResponseEntity<TemplateResponse> getTemplateByType(@PathVariable TemplateType type) {
        return ResponseEntity.ok(templateService.getTemplateByType(type));
    }

    // ==================== Invitation ====================

    @PostMapping
    public ResponseEntity<InvitationResponse> createInvitation(@RequestBody InvitationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.createInvitation(request));
    }

    @GetMapping("/{invitationId}")
    public ResponseEntity<InvitationResponse> getInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(invitationService.getInvitation(invitationId, userId));
    }

    @PutMapping("/{invitationId}")
    public ResponseEntity<InvitationResponse> updateInvitation(
            @PathVariable Long invitationId,
            @RequestBody InvitationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(invitationService.updateInvitation(invitationId, userId, request));
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> deleteInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        invitationService.deleteInvitation(invitationId, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/check-invitation")
    public ResponseEntity<CheckInvitationResponse> checkInvitationId(
            @Valid @RequestBody CheckInvitationRequest request) {
        return ResponseEntity.ok(new CheckInvitationResponse(invitationService.checkInvitationIdExists(request.invitationId())));
    }

    @GetMapping("/{invitationId}/info")
    public ResponseEntity<InvitationInfoResponse> getInvitationInfo(
            @PathVariable String invitationId,
            Authentication authentication) {
        Long userId = authentication != null ? (Long) authentication.getPrincipal() : null;
        return ResponseEntity.ok(invitationService.getInvitationInfo(invitationId, userId));
    }

    // ==================== Couple ====================

    @PostMapping("/{invitationId}/couples")
    public ResponseEntity<CoupleResponse> createCouple(
            @PathVariable Long invitationId,
            @RequestBody CoupleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(coupleService.createCouple(invitationId, userId, request));
    }

    @GetMapping("/{invitationId}/couples")
    public ResponseEntity<List<CoupleResponse>> getCouples(@PathVariable Long invitationId) {
        return ResponseEntity.ok(coupleService.getCouplesByInvitation(invitationId));
    }

    @PutMapping("/couples/{coupleId}")
    public ResponseEntity<CoupleResponse> updateCouple(
            @PathVariable Long coupleId,
            @RequestBody CoupleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(coupleService.updateCouple(coupleId, userId, request));
    }

    @DeleteMapping("/couples/{coupleId}")
    public ResponseEntity<Void> deleteCouple(
            @PathVariable Long coupleId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        coupleService.deleteCouple(coupleId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Schedule ====================

    @PostMapping("/{invitationId}/schedules")
    public ResponseEntity<ScheduleResponse> createSchedule(
            @PathVariable Long invitationId,
            @RequestBody ScheduleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(scheduleService.createSchedule(invitationId, userId, request));
    }

    @GetMapping("/{invitationId}/schedules")
    public ResponseEntity<List<ScheduleResponse>> getSchedules(@PathVariable Long invitationId) {
        return ResponseEntity.ok(scheduleService.getSchedulesByInvitation(invitationId));
    }

    @PutMapping("/schedules/{scheduleId}")
    public ResponseEntity<ScheduleResponse> updateSchedule(
            @PathVariable Long scheduleId,
            @RequestBody ScheduleRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(scheduleService.updateSchedule(scheduleId, userId, request));
    }

    @DeleteMapping("/schedules/{scheduleId}")
    public ResponseEntity<Void> deleteSchedule(
            @PathVariable Long scheduleId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        scheduleService.deleteSchedule(scheduleId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== AccountGroup ====================

    @PostMapping("/{invitationId}/account-groups")
    public ResponseEntity<AccountGroupResponse> createAccountGroup(
            @PathVariable Long invitationId,
            @RequestBody AccountGroupRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccountGroup(invitationId, userId, request));
    }

    @GetMapping("/{invitationId}/account-groups")
    public ResponseEntity<List<AccountGroupResponse>> getAccountGroups(@PathVariable Long invitationId) {
        return ResponseEntity.ok(accountService.getAccountGroupsByInvitation(invitationId));
    }

    @PutMapping("/account-groups/{accountGroupId}")
    public ResponseEntity<AccountGroupResponse> updateAccountGroup(
            @PathVariable Long accountGroupId,
            @RequestBody AccountGroupRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.updateAccountGroup(accountGroupId, userId, request));
    }

    @DeleteMapping("/account-groups/{accountGroupId}")
    public ResponseEntity<Void> deleteAccountGroup(
            @PathVariable Long accountGroupId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        accountService.deleteAccountGroup(accountGroupId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Account ====================

    @PostMapping("/account-groups/{accountGroupId}/accounts")
    public ResponseEntity<AccountResponse> createAccount(
            @PathVariable Long accountGroupId,
            @RequestBody AccountRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(accountService.createAccount(accountGroupId, userId, request));
    }

    @GetMapping("/account-groups/{accountGroupId}/accounts")
    public ResponseEntity<List<AccountResponse>> getAccounts(@PathVariable Long accountGroupId) {
        return ResponseEntity.ok(accountService.getAccountsByGroup(accountGroupId));
    }

    @PutMapping("/accounts/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Long accountId,
            @RequestBody AccountRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(accountService.updateAccount(accountId, userId, request));
    }

    @DeleteMapping("/accounts/{accountId}")
    public ResponseEntity<Void> deleteAccount(
            @PathVariable Long accountId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        accountService.deleteAccount(accountId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Gallery ====================

    @PostMapping("/{invitationId}/galleries")
    public ResponseEntity<GalleryResponse> createGallery(
            @PathVariable Long invitationId,
            @RequestBody GalleryRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(galleryService.createGallery(invitationId, userId, request));
    }

    @GetMapping("/{invitationId}/galleries")
    public ResponseEntity<List<GalleryResponse>> getGalleries(@PathVariable Long invitationId) {
        return ResponseEntity.ok(galleryService.getGalleriesByInvitation(invitationId));
    }

    @PutMapping("/galleries/{galleryId}")
    public ResponseEntity<GalleryResponse> updateGallery(
            @PathVariable Long galleryId,
            @RequestBody GalleryRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(galleryService.updateGallery(galleryId, userId, request));
    }

    @DeleteMapping("/galleries/{galleryId}")
    public ResponseEntity<Void> deleteGallery(
            @PathVariable Long galleryId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        galleryService.deleteGallery(galleryId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Transportation ====================

    @PostMapping("/{invitationId}/transportation")
    public ResponseEntity<TransportationResponse> createTransportation(
            @PathVariable Long invitationId,
            @RequestBody TransportationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(transportationService.createTransportation(invitationId, userId, request));
    }

    @GetMapping("/{invitationId}/transportation")
    public ResponseEntity<List<TransportationResponse>> getTransportations(@PathVariable Long invitationId) {
        return ResponseEntity.ok(transportationService.getTransportationsByInvitation(invitationId));
    }

    @PutMapping("/transportation/{transportationId}")
    public ResponseEntity<TransportationResponse> updateTransportation(
            @PathVariable Long transportationId,
            @RequestBody TransportationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(transportationService.updateTransportation(transportationId, userId, request));
    }

    @DeleteMapping("/transportation/{transportationId}")
    public ResponseEntity<Void> deleteTransportation(
            @PathVariable Long transportationId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        transportationService.deleteTransportation(transportationId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Accommodation ====================

    @PostMapping("/{invitationId}/accommodations")
    public ResponseEntity<AccommodationResponse> createAccommodation(
            @PathVariable Long invitationId,
            @RequestBody AccommodationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(accommodationService.createAccommodation(invitationId, userId, request));
    }

    @GetMapping("/{invitationId}/accommodations")
    public ResponseEntity<List<AccommodationResponse>> getAccommodations(@PathVariable Long invitationId) {
        return ResponseEntity.ok(accommodationService.getAccommodationsByInvitation(invitationId));
    }

    @PutMapping("/accommodations/{accommodationId}")
    public ResponseEntity<AccommodationResponse> updateAccommodation(
            @PathVariable Long accommodationId,
            @RequestBody AccommodationRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(accommodationService.updateAccommodation(accommodationId, userId, request));
    }

    @DeleteMapping("/accommodations/{accommodationId}")
    public ResponseEntity<Void> deleteAccommodation(
            @PathVariable Long accommodationId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        accommodationService.deleteAccommodation(accommodationId, userId);
        return ResponseEntity.noContent().build();
    }

    // ==================== Announcement ====================

    @PostMapping("/{invitationId}/announcements")
    public ResponseEntity<AnnouncementResponse> createAnnouncement(
            @PathVariable Long invitationId,
            @RequestBody AnnouncementRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(announcementService.createAnnouncement(invitationId, userId, request));
    }

    @GetMapping("/{invitationId}/announcements")
    public ResponseEntity<List<AnnouncementResponse>> getAnnouncements(@PathVariable Long invitationId) {
        return ResponseEntity.ok(announcementService.getAnnouncementsByInvitation(invitationId));
    }

    @PutMapping("/announcements/{announcementId}")
    public ResponseEntity<AnnouncementResponse> updateAnnouncement(
            @PathVariable Long announcementId,
            @RequestBody AnnouncementRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(announcementService.updateAnnouncement(announcementId, userId, request));
    }

    @DeleteMapping("/announcements/{announcementId}")
    public ResponseEntity<Void> deleteAnnouncement(
            @PathVariable Long announcementId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        announcementService.deleteAnnouncement(announcementId, userId);
        return ResponseEntity.noContent().build();
    }
}

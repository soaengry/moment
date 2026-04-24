package com.soaengry.moment.domain.invitation.controller;

import com.soaengry.moment.domain.invitation.dto.request.InvitationCreateRequest;
import com.soaengry.moment.domain.invitation.dto.request.InvitationStatusRequest;
import com.soaengry.moment.domain.invitation.dto.response.InvitationResponse;
import com.soaengry.moment.domain.invitation.service.InvitationService;
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

    @PostMapping
    public ResponseEntity<InvitationResponse> createInvitation(
            @RequestBody InvitationCreateRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.status(HttpStatus.CREATED).body(invitationService.createInvitation(userId, request));
    }

    @GetMapping("/events/{eventId}")
    public ResponseEntity<List<InvitationResponse>> getInvitationsByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(invitationService.getInvitationsByEvent(eventId));
    }

    @PutMapping("/{invitationId}/status")
    public ResponseEntity<InvitationResponse> updateStatus(
            @PathVariable Long invitationId,
            @RequestBody InvitationStatusRequest request,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(invitationService.updateStatus(invitationId, userId, request));
    }

    @DeleteMapping("/{invitationId}")
    public ResponseEntity<Void> deleteInvitation(
            @PathVariable Long invitationId,
            Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        invitationService.deleteInvitation(invitationId, userId);
        return ResponseEntity.noContent().build();
    }
}

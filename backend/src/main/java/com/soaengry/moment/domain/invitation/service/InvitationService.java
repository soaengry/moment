package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.invitation.dto.request.InvitationCreateRequest;
import com.soaengry.moment.domain.invitation.dto.request.InvitationStatusRequest;
import com.soaengry.moment.domain.invitation.dto.response.InvitationResponse;
import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.InvitationRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public InvitationResponse createInvitation(Long requesterId, InvitationCreateRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));

        if (!event.getUserId().equals(requesterId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }

        User invitee = userRepository.findById(request.userId())
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));

        if (invitationRepository.existsByEventIdAndUserId(request.eventId(), request.userId())) {
            throw new InvitationException(InvitationErrorCode.INVITATION_ALREADY_EXISTS);
        }

        Invitation invitation = Invitation.create(event, invitee);
        return InvitationResponse.from(invitationRepository.save(invitation));
    }

    @Transactional(readOnly = true)
    public List<InvitationResponse> getInvitationsByEvent(Long eventId) {
        return invitationRepository.findByEventId(eventId).stream()
                .map(InvitationResponse::from)
                .toList();
    }

    public InvitationResponse updateStatus(Long invitationId, Long userId, InvitationStatusRequest request) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));

        if (!invitation.getUser().getId().equals(userId)) {
            throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);
        }

        invitation.updateStatus(request.status());
        return InvitationResponse.from(invitation);
    }

    public void deleteInvitation(Long invitationId, Long requesterId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.INVITATION_NOT_FOUND));

        boolean isEventOwner = invitation.getEvent().getUserId().equals(requesterId);
        boolean isInvitee = invitation.getUser().getId().equals(requesterId);

        if (!isEventOwner && !isInvitee) {
            throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);
        }

        invitationRepository.deleteById(invitationId);
    }
}

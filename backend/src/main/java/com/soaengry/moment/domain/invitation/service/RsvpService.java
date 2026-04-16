package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.request.RsvpRequest;
import com.soaengry.moment.domain.invitation.dto.response.RsvpResponse;
import com.soaengry.moment.domain.invitation.entity.Rsvp;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.RsvpRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class RsvpService {

    private final RsvpRepository rsvpRepository;
    private final WeddingRepository weddingRepository;
    private final UserRepository userRepository;

    public RsvpResponse createRsvp(Long weddingId, Long userId, RsvpRequest request) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        Rsvp rsvp = Rsvp.builder()
                .wedding(wedding)
                .sessionId(request.sessionId())
                .user(user)
                .attendance(request.attendance())
                .name(request.name())
                .side(request.side())
                .phone(request.phone())
                .attendeeCount(request.attendeeCount())
                .willEat(request.willEat())
                .mealCount(request.mealCount())
                .willRide(request.willRide())
                .rideCount(request.rideCount())
                .note(request.note())
                .consent(request.consent())
                .build();

        return RsvpResponse.from(rsvpRepository.save(rsvp));
    }

    @Transactional(readOnly = true)
    public List<RsvpResponse> getRsvps(Long weddingId, Long requesterId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        if (!wedding.getEvent().getUser().getId().equals(requesterId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_UNAUTHORIZED);
        }

        return rsvpRepository.findByWeddingId(weddingId).stream()
                .map(RsvpResponse::from)
                .toList();
    }

    public void deleteRsvp(Long rsvpId, Long requesterId) {
        Rsvp rsvp = rsvpRepository.findById(rsvpId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.RSVP_NOT_FOUND));

        boolean isEventOwner = rsvp.getWedding().getEvent().getUser().getId().equals(requesterId);
        if (!isEventOwner) {
            throw new InvitationException(InvitationErrorCode.INVITATION_UNAUTHORIZED);
        }

        rsvpRepository.deleteById(rsvpId);
    }
}

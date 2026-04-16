package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.invitation.entity.InvitationStatus;

public record InvitationResponse(
        Long id,
        Long eventId,
        Long userId,
        InvitationStatus status
) {
    public static InvitationResponse from(Invitation invitation) {
        return new InvitationResponse(
                invitation.getId(),
                invitation.getEvent().getId(),
                invitation.getUser().getId(),
                invitation.getStatus()
        );
    }
}

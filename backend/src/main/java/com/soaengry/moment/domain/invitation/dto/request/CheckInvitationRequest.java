package com.soaengry.moment.domain.invitation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckInvitationRequest(
        @NotBlank
        String invitationId
) {
}

package com.soaengry.moment.domain.wedding.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CheckInvitationRequest(
        @NotBlank
        String invitationId
) {
}

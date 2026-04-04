package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Transportation;

public record TransportationResponse(
        Long id,
        Long invitationId,
        Transportation.TransportType type,
        String title,
        String description,
        Integer orderIndex
) {
    public static TransportationResponse from(Transportation transportation) {
        return new TransportationResponse(
                transportation.getId(),
                transportation.getInvitationId(),
                transportation.getType(),
                transportation.getTitle(),
                transportation.getDescription(),
                transportation.getOrderIndex()
        );
    }
}

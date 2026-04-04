package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Transportation;

public record TransportationRequest(
        Transportation.TransportType type,
        String title,
        String description,
        Integer orderIndex
) {
    public Transportation toEntity(Long invitationId) {
        return Transportation.create(invitationId, type, title, description, orderIndex);
    }
}

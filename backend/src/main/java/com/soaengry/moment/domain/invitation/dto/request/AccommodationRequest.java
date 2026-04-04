package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Accommodation;

public record AccommodationRequest(
        String name,
        String address,
        String phone,
        String distance,
        String priceRange,
        Integer orderIndex
) {
    public Accommodation toEntity(Long invitationId) {
        return Accommodation.create(invitationId, name, address, phone, distance, priceRange, orderIndex);
    }
}

package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Accommodation;

public record AccommodationResponse(
        Long id,
        Long invitationId,
        String name,
        String address,
        String phone,
        String distance,
        String priceRange,
        Integer orderIndex
) {
    public static AccommodationResponse from(Accommodation accommodation) {
        return new AccommodationResponse(
                accommodation.getId(),
                accommodation.getInvitationId(),
                accommodation.getName(),
                accommodation.getAddress(),
                accommodation.getPhone(),
                accommodation.getDistance(),
                accommodation.getPriceRange(),
                accommodation.getOrderIndex()
        );
    }
}

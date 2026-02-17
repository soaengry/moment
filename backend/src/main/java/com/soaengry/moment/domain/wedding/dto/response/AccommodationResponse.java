package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.Accommodation;

public record AccommodationResponse(
        Long id,
        Long weddingId,
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
                accommodation.getWeddingId(),
                accommodation.getName(),
                accommodation.getAddress(),
                accommodation.getPhone(),
                accommodation.getDistance(),
                accommodation.getPriceRange(),
                accommodation.getOrderIndex()
        );
    }
}
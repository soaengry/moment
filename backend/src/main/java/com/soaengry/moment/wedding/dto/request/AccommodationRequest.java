package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.Accommodation;

public record AccommodationRequest(
        String name,
        String address,
        String phone,
        String distance,
        String priceRange,
        Integer orderIndex
) {
    public Accommodation toEntity(Long weddingId) {
        return Accommodation.create(
                weddingId,
                name,
                address,
                phone,
                distance,
                priceRange,
                orderIndex
        );
    }
}
package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Wedding;

import java.time.LocalDateTime;

public record WeddingRequest(
        String title,
        LocalDateTime weddingDate,
        String venueName,
        String venueAddress,
        String venueDetail,
        String venuePhone,
        String mapImageUrl,
        String dressCode,
        String notice,
        String parkingInfo,
        String mealInfo
) {
    public Wedding toEntity(Double venueLat, Double venueLng) {
        return Wedding.create(
                title,
                weddingDate,
                venueName,
                venueAddress,
                venueDetail,
                venueLat,
                venueLng,
                venuePhone,
                mapImageUrl,
                dressCode,
                notice,
                parkingInfo,
                mealInfo
        );
    }
}
package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.Wedding;

import java.time.LocalDateTime;

public record WeddingResponse(
        Long id,
        String title,
        LocalDateTime weddingDate,
        String venueName,
        String venueAddress,
        String venueDetail,
        Double venueLat,
        Double venueLng,
        String venuePhone,
        String dressCode,
        String notice,
        String parkingInfo,
        String mealInfo
) {
    public static WeddingResponse from(Wedding wedding) {
        return new WeddingResponse(
                wedding.getId(),
                wedding.getTitle(),
                wedding.getWeddingDate(),
                wedding.getVenueName(),
                wedding.getVenueAddress(),
                wedding.getVenueDetail(),
                wedding.getVenueLat(),
                wedding.getVenueLng(),
                wedding.getVenuePhone(),
                wedding.getDressCode(),
                wedding.getNotice(),
                wedding.getParkingInfo(),
                wedding.getMealInfo()
        );
    }
}
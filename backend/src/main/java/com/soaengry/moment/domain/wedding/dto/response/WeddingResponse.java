package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.Wedding;

public record WeddingResponse(
        Long id,
        Long eventId,
        String notice,
        String parkingInfo,
        String mealInfo,
        String greeting
) {
    public static WeddingResponse from(Wedding wedding) {
        return new WeddingResponse(
                wedding.getId(),
                wedding.getEvent().getId(),
                wedding.getNotice(),
                wedding.getParkingInfo(),
                wedding.getMealInfo(),
                wedding.getGreeting()
        );
    }
}

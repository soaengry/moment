package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Wedding;

import java.time.LocalDateTime;

public record WeddingRequest(
        String title,
        String invitationId,
        LocalDateTime weddingDate,
        String venueName,
        String venueAddress,
        String venueDetail,
        String venuePhone,
        String dressCode,
        String notice,
        String parkingInfo,
        String mealInfo
) {
    public Wedding toEntity(Double venueLat, Double venueLng) {
        return Wedding.builder()
                .title(title)
                .invitationId(invitationId)
                .weddingDate(weddingDate)
                .venueName(venueName)
                .venueAddress(venueAddress)
                .venueDetail(venueDetail)
                .venueLat(venueLat)
                .venueLng(venueLng)
                .venuePhone(venuePhone)
                .dressCode(dressCode)
                .notice(notice)
                .parkingInfo(parkingInfo)
                .mealInfo(mealInfo)
                .build();
    }
}
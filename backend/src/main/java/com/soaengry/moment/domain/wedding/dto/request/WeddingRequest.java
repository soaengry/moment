package com.soaengry.moment.domain.wedding.dto.request;

public record WeddingRequest(
        Long eventId,
        String notice,
        String parkingInfo,
        String mealInfo,
        String greeting
) {}

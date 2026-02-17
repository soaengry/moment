package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.Transportation;

public record TransportationResponse(
        Long id,
        Long weddingId,
        Transportation.TransportType type,
        String title,
        String description,
        Integer orderIndex
) {
    public static TransportationResponse from(Transportation transportation) {
        return new TransportationResponse(
                transportation.getId(),
                transportation.getWeddingId(),
                transportation.getType(),
                transportation.getTitle(),
                transportation.getDescription(),
                transportation.getOrderIndex()
        );
    }
}
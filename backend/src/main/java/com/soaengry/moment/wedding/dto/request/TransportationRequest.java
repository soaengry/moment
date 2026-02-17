package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.Transportation;

public record TransportationRequest(
        Transportation.TransportType type,
        String title,
        String description,
        Integer orderIndex
) {
    public Transportation toEntity(Long weddingId) {
        return Transportation.create(
                weddingId,
                type,
                title,
                description,
                orderIndex
        );
    }
}
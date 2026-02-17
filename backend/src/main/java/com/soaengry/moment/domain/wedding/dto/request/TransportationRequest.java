package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Transportation;

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
package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Couple;

public record CoupleRequest(
        Couple.CoupleRole role,
        String name,
        String fatherName,
        String motherName,
        Boolean isFatherAlive,
        Boolean isMotherAlive,
        String contact,
        String profileImageUrl,
        String introduction
) {
    public Couple toEntity(Long weddingId, Long userId) {
        return Couple.create(
                weddingId,
                userId,
                role,
                name,
                fatherName,
                motherName,
                isFatherAlive,
                isMotherAlive,
                contact,
                profileImageUrl,
                introduction
        );
    }
}

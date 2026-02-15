package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.Couple;

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
    public Couple toEntity(Long weddingId) {
        return Couple.create(
                weddingId,
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
package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.Couple;

public record CoupleResponse(
        Long id,
        Long weddingId,
        Long userId,
        String email,
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
    public static CoupleResponse from(Couple couple) {
        return from(couple, null);
    }

    public static CoupleResponse from(Couple couple, Long userId) {
        return new CoupleResponse(
                couple.getId(),
                couple.getWedding().getId(),
                userId,
                couple.getEmail(),
                couple.getRole(),
                couple.getName(),
                couple.getFatherName(),
                couple.getMotherName(),
                couple.getIsFatherAlive(),
                couple.getIsMotherAlive(),
                couple.getContact(),
                couple.getProfileImageUrl(),
                couple.getIntroduction()
        );
    }
}

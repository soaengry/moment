package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Couple;
import com.soaengry.moment.domain.wedding.entity.Wedding;

public record CoupleRequest(
        Couple.CoupleRole role,
        String name,
        String email,
        String fatherName,
        String motherName,
        Boolean isFatherAlive,
        Boolean isMotherAlive,
        String contact,
        String profileImageUrl,
        String introduction
) {
    public Couple toEntity(Wedding wedding) {
        return Couple.builder()
                .wedding(wedding)
                .role(role)
                .name(name)
                .email(email)
                .fatherName(fatherName)
                .motherName(motherName)
                .isFatherAlive(isFatherAlive)
                .isMotherAlive(isMotherAlive)
                .contact(contact)
                .profileImageUrl(profileImageUrl)
                .introduction(introduction)
                .build();
    }
}

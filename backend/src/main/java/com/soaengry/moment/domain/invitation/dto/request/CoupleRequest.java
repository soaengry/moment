package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Couple;
import com.soaengry.moment.domain.invitation.entity.Invitation;

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
    public Couple toEntity(Invitation invitation) {
        return Couple.builder()
                .invitation(invitation)
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

package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.Host;

public record HostRequest(
        Host.HostRole role,
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
    public Host toEntity(Long eventId) {
        return Host.builder()
                .eventId(eventId)
                .role(role)
                .name(name)
                .email(email)
                .fatherName(fatherName)
                .motherName(motherName)
                .isFatherAlive(isFatherAlive != null ? isFatherAlive : true)
                .isMotherAlive(isMotherAlive != null ? isMotherAlive : true)
                .contact(contact)
                .profileImageUrl(profileImageUrl)
                .introduction(introduction)
                .build();
    }
}

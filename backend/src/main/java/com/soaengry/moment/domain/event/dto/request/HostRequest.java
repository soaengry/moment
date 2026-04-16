package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.entity.Host;

public record HostRequest(
        Host.HostRole role,
        String name,
        String email,
        String contact,
        String profileImageUrl,
        String introduction,
        WeddingHostData weddingHostData
) {
    public Host toEntity(Long eventId) {
        return Host.builder()
                .eventId(eventId)
                .role(role)
                .name(name)
                .email(email)
                .contact(contact)
                .profileImageUrl(profileImageUrl)
                .introduction(introduction)
                .build();
    }

    public record WeddingHostData(
            String fatherName,
            String motherName,
            Boolean isFatherAlive,
            Boolean isMotherAlive
    ) {}
}

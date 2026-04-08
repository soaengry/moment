package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.Host;

public record HostResponse(
        Long id,
        Long eventId,
        Long userId,
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
    public static HostResponse from(Host host, Long userId) {
        return new HostResponse(
                host.getId(),
                host.getEventId(),
                userId,
                host.getRole(),
                host.getName(),
                host.getEmail(),
                host.getFatherName(),
                host.getMotherName(),
                host.getIsFatherAlive(),
                host.getIsMotherAlive(),
                host.getContact(),
                host.getProfileImageUrl(),
                host.getIntroduction()
        );
    }
}

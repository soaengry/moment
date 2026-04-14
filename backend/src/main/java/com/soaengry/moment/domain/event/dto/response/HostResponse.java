package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.event.entity.Host;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HostResponse {
    private Long id;
    private Long eventId;
    private String email;
    private Host.HostRole role;
    private String name;
    private String contact;
    private String profileImageUrl;
    private String introduction;

    public static HostResponse from(Host host) {
        return HostResponse.builder()
                .id(host.getId())
                .eventId(host.getEventId())
                .email(host.getEmail())
                .role(host.getRole())
                .name(host.getName())
                .contact(host.getContact())
                .profileImageUrl(host.getProfileImageUrl())
                .introduction(host.getIntroduction())
                .build();
    }
}
package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.event.dto.response.HostResponse;
import com.soaengry.moment.domain.event.entity.Host;
import com.soaengry.moment.domain.wedding.entity.WeddingHost;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeddingHostCombinedResponse {
    private Long id;
    private Long eventId;
    private String email;
    private Host.HostRole role;
    private String name;
    private String contact;
    private String profileImageUrl;
    private String introduction;

    private String fatherName;
    private String motherName;
    private Boolean isFatherAlive;
    private Boolean isMotherAlive;

    public static WeddingHostCombinedResponse of(HostResponse host, WeddingHost weddingHost) {
        return WeddingHostCombinedResponse.builder()
                .id(host.getId())
                .eventId(host.getEventId())
                .email(host.getEmail())
                .role(host.getRole())
                .name(host.getName())
                .contact(host.getContact())
                .profileImageUrl(host.getProfileImageUrl())
                .introduction(host.getIntroduction())
                .fatherName(weddingHost != null ? weddingHost.getFatherName() : null)
                .motherName(weddingHost != null ? weddingHost.getMotherName() : null)
                .isFatherAlive(weddingHost != null ? weddingHost.getIsFatherAlive() : null)
                .isMotherAlive(weddingHost != null ? weddingHost.getIsMotherAlive() : null)
                .build();
    }
}
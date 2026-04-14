package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.event.entity.Host;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeddingHostCombinedCreateRequest {
    private Long eventId;
    private String email;
    private Host.HostRole role;
    private String name;
    private String contact;
    private String profileImageUrl;
    private String introduction;

    // 부모 정보
    private String fatherName;
    private String motherName;
    private Boolean isFatherAlive;
    private Boolean isMotherAlive;
}
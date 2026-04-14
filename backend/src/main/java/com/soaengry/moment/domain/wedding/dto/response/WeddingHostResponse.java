package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.WeddingHost;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeddingHostResponse {
    private Long id;
    private Long hostId;
    private String fatherName;
    private String motherName;
    private Boolean isFatherAlive;
    private Boolean isMotherAlive;

    public static WeddingHostResponse from(WeddingHost weddingHost) {
        return WeddingHostResponse.builder()
                .id(weddingHost.getId())
                .hostId(weddingHost.getHostId())
                .fatherName(weddingHost.getFatherName())
                .motherName(weddingHost.getMotherName())
                .isFatherAlive(weddingHost.getIsFatherAlive())
                .isMotherAlive(weddingHost.getIsMotherAlive())
                .build();
    }
}
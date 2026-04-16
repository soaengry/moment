package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.wedding.dto.response.WeddingHostResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HostWithWeddingResponse {
    private HostResponse host;
    private WeddingHostResponse weddingHost;

    public static HostWithWeddingResponse of(HostResponse host, WeddingHostResponse weddingHost) {
        return HostWithWeddingResponse.builder()
                .host(host)
                .weddingHost(weddingHost)
                .build();
    }
}
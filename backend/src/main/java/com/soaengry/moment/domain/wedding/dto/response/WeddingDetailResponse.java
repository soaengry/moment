package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.event.dto.response.EventDetailResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class WeddingDetailResponse implements EventDetailResponse {
    private String notice;
    private String parkingInfo;
    private String mealInfo;
    private String greeting;
    private List<WeddingHostResponse> weddingHosts;
}

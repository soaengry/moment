package com.soaengry.moment.domain.event.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GatheringDetailResponse implements EventDetailResponse {
    private List<HostResponse> hosts;
}
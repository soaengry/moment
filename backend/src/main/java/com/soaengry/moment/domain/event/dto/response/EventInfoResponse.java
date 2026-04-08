package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.wedding.dto.response.AccountGroupWithAccountsResponse;
import com.soaengry.moment.domain.wedding.dto.response.HostResponse;
import com.soaengry.moment.domain.wedding.dto.response.ScheduleResponse;
import com.soaengry.moment.domain.wedding.dto.response.WeddingResponse;

import java.util.List;

public record EventInfoResponse(
        EventResponse event,
        List<HeroImageResponse> heroImages,
        List<TransportationResponse> transportation,
        List<AnnouncementResponse> announcements,
        WeddingResponse wedding,
        List<HostResponse> hosts,
        List<ScheduleResponse> schedules,
        List<AccountGroupWithAccountsResponse> accountGroups
) {}

package com.soaengry.moment.domain.event.dto.response;

import java.util.List;

public record EventInfoResponse(
        EventResponse event,
        List<HeroImageResponse> heroImages,
        List<TransportationResponse> transportation,
        List<AnnouncementResponse> announcements,
        List<ScheduleResponse> schedules,
        List<AccountGroupWithAccountsResponse> accountGroups,
        EventDetailResponse detail
) {
}
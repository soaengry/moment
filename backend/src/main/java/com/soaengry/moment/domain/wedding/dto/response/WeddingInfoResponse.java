package com.soaengry.moment.domain.wedding.dto.response;

import java.util.List;

public record WeddingInfoResponse(
        WeddingResponse wedding,
        List<HostResponse> hosts,
        List<ScheduleResponse> schedules,
        List<AccountGroupWithAccountsResponse> accountGroups
) {}

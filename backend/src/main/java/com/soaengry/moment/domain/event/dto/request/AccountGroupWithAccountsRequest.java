package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.dto.request.AccountRequest;

import java.util.List;

public record AccountGroupWithAccountsRequest(
        String groupName,
        Integer orderIndex,
        List<AccountRequest> accounts
) {}

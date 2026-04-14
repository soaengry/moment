package com.soaengry.moment.domain.event.dto.response;

import java.util.List;

public record AccountGroupWithAccountsResponse(
        AccountGroupResponse group,
        List<AccountResponse> accounts
) {
    public static AccountGroupWithAccountsResponse of(AccountGroupResponse group, List<AccountResponse> accounts) {
        return new AccountGroupWithAccountsResponse(group, accounts);
    }
}

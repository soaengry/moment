package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.Account;

public record AccountRequest(
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolder,
        String kakaoPayUrl,
        Integer orderIndex
) {
    public Account toEntity(Long accountGroupId) {
        return Account.create(accountGroupId, bankName, bankCode, accountNumber, accountHolder, kakaoPayUrl, orderIndex);
    }
}

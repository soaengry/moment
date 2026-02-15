package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.Account;

public record AccountRequest(
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolder,
        String kakaoPayUrl,
        Integer orderIndex
) {
    public Account toEntity(Long accountGroupId) {
        return Account.create(
                accountGroupId,
                bankName,
                bankCode,
                accountNumber,
                accountHolder,
                kakaoPayUrl,
                orderIndex
        );
    }
}
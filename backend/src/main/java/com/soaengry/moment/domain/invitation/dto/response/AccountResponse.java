package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.Account;

public record AccountResponse(
        Long id,
        Long accountGroupId,
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolder,
        String kakaoPayUrl,
        Integer orderIndex
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountGroupId(),
                account.getBankName(),
                account.getBankCode(),
                account.getAccountNumber(),
                account.getAccountHolder(),
                account.getKakaoPayUrl(),
                account.getOrderIndex()
        );
    }
}

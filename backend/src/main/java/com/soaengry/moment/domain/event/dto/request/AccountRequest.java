package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.entity.Account;

public record AccountRequest(
        String bankName,
        String bankCode,
        String accountNumber,
        String accountHolder,
        String kakaoPayUrl,
        Integer orderIndex
) {
    // 기본 toEntity (orderIndex 직접 지정하지 않을 때)
    public Account toEntity(Long groupId) {
        return Account.builder()
                .accountGroupId(groupId)
                .bankName(bankName)
                .bankCode(bankCode)
                .accountNumber(accountNumber)
                .accountHolder(accountHolder)
                .kakaoPayUrl(kakaoPayUrl)
                .build();
    }

    // orderIndex를 인자로 받는 오버로드
    public Account toEntity(Long groupId, Integer orderIndex) {
        return Account.builder()
                .accountGroupId(groupId)
                .bankName(bankName)
                .bankCode(bankCode)
                .accountNumber(accountNumber)
                .accountHolder(accountHolder)
                .kakaoPayUrl(kakaoPayUrl)
                .orderIndex(orderIndex)
                .build();
    }
}
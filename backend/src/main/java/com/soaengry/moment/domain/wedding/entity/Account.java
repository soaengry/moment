package com.soaengry.moment.domain.wedding.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountGroupId;

    @Column(nullable = false)
    private String bankName;

    @Column(nullable = false)
    private String bankCode;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String accountNumber;

    @Column(nullable = false)
    private String accountHolder;

    private String kakaoPayUrl;

    @Column(nullable = false)
    private Integer orderIndex;

    private Account(Long accountGroupId, String bankName, String bankCode, String accountNumber,
                    String accountHolder, String kakaoPayUrl, Integer orderIndex) {
        this.accountGroupId = accountGroupId;
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.kakaoPayUrl = kakaoPayUrl;
        this.orderIndex = orderIndex;
    }

    public static Account create(Long accountGroupId, String bankName, String bankCode, String accountNumber,
                                 String accountHolder, String kakaoPayUrl, Integer orderIndex) {
        return new Account(accountGroupId, bankName, bankCode, accountNumber, accountHolder, kakaoPayUrl, orderIndex);
    }

    public void update(String bankName, String bankCode, String accountNumber, String accountHolder,
                       String kakaoPayUrl, Integer orderIndex) {
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.accountNumber = accountNumber;
        this.accountHolder = accountHolder;
        this.kakaoPayUrl = kakaoPayUrl;
        this.orderIndex = orderIndex;
    }
}
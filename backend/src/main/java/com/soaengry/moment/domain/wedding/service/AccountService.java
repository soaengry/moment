package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.wedding.dto.request.AccountGroupRequest;
import com.soaengry.moment.domain.wedding.dto.request.AccountRequest;
import com.soaengry.moment.domain.wedding.dto.response.AccountGroupResponse;
import com.soaengry.moment.domain.wedding.dto.response.AccountResponse;
import com.soaengry.moment.domain.wedding.entity.Account;
import com.soaengry.moment.domain.wedding.entity.AccountGroup;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.AccountGroupRepository;
import com.soaengry.moment.domain.wedding.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class AccountService {

    private static final int MAX_ACCOUNT_GROUPS = 4;
    private static final int MAX_ACCOUNTS_PER_GROUP = 3;

    private final AccountGroupRepository accountGroupRepository;
    private final AccountRepository accountRepository;
    private final WeddingService weddingService;

    public AccountGroupResponse createAccountGroup(Long weddingId, Long userId, AccountGroupRequest request) {
        weddingService.validateWeddingAccess(weddingId, userId);

        long count = accountGroupRepository.countByWeddingIdForUpdate(weddingId);
        if (count >= MAX_ACCOUNT_GROUPS) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED);
        }

        AccountGroup saved = accountGroupRepository.save(request.toEntity(weddingId));
        return AccountGroupResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountGroupResponse> getAccountGroupsByWedding(Long weddingId) {
        return accountGroupRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(AccountGroupResponse::from)
                .collect(Collectors.toList());
    }

    public AccountGroupResponse updateAccountGroup(Long accountGroupId, Long userId, AccountGroupRequest request) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        weddingService.validateWeddingAccess(accountGroup.getWeddingId(), userId);
        accountGroup.update(request.side(), request.groupName(), request.orderIndex());
        return AccountGroupResponse.from(accountGroup);
    }

    public void deleteAccountGroup(Long accountGroupId, Long userId) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        weddingService.validateWeddingAccess(accountGroup.getWeddingId(), userId);
        accountGroupRepository.deleteById(accountGroupId);
    }

    public AccountResponse createAccount(Long accountGroupId, Long userId, AccountRequest request) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        weddingService.validateWeddingAccess(accountGroup.getWeddingId(), userId);

        long count = accountRepository.countByAccountGroupIdForUpdate(accountGroupId);
        if (count >= MAX_ACCOUNTS_PER_GROUP) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_LIMIT_EXCEEDED);
        }

        Account saved = accountRepository.save(request.toEntity(accountGroupId));
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccountsByGroup(Long accountGroupId) {
        return accountRepository.findByAccountGroupIdOrderByOrderIndex(accountGroupId).stream()
                .map(AccountResponse::from)
                .collect(Collectors.toList());
    }

    public AccountResponse updateAccount(Long accountId, Long userId, AccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND));

        AccountGroup accountGroup = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        weddingService.validateWeddingAccess(accountGroup.getWeddingId(), userId);
        account.update(request.bankName(), request.bankCode(), request.accountNumber(),
                request.accountHolder(), request.kakaoPayUrl(), request.orderIndex());
        return AccountResponse.from(account);
    }

    public void deleteAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND));

        AccountGroup accountGroup = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        weddingService.validateWeddingAccess(accountGroup.getWeddingId(), userId);
        accountRepository.deleteById(accountId);
    }
}

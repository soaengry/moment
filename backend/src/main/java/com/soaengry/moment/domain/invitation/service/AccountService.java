package com.soaengry.moment.domain.invitation.service;

import com.soaengry.moment.domain.invitation.dto.request.AccountGroupRequest;
import com.soaengry.moment.domain.invitation.dto.request.AccountRequest;
import com.soaengry.moment.domain.invitation.dto.response.AccountGroupResponse;
import com.soaengry.moment.domain.invitation.dto.response.AccountResponse;
import com.soaengry.moment.domain.invitation.entity.Account;
import com.soaengry.moment.domain.invitation.entity.AccountGroup;
import com.soaengry.moment.domain.invitation.exception.InvitationErrorCode;
import com.soaengry.moment.domain.invitation.exception.InvitationException;
import com.soaengry.moment.domain.invitation.repository.AccountGroupRepository;
import com.soaengry.moment.domain.invitation.repository.AccountRepository;
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
    private final InvitationService invitationService;

    public AccountGroupResponse createAccountGroup(Long invitationId, Long userId, AccountGroupRequest request) {
        invitationService.validateInvitationAccess(invitationId, userId);

        long count = accountGroupRepository.countByInvitationIdForUpdate(invitationId);
        if (count >= MAX_ACCOUNT_GROUPS) {
            throw new InvitationException(InvitationErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED);
        }

        AccountGroup saved = accountGroupRepository.save(request.toEntity(invitationId));
        return AccountGroupResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountGroupResponse> getAccountGroupsByInvitation(Long invitationId) {
        return accountGroupRepository.findByInvitationIdOrderByOrderIndex(invitationId).stream()
                .map(AccountGroupResponse::from)
                .collect(Collectors.toList());
    }

    public AccountGroupResponse updateAccountGroup(Long accountGroupId, Long userId, AccountGroupRequest request) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        invitationService.validateInvitationAccess(accountGroup.getInvitationId(), userId);
        accountGroup.update(request.groupName(), request.orderIndex());
        return AccountGroupResponse.from(accountGroup);
    }

    public void deleteAccountGroup(Long accountGroupId, Long userId) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        invitationService.validateInvitationAccess(accountGroup.getInvitationId(), userId);
        accountGroupRepository.deleteById(accountGroupId);
    }

    public AccountResponse createAccount(Long accountGroupId, Long userId, AccountRequest request) {
        AccountGroup accountGroup = accountGroupRepository.findById(accountGroupId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        invitationService.validateInvitationAccess(accountGroup.getInvitationId(), userId);

        long count = accountRepository.countByAccountGroupIdForUpdate(accountGroupId);
        if (count >= MAX_ACCOUNTS_PER_GROUP) {
            throw new InvitationException(InvitationErrorCode.ACCOUNT_LIMIT_EXCEEDED);
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
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOUNT_NOT_FOUND));

        AccountGroup accountGroup = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        invitationService.validateInvitationAccess(accountGroup.getInvitationId(), userId);
        account.update(request.bankName(), request.bankCode(), request.accountNumber(),
                request.accountHolder(), request.kakaoPayUrl(), request.orderIndex());
        return AccountResponse.from(account);
    }

    public void deleteAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOUNT_NOT_FOUND));

        AccountGroup accountGroup = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new InvitationException(InvitationErrorCode.ACCOUNT_GROUP_NOT_FOUND));

        invitationService.validateInvitationAccess(accountGroup.getInvitationId(), userId);
        accountRepository.deleteById(accountId);
    }
}

package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.request.*;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.entity.*;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class WeddingService {

    private static final int MAX_ACCOUNT_GROUPS = 4;
    private static final int MAX_ACCOUNTS_PER_GROUP = 3;

    private final WeddingRepository weddingRepository;
    private final HostRepository hostRepository;
    private final ScheduleRepository scheduleRepository;
    private final AccountGroupRepository accountGroupRepository;
    private final AccountRepository accountRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    public void validateWeddingAccess(Long weddingId, Long userId) {
        validateAndGetWedding(weddingId, userId);
    }

    private Wedding validateAndGetWedding(Long weddingId, Long userId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        if (!wedding.getEvent().getUserId().equals(userId)) {
            throw new WeddingException(WeddingErrorCode.WEDDING_UNAUTHORIZED);
        }
        return wedding;
    }

    private Event validateAndGetEvent(Long eventId, Long userId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.getUserId().equals(userId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }
        return event;
    }

    private Long resolveUserIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(u -> u.getId())
                .orElse(null);
    }

    public Long resolveCoupleUserId(String email) {
        return resolveUserIdByEmail(email);
    }

    // ?�?�?� Wedding CRUD ?�?�?�

    public WeddingResponse createWedding(Long userId, WeddingRequest request) {
        Event event = eventRepository.findById(request.eventId())
                .orElseThrow(() -> new EventException(EventErrorCode.EVENT_NOT_FOUND));
        if (!event.getUserId().equals(userId)) {
            throw new EventException(EventErrorCode.EVENT_UNAUTHORIZED);
        }
        Wedding wedding = Wedding.builder()
                .event(event)
                .notice(request.notice())
                .parkingInfo(request.parkingInfo())
                .mealInfo(request.mealInfo())
                .greeting(request.greeting())
                .build();
        return WeddingResponse.from(weddingRepository.save(wedding));
    }

    @Transactional(readOnly = true)
    public WeddingResponse getWedding(Long weddingId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        return WeddingResponse.from(wedding);
    }

    @Transactional(readOnly = true)
    public WeddingResponse getWeddingByEventId(Long eventId) {
        Wedding wedding = weddingRepository.findByEventId(eventId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        return WeddingResponse.from(wedding);
    }

    public WeddingResponse updateWedding(Long weddingId, Long userId, WeddingRequest request) {
        Wedding wedding = validateAndGetWedding(weddingId, userId);
        wedding.update(request.notice(), request.parkingInfo(), request.mealInfo(), request.greeting());
        return WeddingResponse.from(wedding);
    }

    @Transactional(readOnly = true)
    public WeddingInfoResponse getWeddingInfo(Long weddingId) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));
        Long eventId = wedding.getEvent().getId();

        List<HostResponse> hosts = hostRepository.findByEventIdOrderByRole(eventId).stream()
                .map(h -> HostResponse.from(h, resolveUserIdByEmail(h.getEmail())))
                .toList();

        List<ScheduleResponse> schedules = scheduleRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(ScheduleResponse::from)
                .toList();

        List<AccountGroupWithAccountsResponse> accountGroups = accountGroupRepository
                .findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(group -> {
                    AccountGroupResponse groupResponse = AccountGroupResponse.from(group);
                    List<AccountResponse> accounts = accountRepository
                            .findByAccountGroupIdOrderByOrderIndex(group.getId()).stream()
                            .map(AccountResponse::from)
                            .toList();
                    return AccountGroupWithAccountsResponse.of(groupResponse, accounts);
                })
                .toList();

        return new WeddingInfoResponse(WeddingResponse.from(wedding), hosts, schedules, accountGroups);
    }

    // ?�?�?� Host ?�?�?�

    public HostResponse createHost(Long eventId, Long userId, HostRequest request) {
        validateAndGetEvent(eventId, userId);
        Host host = request.toEntity(eventId);
        Host saved = hostRepository.save(host);
        return HostResponse.from(saved, resolveUserIdByEmail(saved.getEmail()));
    }

    @Transactional(readOnly = true)
    public List<HostResponse> getHosts(Long eventId) {
        return hostRepository.findByEventIdOrderByRole(eventId).stream()
                .map(h -> HostResponse.from(h, resolveUserIdByEmail(h.getEmail())))
                .toList();
    }

    public HostResponse updateHost(Long hostId, Long userId, HostRequest request) {
        Host host = hostRepository.findById(hostId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.HOST_NOT_FOUND));
        validateAndGetEvent(host.getEventId(), userId);
        host.updateName(request.name());
        host.updateFather(request.fatherName(), request.isFatherAlive());
        host.updateMother(request.motherName(), request.isMotherAlive());
        host.updateContact(request.contact());
        host.updateProfileImageUrl(request.profileImageUrl());
        host.updateIntroduction(request.introduction());
        return HostResponse.from(host, resolveUserIdByEmail(host.getEmail()));
    }

    public void deleteHost(Long hostId, Long userId) {
        Host host = hostRepository.findById(hostId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.HOST_NOT_FOUND));
        validateAndGetEvent(host.getEventId(), userId);
        hostRepository.deleteById(hostId);
    }

    // ?�?�?� Schedule ?�?�?�

    public ScheduleResponse createSchedule(Long weddingId, Long userId, ScheduleRequest request) {
        validateAndGetWedding(weddingId, userId);
        Schedule saved = scheduleRepository.save(request.toEntity(weddingId));
        return ScheduleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(Long weddingId) {
        return scheduleRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(ScheduleResponse::from)
                .toList();
    }

    public ScheduleResponse updateSchedule(Long scheduleId, Long userId, ScheduleRequest request) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND));
        validateAndGetWedding(schedule.getWeddingId(), userId);
        schedule.update(request.time(), request.title(), request.description(), request.orderIndex());
        return ScheduleResponse.from(schedule);
    }

    public void deleteSchedule(Long scheduleId, Long userId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.SCHEDULE_NOT_FOUND));
        validateAndGetWedding(schedule.getWeddingId(), userId);
        scheduleRepository.deleteById(scheduleId);
    }

    // ?�?�?� AccountGroup ?�?�?�

    public AccountGroupResponse createAccountGroup(Long weddingId, Long userId, AccountGroupRequest request) {
        validateAndGetWedding(weddingId, userId);
        long count = accountGroupRepository.countByWeddingIdForUpdate(weddingId);
        if (count >= MAX_ACCOUNT_GROUPS) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED);
        }
        AccountGroup saved = accountGroupRepository.save(request.toEntity(weddingId));
        return AccountGroupResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountGroupResponse> getAccountGroups(Long weddingId) {
        return accountGroupRepository.findByWeddingIdOrderByOrderIndex(weddingId).stream()
                .map(AccountGroupResponse::from)
                .toList();
    }

    public AccountGroupResponse updateAccountGroup(Long groupId, Long userId, AccountGroupRequest request) {
        AccountGroup group = accountGroupRepository.findById(groupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetWedding(group.getWeddingId(), userId);
        group.update(request.groupName(), request.orderIndex());
        return AccountGroupResponse.from(group);
    }

    public void deleteAccountGroup(Long groupId, Long userId) {
        AccountGroup group = accountGroupRepository.findById(groupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetWedding(group.getWeddingId(), userId);
        accountGroupRepository.deleteById(groupId);
    }

    // ?�?�?� Account ?�?�?�

    public AccountResponse createAccount(Long groupId, Long userId, AccountRequest request) {
        AccountGroup group = accountGroupRepository.findById(groupId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetWedding(group.getWeddingId(), userId);
        long count = accountRepository.countByAccountGroupIdForUpdate(groupId);
        if (count >= MAX_ACCOUNTS_PER_GROUP) {
            throw new WeddingException(WeddingErrorCode.ACCOUNT_LIMIT_EXCEEDED);
        }
        Account saved = accountRepository.save(request.toEntity(groupId));
        return AccountResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getAccounts(Long groupId) {
        return accountRepository.findByAccountGroupIdOrderByOrderIndex(groupId).stream()
                .map(AccountResponse::from)
                .toList();
    }

    public AccountResponse updateAccount(Long accountId, Long userId, AccountRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND));
        AccountGroup group = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetWedding(group.getWeddingId(), userId);
        account.update(request.bankName(), request.bankCode(), request.accountNumber(),
                request.accountHolder(), request.kakaoPayUrl(), request.orderIndex());
        return AccountResponse.from(account);
    }

    public void deleteAccount(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_NOT_FOUND));
        AccountGroup group = accountGroupRepository.findById(account.getAccountGroupId())
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.ACCOUNT_GROUP_NOT_FOUND));
        validateAndGetWedding(group.getWeddingId(), userId);
        accountRepository.deleteById(accountId);
    }
}

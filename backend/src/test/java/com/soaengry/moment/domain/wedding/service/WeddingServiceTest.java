package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.event.dto.request.AccountGroupRequest;
import com.soaengry.moment.domain.event.dto.request.AccountRequest;
import com.soaengry.moment.domain.event.dto.request.ScheduleRequest;
import com.soaengry.moment.domain.event.dto.response.AccountGroupResponse;
import com.soaengry.moment.domain.event.dto.response.AccountResponse;
import com.soaengry.moment.domain.event.dto.response.ScheduleResponse;
import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.AccountGroupRepository;
import com.soaengry.moment.domain.event.repository.AccountRepository;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.event.repository.ScheduleRepository;
import com.soaengry.moment.domain.event.service.EventService;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.request.WeddingRequest;
import com.soaengry.moment.domain.wedding.dto.response.WeddingResponse;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class WeddingServiceTest {

    @Autowired
    private WeddingService weddingService;
    @Autowired
    private EventService eventService;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private WeddingRepository weddingRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private AccountGroupRepository accountGroupRepository;
    @Autowired
    private AccountRepository accountRepository;

    private User ownerUser;
    private User otherUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        ownerUser = userRepository.save(User.builder()
                .email("owner_ws_" + System.nanoTime() + "@test.com")
                .nickname("owner_ws_" + System.nanoTime())
                .isEmailVerified(true)
                .build());

        otherUser = userRepository.save(User.builder()
                .email("other_ws_" + System.nanoTime() + "@test.com")
                .nickname("other_ws_" + System.nanoTime())
                .isEmailVerified(true)
                .build());

        testEvent = eventRepository.saveAndFlush(Event.builder()
                .user(ownerUser)
                .title("김철수 ❤️ 이영희 결혼식")
                .type(EventType.WEDDING)
                .date(LocalDateTime.of(2026, 12, 25, 14, 0))
                .locationName("그랜드 컨벤션 센터")
                .locationAddress("서울특별시 강남구 테헤란로 152")
                .locationDetail("3층 그랜드홀")
                .locationLat(37.5012)
                .locationLng(127.0361)
                .slug("wedding-test-" + System.nanoTime())
                .build());
    }

    // ─── Wedding CRUD ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("웨딩 생성 성공")
    void createWedding_success() {
        WeddingResponse result = weddingService.createWedding(ownerUser.getId(),
                new WeddingRequest(testEvent.getId(), "유의사항", "주차 무료", "2층 뷔페", "안녕하세요"));

        assertThat(result.id()).isNotNull();
        assertThat(result.eventId()).isEqualTo(testEvent.getId());
        assertThat(result.notice()).isEqualTo("유의사항");
        assertThat(result.parkingInfo()).isEqualTo("주차 무료");
        assertThat(result.mealInfo()).isEqualTo("2층 뷔페");

        System.out.println("✅ 웨딩 생성 성공 - id: " + result.id());
    }

    @Test
    @DisplayName("웨딩 생성 실패 - 이벤트 없음")
    void createWedding_fail_eventNotFound() {
        assertThatThrownBy(() ->
                weddingService.createWedding(ownerUser.getId(), new WeddingRequest(999L, null, null, null, null)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());

        System.out.println("✅ 이벤트 없음 예외 확인");
    }

    @Test
    @DisplayName("웨딩 생성 실패 - 이벤트 소유자가 아님")
    void createWedding_fail_unauthorized() {
        assertThatThrownBy(() ->
                weddingService.createWedding(otherUser.getId(), new WeddingRequest(testEvent.getId(), null, null, null, null)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());

        System.out.println("✅ 이벤트 권한 없음 예외 확인");
    }

    @Test
    @DisplayName("웨딩 단건 조회 성공")
    void getWedding_success() {
        WeddingResponse created = weddingService.createWedding(ownerUser.getId(),
                new WeddingRequest(testEvent.getId(), "유의사항", null, null, null));

        WeddingResponse result = weddingService.getWedding(created.id());

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.notice()).isEqualTo("유의사항");

        System.out.println("✅ 웨딩 단건 조회 성공");
    }

    @Test
    @DisplayName("이벤트 ID로 웨딩 조회 성공")
    void getWeddingByEventId_success() {
        WeddingResponse created = weddingService.createWedding(ownerUser.getId(),
                new WeddingRequest(testEvent.getId(), null, null, null, null));

        WeddingResponse result = weddingService.getWeddingByEventId(testEvent.getId());

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.eventId()).isEqualTo(testEvent.getId());

        System.out.println("✅ 이벤트 ID로 웨딩 조회 성공");
    }

    @Test
    @DisplayName("웨딩 수정 성공")
    void updateWedding_success() {
        WeddingResponse created = weddingService.createWedding(ownerUser.getId(),
                new WeddingRequest(testEvent.getId(), "원래 유의사항", null, null, null));

        WeddingResponse result = weddingService.updateWedding(created.id(), ownerUser.getId(),
                new WeddingRequest(testEvent.getId(), "수정된 유의사항", "무료주차", "2층 뷔페홀", "반갑습니다"));

        assertThat(result.notice()).isEqualTo("수정된 유의사항");
        assertThat(result.parkingInfo()).isEqualTo("무료주차");
        assertThat(result.mealInfo()).isEqualTo("2층 뷔페홀");

        System.out.println("✅ 웨딩 수정 성공");
    }

    @Test
    @DisplayName("웨딩 수정 실패 - 권한 없음")
    void updateWedding_fail_unauthorized() {
        WeddingResponse created = weddingService.createWedding(ownerUser.getId(),
                new WeddingRequest(testEvent.getId(), null, null, null, null));

        assertThatThrownBy(() ->
                weddingService.updateWedding(created.id(), otherUser.getId(),
                        new WeddingRequest(testEvent.getId(), "변경", null, null, null)))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_UNAUTHORIZED.getMessage());

        System.out.println("✅ 웨딩 수정 권한 없음 예외 확인");
    }

    // ─── Schedule ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("식순 추가/조회/수정/삭제 성공 (event 하위)")
    void schedule_crud_success() {
        // add
        ScheduleResponse added = eventService.createSchedule(testEvent.getId(), ownerUser.getId(),
                new ScheduleRequest("식전 음악", "연주 시작", 0));
        assertThat(added.title()).isEqualTo("식전 음악");
        assertThat(added.eventId()).isEqualTo(testEvent.getId());

        List<ScheduleResponse> schedules = eventService.getSchedules(testEvent.getId());
        assertThat(schedules).hasSize(1);

        // update
        ScheduleResponse updated = eventService.updateSchedule(added.id(), ownerUser.getId(),
                new ScheduleRequest("혼인 서약", null, 1));
        assertThat(updated.title()).isEqualTo("혼인 서약");

        // delete
        eventService.deleteSchedule(added.id(), ownerUser.getId());
        assertThat(scheduleRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 식순 추가/조회/수정/삭제 성공");
    }

    // ─── AccountGroup ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계좌 그룹 추가/조회/수정/삭제 성공")
    void accountGroup_crud_success() {
        AccountGroupResponse added = eventService.createAccountGroup(testEvent.getId(), ownerUser.getId(),
                new AccountGroupRequest("신랑측", 0));
        assertThat(added.groupName()).isEqualTo("신랑측");
        assertThat(added.eventId()).isEqualTo(testEvent.getId());

        List<AccountGroupResponse> groups = eventService.getAccountGroups(testEvent.getId());
        assertThat(groups).hasSize(1);

        AccountGroupResponse updated = eventService.updateAccountGroup(added.id(), ownerUser.getId(),
                new AccountGroupRequest("신랑측 (수정)", 0));
        assertThat(updated.groupName()).isEqualTo("신랑측 (수정)");

        eventService.deleteAccountGroup(added.id(), ownerUser.getId());
        assertThat(accountGroupRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 계좌 그룹 추가/조회/수정/삭제 성공");
    }

    @Test
    @DisplayName("계좌 그룹 생성 실패 - 최대 4개 초과")
    void createAccountGroup_fail_limitExceeded() {
        for (int i = 0; i < 4; i++) {
            eventService.createAccountGroup(testEvent.getId(), ownerUser.getId(),
                    new AccountGroupRequest("그룹" + i, i));
        }

        assertThatThrownBy(() ->
                eventService.createAccountGroup(testEvent.getId(), ownerUser.getId(),
                        new AccountGroupRequest("그룹5", 4)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED.getMessage());

        System.out.println("✅ 계좌 그룹 4개 초과 예외 확인");
    }

    // ─── Account ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계좌 추가/조회/수정/삭제 성공")
    void account_crud_success() {
        AccountGroupResponse group = eventService.createAccountGroup(testEvent.getId(), ownerUser.getId(),
                new AccountGroupRequest("신랑측", 0));

        AccountResponse added = eventService.createAccount(group.id(), ownerUser.getId(),
                new AccountRequest("국민은행", "004", "123-456-789", "김철수", null, 0));
        assertThat(added.bankName()).isEqualTo("국민은행");
        assertThat(added.accountNumber()).isEqualTo("123-456-789");

        List<AccountResponse> accounts = eventService.getAccounts(group.id());
        assertThat(accounts).hasSize(1);

        AccountResponse updated = eventService.updateAccount(added.id(), ownerUser.getId(),
                new AccountRequest("신한은행", "088", "987-654-321", "김철수", "https://kakao.pay/link", 0));
        assertThat(updated.bankName()).isEqualTo("신한은행");
        assertThat(updated.kakaoPayUrl()).isEqualTo("https://kakao.pay/link");

        eventService.deleteAccount(added.id(), ownerUser.getId());
        assertThat(accountRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 계좌 추가/조회/수정/삭제 성공");
    }

    @Test
    @DisplayName("계좌 생성 실패 - 그룹당 최대 3개 초과")
    void createAccount_fail_limitExceeded() {
        AccountGroupResponse group = eventService.createAccountGroup(testEvent.getId(), ownerUser.getId(),
                new AccountGroupRequest("신랑측", 0));
        for (int i = 0; i < 3; i++) {
            eventService.createAccount(group.id(), ownerUser.getId(),
                    new AccountRequest("국민은행", "004", "111-" + i, "김철수", null, i));
        }

        assertThatThrownBy(() ->
                eventService.createAccount(group.id(), ownerUser.getId(),
                        new AccountRequest("우리은행", "020", "999-999", "김철수", null, 3)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.ACCOUNT_LIMIT_EXCEEDED.getMessage());

        System.out.println("✅ 계좌 3개 초과 예외 확인");
    }
}

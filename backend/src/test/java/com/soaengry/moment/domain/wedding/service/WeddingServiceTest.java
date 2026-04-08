package com.soaengry.moment.domain.wedding.service;

import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.wedding.dto.request.*;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.entity.Couple;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
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
    private EventRepository eventRepository;

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private CoupleRepository coupleRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private AccountGroupRepository accountGroupRepository;

    @Autowired
    private AccountRepository accountRepository;

    private static final Long OWNER_ID = 10L;
    private static final Long OTHER_ID = 99L;

    private Event testEvent;

    @BeforeEach
    void setUp() {
        testEvent = Event.builder()
                .userId(OWNER_ID)
                .title("김철수 ❤️ 이영희 결혼식")
                .type(EventType.WEDDING)
                .date(LocalDate.of(2026, 12, 25))
                .locationName("그랜드 컨벤션 센터")
                .locationAddress("서울특별시 강남구 테헤란로 152")
                .locationDetail("3층 그랜드홀")
                .locationLat(37.5012)
                .locationLng(127.0361)
                .slug("wedding-test-001")
                .build();
        testEvent = eventRepository.saveAndFlush(testEvent);
    }

    // ─── Wedding CRUD ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("웨딩 생성 성공")
    void createWedding_success() {
        // when
        WeddingResponse result = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), "유의사항", "주차 무료", "2층 뷔페", "안녕하세요"));

        // then
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
                weddingService.createWedding(OWNER_ID, new WeddingRequest(999L, null, null, null, null)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());

        System.out.println("✅ 이벤트 없음 예외 확인");
    }

    @Test
    @DisplayName("웨딩 생성 실패 - 이벤트 소유자가 아님")
    void createWedding_fail_unauthorized() {
        assertThatThrownBy(() ->
                weddingService.createWedding(OTHER_ID, new WeddingRequest(testEvent.getId(), null, null, null, null)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());

        System.out.println("✅ 이벤트 권한 없음 예외 확인");
    }

    @Test
    @DisplayName("웨딩 단건 조회 성공")
    void getWedding_success() {
        // given
        WeddingResponse created = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), "유의사항", null, null, null));

        // when
        WeddingResponse result = weddingService.getWedding(created.id());

        // then
        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.notice()).isEqualTo("유의사항");

        System.out.println("✅ 웨딩 단건 조회 성공");
    }

    @Test
    @DisplayName("이벤트 ID로 웨딩 조회 성공")
    void getWeddingByEventId_success() {
        // given
        WeddingResponse created = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));

        // when
        WeddingResponse result = weddingService.getWeddingByEventId(testEvent.getId());

        // then
        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.eventId()).isEqualTo(testEvent.getId());

        System.out.println("✅ 이벤트 ID로 웨딩 조회 성공");
    }

    @Test
    @DisplayName("웨딩 수정 성공")
    void updateWedding_success() {
        // given
        WeddingResponse created = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), "원래 유의사항", null, null, null));

        // when
        WeddingResponse result = weddingService.updateWedding(created.id(), OWNER_ID,
                new WeddingRequest(testEvent.getId(), "수정된 유의사항", "무료주차", "2층 뷔페홀", "반갑습니다"));

        // then
        assertThat(result.notice()).isEqualTo("수정된 유의사항");
        assertThat(result.parkingInfo()).isEqualTo("무료주차");
        assertThat(result.mealInfo()).isEqualTo("2층 뷔페홀");

        System.out.println("✅ 웨딩 수정 성공");
    }

    @Test
    @DisplayName("웨딩 수정 실패 - 권한 없음")
    void updateWedding_fail_unauthorized() {
        // given
        WeddingResponse created = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));

        // when & then
        assertThatThrownBy(() ->
                weddingService.updateWedding(created.id(), OTHER_ID,
                        new WeddingRequest(testEvent.getId(), "변경", null, null, null)))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_UNAUTHORIZED.getMessage());

        System.out.println("✅ 웨딩 수정 권한 없음 예외 확인");
    }

    @Test
    @DisplayName("웨딩 통합 정보 조회 성공 - couples/schedules/accountGroups 포함")
    void getWeddingInfo_success() {
        // given
        WeddingResponse wedding = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), "유의사항", null, null, null));

        weddingService.createCouple(wedding.id(), OWNER_ID,
                new CoupleRequest(Couple.CoupleRole.GROOM, "김철수", "groom@example.com",
                        "김아버지", "김어머니", true, true, "010-1111-2222", null, null));

        weddingService.createSchedule(wedding.id(), OWNER_ID,
                new ScheduleRequest(LocalTime.of(14, 0), "식전 음악", null, 0));

        AccountGroupResponse group = weddingService.createAccountGroup(wedding.id(), OWNER_ID,
                new AccountGroupRequest("신랑측", 0));
        weddingService.createAccount(group.id(), OWNER_ID,
                new AccountRequest("국민은행", "004", "123-456-789", "김철수", null, 0));

        // when
        WeddingInfoResponse result = weddingService.getWeddingInfo(wedding.id());

        // then
        assertThat(result.wedding().id()).isEqualTo(wedding.id());
        assertThat(result.couples()).hasSize(1);
        assertThat(result.couples().get(0).name()).isEqualTo("김철수");
        assertThat(result.schedules()).hasSize(1);
        assertThat(result.accountGroups()).hasSize(1);
        assertThat(result.accountGroups().get(0).accounts()).hasSize(1);

        System.out.println("✅ 웨딩 통합 정보 조회 성공");
    }

    // ─── Couple ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("커플 추가/조회/수정/삭제 성공")
    void couple_crud_success() {
        // given
        WeddingResponse wedding = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));

        // add
        CoupleResponse groom = weddingService.createCouple(wedding.id(), OWNER_ID,
                new CoupleRequest(Couple.CoupleRole.GROOM, "김철수", "groom@example.com",
                        null, null, true, true, "010-1111-2222", null, null));
        weddingService.createCouple(wedding.id(), OWNER_ID,
                new CoupleRequest(Couple.CoupleRole.BRIDE, "이영희", "bride@example.com",
                        null, null, true, true, "010-3333-4444", null, null));

        List<CoupleResponse> couples = weddingService.getCouples(wedding.id());
        assertThat(couples).hasSize(2);

        // update
        CoupleResponse updated = weddingService.updateCouple(groom.id(), OWNER_ID,
                new CoupleRequest(Couple.CoupleRole.GROOM, "김수정", "groom@example.com",
                        "김아버지", "김어머니", true, true, "010-9999-0000", null, "소개글"));
        assertThat(updated.name()).isEqualTo("김수정");
        assertThat(updated.introduction()).isEqualTo("소개글");

        // delete
        weddingService.deleteCouple(groom.id(), OWNER_ID);
        assertThat(coupleRepository.findById(groom.id())).isEmpty();

        System.out.println("✅ 커플 추가/조회/수정/삭제 성공");
    }

    // ─── Schedule ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("식순 추가/조회/수정/삭제 성공")
    void schedule_crud_success() {
        // given
        WeddingResponse wedding = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));

        // add
        ScheduleResponse added = weddingService.createSchedule(wedding.id(), OWNER_ID,
                new ScheduleRequest(LocalTime.of(14, 0), "식전 음악", "연주 시작", 0));
        assertThat(added.title()).isEqualTo("식전 음악");

        List<ScheduleResponse> schedules = weddingService.getSchedules(wedding.id());
        assertThat(schedules).hasSize(1);

        // update
        ScheduleResponse updated = weddingService.updateSchedule(added.id(), OWNER_ID,
                new ScheduleRequest(LocalTime.of(14, 30), "혼인 서약", null, 1));
        assertThat(updated.title()).isEqualTo("혼인 서약");

        // delete
        weddingService.deleteSchedule(added.id(), OWNER_ID);
        assertThat(scheduleRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 식순 추가/조회/수정/삭제 성공");
    }

    // ─── AccountGroup ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계좌 그룹 추가/조회/수정/삭제 성공")
    void accountGroup_crud_success() {
        // given
        WeddingResponse wedding = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));

        // add
        AccountGroupResponse added = weddingService.createAccountGroup(wedding.id(), OWNER_ID,
                new AccountGroupRequest("신랑측", 0));
        assertThat(added.groupName()).isEqualTo("신랑측");

        List<AccountGroupResponse> groups = weddingService.getAccountGroups(wedding.id());
        assertThat(groups).hasSize(1);

        // update
        AccountGroupResponse updated = weddingService.updateAccountGroup(added.id(), OWNER_ID,
                new AccountGroupRequest("신랑측 (수정)", 0));
        assertThat(updated.groupName()).isEqualTo("신랑측 (수정)");

        // delete
        weddingService.deleteAccountGroup(added.id(), OWNER_ID);
        assertThat(accountGroupRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 계좌 그룹 추가/조회/수정/삭제 성공");
    }

    @Test
    @DisplayName("계좌 그룹 생성 실패 - 최대 4개 초과")
    void createAccountGroup_fail_limitExceeded() {
        // given
        WeddingResponse wedding = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));
        for (int i = 0; i < 4; i++) {
            weddingService.createAccountGroup(wedding.id(), OWNER_ID,
                    new AccountGroupRequest("그룹" + i, i));
        }

        // when & then
        assertThatThrownBy(() ->
                weddingService.createAccountGroup(wedding.id(), OWNER_ID,
                        new AccountGroupRequest("그룹5", 4)))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED.getMessage());

        System.out.println("✅ 계좌 그룹 4개 초과 예외 확인");
    }

    // ─── Account ──────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("계좌 추가/조회/수정/삭제 성공")
    void account_crud_success() {
        // given
        WeddingResponse wedding = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));
        AccountGroupResponse group = weddingService.createAccountGroup(wedding.id(), OWNER_ID,
                new AccountGroupRequest("신랑측", 0));

        // add
        AccountResponse added = weddingService.createAccount(group.id(), OWNER_ID,
                new AccountRequest("국민은행", "004", "123-456-789", "김철수", null, 0));
        assertThat(added.bankName()).isEqualTo("국민은행");
        assertThat(added.accountNumber()).isEqualTo("123-456-789");

        List<AccountResponse> accounts = weddingService.getAccounts(group.id());
        assertThat(accounts).hasSize(1);

        // update
        AccountResponse updated = weddingService.updateAccount(added.id(), OWNER_ID,
                new AccountRequest("신한은행", "088", "987-654-321", "김철수", "https://kakao.pay/link", 0));
        assertThat(updated.bankName()).isEqualTo("신한은행");
        assertThat(updated.kakaoPayUrl()).isEqualTo("https://kakao.pay/link");

        // delete
        weddingService.deleteAccount(added.id(), OWNER_ID);
        assertThat(accountRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 계좌 추가/조회/수정/삭제 성공");
    }

    @Test
    @DisplayName("계좌 생성 실패 - 그룹당 최대 3개 초과")
    void createAccount_fail_limitExceeded() {
        // given
        WeddingResponse wedding = weddingService.createWedding(OWNER_ID,
                new WeddingRequest(testEvent.getId(), null, null, null, null));
        AccountGroupResponse group = weddingService.createAccountGroup(wedding.id(), OWNER_ID,
                new AccountGroupRequest("신랑측", 0));
        for (int i = 0; i < 3; i++) {
            weddingService.createAccount(group.id(), OWNER_ID,
                    new AccountRequest("국민은행", "004", "111-" + i, "김철수", null, i));
        }

        // when & then
        assertThatThrownBy(() ->
                weddingService.createAccount(group.id(), OWNER_ID,
                        new AccountRequest("우리은행", "020", "999-999", "김철수", null, 3)))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.ACCOUNT_LIMIT_EXCEEDED.getMessage());

        System.out.println("✅ 계좌 3개 초과 예외 확인");
    }
}

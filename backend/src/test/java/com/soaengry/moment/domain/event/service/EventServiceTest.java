package com.soaengry.moment.domain.event.service;

import com.soaengry.moment.domain.event.dto.request.*;
import com.soaengry.moment.domain.event.dto.response.*;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.entity.Host;
import com.soaengry.moment.domain.event.entity.Transportation;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.*;
import com.soaengry.moment.domain.event.dto.response.GatheringDetailResponse;
import com.soaengry.moment.domain.wedding.dto.response.WeddingDetailResponse;
import com.soaengry.moment.domain.wedding.dto.response.WeddingHostCombinedResponse;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.config.TestSchemaConfig;
import com.soaengry.moment.global.service.KakaoGeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@Import(TestSchemaConfig.class)
class EventServiceTest {

    @Autowired private EventService eventService;
    @Autowired private EventRepository eventRepository;
    @Autowired private HeroImageRepository heroImageRepository;
    @Autowired private TransportationRepository transportationRepository;
    @Autowired private AnnouncementRepository announcementRepository;
    @Autowired private ScheduleRepository scheduleRepository;
    @Autowired private HostRepository hostRepository;
    @Autowired private AccountGroupRepository accountGroupRepository;
    @Autowired private AccountRepository accountRepository;
    @Autowired private UserRepository userRepository;

    @MockitoBean
    private KakaoGeocodingService kakaoGeocodingService;

    private Long OWNER_ID;
    private Long OTHER_ID;

    @BeforeEach
    void setUp() {
        when(kakaoGeocodingService.geocode(anyString()))
                .thenReturn(new KakaoGeocodingService.Coordinate(37.5665, 126.9780));

        User owner = userRepository.save(User.builder()
                .email("event_owner_" + System.nanoTime() + "@test.com")
                .nickname("owner_" + System.nanoTime())
                .isEmailVerified(true)
                .build());
        User other = userRepository.save(User.builder()
                .email("event_other_" + System.nanoTime() + "@test.com")
                .nickname("other_" + System.nanoTime())
                .isEmailVerified(true)
                .build());
        OWNER_ID = owner.getId();
        OTHER_ID = other.getId();
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private EventRequest weddingRequest(String slug) {
        return new EventRequest(
                "김철수 & 이영희 결혼식", slug, EventType.WEDDING,
                LocalDateTime.of(2026, 12, 25, 14, 0),
                "그랜드 컨벤션", "서울특별시 강남구 테헤란로 152", "3층 홀",
                true, "식사 안내", "주차 안내", "식사 제공", "환영합니다"
        );
    }

    private EventRequest gatheringRequest(String slug) {
        return new EventRequest(
                "동창 모임", slug, EventType.GATHERING,
                LocalDateTime.of(2026, 6, 1, 18, 0),
                null, null, null,
                true, null, null, null, null
        );
    }

    private EventResponse createWedding(String slug) {
        return eventService.createEvent(OWNER_ID, weddingRequest(slug));
    }

    // ─── Event CRUD ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("이벤트 생성 성공 - location 포함")
    void createEvent_withLocation_success() {
        EventResponse result = createWedding("wedding-001");

        assertThat(result.id()).isNotNull();
        assertThat(result.slug()).isEqualTo("wedding-001");
        assertThat(result.type()).isEqualTo(EventType.WEDDING);
        assertThat(result.locationName()).isEqualTo("그랜드 컨벤션");
        assertThat(result.locationLat()).isNotNull();
    }

    @Test
    @DisplayName("이벤트 생성 성공 - location 없이 (gathering)")
    void createEvent_withoutLocation_success() {
        EventResponse result = eventService.createEvent(OWNER_ID, gatheringRequest("gathering-001"));

        assertThat(result.id()).isNotNull();
        assertThat(result.locationName()).isNull();
        assertThat(result.locationLat()).isNull();
    }

    @Test
    @DisplayName("이벤트 생성 실패 - slug 중복")
    void createEvent_fail_slugDuplicated() {
        createWedding("dup-slug");

        assertThatThrownBy(() -> createWedding("dup-slug"))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_SLUG_DUPLICATED.getMessage());
    }

    @Test
    @DisplayName("이벤트 단건 조회 성공")
    void getEvent_success() {
        EventResponse created = createWedding("get-test");

        EventResponse result = eventService.getEvent(created.id());

        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.slug()).isEqualTo("get-test");
    }

    @Test
    @DisplayName("이벤트 단건 조회 실패 - 존재하지 않는 ID")
    void getEvent_fail_notFound() {
        assertThatThrownBy(() -> eventService.getEvent(999L))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이벤트 수정 성공 - location 변경")
    void updateEvent_success() {
        EventResponse created = createWedding("update-test");
        EventRequest req = new EventRequest(
                "수정된 제목", "update-test", EventType.WEDDING,
                LocalDateTime.of(2027, 3, 1, 12, 0),
                "롯데 시그니엘", "서울특별시 송파구 올림픽로 300", null,
                false, null, null, null, null
        );

        EventResponse result = eventService.updateEvent(created.id(), OWNER_ID, req);

        assertThat(result.title()).isEqualTo("수정된 제목");
        assertThat(result.locationName()).isEqualTo("롯데 시그니엘");
        assertThat(result.isPublic()).isFalse();
    }

    @Test
    @DisplayName("이벤트 수정 성공 - locationAddress null이면 기존 location 유지")
    void updateEvent_success_locationUnchanged() {
        EventResponse created = createWedding("loc-unchanged");
        EventRequest req = new EventRequest(
                "제목만 수정", "loc-unchanged", EventType.WEDDING,
                LocalDateTime.of(2026, 12, 25, 14, 0),
                null, null, null, true, null, null, null, null
        );

        EventResponse result = eventService.updateEvent(created.id(), OWNER_ID, req);

        assertThat(result.title()).isEqualTo("제목만 수정");
        assertThat(result.locationName()).isEqualTo("그랜드 컨벤션");
    }

    @Test
    @DisplayName("이벤트 수정 실패 - 권한 없음")
    void updateEvent_fail_unauthorized() {
        EventResponse created = createWedding("auth-update");

        assertThatThrownBy(() -> eventService.updateEvent(created.id(), OTHER_ID, weddingRequest("auth-update")))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("이벤트 삭제 성공")
    void deleteEvent_success() {
        EventResponse created = createWedding("del-test");

        eventService.deleteEvent(created.id(), OWNER_ID);

        assertThat(eventRepository.findById(created.id())).isEmpty();
    }

    @Test
    @DisplayName("이벤트 삭제 실패 - 권한 없음")
    void deleteEvent_fail_unauthorized() {
        EventResponse created = createWedding("del-auth");

        assertThatThrownBy(() -> eventService.deleteEvent(created.id(), OTHER_ID))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());
    }

    // ─── Slug & Info ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("slug 중복 확인 - 존재함")
    void checkSlugExists_true() {
        createWedding("exists-slug");
        assertThat(eventService.checkSlugExists("exists-slug")).isTrue();
    }

    @Test
    @DisplayName("slug 중복 확인 - 존재하지 않음")
    void checkSlugExists_false() {
        assertThat(eventService.checkSlugExists("ghost-slug")).isFalse();
    }

    @Test
    @DisplayName("slug로 이벤트 정보 조회 성공 - public 이벤트, 비인증 접근 가능")
    void getEventInfoBySlug_success_public() {
        EventResponse event = eventService.createEvent(OWNER_ID, gatheringRequest("info-slug"));
        eventService.addHeroImage(event.id(), OWNER_ID,
                new HeroImageRequest("https://s3.example.com/img.jpg", null, 0));
        eventService.addTransportation(event.id(), OWNER_ID,
                new TransportationRequest(Transportation.TransportType.SUBWAY, "2호선 삼성역", "도보 10분", 0));
        eventService.addAnnouncement(event.id(), OWNER_ID,
                new AnnouncementRequest("공지", "내용", true));

        EventInfoResponse result = eventService.getEventInfoBySlug("info-slug", null);

        assertThat(result.event().slug()).isEqualTo("info-slug");
        assertThat(result.heroImages()).hasSize(1);
        assertThat(result.transportation()).hasSize(1);
        assertThat(result.announcements()).hasSize(1);
        assertThat(result.announcements().get(0).isPinned()).isTrue();
    }

    @Test
    @DisplayName("slug로 이벤트 정보 조회 실패 - private 이벤트, 비인증")
    void getEventInfoBySlug_fail_private_unauthenticated() {
        EventRequest privateReq = new EventRequest(
                "비공개 이벤트", "private-event", EventType.WEDDING,
                LocalDateTime.of(2026, 12, 25, 14, 0),
                null, null, null, false, null, null, null, null
        );
        eventService.createEvent(OWNER_ID, privateReq);

        assertThatThrownBy(() -> eventService.getEventInfoBySlug("private-event", null))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("slug로 이벤트 정보 조회 실패 - 없는 slug")
    void getEventInfoBySlug_fail_notFound() {
        assertThatThrownBy(() -> eventService.getEventInfoBySlug("no-such-slug", null))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("ID로 이벤트 정보 조회 성공 - 소유자는 private 이벤트 접근 가능")
    void getEventInfo_success_ownerAccessPrivate() {
        EventRequest privateReq = new EventRequest(
                "비공개 모임", "owner-only-gathering", EventType.GATHERING,
                LocalDateTime.of(2026, 6, 1, 18, 0),
                null, null, null, false, null, null, null, null
        );
        EventResponse created = eventService.createEvent(OWNER_ID, privateReq);

        EventInfoResponse result = eventService.getEventInfo(created.id(), OWNER_ID);

        assertThat(result.event().id()).isEqualTo(created.id());
    }

    // ─── HeroImage ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("히어로 이미지 추가/조회/삭제 성공")
    void heroImage_crud_success() {
        EventResponse event = createWedding("hero-test");

        HeroImageResponse added = eventService.addHeroImage(event.id(), OWNER_ID,
                new HeroImageRequest("https://s3.example.com/img.jpg", "https://s3.example.com/thumb.jpg", 0));
        assertThat(added.imageUrl()).isEqualTo("https://s3.example.com/img.jpg");

        assertThat(eventService.getHeroImages(event.id())).hasSize(1);

        eventService.deleteHeroImage(added.id(), OWNER_ID);
        assertThat(heroImageRepository.findById(added.id())).isEmpty();
    }

    @Test
    @DisplayName("히어로 이미지 삭제 실패 - 권한 없음")
    void heroImage_delete_fail_unauthorized() {
        EventResponse event = createWedding("hero-auth");
        HeroImageResponse added = eventService.addHeroImage(event.id(), OWNER_ID,
                new HeroImageRequest("https://s3.example.com/img.jpg", null, 0));

        assertThatThrownBy(() -> eventService.deleteHeroImage(added.id(), OTHER_ID))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());
    }

    // ─── Transportation ───────────────────────────────────────────────────────

    @Test
    @DisplayName("교통편 추가/수정/삭제 성공")
    void transportation_crud_success() {
        EventResponse event = createWedding("trans-test");

        TransportationResponse added = eventService.addTransportation(event.id(), OWNER_ID,
                new TransportationRequest(Transportation.TransportType.SUBWAY, "2호선 삼성역", "도보 10분", 0));
        assertThat(added.title()).isEqualTo("2호선 삼성역");

        TransportationResponse updated = eventService.updateTransportation(added.id(), OWNER_ID,
                new TransportationRequest(Transportation.TransportType.BUS, "146번 버스", "도보 5분", 0));
        assertThat(updated.title()).isEqualTo("146번 버스");
        assertThat(updated.type()).isEqualTo(Transportation.TransportType.BUS);

        eventService.deleteTransportation(added.id(), OWNER_ID);
        assertThat(transportationRepository.findById(added.id())).isEmpty();
    }

    // ─── Announcement ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("공지사항 추가/수정/삭제 성공")
    void announcement_crud_success() {
        EventResponse event = createWedding("announce-test");

        AnnouncementResponse added = eventService.addAnnouncement(event.id(), OWNER_ID,
                new AnnouncementRequest("공지 제목", "공지 내용", false));
        assertThat(added.title()).isEqualTo("공지 제목");
        assertThat(added.isPinned()).isFalse();

        AnnouncementResponse updated = eventService.updateAnnouncement(added.id(), OWNER_ID,
                new AnnouncementRequest("수정된 제목", "수정된 내용", true));
        assertThat(updated.title()).isEqualTo("수정된 제목");
        assertThat(updated.isPinned()).isTrue();

        eventService.deleteAnnouncement(added.id(), OWNER_ID);
        assertThat(announcementRepository.findById(added.id())).isEmpty();
    }

    // ─── Schedule ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("일정 추가/수정/삭제 성공")
    void schedule_crud_success() {
        EventResponse event = createWedding("schedule-test");

        ScheduleResponse created = eventService.createSchedule(event.id(), OWNER_ID,
                new ScheduleRequest("신랑 입장", "신랑이 입장합니다", 0));
        assertThat(created.title()).isEqualTo("신랑 입장");
        assertThat(created.orderIndex()).isEqualTo(0);

        ScheduleResponse updated = eventService.updateSchedule(created.id(), OWNER_ID,
                new ScheduleRequest("신부 입장", "신부가 입장합니다", 1));
        assertThat(updated.title()).isEqualTo("신부 입장");
        assertThat(updated.orderIndex()).isEqualTo(1);

        assertThat(eventService.getSchedules(event.id())).hasSize(1);

        eventService.deleteSchedule(created.id(), OWNER_ID);
        assertThat(scheduleRepository.findById(created.id())).isEmpty();
    }

    // ─── Host ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("호스트 추가/조회/수정/삭제 성공")
    void host_crud_success() {
        EventResponse event = createWedding("host-test");

        HostResponse created = eventService.createHost(event.id(), OWNER_ID,
                new HostRequest(Host.HostRole.GROOM, "김철수", "groom@test.com", "010-1234-5678", null, "신랑입니다", null));
        assertThat(created.getName()).isEqualTo("김철수");
        assertThat(created.getRole()).isEqualTo(Host.HostRole.GROOM);

        assertThat(eventService.getHosts(event.id())).hasSize(1);

        HostResponse updated = eventService.updateHost(created.getId(), OWNER_ID,
                new HostRequest(Host.HostRole.GROOM, "김민수", "groom@test.com", "010-9999-0000", null, "수정된 소개", null));
        assertThat(updated.getName()).isEqualTo("김민수");

        eventService.deleteHost(created.getId(), OWNER_ID);
        assertThat(hostRepository.findById(created.getId())).isEmpty();
    }

    @Test
    @DisplayName("결혼식 호스트 조회 - WeddingHostData 포함 반환")
    void getWeddingHosts_success() {
        EventResponse event = createWedding("wedding-host-test");
        HostRequest.WeddingHostData weddingData =
                new HostRequest.WeddingHostData("김아버지", "김어머니", true, true);
        eventService.createHost(event.id(), OWNER_ID,
                new HostRequest(Host.HostRole.GROOM, "김철수", null, null, null, null, weddingData));

        List<WeddingHostCombinedResponse> result = eventService.getWeddingHosts(event.id());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("김철수");
        assertThat(result.get(0).getFatherName()).isEqualTo("김아버지");
        assertThat(result.get(0).getIsFatherAlive()).isTrue();
    }

    @Test
    @DisplayName("모임 호스트 조회 - HostResponse 반환")
    void getGatheringHosts_success() {
        EventResponse event = eventService.createEvent(OWNER_ID, gatheringRequest("gathering-host-test"));
        eventService.createHost(event.id(), OWNER_ID,
                new HostRequest(Host.HostRole.HOST, "이진행", null, null, null, null, null));

        List<HostResponse> result = eventService.getGatheringHosts(event.id());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("이진행");
    }

    // ─── AccountGroup & Account ───────────────────────────────────────────────

    @Test
    @DisplayName("계좌 그룹/계좌 CRUD 성공 - orderIndex 자동 부여")
    void accountGroupAndAccount_crud_success() {
        EventResponse event = createWedding("account-test");

        AccountGroupResponse group = eventService.createAccountGroup(event.id(), OWNER_ID,
                new AccountGroupRequest("신랑측", 0));
        assertThat(group.groupName()).isEqualTo("신랑측");

        AccountGroupResponse updatedGroup = eventService.updateAccountGroup(group.id(), OWNER_ID,
                new AccountGroupRequest("신랑 측", 0));
        assertThat(updatedGroup.groupName()).isEqualTo("신랑 측");

        AccountResponse acc1 = eventService.createAccount(group.id(), OWNER_ID,
                new AccountRequest("카카오뱅크", "090", "3333-01-1234567", "김철수", null, null));
        assertThat(acc1.bankName()).isEqualTo("카카오뱅크");
        assertThat(acc1.orderIndex()).isEqualTo(0);

        AccountResponse acc2 = eventService.createAccount(group.id(), OWNER_ID,
                new AccountRequest("신한은행", "088", "110-123-456789", "김철수", null, null));
        assertThat(acc2.orderIndex()).isEqualTo(1);

        assertThat(eventService.getAccounts(group.id())).hasSize(2);

        eventService.deleteAccount(acc1.id(), OWNER_ID);
        assertThat(accountRepository.findById(acc1.id())).isEmpty();

        eventService.deleteAccountGroup(group.id(), OWNER_ID);
        assertThat(accountGroupRepository.findById(group.id())).isEmpty();
    }

    @Test
    @DisplayName("계좌 그룹 초과 생성 실패 - 최대 4개")
    void createAccountGroup_fail_limitExceeded() {
        EventResponse event = createWedding("ag-limit-test");
        for (int i = 0; i < 4; i++) {
            eventService.createAccountGroup(event.id(), OWNER_ID, new AccountGroupRequest("그룹" + i, i));
        }

        assertThatThrownBy(() ->
                eventService.createAccountGroup(event.id(), OWNER_ID, new AccountGroupRequest("초과", 4)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("계좌 초과 생성 실패 - 그룹당 최대 3개")
    void createAccount_fail_limitExceeded() {
        EventResponse event = createWedding("acc-limit-test");
        AccountGroupResponse group = eventService.createAccountGroup(event.id(), OWNER_ID,
                new AccountGroupRequest("그룹", 0));
        for (int i = 0; i < 3; i++) {
            eventService.createAccount(group.id(), OWNER_ID,
                    new AccountRequest("은행" + i, "00" + i, "1234-" + i, "홀더", null, null));
        }

        assertThatThrownBy(() ->
                eventService.createAccount(group.id(), OWNER_ID,
                        new AccountRequest("초과은행", "099", "9999", "홀더", null, null)))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.ACCOUNT_LIMIT_EXCEEDED.getMessage());
    }

    // ─── createEventWithDetails ───────────────────────────────────────────────

    @Test
    @DisplayName("복합 생성 성공 - WEDDING: heroImages/schedules/transportation/announcements/hosts/accountGroups 포함")
    void createEventWithDetails_wedding_success() {
        HostRequest groomHost = new HostRequest(
                Host.HostRole.GROOM, "김철수", "groom@test.com", "010-0000-0001", null, null,
                new HostRequest.WeddingHostData("김아버지", "김어머니", true, true)
        );
        HostRequest brideHost = new HostRequest(
                Host.HostRole.BRIDE, "이영희", "bride@test.com", "010-0000-0002", null, null,
                new HostRequest.WeddingHostData("이아버지", "이어머니", true, false)
        );

        EventCreateRequest req = new EventCreateRequest(
                new EventRequest("결혼식", "wedding-full", EventType.WEDDING,
                        LocalDateTime.of(2026, 12, 25, 14, 0),
                        "그랜드홀", "서울 강남구 테헤란로 1", null,
                        true, "식사 안내", "주차 안내", "식사 제공", "환영합니다"),
                List.of(new HeroImageRequest("https://img.example.com/1.jpg", null, 0)),
                List.of(new ScheduleRequest("신랑 입장", null, 0)),
                List.of(new AccountGroupWithAccountsRequest("신랑측", 0,
                        List.of(new AccountRequest("카카오뱅크", "090", "3333-0", "김철수", null, null)))),
                List.of(new TransportationRequest(Transportation.TransportType.SUBWAY, "삼성역", "10분", 0)),
                List.of(new AnnouncementRequest("공지", "내용", false)),
                List.of(groomHost, brideHost)
        );

        EventInfoResponse result = eventService.createEventWithDetails(OWNER_ID, req);

        assertThat(result.event().slug()).isEqualTo("wedding-full");
        assertThat(result.heroImages()).hasSize(1);
        assertThat(result.schedules()).hasSize(1);
        assertThat(result.transportation()).hasSize(1);
        assertThat(result.announcements()).hasSize(1);
        assertThat(result.accountGroups()).hasSize(1);
        assertThat(result.accountGroups().get(0).accounts()).hasSize(1);
        assertThat(result.detail()).isInstanceOf(WeddingDetailResponse.class);
        WeddingDetailResponse detail = (WeddingDetailResponse) result.detail();
        assertThat(detail.getHosts()).hasSize(2);
        assertThat(detail.getNotice()).isEqualTo("식사 안내");
    }

    @Test
    @DisplayName("복합 생성 성공 - GATHERING: host만 생성")
    void createEventWithDetails_gathering_success() {
        EventCreateRequest req = new EventCreateRequest(
                gatheringRequest("gathering-full"),
                null, null, null, null, null,
                List.of(new HostRequest(Host.HostRole.HOST, "이진행", null, null, null, null, null))
        );

        EventInfoResponse result = eventService.createEventWithDetails(OWNER_ID, req);

        assertThat(result.event().slug()).isEqualTo("gathering-full");
        assertThat(result.detail()).isInstanceOf(GatheringDetailResponse.class);
        GatheringDetailResponse detail = (GatheringDetailResponse) result.detail();
        assertThat(detail.getHosts()).hasSize(1);
        assertThat(detail.getHosts().get(0).getName()).isEqualTo("이진행");
    }

    @Test
    @DisplayName("복합 생성 실패 - AccountGroup 5개 초과")
    void createEventWithDetails_fail_accountGroupLimitExceeded() {
        List<AccountGroupWithAccountsRequest> tooMany = List.of(
                new AccountGroupWithAccountsRequest("그룹1", 0, null),
                new AccountGroupWithAccountsRequest("그룹2", 1, null),
                new AccountGroupWithAccountsRequest("그룹3", 2, null),
                new AccountGroupWithAccountsRequest("그룹4", 3, null),
                new AccountGroupWithAccountsRequest("그룹5", 4, null)
        );
        EventCreateRequest req = new EventCreateRequest(
                gatheringRequest("ag-overflow"),
                null, null, tooMany, null, null,
                List.of(new HostRequest(Host.HostRole.HOST, "호스트", null, null, null, null, null))
        );

        assertThatThrownBy(() -> eventService.createEventWithDetails(OWNER_ID, req))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED.getMessage());
    }
}

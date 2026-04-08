package com.soaengry.moment.domain.event.service;

import com.soaengry.moment.domain.event.dto.request.*;
import com.soaengry.moment.domain.event.dto.response.*;
import com.soaengry.moment.domain.event.entity.Transportation;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.exception.EventErrorCode;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.event.repository.*;
import com.soaengry.moment.global.service.KakaoGeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private HeroImageRepository heroImageRepository;

    @Autowired
    private TransportationRepository transportationRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @MockitoBean
    private KakaoGeocodingService kakaoGeocodingService;

    private static final Long OWNER_ID = 1L;
    private static final Long OTHER_ID = 2L;

    @BeforeEach
    void setUp() {
        when(kakaoGeocodingService.geocode(anyString()))
                .thenReturn(new KakaoGeocodingService.Coordinate(37.5665, 126.9780));
    }

    private EventRequest sampleRequest(String slug) {
        return new EventRequest(
                "김철수 ❤️ 이영희 결혼식",
                slug,
                EventType.WEDDING,
                LocalDate.of(2026, 12, 25),
                "그랜드 컨벤션 센터",
                "서울특별시 강남구 테헤란로 152",
                "3층 그랜드홀"
        );
    }

    // ─── Event CRUD ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("이벤트 생성 성공")
    void createEvent_success() {
        // when
        EventResponse result = eventService.createEvent(OWNER_ID, sampleRequest("wedding-001"));

        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.slug()).isEqualTo("wedding-001");
        assertThat(result.title()).isEqualTo("김철수 ❤️ 이영희 결혼식");
        assertThat(result.type()).isEqualTo(EventType.WEDDING);
        assertThat(result.locationName()).isEqualTo("그랜드 컨벤션 센터");

        System.out.println("✅ 이벤트 생성 성공 - slug: " + result.slug());
    }

    @Test
    @DisplayName("이벤트 생성 실패 - 슬러그 중복")
    void createEvent_fail_slugDuplicated() {
        // given
        eventService.createEvent(OWNER_ID, sampleRequest("duplicate-slug"));

        // when & then
        assertThatThrownBy(() -> eventService.createEvent(OWNER_ID, sampleRequest("duplicate-slug")))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_SLUG_DUPLICATED.getMessage());

        System.out.println("✅ 슬러그 중복 예외 확인");
    }

    @Test
    @DisplayName("이벤트 단건 조회 성공")
    void getEvent_success() {
        // given
        EventResponse created = eventService.createEvent(OWNER_ID, sampleRequest("get-test"));

        // when
        EventResponse result = eventService.getEvent(created.id());

        // then
        assertThat(result.id()).isEqualTo(created.id());
        assertThat(result.slug()).isEqualTo("get-test");

        System.out.println("✅ 이벤트 단건 조회 성공 - id: " + result.id());
    }

    @Test
    @DisplayName("이벤트 단건 조회 실패 - 존재하지 않는 ID")
    void getEvent_fail_notFound() {
        assertThatThrownBy(() -> eventService.getEvent(999L))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());

        System.out.println("✅ 이벤트 없음 예외 확인");
    }

    @Test
    @DisplayName("이벤트 수정 성공")
    void updateEvent_success() {
        // given
        EventResponse created = eventService.createEvent(OWNER_ID, sampleRequest("update-test"));
        EventRequest updateRequest = new EventRequest(
                "수정된 결혼식 제목",
                "update-test",
                EventType.WEDDING,
                LocalDate.of(2027, 6, 15),
                "롯데 시그니엘",
                "서울특별시 송파구 올림픽로 300",
                null
        );

        // when
        EventResponse result = eventService.updateEvent(created.id(), OWNER_ID, updateRequest);

        // then
        assertThat(result.title()).isEqualTo("수정된 결혼식 제목");
        assertThat(result.date()).isEqualTo(LocalDate.of(2027, 6, 15));
        assertThat(result.locationName()).isEqualTo("롯데 시그니엘");

        System.out.println("✅ 이벤트 수정 성공");
    }

    @Test
    @DisplayName("이벤트 수정 실패 - 권한 없음")
    void updateEvent_fail_unauthorized() {
        // given
        EventResponse created = eventService.createEvent(OWNER_ID, sampleRequest("auth-test"));

        // when & then
        assertThatThrownBy(() -> eventService.updateEvent(created.id(), OTHER_ID, sampleRequest("auth-test")))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());

        System.out.println("✅ 이벤트 수정 권한 없음 예외 확인");
    }

    @Test
    @DisplayName("이벤트 삭제 성공")
    void deleteEvent_success() {
        // given
        EventResponse created = eventService.createEvent(OWNER_ID, sampleRequest("delete-test"));

        // when
        eventService.deleteEvent(created.id(), OWNER_ID);

        // then
        assertThat(eventRepository.findById(created.id())).isEmpty();

        System.out.println("✅ 이벤트 삭제 성공");
    }

    @Test
    @DisplayName("이벤트 삭제 실패 - 권한 없음")
    void deleteEvent_fail_unauthorized() {
        // given
        EventResponse created = eventService.createEvent(OWNER_ID, sampleRequest("del-auth-test"));

        // when & then
        assertThatThrownBy(() -> eventService.deleteEvent(created.id(), OTHER_ID))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_UNAUTHORIZED.getMessage());

        System.out.println("✅ 이벤트 삭제 권한 없음 예외 확인");
    }

    // ─── Slug & Info ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("슬러그 중복 확인 - 존재하는 슬러그")
    void checkSlugExists_true() {
        // given
        eventService.createEvent(OWNER_ID, sampleRequest("existing-slug"));

        // when & then
        assertThat(eventService.checkSlugExists("existing-slug")).isTrue();

        System.out.println("✅ 슬러그 중복 확인 - true 반환");
    }

    @Test
    @DisplayName("슬러그 중복 확인 - 없는 슬러그")
    void checkSlugExists_false() {
        assertThat(eventService.checkSlugExists("no-such-slug")).isFalse();

        System.out.println("✅ 슬러그 중복 확인 - false 반환");
    }

    @Test
    @DisplayName("이벤트 공개 정보 조회 성공 - heroImages/transportation/announcements 포함")
    void getEventInfo_success() {
        // given
        EventResponse event = eventService.createEvent(OWNER_ID, sampleRequest("info-test"));
        eventService.addHeroImage(event.id(), OWNER_ID, new HeroImageRequest("https://s3.example.com/img.jpg", null, 0));
        eventService.addTransportation(event.id(), OWNER_ID,
                new TransportationRequest(Transportation.TransportType.SUBWAY, "2호선 삼성역", "도보 10분", 0));
        eventService.addAnnouncement(event.id(), OWNER_ID, new AnnouncementRequest("공지 제목", "공지 내용", true));

        // when
        EventInfoResponse result = eventService.getEventInfo("info-test");

        // then
        assertThat(result.event().slug()).isEqualTo("info-test");
        assertThat(result.heroImages()).hasSize(1);
        assertThat(result.transportation()).hasSize(1);
        assertThat(result.announcements()).hasSize(1);
        assertThat(result.announcements().get(0).title()).isEqualTo("공지 제목");

        System.out.println("✅ 이벤트 공개 정보 조회 성공");
    }

    @Test
    @DisplayName("이벤트 공개 정보 조회 실패 - 없는 슬러그")
    void getEventInfo_fail_notFound() {
        assertThatThrownBy(() -> eventService.getEventInfo("ghost-slug"))
                .isInstanceOf(EventException.class)
                .hasMessage(EventErrorCode.EVENT_NOT_FOUND.getMessage());

        System.out.println("✅ 없는 슬러그 예외 확인");
    }

    // ─── HeroImage ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("히어로 이미지 추가 및 조회 성공")
    void heroImage_addAndGet_success() {
        // given
        EventResponse event = eventService.createEvent(OWNER_ID, sampleRequest("hero-test"));

        // when
        HeroImageResponse added = eventService.addHeroImage(event.id(), OWNER_ID,
                new HeroImageRequest("https://s3.example.com/img.jpg", null, 0));
        List<HeroImageResponse> images = eventService.getHeroImages(event.id());

        // then
        assertThat(added.imageUrl()).isEqualTo("https://s3.example.com/img.jpg");
        assertThat(images).hasSize(1);

        System.out.println("✅ 히어로 이미지 추가/조회 성공");
    }

    @Test
    @DisplayName("히어로 이미지 삭제 성공")
    void heroImage_delete_success() {
        // given
        EventResponse event = eventService.createEvent(OWNER_ID, sampleRequest("hero-del-test"));
        HeroImageResponse added = eventService.addHeroImage(event.id(), OWNER_ID,
                new HeroImageRequest("https://s3.example.com/img.jpg", null, 0));

        // when
        eventService.deleteHeroImage(added.id(), OWNER_ID);

        // then
        assertThat(heroImageRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 히어로 이미지 삭제 성공");
    }

    // ─── Transportation ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("교통편 추가/수정/삭제 성공")
    void transportation_crud_success() {
        // given
        EventResponse event = eventService.createEvent(OWNER_ID, sampleRequest("trans-test"));

        // add
        TransportationResponse added = eventService.addTransportation(event.id(), OWNER_ID,
                new TransportationRequest(Transportation.TransportType.SUBWAY, "2호선 삼성역", "도보 10분", 0));
        assertThat(added.title()).isEqualTo("2호선 삼성역");

        // update
        TransportationResponse updated = eventService.updateTransportation(added.id(), OWNER_ID,
                new TransportationRequest(Transportation.TransportType.BUS, "146번 버스", "도보 5분", 0));
        assertThat(updated.title()).isEqualTo("146번 버스");
        assertThat(updated.type()).isEqualTo(Transportation.TransportType.BUS);

        // delete
        eventService.deleteTransportation(added.id(), OWNER_ID);
        assertThat(transportationRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 교통편 추가/수정/삭제 성공");
    }

    // ─── Announcement ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("공지사항 추가/수정/삭제 성공")
    void announcement_crud_success() {
        // given
        EventResponse event = eventService.createEvent(OWNER_ID, sampleRequest("announce-test"));

        // add
        AnnouncementResponse added = eventService.addAnnouncement(event.id(), OWNER_ID,
                new AnnouncementRequest("공지 제목", "공지 내용입니다", false));
        assertThat(added.title()).isEqualTo("공지 제목");
        assertThat(added.isPinned()).isFalse();

        // update
        AnnouncementResponse updated = eventService.updateAnnouncement(added.id(), OWNER_ID,
                new AnnouncementRequest("수정된 제목", "수정된 내용", true));
        assertThat(updated.title()).isEqualTo("수정된 제목");
        assertThat(updated.isPinned()).isTrue();

        // delete
        eventService.deleteAnnouncement(added.id(), OWNER_ID);
        assertThat(announcementRepository.findById(added.id())).isEmpty();

        System.out.println("✅ 공지사항 추가/수정/삭제 성공");
    }
}

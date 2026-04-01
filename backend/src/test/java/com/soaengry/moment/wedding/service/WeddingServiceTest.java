package com.soaengry.moment.wedding.service;

import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.feed.repository.BookmarkRepository;
import com.soaengry.moment.domain.feed.repository.CommentRepository;
import com.soaengry.moment.domain.feed.repository.PostImageRepository;
import com.soaengry.moment.domain.feed.repository.PostLikeRepository;
import com.soaengry.moment.domain.feed.repository.PostRepository;
import com.soaengry.moment.domain.guestbook.repository.GuestbookEntryRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.dto.request.*;
import com.soaengry.moment.domain.wedding.dto.response.*;
import com.soaengry.moment.domain.wedding.entity.*;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.*;
import com.soaengry.moment.domain.wedding.service.AccommodationService;
import com.soaengry.moment.domain.wedding.service.AccountService;
import com.soaengry.moment.domain.wedding.service.AnnouncementService;
import com.soaengry.moment.domain.wedding.service.CoupleService;
import com.soaengry.moment.domain.wedding.service.GalleryService;
import com.soaengry.moment.domain.wedding.service.ScheduleService;
import com.soaengry.moment.domain.wedding.service.TransportationService;
import com.soaengry.moment.domain.wedding.service.WeddingService;
import com.soaengry.moment.global.service.KakaoGeocodingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
class WeddingServiceTest {

    @Autowired
    private WeddingService weddingService;

    @Autowired
    private CoupleService coupleService;

    @Autowired
    private ScheduleService scheduleService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private GalleryService galleryService;

    @Autowired
    private TransportationService transportationService;

    @Autowired
    private AccommodationService accommodationService;

    @Autowired
    private AnnouncementService announcementService;

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

    @Autowired
    private GalleryRepository galleryRepository;

    @Autowired
    private TransportationRepository transportationRepository;

    @Autowired
    private AccommodationRepository accommodationRepository;

    @Autowired
    private AnnouncementRepository announcementRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private GuestbookEntryRepository guestbookEntryRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private PostLikeRepository postLikeRepository;

    @Autowired
    private BookmarkRepository bookmarkRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    @Autowired
    private PostRepository postRepository;

    @MockitoBean
    private KakaoGeocodingService kakaoGeocodingService;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        // 데이터 정리 (FK 제약 조건 순서 고려, deleteAllInBatch로 즉시 실행)
        announcementRepository.deleteAllInBatch();
        accommodationRepository.deleteAllInBatch();
        transportationRepository.deleteAllInBatch();
        galleryRepository.deleteAllInBatch();
        accountRepository.deleteAllInBatch();
        accountGroupRepository.deleteAllInBatch();
        scheduleRepository.deleteAllInBatch();
        coupleRepository.deleteAllInBatch();
        attendanceRepository.deleteAllInBatch();
        commentRepository.deleteAllInBatch();
        postLikeRepository.deleteAllInBatch();
        bookmarkRepository.deleteAllInBatch();
        postImageRepository.deleteAllInBatch();
        postRepository.deleteAllInBatch();
        guestbookEntryRepository.deleteAllInBatch();
        weddingRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // Mock Kakao Geocoding Service
        when(kakaoGeocodingService.geocode(anyString()))
                .thenReturn(new KakaoGeocodingService.Coordinate(37.5, 127.0));

        // 테스트 사용자 생성
        testUser = userRepository.save(User.builder()
                .email("test@example.com")
                .password("Password123!@")
                .nickname("테스터")
                .profileImageUrl(null)
                .role(User.Role.USER)
                .authProvider(User.AuthProvider.LOCAL)
                .isEmailVerified(true)
                .build());

        adminUser = userRepository.save(User.builder()
                .email("admin@example.com")
                .password("Password123!@")
                .nickname("관리자")
                .profileImageUrl(null)
                .role(User.Role.ADMIN)
                .authProvider(User.AuthProvider.LOCAL)
                .isEmailVerified(true)
                .build());
    }

    // ==================== Wedding CRUD ====================

    @Test
    @DisplayName("Wedding 생성 성공")
    void createWedding_Success() {
        // given
        WeddingRequest request = new WeddingRequest(
                "김철수 ❤️ 이영희 결혼식",
                "suhee",
                LocalDateTime.of(2024, 6, 15, 14, 0),
                "그랜드컨벤션센터",
                "서울시 강남구 테헤란로 123",
                "3층 그랜드홀",
                "02-1234-5678",
                "편안한 캐주얼 복장",
                "주차는 건물 지하 2층에서 가능합니다.",
                "건물 지하 2-3층 무료 주차 가능 (3시간)",
                "뷔페 식사 제공"
        );

        // when
        WeddingResponse response = weddingService.createWedding(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("김철수 ❤️ 이영희 결혼식");
        assertThat(response.venueName()).isEqualTo("그랜드컨벤션센터");
        assertThat(response.venueLat()).isEqualTo(37.5);
        assertThat(response.venueLng()).isEqualTo(127.0);
    }

    @Test
    @DisplayName("Wedding 조회 성공")
    void getWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        // when
        WeddingResponse response = weddingService.getWedding(wedding.getId());

        // then
        assertThat(response.id()).isEqualTo(wedding.getId());
        assertThat(response.title()).isEqualTo("제목");
    }

    @Test
    @DisplayName("존재하지 않는 Wedding 조회 시 예외 발생")
    void getWedding_NotFound() {
        // when & then
        assertThatThrownBy(() -> weddingService.getWedding(999L))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("Wedding 수정 성공 - 커플(이메일 일치)이 수정")
    void updateWedding_Success_AsCouple() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        WeddingRequest updateRequest = new WeddingRequest(
                "변경된 제목",
                "ddd",
                LocalDateTime.of(2024, 7, 20, 15, 0),
                "변경된 장소",
                "변경된 주소",
                null, "010-1234-1234", "dress code", null, null, null
        );

        // when
        WeddingResponse response = weddingService.updateWedding(wedding.getId(), testUser.getId(), updateRequest);

        // then
        assertThat(response.title()).isEqualTo("변경된 제목");
        assertThat(response.venueName()).isEqualTo("변경된 장소");
    }

    @Test
    @DisplayName("Wedding 수정 성공 - ADMIN이 수정")
    void updateWedding_Success_AsAdmin() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        WeddingRequest updateRequest = new WeddingRequest(
                "관리자 변경",
                "ddd",
                LocalDateTime.of(2024, 7, 20, 15, 0),
                "변경된 장소",
                "변경된 주소",
                null, null, null, null, null, null
        );

        // when
        WeddingResponse response = weddingService.updateWedding(wedding.getId(), adminUser.getId(), updateRequest);

        // then
        assertThat(response.title()).isEqualTo("관리자 변경");
    }

    @Test
    @DisplayName("Wedding 수정 실패 - 권한 없음")
    void updateWedding_Fail_Unauthorized() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        WeddingRequest updateRequest = new WeddingRequest(
                "변경 시도",
                "ddd",
                LocalDateTime.now(),
                "변경된 장소",
                "변경된 주소",
                null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> weddingService.updateWedding(wedding.getId(), testUser.getId(), updateRequest))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("Wedding 삭제 성공")
    void deleteWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        // when
        weddingService.deleteWedding(wedding.getId(), testUser.getId());

        // then
        assertThat(weddingRepository.findById(wedding.getId())).isEmpty();
    }

    @Test
    @DisplayName("Wedding 삭제 실패 - 권한 없음")
    void deleteWedding_Fail_Unauthorized() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        // when & then
        assertThatThrownBy(() -> weddingService.deleteWedding(wedding.getId(), testUser.getId()))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_UNAUTHORIZED.getMessage());
    }

    @Test
    @DisplayName("초대장 ID 중복 체크 - 존재함")
    void checkInvitationIdExists_True() {
        // given
        weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("my-wedding-2024")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        // when
        boolean exists = weddingService.checkInvitationIdExists("my-wedding-2024");

        // then
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("초대장 ID 중복 체크 - 존재하지 않음")
    void checkInvitationIdExists_False() {
        // when
        boolean exists = weddingService.checkInvitationIdExists("unique-id");

        // then
        assertThat(exists).isFalse();
    }

    // ==================== Couple CRUD ====================

    @Test
    @DisplayName("Couple 생성 성공")
    void createCouple_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        CoupleRequest request = new CoupleRequest(
                Couple.CoupleRole.BRIDE,
                "이영희",
                "bride@example.com",
                "이아버지",
                "박어머니",
                true,
                true,
                "010-1234-5678",
                "https://example.com/bride.jpg",
                "안녕하세요. 신부 이영희입니다."
        );

        // when
        CoupleResponse response = coupleService.createCouple(wedding.getId(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("이영희");
        assertThat(response.role()).isEqualTo(Couple.CoupleRole.BRIDE);
    }

    @Test
    @DisplayName("Wedding의 Couple 목록 조회 성공")
    void getCouplesByWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        coupleService.createCouple(wedding.getId(), testUser.getId(), new CoupleRequest(
                Couple.CoupleRole.BRIDE, "이영희", "bride@example.com", "이아버지", "최어머니",
                true, false, "010-2222-2222", null, "신부입니다"
        ));

        // when
        List<CoupleResponse> couples = coupleService.getCouplesByWedding(wedding.getId());

        // then
        assertThat(couples).hasSize(2);
        assertThat(couples.get(0).role()).isEqualTo(Couple.CoupleRole.BRIDE);
        assertThat(couples.get(1).role()).isEqualTo(Couple.CoupleRole.GROOM);
    }

    @Test
    @DisplayName("Couple 수정 성공")
    void updateCouple_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        Couple couple = coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        CoupleRequest updateRequest = new CoupleRequest(
                Couple.CoupleRole.GROOM,
                "김철수 수정",
                testUser.getEmail(),
                "김부친",
                "박모친",
                false,
                true,
                "010-9999-9999",
                "https://new.jpg",
                "수정된 소개"
        );

        // when
        CoupleResponse response = coupleService.updateCouple(couple.getId(), testUser.getId(), updateRequest);

        // then
        assertThat(response.name()).isEqualTo("김철수 수정");
        assertThat(response.fatherName()).isEqualTo("김부친");
        assertThat(response.contact()).isEqualTo("010-9999-9999");
    }

    @Test
    @DisplayName("Couple 삭제 성공")
    void deleteCouple_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        Couple couple = coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        // when
        coupleService.deleteCouple(couple.getId(), testUser.getId());

        // then
        assertThat(coupleRepository.findById(couple.getId())).isEmpty();
    }

    // ==================== Schedule CRUD ====================

    @Test
    @DisplayName("Schedule 생성 성공")
    void createSchedule_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        ScheduleRequest request = new ScheduleRequest(
                LocalTime.of(14, 0),
                "신랑 신부 입장",
                "양가 부모님과 함께 입장합니다",
                1
        );

        // when
        ScheduleResponse response = scheduleService.createSchedule(wedding.getId(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("신랑 신부 입장");
        assertThat(response.time()).isEqualTo(LocalTime.of(14, 0));
    }

    @Test
    @DisplayName("Wedding의 Schedule 목록 조회 성공")
    void getSchedulesByWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        scheduleService.createSchedule(wedding.getId(), testUser.getId(),
                new ScheduleRequest(LocalTime.of(14, 0), "입장", "설명1", 1));
        scheduleService.createSchedule(wedding.getId(), testUser.getId(),
                new ScheduleRequest(LocalTime.of(15, 0), "식사", "설명2", 2));

        // when
        List<ScheduleResponse> schedules = scheduleService.getSchedulesByWedding(wedding.getId());

        // then
        assertThat(schedules).hasSize(2);
        assertThat(schedules.get(0).title()).isEqualTo("입장");
        assertThat(schedules.get(1).title()).isEqualTo("식사");
    }

    @Test
    @DisplayName("Schedule 수정 성공")
    void updateSchedule_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        ScheduleResponse created = scheduleService.createSchedule(wedding.getId(), testUser.getId(),
                new ScheduleRequest(LocalTime.of(14, 0), "입장", "설명", 1));

        ScheduleRequest updateRequest = new ScheduleRequest(
                LocalTime.of(15, 30),
                "수정된 제목",
                "수정된 설명",
                2
        );

        // when
        ScheduleResponse response = scheduleService.updateSchedule(created.id(), testUser.getId(), updateRequest);

        // then
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.time()).isEqualTo(LocalTime.of(15, 30));
        assertThat(response.orderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("Schedule 삭제 성공")
    void deleteSchedule_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        ScheduleResponse created = scheduleService.createSchedule(wedding.getId(), testUser.getId(),
                new ScheduleRequest(LocalTime.of(14, 0), "입장", "설명", 1));

        // when
        scheduleService.deleteSchedule(created.id(), testUser.getId());

        // then
        assertThat(scheduleRepository.findById(created.id())).isEmpty();
    }

    // ==================== AccountGroup CRUD ====================

    @Test
    @DisplayName("AccountGroup 생성 성공")
    void createAccountGroup_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroupRequest request = new AccountGroupRequest(
                AccountGroup.Side.GROOM,
                "신랑측",
                1
        );

        // when
        AccountGroupResponse response = accountService.createAccountGroup(wedding.getId(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.side()).isEqualTo(AccountGroup.Side.GROOM);
        assertThat(response.groupName()).isEqualTo("신랑측");
    }

    @Test
    @DisplayName("AccountGroup 생성 시 최대 4개 제한 테스트")
    void createAccountGroup_LimitExceeded() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        accountGroupRepository.save(AccountGroup.create(wedding.getId(), AccountGroup.Side.GROOM, "신랑측1", 1));
        accountGroupRepository.save(AccountGroup.create(wedding.getId(), AccountGroup.Side.BRIDE, "신부측1", 2));
        accountGroupRepository.save(AccountGroup.create(wedding.getId(), AccountGroup.Side.GROOM, "신랑측2", 3));
        accountGroupRepository.save(AccountGroup.create(wedding.getId(), AccountGroup.Side.BRIDE, "신부측2", 4));

        AccountGroupRequest request = new AccountGroupRequest(
                AccountGroup.Side.GROOM, "다섯번째 그룹", 5
        );

        // when & then
        assertThatThrownBy(() -> accountService.createAccountGroup(wedding.getId(), testUser.getId(), request))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.ACCOUNT_GROUP_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("Wedding의 AccountGroup 목록 조회 성공")
    void getAccountGroupsByWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1));
        accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.BRIDE, "신부측", 2));

        // when
        List<AccountGroupResponse> groups = accountService.getAccountGroupsByWedding(wedding.getId());

        // then
        assertThat(groups).hasSize(2);
        assertThat(groups.get(0).groupName()).isEqualTo("신랑측");
        assertThat(groups.get(1).groupName()).isEqualTo("신부측");
    }

    @Test
    @DisplayName("AccountGroup 수정 성공")
    void updateAccountGroup_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroupResponse created = accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1));

        AccountGroupRequest updateRequest = new AccountGroupRequest(
                AccountGroup.Side.BRIDE,
                "신부측으로 변경",
                2
        );

        // when
        AccountGroupResponse response = accountService.updateAccountGroup(created.id(), testUser.getId(), updateRequest);

        // then
        assertThat(response.side()).isEqualTo(AccountGroup.Side.BRIDE);
        assertThat(response.groupName()).isEqualTo("신부측으로 변경");
        assertThat(response.orderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("AccountGroup 삭제 성공")
    void deleteAccountGroup_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroupResponse created = accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1));

        // when
        accountService.deleteAccountGroup(created.id(), testUser.getId());

        // then
        assertThat(accountGroupRepository.findById(created.id())).isEmpty();
    }

    // ==================== Account CRUD ====================

    @Test
    @DisplayName("Account 생성 성공")
    void createAccount_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroupResponse group = accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1));

        AccountRequest request = new AccountRequest(
                "신한은행",
                "088",
                "111-111-111111",
                "김철수",
                null,
                1
        );

        // when
        AccountResponse response = accountService.createAccount(group.id(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.bankName()).isEqualTo("신한은행");
        assertThat(response.accountHolder()).isEqualTo("김철수");
    }

    @Test
    @DisplayName("Account 생성 시 최대 3개 제한 테스트")
    void createAccount_LimitExceeded() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroup group = accountGroupRepository.save(
                AccountGroup.create(wedding.getId(), AccountGroup.Side.GROOM, "신랑측", 1)
        );

        accountRepository.save(Account.create(group.getId(), "신한은행", "088", "111-111-111111", "김철수", null, 1));
        accountRepository.save(Account.create(group.getId(), "국민은행", "004", "222-222-222222", "박철수", null, 2));
        accountRepository.save(Account.create(group.getId(), "우리은행", "020", "333-333-333333", "이철수", null, 3));

        AccountRequest request = new AccountRequest(
                "하나은행", "081", "444-444-444444", "최철수", null, 4
        );

        // when & then
        assertThatThrownBy(() -> accountService.createAccount(group.getId(), testUser.getId(), request))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.ACCOUNT_LIMIT_EXCEEDED.getMessage());
    }

    @Test
    @DisplayName("AccountGroup의 Account 목록 조회 성공")
    void getAccountsByGroup_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroupResponse group = accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1));

        accountService.createAccount(group.id(), testUser.getId(),
                new AccountRequest("신한은행", "088", "111-111-111111", "김철수", null, 1));
        accountService.createAccount(group.id(), testUser.getId(),
                new AccountRequest("국민은행", "004", "222-222-222222", "박철수", null, 2));

        // when
        List<AccountResponse> accounts = accountService.getAccountsByGroup(group.id());

        // then
        assertThat(accounts).hasSize(2);
        assertThat(accounts.get(0).bankName()).isEqualTo("신한은행");
        assertThat(accounts.get(1).bankName()).isEqualTo("국민은행");
    }

    @Test
    @DisplayName("Account 수정 성공")
    void updateAccount_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroupResponse group = accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1));

        AccountResponse created = accountService.createAccount(group.id(), testUser.getId(),
                new AccountRequest("신한은행", "088", "111-111-111111", "김철수", null, 1));

        AccountRequest updateRequest = new AccountRequest(
                "국민은행",
                "004",
                "999-999-999999",
                "이철수",
                "https://kakao.pay/link",
                2
        );

        // when
        AccountResponse response = accountService.updateAccount(created.id(), testUser.getId(), updateRequest);

        // then
        assertThat(response.bankName()).isEqualTo("국민은행");
        assertThat(response.accountNumber()).isEqualTo("999-999-999999");
        assertThat(response.accountHolder()).isEqualTo("이철수");
        assertThat(response.kakaoPayUrl()).isEqualTo("https://kakao.pay/link");
    }

    @Test
    @DisplayName("Account 삭제 성공")
    void deleteAccount_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccountGroupResponse group = accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1));

        AccountResponse created = accountService.createAccount(group.id(), testUser.getId(),
                new AccountRequest("신한은행", "088", "111-111-111111", "김철수", null, 1));

        // when
        accountService.deleteAccount(created.id(), testUser.getId());

        // then
        assertThat(accountRepository.findById(created.id())).isEmpty();
    }

    // ==================== Gallery CRUD ====================

    @Test
    @DisplayName("Gallery 생성 성공")
    void createGallery_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        GalleryRequest request = new GalleryRequest(
                "https://example.com/photo1.jpg",
                "https://example.com/thumb1.jpg",
                "우리의 첫만남",
                1
        );

        // when
        GalleryResponse response = galleryService.createGallery(wedding.getId(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.imageUrl()).isEqualTo("https://example.com/photo1.jpg");
        assertThat(response.caption()).isEqualTo("우리의 첫만남");
    }

    @Test
    @DisplayName("Wedding의 Gallery 목록 조회 성공")
    void getGalleriesByWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        galleryService.createGallery(wedding.getId(), testUser.getId(),
                new GalleryRequest("https://example.com/1.jpg", null, "첫번째", 1));
        galleryService.createGallery(wedding.getId(), testUser.getId(),
                new GalleryRequest("https://example.com/2.jpg", null, "두번째", 2));

        // when
        List<GalleryResponse> galleries = galleryService.getGalleriesByWedding(wedding.getId());

        // then
        assertThat(galleries).hasSize(2);
        assertThat(galleries.get(0).caption()).isEqualTo("첫번째");
        assertThat(galleries.get(1).caption()).isEqualTo("두번째");
    }

    @Test
    @DisplayName("Gallery 수정 성공")
    void updateGallery_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        GalleryResponse created = galleryService.createGallery(wedding.getId(), testUser.getId(),
                new GalleryRequest("https://example.com/1.jpg", null, "첫번째", 1));

        GalleryRequest updateRequest = new GalleryRequest(
                "https://example.com/1.jpg",
                null,
                "수정된 캡션",
                2
        );

        // when
        GalleryResponse response = galleryService.updateGallery(created.id(), testUser.getId(), updateRequest);

        // then
        assertThat(response.caption()).isEqualTo("수정된 캡션");
        assertThat(response.orderIndex()).isEqualTo(2);
    }

    @Test
    @DisplayName("Gallery 삭제 성공")
    void deleteGallery_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        GalleryResponse created = galleryService.createGallery(wedding.getId(), testUser.getId(),
                new GalleryRequest("https://example.com/1.jpg", null, "첫번째", 1));

        // when
        galleryService.deleteGallery(created.id(), testUser.getId());

        // then
        assertThat(galleryRepository.findById(created.id())).isEmpty();
    }

    // ==================== Transportation CRUD ====================

    @Test
    @DisplayName("Transportation 생성 성공")
    void createTransportation_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        TransportationRequest request = new TransportationRequest(
                Transportation.TransportType.SUBWAY,
                "2호선 강남역",
                "1번 출구에서 도보 5분",
                1
        );

        // when
        TransportationResponse response = transportationService.createTransportation(wedding.getId(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.type()).isEqualTo(Transportation.TransportType.SUBWAY);
        assertThat(response.title()).isEqualTo("2호선 강남역");
    }

    @Test
    @DisplayName("Wedding의 Transportation 목록 조회 성공")
    void getTransportationsByWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        transportationService.createTransportation(wedding.getId(), testUser.getId(),
                new TransportationRequest(Transportation.TransportType.SUBWAY, "지하철", "설명1", 1));
        transportationService.createTransportation(wedding.getId(), testUser.getId(),
                new TransportationRequest(Transportation.TransportType.BUS, "버스", "설명2", 2));

        // when
        List<TransportationResponse> transportations = transportationService.getTransportationsByWedding(wedding.getId());

        // then
        assertThat(transportations).hasSize(2);
        assertThat(transportations.get(0).title()).isEqualTo("지하철");
        assertThat(transportations.get(1).title()).isEqualTo("버스");
    }

    @Test
    @DisplayName("Transportation 수정 성공")
    void updateTransportation_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        TransportationResponse created = transportationService.createTransportation(wedding.getId(), testUser.getId(),
                new TransportationRequest(Transportation.TransportType.SUBWAY, "지하철", "설명", 1));

        TransportationRequest updateRequest = new TransportationRequest(
                Transportation.TransportType.BUS,
                "수정된 제목",
                "수정된 설명",
                2
        );

        // when
        TransportationResponse response = transportationService.updateTransportation(created.id(), testUser.getId(), updateRequest);

        // then
        assertThat(response.type()).isEqualTo(Transportation.TransportType.BUS);
        assertThat(response.title()).isEqualTo("수정된 제목");
        assertThat(response.description()).isEqualTo("수정된 설명");
    }

    @Test
    @DisplayName("Transportation 삭제 성공")
    void deleteTransportation_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        TransportationResponse created = transportationService.createTransportation(wedding.getId(), testUser.getId(),
                new TransportationRequest(Transportation.TransportType.SUBWAY, "지하철", "설명", 1));

        // when
        transportationService.deleteTransportation(created.id(), testUser.getId());

        // then
        assertThat(transportationRepository.findById(created.id())).isEmpty();
    }

    // ==================== Accommodation CRUD ====================

    @Test
    @DisplayName("Accommodation 생성 성공")
    void createAccommodation_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccommodationRequest request = new AccommodationRequest(
                "그랜드 호텔",
                "서울시 강남구 테헤란로 456",
                "02-9999-8888",
                "도보 10분",
                "10만원~20만원",
                1
        );

        // when
        AccommodationResponse response = accommodationService.createAccommodation(wedding.getId(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.name()).isEqualTo("그랜드 호텔");
        assertThat(response.distance()).isEqualTo("도보 10분");
    }

    @Test
    @DisplayName("Wedding의 Accommodation 목록 조회 성공")
    void getAccommodationsByWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        accommodationService.createAccommodation(wedding.getId(), testUser.getId(),
                new AccommodationRequest("호텔A", "주소A", "02-1111-1111", "5분", "10만원", 1));
        accommodationService.createAccommodation(wedding.getId(), testUser.getId(),
                new AccommodationRequest("호텔B", "주소B", "02-2222-2222", "10분", "20만원", 2));

        // when
        List<AccommodationResponse> accommodations = accommodationService.getAccommodationsByWedding(wedding.getId());

        // then
        assertThat(accommodations).hasSize(2);
        assertThat(accommodations.get(0).name()).isEqualTo("호텔A");
        assertThat(accommodations.get(1).name()).isEqualTo("호텔B");
    }

    @Test
    @DisplayName("Accommodation 수정 성공")
    void updateAccommodation_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccommodationResponse created = accommodationService.createAccommodation(wedding.getId(), testUser.getId(),
                new AccommodationRequest("호텔A", "주소A", "02-1111-1111", "5분", "10만원", 1));

        AccommodationRequest updateRequest = new AccommodationRequest(
                "수정된 호텔",
                "수정된 주소",
                "02-9999-9999",
                "수정된 거리",
                "30만원~50만원",
                2
        );

        // when
        AccommodationResponse response = accommodationService.updateAccommodation(created.id(), testUser.getId(), updateRequest);

        // then
        assertThat(response.name()).isEqualTo("수정된 호텔");
        assertThat(response.address()).isEqualTo("수정된 주소");
        assertThat(response.priceRange()).isEqualTo("30만원~50만원");
    }

    @Test
    @DisplayName("Accommodation 삭제 성공")
    void deleteAccommodation_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AccommodationResponse created = accommodationService.createAccommodation(wedding.getId(), testUser.getId(),
                new AccommodationRequest("호텔A", "주소A", "02-1111-1111", "5분", "10만원", 1));

        // when
        accommodationService.deleteAccommodation(created.id(), testUser.getId());

        // then
        assertThat(accommodationRepository.findById(created.id())).isEmpty();
    }

    // ==================== Announcement CRUD ====================

    @Test
    @DisplayName("Announcement 생성 성공")
    void createAnnouncement_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AnnouncementRequest request = new AnnouncementRequest(
                "중요 공지사항",
                "시간이 변경되었습니다.",
                true
        );

        // when
        AnnouncementResponse response = announcementService.createAnnouncement(wedding.getId(), testUser.getId(), request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.title()).isEqualTo("중요 공지사항");
        assertThat(response.isPinned()).isTrue();
    }

    @Test
    @DisplayName("Wedding의 Announcement 목록 조회 성공")
    void getAnnouncementsByWedding_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        announcementService.createAnnouncement(wedding.getId(), testUser.getId(),
                new AnnouncementRequest("공지1", "내용1", false));
        announcementService.createAnnouncement(wedding.getId(), testUser.getId(),
                new AnnouncementRequest("공지2", "내용2", true));

        // when
        List<AnnouncementResponse> announcements = announcementService.getAnnouncementsByWedding(wedding.getId());

        // then
        assertThat(announcements).hasSize(2);
    }

    @Test
    @DisplayName("Announcement 수정 성공")
    void updateAnnouncement_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AnnouncementResponse created = announcementService.createAnnouncement(wedding.getId(), testUser.getId(),
                new AnnouncementRequest("공지", "내용", false));

        AnnouncementRequest updateRequest = new AnnouncementRequest(
                "수정된 공지",
                "수정된 내용",
                true
        );

        // when
        AnnouncementResponse response = announcementService.updateAnnouncement(created.id(), testUser.getId(), updateRequest);

        // then
        assertThat(response.title()).isEqualTo("수정된 공지");
        assertThat(response.content()).isEqualTo("수정된 내용");
        assertThat(response.isPinned()).isTrue();
    }

    @Test
    @DisplayName("Announcement 삭제 성공")
    void deleteAnnouncement_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        AnnouncementResponse created = announcementService.createAnnouncement(wedding.getId(), testUser.getId(),
                new AnnouncementRequest("공지", "내용", false));

        // when
        announcementService.deleteAnnouncement(created.id(), testUser.getId());

        // then
        assertThat(announcementRepository.findById(created.id())).isEmpty();
    }

    // ==================== 전체 정보 조회 ====================

    @Test
    @DisplayName("전체 Wedding 정보 조회 성공")
    void getWeddingInfo_Success() {
        // given
        Wedding wedding = weddingRepository.save(
                Wedding.builder()
                        .title("제목")
                        .invitationId("invitation-id")
                        .weddingDate(LocalDateTime.now())
                        .venueAddress("주소")
                        .venueName("장소")
                        .venueLat(37.5)
                        .venueLng(127.0)
                        .build()
        );

        coupleRepository.save(Couple.builder()
                .wedding(wedding)
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .email(testUser.getEmail())
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build());

        coupleService.createCouple(wedding.getId(), testUser.getId(), new CoupleRequest(
                Couple.CoupleRole.BRIDE, "이영희", "bride2@example.com", "이아버지", "박어머니",
                true, true, "010-1111-1111", null, "신부입니다"
        ));

        scheduleService.createSchedule(wedding.getId(), testUser.getId(), new ScheduleRequest(
                LocalTime.of(14, 0), "입장", "설명", 1
        ));

        AccountGroupResponse group = accountService.createAccountGroup(wedding.getId(), testUser.getId(),
                new AccountGroupRequest(AccountGroup.Side.GROOM, "신랑측", 1)
        );

        accountService.createAccount(group.id(), testUser.getId(),
                new AccountRequest("신한은행", "088", "111-111-111111", "김철수", null, 1)
        );

        // when
        WeddingInfoResponse response = weddingService.getWeddingInfo(wedding.getInvitationId());

        // then
        assertThat(response.wedding().id()).isEqualTo(wedding.getId());
        assertThat(response.couples()).hasSize(2);
        assertThat(response.schedules()).hasSize(1);
        assertThat(response.accountGroups()).hasSize(1);
        assertThat(response.accountGroups().get(0).accounts()).hasSize(1);
    }
}

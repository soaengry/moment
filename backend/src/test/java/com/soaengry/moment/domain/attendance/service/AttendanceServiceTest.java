package com.soaengry.moment.domain.attendance.service;

import com.soaengry.moment.domain.attendance.dto.request.AddAttendanceRequest;
import com.soaengry.moment.domain.attendance.dto.response.AttendanceResponse;
import com.soaengry.moment.domain.attendance.exception.AttendanceErrorCode;
import com.soaengry.moment.domain.attendance.exception.AttendanceException;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.wedding.entity.Couple;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.CoupleRepository;
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
class AttendanceServiceTest {

    @Autowired
    private AttendanceService attendanceService;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private CoupleRepository coupleRepository;

    private com.soaengry.moment.domain.user.entity.User testUser;
    private Wedding testWedding1;
    private Wedding testWedding2;

    @Autowired
    private com.soaengry.moment.domain.guestbook.repository.GuestbookEntryRepository guestbookEntryRepository;

    @Autowired
    private com.soaengry.moment.domain.feed.repository.CommentRepository commentRepository;

    @Autowired
    private com.soaengry.moment.domain.feed.repository.PostLikeRepository postLikeRepository;

    @Autowired
    private com.soaengry.moment.domain.feed.repository.BookmarkRepository bookmarkRepository;

    @Autowired
    private com.soaengry.moment.domain.feed.repository.PostImageRepository postImageRepository;

    @Autowired
    private com.soaengry.moment.domain.feed.repository.PostRepository postRepository;

    @Autowired
    private com.soaengry.moment.domain.user.repository.UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // 테스트 전 데이터 정리 (FK 제약 조건 순서 고려)
        commentRepository.deleteAll();
        postLikeRepository.deleteAll();
        bookmarkRepository.deleteAll();
        postImageRepository.deleteAll();
        postRepository.deleteAll();
        guestbookEntryRepository.deleteAll();
        attendanceRepository.deleteAll();
        coupleRepository.deleteAll();
        weddingRepository.deleteAll();
        userRepository.deleteAll();

        // User 생성
        testUser = com.soaengry.moment.domain.user.entity.User.builder()
                .email("testuser@example.com")
                .password("password")
                .nickname("테스트사용자")
                .isEmailVerified(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);

        // Wedding 1 생성
        testWedding1 = Wedding.builder()
                .title("김철수 ❤️ 이영희 결혼식")
                .invitationId("WEDDING001")
                .weddingDate(LocalDateTime.of(2026, 12, 25, 14, 0))
                .venueName("그랜드 컨벤션 센터")
                .venueAddress("서울특별시 강남구 테헤란로 152")
                .venueDetail("3층 그랜드홀")
                .venueLat(37.5012345)
                .venueLng(127.0361234)
                .venuePhone("02-1234-5678")
                .build();
        weddingRepository.save(testWedding1);

        // Couple 1 (Groom)
        Couple groom1 = Couple.builder()
                .wedding(testWedding1)
                .email("groom@example.com")
                .role(Couple.CoupleRole.GROOM)
                .name("김철수")
                .fatherName("김아버지")
                .motherName("김어머니")
                .isFatherAlive(true)
                .isMotherAlive(true)
                .contact("010-1111-1111")
                .profileImageUrl("https://s3.amazonaws.com/groom.jpg")
                .build();
        coupleRepository.save(groom1);

        // Couple 1 (Bride)
        Couple bride1 = Couple.builder()
                .wedding(testWedding1)
                .email("bride@example.com")
                .role(Couple.CoupleRole.BRIDE)
                .name("이영희")
                .fatherName("이아버지")
                .motherName("이어머니")
                .isFatherAlive(true)
                .isMotherAlive(true)
                .contact("010-2222-2222")
                .profileImageUrl("https://s3.amazonaws.com/bride.jpg")
                .build();
        coupleRepository.save(bride1);

        // Wedding 2 생성
        testWedding2 = Wedding.builder()
                .title("박민수 ❤️ 최지연 결혼식")
                .invitationId("WEDDING002")
                .weddingDate(LocalDateTime.of(2027, 6, 15, 15, 0))
                .venueName("로얄 웨딩홀")
                .venueAddress("서울특별시 서초구 서초대로 78")
                .venueDetail("5층 로얄홀")
                .venueLat(37.4876543)
                .venueLng(127.0123456)
                .venuePhone("02-9876-5432")
                .build();
        weddingRepository.save(testWedding2);
    }

    @Test
    @DisplayName("내 참석 목록 조회 (2건)")
    void getMyAttendances_TwoAttendances_Success() {
        // given
        attendanceService.addAttendance(testUser.getId(), new AddAttendanceRequest("WEDDING001"));
        attendanceService.addAttendance(testUser.getId(), new AddAttendanceRequest("WEDDING002"));

        // when
        List<AttendanceResponse> result = attendanceService.getMyAttendances(testUser.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(AttendanceResponse::invitationId)
                .containsExactly("WEDDING002", "WEDDING001"); // createdAt DESC

        AttendanceResponse first = result.get(1); // WEDDING001
        assertThat(first.title()).isEqualTo("김철수 ❤️ 이영희 결혼식");
        assertThat(first.groomName()).isEqualTo("김철수");
        assertThat(first.brideName()).isEqualTo("이영희");

        System.out.println("✅ 내 참석 목록 조회 성공");
        System.out.println("   - 참석 수: " + result.size());
        result.forEach(a -> System.out.println("   - " + a.title()));
    }

    @Test
    @DisplayName("빈 참석 목록 조회")
    void getMyAttendances_Empty_Success() {
        // when
        List<AttendanceResponse> result = attendanceService.getMyAttendances(testUser.getId());

        // then
        assertThat(result).isEmpty();

        System.out.println("✅ 빈 참석 목록 조회 성공");
    }

    @Test
    @DisplayName("삭제된 웨딩 필터링")
    void getMyAttendances_FilterDeletedWedding_Success() {
        // given
        attendanceService.addAttendance(testUser.getId(), new AddAttendanceRequest("WEDDING001"));
        attendanceService.addAttendance(testUser.getId(), new AddAttendanceRequest("WEDDING002"));

        // Wedding 1 삭제 (Couple 먼저 삭제)
        coupleRepository.deleteAll(coupleRepository.findByWeddingId(testWedding1.getId()));
        weddingRepository.delete(testWedding1);

        // when
        List<AttendanceResponse> result = attendanceService.getMyAttendances(testUser.getId());

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).invitationId()).isEqualTo("WEDDING002");

        System.out.println("✅ 삭제된 웨딩 필터링 성공");
        System.out.println("   - WEDDING001 삭제됨 → 필터링됨");
        System.out.println("   - WEDDING002만 조회됨");
    }

    @Test
    @DisplayName("참석 등록 성공 (invitation ID 기반)")
    void addAttendance_Success() {
        // given
        AddAttendanceRequest request = new AddAttendanceRequest("WEDDING001");

        // when
        AttendanceResponse result = attendanceService.addAttendance(testUser.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.weddingId()).isEqualTo(testWedding1.getId());
        assertThat(result.invitationId()).isEqualTo("WEDDING001");
        assertThat(result.title()).isEqualTo("김철수 ❤️ 이영희 결혼식");
        assertThat(result.groomName()).isEqualTo("김철수");
        assertThat(result.brideName()).isEqualTo("이영희");

        // DB 확인
        assertThat(attendanceRepository.existsByUserIdAndWeddingId(testUser.getId(), testWedding1.getId())).isTrue();

        System.out.println("✅ 참석 등록 성공");
        System.out.println("   - 초대장: " + result.invitationId());
        System.out.println("   - 제목: " + result.title());
    }

    @Test
    @DisplayName("참석 등록 실패 - 웨딩 없음")
    void addAttendance_WeddingNotFound_Fail() {
        // given
        AddAttendanceRequest request = new AddAttendanceRequest("INVALID_ID");

        // when & then
        assertThatThrownBy(() -> attendanceService.addAttendance(testUser.getId(), request))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 웨딩 없음 테스트 통과");
    }

    @Test
    @DisplayName("참석 등록 실패 - 중복 참석")
    void addAttendance_Duplicate_Fail() {
        // given
        AddAttendanceRequest request = new AddAttendanceRequest("WEDDING001");
        attendanceService.addAttendance(testUser.getId(), request);

        // when & then
        assertThatThrownBy(() -> attendanceService.addAttendance(testUser.getId(), request))
                .isInstanceOf(AttendanceException.class)
                .hasMessage(AttendanceErrorCode.DUPLICATE_ATTENDANCE.getMessage());

        System.out.println("✅ 중복 참석 테스트 통과");
    }

    @Test
    @DisplayName("참석 삭제 성공")
    void deleteAttendance_Success() {
        // given
        AttendanceResponse attendance = attendanceService.addAttendance(testUser.getId(), new AddAttendanceRequest("WEDDING001"));

        // when
        attendanceService.deleteAttendance(testUser.getId(), attendance.id());

        // then
        assertThat(attendanceRepository.existsById(attendance.id())).isFalse();

        System.out.println("✅ 참석 삭제 성공");
        System.out.println("   - 삭제된 참석 ID: " + attendance.id());
    }

    @Test
    @DisplayName("참석 삭제 실패 - Not Found")
    void deleteAttendance_NotFound_Fail() {
        // given
        Long invalidId = 999L;

        // when & then
        assertThatThrownBy(() -> attendanceService.deleteAttendance(testUser.getId(), invalidId))
                .isInstanceOf(AttendanceException.class)
                .hasMessage(AttendanceErrorCode.ATTENDANCE_NOT_FOUND.getMessage());

        System.out.println("✅ 참석 삭제 실패 테스트 통과 (Not Found)");
    }
}

package com.soaengry.moment.domain.guestbook.service;

import com.soaengry.moment.domain.guestbook.dto.request.GuestbookRequest;
import com.soaengry.moment.domain.guestbook.dto.response.GuestbookResponse;
import com.soaengry.moment.domain.guestbook.entity.GuestbookEntry;
import com.soaengry.moment.domain.guestbook.exception.GuestbookErrorCode;
import com.soaengry.moment.domain.guestbook.exception.GuestbookException;
import com.soaengry.moment.domain.guestbook.repository.GuestbookEntryRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Couple;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.CoupleRepository;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class GuestbookServiceTest {

    @Autowired
    private GuestbookService guestbookService;

    @Autowired
    private GuestbookEntryRepository guestbookEntryRepository;

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private CoupleRepository coupleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User hostUser;
    private User adminUser;
    private Wedding testWedding;

    @Autowired
    private com.soaengry.moment.domain.attendance.repository.AttendanceRepository attendanceRepository;

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

        // 일반 사용자 생성
        testUser = User.builder()
                .email("user@example.com")
                .password("password")
                .nickname("테스터")
                .isEmailVerified(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);

        // 호스트 사용자 생성
        hostUser = User.builder()
                .email("host@example.com")
                .password("password")
                .nickname("호스트")
                .role(User.Role.USER)
                .isEmailVerified(true)
                .build();
        hostUser = userRepository.saveAndFlush(hostUser);

        // 관리자 사용자 생성
        adminUser = User.builder()
                .email("admin@example.com")
                .password("password")
                .nickname("관리자")
                .role(User.Role.ADMIN)
                .isEmailVerified(true)
                .build();
        adminUser = userRepository.saveAndFlush(adminUser);

        // Wedding 생성
        testWedding = Wedding.builder()
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
        testWedding = weddingRepository.saveAndFlush(testWedding);

        // Couple (호스트) 생성
        Couple couple = Couple.builder()
                .wedding(testWedding)
                .email(hostUser.getEmail())
                .role(Couple.CoupleRole.GROOM)
                .name("호스트")
                .fatherName("아버지")
                .motherName("어머니")
                .isFatherAlive(true)
                .isMotherAlive(true)
                .build();
        coupleRepository.saveAndFlush(couple);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("방명록 작성 - 로그인 사용자")
    void createEntry_LoggedInUser_Success() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookRequest request = new GuestbookRequest(
                "테스터",
                "축하합니다!",
                null,
                false
        );

        // when
        GuestbookResponse result = guestbookService.createEntry(testWedding.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isEqualTo(testUser.getId());
        assertThat(result.authorName()).isEqualTo("테스터");
        assertThat(result.content()).isEqualTo("축하합니다!");
        assertThat(result.isSecret()).isFalse();

        System.out.println("✅ 방명록 작성 성공 (로그인 사용자)");
    }

    @Test
    @DisplayName("방명록 작성 - 익명 (비밀번호 포함)")
    void createEntry_Anonymous_Success() {
        // given
        SecurityContextHolder.clearContext(); // 비로그인
        GuestbookRequest request = new GuestbookRequest(
                "익명",
                "축하드립니다",
                "1234",
                false
        );

        // when
        GuestbookResponse result = guestbookService.createEntry(testWedding.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.userId()).isNull();
        assertThat(result.authorName()).isEqualTo("익명");

        // DB에서 확인
        GuestbookEntry entry = guestbookEntryRepository.findById(result.id()).orElseThrow();
        assertThat(entry.getPassword()).isNotNull();
        assertThat(passwordEncoder.matches("1234", entry.getPassword())).isTrue();

        System.out.println("✅ 방명록 작성 성공 (익명, 비밀번호 포함)");
    }

    @Test
    @DisplayName("비밀 방명록 작성")
    void createEntry_Secret_Success() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookRequest request = new GuestbookRequest(
                "테스터",
                "비밀 메시지",
                null,
                true
        );

        // when
        GuestbookResponse result = guestbookService.createEntry(testWedding.getId(), request);

        // then
        assertThat(result.isSecret()).isTrue();

        System.out.println("✅ 비밀 방명록 작성 성공");
    }

    @Test
    @DisplayName("방명록 작성 실패 - 웨딩 없음")
    void createEntry_WeddingNotFound_Fail() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookRequest request = new GuestbookRequest(
                "테스터",
                "내용",
                null,
                false
        );

        // when & then
        assertThatThrownBy(() -> guestbookService.createEntry(999L, request))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 웨딩 없음 테스트 통과");
    }

    @Test
    @DisplayName("공개 방명록만 조회")
    void getEntries_PublicOnly_Success() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자1", "공개 메시지", null, false));
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자2", "비밀 메시지", null, true));

        // 다른 사용자로 조회
        setSecurityContext(adminUser.getId(), "ROLE_USER"); // ADMIN 권한 제외

        // when
        Page<GuestbookResponse> result = guestbookService.getEntries(testWedding.getId(), PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(2);
        GuestbookResponse secretEntry = result.getContent().stream()
                .filter(GuestbookResponse::isSecret)
                .findFirst()
                .orElseThrow();
        assertThat(secretEntry.content()).isEqualTo("비밀 메시지입니다");

        System.out.println("✅ 공개 방명록만 조회 (비밀 메시지 마스킹됨)");
    }

    @Test
    @DisplayName("비밀 방명록 조회 - 호스트는 전체 내용 보기")
    void getEntries_Host_CanSeeSecret() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자", "비밀 메시지", null, true));

        // 호스트로 조회
        setSecurityContext(hostUser.getId(), "ROLE_USER");

        // when
        Page<GuestbookResponse> result = guestbookService.getEntries(testWedding.getId(), PageRequest.of(0, 10));

        // then
        GuestbookResponse entry = result.getContent().get(0);
        assertThat(entry.isSecret()).isTrue();
        assertThat(entry.content()).isEqualTo("비밀 메시지"); // 마스킹 안 됨

        System.out.println("✅ 호스트는 비밀 방명록 전체 내용 보기 가능");
    }

    @Test
    @DisplayName("비밀 방명록 조회 - ADMIN은 전체 내용 보기")
    void getEntries_Admin_CanSeeSecret() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자", "비밀 메시지", null, true));

        // 관리자로 조회
        setSecurityContext(adminUser.getId(), "ROLE_ADMIN");

        // when
        Page<GuestbookResponse> result = guestbookService.getEntries(testWedding.getId(), PageRequest.of(0, 10));

        // then
        GuestbookResponse entry = result.getContent().get(0);
        assertThat(entry.isSecret()).isTrue();
        assertThat(entry.content()).isEqualTo("비밀 메시지"); // 마스킹 안 됨

        System.out.println("✅ ADMIN은 비밀 방명록 전체 내용 보기 가능");
    }

    @Test
    @DisplayName("비밀 방명록 조회 - 타인은 마스킹된 내용")
    void getEntries_Other_SecretMasked() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자", "비밀 메시지", null, true));

        // 다른 사용자로 조회
        User otherUser = User.builder()
                .email("other@example.com")
                .password("password")
                .nickname("다른사용자")
                .build();
        otherUser = userRepository.saveAndFlush(otherUser);
        setSecurityContext(otherUser.getId(), "ROLE_USER");

        // when
        Page<GuestbookResponse> result = guestbookService.getEntries(testWedding.getId(), PageRequest.of(0, 10));

        // then
        GuestbookResponse entry = result.getContent().get(0);
        assertThat(entry.isSecret()).isTrue();
        assertThat(entry.content()).isEqualTo("비밀 메시지입니다"); // 마스킹됨

        System.out.println("✅ 타인은 비밀 방명록 마스킹됨");
    }

    @Test
    @DisplayName("방명록 수정 - 작성자 본인")
    void updateEntry_Author_Success() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "원본 내용", null, false));

        GuestbookRequest updateRequest = new GuestbookRequest(
                "테스터",
                "수정된 내용",
                null,
                false
        );

        // when
        GuestbookResponse result = guestbookService.updateEntry(testWedding.getId(), entry.id(), updateRequest);

        // then
        assertThat(result.content()).isEqualTo("수정된 내용");

        System.out.println("✅ 작성자 본인 수정 성공");
    }

    @Test
    @DisplayName("방명록 수정 - 비밀번호 일치")
    void updateEntry_Password_Success() {
        // given
        SecurityContextHolder.clearContext();
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "원본 내용", "1234", false));

        GuestbookRequest updateRequest = new GuestbookRequest(
                "익명",
                "수정된 내용",
                "1234",
                false
        );

        // when
        GuestbookResponse result = guestbookService.updateEntry(testWedding.getId(), entry.id(), updateRequest);

        // then
        assertThat(result.content()).isEqualTo("수정된 내용");

        System.out.println("✅ 비밀번호 일치 수정 성공");
    }

    @Test
    @DisplayName("방명록 수정 실패 - 비밀번호 불일치")
    void updateEntry_WrongPassword_Fail() {
        // given
        SecurityContextHolder.clearContext();
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "원본 내용", "1234", false));

        GuestbookRequest updateRequest = new GuestbookRequest(
                "익명",
                "수정된 내용",
                "5678", // 잘못된 비밀번호
                false
        );

        // when & then
        assertThatThrownBy(() -> guestbookService.updateEntry(testWedding.getId(), entry.id(), updateRequest))
                .isInstanceOf(GuestbookException.class)
                .hasMessage(GuestbookErrorCode.INVALID_PASSWORD.getMessage());

        System.out.println("✅ 비밀번호 불일치 테스트 통과");
    }

    @Test
    @DisplayName("방명록 수정 실패 - 권한 없음")
    void updateEntry_Unauthorized_Fail() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "원본 내용", null, false));

        // 다른 사용자로 수정 시도
        User otherUser = User.builder()
                .email("other@example.com")
                .password("password")
                .nickname("다른사용자")
                .build();
        otherUser = userRepository.saveAndFlush(otherUser);
        setSecurityContext(otherUser.getId(), "ROLE_USER");

        GuestbookRequest updateRequest = new GuestbookRequest(
                "다른사용자",
                "수정 시도",
                null,
                false
        );

        // when & then
        assertThatThrownBy(() -> guestbookService.updateEntry(testWedding.getId(), entry.id(), updateRequest))
                .isInstanceOf(GuestbookException.class)
                .hasMessage(GuestbookErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 권한 없음 테스트 통과");
    }

    @Test
    @DisplayName("방명록 삭제 - 작성자")
    void deleteEntry_Author_Success() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "내용", null, false));

        // when
        guestbookService.deleteEntry(testWedding.getId(), entry.id(), null);

        // then
        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ 작성자 삭제 성공");
    }

    @Test
    @DisplayName("방명록 삭제 - 호스트")
    void deleteEntry_Host_Success() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "내용", null, false));

        // 호스트로 삭제
        setSecurityContext(hostUser.getId(), "ROLE_USER");

        // when
        guestbookService.deleteEntry(testWedding.getId(), entry.id(), null);

        // then
        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ 호스트 삭제 성공");
    }

    @Test
    @DisplayName("방명록 삭제 - ADMIN")
    void deleteEntry_Admin_Success() {
        // given
        setSecurityContext(testUser.getId(), "ROLE_USER");
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "내용", null, false));

        // 관리자로 삭제
        setSecurityContext(adminUser.getId(), "ROLE_ADMIN");

        // when
        guestbookService.deleteEntry(testWedding.getId(), entry.id(), null);

        // then
        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ ADMIN 삭제 성공");
    }

    @Test
    @DisplayName("방명록 삭제 - 비밀번호 일치")
    void deleteEntry_Password_Success() {
        // given
        SecurityContextHolder.clearContext();
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "내용", "1234", false));

        // when
        guestbookService.deleteEntry(testWedding.getId(), entry.id(), "1234");

        // then
        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ 비밀번호 일치 삭제 성공");
    }

    @Test
    @DisplayName("비밀번호 검증 성공")
    void verifyPassword_Success() {
        // given
        SecurityContextHolder.clearContext();
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "내용", "1234", false));

        // when & then (예외 없이 성공)
        guestbookService.verifyPassword(entry.id(), "1234");

        System.out.println("✅ 비밀번호 검증 성공");
    }

    @Test
    @DisplayName("비밀번호 검증 실패")
    void verifyPassword_Fail() {
        // given
        SecurityContextHolder.clearContext();
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "내용", "1234", false));

        // when & then
        assertThatThrownBy(() -> guestbookService.verifyPassword(entry.id(), "5678"))
                .isInstanceOf(GuestbookException.class)
                .hasMessage(GuestbookErrorCode.INVALID_PASSWORD.getMessage());

        System.out.println("✅ 비밀번호 검증 실패 테스트 통과");
    }

    // === Helper Methods ===

    private void setSecurityContext(Long userId, String... roles) {
        List<SimpleGrantedAuthority> authorities = List.of(roles).stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userId, null, authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }
}

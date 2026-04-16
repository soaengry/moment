package com.soaengry.moment.domain.guestbook.service;

import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.guestbook.dto.request.GuestbookRequest;
import com.soaengry.moment.domain.guestbook.dto.response.GuestbookResponse;
import com.soaengry.moment.domain.guestbook.entity.GuestbookEntry;
import com.soaengry.moment.domain.guestbook.exception.GuestbookErrorCode;
import com.soaengry.moment.domain.guestbook.exception.GuestbookException;
import com.soaengry.moment.domain.guestbook.repository.GuestbookEntryRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class GuestbookServiceTest {

    @Autowired
    private GuestbookService guestbookService;

    @Autowired
    private GuestbookEntryRepository guestbookEntryRepository;

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User hostUser;   // 이벤트 소유자 = 방명록 호스트
    private User adminUser;
    private Wedding testWedding;

    @BeforeEach
    void setUp() {
        // 일반 사용자
        testUser = userRepository.save(User.builder()
                .email("guestbook_user_" + System.nanoTime() + "@test.com")
                .nickname("테스터_" + System.nanoTime())
                .isEmailVerified(true)
                .build());

        // 이벤트 소유자 (= 방명록 호스트)
        hostUser = userRepository.save(User.builder()
                .email("guestbook_host_" + System.nanoTime() + "@test.com")
                .nickname("호스트_" + System.nanoTime())
                .isEmailVerified(true)
                .build());

        // 관리자
        adminUser = userRepository.save(User.builder()
                .email("guestbook_admin_" + System.nanoTime() + "@test.com")
                .nickname("관리자_" + System.nanoTime())
                .isEmailVerified(true)
                .build());

        // hostUser가 소유한 Event + Wedding 생성
        Event event = eventRepository.saveAndFlush(Event.builder()
                .user(hostUser)
                .title("김철수 & 이영희 결혼식")
                .type(EventType.WEDDING)
                .date(LocalDateTime.of(2026, 12, 25, 14, 0))
                .slug("guestbook-test-" + System.nanoTime())
                .build());

        testWedding = weddingRepository.saveAndFlush(Wedding.builder()
                .event(event)
                .notice("유의사항")
                .build());
    }

    // ─── 작성 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("방명록 작성 - 로그인 사용자")
    void createEntry_LoggedInUser_Success() {
        GuestbookRequest request = new GuestbookRequest("테스터", "축하합니다!", null, false);

        GuestbookResponse result = guestbookService.createEntry(testWedding.getId(), request, testUser.getId());

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
        GuestbookRequest request = new GuestbookRequest("익명", "축하드립니다", "1234", false);

        GuestbookResponse result = guestbookService.createEntry(testWedding.getId(), request, null);

        assertThat(result.userId()).isNull();
        assertThat(result.authorName()).isEqualTo("익명");

        GuestbookEntry entry = guestbookEntryRepository.findById(result.id()).orElseThrow();
        assertThat(passwordEncoder.matches("1234", entry.getPassword())).isTrue();

        System.out.println("✅ 방명록 작성 성공 (익명, 비밀번호 포함)");
    }

    @Test
    @DisplayName("비밀 방명록 작성")
    void createEntry_Secret_Success() {
        GuestbookRequest request = new GuestbookRequest("테스터", "비밀 메시지", null, true);

        GuestbookResponse result = guestbookService.createEntry(testWedding.getId(), request, testUser.getId());

        assertThat(result.isSecret()).isTrue();

        System.out.println("✅ 비밀 방명록 작성 성공");
    }

    @Test
    @DisplayName("방명록 작성 실패 - 웨딩 없음")
    void createEntry_WeddingNotFound_Fail() {
        GuestbookRequest request = new GuestbookRequest("테스터", "내용", null, false);

        assertThatThrownBy(() -> guestbookService.createEntry(999L, request, testUser.getId()))
                .isInstanceOf(WeddingException.class)
                .hasMessage(WeddingErrorCode.WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 웨딩 없음 테스트 통과");
    }

    // ─── 조회 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("공개 방명록만 조회 - 타인의 비밀 글은 노출되지 않음")
    void getEntries_PublicOnly_Success() {
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자1", "공개 메시지", null, false), testUser.getId());
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자2", "비밀 메시지", null, true), testUser.getId());

        // 다른 사용자(비호스트, 비어드민)로 조회
        Page<GuestbookResponse> result = guestbookService.getEntries(
                testWedding.getId(), adminUser.getId(), false, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).isSecret()).isFalse();
        assertThat(result.getContent().get(0).content()).isEqualTo("공개 메시지");

        System.out.println("✅ 공개 방명록만 조회 (타인의 비밀 글은 노출 안 됨)");
    }

    @Test
    @DisplayName("비밀 방명록 조회 - 호스트는 전체 내용 보기")
    void getEntries_Host_CanSeeSecret() {
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자", "비밀 메시지", null, true), testUser.getId());

        // 이벤트 소유자(호스트)로 조회
        Page<GuestbookResponse> result = guestbookService.getEntries(
                testWedding.getId(), hostUser.getId(), false, PageRequest.of(0, 10));

        GuestbookResponse entry = result.getContent().get(0);
        assertThat(entry.isSecret()).isTrue();
        assertThat(entry.content()).isEqualTo("비밀 메시지");

        System.out.println("✅ 호스트는 비밀 방명록 전체 내용 보기 가능");
    }

    @Test
    @DisplayName("비밀 방명록 조회 - ADMIN은 전체 내용 보기")
    void getEntries_Admin_CanSeeSecret() {
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자", "비밀 메시지", null, true), testUser.getId());

        Page<GuestbookResponse> result = guestbookService.getEntries(
                testWedding.getId(), adminUser.getId(), true, PageRequest.of(0, 10));

        GuestbookResponse entry = result.getContent().get(0);
        assertThat(entry.isSecret()).isTrue();
        assertThat(entry.content()).isEqualTo("비밀 메시지");

        System.out.println("✅ ADMIN은 비밀 방명록 전체 내용 보기 가능");
    }

    @Test
    @DisplayName("비밀 방명록 조회 - 타인에게는 노출되지 않음")
    void getEntries_Other_SecretNotVisible() {
        guestbookService.createEntry(testWedding.getId(), new GuestbookRequest("작성자", "비밀 메시지", null, true), testUser.getId());

        User otherUser = userRepository.save(User.builder()
                .email("guestbook_other_" + System.nanoTime() + "@test.com")
                .nickname("다른사용자_" + System.nanoTime())
                .build());

        Page<GuestbookResponse> result = guestbookService.getEntries(
                testWedding.getId(), otherUser.getId(), false, PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();

        System.out.println("✅ 타인은 비밀 방명록을 볼 수 없음");
    }

    // ─── 수정 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("방명록 수정 - 작성자 본인")
    void updateEntry_Author_Success() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "원본 내용", null, false), testUser.getId());

        GuestbookResponse result = guestbookService.updateEntry(
                testWedding.getId(), entry.id(),
                new GuestbookRequest("테스터", "수정된 내용", null, false),
                testUser.getId(), false);

        assertThat(result.content()).isEqualTo("수정된 내용");

        System.out.println("✅ 작성자 본인 수정 성공");
    }

    @Test
    @DisplayName("방명록 수정 - 비밀번호 일치")
    void updateEntry_Password_Success() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "원본 내용", "1234", false), null);

        GuestbookResponse result = guestbookService.updateEntry(
                testWedding.getId(), entry.id(),
                new GuestbookRequest("익명", "수정된 내용", "1234", false),
                null, false);

        assertThat(result.content()).isEqualTo("수정된 내용");

        System.out.println("✅ 비밀번호 일치 수정 성공");
    }

    @Test
    @DisplayName("방명록 수정 실패 - 비밀번호 불일치")
    void updateEntry_WrongPassword_Fail() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "원본 내용", "1234", false), null);

        assertThatThrownBy(() -> guestbookService.updateEntry(
                testWedding.getId(), entry.id(),
                new GuestbookRequest("익명", "수정된 내용", "5678", false),
                null, false))
                .isInstanceOf(GuestbookException.class)
                .hasMessage(GuestbookErrorCode.INVALID_PASSWORD.getMessage());

        System.out.println("✅ 비밀번호 불일치 테스트 통과");
    }

    @Test
    @DisplayName("방명록 수정 실패 - 권한 없음")
    void updateEntry_Unauthorized_Fail() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "원본 내용", null, false), testUser.getId());

        User otherUser = userRepository.save(User.builder()
                .email("guestbook_unauth_" + System.nanoTime() + "@test.com")
                .nickname("다른사용자_" + System.nanoTime())
                .build());

        assertThatThrownBy(() -> guestbookService.updateEntry(
                testWedding.getId(), entry.id(),
                new GuestbookRequest("다른사용자", "수정 시도", null, false),
                otherUser.getId(), false))
                .isInstanceOf(GuestbookException.class)
                .hasMessage(GuestbookErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 권한 없음 테스트 통과");
    }

    // ─── 삭제 ─────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("방명록 삭제 - 작성자")
    void deleteEntry_Author_Success() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "내용", null, false), testUser.getId());

        guestbookService.deleteEntry(testWedding.getId(), entry.id(), null, testUser.getId(), false);

        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ 작성자 삭제 성공");
    }

    @Test
    @DisplayName("방명록 삭제 - 호스트 (이벤트 소유자)")
    void deleteEntry_Host_Success() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "내용", null, false), testUser.getId());

        guestbookService.deleteEntry(testWedding.getId(), entry.id(), null, hostUser.getId(), false);

        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ 호스트 삭제 성공");
    }

    @Test
    @DisplayName("방명록 삭제 - ADMIN")
    void deleteEntry_Admin_Success() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("테스터", "내용", null, false), testUser.getId());

        guestbookService.deleteEntry(testWedding.getId(), entry.id(), null, adminUser.getId(), true);

        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ ADMIN 삭제 성공");
    }

    @Test
    @DisplayName("방명록 삭제 - 비밀번호 일치")
    void deleteEntry_Password_Success() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "내용", "1234", false), null);

        guestbookService.deleteEntry(testWedding.getId(), entry.id(), "1234", null, false);

        assertThat(guestbookEntryRepository.existsById(entry.id())).isFalse();

        System.out.println("✅ 비밀번호 일치 삭제 성공");
    }

    // ─── 비밀번호 검증 ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("비밀번호 검증 성공")
    void verifyPassword_Success() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "내용", "1234", false), null);

        guestbookService.verifyPassword(entry.id(), "1234");

        System.out.println("✅ 비밀번호 검증 성공");
    }

    @Test
    @DisplayName("비밀번호 검증 실패")
    void verifyPassword_Fail() {
        GuestbookResponse entry = guestbookService.createEntry(testWedding.getId(),
                new GuestbookRequest("익명", "내용", "1234", false), null);

        assertThatThrownBy(() -> guestbookService.verifyPassword(entry.id(), "5678"))
                .isInstanceOf(GuestbookException.class)
                .hasMessage(GuestbookErrorCode.INVALID_PASSWORD.getMessage());

        System.out.println("✅ 비밀번호 검증 실패 테스트 통과");
    }
}

package com.soaengry.moment.domain.chat.service;

import com.soaengry.moment.domain.chat.dto.request.ChatMessageRequest;
import com.soaengry.moment.domain.chat.dto.response.ChatMessageResponse;
import com.soaengry.moment.domain.chat.entity.ChatMessage;
import com.soaengry.moment.domain.chat.exception.ChatErrorCode;
import com.soaengry.moment.domain.chat.exception.ChatException;
import com.soaengry.moment.domain.chat.repository.ChatMessageRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import com.soaengry.moment.global.service.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private WeddingRepository weddingRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private S3Service s3Service;

    private User testUser;
    private Wedding testWedding;

    @Autowired
    private com.soaengry.moment.domain.guestbook.repository.GuestbookEntryRepository guestbookEntryRepository;

    @Autowired
    private com.soaengry.moment.domain.attendance.repository.AttendanceRepository attendanceRepository;

    @Autowired
    private com.soaengry.moment.domain.wedding.repository.CoupleRepository coupleRepository;

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
        chatMessageRepository.deleteAll();
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
        testUser = User.builder()
                .email("test@example.com")
                .password("encodedPassword")
                .nickname("테스터")
                .profileImageUrl("https://s3.amazonaws.com/profile.jpg")
                .isEmailVerified(true)
                .build();
        testUser = userRepository.saveAndFlush(testUser);

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
    }

    @Test
    @DisplayName("메시지 목록 조회 (페이징)")
    void getMessages_Pageable_Success() {
        // given
        ChatMessage message1 = ChatMessage.create(
                testWedding.getId(), testUser.getId(), testUser.getNickname(),
                testUser.getProfileImageUrl(), "첫 번째 메시지", null, ChatMessage.MessageType.CHAT
        );
        ChatMessage message2 = ChatMessage.create(
                testWedding.getId(), testUser.getId(), testUser.getNickname(),
                testUser.getProfileImageUrl(), "두 번째 메시지", null, ChatMessage.MessageType.CHAT
        );
        chatMessageRepository.save(message1);
        chatMessageRepository.save(message2);

        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ChatMessageResponse> result = chatService.getMessages(testWedding.getId(), pageRequest);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(ChatMessageResponse::content)
                .contains("첫 번째 메시지", "두 번째 메시지");

        System.out.println("✅ 메시지 목록 조회 성공");
        System.out.println("   - 메시지 수: " + result.getContent().size());
    }

    @Test
    @DisplayName("빈 메시지 목록")
    void getMessages_Empty_Success() {
        // given
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when
        Page<ChatMessageResponse> result = chatService.getMessages(testWedding.getId(), pageRequest);

        // then
        assertThat(result.getContent()).isEmpty();

        System.out.println("✅ 빈 메시지 목록 조회 성공");
    }

    @Test
    @DisplayName("웨딩 없음 - 메시지 조회 실패")
    void getMessages_WeddingNotFound_Fail() {
        // given
        Long invalidWeddingId = 999L;
        PageRequest pageRequest = PageRequest.of(0, 10);

        // when & then
        assertThatThrownBy(() -> chatService.getMessages(invalidWeddingId, pageRequest))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 웨딩 없음 - 메시지 조회 실패 테스트 통과");
    }

    @Test
    @DisplayName("텍스트 메시지 저장")
    void saveMessage_TextMessage_Success() {
        // given
        ChatMessageRequest request = new ChatMessageRequest(
                testWedding.getId(),
                "안녕하세요!",
                null,
                "CHAT"
        );

        // when
        ChatMessageResponse result = chatService.saveMessage(testUser.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.weddingId()).isEqualTo(testWedding.getId());
        assertThat(result.userId()).isEqualTo(testUser.getId());
        assertThat(result.nickname()).isEqualTo("테스터");
        assertThat(result.content()).isEqualTo("안녕하세요!");
        assertThat(result.imageUrl()).isNull();
        assertThat(result.type()).isEqualTo("CHAT");

        System.out.println("✅ 텍스트 메시지 저장 성공");
        System.out.println("   - 내용: " + result.content());
    }

    @Test
    @DisplayName("이미지 메시지 저장 (imageUrl 포함)")
    void saveMessage_ImageMessage_Success() {
        // given
        ChatMessageRequest request = new ChatMessageRequest(
                testWedding.getId(),
                "사진 공유합니다",
                "https://s3.amazonaws.com/chat/image.jpg",
                "IMAGE"
        );

        // when
        ChatMessageResponse result = chatService.saveMessage(testUser.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.imageUrl()).isEqualTo("https://s3.amazonaws.com/chat/image.jpg");
        assertThat(result.type()).isEqualTo("IMAGE");

        System.out.println("✅ 이미지 메시지 저장 성공");
        System.out.println("   - 이미지 URL: " + result.imageUrl());
    }

    @Test
    @DisplayName("시스템 메시지 저장")
    void saveMessage_SystemMessage_Success() {
        // given
        ChatMessageRequest request = new ChatMessageRequest(
                testWedding.getId(),
                "테스터님이 입장하셨습니다.",
                null,
                null // type null이면 CHAT으로 기본 설정
        );

        // when
        ChatMessageResponse result = chatService.saveMessage(testUser.getId(), request);

        // then
        assertThat(result).isNotNull();
        assertThat(result.type()).isEqualTo("CHAT");

        System.out.println("✅ 시스템 메시지 저장 성공");
    }

    @Test
    @DisplayName("사용자 없음 - 메시지 저장 실패")
    void saveMessage_UserNotFound_Fail() {
        // given
        Long invalidUserId = 999L;
        ChatMessageRequest request = new ChatMessageRequest(
                testWedding.getId(),
                "메시지",
                null,
                "CHAT"
        );

        // when & then
        assertThatThrownBy(() -> chatService.saveMessage(invalidUserId, request))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 사용자 없음 - 메시지 저장 실패 테스트 통과");
    }

    @Test
    @DisplayName("웨딩 없음 - 메시지 저장 실패")
    void saveMessage_WeddingNotFound_Fail() {
        // given
        ChatMessageRequest request = new ChatMessageRequest(
                999L, // 존재하지 않는 weddingId
                "메시지",
                null,
                "CHAT"
        );

        // when & then
        assertThatThrownBy(() -> chatService.saveMessage(testUser.getId(), request))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 웨딩 없음 - 메시지 저장 실패 테스트 통과");
    }

    @Test
    @DisplayName("채팅 이미지 업로드 성공 (S3)")
    void uploadChatImage_Success() {
        // given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        when(s3Service.uploadChatImage(any())).thenReturn("https://s3.amazonaws.com/chat/test.jpg");

        // when
        String result = chatService.uploadChatImage(testWedding.getId(), testUser.getId(), file);

        // then
        assertThat(result).isEqualTo("https://s3.amazonaws.com/chat/test.jpg");

        System.out.println("✅ 채팅 이미지 업로드 성공");
        System.out.println("   - URL: " + result);
    }

    @Test
    @DisplayName("채팅 이미지 업로드 실패 - 웨딩 없음")
    void uploadChatImage_WeddingNotFound_Fail() {
        // given
        Long invalidWeddingId = 999L;
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // when & then
        assertThatThrownBy(() -> chatService.uploadChatImage(invalidWeddingId, testUser.getId(), file))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 웨딩 없음 - 이미지 업로드 실패 테스트 통과");
    }
}

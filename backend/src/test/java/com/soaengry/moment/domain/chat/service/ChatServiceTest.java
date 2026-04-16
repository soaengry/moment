package com.soaengry.moment.domain.chat.service;

import com.soaengry.moment.domain.attendance.entity.Attendance;
import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.chat.dto.request.ChatMessageRequest;
import com.soaengry.moment.domain.chat.dto.response.ChatMessageResponse;
import com.soaengry.moment.domain.chat.entity.ChatMessage;
import com.soaengry.moment.domain.chat.exception.ChatErrorCode;
import com.soaengry.moment.domain.chat.exception.ChatException;
import com.soaengry.moment.domain.chat.repository.ChatMessageRepository;
import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.event.entity.EventType;
import com.soaengry.moment.domain.event.repository.EventRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AttendanceRepository attendanceRepository;

    @MockitoBean
    private S3Service s3Service;

    private User testUser;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        testUser = userRepository.save(User.builder()
                .email("chat_test_" + System.nanoTime() + "@test.com")
                .nickname("테스터_" + System.nanoTime())
                .profileImageUrl("https://s3.amazonaws.com/profile.jpg")
                .isEmailVerified(true)
                .build());

        testEvent = eventRepository.saveAndFlush(Event.builder()
                .user(testUser)
                .title("김철수 & 이영희 결혼식")
                .type(EventType.WEDDING)
                .date(LocalDateTime.of(2026, 12, 25, 14, 0))
                .slug("chat-test-" + System.nanoTime())
                .build());

        // testUser 참석 등록 (saveMessage 권한 검증에 필요)
        attendanceRepository.saveAndFlush(Attendance.create(testUser.getId(), testEvent.getId()));
    }

    @Test
    @DisplayName("메시지 목록 조회 (페이징)")
    void getMessages_Pageable_Success() {
        chatMessageRepository.save(ChatMessage.create(
                testEvent.getId(), testUser.getId(), testUser.getNickname(),
                testUser.getProfileImageUrl(), "첫 번째 메시지", null, ChatMessage.MessageType.CHAT));
        chatMessageRepository.save(ChatMessage.create(
                testEvent.getId(), testUser.getId(), testUser.getNickname(),
                testUser.getProfileImageUrl(), "두 번째 메시지", null, ChatMessage.MessageType.CHAT));

        Page<ChatMessageResponse> result = chatService.getMessages(testEvent.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(ChatMessageResponse::content)
                .contains("첫 번째 메시지", "두 번째 메시지");

        System.out.println("✅ 메시지 목록 조회 성공");
    }

    @Test
    @DisplayName("빈 메시지 목록")
    void getMessages_Empty_Success() {
        Page<ChatMessageResponse> result = chatService.getMessages(testEvent.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();

        System.out.println("✅ 빈 메시지 목록 조회 성공");
    }

    @Test
    @DisplayName("이벤트 없음 - 메시지 조회 실패")
    void getMessages_EventNotFound_Fail() {
        assertThatThrownBy(() -> chatService.getMessages(999L, PageRequest.of(0, 10)))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 이벤트 없음 - 메시지 조회 실패 테스트 통과");
    }

    @Test
    @DisplayName("텍스트 메시지 저장")
    void saveMessage_TextMessage_Success() {
        ChatMessageRequest request = new ChatMessageRequest(
                testEvent.getId(), "안녕하세요!", null, "CHAT");

        ChatMessageResponse result = chatService.saveMessage(testUser.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.eventId()).isEqualTo(testEvent.getId());
        assertThat(result.userId()).isEqualTo(testUser.getId());
        assertThat(result.nickname()).isEqualTo(testUser.getNickname());
        assertThat(result.content()).isEqualTo("안녕하세요!");
        assertThat(result.imageUrl()).isNull();
        assertThat(result.type()).isEqualTo("CHAT");

        System.out.println("✅ 텍스트 메시지 저장 성공");
    }

    @Test
    @DisplayName("이미지 메시지 저장 (imageUrl 포함)")
    void saveMessage_ImageMessage_Success() {
        ChatMessageRequest request = new ChatMessageRequest(
                testEvent.getId(), "사진 공유합니다",
                "https://s3.amazonaws.com/chat/image.jpg", "IMAGE");

        ChatMessageResponse result = chatService.saveMessage(testUser.getId(), request);

        assertThat(result.imageUrl()).isEqualTo("https://s3.amazonaws.com/chat/image.jpg");
        assertThat(result.type()).isEqualTo("IMAGE");

        System.out.println("✅ 이미지 메시지 저장 성공");
    }

    @Test
    @DisplayName("시스템 메시지 저장 (type=null → CHAT)")
    void saveMessage_SystemMessage_Success() {
        ChatMessageRequest request = new ChatMessageRequest(
                testEvent.getId(), "테스터님이 입장하셨습니다.", null, null);

        ChatMessageResponse result = chatService.saveMessage(testUser.getId(), request);

        assertThat(result.type()).isEqualTo("CHAT");

        System.out.println("✅ 시스템 메시지 저장 성공");
    }

    @Test
    @DisplayName("사용자 없음 - 메시지 저장 실패")
    void saveMessage_UserNotFound_Fail() {
        ChatMessageRequest request = new ChatMessageRequest(
                testEvent.getId(), "메시지", null, "CHAT");

        assertThatThrownBy(() -> chatService.saveMessage(999L, request))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.UNAUTHORIZED_ACCESS.getMessage());

        System.out.println("✅ 사용자 없음 - 메시지 저장 실패 테스트 통과");
    }

    @Test
    @DisplayName("이벤트 없음 - 메시지 저장 실패")
    void saveMessage_EventNotFound_Fail() {
        ChatMessageRequest request = new ChatMessageRequest(
                999L, "메시지", null, "CHAT");

        assertThatThrownBy(() -> chatService.saveMessage(testUser.getId(), request))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 이벤트 없음 - 메시지 저장 실패 테스트 통과");
    }

    @Test
    @DisplayName("채팅 이미지 업로드 성공 (S3)")
    void uploadChatImage_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());

        when(s3Service.uploadChatImage(any())).thenReturn("https://s3.amazonaws.com/chat/test.jpg");

        String result = chatService.uploadChatImage(testEvent.getId(), testUser.getId(), file);

        assertThat(result).isEqualTo("https://s3.amazonaws.com/chat/test.jpg");

        System.out.println("✅ 채팅 이미지 업로드 성공");
    }

    @Test
    @DisplayName("채팅 이미지 업로드 실패 - 이벤트 없음")
    void uploadChatImage_EventNotFound_Fail() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.jpg", "image/jpeg", "test image content".getBytes());

        assertThatThrownBy(() -> chatService.uploadChatImage(999L, testUser.getId(), file))
                .isInstanceOf(ChatException.class)
                .hasMessage(ChatErrorCode.CHAT_WEDDING_NOT_FOUND.getMessage());

        System.out.println("✅ 이벤트 없음 - 이미지 업로드 실패 테스트 통과");
    }
}

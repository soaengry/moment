package com.soaengry.moment.domain.chat.service;

import com.soaengry.moment.domain.attendance.repository.AttendanceRepository;
import com.soaengry.moment.domain.chat.dto.request.ChatMessageRequest;
import com.soaengry.moment.domain.chat.dto.response.ChatMessageResponse;
import com.soaengry.moment.domain.chat.entity.ChatMessage;
import com.soaengry.moment.domain.chat.exception.ChatErrorCode;
import com.soaengry.moment.domain.chat.exception.ChatException;
import com.soaengry.moment.domain.chat.repository.ChatMessageRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.invitation.repository.InvitationRepository;
import com.soaengry.moment.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final InvitationRepository invitationRepository;
    private final UserRepository userRepository;
    private final AttendanceRepository attendanceRepository;
    private final S3Service s3Service;

    public Page<ChatMessageResponse> getMessages(Long weddingId, Pageable pageable) {
        if (!invitationRepository.existsById(weddingId)) {
            throw new ChatException(ChatErrorCode.CHAT_WEDDING_NOT_FOUND);
        }
        return chatMessageRepository.findByWeddingIdOrderByCreatedAtDesc(weddingId, pageable)
                .map(ChatMessageResponse::from);
    }

    public ChatMessageResponse saveMessage(Long userId, ChatMessageRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.UNAUTHORIZED_ACCESS));

        if (!invitationRepository.existsById(request.weddingId())) {
            throw new ChatException(ChatErrorCode.CHAT_WEDDING_NOT_FOUND);
        }

        if (!attendanceRepository.existsByUserIdAndWeddingId(userId, request.weddingId())) {
            throw new ChatException(ChatErrorCode.UNAUTHORIZED_ACCESS);
        }

        ChatMessage.MessageType type = request.type() != null
                ? ChatMessage.MessageType.valueOf(request.type())
                : ChatMessage.MessageType.CHAT;

        ChatMessage message = ChatMessage.create(
                request.weddingId(),
                user.getId(),
                user.getNickname(),
                user.getProfileImageUrl(),
                request.content(),
                request.imageUrl(),
                type
        );

        return ChatMessageResponse.from(chatMessageRepository.save(message));
    }

    public String uploadChatImage(Long weddingId, Long userId, MultipartFile file) {
        if (!invitationRepository.existsById(weddingId)) {
            throw new ChatException(ChatErrorCode.CHAT_WEDDING_NOT_FOUND);
        }
        if (!userRepository.existsById(userId)) {
            throw new ChatException(ChatErrorCode.UNAUTHORIZED_ACCESS);
        }
        return s3Service.uploadChatImage(file);
    }
}

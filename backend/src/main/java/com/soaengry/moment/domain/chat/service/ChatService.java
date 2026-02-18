package com.soaengry.moment.domain.chat.service;

import com.soaengry.moment.domain.chat.dto.request.ChatMessageRequest;
import com.soaengry.moment.domain.chat.dto.request.ChatRoomRequest;
import com.soaengry.moment.domain.chat.dto.response.ChatMessageResponse;
import com.soaengry.moment.domain.chat.dto.response.ChatRoomResponse;
import com.soaengry.moment.domain.chat.entity.ChatMessage;
import com.soaengry.moment.domain.chat.entity.ChatRoom;
import com.soaengry.moment.domain.chat.exception.ChatErrorCode;
import com.soaengry.moment.domain.chat.exception.ChatException;
import com.soaengry.moment.domain.chat.repository.ChatMessageRepository;
import com.soaengry.moment.domain.chat.repository.ChatRoomRepository;
import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.user.repository.UserRepository;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.domain.wedding.exception.WeddingErrorCode;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import com.soaengry.moment.domain.wedding.repository.WeddingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final WeddingRepository weddingRepository;
    private final UserRepository userRepository;

    @Transactional
    public ChatRoomResponse createRoom(Long weddingId, ChatRoomRequest request) {
        Wedding wedding = weddingRepository.findById(weddingId)
                .orElseThrow(() -> new WeddingException(WeddingErrorCode.WEDDING_NOT_FOUND));

        ChatRoom room = ChatRoom.create(wedding, request.name());
        return ChatRoomResponse.from(chatRoomRepository.save(room));
    }

    public List<ChatRoomResponse> getRooms(Long weddingId) {
        return chatRoomRepository.findByWeddingIdOrderByCreatedAtAsc(weddingId)
                .stream().map(ChatRoomResponse::from).toList();
    }

    public Page<ChatMessageResponse> getMessages(Long roomId, Pageable pageable) {
        return chatMessageRepository.findByRoomIdWithUser(roomId, pageable)
                .map(ChatMessageResponse::from);
    }

    @Transactional
    public ChatMessageResponse saveMessage(Long userId, ChatMessageRequest request) {
        ChatRoom room = chatRoomRepository.findById(request.roomId())
                .orElseThrow(() -> new ChatException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ChatException(ChatErrorCode.UNAUTHORIZED_ACCESS));

        ChatMessage.MessageType type = request.type() != null
                ? ChatMessage.MessageType.valueOf(request.type())
                : ChatMessage.MessageType.CHAT;

        ChatMessage message = ChatMessage.create(room, user, request.content(), type);
        return ChatMessageResponse.from(chatMessageRepository.save(message));
    }
}

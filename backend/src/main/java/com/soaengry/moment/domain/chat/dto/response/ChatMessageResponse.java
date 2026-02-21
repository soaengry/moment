package com.soaengry.moment.domain.chat.dto.response;

import com.soaengry.moment.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        Long id,
        Long roomId,
        Long userId,
        String nickname,
        String profileImageUrl,
        String content,
        String type,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getChatRoom().getId(),
                message.getUser().getId(),
                message.getUser().getNickname(),
                message.getUser().getProfileImageUrl(),
                message.getContent(),
                message.getType().name(),
                message.getCreatedAt()
        );
    }
}

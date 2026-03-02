package com.soaengry.moment.domain.chat.dto.response;

import com.soaengry.moment.domain.chat.entity.ChatMessage;

import java.time.LocalDateTime;

public record ChatMessageResponse(
        String id,
        Long weddingId,
        Long userId,
        String nickname,
        String profileImageUrl,
        String content,
        String imageUrl,
        String type,
        LocalDateTime createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return new ChatMessageResponse(
                message.getId(),
                message.getWeddingId(),
                message.getUserId(),
                message.getNickname(),
                message.getProfileImageUrl(),
                message.getContent(),
                message.getImageUrl(),
                message.getType().name(),
                message.getCreatedAt()
        );
    }
}

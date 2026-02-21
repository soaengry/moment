package com.soaengry.moment.domain.chat.dto.response;

import com.soaengry.moment.domain.chat.entity.ChatRoom;

import java.time.LocalDateTime;

public record ChatRoomResponse(
        Long id,
        Long weddingId,
        String name,
        LocalDateTime createdAt
) {
    public static ChatRoomResponse from(ChatRoom room) {
        return new ChatRoomResponse(
                room.getId(),
                room.getWedding().getId(),
                room.getName(),
                room.getCreatedAt()
        );
    }
}

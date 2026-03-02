package com.soaengry.moment.domain.chat.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "chat_messages")
@CompoundIndex(def = "{'weddingId': 1, 'createdAt': -1}")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {

    @Id
    private String id;

    private Long weddingId;

    private Long userId;

    private String nickname;

    private String profileImageUrl;

    private String content;

    private String imageUrl;

    private MessageType type;

    private LocalDateTime createdAt;

    private ChatMessage(Long weddingId, Long userId, String nickname, String profileImageUrl,
                        String content, String imageUrl, MessageType type) {
        this.weddingId = weddingId;
        this.userId = userId;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.content = content;
        this.imageUrl = imageUrl;
        this.type = type != null ? type : MessageType.CHAT;
        this.createdAt = LocalDateTime.now();
    }

    public static ChatMessage create(Long weddingId, Long userId, String nickname, String profileImageUrl,
                                     String content, String imageUrl, MessageType type) {
        return new ChatMessage(weddingId, userId, nickname, profileImageUrl, content, imageUrl, type);
    }

    public enum MessageType {
        CHAT, IMAGE
    }
}

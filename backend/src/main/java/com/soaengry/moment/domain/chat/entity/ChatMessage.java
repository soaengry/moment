package com.soaengry.moment.domain.chat.entity;

import com.soaengry.moment.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_messages")
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MessageType type = MessageType.CHAT;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ChatMessage(ChatRoom chatRoom, User user, String content, MessageType type) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.content = content;
        this.type = type != null ? type : MessageType.CHAT;
    }

    public static ChatMessage create(ChatRoom chatRoom, User user, String content, MessageType type) {
        return new ChatMessage(chatRoom, user, content, type);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum MessageType {
        CHAT, JOIN, LEAVE
    }
}

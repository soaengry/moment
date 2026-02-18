package com.soaengry.moment.domain.chat.entity;

import com.soaengry.moment.domain.wedding.entity.Wedding;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_rooms")
public class ChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false)
    private Wedding wedding;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ChatRoom(Wedding wedding, String name) {
        this.wedding = wedding;
        this.name = name;
    }

    public static ChatRoom create(Wedding wedding, String name) {
        return new ChatRoom(wedding, name);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

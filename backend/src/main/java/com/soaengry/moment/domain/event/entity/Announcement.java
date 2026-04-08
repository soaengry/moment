package com.soaengry.moment.domain.event.entity;

import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "announcements")
public class Announcement extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private Boolean isPinned;

    private Announcement(Long eventId, String title, String content, Boolean isPinned) {
        this.eventId = eventId;
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
    }

    public static Announcement create(Long eventId, String title, String content, Boolean isPinned) {
        return new Announcement(eventId, title, content, isPinned);
    }

    public void update(String title, String content, Boolean isPinned) {
        this.title = title;
        this.content = content;
        this.isPinned = isPinned;
    }
}

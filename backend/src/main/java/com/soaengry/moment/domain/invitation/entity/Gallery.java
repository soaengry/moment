package com.soaengry.moment.domain.invitation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "galleries")
public class Gallery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invitationId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String thumbnailUrl;

    private String caption;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Gallery(Long invitationId, String imageUrl, String thumbnailUrl, String caption, Integer orderIndex) {
        this.invitationId = invitationId;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.caption = caption;
        this.orderIndex = orderIndex;
    }

    public static Gallery create(Long invitationId, String imageUrl, String thumbnailUrl, String caption, Integer orderIndex) {
        return new Gallery(invitationId, imageUrl, thumbnailUrl, caption, orderIndex);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public void update(String caption, Integer orderIndex) {
        this.caption = caption;
        this.orderIndex = orderIndex;
    }
}

package com.soaengry.moment.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hero_images")
public class HeroImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_hero_images_events_event_id"))
    private Event event;

    @Column(nullable = false, length = 255)
    private String imageUrl;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    private HeroImage(Event event, String imageUrl, String thumbnailUrl, Integer orderIndex) {
        this.event = event;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.orderIndex = orderIndex;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}

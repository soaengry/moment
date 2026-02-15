package com.soaengry.moment.wedding.entity;

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
    private Long weddingId;

    @Column(nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String thumbnailUrl;

    private String caption;

    @Column(nullable = false)
    private Integer orderIndex;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private Gallery(Long weddingId, String imageUrl, String thumbnailUrl, String caption, Integer orderIndex) {
        this.weddingId = weddingId;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
        this.caption = caption;
        this.orderIndex = orderIndex;
    }

    public static Gallery create(Long weddingId, String imageUrl, String thumbnailUrl, String caption, Integer orderIndex) {
        return new Gallery(weddingId, imageUrl, thumbnailUrl, caption, orderIndex);
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
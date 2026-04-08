package com.soaengry.moment.domain.event.entity;

import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "events", indexes = {
        @Index(name = "idx_event_slug", columnList = "slug"),
        @Index(name = "idx_event_user_id", columnList = "userId")
})
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(nullable = false, length = 50)
    private String locationName;

    @Column(nullable = false, length = 255)
    private String locationAddress;

    @Column(length = 255)
    private String locationDetail;

    private Double locationLat;
    private Double locationLng;

    @Column(nullable = false, unique = true, length = 100)
    private String slug;

    private LocalDateTime deletedAt;

    @Builder
    private Event(Long userId, String title, EventType type, LocalDateTime date,
                  String locationName, String locationAddress, String locationDetail,
                  Double locationLat, Double locationLng, String slug) {
        this.userId = userId;
        this.title = title;
        this.type = type;
        this.date = date;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationDetail = locationDetail;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.slug = slug;
    }

    public void updateTitle(String title) { this.title = title; }

    public void updateDate(LocalDateTime date) { this.date = date; }

    public void updateLocation(String locationName, String locationAddress, String locationDetail,
                               Double locationLat, Double locationLng) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationDetail = locationDetail;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
    }

    public void softDelete() { this.deletedAt = LocalDateTime.now(); }

    public boolean isDeleted() { return this.deletedAt != null; }
}

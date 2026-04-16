package com.soaengry.moment.domain.event.entity;

import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "events",
        indexes = {
                @Index(name = "idx_event_user_id", columnList = "userId")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_events_slug", columnNames = "slug")
        }
)
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_events_users_user_id"))
    private User user;

    @Column(nullable = false, length = 50)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    @Column(nullable = false)
    private LocalDateTime date;

    @Column(length = 50)
    private String locationName;

    private String locationAddress;
    private String locationDetail;
    private Double locationLat;
    private Double locationLng;

    @Column(nullable = false, unique = true, length = 50)
    private String slug;

    private LocalDateTime deletedAt;

    @Column(nullable = false)
    private boolean isPublic = false;

    @OneToMany(mappedBy = "event", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, orphanRemoval = true)
    private List<HeroImage> heroImages = new ArrayList<>();

    @Builder
    private Event(User user, String title, EventType type, LocalDateTime date,
                  String locationName, String locationAddress, String locationDetail,
                  Double locationLat, Double locationLng, String slug, boolean isPublic) {
        this.user = user;
        this.title = title;
        this.type = type;
        this.date = date;
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationDetail = locationDetail;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
        this.slug = slug;
        this.isPublic = isPublic;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateDate(LocalDateTime date) {
        this.date = date;
    }

    public void updateLocation(String locationName, String locationAddress, String locationDetail,
                               Double locationLat, Double locationLng) {
        this.locationName = locationName;
        this.locationAddress = locationAddress;
        this.locationDetail = locationDetail;
        this.locationLat = locationLat;
        this.locationLng = locationLng;
    }

    public void updateSlug(String slug) {
        this.slug = slug;
    }

    public void updateIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

}

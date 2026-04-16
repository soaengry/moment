package com.soaengry.moment.domain.wedding.entity;

import com.soaengry.moment.domain.event.entity.Event;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weddings")
public class Wedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true,
            foreignKey = @ForeignKey(name = "fk_weddings_events_event_id"))
    private Event event;

    @Column(columnDefinition = "TEXT")
    private String notice;              // 유의사항

    @Column(columnDefinition = "TEXT")
    private String parkingInfo;         // 주차정보

    @Column(columnDefinition = "TEXT")
    private String mealInfo;            // 식사정보

    @Column(columnDefinition = "TEXT")
    private String greeting;            // 인사말

    @Version
    private Integer version = 0;

    @Builder
    private Wedding(Event event, String notice, String parkingInfo, String mealInfo, String greeting) {
        this.event = event;
        this.notice = notice;
        this.parkingInfo = parkingInfo;
        this.mealInfo = mealInfo;
        this.greeting = greeting;
    }

    public void update(String notice, String parkingInfo, String mealInfo, String greeting) {
        this.notice = notice;
        this.parkingInfo = parkingInfo;
        this.mealInfo = mealInfo;
        this.greeting = greeting;
    }
}

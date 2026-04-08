package com.soaengry.moment.domain.wedding.entity;

import com.soaengry.moment.domain.event.entity.Event;
import jakarta.persistence.*;
import lombok.*;

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
    private String notice;

    @Column(columnDefinition = "TEXT")
    private String parkingInfo;

    @Column(columnDefinition = "TEXT")
    private String mealInfo;

    @Column(columnDefinition = "TEXT")
    private String greeting;

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

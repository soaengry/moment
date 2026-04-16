package com.soaengry.moment.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    private Schedule(Long eventId, String title, String description, Integer orderIndex) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public static Schedule create(Long eventId, String title, String description, Integer orderIndex) {
        return new Schedule(eventId, title, description, orderIndex);
    }

    public void update(String title, String description, Integer orderIndex) {
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}

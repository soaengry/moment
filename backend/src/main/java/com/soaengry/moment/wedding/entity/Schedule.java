package com.soaengry.moment.wedding.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long weddingId;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    private Schedule(Long weddingId, LocalTime time, String title, String description, Integer orderIndex) {
        this.weddingId = weddingId;
        this.time = time;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public static Schedule create(Long weddingId, LocalTime time, String title, String description, Integer orderIndex) {
        return new Schedule(weddingId, time, title, description, orderIndex);
    }

    public void update(LocalTime time, String title, String description, Integer orderIndex) {
        this.time = time;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}

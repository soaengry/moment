package com.soaengry.moment.domain.invitation.entity;

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
    private Long invitationId;

    @Column(nullable = false)
    private LocalTime time;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    private Schedule(Long invitationId, LocalTime time, String title, String description, Integer orderIndex) {
        this.invitationId = invitationId;
        this.time = time;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public static Schedule create(Long invitationId, LocalTime time, String title, String description, Integer orderIndex) {
        return new Schedule(invitationId, time, title, description, orderIndex);
    }

    public void update(LocalTime time, String title, String description, Integer orderIndex) {
        this.time = time;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }
}

package com.soaengry.moment.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transportation")
public class Transportation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    private Transportation(Long eventId, TransportType type, String title, String description, Integer orderIndex) {
        this.eventId = eventId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public static Transportation create(Long eventId, TransportType type, String title, String description, Integer orderIndex) {
        return new Transportation(eventId, type, title, description, orderIndex);
    }

    public void update(TransportType type, String title, String description, Integer orderIndex) {
        this.type = type;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public enum TransportType {
        SUBWAY, BUS, SHUTTLE
    }
}

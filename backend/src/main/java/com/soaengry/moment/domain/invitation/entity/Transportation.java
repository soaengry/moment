package com.soaengry.moment.domain.invitation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "transportation")
public class Transportation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invitationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransportType type;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer orderIndex;

    private Transportation(Long invitationId, TransportType type, String title, String description, Integer orderIndex) {
        this.invitationId = invitationId;
        this.type = type;
        this.title = title;
        this.description = description;
        this.orderIndex = orderIndex;
    }

    public static Transportation create(Long invitationId, TransportType type, String title, String description, Integer orderIndex) {
        return new Transportation(invitationId, type, title, description, orderIndex);
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

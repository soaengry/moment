package com.soaengry.moment.domain.attendance.entity;

import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "attendances", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"userId", "eventId"})
})
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long eventId;

    private Attendance(Long userId, Long eventId) {
        this.userId = userId;
        this.eventId = eventId;
    }

    public static Attendance create(Long userId, Long eventId) {
        return new Attendance(userId, eventId);
    }
}

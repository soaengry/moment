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
        @UniqueConstraint(columnNames = {"userId", "weddingId"})
})
public class Attendance extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long weddingId;

    private Attendance(Long userId, Long weddingId) {
        this.userId = userId;
        this.weddingId = weddingId;
    }

    public static Attendance create(Long userId, Long weddingId) {
        return new Attendance(userId, weddingId);
    }
}

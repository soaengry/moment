package com.soaengry.moment.domain.rsvp.entity;

import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "rsvps",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_rsvps_session_wedding",
        columnNames = {"session_id", "wedding_id"}
    )
)
public class Rsvp extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_rsvps_weddings_wedding_id"))
    private Wedding wedding;

    @Column(nullable = false, length = 100)
    private String sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_rsvps_users_user_id"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RsvpAttendance attendance;

    @Column(nullable = false, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RsvpSide side;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(nullable = false)
    private Integer attendeeCount = 1;

    @Column(nullable = false)
    private Boolean willEat = false;

    @Column(nullable = false)
    private Integer mealCount = 0;

    @Column(nullable = false)
    private Boolean willRide = false;

    @Column(nullable = false)
    private Integer rideCount = 0;

    @Column(length = 50)
    private String note;

    @Column(nullable = false)
    private Boolean consent = false;

    @Builder
    private Rsvp(Wedding wedding, String sessionId, User user, RsvpAttendance attendance,
                 String name, RsvpSide side, String phone, Integer attendeeCount,
                 Boolean willEat, Integer mealCount, Boolean willRide, Integer rideCount,
                 String note, Boolean consent) {
        this.wedding = wedding;
        this.sessionId = sessionId;
        this.user = user;
        this.attendance = attendance;
        this.name = name;
        this.side = side;
        this.phone = phone;
        this.attendeeCount = attendeeCount != null ? attendeeCount : 1;
        this.willEat = willEat != null ? willEat : false;
        this.mealCount = mealCount != null ? mealCount : 0;
        this.willRide = willRide != null ? willRide : false;
        this.rideCount = rideCount != null ? rideCount : 0;
        this.note = note;
        this.consent = consent != null ? consent : false;
    }

    public void update(RsvpAttendance attendance, String name, RsvpSide side, String phone,
                       int attendeeCount, boolean willEat, int mealCount,
                       boolean willRide, int rideCount, String note) {
        this.attendance = attendance;
        this.name = name;
        this.side = side;
        this.phone = phone;
        this.attendeeCount = attendeeCount;
        this.willEat = willEat;
        this.mealCount = mealCount;
        this.willRide = willRide;
        this.rideCount = rideCount;
        this.note = note;
    }
}

package com.soaengry.moment.domain.invitation.entity;

import com.soaengry.moment.domain.event.entity.Event;
import com.soaengry.moment.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "invitations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"event_id", "user_id"})
})
public class Invitation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_invitations_events_event_id"))
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_invitations_users_user_id"))
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InvitationStatus status;

    @Builder
    private Invitation(Event event, User user, InvitationStatus status) {
        this.event = event;
        this.user = user;
        this.status = status != null ? status : InvitationStatus.INVITED;
    }

    public static Invitation create(Event event, User user) {
        return Invitation.builder()
                .event(event)
                .user(user)
                .status(InvitationStatus.INVITED)
                .build();
    }

    public void updateStatus(InvitationStatus status) {
        this.status = status;
    }
}

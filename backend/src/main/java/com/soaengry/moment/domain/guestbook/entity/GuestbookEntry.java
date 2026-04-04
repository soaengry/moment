package com.soaengry.moment.domain.guestbook.entity;

import com.soaengry.moment.domain.invitation.entity.Invitation;
import com.soaengry.moment.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "guestbook_entries")
public class GuestbookEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invitation_id", nullable = false)
    private Invitation invitation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 50)
    private String authorName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String password;

    @Column(nullable = false)
    private Boolean isSecret = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private GuestbookEntry(Invitation invitation, User user, String authorName, String content,
                           String password, Boolean isSecret) {
        this.invitation = invitation;
        this.user = user;
        this.authorName = authorName;
        this.content = content;
        this.password = password;
        this.isSecret = isSecret != null ? isSecret : false;
    }

    public static GuestbookEntry create(Invitation invitation, User user, String authorName,
                                         String content, String password, Boolean isSecret) {
        return new GuestbookEntry(invitation, user, authorName, content, password, isSecret);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String content, Boolean isSecret) {
        this.content = content;
        if (isSecret != null) {
            this.isSecret = isSecret;
        }
    }
}

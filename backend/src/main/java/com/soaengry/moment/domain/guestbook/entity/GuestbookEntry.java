package com.soaengry.moment.domain.guestbook.entity;

import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.domain.wedding.entity.Wedding;
import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "guestbook_entries")
public class GuestbookEntry extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_guestbook_entries_weddings_wedding_id"))
    private Wedding wedding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            foreignKey = @ForeignKey(name = "fk_guestbook_entries_users_user_id"))
    private User user;

    @Column(nullable = false, length = 50)
    private String authorName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column
    private String password;

    @Column(nullable = false)
    private Boolean isSecret = false;

    private GuestbookEntry(Wedding wedding, User user, String authorName, String content,
                           String password, Boolean isSecret) {
        this.wedding = wedding;
        this.user = user;
        this.authorName = authorName;
        this.content = content;
        this.password = password;
        this.isSecret = isSecret != null ? isSecret : false;
    }

    public static GuestbookEntry create(Wedding wedding, User user, String authorName,
                                        String content, String password, Boolean isSecret) {
        return new GuestbookEntry(wedding, user, authorName, content, password, isSecret);
    }

    public void update(String content, Boolean isSecret) {
        this.content = content;
        if (isSecret != null) {
            this.isSecret = isSecret;
        }
    }
}

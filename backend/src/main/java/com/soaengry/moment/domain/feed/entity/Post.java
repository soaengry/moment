package com.soaengry.moment.domain.feed.entity;

import com.soaengry.moment.domain.user.entity.User;
import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts", indexes = {
        @Index(name = "idx_post_event_created", columnList = "eventId, createdAt"),
        @Index(name = "idx_post_user_created", columnList = "user_id, createdAt")
})
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_posts_users_user_id"))
    private User user;

    @Column
    private Long eventId;

    @Column(nullable = false, length = 200)
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @org.hibernate.annotations.BatchSize(size = 100)
    private List<PostImage> images = new ArrayList<>();

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer commentCount = 0;

    @Column
    private LocalDateTime deletedAt;

    private Post(User user, String content, Long eventId) {
        this.user = user;
        this.content = content;
        this.eventId = eventId;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    public static Post create(User user, String content) {
        return new Post(user, content, null);
    }

    public static Post create(User user, String content, Long eventId) {
        return new Post(user, content, eventId);
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }

    public void update(String content) {
        this.content = content;
    }

    public void addImage(PostImage image) {
        this.images.add(image);
    }

    public void clearImages() {
        this.images.clear();
    }

    public void incrementLikeCount() {
        this.likeCount++;
    }

    public void decrementLikeCount() {
        if (this.likeCount > 0) this.likeCount--;
    }

    public void incrementCommentCount() {
        this.commentCount++;
    }

    public void decrementCommentCount() {
        if (this.commentCount > 0) this.commentCount--;
    }
}

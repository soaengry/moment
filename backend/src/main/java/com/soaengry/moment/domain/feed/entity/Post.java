package com.soaengry.moment.domain.feed.entity;

import com.soaengry.moment.domain.user.entity.User;
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
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private List<PostImage> images = new ArrayList<>();

    @Column(nullable = false)
    private Integer likeCount = 0;

    @Column(nullable = false)
    private Integer commentCount = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Post(User user, String content) {
        this.user = user;
        this.content = content;
        this.likeCount = 0;
        this.commentCount = 0;
    }

    public static Post create(User user, String content) {
        return new Post(user, content);
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

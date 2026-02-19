package com.soaengry.moment.domain.user.entity;

import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_provider", columnList = "auth_provider, provider_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column
    private String password;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    @Column(length = 500)
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider authProvider = AuthProvider.LOCAL;

    @Column
    private String providerId;

    @Column(nullable = false)
    private Boolean isEmailVerified = false;

    @Column(nullable = false)
    private Integer tokenVersion = 0;

    @Version
    private Long version = 0L;

    @Column
    private LocalDateTime deletedAt;

    @Builder
    public User(String email, String password, String nickname, String profileImageUrl,
                Role role, AuthProvider authProvider, String providerId, Boolean isEmailVerified) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.role = role != null ? role : Role.USER;
        this.authProvider = authProvider != null ? authProvider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.isEmailVerified = isEmailVerified != null ? isEmailVerified : false;
        this.tokenVersion = 0;
    }

    public void updateProfile(String nickname, String profileImageUrl) {
        if (nickname != null) {
            this.nickname = nickname;
        }
        // profileImageUrl을 명시적으로 설정 (null 포함)
        this.profileImageUrl = profileImageUrl;
    }

    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
        incrementTokenVersion();
    }

    public void verifyEmail() {
        this.isEmailVerified = true;
    }

    public void incrementTokenVersion() {
        this.tokenVersion++;
    }

    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
        incrementTokenVersion();
    }

    public void restore() {
        this.deletedAt = null;
        incrementTokenVersion();
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }

    public boolean isSocialLogin() {
        return this.authProvider != AuthProvider.LOCAL;
    }

    public enum Role {
        USER, ADMIN
    }

    public enum AuthProvider {
        LOCAL, KAKAO, NAVER, GOOGLE
    }
}
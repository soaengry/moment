package com.soaengry.moment.domain.email.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "email_verifications", indexes = {
        @Index(name = "idx_email_code", columnList = "email, verification_code")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailVerification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 255)
    private String verificationCode;

    @Column(nullable = false)
    private Integer attemptCount = 0;

    @Column(nullable = false)
    private Boolean isLocked = false;

    @Column
    private LocalDateTime lockedUntil;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public EmailVerification(String email, String verificationCode, LocalDateTime expiresAt) {
        this.email = email;
        this.verificationCode = verificationCode;
        this.attemptCount = 0;
        this.isLocked = false;
        this.expiresAt = expiresAt;
        this.isVerified = false;
        this.createdAt = LocalDateTime.now();
    }

    public void incrementAttempt() {
        this.attemptCount++;
        if (this.attemptCount >= 5) {
            lock();
        }
    }

    public void lock() {
        this.isLocked = true;
        this.lockedUntil = LocalDateTime.now().plusMinutes(30);
    }

    public void verify() {
        this.isVerified = true;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }

    public boolean isLockedNow() {
        if (!this.isLocked) {
            return false;
        }
        if (this.lockedUntil != null && LocalDateTime.now().isAfter(this.lockedUntil)) {
            this.isLocked = false;
            return false;
        }
        return true;
    }

    public int getRemainingAttempts() {
        return Math.max(0, 5 - this.attemptCount);
    }
}

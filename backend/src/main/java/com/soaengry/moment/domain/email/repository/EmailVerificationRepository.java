package com.soaengry.moment.domain.email.repository;

import com.soaengry.moment.domain.email.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email " +
            "AND ev.verificationCode = :code AND ev.isVerified = false " +
            "ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findLatestByEmailAndCode(
            @Param("email") String email,
            @Param("code") String code
    );

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email " +
            "AND ev.isVerified = false ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findLatestByEmail(@Param("email") String email);

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.verificationCode = :code " +
            "AND ev.isVerified = false ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findByVerificationCode(@Param("code") String code);

    @Query("SELECT ev FROM EmailVerification ev WHERE ev.email = :email AND ev.isVerified = true ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findVerifiedByEmail(@Param("email") String email);

    void deleteByEmail(String email);

    boolean existsByEmailAndIsVerified(String email, Boolean isVerified);
}
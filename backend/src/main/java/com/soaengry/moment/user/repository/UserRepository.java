package com.soaengry.moment.user.repository;

import com.soaengry.moment.user.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByNickname(String nickname);

    @Query("SELECT u FROM User u WHERE u.authProvider = :provider AND u.providerId = :providerId")
    Optional<User> findByProviderAndProviderId(
        @Param("provider") User.AuthProvider provider,
        @Param("providerId") String providerId
    );

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    @Lock(LockModeType.OPTIMISTIC)
    @Query("SELECT u FROM User u WHERE u.id = :id")
    Optional<User> findByIdWithOptimisticLock(@Param("id") Long id);

    @Query("SELECT u FROM User u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :threshold")
    List<User> findDeletedUsersBefore(@Param("threshold") LocalDateTime threshold);

    @Query("SELECT u FROM User u WHERE u.email = :email AND u.deletedAt IS NOT NULL " +
           "AND u.deletedAt > :threshold")
    Optional<User> findDeletedUserByEmailAfter(
        @Param("email") String email,
        @Param("threshold") LocalDateTime threshold
    );
}

package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Post;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p.id FROM Post p " +
           "WHERE p.deletedAt IS NULL " +
           "AND (:cursor IS NULL OR p.id < :cursor) " +
           "AND (p.eventId IS NULL " +
           "  OR EXISTS (SELECT e FROM Event e WHERE e.id = p.eventId AND e.isPublic = true) " +
           "  OR (:userId IS NOT NULL AND EXISTS (SELECT a FROM Attendance a WHERE a.eventId = p.eventId AND a.userId = :userId))) " +
           "ORDER BY p.id DESC")
    List<Long> findVisiblePostIdsCursor(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.id IN :ids")
    List<Post> findWithUserAndImagesByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId " +
           "AND p.deletedAt IS NULL AND (:cursor IS NULL OR p.id < :cursor) ORDER BY p.id DESC")
    List<Post> findByUserIdCursor(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.eventId = :eventId " +
           "AND p.deletedAt IS NULL AND (:cursor IS NULL OR p.id < :cursor) ORDER BY p.id DESC")
    List<Post> findByEventIdCursor(@Param("eventId") Long eventId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId " +
           "AND (:eventId IS NULL OR p.eventId = :eventId) AND p.deletedAt IS NULL " +
           "AND (:cursor IS NULL OR p.id < :cursor) ORDER BY p.id DESC")
    List<Post> findByUserIdAndOptionalEventIdCursor(@Param("userId") Long userId, @Param("eventId") Long eventId, @Param("cursor") Long cursor, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.deletedAt = :now WHERE p.eventId = :eventId AND p.deletedAt IS NULL")
    int softDeleteByEventId(@Param("eventId") Long eventId, @Param("now") LocalDateTime now);
}

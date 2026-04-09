package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query(value = "SELECT p.id FROM Post p " +
           "WHERE p.deletedAt IS NULL " +
           "AND (p.weddingId IS NULL " +
           "  OR EXISTS (SELECT i FROM Invitation i WHERE i.id = p.weddingId AND i.isPublic = true) " +
           "  OR (:userId IS NOT NULL AND EXISTS (SELECT a FROM Attendance a WHERE a.weddingId = p.weddingId AND a.userId = :userId))) " +
           "ORDER BY p.createdAt DESC",
           countQuery = "SELECT COUNT(p) FROM Post p " +
           "WHERE p.deletedAt IS NULL " +
           "AND (p.weddingId IS NULL " +
           "  OR EXISTS (SELECT i FROM Invitation i WHERE i.id = p.weddingId AND i.isPublic = true) " +
           "  OR (:userId IS NOT NULL AND EXISTS (SELECT a FROM Attendance a WHERE a.weddingId = p.weddingId AND a.userId = :userId)))")
    Page<Long> findVisiblePostIds(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images WHERE p.id IN :ids")
    List<Post> findWithUserAndImagesByIds(@Param("ids") List<Long> ids);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images " +
           "WHERE p.user.id = :userId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdWithImages(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images " +
           "WHERE p.weddingId = :weddingId AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Post> findByWeddingIdWithUserAndImages(@Param("weddingId") Long weddingId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user LEFT JOIN FETCH p.images " +
           "WHERE p.user.id = :userId AND (:weddingId IS NULL OR p.weddingId = :weddingId) " +
           "AND p.deletedAt IS NULL ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndOptionalWeddingId(@Param("userId") Long userId, @Param("weddingId") Long weddingId, Pageable pageable);

    @Modifying
    @Query("UPDATE Post p SET p.deletedAt = :now WHERE p.weddingId = :weddingId AND p.deletedAt IS NULL")
    int softDeleteByWeddingId(@Param("weddingId") Long weddingId, @Param("now") LocalDateTime now);
}

package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("SELECT p FROM Post p JOIN FETCH p.user ORDER BY p.createdAt DESC")
    Page<Post> findAllWithUserAndImages(Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdWithImages(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.eventId = :eventId ORDER BY p.createdAt DESC")
    Page<Post> findByEventIdWithUserAndImages(@Param("eventId") Long eventId, Pageable pageable);

    @Query("SELECT p FROM Post p JOIN FETCH p.user WHERE p.user.id = :userId AND (:eventId IS NULL OR p.eventId = :eventId) ORDER BY p.createdAt DESC")
    Page<Post> findByUserIdAndOptionalEventId(@Param("userId") Long userId, @Param("eventId") Long eventId, Pageable pageable);
}

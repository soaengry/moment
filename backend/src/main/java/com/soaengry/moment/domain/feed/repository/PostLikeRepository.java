package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<PostLike> findByUserIdAndPostIdIn(Long userId, List<Long> postIds);

    @Query("SELECT pl.post.id FROM PostLike pl WHERE pl.user.id = :userId " +
           "AND (:eventId IS NULL OR pl.post.eventId = :eventId) AND (:cursor IS NULL OR pl.post.id < :cursor) ORDER BY pl.post.id DESC")
    List<Long> findLikedPostIdsByUserIdAndOptionalEventIdCursor(@Param("userId") Long userId, @Param("eventId") Long eventId, @Param("cursor") Long cursor, Pageable pageable);
}

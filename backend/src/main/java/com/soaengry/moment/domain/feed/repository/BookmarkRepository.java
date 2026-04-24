package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Bookmark;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    Optional<Bookmark> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    List<Bookmark> findByUserIdAndPostIdIn(Long userId, List<Long> postIds);

    @Query("SELECT b.post.id FROM Bookmark b WHERE b.user.id = :userId " +
           "AND (:cursor IS NULL OR b.post.id < :cursor) ORDER BY b.post.id DESC")
    List<Long> findBookmarkedPostIdsByUserIdCursor(@Param("userId") Long userId, @Param("cursor") Long cursor, Pageable pageable);

    @Query("SELECT b.post.id FROM Bookmark b WHERE b.user.id = :userId " +
           "AND (:eventId IS NULL OR b.post.eventId = :eventId) AND (:cursor IS NULL OR b.post.id < :cursor) ORDER BY b.post.id DESC")
    List<Long> findBookmarkedPostIdsByUserIdAndOptionalEventIdCursor(@Param("userId") Long userId, @Param("eventId") Long eventId, @Param("cursor") Long cursor, Pageable pageable);
}

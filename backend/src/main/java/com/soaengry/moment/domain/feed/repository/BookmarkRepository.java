package com.soaengry.moment.domain.feed.repository;

import com.soaengry.moment.domain.feed.entity.Bookmark;
import org.springframework.data.domain.Page;
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

    @Query("SELECT b.post.id FROM Bookmark b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    Page<Long> findBookmarkedPostIdsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b.post.id FROM Bookmark b WHERE b.user.id = :userId AND (:eventId IS NULL OR b.post.eventId = :eventId) ORDER BY b.createdAt DESC")
    Page<Long> findBookmarkedPostIdsByUserIdAndOptionalEventId(@Param("userId") Long userId, @Param("eventId") Long eventId, Pageable pageable);
}

package com.soaengry.moment.domain.guestbook.repository;

import com.soaengry.moment.domain.guestbook.entity.GuestbookEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GuestbookEntryRepository extends JpaRepository<GuestbookEntry, Long> {

    @Query("SELECT g FROM GuestbookEntry g LEFT JOIN FETCH g.user WHERE g.wedding.id = :weddingId ORDER BY g.createdAt DESC")
    Page<GuestbookEntry> findByWeddingIdOrderByCreatedAtDesc(@Param("weddingId") Long weddingId, Pageable pageable);

    /**
     * 권한 기반 조회: 비밀 글은 본인 글만 포함 (userId = null이면 비밀 글 전체 제외)
     */
    @Query("SELECT g FROM GuestbookEntry g LEFT JOIN FETCH g.user " +
            "WHERE g.wedding.id = :weddingId " +
            "AND (g.isSecret = false OR (:userId IS NOT NULL AND g.user.id = :userId)) " +
            "ORDER BY g.createdAt DESC")
    Page<GuestbookEntry> findVisibleEntriesForUser(
            @Param("weddingId") Long weddingId,
            @Param("userId") Long userId,
            Pageable pageable);

    long countByWeddingId(Long weddingId);
}

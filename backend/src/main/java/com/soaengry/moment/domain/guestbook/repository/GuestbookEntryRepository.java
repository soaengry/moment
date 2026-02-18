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

    long countByWeddingId(Long weddingId);
}

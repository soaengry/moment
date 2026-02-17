package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByWeddingIdOrderByIsPinnedDescCreatedAtDesc(Long weddingId);
}

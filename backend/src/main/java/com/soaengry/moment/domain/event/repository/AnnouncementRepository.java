package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByEventIdOrderByIsPinnedDescCreatedAtDesc(Long eventId);
}

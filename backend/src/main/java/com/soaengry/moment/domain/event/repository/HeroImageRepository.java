package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.HeroImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HeroImageRepository extends JpaRepository<HeroImage, Long> {
    List<HeroImage> findByEventIdOrderByOrderIndex(Long eventId);

    @Modifying
    @Query("DELETE FROM HeroImage h WHERE h.event.id = :eventId")
    void deleteByEventId(@Param("eventId") Long eventId);
}

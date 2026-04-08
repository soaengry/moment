package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.HeroImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HeroImageRepository extends JpaRepository<HeroImage, Long> {
    List<HeroImage> findByEventIdOrderByOrderIndex(Long eventId);
}

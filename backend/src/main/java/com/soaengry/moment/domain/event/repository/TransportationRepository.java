package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportationRepository extends JpaRepository<Transportation, Long> {
    List<Transportation> findByEventIdOrderByOrderIndex(Long eventId);
}

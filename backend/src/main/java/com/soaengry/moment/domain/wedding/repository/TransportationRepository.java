package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportationRepository extends JpaRepository<Transportation, Long> {

    List<Transportation> findByWeddingIdOrderByOrderIndex(Long weddingId);
}

package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportationRepository extends JpaRepository<Transportation, Long> {

    List<Transportation> findByWeddingIdOrderByOrderIndex(Long weddingId);
}

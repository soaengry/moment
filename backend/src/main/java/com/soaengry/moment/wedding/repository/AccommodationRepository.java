package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {

    List<Accommodation> findByWeddingIdOrderByOrderIndex(Long weddingId);
}

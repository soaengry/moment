package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.Wedding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WeddingRepository extends JpaRepository<Wedding, Long> {
    Optional<Wedding> findByEventId(Long eventId);
    boolean existsByEventId(Long eventId);
}

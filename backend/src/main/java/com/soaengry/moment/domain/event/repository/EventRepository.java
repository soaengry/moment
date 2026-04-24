package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, Long> {
    Optional<Event> findBySlug(String slug);
    boolean existsBySlug(String slug);
    List<Event> findByUserIdOrderByCreatedAtDesc(Long userId);
    Page<Event> findBySlugContainingIgnoreCaseAndIsPublicTrueOrderByCreatedAtDesc(String slug, Pageable pageable);
}

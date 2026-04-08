package com.soaengry.moment.domain.invitation.repository;

import com.soaengry.moment.domain.invitation.entity.Rsvp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RsvpRepository extends JpaRepository<Rsvp, Long> {
    List<Rsvp> findByWeddingId(Long weddingId);
    Optional<Rsvp> findByWeddingIdAndSessionId(Long weddingId, String sessionId);
    boolean existsByWeddingIdAndSessionId(Long weddingId, String sessionId);
}

package com.soaengry.moment.domain.rsvp.repository;

import com.soaengry.moment.domain.rsvp.entity.Rsvp;
import com.soaengry.moment.domain.rsvp.entity.RsvpAttendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RsvpRepository extends JpaRepository<Rsvp, Long> {

    List<Rsvp> findByWeddingId(Long weddingId);

    Page<Rsvp> findByWeddingIdOrderByCreatedAtDesc(Long weddingId, Pageable pageable);

    Optional<Rsvp> findByWeddingIdAndSessionId(Long weddingId, String sessionId);

    Optional<Rsvp> findByWeddingIdAndUserId(Long weddingId, Long userId);

    boolean existsByWeddingIdAndUserId(Long weddingId, Long userId);

    long countByWeddingId(Long weddingId);

    long countByWeddingIdAndAttendance(Long weddingId, RsvpAttendance attendance);

    @Query("SELECT COALESCE(SUM(r.attendeeCount), 0) FROM Rsvp r WHERE r.wedding.id = :weddingId AND r.attendance = :attendance")
    long sumAttendeeCountByWeddingIdAndAttendance(@Param("weddingId") Long weddingId, @Param("attendance") RsvpAttendance attendance);

    @Query("SELECT COALESCE(SUM(r.mealCount), 0) FROM Rsvp r WHERE r.wedding.id = :weddingId AND r.willEat = true")
    long sumMealCountByWeddingId(@Param("weddingId") Long weddingId);

    @Query("SELECT COALESCE(SUM(r.rideCount), 0) FROM Rsvp r WHERE r.wedding.id = :weddingId AND r.willRide = true")
    long sumRideCountByWeddingId(@Param("weddingId") Long weddingId);
}

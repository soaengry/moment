package com.soaengry.moment.domain.attendance.repository;

import com.soaengry.moment.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    List<Attendance> findByUserIdOrderByCreatedAtDesc(Long userId);

    boolean existsByUserIdAndWeddingId(Long userId, Long weddingId);

    Optional<Attendance> findByIdAndUserId(Long id, Long userId);
}

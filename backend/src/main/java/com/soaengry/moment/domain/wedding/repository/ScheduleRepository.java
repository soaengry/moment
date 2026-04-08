package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByWeddingIdOrderByOrderIndex(Long weddingId);
}

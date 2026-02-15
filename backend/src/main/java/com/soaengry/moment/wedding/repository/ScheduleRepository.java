package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    List<Schedule> findByWeddingIdOrderByOrderIndex(Long weddingId);
}

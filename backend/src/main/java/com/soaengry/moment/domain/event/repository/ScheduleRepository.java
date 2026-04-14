package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByEventIdOrderByOrderIndex(Long eventId);
    void deleteByEventId(Long eventId);
}

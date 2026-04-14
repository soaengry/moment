package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.Host;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HostRepository extends JpaRepository<Host, Long> {
    List<Host> findByEventIdOrderByRole(Long eventId);
    List<Host> findByEventId(Long eventId);
    List<Host> findByEventIdIn(List<Long> eventIds);
    void deleteByEventId(Long eventId);
}

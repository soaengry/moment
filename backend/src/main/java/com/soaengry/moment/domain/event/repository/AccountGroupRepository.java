package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.AccountGroup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountGroupRepository extends JpaRepository<AccountGroup, Long> {
    List<AccountGroup> findByEventIdOrderByOrderIndex(Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(ag) FROM AccountGroup ag WHERE ag.eventId = :eventId")
    long countByEventIdForUpdate(@Param("eventId") Long eventId);

    void deleteByEventId(Long eventId);
}

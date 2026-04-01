package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.AccountGroup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountGroupRepository extends JpaRepository<AccountGroup, Long> {

    List<AccountGroup> findByWeddingIdOrderByOrderIndex(Long weddingId);

    long countByWeddingId(Long weddingId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(ag) FROM AccountGroup ag WHERE ag.weddingId = :weddingId")
    long countByWeddingIdForUpdate(@Param("weddingId") Long weddingId);
}

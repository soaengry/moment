package com.soaengry.moment.domain.event.repository;

import com.soaengry.moment.domain.event.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByAccountGroupIdOrderByOrderIndex(Long accountGroupId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(a) FROM Account a WHERE a.accountGroupId = :accountGroupId")
    long countByAccountGroupIdForUpdate(@Param("accountGroupId") Long accountGroupId);

    void deleteByAccountGroupIdIn(List<Long> accountGroupIds);
}

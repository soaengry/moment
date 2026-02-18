package com.soaengry.moment.domain.bank.repository;

import com.soaengry.moment.domain.bank.entity.BankPrefix;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BankPrefixRepository extends JpaRepository<BankPrefix, Long> {

    @Query("SELECT bp FROM BankPrefix bp JOIN FETCH bp.bank WHERE bp.prefix = :prefix")
    Optional<BankPrefix> findByPrefixWithBank(@Param("prefix") String prefix);
}

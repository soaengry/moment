package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findByAccountGroupIdOrderByOrderIndex(Long accountGroupId);

    long countByAccountGroupId(Long accountGroupId);
}

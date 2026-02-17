package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.AccountGroup;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountGroupRepository extends JpaRepository<AccountGroup, Long> {

    List<AccountGroup> findByWeddingIdOrderByOrderIndex(Long weddingId);

    long countByWeddingId(Long weddingId);
}

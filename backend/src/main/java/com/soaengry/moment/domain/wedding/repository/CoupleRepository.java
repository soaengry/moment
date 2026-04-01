package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    List<Couple> findByWeddingIdOrderByRole(Long weddingId);

    List<Couple> findByWeddingId(Long weddingId);

    List<Couple> findByWeddingIdIn(List<Long> weddingIds);

    boolean existsByWeddingIdAndEmail(Long weddingId, String email);
}

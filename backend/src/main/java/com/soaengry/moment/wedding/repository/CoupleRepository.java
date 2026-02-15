package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    List<Couple> findByWeddingIdOrderByRole(Long weddingId);

    List<Couple> findByWeddingId(Long weddingId);
}

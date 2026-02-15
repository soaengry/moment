package com.soaengry.moment.wedding.repository;

import com.soaengry.moment.wedding.entity.Wedding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeddingRepository extends JpaRepository<Wedding, Long> {
}

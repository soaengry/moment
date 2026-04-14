package com.soaengry.moment.domain.wedding.repository;

import com.soaengry.moment.domain.wedding.entity.WeddingHost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WeddingHostRepository extends JpaRepository<WeddingHost, Long> {
    Optional<WeddingHost> findByHostId(Long hostId);

    List<WeddingHost> findByHostIdIn(List<Long> hostIds);

    void deleteByHostId(Long hostId);
    void deleteByHostIdIn(List<Long> hostIds);
}

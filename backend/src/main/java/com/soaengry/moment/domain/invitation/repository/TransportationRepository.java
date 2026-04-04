package com.soaengry.moment.domain.invitation.repository;

import com.soaengry.moment.domain.invitation.entity.Transportation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransportationRepository extends JpaRepository<Transportation, Long> {
    List<Transportation> findByInvitationIdOrderByOrderIndex(Long invitationId);
}

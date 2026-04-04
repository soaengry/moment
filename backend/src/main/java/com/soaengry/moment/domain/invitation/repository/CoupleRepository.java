package com.soaengry.moment.domain.invitation.repository;

import com.soaengry.moment.domain.invitation.entity.Couple;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    List<Couple> findByInvitationIdOrderByRole(Long invitationId);

    List<Couple> findByInvitationId(Long invitationId);

    List<Couple> findByInvitationIdIn(List<Long> invitationIds);

    boolean existsByInvitationIdAndEmail(Long invitationId, String email);
}

package com.soaengry.moment.domain.invitation.repository;

import com.soaengry.moment.domain.invitation.entity.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByInvitationId(String invitationId);
    boolean existsByInvitationId(String invitationId);
}

package com.soaengry.moment.domain.invitation.repository;

import com.soaengry.moment.domain.invitation.entity.Accommodation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    List<Accommodation> findByInvitationIdOrderByOrderIndex(Long invitationId);
}

package com.soaengry.moment.domain.invitation.repository;

import com.soaengry.moment.domain.invitation.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findByInvitationIdOrderByIsPinnedDescCreatedAtDesc(Long invitationId);
}

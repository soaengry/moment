package com.soaengry.moment.domain.invitation.repository;

import com.soaengry.moment.domain.invitation.entity.AccountGroup;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AccountGroupRepository extends JpaRepository<AccountGroup, Long> {

    List<AccountGroup> findByInvitationIdOrderByOrderIndex(Long invitationId);

    long countByInvitationId(Long invitationId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT COUNT(ag) FROM AccountGroup ag WHERE ag.invitationId = :invitationId")
    long countByInvitationIdForUpdate(@Param("invitationId") Long invitationId);
}

package com.soaengry.moment.domain.invitation.dto.request;

import com.soaengry.moment.domain.invitation.entity.AccountGroup;

public record AccountGroupRequest(
        String groupName,
        Integer orderIndex
) {
    public AccountGroup toEntity(Long invitationId) {
        return AccountGroup.create(invitationId, groupName, orderIndex);
    }
}

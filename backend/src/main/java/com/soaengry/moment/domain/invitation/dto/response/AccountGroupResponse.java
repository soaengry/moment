package com.soaengry.moment.domain.invitation.dto.response;

import com.soaengry.moment.domain.invitation.entity.AccountGroup;

public record AccountGroupResponse(
        Long id,
        Long invitationId,
        String groupName,
        Integer orderIndex
) {
    public static AccountGroupResponse from(AccountGroup accountGroup) {
        return new AccountGroupResponse(
                accountGroup.getId(),
                accountGroup.getInvitationId(),
                accountGroup.getGroupName(),
                accountGroup.getOrderIndex()
        );
    }
}

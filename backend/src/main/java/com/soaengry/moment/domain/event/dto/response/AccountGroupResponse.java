package com.soaengry.moment.domain.event.dto.response;

import com.soaengry.moment.domain.event.entity.AccountGroup;

public record AccountGroupResponse(
        Long id,
        Long eventId,
        String groupName,
        Integer orderIndex
) {
    public static AccountGroupResponse from(AccountGroup group) {
        return new AccountGroupResponse(
                group.getId(),
                group.getEventId(),
                group.getGroupName(),
                group.getOrderIndex()
        );
    }
}

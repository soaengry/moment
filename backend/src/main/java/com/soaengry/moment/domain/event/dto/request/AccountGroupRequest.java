package com.soaengry.moment.domain.event.dto.request;

import com.soaengry.moment.domain.event.entity.AccountGroup;

public record AccountGroupRequest(
        String groupName,
        Integer orderIndex
) {
    public AccountGroup toEntity(Long eventId) {
        return AccountGroup.create(eventId, groupName, orderIndex);
    }
}

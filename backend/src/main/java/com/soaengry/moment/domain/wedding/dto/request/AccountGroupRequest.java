package com.soaengry.moment.domain.wedding.dto.request;

import com.soaengry.moment.domain.wedding.entity.AccountGroup;

public record AccountGroupRequest(
        String groupName,
        Integer orderIndex
) {
    public AccountGroup toEntity(Long weddingId) {
        return AccountGroup.create(weddingId, groupName, orderIndex);
    }
}

package com.soaengry.moment.wedding.dto.request;

import com.soaengry.moment.wedding.entity.AccountGroup;

public record AccountGroupRequest(
        AccountGroup.Side side,
        String groupName,
        Integer orderIndex
) {
    public AccountGroup toEntity(Long weddingId) {
        return AccountGroup.create(
                weddingId,
                side,
                groupName,
                orderIndex
        );
    }
}
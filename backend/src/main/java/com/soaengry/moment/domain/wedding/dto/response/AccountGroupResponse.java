package com.soaengry.moment.domain.wedding.dto.response;

import com.soaengry.moment.domain.wedding.entity.AccountGroup;

public record AccountGroupResponse(
        Long id,
        Long weddingId,
        String groupName,
        Integer orderIndex
) {
    public static AccountGroupResponse from(AccountGroup group) {
        return new AccountGroupResponse(
                group.getId(),
                group.getWeddingId(),
                group.getGroupName(),
                group.getOrderIndex()
        );
    }
}

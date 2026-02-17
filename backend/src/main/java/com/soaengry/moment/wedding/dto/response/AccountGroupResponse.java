package com.soaengry.moment.wedding.dto.response;

import com.soaengry.moment.wedding.entity.AccountGroup;

public record AccountGroupResponse(
        Long id,
        Long weddingId,
        AccountGroup.Side side,
        String groupName,
        Integer orderIndex
) {
    public static AccountGroupResponse from(AccountGroup accountGroup) {
        return new AccountGroupResponse(
                accountGroup.getId(),
                accountGroup.getWeddingId(),
                accountGroup.getSide(),
                accountGroup.getGroupName(),
                accountGroup.getOrderIndex()
        );
    }
}
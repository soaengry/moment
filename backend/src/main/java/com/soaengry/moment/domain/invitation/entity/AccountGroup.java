package com.soaengry.moment.domain.invitation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_groups")
public class AccountGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invitationId;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private Integer orderIndex;

    private AccountGroup(Long invitationId, String groupName, Integer orderIndex) {
        this.invitationId = invitationId;
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }

    public static AccountGroup create(Long invitationId, String groupName, Integer orderIndex) {
        return new AccountGroup(invitationId, groupName, orderIndex);
    }

    public void update(String groupName, Integer orderIndex) {
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }
}

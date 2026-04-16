package com.soaengry.moment.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "account_groups")
public class AccountGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private Integer orderIndex;

    private AccountGroup(Long eventId, String groupName, Integer orderIndex) {
        this.eventId = eventId;
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }

    public static AccountGroup create(Long eventId, String groupName, Integer orderIndex) {
        return new AccountGroup(eventId, groupName, orderIndex);
    }

    public void update(String groupName, Integer orderIndex) {
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }
}

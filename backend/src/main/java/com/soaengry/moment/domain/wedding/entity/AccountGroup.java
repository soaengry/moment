package com.soaengry.moment.domain.wedding.entity;

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
    private Long weddingId;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private Integer orderIndex;

    private AccountGroup(Long weddingId, String groupName, Integer orderIndex) {
        this.weddingId = weddingId;
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }

    public static AccountGroup create(Long weddingId, String groupName, Integer orderIndex) {
        return new AccountGroup(weddingId, groupName, orderIndex);
    }

    public void update(String groupName, Integer orderIndex) {
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }
}

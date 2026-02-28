package com.soaengry.moment.domain.wedding.entity;

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
    private Long weddingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side;

    @Column(nullable = false)
    private String groupName;

    @Column(nullable = false)
    private Integer orderIndex;

    private AccountGroup(Long weddingId, Side side, String groupName, Integer orderIndex) {
        this.weddingId = weddingId;
        this.side = side;
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }

    public static AccountGroup create(Long weddingId, Side side, String groupName, Integer orderIndex) {
        return new AccountGroup(weddingId, side, groupName, orderIndex);
    }

    public void update(Side side, String groupName, Integer orderIndex) {
        this.side = side;
        this.groupName = groupName;
        this.orderIndex = orderIndex;
    }

    public enum Side {
        GROOM, GROOM_FAMILY, BRIDE, BRIDE_FAMILY
    }
}

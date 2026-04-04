package com.soaengry.moment.domain.invitation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "accommodations")
public class Accommodation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long invitationId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phone;

    private String distance;

    private String priceRange;

    @Column(nullable = false)
    private Integer orderIndex;

    private Accommodation(Long invitationId, String name, String address, String phone,
                          String distance, String priceRange, Integer orderIndex) {
        this.invitationId = invitationId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.distance = distance;
        this.priceRange = priceRange;
        this.orderIndex = orderIndex;
    }

    public static Accommodation create(Long invitationId, String name, String address, String phone,
                                       String distance, String priceRange, Integer orderIndex) {
        return new Accommodation(invitationId, name, address, phone, distance, priceRange, orderIndex);
    }

    public void update(String name, String address, String phone, String distance, String priceRange, Integer orderIndex) {
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.distance = distance;
        this.priceRange = priceRange;
        this.orderIndex = orderIndex;
    }
}

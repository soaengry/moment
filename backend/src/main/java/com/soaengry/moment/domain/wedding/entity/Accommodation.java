package com.soaengry.moment.domain.wedding.entity;

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
    private Long weddingId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phone;

    private String distance;

    private String priceRange;

    @Column(nullable = false)
    private Integer orderIndex;

    private Accommodation(Long weddingId, String name, String address, String phone, String distance,
                          String priceRange, Integer orderIndex) {
        this.weddingId = weddingId;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.distance = distance;
        this.priceRange = priceRange;
        this.orderIndex = orderIndex;
    }

    public static Accommodation create(Long weddingId, String name, String address, String phone,
                                       String distance, String priceRange, Integer orderIndex) {
        return new Accommodation(weddingId, name, address, phone, distance, priceRange, orderIndex);
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
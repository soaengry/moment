package com.soaengry.moment.wedding.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "couples")
public class Couple {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long weddingId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CoupleRole role;

    @Column(nullable = false)
    private String name;

    private String fatherName;

    private String motherName;

    @Column(nullable = false)
    private Boolean isFatherAlive;

    @Column(nullable = false)
    private Boolean isMotherAlive;

    private String contact;

    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    private Couple(Long weddingId, CoupleRole role, String name, String fatherName, String motherName,
                   Boolean isFatherAlive, Boolean isMotherAlive, String contact, String profileImageUrl, String introduction) {
        this.weddingId = weddingId;
        this.role = role;
        this.name = name;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.isFatherAlive = isFatherAlive;
        this.isMotherAlive = isMotherAlive;
        this.contact = contact;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
    }

    public static Couple create(Long weddingId, CoupleRole role, String name, String fatherName, String motherName,
                                Boolean isFatherAlive, Boolean isMotherAlive, String contact, String profileImageUrl, String introduction) {
        return new Couple(weddingId, role, name, fatherName, motherName, isFatherAlive, isMotherAlive,
                contact, profileImageUrl, introduction);
    }

    public void update(String name, String fatherName, String motherName, Boolean isFatherAlive, Boolean isMotherAlive,
                       String contact, String profileImageUrl, String introduction) {
        this.name = name;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.isFatherAlive = isFatherAlive;
        this.isMotherAlive = isMotherAlive;
        this.contact = contact;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
    }

    public enum CoupleRole {
        GROOM, BRIDE
    }
}

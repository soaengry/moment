package com.soaengry.moment.domain.wedding.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
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

    // Wedding과 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wedding_id", nullable = false)
    private Wedding wedding;

    @Column(nullable = false)
    private String email;

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

    @Builder
    private Couple(Wedding wedding, String email, CoupleRole role, String name, String fatherName, String motherName,
                   Boolean isFatherAlive, Boolean isMotherAlive, String contact, String profileImageUrl, String introduction) {
        this.wedding = wedding;
        this.email = email;
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

    public void updateName(String name) {
        this.name = name;
    }

    public void updateFather(String fatherName, Boolean isFatherAlive) {
        this.fatherName = fatherName;
        this.isFatherAlive = isFatherAlive;
    }

    public void updateMother(String motherName, Boolean isMotherAlive) {
        this.motherName = motherName;
        this.isMotherAlive = isMotherAlive;
    }

    public void updateContact(String contact) {
        this.contact = contact;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void updateProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public enum CoupleRole {
        GROOM, BRIDE
    }
}

package com.soaengry.moment.domain.wedding.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "hosts")
public class Host {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long eventId;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HostRole role;

    @Column(nullable = false)
    private String name;

    private String fatherName;
    private String motherName;

    @Column(nullable = false)
    private Boolean isFatherAlive = true;

    @Column(nullable = false)
    private Boolean isMotherAlive = true;

    private String contact;
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Builder
    private Host(Long eventId, String email, HostRole role, String name,
                 String fatherName, String motherName,
                 Boolean isFatherAlive, Boolean isMotherAlive,
                 String contact, String profileImageUrl, String introduction) {
        this.eventId = eventId;
        this.email = email;
        this.role = role;
        this.name = name;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.isFatherAlive = isFatherAlive != null ? isFatherAlive : true;
        this.isMotherAlive = isMotherAlive != null ? isMotherAlive : true;
        this.contact = contact;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
    }

    public void updateName(String name) { this.name = name; }
    public void updateFather(String fatherName, Boolean isFatherAlive) {
        this.fatherName = fatherName;
        this.isFatherAlive = isFatherAlive;
    }
    public void updateMother(String motherName, Boolean isMotherAlive) {
        this.motherName = motherName;
        this.isMotherAlive = isMotherAlive;
    }
    public void updateContact(String contact) { this.contact = contact; }
    public void updateIntroduction(String introduction) { this.introduction = introduction; }
    public void updateProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public enum HostRole { GROOM, BRIDE, HOST }
}

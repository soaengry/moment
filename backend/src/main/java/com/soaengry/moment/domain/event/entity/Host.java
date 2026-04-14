package com.soaengry.moment.domain.event.entity;

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

    private String contact;
    private String profileImageUrl;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Builder
    private Host(Long eventId, String email, HostRole role, String name,
                 String contact, String profileImageUrl, String introduction) {
        this.eventId = eventId;
        this.email = email;
        this.role = role;
        this.name = name;
        this.contact = contact;
        this.profileImageUrl = profileImageUrl;
        this.introduction = introduction;
    }

    public void updateName(String name) { this.name = name; }
    public void updateContact(String contact) { this.contact = contact; }
    public void updateIntroduction(String introduction) { this.introduction = introduction; }
    public void updateProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public enum HostRole { GROOM, BRIDE, HOST }
}

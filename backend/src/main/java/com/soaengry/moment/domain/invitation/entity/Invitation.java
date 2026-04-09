package com.soaengry.moment.domain.invitation.entity;

import com.soaengry.moment.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "invitations", indexes = {@Index(name = "idx_invitationId", columnList = "invitationId")})
public class Invitation extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String invitationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateType templateType;

    @Column(nullable = false)
    private LocalDateTime eventDate;

    @Column(nullable = false)
    private String venueName;

    @Column(nullable = false)
    private String venueAddress;

    private String venueDetail;

    @Column(nullable = false)
    private Double venueLat;

    @Column(nullable = false)
    private Double venueLng;

    private String venuePhone;

    @Column(columnDefinition = "TEXT")
    private String dressCode;

    @Column(columnDefinition = "TEXT")
    private String notice;

    @Column(columnDefinition = "TEXT")
    private String parkingInfo;

    @Column(columnDefinition = "TEXT")
    private String mealInfo;

    @Column(nullable = false)
    private boolean isPublic = false;

    @Builder
    private Invitation(String title, String invitationId, TemplateType templateType, LocalDateTime eventDate,
                       String venueName, String venueAddress, String venueDetail,
                       Double venueLat, Double venueLng, String venuePhone,
                       String dressCode, String notice, String parkingInfo, String mealInfo,
                       boolean isPublic) {
        this.title = title;
        this.invitationId = invitationId;
        this.templateType = templateType != null ? templateType : TemplateType.WEDDING;
        this.eventDate = eventDate;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueDetail = venueDetail;
        this.venueLat = venueLat;
        this.venueLng = venueLng;
        this.venuePhone = venuePhone;
        this.dressCode = dressCode;
        this.notice = notice;
        this.parkingInfo = parkingInfo;
        this.mealInfo = mealInfo;
        this.isPublic = isPublic;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public void updateVenue(String venueName, String venueAddress, String venueDetail,
                            Double venueLat, Double venueLng, String venuePhone) {
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueDetail = venueDetail;
        this.venueLat = venueLat;
        this.venueLng = venueLng;
        this.venuePhone = venuePhone;
    }

    public void updateDressCode(String dressCode) {
        this.dressCode = dressCode;
    }

    public void updateNotice(String notice) {
        this.notice = notice;
    }

    public void updateParkingInfo(String parkingInfo) {
        this.parkingInfo = parkingInfo;
    }

    public void updateMealInfo(String mealInfo) {
        this.mealInfo = mealInfo;
    }

    public void updateIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}

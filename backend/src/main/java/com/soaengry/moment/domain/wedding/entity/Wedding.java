package com.soaengry.moment.domain.wedding.entity;

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
@Table(name = "weddings", indexes = {@Index(name = "idx_invitationId", columnList = "invitationId")})
public class Wedding extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;                    // wedding id

    @Column(nullable = false)
    private String title;               // 초대장 제목

    @Column(nullable = false, unique = true)
    private String invitationId;        // 초대장 id

    @Column(nullable = false)
    private LocalDateTime weddingDate;  // 결혼식 일시

    @Column(nullable = false)
    private String venueName;           // 예식장 이름

    @Column(nullable = false)
    private String venueAddress;        // 예식장 주소

    private String venueDetail;         // 예식장 상세 주소

    @Column(nullable = false)
    private Double venueLat;            // 예식장 위도

    @Column(nullable = false)
    private Double venueLng;            // 예식장 경도

    private String venuePhone;          // 예식장 번호

    @Column(columnDefinition = "TEXT")
    private String dressCode;           // 드레스 코드

    @Column(columnDefinition = "TEXT")
    private String notice;              // 유의사항

    @Column(columnDefinition = "TEXT")
    private String parkingInfo;         // 주차장 정보

    @Column(columnDefinition = "TEXT")
    private String mealInfo;            // 식사 정보

    @Builder
    private Wedding(String title, String invitationId, LocalDateTime weddingDate, String venueName, String venueAddress,
                    String venueDetail, Double venueLat, Double venueLng, String venuePhone,
                    String dressCode, String notice, String parkingInfo, String mealInfo) {
        this.title = title;
        this.invitationId = invitationId;
        this.weddingDate = weddingDate;
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
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateWeddingDate(LocalDateTime weddingDate) {
        this.weddingDate = weddingDate;
    }

    public void updateVenue(String venueName, String venueAddress, String venueDetail, Double venueLat, Double venueLng, String venuePhone) {
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

}

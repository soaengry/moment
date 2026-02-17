package com.soaengry.moment.domain.wedding.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "weddings")
public class Wedding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime weddingDate;

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

    private String mapImageUrl;

    @Column(columnDefinition = "TEXT")
    private String dressCode;

    @Column(columnDefinition = "TEXT")
    private String notice;

    @Column(columnDefinition = "TEXT")
    private String parkingInfo;

    @Column(columnDefinition = "TEXT")
    private String mealInfo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private Wedding(String title, LocalDateTime weddingDate, String venueName, String venueAddress,
                    String venueDetail, Double venueLat, Double venueLng, String venuePhone,
                    String mapImageUrl, String dressCode, String notice, String parkingInfo, String mealInfo) {
        this.title = title;
        this.weddingDate = weddingDate;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueDetail = venueDetail;
        this.venueLat = venueLat;
        this.venueLng = venueLng;
        this.venuePhone = venuePhone;
        this.mapImageUrl = mapImageUrl;
        this.dressCode = dressCode;
        this.notice = notice;
        this.parkingInfo = parkingInfo;
        this.mealInfo = mealInfo;
    }

    public static Wedding create(String title, LocalDateTime weddingDate, String venueName, String venueAddress,
                                 String venueDetail, Double venueLat, Double venueLng, String venuePhone,
                                 String mapImageUrl, String dressCode, String notice, String parkingInfo, String mealInfo) {
        return new Wedding(title, weddingDate, venueName, venueAddress, venueDetail, venueLat, venueLng,
                venuePhone, mapImageUrl, dressCode, notice, parkingInfo, mealInfo);
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void update(String title, LocalDateTime weddingDate, String venueName, String venueAddress,
                       String venueDetail, Double venueLat, Double venueLng, String venuePhone,
                       String mapImageUrl, String dressCode, String notice, String parkingInfo, String mealInfo) {
        this.title = title;
        this.weddingDate = weddingDate;
        this.venueName = venueName;
        this.venueAddress = venueAddress;
        this.venueDetail = venueDetail;
        this.venueLat = venueLat;
        this.venueLng = venueLng;
        this.venuePhone = venuePhone;
        this.mapImageUrl = mapImageUrl;
        this.dressCode = dressCode;
        this.notice = notice;
        this.parkingInfo = parkingInfo;
        this.mealInfo = mealInfo;
    }
}

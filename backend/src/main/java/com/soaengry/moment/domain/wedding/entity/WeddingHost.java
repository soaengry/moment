package com.soaengry.moment.domain.wedding.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "wedding_hosts")
public class WeddingHost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long hostId;

    private String fatherName;
    private String motherName;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isFatherAlive = true;

    @Column(nullable = false, columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isMotherAlive = true;

    private WeddingHost(Long hostId, String fatherName, String motherName,
                        Boolean isFatherAlive, Boolean isMotherAlive) {
        this.hostId = hostId;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.isFatherAlive = isFatherAlive != null ? isFatherAlive : true;
        this.isMotherAlive = isMotherAlive != null ? isMotherAlive : true;
    }

    public static WeddingHost create(Long hostId, String fatherName, String motherName,
                                     Boolean isFatherAlive, Boolean isMotherAlive) {
        return new WeddingHost(hostId, fatherName, motherName, isFatherAlive, isMotherAlive);
    }

    public void update(String fatherName, String motherName,
                       Boolean isFatherAlive, Boolean isMotherAlive) {
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.isFatherAlive = isFatherAlive != null ? isFatherAlive : true;
        this.isMotherAlive = isMotherAlive != null ? isMotherAlive : true;
    }
}

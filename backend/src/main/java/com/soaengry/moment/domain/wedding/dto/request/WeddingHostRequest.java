package com.soaengry.moment.domain.wedding.dto.request;

public record WeddingHostRequest(
        String fatherName,
        String motherName,
        Boolean isFatherAlive,
        Boolean isMotherAlive
) {}

package com.soaengry.moment.domain.event.dto.response;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.soaengry.moment.domain.event.dto.response.GatheringDetailResponse;
import com.soaengry.moment.domain.wedding.dto.response.WeddingDetailResponse;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "detailType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WeddingDetailResponse.class, name = "wedding"),
        @JsonSubTypes.Type(value = GatheringDetailResponse.class, name = "gathering"),
})
public interface EventDetailResponse {
}
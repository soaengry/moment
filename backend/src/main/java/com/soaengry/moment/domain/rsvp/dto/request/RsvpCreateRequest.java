package com.soaengry.moment.domain.rsvp.dto.request;

import com.soaengry.moment.domain.rsvp.entity.RsvpAttendance;
import com.soaengry.moment.domain.rsvp.entity.RsvpSide;
import jakarta.validation.constraints.*;

public record RsvpCreateRequest(
        @NotNull Long weddingId,
        @NotNull RsvpAttendance attendance,
        @NotBlank @Size(max = 50) String name,
        @NotNull RsvpSide side,
        @NotBlank @Pattern(regexp = "\\d{2,3}-\\d{3,4}-\\d{4}") String phone,
        @Min(1) @Max(99) int attendeeCount,
        @NotNull MealRequest meal,
        @NotNull ShuttleRequest shuttle,
        @Size(max = 50) String note,
        @AssertTrue boolean consent
) {
    public record MealRequest(boolean willEat, @Min(0) @Max(99) int mealCount) {}
    public record ShuttleRequest(boolean willRide, @Min(0) @Max(99) int rideCount) {}
}

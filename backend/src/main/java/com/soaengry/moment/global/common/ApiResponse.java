package com.soaengry.moment.global.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.soaengry.moment.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private ApiStatus status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private T data;

    public static <T> ApiResponse<T> ok(SuccessCode code, T data) {
        return ApiResponse.<T>builder()
                .status(new ApiStatus(code.getCode(), code.getMessage()))
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> error(ErrorCode code) {
        return ApiResponse.<T>builder()
                .status(new ApiStatus(code.getHttpStatus().value(), code.getMessage()))
                .build();
    }
}

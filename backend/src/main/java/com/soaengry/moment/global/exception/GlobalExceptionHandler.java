package com.soaengry.moment.global.exception;

import com.soaengry.moment.global.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 비즈니스 예외 처리
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        log.error("Business exception: {}", e.getMessage());
        
        HttpStatus status = determineHttpStatus(e.getErrorCode());
        ApiResponse<Void> response = ApiResponse.error(e.getMessage());
        
        return ResponseEntity.status(status).body(response);
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationException(
            MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        
        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        log.error("Validation failed: {}", errors);
        
        ApiResponse<Map<String, String>> response = ApiResponse.success(errors);
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * 그 외 모든 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("Unexpected error", e);
        
        ApiResponse<Void> response = ApiResponse.error("서버 오류가 발생했습니다");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * ErrorCode에 따른 HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatus(ErrorCode errorCode) {
        String code = errorCode.name();
        
        if (code.startsWith("AUTH_")) {
            return HttpStatus.UNAUTHORIZED;
        }
        if (code.startsWith("DUPLICATE_")) {
            return HttpStatus.CONFLICT;
        }
        if (code.startsWith("NOT_FOUND_")) {
            return HttpStatus.NOT_FOUND;
        }
        if (code.startsWith("VALIDATION_")) {
            return HttpStatus.BAD_REQUEST;
        }
        
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }
}

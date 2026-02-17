package com.soaengry.moment.global.exception;

import com.soaengry.moment.domain.email.exception.EmailException;
import com.soaengry.moment.domain.user.exception.UserException;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * WeddingException 처리
     */
    @ExceptionHandler(WeddingException.class)
    public ResponseEntity<ErrorResponse> handleWeddingException(WeddingException e) {
        log.warn("Wedding Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorResponse response = ErrorResponse.of(
                e.getErrorCode().name(),
                e.getMessage(),
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }

    /**
     * UserException 처리
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorResponse> handleUserException(UserException e) {
        log.warn("User Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorResponse response = ErrorResponse.of(
                e.getErrorCode().name(),
                e.getMessage(),
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }

    /**
     * FileException 처리
     */
    @ExceptionHandler(FileException.class)
    public ResponseEntity<ErrorResponse> handleFileException(FileException e) {
        log.warn("File Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ErrorResponse response = ErrorResponse.of(
                e.getErrorCode().name(),
                e.getMessage(),
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }

    /**
     * EmailException 처리
     */
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ErrorResponse> handleEmailException(EmailException e) {
        log.error("Email Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponse response = ErrorResponse.of(
                e.getErrorCode().name(),
                e.getMessage(),
                status.value()
        );

        return ResponseEntity.status(status).body(response);
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorResponse response = ErrorResponse.of(
                "VALIDATION_ERROR",
                "입력값이 올바르지 않습니다",
                HttpStatus.BAD_REQUEST.value(),
                errors
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);

        ErrorResponse response = ErrorResponse.of(
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    /**
     * ErrorCode 이름에 따른 HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatusFromCode(String code) {
        if (code.startsWith("AUTH")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (code.startsWith("DUPLICATE")) {
            return HttpStatus.CONFLICT;
        } else if (code.endsWith("NOT_FOUND")) {
            return HttpStatus.NOT_FOUND;
        } else if (code.startsWith("VALIDATION")) {
            return HttpStatus.BAD_REQUEST;
        } else if (code.endsWith("LIMIT_EXCEEDED")) {
            return HttpStatus.BAD_REQUEST;
        } else {
            return HttpStatus.BAD_REQUEST;
        }
    }
}

/**
 * 에러 응답 DTO
 */
record ErrorResponse(
        String code,
        String message,
        int status,
        Map<String, String> errors,
        LocalDateTime timestamp
) {
    public static ErrorResponse of(String code, String message, int status) {
        return new ErrorResponse(code, message, status, null, LocalDateTime.now());
    }

    public static ErrorResponse of(String code, String message, int status, Map<String, String> errors) {
        return new ErrorResponse(code, message, status, errors, LocalDateTime.now());
    }
}
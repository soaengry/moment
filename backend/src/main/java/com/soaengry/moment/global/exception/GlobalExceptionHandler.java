package com.soaengry.moment.global.exception;

import com.soaengry.moment.domain.attendance.exception.AttendanceException;
import com.soaengry.moment.domain.chat.exception.ChatException;
import com.soaengry.moment.domain.email.exception.EmailException;
import com.soaengry.moment.domain.event.exception.EventException;
import com.soaengry.moment.domain.feed.exception.FeedException;
import com.soaengry.moment.domain.guestbook.exception.GuestbookException;
import com.soaengry.moment.domain.user.exception.UserException;
import com.soaengry.moment.domain.wedding.exception.WeddingException;
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
     * EventException 처리
     */
    @ExceptionHandler(EventException.class)
    public ResponseEntity<ApiResponse<?>> handleEventException(EventException e) {
        log.warn("Event Exception: {} - {}", e.getErrorCode().name(), e.getMessage());
        HttpStatus status = e.getErrorCode().getHttpStatus();
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);
        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * AttendanceException 처리
     */
    @ExceptionHandler(AttendanceException.class)
    public ResponseEntity<ApiResponse<?>> handleAttendanceException(AttendanceException e) {
        log.warn("Attendance Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);

        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * WeddingException 처리
     */
    @ExceptionHandler(WeddingException.class)
    public ResponseEntity<ApiResponse<?>> handleWeddingException(WeddingException e) {
        log.warn("Wedding Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);

        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * UserException 처리
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ApiResponse<?>> handleUserException(UserException e) {
        log.warn("User Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);

        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * FileException 처리
     */
    @ExceptionHandler(FileException.class)
    public ResponseEntity<ApiResponse<?>> handleFileException(FileException e) {
        log.warn("File Exception: {} - {}", e.getErrorCode().getCode(), e.getMessage());

        ErrorCode errorCode = e.getErrorCode();
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.error(errorCode));
    }

    /**
     * EmailException 처리
     */
    @ExceptionHandler(EmailException.class)
    public ResponseEntity<ApiResponse<?>> handleEmailException(EmailException e) {
        log.error("Email Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);

        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * GuestbookException 처리
     */
    @ExceptionHandler(GuestbookException.class)
    public ResponseEntity<ApiResponse<?>> handleGuestbookException(GuestbookException e) {
        log.warn("Guestbook Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);

        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * FeedException 처리
     */
    @ExceptionHandler(FeedException.class)
    public ResponseEntity<ApiResponse<?>> handleFeedException(FeedException e) {
        log.warn("Feed Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);

        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * ChatException 처리
     */
    @ExceptionHandler(ChatException.class)
    public ResponseEntity<ApiResponse<?>> handleChatException(ChatException e) {
        log.warn("Chat Exception: {} - {}", e.getErrorCode().name(), e.getMessage());

        HttpStatus status = determineHttpStatusFromCode(e.getErrorCode().name());
        ErrorCode errorCode = ErrorCode.from(e.getErrorCode().name(), e.getMessage(), status);

        return ResponseEntity.status(status).body(ApiResponse.error(errorCode));
    }

    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(MethodArgumentNotValidException e) {
        log.warn("Validation Exception: {}", e.getMessage());

        Map<String, String> errors = new HashMap<>();
        for (FieldError error : e.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }

        ErrorCode errorCode = ErrorCode.from(
                "VALIDATION_ERROR",
                "입력값이 올바르지 않습니다",
                HttpStatus.BAD_REQUEST
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.error(errorCode));
    }

    /**
     * 기타 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleException(Exception e) {
        log.error("Unexpected Exception: {}", e.getMessage(), e);

        ErrorCode errorCode = ErrorCode.from(
                "INTERNAL_SERVER_ERROR",
                "서버 오류가 발생했습니다",
                HttpStatus.INTERNAL_SERVER_ERROR
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.error(errorCode));
    }

    /**
     * ErrorCode 이름에 따른 HTTP 상태 코드 결정
     */
    private HttpStatus determineHttpStatusFromCode(String code) {
        if (code.startsWith("AUTH") || code.equals("UNAUTHORIZED_ACCESS")) {
            return HttpStatus.UNAUTHORIZED;
        } else if (code.equals("INVALID_PASSWORD") || code.endsWith("UNAUTHORIZED")) {
            return HttpStatus.FORBIDDEN;
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

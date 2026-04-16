package com.soaengry.moment.global.exception;

import org.springframework.http.HttpStatus;

public abstract class CustomException extends RuntimeException {

    protected CustomException(String code, String message, HttpStatus httpStatus) {
        super(message);
    }

    protected CustomException(String code, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
    }
}

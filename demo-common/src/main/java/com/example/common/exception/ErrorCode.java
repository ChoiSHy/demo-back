package com.example.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // Common
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C002", "Internal server error"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C003", "Entity not found"),
    INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST, "C004", "Invalid type value"),
    HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "C005", "Access is denied"),

    // Auth
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "A001", "Invalid credentials"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A002", "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Expired token"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A004", "Unauthorized"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "A005", "Email already exists");

    private final HttpStatus status;
    private final String code;
    private final String message;
}

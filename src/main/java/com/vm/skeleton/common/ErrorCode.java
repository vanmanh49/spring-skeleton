package com.vm.skeleton.common;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Application error codes. Each code's message lives in {@code messages.properties} under {@link #getMessageKey()}
 * and is returned as the {@code detail} of a ProblemDetail, with the code in its {@value #PROPERTY} property.
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_ERROR("ERR_01", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_CREDENTIALS("ERR_02", HttpStatus.UNAUTHORIZED),
    MALFORMED_REQUEST("ERR_03", HttpStatus.BAD_REQUEST),
    AUTHENTICATION_REQUIRED("ERR_04", HttpStatus.UNAUTHORIZED),
    VALIDATION_ERROR("ERR_05", HttpStatus.BAD_REQUEST),
    ACCESS_DENIED("ERR_06", HttpStatus.FORBIDDEN);

    /** ProblemDetail property carrying the error code. */
    public static final String PROPERTY = "errorCode";

    private final String code;
    private final HttpStatus status;

    public String getMessageKey() {
        return "error." + code;
    }
}

package com.vm.skeleton.handler;

import org.springframework.http.HttpStatus;

import com.vm.skeleton.common.ErrorCode;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus statusCode;
    private final ErrorCode errorCode;

    public BusinessException(HttpStatus statusCode, ErrorCode errorCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }
}

package com.vm.skeleton.handler;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus statusCode;
    private final String code;

    public BusinessException(HttpStatus statusCode, String code, String message) {
        super(message);
        this.statusCode = statusCode;
        this.code = code;
    }
}

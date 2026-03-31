package com.vm.skeleton.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    INTERNAL_ERROR("ERR_01"),
    INVALID_CREDENTIALS("ERR_02"),
    MALFORMED_REQUEST("ERR_03"),
    AUTHENTICATION_FAILURE("ERR_04"),
    VALIDATION_ERROR("ERR_05"),
    ACCESS_DENIED("ERR_06");

    private final String code;
}

package com.vm.skeleton.handler;

import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;

import com.vm.skeleton.common.ErrorCode;

import lombok.Getter;

/**
 * Domain error rendered as a ProblemDetail. The detail message is resolved from {@code messages.properties} using the
 * error code's message key and the given arguments.
 */
@Getter
public class BusinessException extends ErrorResponseException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, Object... messageArgs) {
        super(errorCode.getStatus(), ProblemDetail.forStatus(errorCode.getStatus()), null,
                errorCode.getMessageKey(), messageArgs);
        this.errorCode = errorCode;
        getBody().setProperty(ErrorCode.PROPERTY, errorCode.getCode());
    }
}

package com.vm.skeleton.dto;

/**
 * Envelope for successful responses. Errors are returned as RFC 9457 {@code ProblemDetail}.
 */
public record ApiResponse<T>(T data, boolean success) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(data, true);
    }
}

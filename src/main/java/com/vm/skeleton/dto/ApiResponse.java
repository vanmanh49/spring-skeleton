package com.vm.skeleton.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.vm.skeleton.common.ErrorCode;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private T data;

    @Builder.Default
    private boolean success = true;

    private String errorCode;

    private String message;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder().data(data).success(true).build();
    }

    public static <T> ApiResponse<T> error(ErrorCode errorCode, String message) {
        return ApiResponse.<T>builder().success(false).errorCode(errorCode.getCode()).message(message).build();
    }
}

package com.vm.skeleton.handler;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class BusinessException extends Exception {
    private static final long serialVersionUID = -5665737953667090090L;

    private HttpStatus statusCode;

    private String code;

    private String message;
}

package com.vm.skeleton.dto;

import javax.validation.constraints.NotEmpty;

import lombok.Data;

@Data
public class JwtRequestDto {

    @NotEmpty(message = "userName must not be empty")
    private String userName;

    @NotEmpty(message = "password must not be empty")
    private String password;
}

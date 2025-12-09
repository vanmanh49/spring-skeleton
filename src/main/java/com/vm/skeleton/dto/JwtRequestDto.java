package com.vm.skeleton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class JwtRequestDto {

    @NotBlank(message = "userName must not be blank")
    @Size(min = 3, max = 50, message = "userName must be between 3 and 50 characters")
    private String userName;

    @NotBlank(message = "password must not be blank")
    @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
    private String password;
}

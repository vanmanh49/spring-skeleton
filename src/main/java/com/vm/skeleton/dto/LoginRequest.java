package com.vm.skeleton.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "userName must not be blank")
        @Size(min = 3, max = 50, message = "userName must be between 3 and 50 characters")
        String userName,

        @NotBlank(message = "password must not be blank")
        @Size(min = 6, max = 100, message = "password must be between 6 and 100 characters")
        String password) {

    @Override
    public String toString() {
        return "LoginRequest[userName=" + userName + ", password=******]";
    }
}

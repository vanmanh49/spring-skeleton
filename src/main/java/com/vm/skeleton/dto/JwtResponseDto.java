package com.vm.skeleton.dto;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponseDto {
//    private String refreshToken;
    private String userName;

    private Set<String> roles;

    private String jwt;
}

package com.vm.skeleton.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vm.skeleton.dto.ApiResponse;
import com.vm.skeleton.dto.JwtRequestDto;
import com.vm.skeleton.dto.JwtResponseDto;
import com.vm.skeleton.service.AuthenticationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/authenticate")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping
    public ApiResponse<JwtResponseDto> authenticate(@Valid @RequestBody JwtRequestDto jwtRequestDto) {
        JwtResponseDto jwtResponseDto = authenticationService.authenticate(jwtRequestDto);
        return ApiResponse.ok(jwtResponseDto);
    }
}
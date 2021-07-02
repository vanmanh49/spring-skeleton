package com.vm.skeleton.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vm.skeleton.dto.ApiResponse;
import com.vm.skeleton.dto.JwtRequestDto;
import com.vm.skeleton.dto.JwtResponseDto;
import com.vm.skeleton.handler.BusinessException;
import com.vm.skeleton.service.AuthenticationService;

@RestController
@RequestMapping("/authenticate")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping
    public ApiResponse<JwtResponseDto> authenicate(@Valid @RequestBody
    JwtRequestDto jwtRequestDto) throws BusinessException {
        JwtResponseDto jwtResponseDto = authenticationService.authenticate(jwtRequestDto);
        return ApiResponse.<JwtResponseDto> builder().data(jwtResponseDto).build();
    }
}
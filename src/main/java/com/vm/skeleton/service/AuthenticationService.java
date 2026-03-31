package com.vm.skeleton.service;

import com.vm.skeleton.dto.JwtRequestDto;
import com.vm.skeleton.dto.JwtResponseDto;

public interface AuthenticationService {

    JwtResponseDto authenticate(JwtRequestDto jwtRequestDto);
}

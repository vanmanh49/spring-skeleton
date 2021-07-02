package com.vm.skeleton.service;

import com.vm.skeleton.dto.JwtRequestDto;
import com.vm.skeleton.dto.JwtResponseDto;
import com.vm.skeleton.handler.BusinessException;

public interface AuthenticationService {

    JwtResponseDto authenticate(JwtRequestDto jwtRequestDto) throws BusinessException;
}

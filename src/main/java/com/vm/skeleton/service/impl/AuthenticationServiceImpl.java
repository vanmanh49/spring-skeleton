package com.vm.skeleton.service.impl;

import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.vm.skeleton.common.ErrorCode;
import com.vm.skeleton.common.JwtUtil;
import com.vm.skeleton.common.MessagePropertySourceUtil;
import com.vm.skeleton.dto.JwtRequestDto;
import com.vm.skeleton.dto.JwtResponseDto;
import com.vm.skeleton.handler.BusinessException;
import com.vm.skeleton.service.AuthenticationService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtUtil jwtUtil;

    private final MessagePropertySourceUtil messageSourceUtil;

    @Override
    public JwtResponseDto authenticate(JwtRequestDto jwtRequestDto) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(jwtRequestDto.getUserName(), jwtRequestDto.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            Set<String> roles = userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            String jwt = jwtUtil.generateToken(userDetails);
            log.info("User '{}' authenticated successfully", userDetails.getUsername());
            return new JwtResponseDto(userDetails.getUsername(), roles, jwt);
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user '{}'", jwtRequestDto.getUserName());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during authentication for user '{}'", jwtRequestDto.getUserName(), e);
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.AUTHENTICATION_FAILURE,
                    messageSourceUtil.getMessage(ErrorCode.AUTHENTICATION_FAILURE.getCode(), null));
        }
    }

}

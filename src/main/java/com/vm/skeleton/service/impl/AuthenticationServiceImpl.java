package com.vm.skeleton.service.impl;

import java.util.Objects;
import java.util.Set;

import org.springframework.resilience.annotation.ConcurrencyLimit;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.vm.skeleton.dto.LoginRequest;
import com.vm.skeleton.dto.TokenResponse;
import com.vm.skeleton.service.AuthenticationService;
import com.vm.skeleton.service.JwtTokenService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;

    private final JwtTokenService jwtTokenService;

    @Override
    @ConcurrencyLimit(20)
    public TokenResponse authenticate(LoginRequest loginRequest) {
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(loginRequest.userName(), loginRequest.password()));
        } catch (AuthenticationException e) {
            log.warn("Authentication failed for user '{}'", loginRequest.userName());
            throw e;
        }

        // Roles come from the user record: Spring Security 7 also adds factor authorities (e.g. FACTOR_PASSWORD)
        // to the Authentication, which must not end up in the token
        UserDetails user = (UserDetails) Objects.requireNonNull(authentication.getPrincipal());
        Set<String> roles = AuthorityUtils.authorityListToSet(user.getAuthorities());
        Jwt jwt = jwtTokenService.issueToken(authentication.getName(), roles);
        log.info("User '{}' authenticated successfully", authentication.getName());
        return new TokenResponse(authentication.getName(), roles, jwt.getTokenValue(), jwt.getExpiresAt());
    }
}

package com.vm.skeleton.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;

import com.vm.skeleton.dto.LoginRequest;
import com.vm.skeleton.dto.TokenResponse;
import com.vm.skeleton.service.JwtTokenService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenService jwtTokenService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void authenticate_withValidCredentials_returnsTokenResponse() {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(3600);
        Jwt jwt = new Jwt("jwt-token-123", issuedAt, expiresAt, Map.of("alg", "HS512"), Map.of("sub", "testuser"));
        UserDetails user = User.withUsername("testuser").password("hashed").authorities("EDITOR").build();
        // Spring Security 7 adds factor authorities to the Authentication; they must not become roles
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(UsernamePasswordAuthenticationToken.authenticated(user, null,
                        AuthorityUtils.createAuthorityList("EDITOR", "FACTOR_PASSWORD")));
        when(jwtTokenService.issueToken(eq("testuser"), anyCollection())).thenReturn(jwt);

        TokenResponse response = authenticationService.authenticate(new LoginRequest("testuser", "testpassword"));

        assertEquals(new TokenResponse("testuser", Set.of("EDITOR"), "jwt-token-123", expiresAt), response);
    }

    @Test
    void authenticate_withInvalidCredentials_propagatesAuthenticationException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest("baduser", "badpassword");
        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));
        verifyNoInteractions(jwtTokenService);
    }
}

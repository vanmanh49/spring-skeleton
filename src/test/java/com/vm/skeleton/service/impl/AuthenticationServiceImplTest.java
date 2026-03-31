package com.vm.skeleton.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.vm.skeleton.common.JwtUtil;
import com.vm.skeleton.common.MessagePropertySourceUtil;
import com.vm.skeleton.dto.JwtRequestDto;
import com.vm.skeleton.dto.JwtResponseDto;
import com.vm.skeleton.handler.BusinessException;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private MessagePropertySourceUtil messageSourceUtil;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    @Test
    void authenticate_withValidCredentials_shouldReturnJwtResponse() {
        JwtRequestDto request = new JwtRequestDto();
        request.setUserName("testuser");
        request.setPassword("testpassword");

        UserDetails userDetails = new User("testuser", "hashed",
                Set.of(new SimpleGrantedAuthority("EDITOR")));
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtil.generateToken(userDetails)).thenReturn("jwt-token-123");

        JwtResponseDto response = authenticationService.authenticate(request);

        assertEquals("testuser", response.getUserName());
        assertEquals(Set.of("EDITOR"), response.getRoles());
        assertEquals("jwt-token-123", response.getJwt());
    }

    @Test
    void authenticate_withInvalidCredentials_shouldThrowAuthenticationException() {
        JwtRequestDto request = new JwtRequestDto();
        request.setUserName("baduser");
        request.setPassword("badpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(BadCredentialsException.class, () -> authenticationService.authenticate(request));
    }

    @Test
    void authenticate_withUnexpectedError_shouldThrowBusinessException() {
        JwtRequestDto request = new JwtRequestDto();
        request.setUserName("testuser");
        request.setPassword("testpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Unexpected"));
        when(messageSourceUtil.getMessage(anyString(), any())).thenReturn("Error occurred");

        assertThrows(BusinessException.class, () -> authenticationService.authenticate(request));
    }
}

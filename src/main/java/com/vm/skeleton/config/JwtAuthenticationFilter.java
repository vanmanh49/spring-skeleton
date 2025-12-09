package com.vm.skeleton.config;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.vm.skeleton.common.JwtUtil;
import com.vm.skeleton.common.SecurityConstants;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (!StringUtils.isEmpty(authorizationHeader)
                && StringUtils.startsWith(authorizationHeader, SecurityConstants.BEARER_PREFIX)) {
            try {
                String jwt = StringUtils.substring(authorizationHeader, SecurityConstants.BEARER_PREFIX.length()).trim();
                String usernameFromToken = jwtUtil.getUsernameFromToken(jwt);
                if (SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(usernameFromToken);
                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities()));
                    }
                }
            } catch (Exception e) {
                log.error("Failed to verify JWT", e);
            }
        }

        filterChain.doFilter(request, response);
    }
}


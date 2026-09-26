package com.vm.skeleton.service.impl;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsPasswordService;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vm.skeleton.entity.Role;
import com.vm.skeleton.entity.User;
import com.vm.skeleton.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService, UserDetailsPasswordService {

    private final UserRepository userRepository;

    /**
     * Retries only transient database failures; an unknown user fails immediately.
     */
    @Override
    @Transactional(readOnly = true)
    @Retryable(includes = TransientDataAccessException.class, maxRetries = 2, delay = 500)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return toUserDetails(user);
    }

    /**
     * Called by Spring Security after a successful login when the stored hash uses an outdated encoding (e.g. a
     * BCrypt hash without the {@code {bcrypt}} prefix), so hashes migrate to the current encoder over time.
     */
    @Override
    @Transactional
    public UserDetails updatePassword(UserDetails userDetails, String newPassword) {
        User user = userRepository.findByUserName(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userDetails.getUsername()));
        user.changePassword(newPassword);
        return toUserDetails(user);
    }

    private static UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User.withUsername(user.getUserName())
                .password(user.getHashedPassword())
                .authorities(user.getRoles().stream().map(Role::getCode).toArray(String[]::new))
                .build();
    }
}

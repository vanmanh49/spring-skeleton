package com.vm.skeleton.service.impl;

import org.springframework.dao.TransientDataAccessException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.vm.skeleton.entity.Role;
import com.vm.skeleton.entity.User;
import com.vm.skeleton.repository.UserDetailRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserDetailRepository userDetailRepository;

    /**
     * Retries only transient database failures; an unknown user fails immediately.
     */
    @Override
    @Transactional(readOnly = true)
    @Retryable(includes = TransientDataAccessException.class, maxRetries = 2, delay = 500)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userDetailRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        String[] roles = user.getRoles() == null ? new String[0]
                : user.getRoles().stream().map(Role::getRoleCode).toArray(String[]::new);

        return org.springframework.security.core.userdetails.User.withUsername(user.getUserName())
                .password(user.getHashedPassword())
                .authorities(roles)
                .build();
    }
}

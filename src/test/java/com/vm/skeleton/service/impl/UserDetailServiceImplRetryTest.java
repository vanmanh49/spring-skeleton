package com.vm.skeleton.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.vm.skeleton.entity.Role;
import com.vm.skeleton.entity.User;
import com.vm.skeleton.repository.UserDetailRepository;

/**
 * Verifies {@code @Retryable} on the Spring proxy: transient DB errors are retried, unknown users are not.
 * Class-based proxies mirror the application (Spring Boot's {@code spring.aop.proxy-target-class=true}); with an
 * interface proxy the annotation on the implementation method would not be found.
 */
@SpringJUnitConfig(UserDetailServiceImplRetryTest.Config.class)
class UserDetailServiceImplRetryTest {

    @Configuration(proxyBeanMethods = false)
    @EnableResilientMethods(proxyTargetClass = true)
    @Import(UserDetailServiceImpl.class)
    static class Config {
    }

    @MockitoBean
    private UserDetailRepository userDetailRepository;

    @Autowired
    private UserDetailsService userDetailsService;

    @Test
    void unknownUser_isNotRetried() {
        when(userDetailRepository.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> userDetailsService.loadUserByUsername("unknown"));
        verify(userDetailRepository, times(1)).findByUserName("unknown");
    }

    @Test
    void transientDatabaseError_isRetried() {
        User user = new User();
        user.setUserName("alice");
        user.setHashedPassword("hashed");
        Role role = new Role();
        role.setRoleCode("EDITOR");
        user.setRoles(List.of(role));
        when(userDetailRepository.findByUserName("alice"))
                .thenThrow(new QueryTimeoutException("timeout"))
                .thenReturn(Optional.of(user));

        assertEquals("alice", userDetailsService.loadUserByUsername("alice").getUsername());
        verify(userDetailRepository, times(2)).findByUserName("alice");
    }
}

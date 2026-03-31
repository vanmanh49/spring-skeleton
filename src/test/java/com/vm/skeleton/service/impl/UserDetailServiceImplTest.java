package com.vm.skeleton.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.vm.skeleton.entity.Role;
import com.vm.skeleton.entity.User;
import com.vm.skeleton.repository.UserDetailRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest {

    @Mock
    private UserDetailRepository userDetailRepository;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @Test
    void loadUserByUsername_withExistingUser_shouldReturnUserDetails() {
        User user = new User();
        user.setUserName("testuser");
        user.setHashedPassword("hashedpwd");

        Role role = new Role();
        role.setRoleCode("EDITOR");
        role.setUser(user);
        user.setRoles(List.of(role));

        when(userDetailRepository.findByUserName("testuser")).thenReturn(Optional.of(user));

        UserDetails result = userDetailService.loadUserByUsername("testuser");

        assertEquals("testuser", result.getUsername());
        assertEquals("hashedpwd", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("EDITOR")));
    }

    @Test
    void loadUserByUsername_withNonExistingUser_shouldThrowException() {
        when(userDetailRepository.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailService.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_withNoRoles_shouldReturnEmptyAuthorities() {
        User user = new User();
        user.setUserName("noroleuser");
        user.setHashedPassword("hashedpwd");
        user.setRoles(null);

        when(userDetailRepository.findByUserName("noroleuser")).thenReturn(Optional.of(user));

        UserDetails result = userDetailService.loadUserByUsername("noroleuser");

        assertEquals("noroleuser", result.getUsername());
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void loadUserByUsername_withEmptyRoles_shouldReturnEmptyAuthorities() {
        User user = new User();
        user.setUserName("emptyroles");
        user.setHashedPassword("hashedpwd");
        user.setRoles(List.of());

        when(userDetailRepository.findByUserName("emptyroles")).thenReturn(Optional.of(user));

        UserDetails result = userDetailService.loadUserByUsername("emptyroles");

        assertTrue(result.getAuthorities().isEmpty());
    }
}

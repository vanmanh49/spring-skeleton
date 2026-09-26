package com.vm.skeleton.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.vm.skeleton.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @Test
    void loadUserByUsername_withExistingUser_shouldReturnUserDetails() {
        User user = new User("testuser", "hashedpwd").addRole(new Role("EDITOR"));

        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));

        UserDetails result = userDetailService.loadUserByUsername("testuser");

        assertEquals("testuser", result.getUsername());
        assertEquals("hashedpwd", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
        assertTrue(result.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("EDITOR")));
    }

    @Test
    void loadUserByUsername_withNonExistingUser_shouldThrowException() {
        when(userRepository.findByUserName("unknown")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailService.loadUserByUsername("unknown"));
    }

    @Test
    void loadUserByUsername_withNoRoles_shouldReturnEmptyAuthorities() {
        User user = new User("noroleuser", "hashedpwd");

        when(userRepository.findByUserName("noroleuser")).thenReturn(Optional.of(user));

        UserDetails result = userDetailService.loadUserByUsername("noroleuser");

        assertEquals("noroleuser", result.getUsername());
        assertTrue(result.getAuthorities().isEmpty());
    }

    @Test
    void updatePassword_storesNewHashAndReturnsUpdatedUserDetails() {
        User user = new User("testuser", "oldhash").addRole(new Role("EDITOR"));
        when(userRepository.findByUserName("testuser")).thenReturn(Optional.of(user));

        UserDetails result = userDetailService.updatePassword(
                userDetailService.loadUserByUsername("testuser"), "{bcrypt}newhash");

        assertEquals("{bcrypt}newhash", user.getHashedPassword());
        assertEquals("{bcrypt}newhash", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
    }
}

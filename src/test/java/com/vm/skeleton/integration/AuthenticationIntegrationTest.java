package com.vm.skeleton.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vm.skeleton.dto.ApiResponse;
import com.vm.skeleton.dto.JwtRequestDto;
import com.vm.skeleton.dto.JwtResponseDto;
import com.vm.skeleton.entity.Role;
import com.vm.skeleton.entity.User;
import com.vm.skeleton.repository.UserDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private UserDetailRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Clear and initialize test data
        userRepository.deleteAll();
        
        // Create test user
        User testUser = new User();
        testUser.setUserName("testuser");
        testUser.setHashedPassword(passwordEncoder.encode("testpassword"));
        
        Role editorRole = new Role();
        editorRole.setRoleCode("EDITOR");
        editorRole.setUser(testUser);
        testUser.setRoles(List.of(editorRole));
        
        userRepository.save(testUser);

        // Create admin user
        User adminUser = new User();
        adminUser.setUserName("adminuser");
        adminUser.setHashedPassword(passwordEncoder.encode("testpassword"));
        
        Role adminRole = new Role();
        adminRole.setRoleCode("ADMINISTRATOR");
        adminRole.setUser(adminUser);
        adminUser.setRoles(List.of(adminRole));
        
        userRepository.save(adminUser);
    }

    @Test
    void testAuthenticateWithValidCredentials() throws Exception {
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("testuser");
        requestDto.setPassword("testpassword");

        MvcResult result = mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        ApiResponse<JwtResponseDto> response = objectMapper.readValue(responseBody,
                objectMapper.getTypeFactory().constructParametricType(ApiResponse.class, JwtResponseDto.class));

        assertNotNull(response.getData());
        assertNotNull(response.getData().getJwt());
        assertNotNull(response.getData().getUserName());
    }

    @Test
    void testAuthenticateWithInvalidCredentials() throws Exception {
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("invaliduser");
        requestDto.setPassword("invalidpassword");

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAuthenticateWithEmptyUsername() throws Exception {
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("");
        requestDto.setPassword("testpassword");

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testAuthenticateWithShortPassword() throws Exception {
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("testuser");
        requestDto.setPassword("short");

        mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest());
    }
}


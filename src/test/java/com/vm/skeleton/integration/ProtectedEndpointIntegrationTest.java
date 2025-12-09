package com.vm.skeleton.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProtectedEndpointIntegrationTest {

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
    void testProtectedEndpointWithoutToken() throws Exception {
        mockMvc.perform(get("/test/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithInvalidToken() throws Exception {
        mockMvc.perform(get("/test/admin")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpointWithValidToken() throws Exception {
        // First authenticate to get a token
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("adminuser");
        requestDto.setPassword("testpassword");

        String authResponse = mockMvc.perform(post("/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JwtResponseDto jwtResponse = objectMapper.readValue(
                objectMapper.readTree(authResponse).get("data").toString(),
                JwtResponseDto.class);

        String token = jwtResponse.getJwt();

        // Use token to access protected endpoint
        mockMvc.perform(get("/test/admin")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testSwaggerEndpointsArePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }
}


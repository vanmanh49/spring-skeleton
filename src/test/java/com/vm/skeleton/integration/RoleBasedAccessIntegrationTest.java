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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class RoleBasedAccessIntegrationTest {

    @Autowired
    private UserDetailRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String adminToken;
    private String editorToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAll();

        // Create admin user
        User adminUser = new User();
        adminUser.setUserName("adminuser");
        adminUser.setHashedPassword(passwordEncoder.encode("testpassword"));
        Role adminRole = new Role();
        adminRole.setRoleCode("ADMINISTRATOR");
        adminRole.setUser(adminUser);
        adminUser.setRoles(List.of(adminRole));
        userRepository.save(adminUser);

        // Create editor user
        User editorUser = new User();
        editorUser.setUserName("editoruser");
        editorUser.setHashedPassword(passwordEncoder.encode("testpassword"));
        Role editorRole = new Role();
        editorRole.setRoleCode("EDITOR");
        editorRole.setUser(editorUser);
        editorUser.setRoles(List.of(editorRole));
        userRepository.save(editorUser);

        adminToken = obtainToken("adminuser", "testpassword");
        editorToken = obtainToken("editoruser", "testpassword");
    }

    private String obtainToken(String username, String password) throws Exception {
        JwtRequestDto request = new JwtRequestDto();
        request.setUserName(username);
        request.setPassword(password);

        String response = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JwtResponseDto jwt = objectMapper.readValue(
                objectMapper.readTree(response).get("data").toString(), JwtResponseDto.class);
        return jwt.getJwt();
    }

    @Test
    void adminEndpoint_withAdminToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_withEditorToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + editorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void editorEndpoint_withEditorToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/test/editor")
                        .header("Authorization", "Bearer " + editorToken))
                .andExpect(status().isOk());
    }

    @Test
    void editorEndpoint_withAdminToken_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer " + editorToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserEndpoint_withAdminToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/test/authenticated-user")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    void authenticatedUserEndpoint_withEditorToken_shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/v1/test/authenticated-user")
                        .header("Authorization", "Bearer " + editorToken))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpoint_withoutToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpoint_withMalformedToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/test/admin")
                        .header("Authorization", "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void loginResponse_shouldContainExpectedStructure() throws Exception {
        JwtRequestDto request = new JwtRequestDto();
        request.setUserName("adminuser");
        request.setPassword("testpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.jwt").isNotEmpty())
                .andExpect(jsonPath("$.data.userName").value("adminuser"))
                .andExpect(jsonPath("$.data.roles").isArray());
    }

    @Test
    void loginWithInvalidCredentials_shouldReturnErrorStructure() throws Exception {
        JwtRequestDto request = new JwtRequestDto();
        request.setUserName("adminuser");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }
}

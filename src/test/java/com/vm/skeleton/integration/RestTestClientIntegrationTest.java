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
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestTestClient
@ActiveProfiles("test")
@Transactional
class RestTestClientIntegrationTest {

    @Autowired
    private UserDetailRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private RestTestClient restTestClient;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setUserName("testuser");
        testUser.setHashedPassword(passwordEncoder.encode("testpassword"));

        Role editorRole = new Role();
        editorRole.setRoleCode("EDITOR");
        editorRole.setUser(testUser);
        testUser.setRoles(List.of(editorRole));

        userRepository.save(testUser);

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
    void testLoginWithRestTestClient() throws Exception {
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("testuser");
        requestDto.setPassword("testpassword");

        byte[] responseBody = restTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(requestDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();

        assertNotNull(responseBody);
        JwtResponseDto jwtResponse = objectMapper.readValue(
                objectMapper.readTree(responseBody).get("data").toString(),
                JwtResponseDto.class);
        assertNotNull(jwtResponse.getJwt());
    }

    @Test
    void testProtectedEndpointUnauthorized() {
        restTestClient.get()
                .uri("/api/test/admin")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testProtectedEndpointWithToken() throws Exception {
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("adminuser");
        requestDto.setPassword("testpassword");

        byte[] authBody = restTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(requestDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();

        JwtResponseDto jwtResponse = objectMapper.readValue(
                objectMapper.readTree(authBody).get("data").toString(),
                JwtResponseDto.class);

        restTestClient.get()
                .uri("/api/test/admin")
                .header("Authorization", "Bearer " + jwtResponse.getJwt())
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testEditorCannotAccessAdminEndpoint() throws Exception {
        JwtRequestDto requestDto = new JwtRequestDto();
        requestDto.setUserName("testuser");
        requestDto.setPassword("testpassword");

        byte[] authBody = restTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(objectMapper.writeValueAsString(requestDto))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .returnResult()
                .getResponseBody();

        JwtResponseDto jwtResponse = objectMapper.readValue(
                objectMapper.readTree(authBody).get("data").toString(),
                JwtResponseDto.class);

        restTestClient.get()
                .uri("/api/test/admin")
                .header("Authorization", "Bearer " + jwtResponse.getJwt())
                .exchange()
                .expectStatus().isForbidden();
    }
}

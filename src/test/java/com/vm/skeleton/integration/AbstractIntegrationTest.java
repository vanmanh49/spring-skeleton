package com.vm.skeleton.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.vm.skeleton.dto.LoginRequest;
import com.vm.skeleton.entity.User;
import com.vm.skeleton.repository.RoleRepository;
import com.vm.skeleton.repository.UserRepository;

import tools.jackson.databind.json.JsonMapper;

/**
 * Boots the full application against in-memory H2 (schema and roles from Flyway) and seeds one user per role.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
abstract class AbstractIntegrationTest {

    static final String PASSWORD = "testpassword";
    static final String ADMIN = "adminuser";
    static final String EDITOR = "editoruser";

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected JsonMapper jsonMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void seedUsers() {
        userRepository.deleteAll();
        userRepository.save(user(ADMIN, "ADMINISTRATOR"));
        userRepository.save(user(EDITOR, "EDITOR"));
    }

    protected String loginJson(String userName, String password) {
        return jsonMapper.writeValueAsString(new LoginRequest(userName, password));
    }

    protected String obtainToken(String userName) throws Exception {
        String response = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(userName, PASSWORD)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return jsonMapper.readTree(response).path("jwt").asString();
    }

    /** Roles are reference data seeded by the Flyway migrations. */
    private User user(String userName, String roleCode) {
        return new User(userName, passwordEncoder.encode(PASSWORD))
                .addRole(roleRepository.findByCode(roleCode).orElseThrow());
    }
}

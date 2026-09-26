package com.vm.skeleton.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.vm.skeleton.entity.User;

class AuthenticationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void login_withValidCredentials_returnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value(ADMIN))
                .andExpect(jsonPath("$.roles", containsInAnyOrder("ADMINISTRATOR")))
                .andExpect(jsonPath("$.jwt").isNotEmpty())
                .andExpect(jsonPath("$.expiresAt").isNotEmpty());
    }

    @Test
    void login_withWrongPassword_returns401Problem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN, "wrongpassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.errorCode").value("ERR_02"))
                .andExpect(jsonPath("$.detail").value("Username or password is incorrect"));
    }

    @Test
    void login_withUnknownUser_returns401Problem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("invaliduser", "invalidpassword")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("ERR_02"));
    }

    @Test
    void login_withBlankUsername_returns400ValidationProblem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("", PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("ERR_05"))
                .andExpect(jsonPath("$.errors.userName").isNotEmpty());
    }

    @Test
    void login_withShortPassword_returns400ValidationProblem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN, "short")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERR_05"))
                .andExpect(jsonPath("$.errors.password").value("password must be between 6 and 100 characters"));
    }

    @Test
    void login_withMalformedBody_returns400Problem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("ERR_03"));
    }

    @Test
    void login_withUnsupportedApiVersion_returns400Problem() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .header("API-Version", "2")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(ADMIN, PASSWORD)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("ERR_03"));
    }

    @Test
    void login_withLegacyUnprefixedBcryptHash_succeedsAndUpgradesHash() throws Exception {
        userRepository.save(new User("legacyuser", new BCryptPasswordEncoder().encode(PASSWORD)));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("legacyuser", PASSWORD)))
                .andExpect(status().isOk());

        assertThat(userRepository.findByUserName("legacyuser").orElseThrow().getHashedPassword())
                .startsWith("{bcrypt}");
    }
}

package com.vm.skeleton.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class RoleBasedAccessIntegrationTest extends AbstractIntegrationTest {

    private String adminToken;
    private String editorToken;

    @BeforeEach
    void obtainTokens() throws Exception {
        adminToken = obtainToken(ADMIN);
        editorToken = obtainToken(EDITOR);
    }

    @Test
    void adminEndpoint_withAdminToken_returns200() throws Exception {
        call("/api/test/admin", adminToken).andExpect(status().isOk());
    }

    @Test
    void adminEndpoint_withEditorToken_returns403Problem() throws Exception {
        call("/api/test/admin", editorToken)
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.errorCode").value("ERR_06"));
    }

    @Test
    void editorEndpoint_withEditorToken_returns200() throws Exception {
        call("/api/test/editor", editorToken).andExpect(status().isOk());
    }

    @Test
    void editorEndpoint_withAdminToken_returns403() throws Exception {
        call("/api/test/editor", adminToken).andExpect(status().isForbidden());
    }

    @Test
    void authenticatedUserEndpoint_withAdminToken_returns200() throws Exception {
        call("/api/test/authenticated-user", adminToken).andExpect(status().isOk());
    }

    @Test
    void authenticatedUserEndpoint_withEditorToken_returns200() throws Exception {
        call("/api/test/authenticated-user", editorToken).andExpect(status().isOk());
    }

    private ResultActions call(String path, String token) throws Exception {
        return mockMvc.perform(get(path).header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
    }
}

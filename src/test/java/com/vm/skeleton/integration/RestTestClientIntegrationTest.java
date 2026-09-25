package com.vm.skeleton.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureRestTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.client.RestTestClient;

import com.vm.skeleton.dto.LoginRequest;

@AutoConfigureRestTestClient
class RestTestClientIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private RestTestClient restTestClient;

    @Test
    void login_returnsToken() {
        restTestClient.post()
                .uri("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new LoginRequest(ADMIN, PASSWORD))
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.jwt").isNotEmpty()
                .jsonPath("$.data.userName").isEqualTo(ADMIN);
    }

    @Test
    void protectedEndpoint_withoutToken_returns401() {
        restTestClient.get()
                .uri("/api/test/admin")
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("ERR_04");
    }

    @Test
    void protectedEndpoint_withAdminToken_returns200() throws Exception {
        restTestClient.get()
                .uri("/api/test/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + obtainToken(ADMIN))
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class).isEqualTo("Hello admin");
    }

    @Test
    void protectedEndpoint_withEditorToken_returns403() throws Exception {
        restTestClient.get()
                .uri("/api/test/admin")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + obtainToken(EDITOR))
                .exchange()
                .expectStatus().isForbidden();
    }
}

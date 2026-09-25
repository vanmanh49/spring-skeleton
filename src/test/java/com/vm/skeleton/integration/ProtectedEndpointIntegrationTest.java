package com.vm.skeleton.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.vm.skeleton.common.SecurityConstants;
import com.vm.skeleton.config.JwtProperties;

class ProtectedEndpointIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private JwtEncoder jwtEncoder;

    @Autowired
    private JwtProperties jwtProperties;

    @Test
    void withoutToken_returns401Problem() throws Exception {
        mockMvc.perform(get("/api/test/admin"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.errorCode").value("ERR_04"));
    }

    @Test
    void withMalformedToken_returns401InvalidToken() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer not.a.valid.jwt"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.WWW_AUTHENTICATE, "Bearer error=\"invalid_token\""))
                .andExpect(jsonPath("$.errorCode").value("ERR_04"));
    }

    @Test
    void withExpiredToken_returns401() throws Exception {
        Instant issuedAt = Instant.now().minusSeconds(7200);
        String token = encode(jwtEncoder, jwtProperties.issuer(), issuedAt, issuedAt.plusSeconds(3600));

        mockMvc.perform(get("/api/test/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withTokenFromOtherIssuer_returns401() throws Exception {
        String token = encode(jwtEncoder, "someone-else", Instant.now(), Instant.now().plusSeconds(600));

        mockMvc.perform(get("/api/test/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withTokenSignedByOtherKey_returns401() throws Exception {
        JwtEncoder foreignEncoder = NimbusJwtEncoder
                .withSecretKey(new SecretKeySpec(
                        "another-secret-key-that-is-also-at-least-sixty-four-bytes-long-for-hs512".getBytes(
                                StandardCharsets.UTF_8),
                        "HmacSHA512"))
                .algorithm(MacAlgorithm.HS512)
                .build();
        String token = encode(foreignEncoder, jwtProperties.issuer(), Instant.now(), Instant.now().plusSeconds(600));

        mockMvc.perform(get("/api/test/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withValidToken_returns200() throws Exception {
        mockMvc.perform(get("/api/test/admin")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + obtainToken(ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void swaggerEndpoints_arePublic() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    void healthEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    private static String encode(JwtEncoder encoder, String issuer, Instant issuedAt, Instant expiresAt) {
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .subject(ADMIN)
                .issuedAt(issuedAt)
                .expiresAt(expiresAt)
                .claim(SecurityConstants.ROLES_CLAIM, List.of("ADMINISTRATOR"))
                .build();
        return encoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS512).build(), claims))
                .getTokenValue();
    }
}

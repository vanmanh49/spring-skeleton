package com.vm.skeleton.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import com.vm.skeleton.common.SecurityConstants;
import com.vm.skeleton.config.JwtConfig;
import com.vm.skeleton.config.JwtProperties;

class JwtTokenServiceTest {

    private static final JwtProperties PROPERTIES = new JwtProperties(
            "testSecretKeyForTestingPurposesOnlyMustBeAtLeast64CharactersLongForHS512Algorithm",
            Duration.ofMinutes(30), "test-issuer");

    private final JwtConfig jwtConfig = new JwtConfig();

    private final JwtTokenService tokenService = new JwtTokenService(jwtConfig.jwtEncoder(PROPERTIES), PROPERTIES);

    private final JwtDecoder decoder = jwtConfig.jwtDecoder(PROPERTIES);

    @Test
    void issueToken_roundTripsThroughDecoder() {
        Jwt issued = tokenService.issueToken("alice", List.of("EDITOR", "ADMINISTRATOR"));

        Jwt decoded = decoder.decode(issued.getTokenValue());

        assertEquals("alice", decoded.getSubject());
        assertEquals("test-issuer", decoded.getClaimAsString("iss"));
        assertEquals("HS512", decoded.getHeaders().get("alg"));
        assertEquals(List.of("EDITOR", "ADMINISTRATOR"), decoded.getClaimAsStringList(SecurityConstants.ROLES_CLAIM));
        assertEquals(Duration.ofMinutes(30), Duration.between(decoded.getIssuedAt(), decoded.getExpiresAt()));
        assertTrue(decoded.getExpiresAt().isAfter(Instant.now()));
    }

    @Test
    void decode_rejectsTamperedSignature() {
        String token = tokenService.issueToken("alice", List.of("EDITOR")).getTokenValue();
        // Flip a character in the middle of the signature (the last base64url char may only hold padding bits)
        int index = token.lastIndexOf('.') + (token.length() - token.lastIndexOf('.')) / 2;
        char replacement = token.charAt(index) == 'A' ? 'B' : 'A';
        String tampered = token.substring(0, index) + replacement + token.substring(index + 1);

        assertThrows(JwtException.class, () -> decoder.decode(tampered));
    }

    @Test
    void decode_rejectsTokenFromAnotherIssuer() {
        JwtProperties otherIssuer = new JwtProperties(PROPERTIES.secretKey(), PROPERTIES.validity(), "other");
        String token = new JwtTokenService(jwtConfig.jwtEncoder(otherIssuer), otherIssuer)
                .issueToken("alice", List.of("EDITOR"))
                .getTokenValue();

        assertThrows(JwtException.class, () -> decoder.decode(token));
    }

    @Test
    void secretKeyShorterThan64Bytes_isRejected() {
        assertTrue(PROPERTIES.isSecretKeyLongEnough());
        assertTrue(!new JwtProperties("too-short", Duration.ofMinutes(1), "x").isSecretKeyLongEnough());
    }
}

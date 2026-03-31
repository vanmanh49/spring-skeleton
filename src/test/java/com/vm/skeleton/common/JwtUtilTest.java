package com.vm.skeleton.common;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private static final String SECRET = "testSecretKeyForTestingPurposesOnlyMustBeAtLeast64CharactersLongForHS512Algorithm";
    private static final long VALIDITY = 3600000L; // 1 hour

    @BeforeEach
    void setUp() throws Exception {
        jwtUtil = new JwtUtil();
        setField(jwtUtil, "secretKey", SECRET);
        setField(jwtUtil, "jwtValidity", VALIDITY);
        jwtUtil.validateSecretKey();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private UserDetails createUserDetails(String username, String... roles) {
        var authorities = java.util.Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .toList();
        return new User(username, "password", authorities);
    }

    @Test
    void generateToken_shouldReturnNonNullToken() {
        UserDetails userDetails = createUserDetails("testuser", "ADMINISTRATOR");
        String token = jwtUtil.generateToken(userDetails);
        assertNotNull(token);
        assertFalse(token.isBlank());
    }

    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {
        UserDetails userDetails = createUserDetails("testuser", "EDITOR");
        String token = jwtUtil.generateToken(userDetails);

        String username = jwtUtil.getUsernameFromToken(token);
        assertEquals("testuser", username);
    }

    @Test
    void getExpirationDateFromToken_shouldReturnFutureDate() {
        UserDetails userDetails = createUserDetails("testuser", "EDITOR");
        String token = jwtUtil.generateToken(userDetails);

        Date expiration = jwtUtil.getExpirationDateFromToken(token);
        assertTrue(expiration.after(new Date()));
    }

    @Test
    void getRolesFromToken_shouldReturnRoles() {
        UserDetails userDetails = createUserDetails("testuser", "ADMINISTRATOR", "EDITOR");
        String token = jwtUtil.generateToken(userDetails);

        Collection<?> roles = jwtUtil.getRolesFromToken(token);
        assertNotNull(roles);
        assertFalse(roles.isEmpty());
    }

    @Test
    void validateToken_shouldReturnTrueForValidToken() {
        UserDetails userDetails = createUserDetails("testuser", "EDITOR");
        String token = jwtUtil.generateToken(userDetails);

        assertTrue(jwtUtil.validateToken(token, userDetails));
    }

    @Test
    void validateToken_shouldReturnFalseForWrongUser() {
        UserDetails userDetails = createUserDetails("testuser", "EDITOR");
        UserDetails otherUser = createUserDetails("otheruser", "EDITOR");
        String token = jwtUtil.generateToken(userDetails);

        assertFalse(jwtUtil.validateToken(token, otherUser));
    }

    @Test
    void validateToken_shouldThrowForTamperedToken() {
        UserDetails userDetails = createUserDetails("testuser", "EDITOR");
        String token = jwtUtil.generateToken(userDetails);
        String tampered = token + "xyz";

        assertThrows(Exception.class, () -> jwtUtil.validateToken(tampered, userDetails));
    }

    @Test
    void validateSecretKey_shouldThrowForBlankKey() throws Exception {
        JwtUtil util = new JwtUtil();
        setField(util, "secretKey", "");
        setField(util, "jwtValidity", VALIDITY);

        assertThrows(IllegalStateException.class, util::validateSecretKey);
    }

    @Test
    void generateToken_expiredToken_shouldThrowOnValidation() throws Exception {
        JwtUtil shortLivedUtil = new JwtUtil();
        setField(shortLivedUtil, "secretKey", SECRET);
        setField(shortLivedUtil, "jwtValidity", -1000L); // already expired
        shortLivedUtil.validateSecretKey();

        UserDetails userDetails = createUserDetails("testuser", "EDITOR");
        String token = shortLivedUtil.generateToken(userDetails);

        // Parsing an expired token throws ExpiredJwtException
        assertThrows(io.jsonwebtoken.ExpiredJwtException.class,
                () -> shortLivedUtil.validateToken(token, userDetails));
    }
}

package com.vm.skeleton.common;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.validity}")
    private long jwtValidity;

    public static final String ROLE_CLAIM_KEY = "role";
    private static final int MIN_SECRET_KEY_LENGTH = 64;

    @PostConstruct
    public void validateSecretKey() {
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalStateException("JWT secret key must not be blank");
        }
        if (secretKey.length() < MIN_SECRET_KEY_LENGTH) {
            log.warn("JWT secret key length ({}) is less than recommended minimum ({}). " +
                    "Consider using a longer key for better security.", secretKey.length(), MIN_SECRET_KEY_LENGTH);
        }
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .subject(userDetails.getUsername())
                .claim(ROLE_CLAIM_KEY, userDetails.getAuthorities())
                .issuedAt(new Date())
                .issuer(userDetails.getUsername())
                .expiration(new Date(System.currentTimeMillis() + jwtValidity))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    public Collection<? extends GrantedAuthority> getRolesFromToken(String token) {
        return getClaimFromToken(token, claim -> claim.get(ROLE_CLAIM_KEY, Collection.class));
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
}

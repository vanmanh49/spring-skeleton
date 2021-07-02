package com.vm.skeleton.common;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Date;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.DefaultClaims;

@Component
public class JwtUtil {

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.validity}")
    private long jwtValidity;

    public static final String ROLE_CLAIM_KEY = "role";

    private static byte[] getByteArrFromString(String text) {
        return StringUtils.getBytes(text, StandardCharsets.UTF_8);
    }

    public String generateToken(UserDetails userDetails) {
        Claims claims = new DefaultClaims();
        claims.put(ROLE_CLAIM_KEY, userDetails.getAuthorities());
        return Jwts.builder().setClaims(claims).setSubject(userDetails.getUsername()).setIssuedAt(new Date())
                .setIssuer(userDetails.getUsername()).setExpiration(new Date(System.currentTimeMillis() + jwtValidity))
                .signWith(SignatureAlgorithm.HS512, getByteArrFromString(secretKey)).compact();
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
        return Jwts.parser().setSigningKey(getByteArrFromString(secretKey)).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}

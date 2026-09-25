package com.vm.skeleton.config;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * JWT signing settings. {@code validity} accepts a plain number (milliseconds) or a duration such as {@code 3h}.
 */
@Validated
@ConfigurationProperties("jwt")
public record JwtProperties(
        @NotBlank String secretKey,
        @NotNull @DefaultValue("3h") Duration validity,
        @NotBlank @DefaultValue("spring-skeleton") String issuer) {

    /** HS512 requires a key of at least 512 bits. */
    public static final int MIN_SECRET_KEY_BYTES = 64;

    @AssertTrue(message = "jwt.secret-key must be at least " + MIN_SECRET_KEY_BYTES + " bytes for HS512")
    public boolean isSecretKeyLongEnough() {
        return secretKey == null || secretKey.getBytes(StandardCharsets.UTF_8).length >= MIN_SECRET_KEY_BYTES;
    }
}

package com.vm.skeleton.common;

public final class SecurityConstants {
    private SecurityConstants() {
        throw new IllegalStateException();
    }

    public static final String[] SWAGGER_RESOURCES = { "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" };

    public static final String[] ALLOWED_URLS = { "/authenticate" };

    public static final String BEARER_PREFIX = "Bearer";
}


package com.vm.skeleton.common;

public final class SecurityConstants {
    private SecurityConstants() {
        throw new IllegalStateException();
    }

    public static final String API_V1 = "/api/v1";

    public static final String[] SWAGGER_RESOURCES = { "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" };

    public static final String[] ALLOWED_URLS = { API_V1 + "/auth/**", "/actuator/health" };

    public static final String BEARER_PREFIX = "Bearer";
}


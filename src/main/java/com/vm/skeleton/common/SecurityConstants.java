package com.vm.skeleton.common;

public final class SecurityConstants {
    private SecurityConstants() {
        throw new IllegalStateException();
    }

    public static final String API_BASE = "/api";

    public static final String[] SWAGGER_RESOURCES = { "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html" };

    public static final String[] ALLOWED_URLS = { API_BASE + "/auth/**", "/actuator/health", "/actuator/health/**" };

    /** JWT claim holding the user's role codes. */
    public static final String ROLES_CLAIM = "roles";
}

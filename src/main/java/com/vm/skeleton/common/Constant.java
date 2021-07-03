package com.vm.skeleton.common;

public final class Constant {
    private Constant() {
        throw new IllegalStateException();
    }

    public static final String[] SWAGGER_RESOURCES = { "/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
            "/configuration/security", "/swagger-ui.html", "/webjars/**" };

    public static final String[] ALLOWED_URLS = { "/authenticate" };

    public static final String BEARER_PREFIX = "Bearer";
}

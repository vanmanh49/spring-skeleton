package com.vm.skeleton.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("cors")
public record CorsProperties(
        @DefaultValue("*") List<String> allowedOrigins,
        @DefaultValue({ "GET", "POST", "PUT", "DELETE", "OPTIONS" }) List<String> allowedMethods,
        @DefaultValue("*") List<String> allowedHeaders) {
}

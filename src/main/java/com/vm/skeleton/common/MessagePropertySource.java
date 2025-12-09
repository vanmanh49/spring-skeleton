package com.vm.skeleton.common;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotEmpty;

import lombok.Getter;
import lombok.Setter;

@Configuration
@ConfigurationProperties(prefix = "")
@Setter
@Getter
@Validated
public class MessagePropertySource {

    @NotEmpty
    private Map<String, String> messages;

    public String getMessage(String code) {
        return messages != null ? messages.getOrDefault(code, "") : "";
    }
}

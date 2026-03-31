package com.vm.skeleton.common;

import java.text.MessageFormat;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessagePropertySourceUtil {

    private final MessagePropertySource messagePropertySource;

    public String getMessage(String code, Object[] args) {
        try {
            String message = messagePropertySource.getMessage(code);
            if (message == null || message.isBlank()) {
                log.warn("Message code '{}' not found in messages.yml", code);
                return "";
            }
            return (args == null || args.length == 0) ? message : MessageFormat.format(message, args);
        } catch (Exception e) {
            log.error("Error formatting message for code: {}", code, e);
            return "";
        }
    }
}

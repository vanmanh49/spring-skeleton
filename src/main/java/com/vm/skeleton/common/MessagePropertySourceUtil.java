package com.vm.skeleton.common;

import java.text.MessageFormat;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
            if (StringUtils.isBlank(message)) {
                log.warn("Message code '{}' not found in messages.yml", code);
                return StringUtils.EMPTY;
            }
            return ObjectUtils.isEmpty(args) ? message : MessageFormat.format(message, args);
        } catch (Exception e) {
            log.error("Error formatting message for code: {}", code, e);
            return StringUtils.EMPTY;
        }
    }
}

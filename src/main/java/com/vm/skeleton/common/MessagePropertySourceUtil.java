package com.vm.skeleton.common;

import java.lang.reflect.Field;
import java.text.MessageFormat;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MessagePropertySourceUtil {

    @Autowired
    private MessagePropertySource messagePropertySource;

    public String getMessage(String code, Object[] args) {
        try {
            Field declaredField = MessagePropertySource.class.getDeclaredField(code);
            declaredField.setAccessible(true);
            String message = (String) declaredField.get(messagePropertySource);
            return ObjectUtils.isEmpty(args) ? message : MessageFormat.format(message, args).toString();
        } catch (Exception e) {
            return StringUtils.EMPTY;
        }
    }
}

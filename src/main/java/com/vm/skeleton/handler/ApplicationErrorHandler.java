package com.vm.skeleton.handler;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.vm.skeleton.common.MessagePropertySourceUtil;
import com.vm.skeleton.dto.MessageResponseDto;

@RestControllerAdvice
public class ApplicationErrorHandler {

    @Autowired
    private MessagePropertySourceUtil sourceUtil;

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public MessageResponseDto handleException(Exception e) {
        return MessageResponseDto.builder().code("ERR_01").message(sourceUtil.getMessage("ERR_01", null)).build();
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponseDto handleAuthenticationException(AuthenticationException authenticationException) {
        return MessageResponseDto.builder().code("ERR_02").message(sourceUtil.getMessage("ERR_02", null)).build();
    }

    @ExceptionHandler(BusinessException.class)
    public MessageResponseDto handleBusinessException(BusinessException businessException,
            HttpServletResponse response) {
        response.setStatus(businessException.getStatusCode().value());
        return MessageResponseDto.builder().code(businessException.getCode()).message(businessException.getMessage())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponseDto handleBusinessException(HttpMessageNotReadableException notReadableException) {
        String errorMessage = notReadableException.getMessage().split(":")[0];
        return MessageResponseDto.builder().code("ERR_03")
                .message(sourceUtil.getMessage("ERR_03", new String[] { errorMessage })).build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public MessageResponseDto handleBusinessException(MethodArgumentNotValidException argumentNotValidException) {
        List<ObjectError> allErrors = argumentNotValidException.getAllErrors();
        String errorMessage = allErrors.stream().map(ObjectError::getDefaultMessage).collect(Collectors.joining(", "));
        return MessageResponseDto.builder().code("ERR_05")
                .message(sourceUtil.getMessage("ERR_05", new String[] { errorMessage })).build();
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public MessageResponseDto handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        String errorMessage = accessDeniedException.getMessage();
        return MessageResponseDto.builder().code("ERR_06")
                .message(sourceUtil.getMessage("ERR_06", new String[] { errorMessage })).build();
    }

}

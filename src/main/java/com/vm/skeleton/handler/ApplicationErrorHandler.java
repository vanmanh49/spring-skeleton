package com.vm.skeleton.handler;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.Nullable;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.vm.skeleton.common.ErrorCode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Renders every error as an RFC 9457 ProblemDetail carrying an {@code errorCode} property. Spring MVC exceptions
 * (including {@link BusinessException} and API version errors) are handled by {@link ResponseEntityExceptionHandler};
 * security exceptions also arrive here from the filter chain via {@code WebSecurityConfig}.
 */
@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class ApplicationErrorHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleAuthenticationException(AuthenticationException e) {
        ErrorCode errorCode = e instanceof BadCredentialsException
                ? ErrorCode.INVALID_CREDENTIALS
                : ErrorCode.AUTHENTICATION_REQUIRED;
        String challenge = e instanceof InvalidBearerTokenException ? "Bearer error=\"invalid_token\"" : "Bearer";
        return ResponseEntity.status(errorCode.getStatus())
                .header(HttpHeaders.WWW_AUTHENTICATE, challenge)
                .body(problem(errorCode));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getStatus()).body(problem(ErrorCode.ACCESS_DENIED));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleUnexpectedException(Exception e) {
        log.error("Unhandled exception", e);
        return ResponseEntity.status(ErrorCode.INTERNAL_ERROR.getStatus()).body(problem(ErrorCode.INTERNAL_ERROR));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.putIfAbsent(fieldError.getField(), String.valueOf(fieldError.getDefaultMessage()));
        }
        String summary = String.join(", ", fieldErrors.values());
        ProblemDetail body = problem(ErrorCode.VALIDATION_ERROR, summary);
        body.setProperty("errors", fieldErrors);
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ProblemDetail body = problem(ErrorCode.MALFORMED_REQUEST, "request body is missing or unreadable");
        return handleExceptionInternal(ex, body, headers, status, request);
    }

    /**
     * Adds an {@code errorCode} to ProblemDetails produced by the base class for standard MVC exceptions.
     */
    @Override
    protected ResponseEntity<Object> createResponseEntity(@Nullable Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        if (body instanceof ProblemDetail problemDetail
                && (problemDetail.getProperties() == null
                        || !problemDetail.getProperties().containsKey(ErrorCode.PROPERTY))) {
            ErrorCode fallback = statusCode.is5xxServerError() ? ErrorCode.INTERNAL_ERROR : ErrorCode.MALFORMED_REQUEST;
            problemDetail.setProperty(ErrorCode.PROPERTY, fallback.getCode());
        }
        return super.createResponseEntity(body, headers, statusCode, request);
    }

    private ProblemDetail problem(ErrorCode errorCode, Object... args) {
        String detail = messageSource.getMessage(errorCode.getMessageKey(), args, LocaleContextHolder.getLocale());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);
        problemDetail.setProperty(ErrorCode.PROPERTY, errorCode.getCode());
        return problemDetail;
    }
}

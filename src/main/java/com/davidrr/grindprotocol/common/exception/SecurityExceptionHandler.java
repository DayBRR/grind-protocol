package com.davidrr.grindprotocol.common.exception;

import com.davidrr.security.auth.exception.*;
import com.davidrr.security.common.GlobalExceptionHandler;
import com.davidrr.security.ratelimit.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Gives security errors precedence over the application's generic fallback. */
@RestControllerAdvice
@Order(-1)
public class SecurityExceptionHandler {

    private final GlobalExceptionHandler delegate = new GlobalExceptionHandler();

    // Delegate only security errors: validation must retain ValidationApiError.
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleBadCredentials(
            BadCredentialsException ex, HttpServletRequest request) {
        return delegate.handleBadCredentials(ex, request);
    }

    @ExceptionHandler(RefreshTokenMissingException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleRefreshMissing(
            RefreshTokenMissingException ex, HttpServletRequest request) {
        return delegate.handleRefreshMissing(ex, request);
    }

    @ExceptionHandler(RefreshTokenInvalidException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleRefreshInvalid(
            RefreshTokenInvalidException ex, HttpServletRequest request) {
        return delegate.handleRefreshInvalid(ex, request);
    }

    @ExceptionHandler(RefreshTokenExpiredException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleRefreshExpired(
            RefreshTokenExpiredException ex, HttpServletRequest request) {
        return delegate.handleRefreshExpired(ex, request);
    }

    @ExceptionHandler(RefreshTokenRevokedException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleRefreshRevoked(
            RefreshTokenRevokedException ex, HttpServletRequest request) {
        return delegate.handleRefreshRevoked(ex, request);
    }

    @ExceptionHandler(RefreshTokenReuseException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleRefreshTokenReuse(
            RefreshTokenReuseException ex, HttpServletRequest request) {
        return delegate.handleRefreshTokenReuse(ex, request);
    }

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleSessionNotFound(
            SessionNotFoundException ex, HttpServletRequest request) {
        return delegate.handleSessionNotFound(ex, request);
    }

    @ExceptionHandler(SuspiciousSessionException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleSuspiciousSession(
            SuspiciousSessionException ex, HttpServletRequest request) {
        return delegate.handleSuspiciousSession(ex, request);
    }

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<com.davidrr.security.common.ApiError> handleRateLimitExceeded(
            RateLimitExceededException ex, HttpServletRequest request) {
        return delegate.handleRateLimitExceeded(ex, request);
    }
}

package com.davidrr.grindprotocol.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@Slf4j
@RestControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiError> handleApiException(
            ApiException ex,
            HttpServletRequest request
    ) {
        log.warn(
                "API_ERROR code={} status={} path={} exception={} message={}",
                ex.getCode(),
                ex.getStatus().value(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage()
        );

        return buildError(
                ex.getStatus(),
                ex.getCode(),
                ex.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse(ErrorMessages.VALIDATION_FAILED);

        log.warn(
                "VALIDATION_ERROR code={} status={} path={} message={}",
                ErrorCodes.VALIDATION_ERROR,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                message
        );

        return buildError(
                HttpStatus.BAD_REQUEST,
                ErrorCodes.VALIDATION_ERROR,
                message,
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "UNHANDLED_ERROR code={} status={} path={} exception={} message={}",
                ErrorCodes.INTERNAL_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.INTERNAL_ERROR,
                ErrorMessages.INTERNAL_ERROR,
                request
        );
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String code,
            String message,
            HttpServletRequest request
    ) {
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                code,
                message,
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(body);
    }
}
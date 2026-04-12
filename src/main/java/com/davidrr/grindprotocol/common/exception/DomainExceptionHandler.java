package com.davidrr.grindprotocol.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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
    public ResponseEntity<ValidationApiError> handleValidation(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<ValidationErrorDetail> errors = getValidationErrorDetails(ex);

        String message = errors.isEmpty()
                ? ErrorMessages.Validation.FAILED
                : "Se han encontrado errores de validación";

        log.warn(
                "VALIDATION_ERROR code={} status={} path={} errors={}",
                ErrorCodes.Validation.ERROR,
                HttpStatus.BAD_REQUEST.value(),
                request.getRequestURI(),
                errors
        );

        ValidationApiError body = new ValidationApiError(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                ErrorCodes.Validation.ERROR,
                message,
                request.getRequestURI(),
                errors
        );

        return ResponseEntity.badRequest().body(body);
    }

    private @NonNull List<ValidationErrorDetail> getValidationErrorDetails(MethodArgumentNotValidException ex) {
        List<ValidationErrorDetail> errors = new ArrayList<>();

        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.add(new ValidationErrorDetail(
                    fieldError.getField(),
                    fieldError.getDefaultMessage()
            ));
        }

        for (ObjectError globalError : ex.getBindingResult().getGlobalErrors()) {
            errors.add(new ValidationErrorDetail(
                    globalError.getObjectName(),
                    globalError.getDefaultMessage()
            ));
        }
        return errors;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        log.error(
                "UNHANDLED_ERROR code={} status={} path={} exception={} message={}",
                ErrorCodes.Generic.INTERNAL_ERROR,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                request.getRequestURI(),
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );

        return buildError(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCodes.Generic.INTERNAL_ERROR,
                ErrorMessages.Generic.INTERNAL_ERROR,
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
package com.davidrr.grindprotocol.common.exception;

import java.time.Instant;
import java.util.List;

public record ValidationApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        List<ValidationErrorDetail> errors
) {
}
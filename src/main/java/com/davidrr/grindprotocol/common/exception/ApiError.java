package com.davidrr.grindprotocol.common.exception;

import java.time.Instant;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path
) {
}
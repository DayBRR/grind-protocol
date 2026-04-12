package com.davidrr.grindprotocol.common.exception;

public record ValidationErrorDetail(
        String field,
        String message
) {
}
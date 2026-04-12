package com.davidrr.grindprotocol.common.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApiException {

    public BusinessException(String code, String message) {
        super(code, message, HttpStatus.BAD_REQUEST);
    }
}
package com.davidrr.grindprotocol.user.exception;

import com.davidrr.grindprotocol.common.exception.ApiException;
import com.davidrr.grindprotocol.common.exception.ErrorCodes;
import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ApiException {

    public UserAlreadyExistsException(String username) {
        super(
                ErrorCodes.USER_ALREADY_EXISTS,
                ErrorMessages.USER_ALREADY_EXISTS + ": " + username,
                HttpStatus.CONFLICT
        );
    }
}
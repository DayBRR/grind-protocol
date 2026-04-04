package com.davidrr.grindprotocol.common.exception;

public final class ErrorCodes {

    private ErrorCodes() {
    }

    // --- VALIDATION ---
    public static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    // --- USER ---
    public static final String USER_ALREADY_EXISTS = "USER_ALREADY_EXISTS";
    public static final String USER_NOT_FOUND = "USER_NOT_FOUND";

    // --- USER PROFILE ---
    public static final String USER_PROFILE_NOT_FOUND = "USER_PROFILE_NOT_FOUND";

    // --- TASK ---
    public static final String TASK_NOT_FOUND = "TASK_NOT_FOUND";
    public static final String TASK_COMPLETION_NOT_ALLOWED = "TASK_COMPLETION_NOT_ALLOWED";

    // --- GENERIC ---
    public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
}
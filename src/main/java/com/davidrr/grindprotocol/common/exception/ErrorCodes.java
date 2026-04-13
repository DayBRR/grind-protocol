package com.davidrr.grindprotocol.common.exception;


public final class ErrorCodes {

    private ErrorCodes() {
        throw new AssertionError(ErrorMessages.NO_INSTANCES);
    }

    public static final class Generic {
        private Generic() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String INTERNAL_ERROR = "INTERNAL_ERROR";
    }

    public static final class Validation {
        private Validation() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String ERROR = "VALIDATION_ERROR";
    }

    public static final class User {
        private User() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String ALREADY_EXISTS = "USER_ALREADY_EXISTS";
        public static final String NOT_FOUND = "USER_NOT_FOUND";
    }

    public static final class UserProfile {
        private UserProfile() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "USER_PROFILE_NOT_FOUND";
    }

    public static final class Task {
        private Task() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "TASK_NOT_FOUND";
        public static final String COMPLETION_NOT_ALLOWED = "TASK_COMPLETION_NOT_ALLOWED";
        public static final String CONFIGURATION_INVALID = "TASK_CONFIGURATION_INVALID";
        public static final String TRAIT_NOT_FOUND_OR_INACTIVE = "TRAIT_NOT_FOUND_OR_INACTIVE";
    }

    public static final class TaskTemplate {
        private TaskTemplate() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "TASK_NOT_FOUND";
        public static final String COMPLETION_NOT_ALLOWED = "TASK_COMPLETION_NOT_ALLOWED";
        public static final String CONFIGURATION_INVALID = "TASK_CONFIGURATION_INVALID";
        public static final String TRAIT_NOT_FOUND_OR_INACTIVE = "TRAIT_NOT_FOUND_OR_INACTIVE";
    }
}
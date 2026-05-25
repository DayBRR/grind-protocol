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

        public static final String NOT_FOUND = "TASK_TEMPLATE_NOT_FOUND";
        public static final String TRAIT_NOT_FOUND_OR_INACTIVE = "TRAIT_NOT_FOUND_OR_INACTIVE";
    }

    public static final class DailyProgress {
        private DailyProgress() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "DAILY_PROGRESS_TEMPLATE_NOT_FOUND";
    }

    public static final class Reward {
        private Reward() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "REWARD_NOT_FOUND";
        public static final String NOT_ENOUGH_CORE_POINTS = "REWARD_NOT_ENOUGH_CORE_POINTS";
        public static final String REQUIRED_LEVEL_NOT_REACHED = "REWARD_REQUIRED_LEVEL_NOT_REACHED";
        public static final String REQUIRED_STREAK_NOT_REACHED = "REWARD_REQUIRED_STREAK_NOT_REACHED";
        public static final String REDEMPTION_NOT_FOUND = "REWARD_REDEMPTION_NOT_FOUND";
        public static final String REDEMPTION_NOT_USABLE = "REWARD_REDEMPTION_NOT_USABLE";
        public static final String NOT_REPEATABLE = "REWARD_NOT_REPEATABLE";
        public static final String COOLDOWN_ACTIVE = "REWARD_COOLDOWN_ACTIVE";
    }

    public static final class Achievement {
        private Achievement() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "ACHIEVEMENT_NOT_FOUND";
        public static final String NOT_UNLOCKED = "ACHIEVEMENT_NOT_UNLOCKED";
        public static final String ALREADY_CLAIMED = "ACHIEVEMENT_ALREADY_CLAIMED";
    }
}
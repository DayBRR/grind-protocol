package com.davidrr.grindprotocol.common.exception;

public final class ErrorMessages {

    public static final String NO_INSTANCES = "No instances";

    private ErrorMessages() {
        throw new AssertionError(NO_INSTANCES);
    }

    public static final class Generic {
        private Generic() {
            throw new AssertionError(NO_INSTANCES);
        }

        public static final String INTERNAL_ERROR = "Se ha producido un error interno";
    }

    public static final class Validation {
        private Validation() {
            throw new AssertionError(NO_INSTANCES);
        }

        public static final String FAILED = "Validación fallida";
    }

    public static final class User {
        private User() {
            throw new AssertionError(NO_INSTANCES);
        }

        public static final String ALREADY_EXISTS = "El usuario ya existe";
        public static final String NOT_FOUND = "Usuario no encontrado";
    }

    public static final class UserProfile {
        private UserProfile() {
            throw new AssertionError(NO_INSTANCES);
        }

        public static final String NOT_FOUND = "Perfil de usuario no encontrado";
    }

    public static final class Task {
        private Task() {
            throw new AssertionError(NO_INSTANCES);
        }

        public static final String NOT_FOUND = "Tarea no encontrada";
        public static final String COMPLETION_NOT_ALLOWED = "La tarea no puede completarse";
        public static final String CONFIGURATION_INVALID = "La configuración de la tarea no es válida";
        public static final String TRAIT_NOT_FOUND_OR_INACTIVE = "Uno o varios traits no existen o están inactivos";
    }

    public static final class TaskTemplate {
        private TaskTemplate () {
            throw new AssertionError(NO_INSTANCES);
        }

        public static final String NOT_FOUND = "Plantilla no encontrada";
        public static final String PUBLIC_NOT_FOUND = "Plantilla pública no encontrada";
    }

    public static final class Reward {
        private Reward() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "Reward not found.";
        public static final String NOT_ENOUGH_CORE_POINTS = "Not enough Core Points to redeem this reward.";
        public static final String REQUIRED_LEVEL_NOT_REACHED = "Required level not reached for this reward.";
        public static final String REQUIRED_STREAK_NOT_REACHED = "Required streak not reached for this reward.";
        public static final String REDEMPTION_NOT_FOUND = "Reward redemption not found.";
        public static final String REDEMPTION_NOT_USABLE = "Reward redemption cannot be used.";
        public static final String NOT_REPEATABLE = "This reward is not repeatable.";
        public static final String COOLDOWN_ACTIVE = "Reward cooldown is still active.";
    }
    public static final class Achievement {
        private Achievement() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "Achievement not found.";
        public static final String NOT_UNLOCKED = "Achievement is not unlocked.";
        public static final String ALREADY_CLAIMED = "Achievement reward already claimed.";
    }

    public static final class Quest {
        private Quest() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String NOT_FOUND = "Quest not found.";
        public static final String NOT_COMPLETED = "Quest is not completed.";
        public static final String ALREADY_CLAIMED = "Quest already claimed.";
    }
}
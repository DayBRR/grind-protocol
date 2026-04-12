package com.davidrr.grindprotocol.common.exception;

public final class ErrorMessages {

    public static final String NO_INSTANCES = "No instances";

    private ErrorMessages() {
        throw new AssertionError(NO_INSTANCES);
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

    public static final class Generic {
        private Generic() {
            throw new AssertionError(NO_INSTANCES);
        }

        public static final String INTERNAL_ERROR = "Se ha producido un error interno";
    }
}
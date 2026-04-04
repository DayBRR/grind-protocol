package com.davidrr.grindprotocol.common.exception;

public final class ErrorMessages {

    private ErrorMessages() {
    }

    // --- VALIDATION ---
    public static final String VALIDATION_FAILED = "Validación fallida";

    // --- USER ---
    public static final String USER_ALREADY_EXISTS = "El usuario ya existe";
    public static final String USER_NOT_FOUND = "Usuario no encontrado";

    // --- USER PROFILE ---
    public static final String USER_PROFILE_NOT_FOUND = "Perfil de usuario no encontrado";

    // --- TASK ---
    public static final String TASK_NOT_FOUND = "Tarea no encontrada";
    public static final String TASK_COMPLETION_NOT_ALLOWED = "La tarea no puede completarse";

    // --- GENERIC ---
    public static final String INTERNAL_ERROR = "Se ha producido un error interno";
}
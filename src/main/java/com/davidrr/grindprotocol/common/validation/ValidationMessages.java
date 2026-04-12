package com.davidrr.grindprotocol.common.validation;

import com.davidrr.grindprotocol.common.exception.ErrorMessages;

public class ValidationMessages {
    private ValidationMessages() {
        throw new AssertionError(ErrorMessages.NO_INSTANCES);
    }

    public static final class Task {
        private Task() {
            throw new AssertionError(ErrorMessages.NO_INSTANCES);
        }

        public static final String CONFIG_NOT_VALID = "La configuración de la tarea no es válida";
        public static final String TITLE_OBLIGATORY = "El campo 'title' es obligatorio";
        public static final String TITLE_SIZE = "El campo 'title' no puede superar 150 caracteres";
        public static final String DESCRIPTION_SIZE = "El campo 'description' no puede superar 1000 caracteres";
        public static final String CATEGORY_OBLIGATORY = "El campo 'category' es obligatoria";
        public static final String DIFFICULTY_OBLIGATORY = "El campo 'difficulty' es obligatoria";
        public static final String TASK_TYPE_OBLIGATORY = "El campo 'taskType' es obligatorio";
        public static final String BASE_XP_OBLIGATORY = "El campo 'baseXp' es obligatorio";
        public static final String BASE_XP_MIN = "El campo 'baseXp' debe ser mayor o igual que 1";
        public static final String MAX_COMPLETIONS_PER_DAY_OBLIGATORY = "El campo 'maxCompletionsPerDay' es obligatorio";
        public static final String MAX_COMPLETIONS_PER_DAY_MIN = "El campo 'maxCompletionsPerDay' debe ser mayor o igual que 1";
        public static final String MAX_COMPLETIONS_PER_DAY_MAX = "El campo 'maxCompletionsPerDay' no puede ser mayor que 50";
        public static final String WEEKLY_CLOSING_DAY_IN_RANGE = "El campo 'weeklyClosingDay' debe estar entre 1 y 7";
        public static final String TRAIT_CODES_LIMIT = "El campo 'traitCodes' no puede contener más de 10 elementos";
        public static final String TRAIT_CODES_EMPTY_VALUES = "El campo 'traitCodes' no puede contener valores vacíos";
        public static final String TRAIT_CODE_PATTERN_ERROR = "El campo cada 'traitCode' debe estar en mayúsculas y usar solo letras y guiones bajos";
        public static final String TRAIT_CODE_PATTERN = "^[A-Z_]+$";
    }
}

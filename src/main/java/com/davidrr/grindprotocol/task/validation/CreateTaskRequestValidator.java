package com.davidrr.grindprotocol.task.validation;

import com.davidrr.grindprotocol.common.exception.ErrorMessages;
import com.davidrr.grindprotocol.task.dto.CreateTaskRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CreateTaskRequestValidator implements ConstraintValidator<ValidCreateTaskRequest, CreateTaskRequest> {

    @Override
    public boolean isValid(CreateTaskRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (!request.isRepeatable()
                && request.getMaxCompletionsPerDay() != null
                && request.getMaxCompletionsPerDay() > 1) {

            context.buildConstraintViolationWithTemplate(
                    ErrorMessages.Task.CONFIGURATION_INVALID
                    )
                    .addPropertyNode("maxCompletionsPerDay")
                    .addConstraintViolation();

            valid = false;
        }

        if (request.isDiminishingReturnsEnabled() && !request.isRepeatable()) {
            context.buildConstraintViolationWithTemplate(
                            "diminishingReturnsEnabled solo tiene sentido cuando la tarea es repeatable"
                    )
                    .addPropertyNode("diminishingReturnsEnabled")
                    .addConstraintViolation();

            valid = false;
        }

        return valid;
    }
}
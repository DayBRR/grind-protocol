package com.davidrr.grindprotocol.task.validation;

import com.davidrr.grindprotocol.common.validation.ValidationMessages;
import com.davidrr.grindprotocol.task.dto.CreateTaskTemplateRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CreateTaskTemplateRequestValidator
        implements ConstraintValidator<ValidCreateTaskTemplateRequest, CreateTaskTemplateRequest> {

    @Override
    public boolean isValid(CreateTaskTemplateRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (!request.isRepeatable()
                && request.getMaxCompletionsPerDay() != null
                && request.getMaxCompletionsPerDay() > 1) {

            context.buildConstraintViolationWithTemplate(
                            ValidationMessages.TaskTemplate.NON_REPEATABLE_MAX_COMPLETIONS
                    )
                    .addPropertyNode("maxCompletionsPerDay")
                    .addConstraintViolation();

            valid = false;
        }

        if (request.isDiminishingReturnsEnabled() && !request.isRepeatable()) {
            context.buildConstraintViolationWithTemplate(
                            ValidationMessages.TaskTemplate.DIMINISHING_RETURNS_REQUIRES_REPEATABLE
                    )
                    .addPropertyNode("diminishingReturnsEnabled")
                    .addConstraintViolation();

            valid = false;
        }

        return valid;
    }
}
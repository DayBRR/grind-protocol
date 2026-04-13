package com.davidrr.grindprotocol.task.validation;

import com.davidrr.grindprotocol.common.validation.ValidationMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CreateTaskTemplateRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCreateTaskTemplateRequest {

    String message() default ValidationMessages.TaskTemplate.CONFIG_NOT_VALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
package com.davidrr.grindprotocol.task.validation;

import com.davidrr.grindprotocol.common.validation.ValidationMessages;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CreateTaskRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCreateTaskRequest {

    String message() default ValidationMessages.Task.CONFIG_NOT_VALID;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
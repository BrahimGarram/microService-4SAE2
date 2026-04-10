package com.example.servicetest.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = QuestionTestValidator.class)
@Documented
public @interface ValidQuestionTest {
    String message() default "Invalid question: for QCM at least 2 choices required; for non-CODING types correct answer is required.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

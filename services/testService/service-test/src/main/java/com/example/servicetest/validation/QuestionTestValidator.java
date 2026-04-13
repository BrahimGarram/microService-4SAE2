package com.example.servicetest.validation;

import com.example.servicetest.entity.QuestionTest;
import com.example.servicetest.entity.QuestionType;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class QuestionTestValidator implements ConstraintValidator<ValidQuestionTest, QuestionTest> {

    @Override
    public boolean isValid(QuestionTest value, ConstraintValidatorContext context) {
        if (value == null) return true;

        boolean valid = true;
        context.disableDefaultConstraintViolation();

        if (value.getQuestionType() == QuestionType.QCM_SIMPLE) {
            List<String> choices = value.getChoices();
            long filledCount = choices == null ? 0 : choices.stream()
                    .filter(c -> c != null && !c.isBlank())
                    .count();
            if (filledCount < 2) {
                context.buildConstraintViolationWithTemplate("For QCM, at least 2 choices are required.")
                        .addPropertyNode("choices")
                        .addConstraintViolation();
                valid = false;
            }
        }

        if (value.getQuestionType() != null && value.getQuestionType() != QuestionType.CODING) {
            String correct = value.getCorrectAnswer();
            if (correct == null || correct.isBlank()) {
                context.buildConstraintViolationWithTemplate("Correct answer is required for this question type.")
                        .addPropertyNode("correctAnswer")
                        .addConstraintViolation();
                valid = false;
            }
        }

        return valid;
    }
}

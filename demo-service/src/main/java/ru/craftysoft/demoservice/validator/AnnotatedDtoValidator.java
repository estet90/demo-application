package ru.craftysoft.demoservice.validator;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AnnotatedDtoValidator {

    private static final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

    public static <T> void validate(T request, Consumer<String> errorMessageConsumer) {
        var violations = factory.getValidator().validate(request);
        if (!violations.isEmpty()) {
            var message = violations.stream()
                    .map(AnnotatedDtoValidator::buildErrorMessage)
                    .collect(Collectors.joining(";"));
            errorMessageConsumer.accept(message);
        }
    }

    private static <T> String buildErrorMessage(ConstraintViolation<T> violation) {
        return String.format("параметр: %s, текущее значение: %s, сообщение об ошибке: '%s'", violation.getPropertyPath(), violation.getInvalidValue(), violation.getMessage());
    }
}

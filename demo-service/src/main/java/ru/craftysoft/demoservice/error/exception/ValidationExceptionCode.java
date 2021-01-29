package ru.craftysoft.demoservice.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.craftysoft.error.exception.ValidationException;
import ru.craftysoft.error.exception.code.ExceptionCode;

@RequiredArgsConstructor
@Getter
public enum ValidationExceptionCode implements ExceptionCode<ValidationException> {
    INCORRECT_REQUEST("01", "Некорректный запрос"),
    INCORRECT_RESPONSE("02", "Невалидный ответ"),
    ;

    private final String code;
    private final String message;
}

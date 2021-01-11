package ru.craftysoft.demoservice.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.craftysoft.error.exception.InvocationException;
import ru.craftysoft.error.exception.code.ExceptionCode;

@RequiredArgsConstructor
@Getter
public enum InvocationExceptionCode implements ExceptionCode<InvocationException> {
    DB("01", "Ошибка при запросе к БД"),
    ;

    private final String code;
    private final String message;
}

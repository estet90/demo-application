package ru.craftysoft.demoservice.error.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.craftysoft.error.exception.BusinessException;
import ru.craftysoft.error.exception.code.ExceptionCode;

@RequiredArgsConstructor
@Getter
public enum BusinessExceptionCode implements ExceptionCode<BusinessException> {
    USER_NOT_FOUND("01", "Пользователь не найден"),
    ;

    private final String code;
    private final String message;
}

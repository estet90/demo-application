package ru.craftysoft.demoservice.util;

import ru.craftysoft.error.exception.BaseException;

import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.error.exception.ExceptionFactory.newInternalException;

public class ExceptionMapper {

    public static BaseException mapException(Throwable e) {
        return e instanceof BaseException baseException
                ? baseException
                : newInternalException(e, resolve(), e.getMessage());
    }

}

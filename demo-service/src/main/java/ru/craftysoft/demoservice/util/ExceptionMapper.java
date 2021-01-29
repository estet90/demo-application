package ru.craftysoft.demoservice.util;

import reactor.util.context.ContextView;
import ru.craftysoft.error.exception.BaseException;

import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.error.exception.ExceptionFactory.newInternalException;

public class ExceptionMapper {

    public static BaseException mapException(ContextView context, Throwable e) {
        return e instanceof BaseException baseException
                ? baseException
                : newInternalException(e, resolve(context), e.getMessage());
    }

}

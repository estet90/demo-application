package ru.craftysoft.error.exception;

import ru.craftysoft.error.exception.code.ExceptionCode;
import ru.craftysoft.error.exception.type.ExceptionType;
import ru.craftysoft.error.operation.OperationCode;

public class InvocationException extends BaseException {
    InvocationException(String message, Throwable cause, String service, ExceptionCode<?> exceptionCode, OperationCode operationCode) {
        super(message, cause, service, ExceptionType.INVOCATION, exceptionCode, operationCode);
    }
}

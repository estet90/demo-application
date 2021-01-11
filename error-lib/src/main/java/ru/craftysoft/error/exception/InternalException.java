package ru.craftysoft.error.exception;

import ru.craftysoft.error.exception.type.ExceptionType;
import ru.craftysoft.error.operation.OperationCode;

public class InternalException extends BaseException {
    InternalException(String message, Throwable cause, String service, OperationCode operationCode) {
        super(message, cause, service, ExceptionType.INTERNAL, OTHER_EXCEPTION_CODE, operationCode);
    }
}

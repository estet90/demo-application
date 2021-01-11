package ru.craftysoft.error.exception;

import ru.craftysoft.error.exception.code.ExceptionCode;
import ru.craftysoft.error.exception.code.OtherExceptionCode;
import ru.craftysoft.error.exception.type.ExceptionType;
import ru.craftysoft.error.operation.OperationCode;

import static java.lang.String.format;

public class BaseException extends RuntimeException {

    private final String service;
    private final String type;
    private final String exceptionCode;
    private final String operation;
    private Object payload;
    private String originalCode;
    private String originalMessage;

    static final ExceptionCode<?> OTHER_EXCEPTION_CODE = new OtherExceptionCode();

    BaseException(String message,
                  Throwable cause,
                  String service,
                  ExceptionType type,
                  ExceptionCode<?> exceptionCode,
                  OperationCode operationCode) {
        super(message, cause);
        this.service = service;
        this.type = type == null ? null : type.getCode();
        this.exceptionCode = exceptionCode == null ? null : exceptionCode.getCode();
        this.operation = operationCode == null ? null : operationCode.getCode();
    }

    public String getFullErrorCode() {
        return format("%s-%s-%s%s", service, operation, type, exceptionCode);
    }

    public <T extends BaseException> T setPayload(Object payload) {
        this.payload = payload;
        return (T) this;
    }

    public <T extends BaseException> T setOriginalCode(String originalCode) {
        this.originalCode = originalCode;
        return (T) this;
    }

    public <T extends BaseException> T setOriginalMessage(String originalMessage) {
        this.originalMessage = originalMessage;
        return (T) this;
    }

    public Object getPayload() {
        return payload;
    }

    public String getOriginalCode() {
        return originalCode;
    }

    public String getOriginalMessage() {
        return originalMessage;
    }

    public String toString() {
        return getFullErrorCode();
    }
}

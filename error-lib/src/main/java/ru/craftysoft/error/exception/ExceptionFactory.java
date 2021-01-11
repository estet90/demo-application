package ru.craftysoft.error.exception;

import ru.craftysoft.error.exception.code.ExceptionCode;
import ru.craftysoft.error.operation.OperationCode;

import static java.util.Arrays.stream;

public class ExceptionFactory {

    private static final String DEFAULT_MESSAGE = "Сообщение не определено";

    private static String serviceCode;

    public ExceptionFactory(String serviceCode) {
        ExceptionFactory.serviceCode = serviceCode;
    }

    public static InvocationException newInvocationException(Throwable cause,
                                                             OperationCode operationCode,
                                                             ExceptionCode<InvocationException> exceptionCode,
                                                             String... args) {
        var message = prepareMessage(exceptionCode, args);
        return new InvocationException(message, cause, serviceCode, exceptionCode, operationCode);
    }

    public static InvocationException newInvocationException(OperationCode operationCode,
                                                             ExceptionCode<InvocationException> exceptionCode,
                                                             String... args) {
        var message = prepareMessage(exceptionCode, args);
        return new InvocationException(message, null, serviceCode, exceptionCode, operationCode);
    }

    public static ValidationException newValidationException(Throwable cause,
                                                             OperationCode operationCode,
                                                             ExceptionCode<ValidationException> exceptionCode,
                                                             String... args) {
        var message = prepareMessage(exceptionCode, args);
        return new ValidationException(message, cause, serviceCode, exceptionCode, operationCode);
    }

    public static ValidationException newValidationException(OperationCode operationCode,
                                                             ExceptionCode<ValidationException> exceptionCode,
                                                             String... args) {
        var message = prepareMessage(exceptionCode, args);
        return new ValidationException(message, null, serviceCode, exceptionCode, operationCode);
    }

    public static BusinessException newBusinessException(Throwable cause,
                                                         OperationCode operationCode,
                                                         ExceptionCode<BusinessException> exceptionCode,
                                                         String... args) {
        var message = prepareMessage(exceptionCode, args);
        return new BusinessException(message, cause, serviceCode, exceptionCode, operationCode);
    }

    public static BusinessException newBusinessException(OperationCode operationCode,
                                                         ExceptionCode<BusinessException> exceptionCode,
                                                         String... args) {
        var message = prepareMessage(exceptionCode, args);
        return new BusinessException(message, null, serviceCode, exceptionCode, operationCode);
    }

    public static InternalException newInternalException(Throwable cause,
                                                         OperationCode operationCode,
                                                         String... args) {
        var message = prepareInternalExceptionMessage(args);
        return new InternalException(message, cause, serviceCode, operationCode);
    }

    private static String prepareMessage(ExceptionCode<?> exceptionCode, String... args) {
        String message = exceptionCode.getMessage();
        return appendArguments(message != null ? message : DEFAULT_MESSAGE, args);
    }

    private static String prepareInternalExceptionMessage(String[] args) {
        return appendArguments("Иная ошибка", args);
    }

    private static String appendArguments(String message, String... args) {
        return message + stream(args).map(o -> ", '" + o + '\'').reduce(String::concat).orElse("");
    }

}

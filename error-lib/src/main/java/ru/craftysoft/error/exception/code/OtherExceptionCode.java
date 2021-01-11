package ru.craftysoft.error.exception.code;

public class OtherExceptionCode implements ExceptionCode {

    private static final String OTHER_EXCEPTION_MESSAGE = "Необработанное исключение";
    private static final String OTHER_EXCEPTION_CODE = "99";

    @Override
    public String getCode() {
        return OTHER_EXCEPTION_CODE;
    }

    @Override
    public String getMessage() {
        return OTHER_EXCEPTION_MESSAGE;
    }
}

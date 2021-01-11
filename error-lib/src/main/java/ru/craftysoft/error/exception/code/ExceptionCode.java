package ru.craftysoft.error.exception.code;

import ru.craftysoft.error.exception.BaseException;

public interface ExceptionCode<T extends BaseException> {

    String getCode();

    String getMessage();

}

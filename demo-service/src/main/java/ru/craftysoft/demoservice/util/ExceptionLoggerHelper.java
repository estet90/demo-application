package ru.craftysoft.demoservice.util;

import org.slf4j.Logger;
import ru.craftysoft.error.exception.BaseException;

import static java.util.Objects.nonNull;


public class ExceptionLoggerHelper {

    public static void logError(Logger logger, String point, Throwable e) {
        if (e instanceof BaseException baseException) {
            if (nonNull(baseException.getOriginalCode()) || nonNull(baseException.getOriginalMessage())) {
                logger.error("{}.thrown code={}\noriginalCode={}\noriginalMessage={}",
                        point,
                        baseException.getFullErrorCode(),
                        baseException.getOriginalCode(),
                        baseException.getOriginalMessage(),
                        baseException
                );
            } else {
                logger.error("{}.thrown code={}", point, baseException.getFullErrorCode(), baseException);
            }
        } else {
            logger.error("{}.thrown ", point, e);
        }
    }

    private static void logErrorMessages(Logger logger, String point, Throwable e) {
        if (e instanceof BaseException baseException) {
            if (nonNull(baseException.getOriginalCode()) || nonNull(baseException.getOriginalMessage())) {
                logger.error("{}.thrown code={}  message={}\noriginalCode={}\noriginalMessage={}",
                        point,
                        baseException.getFullErrorCode(),
                        baseException.getMessage(),
                        baseException.getOriginalCode(),
                        baseException.getOriginalMessage()
                );
            } else {
                logger.error("{}.thrown code={} message={}", point, baseException.getFullErrorCode(), baseException.getMessage());
            }
        } else {
            logger.error("{}.thrown {}", point, e.getMessage());
        }
    }

}

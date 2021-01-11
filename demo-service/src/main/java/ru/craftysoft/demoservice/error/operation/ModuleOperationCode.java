package ru.craftysoft.demoservice.error.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import reactor.util.context.ContextView;
import ru.craftysoft.error.operation.OperationCode;

import javax.annotation.Nonnull;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.util.module.common.logging.MdcKey.OPERATION_NAME;

@Getter
@RequiredArgsConstructor
public enum ModuleOperationCode implements OperationCode {

    GET_ALL_TASKS_BY_USER_ID("01"),
    ADD_TASK_TO_USER("02"),
    ;

    private final String code;

    public static OperationCode resolve() {
        try {
            return Enum.valueOf(ModuleOperationCode.class, MDC.get(OPERATION_NAME));
        } catch (IllegalArgumentException ex) {
            return () -> OTHER;
        }
    }

}

package ru.craftysoft.demoservice.error.operation;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.util.context.ContextView;
import ru.craftysoft.error.operation.OperationCode;

import java.util.Map;
import java.util.NoSuchElementException;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.util.module.common.logging.MdcKey.OPERATION_NAME;

@Getter
@RequiredArgsConstructor
public enum ModuleOperationCode implements OperationCode {

    GET_ALL_TASKS_BY_USER_ID("01"),
    ADD_TASK_TO_USER("02"),
    ADD_USER("03"),
    ;

    private final String code;

    public static OperationCode resolve(ContextView context) {
        try {
            var operationName = ofNullable(context.getOrDefault("mdc", Map.of()))
                    .map(map -> map.get(OPERATION_NAME))
                    .map(String.class::cast)
                    .orElseThrow();
            return Enum.valueOf(ModuleOperationCode.class, operationName);
        } catch (IllegalArgumentException | NullPointerException | NoSuchElementException ex) {
            return () -> OTHER;
        }
    }

}

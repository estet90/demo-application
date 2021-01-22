package ru.craftysoft.demoservice.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import ru.craftysoft.demoservice.logic.AddTaskToUserOperation;
import ru.craftysoft.demoservice.logic.GetAllTasksByUserIdOperation;
import ru.craftysoft.demoservice.model.Error;
import ru.craftysoft.demoservice.model.Task;
import ru.craftysoft.error.exception.BaseException;
import ru.craftysoft.error.exception.ValidationException;
import ru.craftysoft.util.module.common.json.Jackson;
import ru.craftysoft.util.module.common.properties.annotation.Property;
import ru.craftysoft.util.module.server.HttpResponse;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.Map;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.demoservice.error.exception.ValidationExceptionCode.INCORRECT_REQUEST;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.*;
import static ru.craftysoft.demoservice.util.ExceptionLoggerHelper.logError;
import static ru.craftysoft.demoservice.util.ExceptionMapper.mapException;
import static ru.craftysoft.demoservice.validator.AnnotatedDtoValidator.validate;
import static ru.craftysoft.error.exception.ExceptionFactory.newValidationException;
import static ru.craftysoft.util.module.common.logging.MdcKey.OPERATION_NAME;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.appendMdc;

@Singleton
@Slf4j
public class TaskController {

    private final GetAllTasksByUserIdOperation getAllTasksByUserIdOperation;
    private final AddTaskToUserOperation addTaskToUserOperation;
    private final Jackson jackson;

    @Inject
    public TaskController(@Property(value = "test", defaultValue = "3") @Named("test") int test,
                          GetAllTasksByUserIdOperation getAllTasksByUserIdOperation,
                          AddTaskToUserOperation addTaskToUserOperation,
                          Jackson jackson) {
        this.getAllTasksByUserIdOperation = getAllTasksByUserIdOperation;
        this.addTaskToUserOperation = addTaskToUserOperation;
        this.jackson = jackson;
    }

    public Mono<HttpResponse> getAllTasksByUserId(HttpServerRequest request) {
        return Mono
                .defer(() -> {
                            var userId = ofNullable(request.param("id"))
                                    .map(Long::valueOf)
                                    .orElseThrow(() -> newValidationException(resolve(), INCORRECT_REQUEST, "Не передан обязательный аргумент 'id'"));
                            return getAllTasksByUserIdOperation.process(userId)
                                    .map(tasks -> buildResponse(tasks, HttpResponseStatus.OK));
                        }
                )
                .onErrorMap(NumberFormatException.class, e -> newValidationException(resolve(), INCORRECT_REQUEST, e.getMessage()))
                .doOnError(throwable -> rethrow("TaskController.getAllTasksByUserId", throwable))
                .onErrorResume(BaseException.class, this::processErrorResponse)
                .contextWrite(appendMdc(OPERATION_NAME, GET_ALL_TASKS_BY_USER_ID.name()));
    }

    public Mono<HttpResponse> addTaskToUser(HttpServerRequest request, byte[] payload) {
        return Mono
                .defer(() -> {
                            var userId = ofNullable(request.param("id"))
                                    .map(Long::valueOf)
                                    .orElse(null);
                            var task = jackson.read(payload, Task.class);
                            validateAddTaskToUserRequest(userId, task);
                            return addTaskToUserOperation.process(userId, task)
                                    .map(tasks -> buildResponse(tasks, HttpResponseStatus.OK));
                        }
                )
                .onErrorMap(NumberFormatException.class, e -> newValidationException(resolve(), INCORRECT_REQUEST, e.getMessage()))
                .doOnError(throwable -> rethrow("TaskController.addTaskToUser", throwable))
                .onErrorResume(BaseException.class, this::processErrorResponse)
                .contextWrite(appendMdc(OPERATION_NAME, ADD_TASK_TO_USER.name()));
    }

    private void validateAddTaskToUserRequest(Long userId, Task task) {
        var errors = new ArrayList<String>();
        if (userId == null) {
            errors.add("Не передан обязательный аргумент 'id'");
        }
        validate(task, errors::add);
        if (!errors.isEmpty()) {
            throw newValidationException(resolve(), INCORRECT_REQUEST, errors.toString());
        }
    }

    private Mono<HttpResponse> processErrorResponse(BaseException baseException) {
        var error = new Error(baseException.getFullErrorCode(), baseException.getMessage());
        var status = baseException instanceof ValidationException
                ? HttpResponseStatus.BAD_REQUEST
                : HttpResponseStatus.INTERNAL_SERVER_ERROR;
        return Mono.just(buildResponse(error, status));
    }

    private void rethrow(String point, Throwable throwable) {
        var baseException = mapException(throwable);
        logError(log, point, baseException);
        throw baseException;
    }

    private HttpResponse buildResponse(Object result, HttpResponseStatus status) {
        var bytes = jackson.toByteArray(result);
        var responseHeaders = Map.of(
                "Content-Type", "application/json",
                "Content-Length", String.valueOf(bytes.length)
        );
        return new HttpResponse(
                bytes,
                status,
                responseHeaders
        );
    }
}

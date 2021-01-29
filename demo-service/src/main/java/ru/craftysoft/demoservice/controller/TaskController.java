package ru.craftysoft.demoservice.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerRequest;
import reactor.util.context.ContextView;
import ru.craftysoft.demoservice.logic.AddTaskToUserOperation;
import ru.craftysoft.demoservice.logic.GetAllTasksByUserIdOperation;
import ru.craftysoft.demoservice.model.Task;
import ru.craftysoft.demoservice.util.HttpControllerHelper;
import ru.craftysoft.error.exception.BaseException;
import ru.craftysoft.util.module.common.json.Jackson;
import ru.craftysoft.util.module.reactornetty.server.HttpResponse;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;

import static java.util.Optional.ofNullable;
import static ru.craftysoft.demoservice.error.exception.ValidationExceptionCode.INCORRECT_REQUEST;
import static ru.craftysoft.demoservice.error.exception.ValidationExceptionCode.INCORRECT_RESPONSE;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.*;
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
    private final HttpControllerHelper controllerHelper;

    @Inject
    public TaskController(GetAllTasksByUserIdOperation getAllTasksByUserIdOperation,
                          AddTaskToUserOperation addTaskToUserOperation,
                          Jackson jackson) {
        this.getAllTasksByUserIdOperation = getAllTasksByUserIdOperation;
        this.addTaskToUserOperation = addTaskToUserOperation;
        this.jackson = jackson;
        this.controllerHelper = new HttpControllerHelper(log, jackson);
    }

    public Mono<HttpResponse> getAllTasksByUserId(HttpServerRequest request) {
        return Mono.deferContextual(context -> Mono
                .defer(() -> {
                            var userId = ofNullable(request.param("id"))
                                    .map(Long::valueOf)
                                    .orElseThrow(() -> newValidationException(resolve(context), INCORRECT_REQUEST, "Не передан обязательный аргумент 'id'"));
                            return getAllTasksByUserIdOperation.process(context, userId)
                                    .doOnNext(response -> validate(response, message -> {
                                        throw newValidationException(resolve(context), INCORRECT_RESPONSE, message);
                                    }))
                                    .map(tasks -> controllerHelper.buildResponse(tasks, HttpResponseStatus.OK));
                        }
                )
                .onErrorMap(NumberFormatException.class, e -> newValidationException(resolve(context), INCORRECT_REQUEST, e.getMessage()))
                .doOnError(throwable -> controllerHelper.rethrow("TaskController.getAllTasksByUserId", context, throwable))
                .onErrorResume(BaseException.class, controllerHelper::processErrorResponse)
        ).contextWrite(appendMdc(OPERATION_NAME, GET_ALL_TASKS_BY_USER_ID.name()));
    }

    public Mono<HttpResponse> addTaskToUser(HttpServerRequest request, byte[] payload) {
        return Mono.deferContextual(context -> Mono
                .defer(() -> {
                            var userId = ofNullable(request.param("id"))
                                    .map(Long::valueOf)
                                    .orElse(null);
                            var task = jackson.read(payload, Task.class);
                            validateAddTaskToUserRequest(context, userId, task);
                            return addTaskToUserOperation.process(context, userId, task)
                                    .doOnNext(response -> validate(response, message -> {
                                        throw newValidationException(resolve(context), INCORRECT_RESPONSE, message);
                                    }))
                                    .map(tasks -> controllerHelper.buildResponse(tasks, HttpResponseStatus.OK));
                        }
                )
                .onErrorMap(NumberFormatException.class, e -> newValidationException(resolve(context), INCORRECT_REQUEST, e.getMessage()))
                .doOnError(throwable -> controllerHelper.rethrow("TaskController.addTaskToUser", context, throwable))
                .onErrorResume(BaseException.class, controllerHelper::processErrorResponse)
        ).contextWrite(appendMdc(OPERATION_NAME, ADD_TASK_TO_USER.name()));
    }

    private void validateAddTaskToUserRequest(ContextView context, Long userId, Task task) {
        var errors = new ArrayList<String>();
        if (userId == null) {
            errors.add("Не передан обязательный аргумент 'id'");
        }
        if (task == null) {
            errors.add("Не передана сущность 'Task'");
        } else {
            validate(task, errors::add);
        }
        if (!errors.isEmpty()) {
            throw newValidationException(resolve(context), INCORRECT_REQUEST, errors.toString());
        }
    }
}

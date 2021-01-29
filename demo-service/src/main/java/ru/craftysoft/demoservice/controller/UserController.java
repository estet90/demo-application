package ru.craftysoft.demoservice.controller;

import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.craftysoft.demoservice.logic.AddUserOperation;
import ru.craftysoft.demoservice.model.User;
import ru.craftysoft.demoservice.util.HttpControllerHelper;
import ru.craftysoft.error.exception.BaseException;
import ru.craftysoft.util.module.common.json.Jackson;
import ru.craftysoft.util.module.reactornetty.server.HttpResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import static ru.craftysoft.demoservice.error.exception.ValidationExceptionCode.INCORRECT_REQUEST;
import static ru.craftysoft.demoservice.error.exception.ValidationExceptionCode.INCORRECT_RESPONSE;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.ADD_TASK_TO_USER;
import static ru.craftysoft.demoservice.error.operation.ModuleOperationCode.resolve;
import static ru.craftysoft.demoservice.validator.AnnotatedDtoValidator.validate;
import static ru.craftysoft.error.exception.ExceptionFactory.newValidationException;
import static ru.craftysoft.util.module.common.logging.MdcKey.OPERATION_NAME;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.appendMdc;

@Slf4j
@Singleton
public class UserController {

    private final AddUserOperation addUserOperation;
    private final Jackson jackson;
    private final HttpControllerHelper controllerHelper;

    @Inject
    public UserController(AddUserOperation addUserOperation,
                          Jackson jackson) {
        this.addUserOperation = addUserOperation;
        this.jackson = jackson;
        this.controllerHelper = new HttpControllerHelper(log, jackson);
    }

    public Mono<HttpResponse> addUser(byte[] payload) {
        return Mono.deferContextual(context -> Mono
                .defer(() -> {
                            var user = jackson.read(payload, User.class);
                            validate(user, message -> {
                                throw newValidationException(resolve(context), INCORRECT_REQUEST, message);
                            });
                            return addUserOperation.process(context, user)
                                    .doOnNext(response -> validate(response, message -> {
                                        throw newValidationException(resolve(context), INCORRECT_RESPONSE, message);
                                    }))
                                    .map(tasks -> controllerHelper.buildResponse(tasks, HttpResponseStatus.OK));
                        }
                )
                .doOnError(throwable -> controllerHelper.rethrow("TaskController.addTaskToUser", context, throwable))
                .onErrorResume(BaseException.class, controllerHelper::processErrorResponse)
        ).contextWrite(appendMdc(OPERATION_NAME, ADD_TASK_TO_USER.name()));
    }
}

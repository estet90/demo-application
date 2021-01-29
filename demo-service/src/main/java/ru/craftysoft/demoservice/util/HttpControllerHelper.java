package ru.craftysoft.demoservice.util;

import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;
import ru.craftysoft.demoservice.model.Error;
import ru.craftysoft.error.exception.BaseException;
import ru.craftysoft.error.exception.ValidationException;
import ru.craftysoft.util.module.common.json.Jackson;
import ru.craftysoft.util.module.reactornetty.server.HttpResponse;

import javax.inject.Inject;
import javax.inject.Singleton;

import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaderValues.APPLICATION_JSON;
import static ru.craftysoft.demoservice.util.ExceptionLoggerHelper.logError;
import static ru.craftysoft.demoservice.util.ExceptionMapper.mapException;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.withContext;

public class HttpControllerHelper {

    private final Logger log;
    private final Jackson jackson;

    public HttpControllerHelper(Logger log, Jackson jackson) {
        this.log = log;
        this.jackson = jackson;
    }

    public Mono<HttpResponse> processErrorResponse(BaseException baseException) {
        var error = new Error()
                .code(baseException.getFullErrorCode())
                .message(baseException.getMessage());
        var status = baseException instanceof ValidationException
                ? HttpResponseStatus.BAD_REQUEST
                : HttpResponseStatus.INTERNAL_SERVER_ERROR;
        return Mono.just(buildResponse(error, status));
    }

    public void rethrow(String point, ContextView context, Throwable throwable) {
        var baseException = mapException(context, throwable);
        withContext(context, () -> logError(log, point, baseException));
        throw baseException;
    }

    public HttpResponse buildResponse(Object result, HttpResponseStatus status) {
        var bytes = jackson.toByteArray(result);
        var headers = new DefaultHttpHeaders()
                .add(CONTENT_TYPE.toString(), APPLICATION_JSON.toString())
                .add(CONTENT_LENGTH.toString(), String.valueOf(bytes.length));
        return new HttpResponse(
                bytes,
                status,
                headers
        );
    }

}

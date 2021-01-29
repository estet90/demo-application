package ru.craftysoft.demoservice.util;

import org.slf4j.Logger;
import reactor.core.publisher.Mono;
import reactor.util.context.ContextView;

import java.util.function.Function;

import static ru.craftysoft.demoservice.util.ExceptionMapper.mapException;
import static ru.craftysoft.util.module.common.reactor.MdcUtils.*;

public class OperationWrapper {

    public static <T> Mono<T> wrap(Logger log, String point, ContextView context, Function<ContextView, Mono<T>> action) {
        return action.apply(context)
                .doOnEach(onSubscribe(() -> log.info("{}.in", point)))
                .doOnEach(onComplete(() -> log.info("{}.out", point)))
                .doOnEach(onError(e -> {
                    log.error("{}.thrown {}", point, e.getMessage());
                    throw mapException(context, e);
                }));
    }

}

package ru.craftysoft.demoservice.util;

import org.slf4j.Logger;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

import static ru.craftysoft.demoservice.util.ExceptionMapper.mapException;

public class OperationWrapper {

    public static <T> Mono<T> wrap(Logger log, String point, Supplier<Mono<T>> action) {
        return action.get()
                .doOnSubscribe(subscription -> log.info("{}.in", point))
                .doOnSuccess(result -> log.info("{}.out", point))
                .doOnError(e -> {
                    log.error("{}.thrown {}", point, e.getMessage());
                    throw mapException(e);
                });
    }

}

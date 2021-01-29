package ru.craftysoft.util.module.common.reactor;

import org.slf4j.MDC;
import reactor.core.publisher.Signal;
import reactor.util.context.Context;
import reactor.util.context.ContextView;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.Optional.ofNullable;

public class MdcUtils {

    public static <T> Consumer<Signal<T>> onComplete(Runnable runnable) {
        return signal -> {
            if (signal.isOnComplete()) {
                withContext(signal.getContextView(), runnable);
            }
        };
    }

    public static <T> Consumer<Signal<T>> onNext(Runnable runnable) {
        return signal -> {
            if (signal.isOnNext()) {
                withContext(signal.getContextView(), runnable);
            }
        };
    }

    public static <T> Consumer<Signal<T>> onNext(Consumer<T> consumer) {
        return signal -> {
            if (signal.hasValue()) {
                withContext(signal.getContextView(), signal.get(), consumer);
            }
        };
    }

    public static <T> Consumer<Signal<T>> onSubscribe(Runnable runnable) {
        return signal -> {
            if (signal.isOnSubscribe()) {
                withContext(signal.getContextView(), runnable);
            }
        };
    }

    public static <T> Consumer<Signal<T>> onError(Consumer<Throwable> consumer) {
        return signal -> {
            if (signal.hasError()) {
                withContext(signal.getContextView(), signal.getThrowable(), consumer);
            }
        };
    }

    public static <T> void withContext(ContextView ctx, T target, Consumer<T> consumer) {
        withContext(ctx, () -> consumer.accept(target));
    }

    public static void withContext(ContextView ctx, Runnable runnable) {
        var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
        var oldMdc = MDC.getCopyOfContextMap();
        MDC.setContextMap(mdc);
        try {
            runnable.run();
        } finally {
            MDC.setContextMap(requireNonNullElseGet(oldMdc, Map::of));
        }
    }

    public static Function<Context, Context> appendMdc(Map<String, String> newMdc) {
        return context -> contextWithMdc(newMdc, context);
    }

    public static Context contextWithMdc(Map<String, String> newMdc, Context context) {
        var mdc = context.getOrDefault("mdc", Map.<String, String>of());
        var mergedMdc = Stream
                .concat(
                        ofNullable(mdc).stream().flatMap(m -> m.entrySet().stream()),
                        ofNullable(newMdc).stream().flatMap(m -> m.entrySet().stream())
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
        return context.put("mdc", mergedMdc);
    }

    public static Function<Context, Context> appendMdc(@Nonnull String key, String value) {
        requireNonNull(key);
        return context -> contextWithMdc(key, value, context);
    }

    public static Context contextWithMdc(@Nonnull String key, String value, Context context) {
        var mdc = context.getOrDefault("mdc", Map.<String, String>of());
        var mergedMdc = Stream
                .concat(
                        ofNullable(mdc).stream().flatMap(m -> m.entrySet().stream()),
                        Stream.of(Map.entry(key, ofNullable(value).orElse("null")))
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v2));
        return context.put("mdc", mergedMdc);
    }
}

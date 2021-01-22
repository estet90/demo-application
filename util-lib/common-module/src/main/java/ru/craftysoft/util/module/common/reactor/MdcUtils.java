package ru.craftysoft.util.module.common.reactor;

import org.slf4j.MDC;
import reactor.util.context.Context;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.Optional.ofNullable;

public class MdcUtils {
    public static void withContext(Context ctx, Runnable runnable) {
        var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
        var oldMdc = MDC.getCopyOfContextMap();
        MDC.setContextMap(mdc);
        try {
            runnable.run();
        } finally {
            MDC.setContextMap(oldMdc);
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

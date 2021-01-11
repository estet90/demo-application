package ru.craftysoft.util.module.common.reactor;

import org.slf4j.MDC;
import reactor.util.context.ContextView;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;

public class SwitchMdc implements AutoCloseable {
    private final Map<String, String> oldMdc = MDC.getCopyOfContextMap();

    public SwitchMdc(ContextView ctx) {
        MDC.setContextMap(ctx.getOrDefault("mdc", Map.of()));
    }

    public SwitchMdc(ContextView ctx, Map<String, String> newMdc) {
        var mdc = ctx.getOrDefault("mdc", Map.<String, String>of());
        var mergedMdc = Stream
                .concat(
                        ofNullable(mdc).stream().flatMap(m -> m.entrySet().stream()),
                        ofNullable(newMdc).stream().flatMap(m -> m.entrySet().stream())
                )
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b));
        MDC.setContextMap(mergedMdc);
    }

    @Override
    public void close() {
        MDC.setContextMap(ofNullable(oldMdc).orElseGet(Map::of));
    }
}

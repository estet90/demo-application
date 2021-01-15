package ru.craftysoft.util.module.common.properties;

import reactor.core.publisher.Flux;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

public class ApplicationProperties {
    private final List<PropertySource> sources;

    public ApplicationProperties(Set<PropertySource> sources) {
        this.sources = sources.stream()
                .sorted(comparing(PropertySource::priority).reversed())
                .collect(Collectors.toList());
    }

    @Nullable
    public String getProperty(String key) {
        return this.sources.stream()
                .map(s -> s.getProperty(key))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public Map<String, String> getProperties(String prefix) {
        var dottedPrefix = prefix.endsWith(".") || prefix.isEmpty()
                ? prefix
                : prefix + ".";
        return this.sources.stream()
                .map(s -> s.getProperties(dottedPrefix))
                .flatMap(m -> m.entrySet().stream())
                .collect(toMap(e -> e.getKey().substring(dottedPrefix.length()), Map.Entry::getValue, (s1, s2) -> s1));
    }

    public Flux<Map.Entry<String, String>> changes(String prefix) {
        var dottedPrefix = prefix.endsWith(".") || prefix.isEmpty()
                ? prefix
                : prefix + ".";
        return Flux.fromIterable(sources)
                .flatMap(s -> s.changes().onErrorContinue((e, t) -> {
                }))
                .flatMapIterable(Function.identity())
                .filter(key -> key.startsWith(dottedPrefix))
                .map(key -> new AbstractMap.SimpleImmutableEntry<>(key.substring(dottedPrefix.length()), this.getProperty(key)));
    }

    public String getProperty(String key, String defaultValue) {
        var property = getProperty(key);
        if (property == null) {
            return defaultValue;
        }
        return property;
    }

    public String getRequiredProperty(String key) {
        var property = getProperty(key);
        if (property == null) {
            throw new IllegalStateException(String.format("Required property %s was not found", key));
        }
        return property;
    }

    @Nullable
    public <T> T getProperty(String key, Function<String, T> converter) {
        var property = getProperty(key);
        if (property == null) {
            return null;
        }
        return converter.apply(property);
    }

    public <T> T getProperty(String key, T defaultValue, Function<String, T> converter) {
        var property = getProperty(key, converter);
        if (property == null) {
            return defaultValue;
        }
        return property;
    }

    public <T> T getRequiredProperty(String key, Function<String, T> converter) {
        return converter.apply(getRequiredProperty(key));
    }
}

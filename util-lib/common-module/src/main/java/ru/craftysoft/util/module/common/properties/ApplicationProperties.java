package ru.craftysoft.util.module.common.properties;

import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

@Slf4j
public class ApplicationProperties extends ConfigurationPropertiesSubscriber {

    public ApplicationProperties(Map<String, String> initialProperties) {
        super("*", log, initialProperties);
    }

    @Nullable
    public String getProperty(String key) {
        return this.properties.get(key);
    }

    public Map<String, String> getProperties(String prefix) {
        var dottedPrefix = prefix.endsWith(".") || prefix.isEmpty()
                ? prefix
                : prefix + ".";
        return this.properties.entrySet().stream()
                .filter(property -> property.getKey().startsWith(prefix))
                .collect(toMap(e -> e.getKey().substring(dottedPrefix.length()), Map.Entry::getValue, (s1, s2) -> s1));
    }

    public String getProperty(String key, String defaultValue) {
        var property = getProperty(key);
        if (property == null) {
            return defaultValue;
        }
        return property;
    }

    public String getRequiredProperty(String key) {
        return Objects.requireNonNull(getProperty(key), String.format("Не удалось получить значение по ключу '%s'", key));
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

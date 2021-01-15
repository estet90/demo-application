package ru.craftysoft.util.module.common.properties;

import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

@Slf4j
public class PropertyResolver {

    private final Map<String, String> properties;

    public PropertyResolver(String propertyPath) {
        var isClassPath = propertyPath.startsWith("classpath:/");
        try (var inputStream = isClassPath
                ? getClass().getResourceAsStream(propertyPath.substring(10))
                : Files.newInputStream(Paths.get(propertyPath))) {
            this.properties = YamlParser.parseYaml(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> getProperties(String startsWith) {
        return this.properties.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(startsWith))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (s1, s2) -> s1));
    }

    public String getProperty(String key) {
        return getProperty(key, Function.identity());
    }

    public String getRequiredProperty(String key) {
        return getRequiredProperty(key, Function.identity());
    }

    public int getRequiredIntProperty(String key) {
        return getRequiredProperty(key, Integer::parseInt);
    }

    private <T> T getRequiredProperty(String key, Function<String, T> transformer) {
        try {
            requireNonNull(key);
        } catch (Exception e) {
            log.error("PropertyResolver.getRequiredProperty.thrown key={}", key, e);
            throw e;
        }
        return getProperty(key, transformer);
    }

    private <T> T getProperty(String key, Function<String, T> transformer) {
        try {
            var value = requireNonNull(properties.get(key));
            log.info("PropertyResolver.getProperty key={} value={}", key, value);
            return transformer.apply(value);
        } catch (Exception e) {
            log.error("PropertyResolver.getProperty.thrown key={}", key, e);
            throw e;
        }
    }

}

package ru.craftysoft.util.module.common.properties;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class MapPropertySource implements PropertySource {
    private final Map<String, String> properties;
    private final int priority;

    public MapPropertySource(Map<String, String> properties, int priority) {
        this.properties = properties;
        this.priority = priority;
    }

    @Nullable
    @Override
    public String getProperty(String key) {
        return this.properties.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(key))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);

    }

    @Override
    public Map<String, String> getProperties(String startsWith) {
        return this.properties.entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(startsWith))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (s1, s2) -> s1));
    }

    @Override
    public int priority() {
        return this.priority;
    }
}

package ru.craftysoft.util.module.common.properties.source;

import java.util.Map;

public class MapPropertySource implements PropertySource {
    private final Map<String, String> properties;
    private final int priority;

    public MapPropertySource(Map<String, String> properties, int priority) {
        this.properties = properties;
        this.priority = priority;
    }

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int priority() {
        return this.priority;
    }
}

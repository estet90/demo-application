package ru.craftysoft.util.module.common.properties.source;

import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class MapPropertySource implements PropertySource {
    private final Map<String, String> properties;
    private final int priority;

    @Override
    public Map<String, String> getProperties() {
        return properties;
    }

    @Override
    public int priority() {
        return this.priority;
    }
}

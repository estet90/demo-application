package ru.craftysoft.util.module.common.properties.source;

import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class SystemPropertiesPropertySource implements PropertySource {
    @Override
    public Map<String, String> getProperties() {
        return System.getProperties().entrySet()
                .stream()
                .collect(toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }
}

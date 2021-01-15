package ru.craftysoft.util.module.common.properties;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class SystemPropertiesPropertySource implements PropertySource {
    @Nullable
    @Override
    public String getProperty(String key) {
        return System.getProperty(key);
    }

    @Override
    public Map<String, String> getProperties(String startsWith) {
        return System.getProperties().entrySet()
                .stream()
                .filter(e -> e.getKey().toString().startsWith(startsWith))
                .collect(toMap(e -> e.getKey().toString(), e -> e.getValue().toString()));
    }
}

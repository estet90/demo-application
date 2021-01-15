package ru.craftysoft.util.module.common.properties;

import javax.annotation.Nullable;
import java.util.Map;

import static java.util.stream.Collectors.toMap;

public class EnvironmentPropertySource implements PropertySource {
    @Nullable
    @Override
    public String getProperty(String key) {
        return System.getenv(key);
    }

    @Override
    public Map<String, String> getProperties(String startsWith) {
        return System.getenv().entrySet()
                .stream()
                .filter(e -> e.getKey().startsWith(startsWith))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}

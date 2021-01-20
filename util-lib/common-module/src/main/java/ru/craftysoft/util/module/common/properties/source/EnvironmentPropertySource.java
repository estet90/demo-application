package ru.craftysoft.util.module.common.properties.source;

import java.util.Map;

public class EnvironmentPropertySource implements PropertySource {
    @Override
    public Map<String, String> getProperties() {
        return System.getenv();
    }
}

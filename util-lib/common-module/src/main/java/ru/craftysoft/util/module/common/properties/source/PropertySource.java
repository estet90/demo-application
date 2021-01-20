package ru.craftysoft.util.module.common.properties.source;

import java.util.Map;

public interface PropertySource {
    int HIGHEST_PRIORITY = Integer.MAX_VALUE;
    int LOWEST_PRIORITY = Integer.MIN_VALUE;
    int COMMON_PROPERTY = 0;
    int TEST_PROPERTY = 1000;

    Map<String, String> getProperties();

    default int priority() {
        return COMMON_PROPERTY;
    }
}

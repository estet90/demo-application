package ru.craftysoft.util.module.common.properties;

import reactor.core.publisher.Flux;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;

public interface PropertySource {
    int HIGHEST_PRIORITY = Integer.MAX_VALUE;
    int LOWEST_PRIORITY = Integer.MIN_VALUE;
    int COMMON_PROPERTY = 0;
    int TEST_PROPERTY = 1000;

    @Nullable
    String getProperty(String key);

    Map<String, String> getProperties(String startsWith);

    default Flux<? extends Collection<String>> changes() {
        return Flux.never();
    }

    default int priority() {
        return COMMON_PROPERTY;
    }
}

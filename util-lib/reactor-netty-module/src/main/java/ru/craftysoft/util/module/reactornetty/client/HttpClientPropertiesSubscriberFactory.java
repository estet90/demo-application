package ru.craftysoft.util.module.reactornetty.client;

import io.netty.channel.ChannelOption;
import org.slf4j.Logger;
import reactor.netty.http.client.HttpClient;
import ru.craftysoft.util.module.common.properties.ConfigurationPropertiesSubscriber;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpClientPropertiesSubscriberFactory {
    public static ConfigurationPropertiesSubscriber httpClientPropertiesSubscriber(String prefix, Logger logger, HttpClient client) {
        return new ConfigurationPropertiesSubscriber(prefix, logger, new HashMap<>()) {
            @Override
            protected void refresh(Map<String, String> propertiesToAdd, Set<String> propertiesToRemove) {
                propertiesToAdd.forEach((key, value) -> {
                    try {
                        switch (key) {
                            case "url" -> {
                                var configuration = client.configuration();
                                var baseUrlField = configuration.getClass().getDeclaredField("baseUrl");
                                baseUrlField.setAccessible(true);
                                baseUrlField.set(configuration, value);
                            }
                            case "connect-timeout" -> {
                                var configuration = client.configuration();
                                var optionsField = configuration.getClass().getSuperclass().getSuperclass().getDeclaredField("options");
                                optionsField.setAccessible(true);
                                var options = (Map<ChannelOption<?>, Object>) optionsField.get(configuration);
                                var newOptions = new HashMap<ChannelOption<?>, Object>(options.size() + 1);
                                newOptions.putAll(options);
                                newOptions.put(ChannelOption.CONNECT_TIMEOUT_MILLIS, Integer.valueOf(value));
                                optionsField.set(configuration, newOptions);
                            }
                        }
                    } catch (Exception e) {
                        logger.error("HttpClientPropertiesSubscriber.refresh.thrown", e);
                    }
                });
                super.refresh(propertiesToAdd, propertiesToRemove);
            }
        };
    }

}

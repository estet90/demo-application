package ru.craftysoft.util.module.kafka;

import dagger.Module;
import dagger.Provides;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import ru.craftysoft.util.module.common.properties.PropertyResolver;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Module
public class KafkaConfigModule {

    @Provides
    @Singleton
    @Named("providesKafkaConsumerConfig")
    static Map<String, Object> providesKafkaConsumerConfig(PropertyResolver propertyResolver, @Named("kafkaApplicationName") String applicationName) {
        var consumerConfigs = new HashMap<String, Object>();
        var username = propertyResolver.getRequiredProperty("kafka.username");
        consumerConfigs.putIfAbsent(ConsumerConfig.GROUP_ID_CONFIG, username + "." + applicationName);
        consumerConfigs.putIfAbsent(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        consumerConfigs.putIfAbsent(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
        consumerConfigs.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "50");
        consumerConfigs.putIfAbsent(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "true");
        consumerConfigs.putIfAbsent(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");

        return commonConfig(propertyResolver, applicationName, consumerConfigs);
    }

    private static Map<String, Object> commonConfig(PropertyResolver properties, String applicationName, Map<String, Object> concreteConfig) {
        var commonProperties = properties.getProperties("kafka")
                .entrySet()
                .stream()
                .filter(e -> !e.getKey().equals("username"))
                .filter(e -> !e.getKey().equals("password"))
                .filter(e -> !e.getKey().startsWith("topic"))
                .filter(e -> !e.getKey().startsWith("consumer"))
                .filter(e -> !e.getKey().startsWith("producer"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        var config = new HashMap<String, Object>(commonProperties);
        var username = properties.getRequiredProperty("kafka.username");
        var password = properties.getRequiredProperty("kafka.password");

        config.put(CommonClientConfigs.BOOTSTRAP_SERVERS_CONFIG, properties.getRequiredProperty("kafka.bootstrap.servers"));
        config.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        config.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-512");
        config.put(SaslConfigs.SASL_JAAS_CONFIG, String.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";", username, password));
        config.putIfAbsent(CommonClientConfigs.CLIENT_ID_CONFIG, username + "." + applicationName);

        config.putAll(concreteConfig);
        return config;
    }

    @Provides
    @Singleton
    @Named("kafkaApplicationName")
    static String providesKafkaApplicationName(@Named("applicationName") String applicationName) {
        return applicationName.replace('-', '.').replace('_', '.').toLowerCase();
    }
}

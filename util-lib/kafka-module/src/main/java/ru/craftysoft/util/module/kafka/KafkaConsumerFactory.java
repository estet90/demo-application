package ru.craftysoft.util.module.kafka;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;

public interface KafkaConsumerFactory {
    <K, V> KafkaConsumer<K, V> get(Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer);
}

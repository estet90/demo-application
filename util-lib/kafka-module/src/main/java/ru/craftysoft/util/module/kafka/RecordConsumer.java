package ru.craftysoft.util.module.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

public interface RecordConsumer<K, V> {
    void process(KafkaConsumer<K, V> consumer, ConsumerRecords<K, V> records);
}

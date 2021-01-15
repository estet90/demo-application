package ru.craftysoft.util.module.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import reactor.core.publisher.Mono;

public interface RecordConsumer<K, V> {
    Mono<Void> process(KafkaConsumer<K, V> consumer, ConsumerRecords<K, V> records);
}

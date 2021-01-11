package ru.craftysoft.util.module.kafka;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;

import java.util.Collection;

public class KafkaListenerTask<K, V> implements ConsumerRebalanceListener {

    private final RecordConsumer<K, V> recordConsumer;
    private final KafkaConsumerFactory kafkaConsumerFactory;
    private final Logger logger;

    public KafkaListenerTask(RecordConsumer<K, V> recordConsumer,
                             KafkaConsumerFactory kafkaConsumerFactory,
                             Logger logger) {
        this.kafkaConsumerFactory = kafkaConsumerFactory;
        this.logger = logger;
        this.recordConsumer = recordConsumer;
    }

    @Override
    public void onPartitionsRevoked(Collection<TopicPartition> partitions) {

    }

    @Override
    public void onPartitionsAssigned(Collection<TopicPartition> partitions) {

    }

    @Override
    public void onPartitionsLost(Collection<TopicPartition> partitions) {

    }
}

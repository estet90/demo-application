package ru.craftysoft.util.module.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.craftysoft.util.module.common.reactor.MdcUtils;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static ru.craftysoft.util.module.common.logging.MdcKey.REQUEST_ID;

@Slf4j
public class KafkaListenerTask<K, V> implements ConsumerRebalanceListener {

    private final String topic;
    private final KafkaConsumerFactory kafkaConsumerFactory;
    private final Deserializer<K> keyDeserializer;
    private final Deserializer<V> valueDeserializer;
    private final RecordConsumer<K, V> recordConsumer;
    private final Logger logger;
    private KafkaConsumer<K, V> consumer = null;

    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public KafkaListenerTask(String topic,
                             KafkaConsumerFactory kafkaConsumerFactory,
                             Deserializer<K> keyDeserializer,
                             Deserializer<V> valueDeserializer,
                             RecordConsumer<K, V> recordConsumer) {
        this.topic = topic;
        this.kafkaConsumerFactory = kafkaConsumerFactory;
        this.keyDeserializer = keyDeserializer;
        this.valueDeserializer = valueDeserializer;
        this.recordConsumer = recordConsumer;
        this.logger = LoggerFactory.getLogger("ru.craftysoft.util.module.kafka." + topic + ".in");
    }

    public void start() {
        if (this.isRunning.compareAndSet(false, true)) {
            log.info("Запущено чтение из топика {}", this.topic);
        }
    }

    protected void run() {
        while (this.isRunning.get()) {
            log.trace("Trying new connection");
            try (var consumer = this.kafkaConsumerFactory.get(this.keyDeserializer, this.valueDeserializer)) {
                this.consumer = consumer;
                log.trace("Subscribing to topics");
                consumer.subscribe(List.of(topic), this);
                while (this.isRunning.get()) {
                    log.trace("Fetching new records");
                    var records = consumer.poll(Duration.ofSeconds(10));
                    log.trace("Records fetched. Size: {}", records.count());
                    if (records.isEmpty()) {
                        continue;
                    }
                    recordConsumer.process(consumer, records)
                            .contextWrite(MdcUtils.appendMdc(REQUEST_ID, UUID.randomUUID().toString()))
                            .subscribe();
                }
            } catch (WakeupException ignore) {
            } finally {
                this.consumer = null;
            }
        }
    }

    public void stop() {
        if (this.isRunning.compareAndSet(true, false)) {
            log.info("Остановлено чтение из топика {}", this.topic);
            if (this.consumer != null) {
                this.consumer.wakeup();
            }
        }
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

package ru.craftysoft.util.module.kafka;

import lombok.extern.slf4j.Slf4j;
import ru.craftysoft.util.module.common.properties.PropertyResolver;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Slf4j
public class KafkaListenerStarter {

    private final ExecutorService executorService;
    private final Supplier<KafkaListenerTask<?, ?>> taskFactory;
    private final int threadsCount;
    private final String prefix;
    private final String consumerName;

    private final List<KafkaListenerTask<?, ?>> containers = new ArrayList<>();
    private final AtomicReference<Boolean> isStarted = new AtomicReference<>(false);

    public KafkaListenerStarter(Supplier<KafkaListenerTask<?, ?>> taskFactory,
                                PropertyResolver propertyResolver,
                                String prefix) {
        this.taskFactory = taskFactory;
        this.threadsCount = propertyResolver.getRequiredIntProperty("kafka." + prefix + "threads-count");
        this.prefix = prefix;
        this.consumerName = "kafka-consumer." + prefix;
        this.executorService = Executors.newFixedThreadPool(threadsCount, namedThreadPoolFactory(consumerName));
    }

    public synchronized void start() {
        if (this.isStarted.compareAndSet(false, true)) {
            if (this.threadsCount <= 0) {
                log.info("Недопустимое значение '{}' параметра {}", threadsCount, "kafka." + prefix + "threads-count");
                return;
            }
            for (int i = 0; i < this.threadsCount; i++) {
                var kafkaListenerTask = taskFactory.get();
                this.containers.add(kafkaListenerTask);
                executorService.submit(kafkaListenerTask::start);
            }
            log.info("Количество потребителей {}", threadsCount);
        }
    }

    public synchronized void stop() {
        if (this.isStarted.compareAndSet(true, false)) {
            this.containers.forEach(KafkaListenerTask::stop);
        }
        shutdownExecutorServices(executorService);
    }

    private void shutdownExecutorServices(ExecutorService executorService) {
        log.info("Shutdown executor services of {}", consumerName);
        try {
            executorService.shutdown();
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
            if (!executorService.awaitTermination(2, TimeUnit.SECONDS)) {
                //we shouldn't see it in log
                log.error("Executor service {} is not shutdowned gracefully!!!", consumerName);
            }
        } catch (InterruptedException ex) {
            log.error("Interrupted exception during shutdown executor service", ex);
            Thread.currentThread().interrupt();
        }
    }


    private ThreadFactory namedThreadPoolFactory(String name) {
        return new ThreadFactory() {

            private final AtomicInteger counter = new AtomicInteger(0);

            @Override
            public Thread newThread(@Nonnull Runnable r) {
                return new Thread(r, name + "-" + counter.getAndIncrement());
            }
        };
    }
}

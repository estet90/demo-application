package ru.craftysoft.util.module.common;

import lombok.extern.slf4j.Slf4j;

import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
public class ScheduledTaskManager {

    private final ScheduledExecutorService scheduledExecutorService;
    private final int period;
    private final Set<Runnable> tasks;
    private final AtomicBoolean isStarted = new AtomicBoolean(false);

    public ScheduledTaskManager(ScheduledExecutorService scheduledExecutorService, int period, Set<Runnable> tasks) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.period = period;
        this.tasks = tasks;
    }

    public void start() {
        if (isStarted.compareAndSet(false, true)) {
            for (var task : tasks) {
                scheduledExecutorService.scheduleAtFixedRate(task, 0, period, TimeUnit.SECONDS);
            }
            log.info("ScheduledTaskManager.start количество запущенных задач: {}", tasks.size());
        }
    }

    public void stop() {
        if (isStarted.compareAndSet(true, false)) {
            this.scheduledExecutorService.shutdown();
            try {
                if (!this.scheduledExecutorService.awaitTermination(10, TimeUnit.SECONDS)) {
                    this.scheduledExecutorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                log.error("ScheduledTaskManager.stop.thrown", e);
            }
            log.info("ScheduledTaskManager.stop");
        }
    }
}

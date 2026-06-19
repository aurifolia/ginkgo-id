package org.aurifolia.cloud.id.sdk.internal;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 恢复探测器
 * <p>
 * 降级模式下定时探测ID服务是否恢复，恢复后填充双缓冲并退出降级模式。
 * 使用ScheduledExecutorService单线程定时执行，探测间隔5秒。
 *
 * @author Peng Dan
 * @since 2.0
 */
@Slf4j
final class RecoveryProbe {

    private static final long INITIAL_DELAY_SECONDS = 5;
    private static final long PERIOD_SECONDS = 5;

    private final Runnable recoveryAction;
    private final ScheduledExecutorService executor;
    private volatile ScheduledFuture<?> probeTask;

    RecoveryProbe(Runnable recoveryAction) {
        this.recoveryAction = recoveryAction;
        this.executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "id-recovery-probe");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 启动恢复探测
     */
    synchronized void start() {
        if (probeTask == null || probeTask.isDone()) {
            log.info("Starting recovery probe");
            probeTask = executor.scheduleAtFixedRate(
                    this::probe, INITIAL_DELAY_SECONDS, PERIOD_SECONDS, TimeUnit.SECONDS);
        }
    }

    /**
     * 停止恢复探测
     */
    synchronized void stop() {
        if (probeTask != null) {
            probeTask.cancel(false);
            probeTask = null;
            log.info("Stopping recovery probe");
        }
    }

    /**
     * 关闭探测器，释放线程资源
     */
    void shutdown() {
        stop();
        executor.shutdown();
    }

    private void probe() {
        try {
            recoveryAction.run();
        } catch (Exception e) {
            log.debug("Recovery probe exception", e);
        }
    }
}

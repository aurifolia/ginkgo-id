package org.aurifolia.cloud.id.sdk.internal;

import org.aurifolia.cloud.id.sdk.IdGenerator;
import org.aurifolia.cloud.id.sdk.fetcher.SegmentFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 号段ID生成器
 * <p>
 * 核心实现，组合双缓冲、TLAB、降级生成器和恢复探测器。
 * 启动时从ID服务获取2个号段填充双缓冲，每个线程通过TLAB无锁获取ID。
 * 当号段耗尽且无法获取新号段时进入降级模式，使用纯本地Snowflake生成ID。
 *
 * @author Peng Dan
 * @since 2.0
 */
public class SegmentIdGenerator implements IdGenerator {

    private static final Logger log = LoggerFactory.getLogger(SegmentIdGenerator.class);

    private final SegmentFetcher fetcher;
    private final DegradedIdGenerator degradedGenerator;
    private final SegmentBuffer buffer;
    private final RecoveryProbe recoveryProbe;
    private final ExecutorService fetchExecutor;

    private final ThreadLocal<ThreadLocalAllocator> threadLocalAllocator;

    /**
     * 构造函数，初始化双缓冲
     *
     * @param fetcher 号段获取器
     * @param degradedGenerator 降级模式ID生成器
     */
    public SegmentIdGenerator(SegmentFetcher fetcher, DegradedIdGenerator degradedGenerator) {
        this.fetcher = fetcher;
        this.degradedGenerator = degradedGenerator;
        this.buffer = new SegmentBuffer();
        this.fetchExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "id-segment-fetcher");
            t.setDaemon(true);
            return t;
        });
        this.recoveryProbe = new RecoveryProbe(this::attemptRecovery);
        this.threadLocalAllocator = ThreadLocal.withInitial(() -> new ThreadLocalAllocator(this));

        if (!fillBuffer()) {
            log.warn("初始化号段缓冲失败，进入降级模式");
            buffer.state.set(SegmentBuffer.STATE_DEGRADED);
            recoveryProbe.start();
        }
    }

    @Override
    public long nextId() {
        long id = threadLocalAllocator.get().nextId();
        if (id >= 0) {
            return id;
        }
        return degradedGenerator.nextId();
    }

    /**
     * 从双缓冲中分配一段序列号（供ThreadLocalAllocator调用）
     *
     * @param chunkSize 请求的序列号块大小
     * @return [segmentNumber, seqStart, seqEnd]，降级时返回null
     */
    long[] allocateChunk(int chunkSize) {
        while (true) {
            int state = buffer.state.get();

            if (state == SegmentBuffer.STATE_DEGRADED) {
                return null;
            }

            int slotIdx = buffer.activeSlot;
            SegmentSlot slot = buffer.slots[slotIdx];

            while (true) {
                long seq = slot.sequence.get();
                if (seq >= IdFormat.MAX_SEQ) {
                    break;
                }
                long newSeq = Math.min(seq + chunkSize, IdFormat.MAX_SEQ + 1);
                if (slot.sequence.compareAndSet(seq, newSeq)) {
                    return new long[]{slot.segmentNumber, seq, newSeq};
                }
            }

            if (state == SegmentBuffer.STATE_SWITCHING) {
                Thread.onSpinWait();
                continue;
            }

            if (buffer.state.compareAndSet(SegmentBuffer.STATE_NORMAL, SegmentBuffer.STATE_SWITCHING)) {
                if (buffer.trySwitch()) {
                    triggerAsyncFetch();
                } else {
                    buffer.state.set(SegmentBuffer.STATE_DEGRADED);
                    recoveryProbe.start();
                    return null;
                }
            }
        }
    }

    private void triggerAsyncFetch() {
        fetchExecutor.submit(() -> {
            try {
                Long segmentNumber = fetcher.fetchSegment();
                if (segmentNumber != null) {
                    buffer.refillInactiveSlot(segmentNumber);
                    log.debug("号段补充成功: segmentNumber={}", segmentNumber);
                } else {
                    log.warn("号段补充失败，当前活跃号段仍可继续使用");
                }
            } catch (Exception e) {
                log.warn("号段补充异常，当前活跃号段仍可继续使用", e);
            } finally {
                buffer.state.set(SegmentBuffer.STATE_NORMAL);
            }
        });
    }

    private boolean fillBuffer() {
        try {
            Long seg0 = fetcher.fetchSegment();
            if (seg0 == null) {
                return false;
            }
            Long seg1 = fetcher.fetchSegment();
            if (seg1 == null) {
                return false;
            }
            buffer.init(seg0, seg1);
            log.info("号段缓冲初始化成功: seg0={}, seg1={}", seg0, seg1);
            return true;
        } catch (Exception e) {
            log.error("号段缓冲初始化异常", e);
            return false;
        }
    }

    private void attemptRecovery() {
        if (buffer.state.get() != SegmentBuffer.STATE_DEGRADED) {
            recoveryProbe.stop();
            return;
        }
        try {
            Long seg0 = fetcher.fetchSegment();
            if (seg0 == null) {
                return;
            }
            Long seg1 = fetcher.fetchSegment();
            if (seg1 == null) {
                return;
            }
            buffer.init(seg0, seg1);
            buffer.state.set(SegmentBuffer.STATE_NORMAL);
            recoveryProbe.stop();
            log.info("服务恢复，退出降级模式");
        } catch (Exception e) {
            log.debug("恢复探测失败", e);
        }
    }

    /**
     * 关闭生成器，释放所有后台线程资源
     */
    public void shutdown() {
        recoveryProbe.shutdown();
        fetchExecutor.shutdown();
    }
}

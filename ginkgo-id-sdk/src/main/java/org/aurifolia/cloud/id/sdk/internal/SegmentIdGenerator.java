package org.aurifolia.cloud.id.sdk.internal;

import lombok.extern.slf4j.Slf4j;
import org.aurifolia.cloud.id.sdk.IdGenerator;
import org.aurifolia.cloud.id.sdk.fetcher.SegmentFetcher;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
@Slf4j
public class SegmentIdGenerator implements IdGenerator {

    private final SegmentFetcher fetcher;
    private final DegradedIdGenerator degradedGenerator;
    private final SegmentBuffer buffer;
    private final RecoveryProbe recoveryProbe;
    private final ExecutorService fetchExecutor;
    private final boolean degradeEnabled;

    private final ThreadLocal<ThreadLocalAllocator> threadLocalAllocator;

    /**
     * 构造函数，初始化双缓冲
     *
     * @param fetcher           号段获取器
     * @param degradedGenerator 降级模式ID生成器
     * @param degradeEnabled    是否启用降级模式
     */
    public SegmentIdGenerator(SegmentFetcher fetcher, DegradedIdGenerator degradedGenerator, boolean degradeEnabled) {
        this.fetcher = fetcher;
        this.degradedGenerator = degradedGenerator;
        this.degradeEnabled = degradeEnabled;
        this.buffer = new SegmentBuffer();
        this.fetchExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "id-segment-fetcher");
            t.setDaemon(true);
            return t;
        });
        this.recoveryProbe = new RecoveryProbe(this::attemptRecovery);
        this.threadLocalAllocator = ThreadLocal.withInitial(() -> new ThreadLocalAllocator(this));

        if (!fillBuffer()) {
            if (degradeEnabled) {
                log.warn("Failed to initialize segment buffer, entering degraded mode");
                buffer.setStateVolatile(SegmentBuffer.STATE_DEGRADED);
                recoveryProbe.start();
            } else {
                throw new IllegalStateException("Failed to initialize segment buffer and degradation is disabled");
            }
        }
    }

    @Override
    public long nextId() {
        long id = threadLocalAllocator.get().nextId();
        if (id >= 0) {
            return id;
        }
        if (!degradeEnabled) {
            throw new IllegalStateException("Segment buffer exhausted and degradation is disabled");
        }
        return degradedGenerator.nextId();
    }

    /**
     * 从双缓冲中分配一段序列号（供ThreadLocalAllocator调用）
     *
     * @param chunkSize 请求的序列号块大小
     * @param chunk     输出对象，分配成功时填充其字段
     * @return 分配成功返回true，降级时返回false
     */
    boolean allocateChunk(int chunkSize, Chunk chunk) {
        while (true) {
            int state = buffer.getStateOpaque();

            if (state == SegmentBuffer.STATE_DEGRADED) {
                return false;
            }

            int slotIdx = buffer.activeSlot;
            SegmentSlot slot = buffer.slots[slotIdx];

            while (true) {
                long seq = SegmentSlot.getSequenceOpaque(slot);
                if (seq >= IdFormat.MAX_SEQ) {
                    break;
                }
                long newSeq = Math.min(seq + chunkSize, IdFormat.MAX_SEQ + 1);
                if (SegmentSlot.compareAndSetSequence(slot, seq, newSeq)) {
                    chunk.segmentNumber = slot.segmentNumber;
                    chunk.seqStart = seq;
                    chunk.seqEnd = newSeq;
                    return true;
                }
            }

            if (state == SegmentBuffer.STATE_SWITCHING) {
                Thread.onSpinWait();
                continue;
            }

            if (buffer.compareAndSetState(SegmentBuffer.STATE_NORMAL, SegmentBuffer.STATE_SWITCHING)) {
                if (buffer.trySwitch()) {
                    triggerAsyncFetch();
                } else {
                    if (degradeEnabled) {
                        buffer.setStateVolatile(SegmentBuffer.STATE_DEGRADED);
                        recoveryProbe.start();
                        return false;
                    } else {
                        buffer.setStateVolatile(SegmentBuffer.STATE_NORMAL);
                        throw new IllegalStateException("Segment buffer exhausted and degradation is disabled");
                    }
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
                    log.debug("Segment refilled successfully: segmentNumber={}", segmentNumber);
                } else {
                    log.warn("Segment refill failed, current active segment still usable");
                }
            } catch (Exception e) {
                log.warn("Segment refill exception, current active segment still usable", e);
            } finally {
                buffer.setStateVolatile(SegmentBuffer.STATE_NORMAL);
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
            log.info("Segment buffer initialized successfully: seg0={}, seg1={}", seg0, seg1);
            return true;
        } catch (Exception e) {
            log.error("Segment buffer initialization exception", e);
            return false;
        }
    }

    private void attemptRecovery() {
        if (buffer.getStateOpaque() != SegmentBuffer.STATE_DEGRADED) {
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
            buffer.setStateVolatile(SegmentBuffer.STATE_NORMAL);
            recoveryProbe.stop();
            log.info("Service recovered, exiting degraded mode");
        } catch (Exception e) {
            log.debug("Recovery probe failed", e);
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

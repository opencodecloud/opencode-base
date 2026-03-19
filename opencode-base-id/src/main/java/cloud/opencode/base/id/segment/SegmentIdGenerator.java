package cloud.opencode.base.id.segment;

import cloud.opencode.base.id.IdGenerator;
import cloud.opencode.base.id.exception.OpenIdGenerationException;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Segment Mode ID Generator
 * 号段模式ID生成器
 *
 * <p>Generates IDs using segment mode with double buffering for high performance.
 * Segments are fetched from a SegmentAllocator and cached locally.</p>
 * <p>使用双缓冲号段模式生成ID以实现高性能。
 * 号段从SegmentAllocator获取并在本地缓存。</p>
 *
 * <p><strong>Double Buffering | 双缓冲:</strong></p>
 * <pre>
 * Buffer1: [1, 1000] → Currently using
 * Buffer2: [1001, 2000] → Preloaded
 *
 * When Buffer1 reaches 20%, async load Buffer2
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Double buffering - 双缓冲</li>
 *   <li>Async preloading - 异步预加载</li>
 *   <li>High throughput - 高吞吐量</li>
 *   <li>Sequential IDs - 顺序ID</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JdbcSegmentAllocator allocator = JdbcSegmentAllocator.create(dataSource);
 * SegmentIdGenerator gen = SegmentIdGenerator.create(allocator, "order");
 *
 * long orderId = gen.generate();
 *
 * // Monitor status
 * var status = gen.getBufferStatus();
 * System.out.println("Usage: " + status.usagePercent() + "%");
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>~10M ops/sec single thread - 单线程约10M次/秒</li>
 *   <li>~80M ops/sec with 8 threads - 8线程约80M次/秒</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class SegmentIdGenerator implements IdGenerator<Long>, AutoCloseable {

    private static final String DEFAULT_BIZ_TAG = "default";
    private static final long SPIN_WAIT_TIMEOUT_NANOS = 5_000_000_000L; // 5 seconds

    private final SegmentAllocator allocator;
    private final String bizTag;
    private final ReentrantLock lock = new ReentrantLock();
    private final AtomicBoolean preloading = new AtomicBoolean(false);
    private final AtomicBoolean closed = new AtomicBoolean(false);
    private final ExecutorService preloadExecutor;

    private volatile SegmentBuffer current;
    private volatile SegmentBuffer next;

    /**
     * Creates a generator with allocator
     * 使用分配器创建生成器
     *
     * @param allocator the segment allocator | 号段分配器
     */
    private SegmentIdGenerator(SegmentAllocator allocator, String bizTag) {
        this.allocator = allocator;
        this.bizTag = bizTag;
        this.current = new SegmentBuffer();
        this.next = new SegmentBuffer();
        this.preloadExecutor = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual().name("segment-preloader-" + bizTag).factory()
        );

        // Initialize first segment
        loadSegment(current);
    }

    /**
     * Creates a generator with default business tag
     * 使用默认业务标识创建生成器
     *
     * @param allocator the segment allocator | 号段分配器
     * @return generator | 生成器
     */
    public static SegmentIdGenerator create(SegmentAllocator allocator) {
        return new SegmentIdGenerator(allocator, DEFAULT_BIZ_TAG);
    }

    /**
     * Creates a generator with specific business tag
     * 使用指定业务标识创建生成器
     *
     * @param allocator the segment allocator | 号段分配器
     * @param bizTag    the business tag | 业务标识
     * @return generator | 生成器
     */
    public static SegmentIdGenerator create(SegmentAllocator allocator, String bizTag) {
        return new SegmentIdGenerator(allocator, bizTag);
    }

    @Override
    public Long generate() {
        // Check if closed
        if (closed.get()) {
            throw new IllegalStateException("SegmentIdGenerator has been closed");
        }

        // Check if should preload
        if (current.shouldPreload() && !next.isInitialized()) {
            asyncPreload();
        }

        // Try to get from current buffer
        long value = current.getNext();
        if (value >= 0) {
            return value;
        }

        // Current exhausted, switch to next
        lock.lock();
        try {
            // Check closed again under lock
            if (closed.get()) {
                throw new IllegalStateException("SegmentIdGenerator has been closed");
            }

            // Double check
            value = current.getNext();
            if (value >= 0) {
                return value;
            }

            // Switch buffers
            // If preload is in flight, wait for it to avoid segment ordering issues
            if (!next.isInitialized() && preloading.get()) {
                long spinStart = System.nanoTime();
                while (preloading.get()) {
                    if (System.nanoTime() - spinStart > SPIN_WAIT_TIMEOUT_NANOS) {
                        throw new OpenIdGenerationException("segment", bizTag,
                                "Timed out waiting for segment preload to complete for bizTag: " + bizTag);
                    }
                    Thread.onSpinWait();
                }
            }
            if (next.isInitialized()) {
                current = next;
                next = new SegmentBuffer();
            } else {
                // Next not ready, load synchronously
                loadSegment(current);
            }

            value = current.getNext();
            if (value >= 0) {
                return value;
            }

            throw OpenIdGenerationException.segmentExhausted(bizTag);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets the business tag
     * 获取业务标识
     *
     * @return business tag | 业务标识
     */
    public String getBizTag() {
        return bizTag;
    }

    /**
     * Gets the current buffer status
     * 获取当前缓冲区状态
     *
     * @return buffer status | 缓冲区状态
     */
    public BufferStatus getBufferStatus() {
        return new BufferStatus(
                current.getCurrentValue(),
                current.getMaxValue(),
                current.getUsagePercent(),
                next.isInitialized()
        );
    }

    @Override
    public String getType() {
        return "Segment";
    }

    /**
     * Shuts down the preload executor and releases resources.
     * 关闭预加载执行器并释放资源。
     */
    @Override
    public void close() {
        closed.set(true);
        preloadExecutor.shutdown();
        try {
            if (!preloadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                preloadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            preloadExecutor.shutdownNow();
        }
    }

    private void loadSegment(SegmentBuffer buffer) {
        try {
            SegmentAllocator.Segment segment = allocator.allocate(bizTag);
            buffer.init(segment);
        } catch (Exception e) {
            throw OpenIdGenerationException.segmentAllocationFailed(bizTag, e);
        }
    }

    private void asyncPreload() {
        if (preloading.compareAndSet(false, true)) {
            preloadExecutor.submit(() -> {
                try {
                    loadSegment(next);
                } finally {
                    preloading.set(false);
                }
            });
        }
    }

    /**
     * Buffer Status
     * 缓冲区状态
     *
     * @param currentValue   the current value | 当前值
     * @param maxValue       the max value | 最大值
     * @param usagePercent   the usage percentage | 使用百分比
     * @param nextBufferReady whether next buffer is ready | 下一缓冲区是否就绪
     */
    public record BufferStatus(
            long currentValue,
            long maxValue,
            int usagePercent,
            boolean nextBufferReady
    ) {
        @Override
        public String toString() {
            return String.format("BufferStatus{current=%d, max=%d, usage=%d%%, nextReady=%s}",
                    currentValue, maxValue, usagePercent, nextBufferReady);
        }
    }
}

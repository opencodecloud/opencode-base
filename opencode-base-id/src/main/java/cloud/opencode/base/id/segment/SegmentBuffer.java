package cloud.opencode.base.id.segment;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Segment Buffer for Double Buffering
 * 双缓冲号段缓冲区
 *
 * <p>Internal buffer used by SegmentIdGenerator for double-buffering.
 * Allows preloading the next segment while the current one is in use.</p>
 * <p>SegmentIdGenerator用于双缓冲的内部缓冲区。
 * 允许在使用当前号段时预加载下一个号段。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Atomic counter for thread safety - 原子计数器确保线程安全</li>
 *   <li>Buffer state tracking - 缓冲区状态跟踪</li>
 *   <li>Preload threshold detection - 预加载阈值检测</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // SegmentBuffer is used internally by SegmentIdGenerator
 * // SegmentBuffer由SegmentIdGenerator内部使用
 * SegmentBuffer buffer = new SegmentBuffer(allocator);
 * long id = buffer.nextId();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class SegmentBuffer {

    /**
     * Default preload threshold (20%)
     * 默认预加载阈值（20%）
     */
    public static final double DEFAULT_PRELOAD_THRESHOLD = 0.2;

    private final AtomicLong currentValue;
    private volatile long maxValue;
    private volatile int step;
    private volatile boolean initialized;
    private final double preloadThreshold;

    /**
     * Creates a segment buffer
     * 创建号段缓冲区
     */
    public SegmentBuffer() {
        this(DEFAULT_PRELOAD_THRESHOLD);
    }

    /**
     * Creates a segment buffer with custom preload threshold
     * 使用自定义预加载阈值创建号段缓冲区
     *
     * @param preloadThreshold the threshold (0.0 - 1.0) | 阈值（0.0 - 1.0）
     */
    public SegmentBuffer(double preloadThreshold) {
        this.currentValue = new AtomicLong(0);
        this.maxValue = 0;
        this.step = 0;
        this.initialized = false;
        this.preloadThreshold = preloadThreshold;
    }

    /**
     * Initializes the buffer with a segment
     * 使用号段初始化缓冲区
     *
     * @param segment the segment | 号段
     */
    public void init(SegmentAllocator.Segment segment) {
        if (segment.start() < 0) {
            throw new IllegalArgumentException("Segment start must be non-negative: " + segment.start());
        }
        if (segment.end() < segment.start()) {
            throw new IllegalArgumentException("Segment end must be >= start: " + segment);
        }
        this.currentValue.set(segment.start());
        this.maxValue = segment.end();
        this.step = segment.step();
        this.initialized = true;
    }

    /**
     * Gets the next value
     * 获取下一个值
     *
     * <p>Uses compare-and-set loop to prevent overshoot past maxValue
     * and overflow near Long.MAX_VALUE.</p>
     * <p>使用CAS循环防止超过maxValue以及在Long.MAX_VALUE附近溢出。</p>
     *
     * @return next value, or -1 if exhausted | 下一个值，如果耗尽返回-1
     */
    public long getNext() {
        while (true) {
            long current = currentValue.get();
            if (current >= maxValue || current < 0) {
                return -1;
            }
            if (currentValue.compareAndSet(current, current + 1)) {
                return current;
            }
        }
    }

    /**
     * Checks if buffer is exhausted
     * 检查缓冲区是否耗尽
     *
     * @return true if exhausted | 如果耗尽返回true
     */
    public boolean isExhausted() {
        return currentValue.get() >= maxValue;
    }

    /**
     * Checks if should preload next segment
     * 检查是否应预加载下一个号段
     *
     * @return true if should preload | 如果应预加载返回true
     */
    public boolean shouldPreload() {
        if (!initialized) {
            return false;
        }
        long current = currentValue.get();
        if (current < 0 || current >= maxValue) {
            return true; // Exhausted or overflowed, definitely preload
        }
        long remaining = maxValue - current;
        long threshold = (long) (step * preloadThreshold);
        return remaining <= threshold;
    }

    /**
     * Gets the current value
     * 获取当前值
     *
     * @return current value | 当前值
     */
    public long getCurrentValue() {
        return currentValue.get();
    }

    /**
     * Gets the max value
     * 获取最大值
     *
     * @return max value | 最大值
     */
    public long getMaxValue() {
        return maxValue;
    }

    /**
     * Gets the step size
     * 获取步长
     *
     * @return step size | 步长
     */
    public int getStep() {
        return step;
    }

    /**
     * Checks if initialized
     * 检查是否已初始化
     *
     * @return true if initialized | 如果已初始化返回true
     */
    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the usage percentage
     * 获取使用百分比
     *
     * @return usage percentage (0-100) | 使用百分比（0-100）
     */
    public int getUsagePercent() {
        if (!initialized || step == 0) {
            return 0;
        }
        long used = Math.max(0, currentValue.get() - (maxValue - step));
        return Math.min(100, (int) (used * 100 / step));
    }

    /**
     * Gets the remaining count
     * 获取剩余数量
     *
     * @return remaining count | 剩余数量
     */
    public long getRemaining() {
        return Math.max(0, maxValue - currentValue.get());
    }

    @Override
    public String toString() {
        return String.format("SegmentBuffer{current=%d, max=%d, step=%d, initialized=%s}",
                currentValue.get(), maxValue, step, initialized);
    }
}

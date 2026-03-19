package cloud.opencode.base.id.segment;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Memory-based Segment Allocator
 * 基于内存的号段分配器
 *
 * <p>A simple in-memory implementation of SegmentAllocator.
 * Useful for testing, development, and single-node scenarios.</p>
 * <p>SegmentAllocator的简单内存实现。
 * 适用于测试、开发和单节点场景。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>No external dependencies - 无外部依赖</li>
 *   <li>Thread-safe - 线程安全</li>
 *   <li>Multiple business tags support - 多业务标识支持</li>
 *   <li>Configurable starting value and step - 可配置起始值和步长</li>
 * </ul>
 *
 * <p><strong>Limitations | 限制:</strong></p>
 * <ul>
 *   <li>Not persistent - data lost on restart | 非持久化 - 重启后数据丢失</li>
 *   <li>Not suitable for distributed systems | 不适用于分布式系统</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic usage
 * SegmentAllocator allocator = MemorySegmentAllocator.create();
 * IdGenerator<Long> gen = SegmentIdGenerator.create(allocator, "order");
 *
 * // Custom step
 * SegmentAllocator allocator = MemorySegmentAllocator.create(5000);
 *
 * // Custom start and step
 * SegmentAllocator allocator = MemorySegmentAllocator.create(10000, 1000);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.1.0
 */
public final class MemorySegmentAllocator implements SegmentAllocator {

    private final int step;
    private final long startValue;
    private final Map<String, AtomicLong> counters;

    private MemorySegmentAllocator(long startValue, int step) {
        if (startValue < 0) {
            throw new IllegalArgumentException("Start value must be non-negative: " + startValue);
        }
        if (step <= 0) {
            throw new IllegalArgumentException("Step must be positive: " + step);
        }
        this.startValue = startValue;
        this.step = step;
        this.counters = new ConcurrentHashMap<>();
    }

    /**
     * Creates a memory allocator with default settings
     * 使用默认设置创建内存分配器
     *
     * @return allocator | 分配器
     */
    public static MemorySegmentAllocator create() {
        return new MemorySegmentAllocator(0, DEFAULT_STEP);
    }

    /**
     * Creates a memory allocator with custom step
     * 使用自定义步长创建内存分配器
     *
     * @param step the step size | 步长
     * @return allocator | 分配器
     */
    public static MemorySegmentAllocator create(int step) {
        return new MemorySegmentAllocator(0, step);
    }

    /**
     * Creates a memory allocator with custom start value and step
     * 使用自定义起始值和步长创建内存分配器
     *
     * @param startValue the starting value | 起始值
     * @param step       the step size | 步长
     * @return allocator | 分配器
     */
    public static MemorySegmentAllocator create(long startValue, int step) {
        return new MemorySegmentAllocator(startValue, step);
    }

    @Override
    public Segment allocate(String bizTag) {
        AtomicLong counter = counters.computeIfAbsent(bizTag,
                _ -> new AtomicLong(startValue));

        long start = counter.getAndAdd(step);
        long end = start + step;

        return new Segment(start, end, step);
    }

    @Override
    public int getStep() {
        return step;
    }

    /**
     * Gets the current value for a business tag
     * 获取业务标识的当前值
     *
     * @param bizTag the business tag | 业务标识
     * @return current value, or startValue if not allocated | 当前值，如果未分配则返回起始值
     */
    public long getCurrentValue(String bizTag) {
        AtomicLong counter = counters.get(bizTag);
        return counter != null ? counter.get() : startValue;
    }

    /**
     * Resets the counter for a business tag
     * 重置业务标识的计数器
     *
     * @param bizTag the business tag | 业务标识
     */
    public void reset(String bizTag) {
        counters.remove(bizTag);
    }

    /**
     * Resets all counters
     * 重置所有计数器
     */
    public void resetAll() {
        counters.clear();
    }

    /**
     * Gets the number of registered business tags
     * 获取已注册的业务标识数量
     *
     * @return count | 数量
     */
    public int getTagCount() {
        return counters.size();
    }
}

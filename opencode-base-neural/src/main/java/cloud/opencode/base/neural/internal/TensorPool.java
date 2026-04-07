package cloud.opencode.base.neural.internal;

import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Tensor Buffer Object Pool
 * 张量缓冲区对象池
 *
 * <p>Manages reusable float arrays organized by size buckets to reduce GC pressure
 * during neural network inference. When a tensor operation completes, its backing
 * array can be returned to the pool for reuse by subsequent operations.</p>
 * <p>按大小桶管理可复用的浮点数组，以减少神经网络推理过程中的 GC 压力。
 * 当张量操作完成后，其底层数组可以归还到池中供后续操作复用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Size-bucketed array pooling for zero-allocation inference - 按大小分桶的数组池化，实现零分配推理</li>
 *   <li>Bounded pool size per bucket to limit memory usage - 每桶有界池大小以限制内存使用</li>
 *   <li>Lock-free concurrent access via ConcurrentLinkedDeque - 通过 ConcurrentLinkedDeque 实现无锁并发访问</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap + ConcurrentLinkedDeque) - 线程安全: 是</li>
 *   <li>Arrays are not zeroed on release (caller responsibility) - 归还时不清零数组（调用方职责）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-neural V1.0.0
 */
public final class TensorPool {

    private final ConcurrentHashMap<Integer, Deque<float[]>> pool = new ConcurrentHashMap<>();
    private final int maxPoolSize;

    /**
     * Default maximum number of arrays per size bucket.
     * 每个大小桶的默认最大数组数量。
     */
    private static final int DEFAULT_MAX_POOL_SIZE = 64;

    /**
     * Create a TensorPool with the specified maximum pool size per bucket.
     * 创建指定每桶最大池大小的 TensorPool。
     *
     * @param maxPoolSize maximum number of arrays to keep per size bucket | 每个大小桶保留的最大数组数量
     * @throws IllegalArgumentException if maxPoolSize is not positive | 如果 maxPoolSize 非正数
     */
    public TensorPool(int maxPoolSize) {
        if (maxPoolSize <= 0) {
            throw new IllegalArgumentException("maxPoolSize must be positive: " + maxPoolSize);
        }
        this.maxPoolSize = maxPoolSize;
    }

    /**
     * Create a TensorPool with the default maximum pool size (64 per bucket).
     * 创建默认最大池大小（每桶64个）的 TensorPool。
     */
    public TensorPool() {
        this(DEFAULT_MAX_POOL_SIZE);
    }

    /**
     * Acquire a float array of the given size.
     * 获取指定大小的浮点数组。
     *
     * <p>Attempts to reuse a pooled array of the exact size. If none is available,
     * a new array is allocated.</p>
     * <p>尝试复用池中精确大小的数组。如果没有可用的，则分配新数组。</p>
     *
     * @param size the required array length | 所需的数组长度
     * @return a float array of the specified size | 指定大小的浮点数组
     * @throws IllegalArgumentException if size is negative | 如果 size 为负数
     */
    public float[] acquire(int size) {
        if (size < 0) {
            throw new IllegalArgumentException("size must not be negative: " + size);
        }
        Deque<float[]> bucket = pool.get(size);
        if (bucket != null) {
            float[] array = bucket.pollFirst();
            if (array != null) {
                // Zero out pooled arrays to prevent data leakage between inference requests
                java.util.Arrays.fill(array, 0.0f);
                return array;
            }
        }
        return new float[size];
    }

    /**
     * Return a float array to the pool for future reuse.
     * 将浮点数组归还到池中以供将来复用。
     *
     * <p>If the bucket for this array's size is already at capacity, the array is
     * discarded and left to be garbage collected.</p>
     * <p>如果该数组大小对应的桶已满，数组将被丢弃并交给垃圾回收器处理。</p>
     *
     * @param buffer the array to return to the pool | 要归还到池中的数组
     * @throws IllegalArgumentException if buffer is null | 如果 buffer 为 null
     */
    public void release(float[] buffer) {
        if (buffer == null) {
            throw new IllegalArgumentException("buffer must not be null");
        }
        int size = buffer.length;
        Deque<float[]> bucket = pool.computeIfAbsent(size, k -> new ConcurrentLinkedDeque<>());
        // Best-effort size limit: concurrent releases may briefly exceed maxPoolSize.
        // This is acceptable as the pool is a performance optimization, not a hard memory bound.
        if (bucket.size() < maxPoolSize) {
            bucket.offerFirst(buffer);
        }
        // else: discard, let GC collect
    }

    /**
     * Clear all pooled arrays from all buckets.
     * 清除所有桶中的所有已池化数组。
     */
    public void clear() {
        pool.clear();
    }

    /**
     * Get the total number of pooled arrays across all buckets (for diagnostics).
     * 获取所有桶中已池化数组的总数（用于诊断）。
     *
     * @return the total count of pooled arrays | 已池化数组的总数
     */
    public int pooledCount() {
        int count = 0;
        for (Deque<float[]> bucket : pool.values()) {
            count += bucket.size();
        }
        return count;
    }
}

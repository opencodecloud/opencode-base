package cloud.opencode.base.cache.internal.eviction;

import cloud.opencode.base.cache.model.CacheEntry;
import cloud.opencode.base.cache.spi.EvictionPolicy;

import java.util.*;

/**
 * W-TinyLFU Eviction Policy - Window TinyLFU eviction strategy
 * W-TinyLFU 淘汰策略 - 窗口 TinyLFU 淘汰策略
 *
 * <p>High-performance eviction policy combining recency and frequency.</p>
 * <p>高性能淘汰策略，结合新鲜度和频率。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Window for new entries - 新条目窗口区</li>
 *   <li>Frequency sketch (Count-Min) - 频率估计（Count-Min）</li>
 *   <li>Protected/Probation segments - 保护区/试用区</li>
 *   <li>High hit rate - 高命中率</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Thread Safety Contract | 线程安全约定:</strong></p>
 * <p>This class is <strong>NOT</strong> thread-safe. All methods that mutate internal state
 * ({@code recordAccess}, {@code recordWrite}, {@code selectVictim}, {@code onRemoval},
 * {@code reset}) must be called while holding an external lock. The {@code DefaultCache}
 * class satisfies this requirement by synchronizing all cache operations that invoke
 * eviction policy methods. Direct usage of this class from multiple threads without
 * external synchronization will result in data corruption.</p>
 * <p>此类<strong>不是</strong>线程安全的。所有修改内部状态的方法（{@code recordAccess}、
 * {@code recordWrite}、{@code selectVictim}、{@code onRemoval}、{@code reset}）
 * 必须在持有外部锁的情况下调用。{@code DefaultCache} 类通过同步所有调用淘汰策略方法的
 * 缓存操作来满足此要求。在没有外部同步的情况下从多个线程直接使用此类将导致数据损坏。</p>
 *
 * @param <K> the type of keys | 键类型
 * @param <V> the type of values | 值类型
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default sketch size (expectedSize=10000)
 * EvictionPolicy<String, User> policy = EvictionPolicy.wTinyLfu();
 *
 * // Sized to match cache capacity
 * EvictionPolicy<String, User> policy = EvictionPolicy.wTinyLfu(100_000);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class WTinyLfuEvictionPolicy<K, V> implements EvictionPolicy<K, V> {

    /** Default expected size used when no size is specified | 未指定大小时使用的默认期望大小 */
    private static final int DEFAULT_EXPECTED_SIZE = 10_000;

    /** Minimum sketch width (power of 2) | 最小 sketch 宽度（2 的幂） */
    private static final int MIN_SKETCH_WIDTH = 1024;

    // Window size ratio (1% of total)
    private static final double WINDOW_RATIO = 0.01;

    // Frequency sketch parameters
    private static final int SKETCH_DEPTH = 4;
    private static final int COUNTER_BITS = 4;
    private static final int COUNTER_MAX = (1 << COUNTER_BITS) - 1;
    // Per-row hash seeds to reduce hash collisions across depths
    private static final int[] HASH_SEEDS = {0x7feb352d, 0x846ca68b, 0x2c9277b5, 0x6ff2a933};

    /**
     * Sketch width, always a power of 2, derived from expectedSize.
     * sketch 宽度，始终为 2 的幂，根据 expectedSize 计算。
     */
    private final int sketchWidth;

    /**
     * Bit-mask for fast modulo: {@code sketchWidth - 1}.
     * 快速取模的位掩码：{@code sketchWidth - 1}。
     */
    private final int sketchMask;

    // Frequency sketch (Count-Min Sketch) - NOT thread-safe, requires external sync
    private final long[][] sketch;

    /**
     * Creates a new WTinyLfuEvictionPolicy with the default expected size (10,000).
     * 使用默认期望大小（10,000）创建新的 WTinyLfuEvictionPolicy 实例。
     */
    public WTinyLfuEvictionPolicy() {
        this(DEFAULT_EXPECTED_SIZE);
    }

    /**
     * Creates a new WTinyLfuEvictionPolicy with a sketch sized for the given expected cache capacity.
     * 创建新的 WTinyLfuEvictionPolicy 实例，sketch 大小根据给定的期望缓存容量计算。
     *
     * <p>The sketch width is set to {@code max(1024, highestOneBit(expectedSize * 4))} to ensure
     * a low collision rate while remaining a power of two for fast modulo via bit-masking.</p>
     * <p>sketch 宽度设为 {@code max(1024, highestOneBit(expectedSize * 4))}，以确保低碰撞率，
     * 同时保持 2 的幂次方以便通过位掩码快速取模。</p>
     *
     * @param expectedSize the expected maximum number of cache entries | 期望的缓存最大条目数
     */
    public WTinyLfuEvictionPolicy(int expectedSize) {
        if (expectedSize <= 0) {
            throw new IllegalArgumentException("expectedSize must be positive, got: " + expectedSize);
        }
        this.sketchWidth = computeSketchWidth(expectedSize);
        this.sketchMask = sketchWidth - 1;
        this.sketch = new long[SKETCH_DEPTH][sketchWidth];
    }

    // @GuardedBy("external lock") - Window segment (for new entries), NOT thread-safe
    private final LinkedHashSet<K> window = new LinkedHashSet<>();

    // @GuardedBy("external lock") - Main cache segments, NOT thread-safe
    private final LinkedHashSet<K> probation = new LinkedHashSet<>();
    private final LinkedHashSet<K> protected_ = new LinkedHashSet<>();

    // @GuardedBy("external lock") - Sample counter for aging
    private int sampleCount = 0;
    private static final int SAMPLE_THRESHOLD = 10_000;

    @Override
    public void recordAccess(CacheEntry<K, V> entry) {
        K key = entry.key();
        incrementFrequency(key);

        if (protected_.contains(key)) {
            // Already protected, just update access
            protected_.remove(key);
            protected_.add(key);
        } else if (probation.contains(key)) {
            // Promote from probation to protected
            probation.remove(key);
            protected_.add(key);
        } else if (window.contains(key)) {
            // Update window position
            window.remove(key);
            window.add(key);
        }
    }

    @Override
    public void recordWrite(CacheEntry<K, V> entry) {
        K key = entry.key();
        incrementFrequency(key);

        // New entries go to window
        if (!window.contains(key) && !probation.contains(key) && !protected_.contains(key)) {
            window.add(key);
        }
    }

    @Override
    public Optional<K> selectVictim(Map<K, CacheEntry<K, V>> entries) {
        // Use iterative loop instead of recursion to avoid stack overflow
        // when protected_ segment has many entries to demote
        for (int attempts = 0; attempts < 3; attempts++) {
            // First try window
            if (!window.isEmpty()) {
                K windowVictim = window.iterator().next();
                // Try to admit to main cache
                if (!probation.isEmpty()) {
                    K probationVictim = probation.iterator().next();
                    if (getFrequency(windowVictim) > getFrequency(probationVictim)) {
                        // Window victim is hotter, admit to probation
                        window.remove(windowVictim);
                        probation.remove(probationVictim);
                        probation.add(windowVictim);
                        return Optional.of(probationVictim);
                    }
                }
                return Optional.of(windowVictim);
            }

            // Then try probation
            if (!probation.isEmpty()) {
                return Optional.of(probation.iterator().next());
            }

            // Finally try protected - demote to probation and loop
            if (!protected_.isEmpty()) {
                K victim = protected_.iterator().next();
                // Demote to probation instead of evicting
                protected_.remove(victim);
                probation.add(victim);
                // Continue loop to re-evaluate with the demoted entry in probation
                continue;
            }

            // No entries in any segment
            break;
        }

        return entries.keySet().stream().findFirst();
    }

    @Override
    public void onRemoval(K key) {
        window.remove(key);
        probation.remove(key);
        protected_.remove(key);
    }

    @Override
    public void reset() {
        window.clear();
        probation.clear();
        protected_.clear();
        for (long[] row : sketch) {
            Arrays.fill(row, 0);
        }
        sampleCount = 0;
    }

    private void incrementFrequency(K key) {
        int baseHash = key.hashCode();
        for (int i = 0; i < SKETCH_DEPTH; i++) {
            int hash = spread(baseHash ^ HASH_SEEDS[i]);
            int index = hash & sketchMask;
            int counterOffset = (hash >>> 10) & 3;
            long current = (sketch[i][index] >>> (COUNTER_BITS * counterOffset)) & COUNTER_MAX;
            if (current < COUNTER_MAX) {
                sketch[i][index] += 1L << (COUNTER_BITS * counterOffset);
            }
        }

        // Age the sketch periodically
        if (++sampleCount >= SAMPLE_THRESHOLD) {
            sampleCount = 0;
            ageSketch();
        }
    }

    private int getFrequency(K key) {
        int baseHash = key.hashCode();
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < SKETCH_DEPTH; i++) {
            int hash = spread(baseHash ^ HASH_SEEDS[i]);
            int index = hash & sketchMask;
            int counterOffset = (hash >>> 10) & 3;
            int count = (int) ((sketch[i][index] >>> (COUNTER_BITS * counterOffset)) & COUNTER_MAX);
            min = Math.min(min, count);
        }
        return min;
    }

    private void ageSketch() {
        for (int i = 0; i < SKETCH_DEPTH; i++) {
            for (int j = 0; j < sketchWidth; j++) {
                sketch[i][j] = (sketch[i][j] >>> 1) & 0x7777777777777777L;
            }
        }
    }

    /**
     * Compute sketch width as a power of 2, sized proportionally to the expected cache capacity.
     * 计算 sketch 宽度为 2 的幂，与期望缓存容量成比例。
     *
     * @param expectedSize the expected cache capacity | 期望缓存容量
     * @return sketch width (power of 2, at least {@value MIN_SKETCH_WIDTH}) | sketch 宽度
     */
    private static int computeSketchWidth(int expectedSize) {
        // Use 4x the expected size to keep collision rate low, clamped to int range
        long raw = Math.min((long) expectedSize * 4L, Integer.MAX_VALUE);
        int highBit = Integer.highestOneBit((int) Math.max(raw, 1));
        return Math.max(MIN_SKETCH_WIDTH, highBit);
    }

    private static int spread(int h) {
        h ^= (h >>> 16);
        h *= 0x85ebca6b;
        h ^= (h >>> 13);
        h *= 0xc2b2ae35;
        h ^= (h >>> 16);
        return h;
    }
}

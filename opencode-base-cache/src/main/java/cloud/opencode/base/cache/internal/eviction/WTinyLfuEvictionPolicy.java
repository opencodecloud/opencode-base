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
 * EvictionPolicy<String, User> policy = EvictionPolicy.wTinyLfu();
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.0
 */
public final class WTinyLfuEvictionPolicy<K, V> implements EvictionPolicy<K, V> {

    // Window size ratio (1% of total)
    private static final double WINDOW_RATIO = 0.01;

    // Frequency sketch parameters
    private static final int SKETCH_DEPTH = 4;
    private static final int SKETCH_WIDTH = 1024;
    private static final int COUNTER_BITS = 4;
    private static final int COUNTER_MAX = (1 << COUNTER_BITS) - 1;

    // Frequency sketch (Count-Min Sketch) - NOT thread-safe, requires external sync
    private final long[][] sketch = new long[SKETCH_DEPTH][SKETCH_WIDTH];
    // Per-row hash seeds to reduce hash collisions across depths
    private static final int[] HASH_SEEDS = {0x7feb352d, 0x846ca68b, 0x2c9277b5, 0x6ff2a933};

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
            int index = hash & (SKETCH_WIDTH - 1);
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
            int index = hash & (SKETCH_WIDTH - 1);
            int counterOffset = (hash >>> 10) & 3;
            int count = (int) ((sketch[i][index] >>> (COUNTER_BITS * counterOffset)) & COUNTER_MAX);
            min = Math.min(min, count);
        }
        return min;
    }

    private void ageSketch() {
        for (int i = 0; i < SKETCH_DEPTH; i++) {
            for (int j = 0; j < SKETCH_WIDTH; j++) {
                sketch[i][j] = (sketch[i][j] >>> 1) & 0x7777777777777777L;
            }
        }
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

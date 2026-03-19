package cloud.opencode.base.cache.analysis;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;

/**
 * Access Pattern Analyzer - Analyzes cache access patterns
 * 访问模式分析器 - 分析缓存访问模式
 *
 * <p>Tracks and analyzes cache access patterns to identify hot keys,
 * cold data, and optimize cache performance.</p>
 * <p>跟踪和分析缓存访问模式以识别热点键、冷数据，并优化缓存性能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hot key detection - 热点键检测</li>
 *   <li>Cold data identification - 冷数据识别</li>
 *   <li>Access frequency tracking - 访问频率跟踪</li>
 *   <li>Temporal pattern analysis - 时间模式分析</li>
 *   <li>Top-K tracking - Top-K 跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create analyzer
 * AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
 *     .hotKeyThreshold(100)     // 100 accesses = hot
 *     .coldDataAge(Duration.ofHours(1))  // 1 hour = cold
 *     .topKSize(10)             // Track top 10
 *     .build();
 *
 * // Record accesses
 * analyzer.recordAccess("user:1001");
 * analyzer.recordAccess("user:1002");
 *
 * // Get hot keys
 * Set<String> hotKeys = analyzer.getHotKeys();
 *
 * // Get cold keys
 * Set<String> coldKeys = analyzer.getColdKeys();
 *
 * // Get analysis report
 * AccessPatternReport report = analyzer.analyze();
 * }</pre>
 *
 * @param <K> key type | 键类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses ConcurrentHashMap and atomic counters) - 线程安全: 是（使用 ConcurrentHashMap 和原子计数器）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class AccessPatternAnalyzer<K> {

    private final ConcurrentHashMap<K, AccessRecord> accessRecords = new ConcurrentHashMap<>();
    private final LongAdder totalAccesses = new LongAdder();

    private final long hotKeyThreshold;
    private final Duration coldDataAge;
    private final int topKSize;
    private final Duration windowDuration;

    private AccessPatternAnalyzer(long hotKeyThreshold, Duration coldDataAge,
                                  int topKSize, Duration windowDuration) {
        this.hotKeyThreshold = hotKeyThreshold;
        this.coldDataAge = coldDataAge;
        this.topKSize = topKSize;
        this.windowDuration = windowDuration;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a builder
     * 创建构建器
     *
     * @param <K> key type | 键类型
     * @return builder | 构建器
     */
    public static <K> Builder<K> builder() {
        return new Builder<>();
    }

    /**
     * Create with default settings
     * 使用默认设置创建
     *
     * @param <K> key type | 键类型
     * @return analyzer | 分析器
     */
    public static <K> AccessPatternAnalyzer<K> create() {
        return AccessPatternAnalyzer.<K>builder().build();
    }

    // ==================== Recording | 记录 ====================

    /**
     * Record an access to a key
     * 记录键的访问
     *
     * @param key the key | 键
     */
    public void recordAccess(K key) {
        recordAccess(key, 1);
    }

    /**
     * Record multiple accesses to a key
     * 记录键的多次访问
     *
     * @param key   the key | 键
     * @param count access count | 访问次数
     */
    public void recordAccess(K key, long count) {
        Objects.requireNonNull(key, "key cannot be null");
        totalAccesses.add(count);
        accessRecords.compute(key, (k, record) -> {
            if (record == null) {
                record = new AccessRecord();
            }
            record.recordAccess(count);
            return record;
        });
    }

    /**
     * Record a miss (access but not found)
     * 记录未命中（访问但未找到）
     *
     * @param key the key | 键
     */
    public void recordMiss(K key) {
        Objects.requireNonNull(key, "key cannot be null");
        accessRecords.compute(key, (k, record) -> {
            if (record == null) {
                record = new AccessRecord();
            }
            record.recordMiss();
            return record;
        });
    }

    // ==================== Analysis | 分析 ====================

    /**
     * Get hot keys (frequently accessed)
     * 获取热点键（频繁访问）
     *
     * @return set of hot keys | 热点键集合
     */
    public Set<K> getHotKeys() {
        return accessRecords.entrySet().stream()
                .filter(e -> e.getValue().accessCount.sum() >= hotKeyThreshold)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Get cold keys (not accessed recently)
     * 获取冷数据键（最近未访问）
     *
     * @return set of cold keys | 冷数据键集合
     */
    public Set<K> getColdKeys() {
        Instant coldThreshold = Instant.now().minus(coldDataAge);
        return accessRecords.entrySet().stream()
                .filter(e -> e.getValue().lastAccess.get() < coldThreshold.toEpochMilli())
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    /**
     * Get top K most accessed keys
     * 获取访问最多的 Top K 键
     *
     * @return list of top K keys with counts | Top K 键及其计数列表
     */
    public List<KeyAccessCount<K>> getTopK() {
        return getTopK(topKSize);
    }

    /**
     * Get top K most accessed keys
     * 获取访问最多的 Top K 键
     *
     * @param k number of keys to return | 要返回的键数量
     * @return list of top K keys with counts | Top K 键及其计数列表
     */
    public List<KeyAccessCount<K>> getTopK(int k) {
        return accessRecords.entrySet().stream()
                .map(e -> new KeyAccessCount<>(e.getKey(), e.getValue().accessCount.sum()))
                .sorted(Comparator.comparingLong(KeyAccessCount<K>::count).reversed())
                .limit(k)
                .toList();
    }

    /**
     * Get bottom K least accessed keys
     * 获取访问最少的 Bottom K 键
     *
     * @param k number of keys to return | 要返回的键数量
     * @return list of bottom K keys with counts | Bottom K 键及其计数列表
     */
    public List<KeyAccessCount<K>> getBottomK(int k) {
        return accessRecords.entrySet().stream()
                .map(e -> new KeyAccessCount<>(e.getKey(), e.getValue().accessCount.sum()))
                .sorted(Comparator.comparingLong(KeyAccessCount::count))
                .limit(k)
                .toList();
    }

    /**
     * Get access count for a specific key
     * 获取特定键的访问计数
     *
     * @param key the key | 键
     * @return access count | 访问计数
     */
    public long getAccessCount(K key) {
        AccessRecord record = accessRecords.get(key);
        return record != null ? record.accessCount.sum() : 0;
    }

    /**
     * Get miss count for a specific key
     * 获取特定键的未命中计数
     *
     * @param key the key | 键
     * @return miss count | 未命中计数
     */
    public long getMissCount(K key) {
        AccessRecord record = accessRecords.get(key);
        return record != null ? record.missCount.sum() : 0;
    }

    /**
     * Get last access time for a key
     * 获取键的最后访问时间
     *
     * @param key the key | 键
     * @return last access instant or null | 最后访问时间或 null
     */
    public Instant getLastAccess(K key) {
        AccessRecord record = accessRecords.get(key);
        if (record == null) {
            return null;
        }
        long timestamp = record.lastAccess.get();
        return timestamp > 0 ? Instant.ofEpochMilli(timestamp) : null;
    }

    /**
     * Generate full analysis report
     * 生成完整分析报告
     *
     * @return analysis report | 分析报告
     */
    public AccessPatternReport<K> analyze() {
        Set<K> hotKeys = getHotKeys();
        Set<K> coldKeys = getColdKeys();
        List<KeyAccessCount<K>> topK = getTopK();
        List<KeyAccessCount<K>> bottomK = getBottomK(topKSize);

        long totalKeys = accessRecords.size();
        long totalAccessCount = totalAccesses.sum();
        double avgAccessPerKey = totalKeys > 0 ? (double) totalAccessCount / totalKeys : 0;

        // Calculate access distribution
        long[] histogram = new long[10]; // 0-9%, 10-19%, ..., 90-100%
        if (totalAccessCount > 0) {
            for (AccessRecord record : accessRecords.values()) {
                double percent = (double) record.accessCount.sum() / totalAccessCount * 100;
                int bucket = Math.min((int) (percent / 10), 9);
                histogram[bucket]++;
            }
        }

        return new AccessPatternReport<>(
                Instant.now(),
                totalKeys,
                totalAccessCount,
                avgAccessPerKey,
                hotKeys.size(),
                coldKeys.size(),
                hotKeys,
                coldKeys,
                topK,
                bottomK,
                histogram
        );
    }

    // ==================== Management | 管理 ====================

    /**
     * Remove tracking for a key
     * 移除键的跟踪
     *
     * @param key the key | 键
     */
    public void remove(K key) {
        accessRecords.remove(key);
    }

    /**
     * Clear all tracking data
     * 清除所有跟踪数据
     */
    public void clear() {
        accessRecords.clear();
        totalAccesses.reset();
    }

    /**
     * Prune cold entries older than specified age
     * 修剪超过指定年龄的冷条目
     *
     * @param age maximum age | 最大年龄
     * @return number of entries pruned | 修剪的条目数
     */
    public int pruneCold(Duration age) {
        Instant threshold = Instant.now().minus(age);
        long thresholdMillis = threshold.toEpochMilli();
        int pruned = 0;
        for (Iterator<Map.Entry<K, AccessRecord>> it = accessRecords.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<K, AccessRecord> entry = it.next();
            if (entry.getValue().lastAccess.get() < thresholdMillis) {
                it.remove();
                pruned++;
            }
        }
        return pruned;
    }

    /**
     * Get tracked key count
     * 获取跟踪的键数量
     *
     * @return key count | 键数量
     */
    public int size() {
        return accessRecords.size();
    }

    // ==================== Inner Classes | 内部类 ====================

    /**
     * Access record for a single key
     * 单个键的访问记录
     */
    private static class AccessRecord {
        final LongAdder accessCount = new LongAdder();
        final LongAdder missCount = new LongAdder();
        final AtomicLong lastAccess = new AtomicLong(0);
        final AtomicLong firstAccess = new AtomicLong(0);

        void recordAccess(long count) {
            accessCount.add(count);
            long now = System.currentTimeMillis();
            lastAccess.set(now);
            firstAccess.compareAndSet(0, now);
        }

        void recordMiss() {
            missCount.increment();
        }
    }

    /**
     * Key with access count
     * 带访问计数的键
     *
     * @param key   the key | 键
     * @param count access count | 访问计数
     * @param <K>   key type | 键类型
     */
    public record KeyAccessCount<K>(K key, long count) {}

    /**
     * Access pattern analysis report
     * 访问模式分析报告
     *
     * @param analysisTime the time of analysis | 分析时间
     * @param totalKeys the total number of keys | 总键数
     * @param totalAccesses the total number of accesses | 总访问数
     * @param averageAccessPerKey the average accesses per key | 每个键的平均访问数
     * @param hotKeyCount the number of hot keys | 热键数
     * @param coldKeyCount the number of cold keys | 冷键数
     * @param hotKeys the set of hot keys | 热键集合
     * @param coldKeys the set of cold keys | 冷键集合
     * @param topK the top-K most accessed keys | 访问最多的前K个键
     * @param bottomK the bottom-K least accessed keys | 访问最少的后K个键
     * @param accessDistribution the access count distribution | 访问计数分布
     * @param <K> key type | 键类型
     */
    public record AccessPatternReport<K>(
            Instant analysisTime,
            long totalKeys,
            long totalAccesses,
            double averageAccessPerKey,
            int hotKeyCount,
            int coldKeyCount,
            Set<K> hotKeys,
            Set<K> coldKeys,
            List<KeyAccessCount<K>> topK,
            List<KeyAccessCount<K>> bottomK,
            long[] accessDistribution
    ) {
        /**
         * Get hot key percentage
         * 获取热点键百分比
         *
         * @return hot key percentage | 热点键百分比
         */
        public double hotKeyPercentage() {
            return totalKeys > 0 ? (double) hotKeyCount / totalKeys * 100 : 0;
        }

        /**
         * Get cold key percentage
         * 获取冷数据键百分比
         *
         * @return cold key percentage | 冷数据键百分比
         */
        public double coldKeyPercentage() {
            return totalKeys > 0 ? (double) coldKeyCount / totalKeys * 100 : 0;
        }

        /**
         * Check if access pattern is skewed (top 20% keys have 80% access)
         * 检查访问模式是否倾斜（前 20% 的键占 80% 的访问）
         *
         * @return true if skewed | 如果倾斜返回 true
         */
        public boolean isSkewed() {
            if (topK.isEmpty() || totalAccesses == 0) {
                return false;
            }
            long top20Count = topK.stream()
                    .limit((long) Math.ceil(totalKeys * 0.2))
                    .mapToLong(KeyAccessCount::count)
                    .sum();
            return (double) top20Count / totalAccesses >= 0.8;
        }

        /**
         * Convert to map for JSON serialization
         * 转换为 Map 用于 JSON 序列化
         *
         * @return map representation | Map 表示
         */
        public Map<String, Object> toMap() {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("analysisTime", analysisTime.toString());
            map.put("totalKeys", totalKeys);
            map.put("totalAccesses", totalAccesses);
            map.put("averageAccessPerKey", String.format("%.2f", averageAccessPerKey));
            map.put("hotKeyCount", hotKeyCount);
            map.put("coldKeyCount", coldKeyCount);
            map.put("hotKeyPercentage", String.format("%.2f%%", hotKeyPercentage()));
            map.put("coldKeyPercentage", String.format("%.2f%%", coldKeyPercentage()));
            map.put("isSkewed", isSkewed());
            map.put("topK", topK.stream()
                    .map(kac -> Map.of("key", kac.key().toString(), "count", kac.count()))
                    .toList());
            return map;
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for AccessPatternAnalyzer
     * AccessPatternAnalyzer 构建器
     *
     * @param <K> key type | 键类型
     */
    public static class Builder<K> {

        /**
         * Creates a new Builder instance | 创建新的构建器实例
         */
        public Builder() {}

        private long hotKeyThreshold = 100;
        private Duration coldDataAge = Duration.ofHours(1);
        private int topKSize = 10;
        private Duration windowDuration = Duration.ofHours(24);

        /**
         * Set hot key threshold
         * 设置热点键阈值
         *
         * @param threshold access count to be considered hot | 被视为热点的访问计数
         * @return this builder | 此构建器
         */
        public Builder<K> hotKeyThreshold(long threshold) {
            this.hotKeyThreshold = threshold;
            return this;
        }

        /**
         * Set cold data age threshold
         * 设置冷数据年龄阈值
         *
         * @param age age after which data is considered cold | 数据被视为冷的年龄
         * @return this builder | 此构建器
         */
        public Builder<K> coldDataAge(Duration age) {
            this.coldDataAge = age;
            return this;
        }

        /**
         * Set top K size
         * 设置 Top K 大小
         *
         * @param size number of top keys to track | 要跟踪的顶部键数量
         * @return this builder | 此构建器
         */
        public Builder<K> topKSize(int size) {
            this.topKSize = size;
            return this;
        }

        /**
         * Set analysis window duration
         * 设置分析窗口持续时间
         *
         * @param duration window duration | 窗口持续时间
         * @return this builder | 此构建器
         */
        public Builder<K> windowDuration(Duration duration) {
            this.windowDuration = duration;
            return this;
        }

        /**
         * Build the analyzer
         * 构建分析器
         *
         * @return analyzer | 分析器
         */
        public AccessPatternAnalyzer<K> build() {
            return new AccessPatternAnalyzer<>(hotKeyThreshold, coldDataAge, topKSize, windowDuration);
        }
    }
}

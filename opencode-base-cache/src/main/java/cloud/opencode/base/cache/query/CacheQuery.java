package cloud.opencode.base.cache.query;

import cloud.opencode.base.cache.Cache;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Cache Query - Fluent API for querying cache entries
 * 缓存查询 - 用于查询缓存条目的流式 API
 *
 * <p>Provides a fluent builder pattern for querying cache entries
 * with filtering, pagination, and projection capabilities.</p>
 * <p>提供用于查询缓存条目的流式构建器模式，
 * 具有过滤、分页和投影功能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Key pattern matching - 键模式匹配</li>
 *   <li>Value predicate filtering - 值谓词过滤</li>
 *   <li>Range queries - 范围查询</li>
 *   <li>Pagination - 分页</li>
 *   <li>Sorting - 排序</li>
 *   <li>Projection - 投影</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Simple key prefix query
 * List<User> users = CacheQuery.from(cache)
 *     .keyPrefix("user:")
 *     .values();
 *
 * // Complex query with filtering and pagination
 * CacheQuery.Result<String, User> result = CacheQuery.from(cache)
 *     .keyPattern("user:*")
 *     .valueFilter(user -> user.isActive())
 *     .orderByKey()
 *     .skip(10)
 *     .limit(20)
 *     .execute();
 *
 * // Range query for comparable keys
 * List<Order> orders = CacheQuery.from(orderCache)
 *     .keyRange("order:2024-01-01", "order:2024-12-31")
 *     .values();
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable builder, delegates to thread-safe cache) - 线程安全: 是（不可变构建器，委托给线程安全的缓存）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class CacheQuery<K, V> {

    private final Cache<K, V> cache;
    private Predicate<K> keyFilter = k -> true;
    private Predicate<V> valueFilter = v -> true;
    private BiPredicate<K, V> entryFilter = (k, v) -> true;
    private Comparator<Map.Entry<K, V>> sorter = null;
    private long skip = 0;
    private long limit = Long.MAX_VALUE;

    private CacheQuery(Cache<K, V> cache) {
        this.cache = Objects.requireNonNull(cache, "cache cannot be null");
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create a query for a cache
     * 为缓存创建查询
     *
     * @param cache the cache to query | 要查询的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return cache query builder | 缓存查询构建器
     */
    public static <K, V> CacheQuery<K, V> from(Cache<K, V> cache) {
        return new CacheQuery<>(cache);
    }

    // ==================== Key Filters | 键过滤器 ====================

    /**
     * Filter by key predicate
     * 通过键谓词过滤
     *
     * @param predicate key predicate | 键谓词
     * @return this query | 此查询
     */
    public CacheQuery<K, V> keyFilter(Predicate<K> predicate) {
        this.keyFilter = this.keyFilter.and(predicate);
        return this;
    }

    /**
     * Filter by key prefix (for String keys)
     * 通过键前缀过滤（用于 String 键）
     *
     * @param prefix key prefix | 键前缀
     * @return this query | 此查询
     */
    public CacheQuery<K, V> keyPrefix(String prefix) {
        return keyFilter(k -> k.toString().startsWith(prefix));
    }

    /**
     * Filter by key suffix (for String keys)
     * 通过键后缀过滤（用于 String 键）
     *
     * @param suffix key suffix | 键后缀
     * @return this query | 此查询
     */
    public CacheQuery<K, V> keySuffix(String suffix) {
        return keyFilter(k -> k.toString().endsWith(suffix));
    }

    /**
     * Filter by key pattern (glob-style, for String keys)
     * 通过键模式过滤（glob 风格，用于 String 键）
     *
     * @param pattern glob pattern (e.g., "user:*:profile") | glob 模式
     * @return this query | 此查询
     */
    public CacheQuery<K, V> keyPattern(String pattern) {
        String regex = pattern
                .replace(".", "\\.")
                .replace("*", ".*")
                .replace("?", ".");
        java.util.regex.Pattern compiled = java.util.regex.Pattern.compile(regex);
        return keyFilter(k -> compiled.matcher(k.toString()).matches());
    }

    /**
     * Filter by key regex pattern (for String keys)
     * 通过键正则表达式模式过滤（用于 String 键）
     *
     * @param regex regex pattern | 正则表达式模式
     * @return this query | 此查询
     */
    public CacheQuery<K, V> keyRegex(String regex) {
        java.util.regex.Pattern compiled = java.util.regex.Pattern.compile(regex);
        return keyFilter(k -> compiled.matcher(k.toString()).matches());
    }

    /**
     * Filter by key range (for Comparable keys)
     * 通过键范围过滤（用于 Comparable 键）
     *
     * @param fromKey start key (inclusive) | 起始键（包含）
     * @param toKey   end key (exclusive) | 结束键（不包含）
     * @return this query | 此查询
     */
    @SuppressWarnings("unchecked")
    public CacheQuery<K, V> keyRange(K fromKey, K toKey) {
        return keyFilter(k -> {
            if (k instanceof Comparable) {
                Comparable<K> ck = (Comparable<K>) k;
                return ck.compareTo(fromKey) >= 0 && ck.compareTo(toKey) < 0;
            }
            return true;
        });
    }

    /**
     * Filter by key set
     * 通过键集合过滤
     *
     * @param keys set of keys to include | 要包含的键集合
     * @return this query | 此查询
     */
    public CacheQuery<K, V> keyIn(Set<K> keys) {
        return keyFilter(keys::contains);
    }

    /**
     * Exclude specific keys
     * 排除特定键
     *
     * @param keys set of keys to exclude | 要排除的键集合
     * @return this query | 此查询
     */
    public CacheQuery<K, V> keyNotIn(Set<K> keys) {
        return keyFilter(k -> !keys.contains(k));
    }

    // ==================== Value Filters | 值过滤器 ====================

    /**
     * Filter by value predicate
     * 通过值谓词过滤
     *
     * @param predicate value predicate | 值谓词
     * @return this query | 此查询
     */
    public CacheQuery<K, V> valueFilter(Predicate<V> predicate) {
        this.valueFilter = this.valueFilter.and(predicate);
        return this;
    }

    /**
     * Filter by entry predicate (key and value)
     * 通过条目谓词过滤（键和值）
     *
     * @param predicate entry predicate | 条目谓词
     * @return this query | 此查询
     */
    public CacheQuery<K, V> entryFilter(BiPredicate<K, V> predicate) {
        this.entryFilter = this.entryFilter.and(predicate);
        return this;
    }

    /**
     * Filter non-null values only
     * 仅过滤非 null 值
     *
     * @return this query | 此查询
     */
    public CacheQuery<K, V> nonNull() {
        return valueFilter(Objects::nonNull);
    }

    // ==================== Sorting | 排序 ====================

    /**
     * Order by key (ascending, requires Comparable keys)
     * 按键排序（升序，需要 Comparable 键）
     *
     * @return this query | 此查询
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CacheQuery<K, V> orderByKey() {
        this.sorter = (e1, e2) -> ((Comparable) e1.getKey()).compareTo(e2.getKey());
        return this;
    }

    /**
     * Order by key descending (requires Comparable keys)
     * 按键降序排序（需要 Comparable 键）
     *
     * @return this query | 此查询
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public CacheQuery<K, V> orderByKeyDesc() {
        this.sorter = (e1, e2) -> ((Comparable) e2.getKey()).compareTo(e1.getKey());
        return this;
    }

    /**
     * Order by custom comparator
     * 按自定义比较器排序
     *
     * @param comparator entry comparator | 条目比较器
     * @return this query | 此查询
     */
    public CacheQuery<K, V> orderBy(Comparator<Map.Entry<K, V>> comparator) {
        this.sorter = comparator;
        return this;
    }

    // ==================== Pagination | 分页 ====================

    /**
     * Skip first N results
     * 跳过前 N 个结果
     *
     * @param count number to skip | 要跳过的数量
     * @return this query | 此查询
     */
    public CacheQuery<K, V> skip(long count) {
        this.skip = count;
        return this;
    }

    /**
     * Limit results to N
     * 将结果限制为 N 个
     *
     * @param count maximum results | 最大结果数
     * @return this query | 此查询
     */
    public CacheQuery<K, V> limit(long count) {
        this.limit = count;
        return this;
    }

    /**
     * Paginate results
     * 分页结果
     *
     * @param page     page number (0-based) | 页码（从 0 开始）
     * @param pageSize page size | 页大小
     * @return this query | 此查询
     */
    public CacheQuery<K, V> page(int page, int pageSize) {
        this.skip = (long) page * pageSize;
        this.limit = pageSize;
        return this;
    }

    // ==================== Execution | 执行 ====================

    /**
     * Execute query and return result
     * 执行查询并返回结果
     *
     * @return query result | 查询结果
     */
    public Result<K, V> execute() {
        long startTime = System.nanoTime();

        Stream<Map.Entry<K, V>> stream = cache.entries().stream()
                .filter(e -> keyFilter.test(e.getKey()))
                .filter(e -> valueFilter.test(e.getValue()))
                .filter(e -> entryFilter.test(e.getKey(), e.getValue()));

        if (sorter != null) {
            stream = stream.sorted(sorter);
        }

        List<Map.Entry<K, V>> entries = stream
                .skip(skip)
                .limit(limit)
                .toList();

        long executionTimeNanos = System.nanoTime() - startTime;

        return new Result<>(entries, cache.size(), skip, limit, executionTimeNanos);
    }

    /**
     * Execute and return only keys
     * 执行并仅返回键
     *
     * @return list of matching keys | 匹配键列表
     */
    public List<K> keys() {
        return execute().keys();
    }

    /**
     * Execute and return only values
     * 执行并仅返回值
     *
     * @return list of matching values | 匹配值列表
     */
    public List<V> values() {
        return execute().values();
    }

    /**
     * Execute and return as map
     * 执行并作为 Map 返回
     *
     * @return map of matching entries | 匹配条目的 Map
     */
    public Map<K, V> toMap() {
        return execute().toMap();
    }

    /**
     * Execute and return count
     * 执行并返回计数
     *
     * @return count of matching entries | 匹配条目的计数
     */
    public long count() {
        return cache.entries().stream()
                .filter(e -> keyFilter.test(e.getKey()))
                .filter(e -> valueFilter.test(e.getValue()))
                .filter(e -> entryFilter.test(e.getKey(), e.getValue()))
                .count();
    }

    /**
     * Execute and return first match
     * 执行并返回第一个匹配项
     *
     * @return optional first entry | 可选的第一个条目
     */
    public Optional<Map.Entry<K, V>> first() {
        return cache.entries().stream()
                .filter(e -> keyFilter.test(e.getKey()))
                .filter(e -> valueFilter.test(e.getValue()))
                .filter(e -> entryFilter.test(e.getKey(), e.getValue()))
                .findFirst();
    }

    /**
     * Check if any entry matches
     * 检查是否有任何条目匹配
     *
     * @return true if any match | 如果有匹配返回 true
     */
    public boolean exists() {
        return cache.entries().stream()
                .filter(e -> keyFilter.test(e.getKey()))
                .filter(e -> valueFilter.test(e.getValue()))
                .filter(e -> entryFilter.test(e.getKey(), e.getValue()))
                .findAny()
                .isPresent();
    }

    /**
     * Return as stream for custom processing
     * 返回为 Stream 以进行自定义处理
     *
     * @return stream of matching entries | 匹配条目的 Stream
     */
    public Stream<Map.Entry<K, V>> stream() {
        Stream<Map.Entry<K, V>> stream = cache.entries().stream()
                .filter(e -> keyFilter.test(e.getKey()))
                .filter(e -> valueFilter.test(e.getValue()))
                .filter(e -> entryFilter.test(e.getKey(), e.getValue()));

        if (sorter != null) {
            stream = stream.sorted(sorter);
        }

        return stream.skip(skip).limit(limit);
    }

    // ==================== Result Class | 结果类 ====================

    /**
     * Query result containing matched entries and metadata
     * 包含匹配条目和元数据的查询结果
     *
     * @param entries the matched entries | 匹配的条目
     * @param totalInCache the total entries in cache | 缓存中的总条目数
     * @param skipped the number of skipped entries | 跳过的条目数
     * @param limit the query limit | 查询限制
     * @param executionTimeNanos the execution time in nanoseconds | 执行时间（纳秒）
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public record Result<K, V>(
            List<Map.Entry<K, V>> entries,
            long totalInCache,
            long skipped,
            long limit,
            long executionTimeNanos
    ) {
        /**
         * Get result size
         * 获取结果大小
         *
         * @return result size | 结果大小
         */
        public int size() {
            return entries.size();
        }

        /**
         * Check if empty
         * 检查是否为空
         *
         * @return true if empty | 如果为空返回 true
         */
        public boolean isEmpty() {
            return entries.isEmpty();
        }

        /**
         * Get keys only
         * 仅获取键
         *
         * @return list of keys | 键列表
         */
        public List<K> keys() {
            return entries.stream().map(Map.Entry::getKey).toList();
        }

        /**
         * Get values only
         * 仅获取值
         *
         * @return list of values | 值列表
         */
        public List<V> values() {
            return entries.stream().map(Map.Entry::getValue).toList();
        }

        /**
         * Convert to map
         * 转换为 Map
         *
         * @return map of entries | 条目的 Map
         */
        public Map<K, V> toMap() {
            Map<K, V> map = new LinkedHashMap<>();
            for (Map.Entry<K, V> entry : entries) {
                map.put(entry.getKey(), entry.getValue());
            }
            return map;
        }

        /**
         * Get execution time in milliseconds
         * 获取执行时间（毫秒）
         *
         * @return execution time ms | 执行时间毫秒
         */
        public double executionTimeMs() {
            return executionTimeNanos / 1_000_000.0;
        }

        /**
         * Check if there are more results
         * 检查是否有更多结果
         *
         * @return true if more results exist | 如果有更多结果返回 true
         */
        public boolean hasMore() {
            return entries.size() >= limit;
        }
    }
}

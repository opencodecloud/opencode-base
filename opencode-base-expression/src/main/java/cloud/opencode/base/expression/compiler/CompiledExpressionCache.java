package cloud.opencode.base.expression.compiler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Compiled Expression Cache
 * 编译表达式缓存
 *
 * <p>Caches compiled expressions for improved performance.
 * Uses LRU-style eviction when the cache reaches its maximum size.</p>
 * <p>缓存编译后的表达式以提高性能。当缓存达到最大容量时使用 LRU 式淘汰。</p>
 *
 * <h2>Usage | 用法</h2>
 * <pre>{@code
 * CompiledExpressionCache cache = new CompiledExpressionCache(1000);
 *
 * // Get or compile expression
 * CompiledExpression expr = cache.getOrCompile("a + b", str -> CompiledExpression.compile(str));
 * }</pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LRU-style eviction when capacity is reached - 达到容量时LRU式淘汰</li>
 *   <li>Thread-safe with ConcurrentHashMap and ReentrantLock - 使用ConcurrentHashMap和ReentrantLock实现线程安全</li>
 *   <li>Global singleton instance and custom instances - 全局单例实例和自定义实例</li>
 *   <li>Cache statistics with utilization percentage - 缓存统计信息与使用率百分比</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, ConcurrentHashMap with ReentrantLock for writes - 线程安全: 是，ConcurrentHashMap配合ReentrantLock写入</li>
 *   <li>Null-safe: No, null keys not supported - 空值安全: 否，不支持null键</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CompiledExpressionCache cache = new CompiledExpressionCache(500);
 * CompiledExpression expr = cache.getOrCompile("a + b", CompiledExpression::compile);
 *
 * // Global singleton
 * CompiledExpression expr2 = CompiledExpressionCache.global().getOrCompile("x * y", CompiledExpression::compile);
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public class CompiledExpressionCache {

    private static final CompiledExpressionCache GLOBAL = new CompiledExpressionCache(10000);

    private final Map<String, CacheEntry> cache;
    private final int maxSize;
    private final ReentrantLock writeLock = new ReentrantLock();

    /**
     * Create cache with default size (1000)
     * 使用默认大小（1000）创建缓存
     */
    public CompiledExpressionCache() {
        this(1000);
    }

    /**
     * Create cache with specified max size
     * 使用指定最大大小创建缓存
     *
     * @param maxSize the maximum cache size | 最大缓存大小
     */
    public CompiledExpressionCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Get the global cache instance
     * 获取全局缓存实例
     *
     * @return the global cache | 全局缓存
     */
    public static CompiledExpressionCache global() {
        return GLOBAL;
    }

    /**
     * Create a new cache
     * 创建新缓存
     *
     * @param maxSize the max size | 最大大小
     * @return the cache | 缓存
     */
    public static CompiledExpressionCache create(int maxSize) {
        return new CompiledExpressionCache(maxSize);
    }

    /**
     * Get or compile expression
     * 获取或编译表达式
     *
     * @param expression the expression string | 表达式字符串
     * @param compiler the compiler function | 编译器函数
     * @return the compiled expression | 编译后的表达式
     */
    public CompiledExpression getOrCompile(String expression,
            Function<String, CompiledExpression> compiler) {
        // Fast path: check without locking
        CacheEntry existing = cache.get(expression);
        if (existing != null) {
            existing.access();
            return existing.expression;
        }
        // Slow path: synchronize size-check-and-insert to avoid TOCTOU race
        writeLock.lock();
        try {
            // Double-check after acquiring lock
            CacheEntry entry = cache.get(expression);
            if (entry != null) {
                entry.access();
                return entry.expression;
            }
            if (cache.size() >= maxSize) {
                evictOldest();
            }
            entry = new CacheEntry(compiler.apply(expression));
            cache.put(expression, entry);
            return entry.expression;
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Get compiled expression
     * 获取编译后的表达式
     *
     * @param expression the expression string | 表达式字符串
     * @return the compiled expression, or null if not cached | 编译后的表达式，如果未缓存则为 null
     */
    public CompiledExpression get(String expression) {
        CacheEntry entry = cache.get(expression);
        if (entry != null) {
            entry.access();
            return entry.expression;
        }
        return null;
    }

    /**
     * Put compiled expression in cache
     * 将编译后的表达式放入缓存
     *
     * @param expression the expression string | 表达式字符串
     * @param compiled the compiled expression | 编译后的表达式
     */
    public void put(String expression, CompiledExpression compiled) {
        writeLock.lock();
        try {
            if (cache.size() >= maxSize) {
                evictOldest();
            }
            cache.put(expression, new CacheEntry(compiled));
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Check if expression is cached
     * 检查表达式是否已缓存
     *
     * @param expression the expression string | 表达式字符串
     * @return true if cached | 如果已缓存返回 true
     */
    public boolean contains(String expression) {
        return cache.containsKey(expression);
    }

    /**
     * Remove expression from cache
     * 从缓存中移除表达式
     *
     * @param expression the expression string | 表达式字符串
     */
    public void remove(String expression) {
        cache.remove(expression);
    }

    /**
     * Clear the cache
     * 清除缓存
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Get current cache size
     * 获取当前缓存大小
     *
     * @return the size | 大小
     */
    public int size() {
        return cache.size();
    }

    /**
     * Get max cache size
     * 获取最大缓存大小
     *
     * @return the max size | 最大大小
     */
    public int maxSize() {
        return maxSize;
    }

    /**
     * Get cache stats
     * 获取缓存统计信息
     *
     * @return the stats | 统计信息
     */
    public CacheStats getStats() {
        return new CacheStats(cache.size(), maxSize);
    }

    private void evictOldest() {
        // Find and remove the entry with oldest last access time
        cache.entrySet().stream()
                .min((e1, e2) -> Long.compare(e1.getValue().lastAccess, e2.getValue().lastAccess))
                .ifPresent(e -> cache.remove(e.getKey()));
    }

    /**
     * Cache entry with access tracking
     * 带访问跟踪的缓存条目
     */
    private static class CacheEntry {
        final CompiledExpression expression;
        volatile long lastAccess;

        CacheEntry(CompiledExpression expression) {
            this.expression = expression;
            this.lastAccess = System.nanoTime();
        }

        void access() {
            this.lastAccess = System.nanoTime();
        }
    }

    /**
     * Cache statistics
     * 缓存统计信息
     *
     * @param size current size | 当前大小
     * @param maxSize max size | 最大大小
     */
    public record CacheStats(int size, int maxSize) {
        /**
         * Get utilization percentage
         * 获取使用率百分比
         *
         * @return utilization (0-100) | 使用率（0-100）
         */
        public double utilization() {
            return maxSize > 0 ? (size * 100.0 / maxSize) : 0;
        }
    }
}

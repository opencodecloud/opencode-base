package cloud.opencode.base.expression.compiler;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Expression Cache
 * 表达式缓存
 *
 * <p>Caches compiled expressions for improved performance.</p>
 * <p>缓存编译后的表达式以提高性能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auto-compile on cache miss - 缓存未命中时自动编译</li>
 *   <li>Quarter-size batch eviction strategy - 四分之一大小批量淘汰策略</li>
 *   <li>Thread-safe with ConcurrentHashMap - 使用ConcurrentHashMap实现线程安全</li>
 *   <li>Global singleton instance - 全局单例实例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * ExpressionCache cache = ExpressionCache.create(500);
 * CompiledExpression expr = cache.get("a + b");  // auto-compiles if not cached
 * boolean cached = cache.contains("a + b");  // true
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, ConcurrentHashMap with putIfAbsent - 线程安全: 是，ConcurrentHashMap配合putIfAbsent</li>
 *   <li>Null-safe: No, null expression not supported - 空值安全: 否，不支持null表达式</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class ExpressionCache {

    private static final ExpressionCache GLOBAL = new ExpressionCache(1000);

    private final Map<String, CompiledExpression> cache;
    private final int maxSize;

    /**
     * Create cache with max size
     * 创建具有最大容量的缓存
     *
     * @param maxSize the maximum cache size | 最大缓存大小
     */
    public ExpressionCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>();
    }

    /**
     * Get the global cache instance
     * 获取全局缓存实例
     *
     * @return the global cache | 全局缓存
     */
    public static ExpressionCache global() {
        return GLOBAL;
    }

    /**
     * Create a new cache
     * 创建新缓存
     *
     * @param maxSize the maximum size | 最大大小
     * @return the cache | 缓存
     */
    public static ExpressionCache create(int maxSize) {
        return new ExpressionCache(maxSize);
    }

    /**
     * Get or compile expression
     * 获取或编译表达式
     *
     * @param expression the expression string | 表达式字符串
     * @return the compiled expression | 编译后的表达式
     */
    public CompiledExpression get(String expression) {
        CompiledExpression cached = cache.get(expression);
        if (cached != null) {
            return cached;
        }
        // Compile first, then insert and evict if needed to avoid race condition
        CompiledExpression compiled = CompiledExpression.compile(expression);
        cached = cache.putIfAbsent(expression, compiled);
        if (cached != null) {
            return cached; // Another thread inserted first
        }
        // Evict after adding to avoid race between size check and computeIfAbsent
        if (cache.size() > maxSize) {
            evictOldest();
        }
        return compiled;
    }

    /**
     * Put compiled expression in cache
     * 将编译后的表达式放入缓存
     *
     * @param expression the expression string | 表达式字符串
     * @param compiled the compiled expression | 编译后的表达式
     */
    public void put(String expression, CompiledExpression compiled) {
        if (cache.size() >= maxSize) {
            evictOldest();
        }
        cache.put(expression, compiled);
    }

    /**
     * Check if expression is cached
     * 检查表达式是否已缓存
     *
     * @param expression the expression string | 表达式字符串
     * @return true if cached | 如果已缓存返回true
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
     * Get cache size
     * 获取缓存大小
     *
     * @return the current size | 当前大小
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

    private void evictOldest() {
        int toRemove = maxSize / 4;
        var iterator = cache.keySet().iterator();
        int removed = 0;
        while (iterator.hasNext() && removed < toRemove) {
            iterator.next();
            iterator.remove();
            removed++;
        }
    }
}

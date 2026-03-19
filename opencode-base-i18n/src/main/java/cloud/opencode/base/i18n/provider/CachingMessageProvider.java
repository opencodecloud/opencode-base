package cloud.opencode.base.i18n.provider;

import cloud.opencode.base.i18n.spi.MessageProvider;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Caching message provider wrapper
 * 带缓存的消息提供者包装器
 *
 * <p>Wraps another provider and adds caching functionality for improved performance.</p>
 * <p>包装其他提供者并添加缓存功能以提高性能。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Size-limited cache with TTL - 带大小限制和TTL的缓存</li>
 *   <li>Automatic expiry eviction - 自动过期驱逐</li>
 *   <li>Cache statistics - 缓存统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * MessageProvider delegate = new ResourceBundleProvider("messages");
 * CachingMessageProvider cached = new CachingMessageProvider(delegate, 1000, Duration.ofHours(1));
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
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class CachingMessageProvider implements MessageProvider {

    private final MessageProvider delegate;
    private final Map<CacheKey, CacheEntry> cache;
    private final int maxSize;
    private final Duration ttl;
    private final AtomicLong hits = new AtomicLong();
    private final AtomicLong misses = new AtomicLong();

    /**
     * Creates a caching provider with default settings
     * 使用默认设置创建缓存提供者
     *
     * @param delegate the delegate provider | 委托提供者
     */
    public CachingMessageProvider(MessageProvider delegate) {
        this(delegate, 1000, Duration.ofHours(1));
    }

    /**
     * Creates a caching provider with custom settings
     * 使用自定义设置创建缓存提供者
     *
     * @param delegate  the delegate provider | 委托提供者
     * @param cacheSize maximum cache size | 最大缓存大小
     * @param ttl       time to live | 存活时间
     */
    public CachingMessageProvider(MessageProvider delegate, int cacheSize, Duration ttl) {
        this.delegate = delegate;
        this.maxSize = cacheSize;
        this.ttl = ttl;
        // Use access-order LinkedHashMap for LRU eviction, wrapped with synchronization
        this.cache = Collections.synchronizedMap(
            new LinkedHashMap<>(cacheSize, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<CacheKey, CacheEntry> eldest) {
                    return size() > maxSize || eldest.getValue().isExpired();
                }
            }
        );
    }

    @Override
    public Optional<String> getMessageTemplate(String key, Locale locale) {
        CacheKey cacheKey = new CacheKey(key, locale);

        CacheEntry entry = cache.get(cacheKey);
        if (entry != null && !entry.isExpired()) {
            hits.incrementAndGet();
            return entry.value();
        }

        misses.incrementAndGet();
        Optional<String> value = delegate.getMessageTemplate(key, locale);

        // LRU eviction is handled automatically by LinkedHashMap.removeEldestEntry
        cache.put(cacheKey, new CacheEntry(value, Instant.now().plus(ttl)));
        return value;
    }

    @Override
    public boolean containsMessage(String key, Locale locale) {
        return getMessageTemplate(key, locale).isPresent();
    }

    @Override
    public Set<String> getKeys(Locale locale) {
        return delegate.getKeys(locale);
    }

    @Override
    public Set<Locale> getSupportedLocales() {
        return delegate.getSupportedLocales();
    }

    @Override
    public void refresh() {
        cache.clear();
        delegate.refresh();
        hits.set(0);
        misses.set(0);
    }

    /**
     * Gets the cache hit rate
     * 获取缓存命中率
     *
     * @return hit rate (0.0 - 1.0) | 命中率（0.0 - 1.0）
     */
    public double getHitRate() {
        long h = hits.get();
        long m = misses.get();
        long total = h + m;
        return total == 0 ? 0.0 : (double) h / total;
    }

    /**
     * Gets the current cache size
     * 获取当前缓存大小
     *
     * @return cache size | 缓存大小
     */
    public int getCacheSize() {
        return cache.size();
    }

    /**
     * Clears the cache
     * 清除缓存
     */
    public void clearCache() {
        cache.clear();
    }

    private record CacheKey(String key, Locale locale) {
    }

    private record CacheEntry(Optional<String> value, Instant expiry) {
        boolean isExpired() {
            return Instant.now().isAfter(expiry);
        }
    }
}

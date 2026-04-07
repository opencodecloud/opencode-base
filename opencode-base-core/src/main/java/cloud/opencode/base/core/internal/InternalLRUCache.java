package cloud.opencode.base.core.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Internal LRU Cache - Thread-safe LRU cache implementation
 * 内部 LRU 缓存 - 线程安全的 LRU 缓存实现
 *
 * <p>Thread-safe LRU cache using LinkedHashMap with ReadWriteLock. For internal use only.</p>
 * <p>使用 LinkedHashMap 和 ReadWriteLock 的线程安全 LRU 缓存。仅供内部使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>LRU eviction policy - LRU 淘汰策略</li>
 *   <li>Thread-safe (ReentrantReadWriteLock) - 线程安全</li>
 *   <li>Configurable max size - 可配置最大容量</li>
 *   <li>Standard cache operations - 标准缓存操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * InternalLRUCache<String, Object> cache = InternalLRUCache.create(100);
 * cache.put("key", value);
 * Object result = cache.computeIfAbsent("key", k -> compute(k));
 * int maxSize = cache.getMaxSize();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ReentrantReadWriteLock) - 线程安全: 是</li>
 *   <li>Internal API: Not for public use - 内部 API: 非公开使用</li>
 * </ul>
 *
 * @param <K> Key type - Key 类型
 * @param <V> Value type - Value 类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public class InternalLRUCache<K, V> implements InternalCache<K, V> {

    private final int maxSize;
    private final Map<K, V> cache;
    private final ReentrantLock lock = new ReentrantLock();

    InternalLRUCache(int maxSize) {
        this.maxSize = maxSize;
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > InternalLRUCache.this.maxSize;
            }
        };
    }

    /**
     * Creates
     * 创建 LRU 缓存
     */
    public static <K, V> InternalLRUCache<K, V> create(int maxSize) {
        return new InternalLRUCache<>(maxSize);
    }

    @Override
    public V get(K key) {
        // Must use writeLock because LinkedHashMap with accessOrder=true mutates on get()
        lock.lock();
        try {
            return cache.get(key);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @implNote Null values returned by the mapping function are not cached.
     * Subsequent calls with the same key will re-invoke the mapping function.
     * 映射函数返回 null 时不会缓存，后续相同 key 的调用会重新执行映射函数。
     */
    @Override
    public V computeIfAbsent(K key, Function<K, V> mappingFunction) {
        lock.lock();
        try {
            V value = cache.get(key);
            if (value == null) {
                value = mappingFunction.apply(key);
                if (value != null) {
                    cache.put(key, value);
                }
            }
            return value;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V put(K key, V value) {
        lock.lock();
        try {
            return cache.put(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V putIfAbsent(K key, V value) {
        lock.lock();
        try {
            return cache.putIfAbsent(key, value);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public V remove(K key) {
        lock.lock();
        try {
            return cache.remove(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean containsKey(K key) {
        lock.lock();
        try {
            return cache.containsKey(key);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int size() {
        lock.lock();
        try {
            return cache.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        lock.lock();
        try {
            cache.clear();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Gets
     * 获取最大容量
     */
    public int getMaxSize() {
        return maxSize;
    }
}

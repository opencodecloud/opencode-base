package cloud.opencode.base.cache;

import java.io.*;
import java.io.ObjectInputFilter;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Copy-on-Read Cache - Returns deep copies of cached values for thread safety
 * 读时复制缓存 - 返回缓存值的深拷贝以保证线程安全
 *
 * <p>Wraps an existing cache to return deep copies of values on read operations.
 * This prevents external modifications from affecting cached data.</p>
 * <p>包装现有缓存，在读取操作时返回值的深拷贝。这防止外部修改影响缓存数据。</p>
 *
 * <p><strong>Use Cases | 使用场景:</strong></p>
 * <ul>
 *   <li>Mutable cached objects that might be modified by callers | 可能被调用者修改的可变缓存对象</li>
 *   <li>Thread-safe access to cached collections | 线程安全访问缓存集合</li>
 *   <li>Isolation between cache consumers | 缓存消费者之间的隔离</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // With default deep copy (Java serialization)
 * Cache<String, User> cache = OpenCache.getOrCreate("users");
 * CopyOnReadCache<String, User> copyCache = CopyOnReadCache.wrap(cache).build();
 *
 * User user = copyCache.get("user:1");
 * user.setName("Modified");  // Does not affect cached value
 *
 * // With custom copier
 * CopyOnReadCache<String, Config> configCache = CopyOnReadCache.wrap(cache)
 *     .copier(config -> config.toBuilder().build())  // Use builder pattern
 *     .build();
 *
 * // With Jackson for JSON-based copy
 * ObjectMapper mapper = new ObjectMapper();
 * CopyOnReadCache<String, Data> dataCache = CopyOnReadCache.wrap(cache)
 *     .copier(data -> mapper.readValue(mapper.writeValueAsBytes(data), Data.class))
 *     .build();
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Deep copy on read operations - 读操作时深拷贝</li>
 *   <li>Optional copy on write - 可选写时复制</li>
 *   <li>Custom copier support - 自定义复制器支持</li>
 *   <li>Serialization-based default copy - 基于序列化的默认复制</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe cache) - 线程安全: 是（委托给线程安全的缓存）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.0
 */
public final class CopyOnReadCache<K, V> implements Cache<K, V> {

    private static final System.Logger LOGGER = System.getLogger(CopyOnReadCache.class.getName());

    private final Cache<K, V> delegate;
    private final UnaryOperator<V> copier;
    private final boolean copyOnWrite;

    private CopyOnReadCache(Builder<K, V> builder) {
        this.delegate = builder.delegate;
        this.copier = builder.copier != null ? builder.copier : defaultCopier();
        this.copyOnWrite = builder.copyOnWrite;
    }

    /**
     * Wrap a cache with copy-on-read behavior
     * 使用读时复制行为包装缓存
     *
     * @param cache cache to wrap | 要包装的缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    /**
     * Create a default copier using Java serialization.
     * 使用 Java 序列化创建默认复制器。
     *
     * <p><strong>Security Warning | 安全警告:</strong> The default copier uses Java serialization
     * with an {@link ObjectInputFilter} restricting depth, size, and array length. For production
     * use with untrusted data, provide a custom copier via
     * {@code CopyOnReadCache.wrap(cache).copier(...)} (e.g., using Jackson or a builder pattern).</p>
     * <p>默认复制器使用带 {@link ObjectInputFilter} 的 Java 序列化，限制深度、大小和数组长度。
     * 对于生产环境中处理不可信数据的场景，建议通过
     * {@code CopyOnReadCache.wrap(cache).copier(...)} 提供自定义复制器。</p>
     *
     * @return default copier using serialization | 基于序列化的默认复制器
     */
    @SuppressWarnings("unchecked")
    private UnaryOperator<V> defaultCopier() {
        return value -> {
            if (value == null) {
                return null;
            }

            if (!(value instanceof Serializable)) {
                throw new IllegalStateException(
                        "Cannot create defensive copy of non-Serializable type "
                                + value.getClass().getName()
                                + ". Provide a custom copier via CopyOnReadCache.wrap(cache).copier(...).");
            }

            try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                 ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(value);
                oos.flush();

                try (ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
                     ObjectInputStream ois = new ObjectInputStream(bis)) {
                    ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter(
                            "maxdepth=20;maxbytes=1048576;maxarray=10000"));
                    return (V) ois.readObject();
                }
            } catch (IOException | ClassNotFoundException e) {
                throw new IllegalStateException(
                        "Failed to create defensive copy of value type " + value.getClass().getName()
                                + ". Value must be Serializable or provide a custom copier.", e);
            }
        };
    }

    private V copy(V value) {
        if (value == null) {
            return null;
        }
        return copier.apply(value);
    }

    // ==================== Read Operations (with copy) ====================

    @Override
    public V get(K key) {
        return copy(delegate.get(key));
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        return copy(delegate.get(key, loader));
    }

    @Override
    public V getOrDefault(K key, V defaultValue) {
        V value = delegate.get(key);
        return value != null ? copy(value) : defaultValue;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, V> result = delegate.getAll(keys);
        Map<K, V> copied = new LinkedHashMap<>();
        result.forEach((k, v) -> copied.put(k, copy(v)));
        return copied;
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, V> result = delegate.getAll(keys, loader);
        Map<K, V> copied = new LinkedHashMap<>();
        result.forEach((k, v) -> copied.put(k, copy(v)));
        return copied;
    }

    @Override
    public Collection<V> values() {
        return delegate.values().stream().map(this::copy).toList();
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        Set<Map.Entry<K, V>> copied = new LinkedHashSet<>();
        for (Map.Entry<K, V> entry : delegate.entries()) {
            copied.add(Map.entry(entry.getKey(), copy(entry.getValue())));
        }
        return copied;
    }

    @Override
    public V getAndRemove(K key) {
        return copy(delegate.getAndRemove(key));
    }

    // ==================== Write Operations (optionally with copy) ====================

    @Override
    public void put(K key, V value) {
        delegate.put(key, copyOnWrite ? copy(value) : value);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        if (copyOnWrite) {
            Map<K, V> copied = new LinkedHashMap<>();
            map.forEach((k, v) -> copied.put(k, copy(v)));
            delegate.putAll(copied);
        } else {
            delegate.putAll(map);
        }
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, copyOnWrite ? copy(value) : value);
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, copyOnWrite ? copy(value) : value, ttl);
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        if (copyOnWrite) {
            Map<K, V> copied = new LinkedHashMap<>();
            map.forEach((k, v) -> copied.put(k, copy(v)));
            delegate.putAllWithTtl(copied, ttl);
        } else {
            delegate.putAllWithTtl(map, ttl);
        }
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        return delegate.putIfAbsentWithTtl(key, copyOnWrite ? copy(value) : value, ttl);
    }

    @Override
    public V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return copy(delegate.computeIfPresent(key, remappingFunction));
    }

    @Override
    public V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return copy(delegate.compute(key, remappingFunction));
    }

    @Override
    public V replace(K key, V value) {
        return copy(delegate.replace(key, copyOnWrite ? copy(value) : value));
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        return delegate.replace(key, oldValue, copyOnWrite ? copy(newValue) : newValue);
    }

    // ==================== Pass-through Operations ====================

    @Override
    public void invalidate(K key) {
        delegate.invalidate(key);
    }

    @Override
    public void invalidateAll(Iterable<? extends K> keys) {
        delegate.invalidateAll(keys);
    }

    @Override
    public void invalidateAll() {
        delegate.invalidateAll();
    }

    @Override
    public boolean containsKey(K key) {
        return delegate.containsKey(key);
    }

    @Override
    public long size() {
        return delegate.size();
    }

    @Override
    public long estimatedSize() {
        return delegate.estimatedSize();
    }

    @Override
    public Set<K> keys() {
        return delegate.keys();
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        // Return a wrapper that copies on get
        return new CopyOnReadMap<>(delegate.asMap(), copier);
    }

    @Override
    public CacheStats stats() {
        return delegate.stats();
    }

    @Override
    public CacheMetrics metrics() {
        return delegate.metrics();
    }

    @Override
    public void cleanUp() {
        delegate.cleanUp();
    }

    @Override
    public AsyncCache<K, V> async() {
        return delegate.async();
    }

    @Override
    public String name() {
        return delegate.name();
    }

    // ==================== Builder ====================

    /**
     * Builder for CopyOnReadCache
     *
     * @param <K> the key type | 键类型
     * @param <V> the value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> delegate;
        private UnaryOperator<V> copier;
        private boolean copyOnWrite = false;

        Builder(Cache<K, V> delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate cannot be null");
        }

        /**
         * Set custom copier function
         * 设置自定义复制函数
         *
         * @param copier the copier | 复制器
         * @return this builder | 此构建器
         */
        public Builder<K, V> copier(UnaryOperator<V> copier) {
            this.copier = copier;
            return this;
        }

        /**
         * Also copy values on write operations
         * 写入操作也复制值
         *
         * @param copyOnWrite true to copy on write | 写时复制为 true
         * @return this builder | 此构建器
         */
        public Builder<K, V> copyOnWrite(boolean copyOnWrite) {
            this.copyOnWrite = copyOnWrite;
            return this;
        }

        /**
         * Build the copy-on-read cache
         * 构建读时复制缓存
          * @return the result | 结果
         */
        public CopyOnReadCache<K, V> build() {
            return new CopyOnReadCache<>(this);
        }
    }

    // ==================== CopyOnReadMap ====================

    private static class CopyOnReadMap<K, V> implements ConcurrentMap<K, V> {
        private final ConcurrentMap<K, V> delegate;
        private final UnaryOperator<V> copier;

        CopyOnReadMap(ConcurrentMap<K, V> delegate, UnaryOperator<V> copier) {
            this.delegate = delegate;
            this.copier = copier;
        }

        private V copy(V value) {
            return value != null ? copier.apply(value) : null;
        }

        @Override
        public V get(Object key) {
            return copy(delegate.get(key));
        }

        @Override
        public V getOrDefault(Object key, V defaultValue) {
            V value = delegate.get(key);
            return value != null ? copy(value) : defaultValue;
        }

        @Override
        public int size() {
            return delegate.size();
        }

        @Override
        public boolean isEmpty() {
            return delegate.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return delegate.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return delegate.containsValue(value);
        }

        @Override
        public V put(K key, V value) {
            return delegate.put(key, value);
        }

        @Override
        public V remove(Object key) {
            return copy(delegate.remove(key));
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            delegate.putAll(m);
        }

        @Override
        public void clear() {
            delegate.clear();
        }

        @Override
        public Set<K> keySet() {
            return delegate.keySet();
        }

        @Override
        public Collection<V> values() {
            return delegate.values().stream().map(this::copy).toList();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            Set<Entry<K, V>> copied = new LinkedHashSet<>();
            for (Entry<K, V> entry : delegate.entrySet()) {
                copied.add(Map.entry(entry.getKey(), copy(entry.getValue())));
            }
            return copied;
        }

        @Override
        public V putIfAbsent(K key, V value) {
            return copy(delegate.putIfAbsent(key, value));
        }

        @Override
        public boolean remove(Object key, Object value) {
            return delegate.remove(key, value);
        }

        @Override
        public boolean replace(K key, V oldValue, V newValue) {
            return delegate.replace(key, oldValue, newValue);
        }

        @Override
        public V replace(K key, V value) {
            return copy(delegate.replace(key, value));
        }
    }
}

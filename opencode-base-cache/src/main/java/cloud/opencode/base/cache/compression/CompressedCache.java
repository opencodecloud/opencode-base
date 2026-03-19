package cloud.opencode.base.cache.compression;

import cloud.opencode.base.cache.*;

import java.io.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * Compressed Cache - Cache decorator that compresses values
 * 压缩缓存 - 压缩值的缓存装饰器
 *
 * <p>Wraps values with compression to reduce memory footprint.
 * Compression is transparent to the caller.</p>
 * <p>用压缩包装值以减少内存占用。压缩对调用者是透明的。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Transparent compression/decompression - 透明压缩/解压</li>
 *   <li>Configurable threshold - 可配置阈值</li>
 *   <li>Multiple algorithms - 多种算法支持</li>
 *   <li>Compression statistics - 压缩统计</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create compressed cache
 * CompressedCache<String, byte[]> cache = CompressedCache.<String, byte[]>wrap(baseCache)
 *     .compressor(ValueCompressor.gzip(1024))
 *     .serializer(new ByteArraySerializer())
 *     .build();
 *
 * // Use normally - compression is automatic
 * cache.put("large-data", largeByteArray);
 * byte[] data = cache.get("large-data");
 *
 * // Get compression stats
 * CompressedCache.CompressionStats stats = cache.compressionStats();
 * System.out.println("Compression ratio: " + stats.compressionRatio());
 * }</pre>
 *
 * @param <K> key type | 键类型
 * @param <V> value type | 值类型
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe cache) - 线程安全: 是（委托给线程安全的缓存）</li>
 *   <li>Null-safe: Partial (null values not allowed) - 空值安全: 部分（不允许 null 值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public class CompressedCache<K, V> implements Cache<K, V> {

    private final Cache<K, byte[]> delegate;
    private final ValueCompressor compressor;
    private final ValueSerializer<V> serializer;

    // Statistics
    private final AtomicLong totalCompressed = new AtomicLong(0);
    private final AtomicLong totalDecompressed = new AtomicLong(0);
    private final AtomicLong bytesBeforeCompression = new AtomicLong(0);
    private final AtomicLong bytesAfterCompression = new AtomicLong(0);
    private final AtomicLong compressionTimeNanos = new AtomicLong(0);
    private final AtomicLong decompressionTimeNanos = new AtomicLong(0);

    @SuppressWarnings("unchecked")
    private CompressedCache(Cache<K, ?> delegate, ValueCompressor compressor, ValueSerializer<V> serializer) {
        this.delegate = (Cache<K, byte[]>) delegate;
        this.compressor = compressor;
        this.serializer = serializer;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create builder to wrap an existing cache
     * 创建构建器以包装现有缓存
     *
     * @param cache base cache | 基础缓存
     * @param <K>   key type | 键类型
     * @param <V>   value type | 值类型
     * @return builder | 构建器
     */
    public static <K, V> Builder<K, V> wrap(Cache<K, V> cache) {
        return new Builder<>(cache);
    }

    // ==================== Cache Operations | 缓存操作 ====================

    @Override
    public V get(K key) {
        byte[] compressed = delegate.get(key);
        return decompress(compressed);
    }

    @Override
    public V get(K key, Function<? super K, ? extends V> loader) {
        byte[] compressed = delegate.get(key, k -> compress(loader.apply(k)));
        return decompress(compressed);
    }

    @Override
    public Map<K, V> getAll(Iterable<? extends K> keys) {
        Map<K, byte[]> raw = delegate.getAll(keys);
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, byte[]> entry : raw.entrySet()) {
            result.put(entry.getKey(), decompress(entry.getValue()));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<K, V> getAll(Iterable<? extends K> keys,
                           Function<? super Set<? extends K>, ? extends Map<K, V>> loader) {
        Map<K, byte[]> raw = delegate.getAll(keys, missingKeys -> {
            Map<K, V> loaded = loader.apply(missingKeys);
            Map<K, byte[]> compressed = new LinkedHashMap<>();
            for (Map.Entry<K, V> entry : loaded.entrySet()) {
                compressed.put(entry.getKey(), compress(entry.getValue()));
            }
            return compressed;
        });
        Map<K, V> result = new LinkedHashMap<>();
        for (Map.Entry<K, byte[]> entry : raw.entrySet()) {
            result.put(entry.getKey(), decompress(entry.getValue()));
        }
        return result;
    }

    @Override
    public void put(K key, V value) {
        delegate.put(key, compress(value));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        Map<K, byte[]> compressed = new LinkedHashMap<>();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            compressed.put(entry.getKey(), compress(entry.getValue()));
        }
        delegate.putAll(compressed);
    }

    @Override
    public boolean putIfAbsent(K key, V value) {
        return delegate.putIfAbsent(key, compress(value));
    }

    @Override
    public void putWithTtl(K key, V value, Duration ttl) {
        delegate.putWithTtl(key, compress(value), ttl);
    }

    @Override
    public void putAllWithTtl(Map<? extends K, ? extends V> map, Duration ttl) {
        Map<K, byte[]> compressed = new LinkedHashMap<>();
        for (Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            compressed.put(entry.getKey(), compress(entry.getValue()));
        }
        delegate.putAllWithTtl(compressed, ttl);
    }

    @Override
    public boolean putIfAbsentWithTtl(K key, V value, Duration ttl) {
        return delegate.putIfAbsentWithTtl(key, compress(value), ttl);
    }

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
    public Collection<V> values() {
        List<V> result = new ArrayList<>();
        for (byte[] compressed : delegate.values()) {
            result.add(decompress(compressed));
        }
        return result;
    }

    @Override
    public Set<Map.Entry<K, V>> entries() {
        Set<Map.Entry<K, V>> result = new LinkedHashSet<>();
        for (Map.Entry<K, byte[]> entry : delegate.entries()) {
            result.add(new AbstractMap.SimpleImmutableEntry<>(
                    entry.getKey(), decompress(entry.getValue())));
        }
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public ConcurrentMap<K, V> asMap() {
        // Note: This returns an uncompressed view which may be expensive
        throw new UnsupportedOperationException("asMap() not supported for CompressedCache");
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
    @SuppressWarnings("unchecked")
    public AsyncCache<K, V> async() {
        throw new UnsupportedOperationException("async() not supported for CompressedCache");
    }

    @Override
    public String name() {
        return delegate.name();
    }

    // ==================== Compression Methods | 压缩方法 ====================

    private byte[] compress(V value) {
        if (value == null) {
            return null;
        }
        long start = System.nanoTime();
        try {
            byte[] serialized = serializer.serialize(value);
            bytesBeforeCompression.addAndGet(serialized.length);

            byte[] compressed = compressor.compress(serialized);
            bytesAfterCompression.addAndGet(compressed.length);

            totalCompressed.incrementAndGet();
            return compressed;
        } finally {
            compressionTimeNanos.addAndGet(System.nanoTime() - start);
        }
    }

    private V decompress(byte[] data) {
        if (data == null) {
            return null;
        }
        long start = System.nanoTime();
        try {
            byte[] decompressed = compressor.decompress(data);
            totalDecompressed.incrementAndGet();
            return serializer.deserialize(decompressed);
        } finally {
            decompressionTimeNanos.addAndGet(System.nanoTime() - start);
        }
    }

    // ==================== Statistics | 统计 ====================

    /**
     * Get compression statistics
     * 获取压缩统计
     *
     * @return compression stats | 压缩统计
     */
    public CompressionStats compressionStats() {
        return new CompressionStats(
                compressor.algorithm(),
                totalCompressed.get(),
                totalDecompressed.get(),
                bytesBeforeCompression.get(),
                bytesAfterCompression.get(),
                compressionTimeNanos.get(),
                decompressionTimeNanos.get()
        );
    }

    /**
     * Reset compression statistics
     * 重置压缩统计
     */
    public void resetCompressionStats() {
        totalCompressed.set(0);
        totalDecompressed.set(0);
        bytesBeforeCompression.set(0);
        bytesAfterCompression.set(0);
        compressionTimeNanos.set(0);
        decompressionTimeNanos.set(0);
    }

    /**
     * Compression statistics record
     * 压缩统计记录
     */
    public record CompressionStats(
            CompressionAlgorithm algorithm,
            long totalCompressed,
            long totalDecompressed,
            long bytesBeforeCompression,
            long bytesAfterCompression,
            long compressionTimeNanos,
            long decompressionTimeNanos
    ) {
        /**
         * Get compression ratio (smaller is better)
         * 获取压缩比（越小越好）
         *
         * @return compression ratio | 压缩比
         */
        public double compressionRatio() {
            return bytesBeforeCompression == 0 ? 1.0 :
                    (double) bytesAfterCompression / bytesBeforeCompression;
        }

        /**
         * Get space saved in bytes
         * 获取节省的空间（字节）
         *
         * @return bytes saved | 节省的字节数
         */
        public long bytesSaved() {
            return bytesBeforeCompression - bytesAfterCompression;
        }

        /**
         * Get average compression time in nanoseconds
         * 获取平均压缩时间（纳秒）
         *
         * @return average compression time | 平均压缩时间
         */
        public long avgCompressionTimeNanos() {
            return totalCompressed == 0 ? 0 : compressionTimeNanos / totalCompressed;
        }

        /**
         * Get average decompression time in nanoseconds
         * 获取平均解压时间（纳秒）
         *
         * @return average decompression time | 平均解压时间
         */
        public long avgDecompressionTimeNanos() {
            return totalDecompressed == 0 ? 0 : decompressionTimeNanos / totalDecompressed;
        }
    }

    // ==================== Serializer Interface | 序列化器接口 ====================

    /**
     * Value serializer for compression
     * 用于压缩的值序列化器
     *
     * @param <V> value type | 值类型
     */
    public interface ValueSerializer<V> {
        /**
         * Serialize value to bytes
         * 将值序列化为字节
         *
         * @param value the value | 值
         * @return serialized bytes | 序列化的字节
         */
        byte[] serialize(V value);

        /**
         * Deserialize bytes to value
         * 将字节反序列化为值
         *
         * @param data serialized bytes | 序列化的字节
         * @return deserialized value | 反序列化的值
         */
        V deserialize(byte[] data);

        /**
         * Create a Java serialization based serializer
         * 创建基于 Java 序列化的序列化器
         *
         * @param <V> value type | 值类型
         * @return serializer | 序列化器
         */
        @SuppressWarnings("unchecked")
        static <V> ValueSerializer<V> java() {
            return new ValueSerializer<>() {
                @Override
                public byte[] serialize(V value) {
                    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                         ObjectOutputStream oos = new ObjectOutputStream(baos)) {
                        oos.writeObject(value);
                        return baos.toByteArray();
                    } catch (IOException e) {
                        throw new RuntimeException("Serialization failed", e);
                    }
                }

                @Override
                public V deserialize(byte[] data) {
                    try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                         ObjectInputStream ois = new ObjectInputStream(bais)) {
                        return (V) ois.readObject();
                    } catch (IOException | ClassNotFoundException e) {
                        throw new RuntimeException("Deserialization failed", e);
                    }
                }
            };
        }

        /**
         * Create a pass-through serializer for byte arrays
         * 为字节数组创建直通序列化器
         *
         * @return serializer | 序列化器
         */
        static ValueSerializer<byte[]> passThrough() {
            return new ValueSerializer<>() {
                @Override
                public byte[] serialize(byte[] value) {
                    return value;
                }

                @Override
                public byte[] deserialize(byte[] data) {
                    return data;
                }
            };
        }

        /**
         * Create a String serializer using UTF-8
         * 使用 UTF-8 创建字符串序列化器
         *
         * @return serializer | 序列化器
         */
        static ValueSerializer<String> string() {
            return new ValueSerializer<>() {
                @Override
                public byte[] serialize(String value) {
                    return value.getBytes(java.nio.charset.StandardCharsets.UTF_8);
                }

                @Override
                public String deserialize(byte[] data) {
                    return new String(data, java.nio.charset.StandardCharsets.UTF_8);
                }
            };
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for CompressedCache
     * CompressedCache 构建器
     *
     * @param <K> key type | 键类型
     * @param <V> value type | 值类型
     */
    public static class Builder<K, V> {
        private final Cache<K, V> cache;
        private ValueCompressor compressor = ValueCompressor.gzip();
        private ValueSerializer<V> serializer;

        Builder(Cache<K, V> cache) {
            this.cache = cache;
        }

        /**
         * Set compressor
         * 设置压缩器
         *
         * @param compressor the compressor | 压缩器
         * @return this builder | 此构建器
         */
        public Builder<K, V> compressor(ValueCompressor compressor) {
            this.compressor = compressor;
            return this;
        }

        /**
         * Set serializer
         * 设置序列化器
         *
         * @param serializer the serializer | 序列化器
         * @return this builder | 此构建器
         */
        public Builder<K, V> serializer(ValueSerializer<V> serializer) {
            this.serializer = serializer;
            return this;
        }

        /**
         * Build the compressed cache
         * 构建压缩缓存
         *
         * @return compressed cache | 压缩缓存
         */
        @SuppressWarnings("unchecked")
        public CompressedCache<K, V> build() {
            if (serializer == null) {
                serializer = ValueSerializer.java();
            }
            return new CompressedCache<>(cache, compressor, serializer);
        }
    }
}

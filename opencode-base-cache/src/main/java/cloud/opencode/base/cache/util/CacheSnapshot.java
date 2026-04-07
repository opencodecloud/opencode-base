package cloud.opencode.base.cache.util;

import cloud.opencode.base.cache.Cache;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Cache Snapshot - Utility for saving and restoring cache state to/from disk
 * 缓存快照 - 将缓存状态保存到磁盘并从磁盘恢复的工具
 *
 * <p>Enables cache warm restart by persisting cache entries to a file
 * and restoring them on startup. Expired entries are skipped during restore.</p>
 * <p>通过将缓存条目持久化到文件来实现缓存热重启，启动时恢复。恢复时跳过过期条目。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Save cache entries to disk - 将缓存条目保存到磁盘</li>
 *   <li>Restore cache entries from disk - 从磁盘恢复缓存条目</li>
 *   <li>Base64 encoding for safe serialization - Base64 编码保证安全序列化</li>
 *   <li>Convenience methods for String caches - String 缓存的便捷方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Save a String cache
 * CacheSnapshot.saveStringCache(cache, Path.of("/tmp/cache.snapshot"));
 *
 * // Restore a String cache
 * int count = CacheSnapshot.restoreStringCache(Path.of("/tmp/cache.snapshot"), cache);
 *
 * // Save with custom serializers
 * CacheSnapshot.save(cache, path, Object::toString, Object::toString);
 *
 * // Restore with custom deserializers
 * int count = CacheSnapshot.restore(path, cache, Function.identity(), Function.identity());
 * }</pre>
 *
 * <p><strong>File Format | 文件格式:</strong></p>
 * <pre>
 * # OpenCache Snapshot v1
 * # Created: 2026-04-03T10:00:00Z
 * # Entries: 1234
 * KEY_BASE64\tVALUE_BASE64\tTTL_REMAINING_MS
 * </pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Uses buffered I/O for efficient disk access - 使用缓冲 I/O 高效磁盘访问</li>
 *   <li>Base64 encoding overhead: ~33% size increase - Base64 编码开销: 约 33% 大小增加</li>
 *   <li>Save uses {@code asMap().forEach()} for streaming iteration — avoids
 *       full-copy overhead of {@code entries()} on large caches
 *       - Save 使用 {@code asMap().forEach()} 流式遍历 — 避免大缓存时 {@code entries()} 的全量拷贝开销</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Does NOT use Java native serialization (ObjectInputStream/ObjectOutputStream)
 *       - 不使用 Java 原生序列化</li>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 *   <li>Null-safe: No (null keys/values not supported) - 空值安全: 否（不支持 null 键/值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V1.0.3
 */
public final class CacheSnapshot {

    /** Snapshot file format version | 快照文件格式版本 */
    private static final String FORMAT_VERSION = "v1";

    /** Header prefix for comment lines | 注释行的头部前缀 */
    private static final String HEADER_PREFIX = "# ";

    /** Tab separator between fields | 字段间的 Tab 分隔符 */
    private static final char FIELD_SEPARATOR = '\t';

    /** TTL value indicating default TTL should be used | 表示使用默认 TTL 的值 */
    private static final long DEFAULT_TTL_MARKER = -1;

    private CacheSnapshot() {
        // Utility class - no instances
    }

    /**
     * Save cache entries to a file using the provided serializers.
     * 使用提供的序列化器将缓存条目保存到文件。
     *
     * <p>Each entry is serialized as a line with Base64-encoded key and value
     * separated by tabs. TTL is always {@code -1} (default TTL) because the
     * {@link Cache} interface does not expose per-entry TTL information.</p>
     * <p>每个条目被序列化为一行，Base64 编码的键和值用制表符分隔。
     * TTL 始终为 {@code -1}（默认 TTL），因为 {@link Cache} 接口不暴露单条目 TTL 信息。</p>
     *
     * @param <K>             the key type | 键类型
     * @param <V>             the value type | 值类型
     * @param cache           the cache to snapshot | 要快照的缓存
     * @param path            the file path to write | 要写入的文件路径
     * @param keySerializer   function to serialize keys to strings | 键序列化函数
     * @param valueSerializer function to serialize values to strings | 值序列化函数
     * @throws IOException if an I/O error occurs | 发生 I/O 错误时抛出
     */
    public static <K, V> void save(Cache<K, V> cache,
                                    Path path,
                                    Function<K, String> keySerializer,
                                    Function<V, String> valueSerializer) throws IOException {
        Objects.requireNonNull(cache, "cache cannot be null");
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(keySerializer, "keySerializer cannot be null");
        Objects.requireNonNull(valueSerializer, "valueSerializer cannot be null");

        var encoder = Base64.getEncoder();

        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
            // Write header — use estimatedSize() to avoid full scan; exact count
            // is not critical for a snapshot header comment
            // 使用 estimatedSize() 避免全量扫描；快照头注释中精确计数并非关键
            writer.write(HEADER_PREFIX + "OpenCache Snapshot " + FORMAT_VERSION);
            writer.newLine();
            writer.write(HEADER_PREFIX + "Created: " + Instant.now());
            writer.newLine();
            writer.write(HEADER_PREFIX + "Entries: ~" + cache.estimatedSize());
            writer.newLine();

            // Stream iteration via asMap().forEach() — no full-copy snapshot of entries.
            // This avoids O(n) extra memory from cache.entries() which creates a Set copy.
            // 通过 asMap().forEach() 流式遍历 — 不创建全量条目副本。
            // 避免 cache.entries() 创建 Set 拷贝带来的 O(n) 额外内存。
            cache.asMap().forEach((key, value) -> {
                if (key == null || value == null) {
                    return;
                }
                try {
                    String keyStr = keySerializer.apply(key);
                    String valueStr = valueSerializer.apply(value);

                    String keyB64 = encoder.encodeToString(keyStr.getBytes(StandardCharsets.UTF_8));
                    String valueB64 = encoder.encodeToString(valueStr.getBytes(StandardCharsets.UTF_8));

                    writer.write(keyB64);
                    writer.write(FIELD_SEPARATOR);
                    writer.write(valueB64);
                    writer.write(FIELD_SEPARATOR);
                    writer.write(Long.toString(DEFAULT_TTL_MARKER));
                    writer.newLine();
                } catch (IOException e) {
                    throw new java.io.UncheckedIOException(e);
                }
            });
        } catch (java.io.UncheckedIOException e) {
            throw e.getCause();
        }
    }

    /**
     * Restore cache entries from a file.
     * 从文件恢复缓存条目。
     *
     * <p>Reads the snapshot file and puts each entry into the target cache.
     * Lines with TTL of 0 are skipped (expired). Lines with TTL of -1
     * use the cache's default TTL.</p>
     * <p>读取快照文件并将每个条目放入目标缓存。TTL 为 0 的行被跳过（已过期）。
     * TTL 为 -1 的行使用缓存的默认 TTL。</p>
     *
     * @param <K>               the key type | 键类型
     * @param <V>               the value type | 值类型
     * @param path              the file path to read | 要读取的文件路径
     * @param cache             the target cache | 目标缓存
     * @param keyDeserializer   function to deserialize keys from strings | 键反序列化函数
     * @param valueDeserializer function to deserialize values from strings | 值反序列化函数
     * @return number of entries restored | 恢复的条目数
     * @throws IOException if an I/O error occurs | 发生 I/O 错误时抛出
     */
    public static <K, V> int restore(Path path,
                                      Cache<K, V> cache,
                                      Function<String, K> keyDeserializer,
                                      Function<String, V> valueDeserializer) throws IOException {
        Objects.requireNonNull(path, "path cannot be null");
        Objects.requireNonNull(cache, "cache cannot be null");
        Objects.requireNonNull(keyDeserializer, "keyDeserializer cannot be null");
        Objects.requireNonNull(valueDeserializer, "valueDeserializer cannot be null");

        var decoder = Base64.getDecoder();
        int count = 0;

        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comment lines
                if (line.startsWith(HEADER_PREFIX) || line.isBlank()) {
                    continue;
                }

                int firstTab = line.indexOf(FIELD_SEPARATOR);
                int lastTab = line.lastIndexOf(FIELD_SEPARATOR);
                if (firstTab < 0 || lastTab <= firstTab) {
                    // Malformed line — skip
                    continue;
                }

                String keyB64 = line.substring(0, firstTab);
                String valueB64 = line.substring(firstTab + 1, lastTab);
                String ttlStr = line.substring(lastTab + 1);

                long ttlMs;
                try {
                    ttlMs = Long.parseLong(ttlStr);
                } catch (NumberFormatException e) {
                    // Malformed TTL — skip
                    continue;
                }

                // Skip expired entries (TTL == 0 means expired)
                if (ttlMs == 0) {
                    continue;
                }

                String keyStr;
                String valueStr;
                try {
                    keyStr = new String(decoder.decode(keyB64), StandardCharsets.UTF_8);
                    valueStr = new String(decoder.decode(valueB64), StandardCharsets.UTF_8);
                } catch (IllegalArgumentException e) {
                    // Corrupted Base64 data — skip this line
                    continue;
                }

                K key = keyDeserializer.apply(keyStr);
                V value = valueDeserializer.apply(valueStr);

                if (key == null || value == null) {
                    continue;
                }

                if (ttlMs > 0) {
                    cache.putWithTtl(key, value, java.time.Duration.ofMillis(ttlMs));
                } else {
                    // DEFAULT_TTL_MARKER (-1) → use cache default TTL
                    cache.put(key, value);
                }
                count++;
            }
        }

        return count;
    }

    /**
     * Save a String-keyed, String-valued cache to a file.
     * 将 String 键值缓存保存到文件。
     *
     * <p>Convenience method for {@code Cache<String, String>} — uses identity
     * serializers (strings are encoded as-is via Base64).</p>
     * <p>{@code Cache<String, String>} 的便捷方法 — 使用恒等序列化器
     * （字符串通过 Base64 原样编码）。</p>
     *
     * @param cache the string cache to save | 要保存的 String 缓存
     * @param path  the file path | 文件路径
     * @throws IOException if an I/O error occurs | 发生 I/O 错误时抛出
     */
    public static void saveStringCache(Cache<String, String> cache, Path path) throws IOException {
        save(cache, path, Function.identity(), Function.identity());
    }

    /**
     * Restore a String-keyed, String-valued cache from a file.
     * 从文件恢复 String 键值缓存。
     *
     * <p>Convenience method for {@code Cache<String, String>} — uses identity
     * deserializers.</p>
     * <p>{@code Cache<String, String>} 的便捷方法 — 使用恒等反序列化器。</p>
     *
     * @param path  the file path | 文件路径
     * @param cache the target string cache | 目标 String 缓存
     * @return number of entries restored | 恢复的条目数
     * @throws IOException if an I/O error occurs | 发生 I/O 错误时抛出
     */
    public static int restoreStringCache(Path path, Cache<String, String> cache) throws IOException {
        return restore(path, cache, Function.identity(), Function.identity());
    }
}

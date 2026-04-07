
package cloud.opencode.base.serialization;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * SerializationResult - Immutable Serialization Result Wrapper
 * 序列化结果包装类 - 不可变
 *
 * <p>Wraps the result of a serialization operation, including the serialized data, format,
 * size information, compression status, and timing information.</p>
 * <p>包装序列化操作的结果，包括序列化数据、格式、大小信息、压缩状态和计时信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable result wrapper - 不可变结果包装</li>
 *   <li>Timing support via timed() factory - 通过 timed() 工厂方法支持计时</li>
 *   <li>Format and compression metadata - 格式和压缩元数据</li>
 *   <li>Defensive copy of byte array data - 字节数组数据的防御性拷贝</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic result
 * SerializationResult result = SerializationResult.of(data, "json");
 *
 * // With timing
 * SerializationResult result = SerializationResult.of(data, "json", durationNanos);
 *
 * // Auto-timed serialization
 * SerializationResult result = SerializationResult.timed(
 *     () -> serializer.serialize(obj), "json");
 *
 * // Via OpenSerializer
 * SerializationResult result = OpenSerializer.serializeWithResult(obj);
 * System.out.println(result.format());        // "json"
 * System.out.println(result.data().length);   // byte count
 * System.out.println(result.durationNanos()); // nanoseconds
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable with defensive copies) - 线程安全: 是（不可变，防御性拷贝）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.3
 */
public final class SerializationResult {

    private final byte[] data;
    private final String format;
    private final int originalSize;
    private final boolean compressed;
    private final long durationNanos;
    @SuppressWarnings("FieldMayBeFinal")
    private volatile int hashCache; // lazily computed, 0 = not yet cached

    /**
     * Public constructor with validation and defensive copy.
     * 带验证和防御性拷贝的公共构造函数。
     *
     * @param data          the serialized data | 序列化后的数据
     * @param format        the format | 格式
     * @param originalSize  the original size estimate (-1 if unknown) | 原始大小估计
     * @param compressed    whether compressed | 是否压缩
     * @param durationNanos duration in nanoseconds | 耗时（纳秒）
     */
    public SerializationResult(byte[] data, String format, int originalSize,
                               boolean compressed, long durationNanos) {
        Objects.requireNonNull(data, "Data must not be null");
        Objects.requireNonNull(format, "Format must not be null");
        this.data = data.clone(); // defensive copy
        this.format = format;
        this.originalSize = originalSize;
        this.compressed = compressed;
        this.durationNanos = durationNanos;
    }

    /**
     * Trusted internal constructor — skips defensive copy when data is freshly created.
     * 可信内部构造函数 — 当 data 是新创建的数组时跳过防御性拷贝。
     */
    private SerializationResult(byte[] data, String format, int originalSize,
                                boolean compressed, long durationNanos, boolean trusted) {
        // trusted flag is only used to disambiguate overloads; data is NOT cloned
        this.data = data;
        this.format = format;
        this.originalSize = originalSize;
        this.compressed = compressed;
        this.durationNanos = durationNanos;
    }

    // ==================== Accessors | 访问器 ====================

    /**
     * Returns a defensive copy of the serialized data.
     * 返回序列化数据的防御性拷贝。
     *
     * @return a copy of the data array | 数据数组的拷贝
     */
    public byte[] data() {
        return data.clone();
    }

    /**
     * Returns the internal byte array without copying.
     * 返回内部字节数组，不进行拷贝。
     *
     * <p><strong>Warning:</strong> The returned array is the internal representation.
     * Callers <em>must not</em> modify it. Use {@link #data()} if you need a mutable copy.</p>
     * <p><strong>警告：</strong>返回的数组是内部表示。
     * 调用方<em>不得</em>修改它。如需可修改副本请使用 {@link #data()}。</p>
     *
     * @return the internal data array (read-only by contract) | 内部数据数组（约定只读）
     * @since JDK 25, opencode-base-serialization V1.0.3
     */
    public byte[] dataUnsafe() {
        return data;
    }

    /**
     * Returns the serialization format.
     * 返回序列化格式。
     *
     * @return the format | 格式
     */
    public String format() {
        return format;
    }

    /**
     * Returns the estimated original size, or -1 if unknown.
     * 返回估计的原始大小，未知则为 -1。
     *
     * @return the original size | 原始大小
     */
    public int originalSize() {
        return originalSize;
    }

    /**
     * Returns whether the data is compressed.
     * 返回数据是否被压缩。
     *
     * @return true if compressed | 如果已压缩则为 true
     */
    public boolean compressed() {
        return compressed;
    }

    /**
     * Returns the serialization duration in nanoseconds, 0 if not measured.
     * 返回序列化耗时（纳秒），未测量则为 0。
     *
     * @return the duration in nanoseconds | 耗时（纳秒）
     */
    public long durationNanos() {
        return durationNanos;
    }

    /**
     * Returns the size of the serialized data in bytes.
     * 返回序列化数据的字节大小。
     *
     * @return the data size in bytes | 数据字节大小
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns the serialized data as a UTF-8 string.
     * 将序列化数据作为 UTF-8 字符串返回。
     *
     * @return the data as a UTF-8 string | 数据的 UTF-8 字符串表示
     */
    public String asString() {
        return new String(data, StandardCharsets.UTF_8);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a basic serialization result.
     * 创建基本的序列化结果。
     *
     * @param data   the serialized data | 序列化后的数据
     * @param format the format name | 格式名称
     * @return the result | 结果
     */
    public static SerializationResult of(byte[] data, String format) {
        return new SerializationResult(data, format, -1, false, 0L);
    }

    /**
     * Creates a serialization result with timing information.
     * 创建带计时信息的序列化结果。
     *
     * @param data          the serialized data | 序列化后的数据
     * @param format        the format name | 格式名称
     * @param durationNanos the duration in nanoseconds | 耗时（纳秒）
     * @return the result | 结果
     */
    public static SerializationResult of(byte[] data, String format, long durationNanos) {
        return new SerializationResult(data, format, -1, false, durationNanos);
    }

    /**
     * Creates a timed serialization result by executing the supplier and measuring elapsed time.
     * 通过执行 supplier 并测量耗时来创建计时序列化结果。
     *
     * <p>The byte array returned by the supplier is used directly without defensive copy,
     * since it is freshly created and not retained by the caller.</p>
     * <p>供应者返回的字节数组直接使用，不做防御性拷贝，因为它是新创建的且调用方不会持有引用。</p>
     *
     * @param serialization the serialization supplier | 序列化供应者
     * @param format        the format name | 格式名称
     * @return the result with timing | 带计时的结果
     */
    public static SerializationResult timed(Supplier<byte[]> serialization, String format) {
        Objects.requireNonNull(serialization, "Serialization supplier must not be null");
        Objects.requireNonNull(format, "Format must not be null");
        long start = System.nanoTime();
        byte[] data = serialization.get();
        long duration = System.nanoTime() - start;
        Objects.requireNonNull(data, "Data must not be null");
        return new SerializationResult(data, format, -1, false, duration, true);
    }

    // ==================== Object Methods | 对象方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SerializationResult that)) return false;
        return originalSize == that.originalSize
                && compressed == that.compressed
                && durationNanos == that.durationNanos
                && Arrays.equals(data, that.data)
                && format.equals(that.format);
    }

    @Override
    public int hashCode() {
        int h = hashCache;
        if (h == 0) {
            h = Arrays.hashCode(data);
            h = 31 * h + format.hashCode();
            h = 31 * h + originalSize;
            h = 31 * h + Boolean.hashCode(compressed);
            h = 31 * h + Long.hashCode(durationNanos);
            hashCache = h;
        }
        return h;
    }

    @Override
    public String toString() {
        return "SerializationResult[format=" + format
                + ", size=" + data.length
                + ", originalSize=" + originalSize
                + ", compressed=" + compressed
                + ", durationNanos=" + durationNanos + "]";
    }
}

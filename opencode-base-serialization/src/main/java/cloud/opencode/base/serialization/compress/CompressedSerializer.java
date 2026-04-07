
package cloud.opencode.base.serialization.compress;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;

import java.io.*;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.zip.*;

/**
 * CompressedSerializer - Compression Decorator for Serializers
 * 压缩序列化装饰器
 *
 * <p>This decorator adds compression capability to any serializer using the Decorator pattern.
 * It automatically compresses data exceeding the threshold and adds a header byte to identify the algorithm.</p>
 * <p>此装饰器使用装饰器模式为任意序列化器添加压缩能力。
 * 它自动压缩超过阈值的数据并添加头部字节来标识算法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Automatic compression based on threshold - 基于阈值的自动压缩</li>
 *   <li>GZIP and Deflate algorithm support (JDK built-in) - GZIP 和 Deflate 算法支持（JDK 内置）</li>
 *   <li>Transparent decompression - 透明解压</li>
 *   <li>Header-based algorithm detection - 基于头部的算法检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Serializer jsonSerializer = OpenSerializer.get("json");
 * CompressedSerializer compressed = new CompressedSerializer(
 *     jsonSerializer,
 *     CompressionAlgorithm.GZIP,
 *     1024  // Compress if > 1KB
 * );
 *
 * byte[] data = compressed.serialize(largeObject);
 * Object restored = compressed.deserialize(data, LargeObject.class);
 * }</pre>
 *
 * <p><strong>Data Format | 数据格式:</strong></p>
 * <pre>
 * Below threshold / NONE:
 *   [raw delegate bytes]              — zero overhead, direct pass-through
 *
 * Above threshold (compressed):
 *   +--------+------------------+
 *   | Header | Compressed Data  |     — 1-byte algorithm ID prefix
 *   | 1 byte | Variable length  |
 *   +--------+------------------+
 *   Header: Algorithm ID (1=GZIP, 5=DEFLATE)
 * </pre>
 *
 * <p><strong>Detection | 检测逻辑:</strong></p>
 * <p>On deserialization, the first byte is checked against known compression algorithm IDs.
 * GZIP (0x01) and DEFLATE (0x05) are never valid first bytes of JSON ({@code 0x7B}, {@code 0x5B}, etc.)
 * or JDK serialization ({@code 0xAC}), so the detection is unambiguous.</p>
 * <p>反序列化时检查第一字节是否为已知压缩算法 ID。GZIP (0x01) 和 DEFLATE (0x05)
 * 不会出现在 JSON 或 JDK 序列化输出的第一字节，因此检测无歧义。</p>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (if delegate is thread-safe) - 线程安全: 是（如果委托是线程安全的）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) for serialization + compression - 序列化+压缩 O(n)</li>
 *   <li>Space complexity: O(n) for compressed output - 压缩输出 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-serialization V1.0.0
 */
public class CompressedSerializer implements Serializer {

    /**
     * Default compression threshold (1024 bytes)
     * 默认压缩阈值（1024 字节）
     */
    public static final int DEFAULT_THRESHOLD = 1024;

    /**
     * Maximum allowed decompressed size (256MB)
     */
    private static final int MAX_DECOMPRESSED_SIZE = 256 * 1024 * 1024; // 256MB

    /**
     * The delegate serializer
     * 委托的序列化器
     */
    private final Serializer delegate;

    /**
     * The compression algorithm
     * 压缩算法
     */
    private final CompressionAlgorithm algorithm;

    /**
     * The compression threshold in bytes
     * 压缩阈值（字节）
     */
    private final int threshold;

    /** Pre-computed format string, avoids string concatenation on every getFormat() call. */
    private final String format;

    /**
     * Creates a compressed serializer with default threshold (1024 bytes).
     * 创建带默认阈值（1024 字节）的压缩序列化器。
     *
     * @param delegate  the delegate serializer - 委托的序列化器
     * @param algorithm the compression algorithm - 压缩算法
     */
    public CompressedSerializer(Serializer delegate, CompressionAlgorithm algorithm) {
        this(delegate, algorithm, DEFAULT_THRESHOLD);
    }

    /**
     * Creates a compressed serializer with custom threshold.
     * 创建带自定义阈值的压缩序列化器。
     *
     * @param delegate  the delegate serializer - 委托的序列化器
     * @param algorithm the compression algorithm - 压缩算法
     * @param threshold the compression threshold in bytes - 压缩阈值（字节）
     */
    public CompressedSerializer(Serializer delegate, CompressionAlgorithm algorithm, int threshold) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate serializer must not be null");
        this.algorithm = Objects.requireNonNull(algorithm, "Algorithm must not be null");
        if (threshold < 0) {
            throw new IllegalArgumentException("Threshold must be non-negative");
        }
        this.threshold = threshold;
        this.format = (algorithm == CompressionAlgorithm.NONE)
                ? delegate.getFormat()
                : delegate.getFormat() + "+" + algorithm.getName();
    }

    // ==================== Serializer Implementation | 序列化器实现 ====================

    @Override
    public byte[] serialize(Object obj) {
        byte[] data = delegate.serialize(obj);

        // Below threshold or NONE: return raw delegate bytes directly — zero copy, zero overhead
        if (data.length < threshold || algorithm == CompressionAlgorithm.NONE) {
            return data;
        }

        // Above threshold: compress with 1-byte algorithm header
        return switch (algorithm) {
            case GZIP -> compressGzipWithHeader(data);
            case DEFLATE -> compressDeflateWithHeader(data);
            default -> data;
        };
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        byte[] decompressed = decompressIfNeeded(data);
        return delegate.deserialize(decompressed, type);
    }

    @Override
    public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
        byte[] decompressed = decompressIfNeeded(data);
        return delegate.deserialize(decompressed, typeRef);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        byte[] decompressed = decompressIfNeeded(data);
        return delegate.deserialize(decompressed, type);
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public String getMimeType() {
        return delegate.getMimeType();
    }

    @Override
    public boolean supports(Class<?> type) {
        return delegate.supports(type);
    }

    @Override
    public boolean isTextBased() {
        return false; // Compressed data is always binary
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Returns the delegate serializer.
     * 返回委托的序列化器。
     *
     * @return the delegate - 委托
     */
    public Serializer getDelegate() {
        return delegate;
    }

    /**
     * Returns the compression algorithm.
     * 返回压缩算法。
     *
     * @return the algorithm - 算法
     */
    public CompressionAlgorithm getAlgorithm() {
        return algorithm;
    }

    /**
     * Returns the compression threshold.
     * 返回压缩阈值。
     *
     * @return the threshold in bytes - 阈值（字节）
     */
    public int getThreshold() {
        return threshold;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Detects whether data is compressed and decompresses if needed.
     * 检测数据是否已压缩，如需要则解压。
     *
     * <p>Peeks at the first byte to determine if data carries a compression header.
     * Known algorithm IDs (GZIP=0x01, DEFLATE=0x05) never appear as the first byte
     * of JSON or JDK serialization output, so detection is unambiguous.</p>
     * <p>通过第一字节判断数据是否携带压缩头部。已知算法 ID (GZIP=0x01, DEFLATE=0x05)
     * 不会出现在 JSON 或 JDK 序列化输出的第一字节，因此检测无歧义。</p>
     */
    private byte[] decompressIfNeeded(byte[] data) {
        if (data.length < 1) {
            throw new OpenSerializationException("Invalid compressed data: empty");
        }

        CompressionAlgorithm alg = CompressionAlgorithm.fromId(data[0]);

        if (alg == CompressionAlgorithm.NONE || !alg.isBuiltIn()) {
            // Not compressed — raw delegate output, zero copy pass-through
            return data;
        }

        // Compressed: strip 1-byte header and decompress from offset 1
        return decompress(data, 1, data.length - 1, alg);
    }

/**
     * Decompresses data at the given offset/length using the specified algorithm.
     * 使用指定的算法从给定偏移/长度解压数据。
     */
    private byte[] decompress(byte[] data, int offset, int length, CompressionAlgorithm alg) {
        return switch (alg) {
            case GZIP -> decompressGzip(data, offset, length);
            case DEFLATE -> decompressDeflate(data, offset, length);
            default -> {
                byte[] result = new byte[length];
                System.arraycopy(data, offset, result, 0, length);
                yield result;
            }
        };
    }

    // ==================== GZIP Implementation | GZIP 实现 ====================

    /**
     * Compresses with GZIP and writes the algorithm header byte in a single stream pass,
     * avoiding an extra full array copy from addHeader().
     * 使用 GZIP 压缩并在单次流操作中写入算法头部字节，避免 addHeader() 的额外全量数组拷贝。
     */
    private byte[] compressGzipWithHeader(byte[] data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 3 / 4 + 1)) {
            bos.write(algorithm.getId()); // header byte written first
            try (GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
                gzip.write(data);
                gzip.finish();
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw OpenSerializationException.compressionFailed("GZIP", e);
        }
    }

    private byte[] decompressGzip(byte[] data, int offset, int length) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, length);
             GZIPInputStream gzip = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream((int) Math.min((long) length * 2, 1024 * 1024))) {
            byte[] buffer = new byte[8192];
            long totalRead = 0;
            int bytesRead;
            while ((bytesRead = gzip.read(buffer)) != -1) {
                totalRead += bytesRead;
                if (totalRead > MAX_DECOMPRESSED_SIZE) {
                    throw new OpenSerializationException(
                            "GZIP decompressed size exceeds limit of " + MAX_DECOMPRESSED_SIZE + " bytes");
                }
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } catch (OpenSerializationException e) {
            throw e;
        } catch (IOException e) {
            throw OpenSerializationException.decompressionFailed("GZIP", e);
        }
    }

    // ==================== Deflate Implementation | Deflate 实现 ====================

    /**
     * Compresses with Deflate and writes the algorithm header byte in a single stream pass.
     * 使用 Deflate 压缩并在单次流操作中写入算法头部字节。
     */
    private byte[] compressDeflateWithHeader(byte[] data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length * 3 / 4 + 1)) {
            bos.write(algorithm.getId()); // header byte written first
            try (DeflaterOutputStream deflater = new DeflaterOutputStream(bos)) {
                deflater.write(data);
                deflater.finish();
            }
            return bos.toByteArray();
        } catch (IOException e) {
            throw OpenSerializationException.compressionFailed("DEFLATE", e);
        }
    }

    private byte[] decompressDeflate(byte[] data, int offset, int length) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data, offset, length);
             InflaterInputStream inflater = new InflaterInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream((int) Math.min((long) length * 2, 1024 * 1024))) {
            byte[] buffer = new byte[8192];
            long totalRead = 0;
            int bytesRead;
            while ((bytesRead = inflater.read(buffer)) != -1) {
                totalRead += bytesRead;
                if (totalRead > MAX_DECOMPRESSED_SIZE) {
                    throw new OpenSerializationException(
                            "DEFLATE decompressed size exceeds limit of " + MAX_DECOMPRESSED_SIZE + " bytes");
                }
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        } catch (OpenSerializationException e) {
            throw e;
        } catch (IOException e) {
            throw OpenSerializationException.decompressionFailed("DEFLATE", e);
        }
    }
}

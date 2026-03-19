
package cloud.opencode.base.serialization.compress;

import cloud.opencode.base.serialization.Serializer;
import cloud.opencode.base.serialization.TypeReference;
import cloud.opencode.base.serialization.exception.OpenSerializationException;

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

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
 *   <li>Multiple algorithm support - 多种算法支持</li>
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
 * +--------+------------------+
 * | Header | Compressed Data  |
 * | 1 byte | Variable length  |
 * +--------+------------------+
 * Header: Algorithm ID (0=NONE, 1=GZIP, 2=LZ4, etc.)
 * </pre>
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

    // ==================== Cached Reflection for LZ4 | LZ4 缓存反射 ====================

    private static final Class<?> LZ4_FACTORY_CLASS;
    private static final Method LZ4_FASTEST_INSTANCE;
    private static final Method LZ4_FAST_COMPRESSOR;
    private static final Method LZ4_FAST_DECOMPRESSOR;
    private static final Method LZ4_MAX_COMPRESSED_LENGTH;
    private static final Method LZ4_COMPRESS_METHOD;
    private static final Method LZ4_DECOMPRESS_METHOD;

    static {
        Class<?> lz4FactoryClass = null;
        Method lz4FastestInstance = null;
        Method lz4FastCompressor = null;
        Method lz4FastDecompressor = null;
        Method lz4MaxCompressedLength = null;
        Method lz4CompressMethod = null;
        Method lz4DecompressMethod = null;
        try {
            lz4FactoryClass = Class.forName("net.jpountz.lz4.LZ4Factory");
            lz4FastestInstance = lz4FactoryClass.getMethod("fastestInstance");
            lz4FastCompressor = lz4FactoryClass.getMethod("fastCompressor");
            lz4FastDecompressor = lz4FactoryClass.getMethod("fastDecompressor");

            // Cache compressor/decompressor methods by obtaining instances once
            Object factory = lz4FastestInstance.invoke(null);
            Object compressor = lz4FastCompressor.invoke(factory);
            Object decompressor = lz4FastDecompressor.invoke(factory);

            lz4MaxCompressedLength = compressor.getClass()
                    .getMethod("maxCompressedLength", int.class);
            lz4CompressMethod = compressor.getClass()
                    .getMethod("compress", byte[].class, int.class, int.class, byte[].class, int.class, int.class);
            lz4DecompressMethod = decompressor.getClass()
                    .getMethod("decompress", byte[].class, int.class, byte[].class, int.class, int.class);
        } catch (Exception e) {
            // LZ4 not available
        }
        LZ4_FACTORY_CLASS = lz4FactoryClass;
        LZ4_FASTEST_INSTANCE = lz4FastestInstance;
        LZ4_FAST_COMPRESSOR = lz4FastCompressor;
        LZ4_FAST_DECOMPRESSOR = lz4FastDecompressor;
        LZ4_MAX_COMPRESSED_LENGTH = lz4MaxCompressedLength;
        LZ4_COMPRESS_METHOD = lz4CompressMethod;
        LZ4_DECOMPRESS_METHOD = lz4DecompressMethod;
    }

    // ==================== Cached Reflection for Snappy | Snappy 缓存反射 ====================

    private static final Class<?> SNAPPY_CLASS;
    private static final Method SNAPPY_COMPRESS;
    private static final Method SNAPPY_UNCOMPRESS;

    static {
        Class<?> snappyClass = null;
        Method snappyCompress = null;
        Method snappyUncompress = null;
        try {
            snappyClass = Class.forName("org.xerial.snappy.Snappy");
            snappyCompress = snappyClass.getMethod("compress", byte[].class);
            snappyUncompress = snappyClass.getMethod("uncompress", byte[].class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // Snappy not available
        }
        SNAPPY_CLASS = snappyClass;
        SNAPPY_COMPRESS = snappyCompress;
        SNAPPY_UNCOMPRESS = snappyUncompress;
    }

    // ==================== Cached Reflection for ZSTD | ZSTD 缓存反射 ====================

    private static final Class<?> ZSTD_CLASS;
    private static final Method ZSTD_COMPRESS;
    private static final Method ZSTD_DECOMPRESSED_SIZE;
    private static final Method ZSTD_DECOMPRESS;

    static {
        Class<?> zstdClass = null;
        Method zstdCompress = null;
        Method zstdDecompressedSize = null;
        Method zstdDecompress = null;
        try {
            zstdClass = Class.forName("com.github.luben.zstd.Zstd");
            zstdCompress = zstdClass.getMethod("compress", byte[].class);
            zstdDecompressedSize = zstdClass.getMethod("decompressedSize", byte[].class);
            zstdDecompress = zstdClass.getMethod("decompress", byte[].class, int.class);
        } catch (ClassNotFoundException | NoSuchMethodException e) {
            // ZSTD not available
        }
        ZSTD_CLASS = zstdClass;
        ZSTD_COMPRESS = zstdCompress;
        ZSTD_DECOMPRESSED_SIZE = zstdDecompressedSize;
        ZSTD_DECOMPRESS = zstdDecompress;
    }

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
    }

    // ==================== Serializer Implementation | 序列化器实现 ====================

    @Override
    public byte[] serialize(Object obj) {
        byte[] data = delegate.serialize(obj);

        // Don't compress if below threshold or NONE algorithm
        if (data.length < threshold || algorithm == CompressionAlgorithm.NONE) {
            return addHeader(data, CompressionAlgorithm.NONE);
        }

        // Compress and add header
        byte[] compressed = compress(data);
        return addHeader(compressed, algorithm);
    }

    @Override
    public <T> T deserialize(byte[] data, Class<T> type) {
        byte[] decompressed = decompressWithHeader(data);
        return delegate.deserialize(decompressed, type);
    }

    @Override
    public <T> T deserialize(byte[] data, TypeReference<T> typeRef) {
        byte[] decompressed = decompressWithHeader(data);
        return delegate.deserialize(decompressed, typeRef);
    }

    @Override
    public <T> T deserialize(byte[] data, Type type) {
        byte[] decompressed = decompressWithHeader(data);
        return delegate.deserialize(decompressed, type);
    }

    @Override
    public String getFormat() {
        if (algorithm == CompressionAlgorithm.NONE) {
            return delegate.getFormat();
        }
        return delegate.getFormat() + "+" + algorithm.getName();
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
     * Adds header byte to data.
     * 向数据添加头部字节。
     */
    private byte[] addHeader(byte[] data, CompressionAlgorithm alg) {
        byte[] result = new byte[data.length + 1];
        result[0] = alg.getId();
        System.arraycopy(data, 0, result, 1, data.length);
        return result;
    }

    /**
     * Removes header byte from data.
     * 从数据移除头部字节。
     */
    private byte[] removeHeader(byte[] data) {
        if (data.length < 1) {
            throw new OpenSerializationException("Invalid compressed data: missing header");
        }
        byte[] result = new byte[data.length - 1];
        System.arraycopy(data, 1, result, 0, result.length);
        return result;
    }

    /**
     * Decompresses data with header detection.
     * 带头部检测的数据解压。
     */
    private byte[] decompressWithHeader(byte[] data) {
        if (data.length < 1) {
            throw new OpenSerializationException("Invalid compressed data: empty");
        }

        CompressionAlgorithm alg = CompressionAlgorithm.fromId(data[0]);
        byte[] payload = removeHeader(data);

        if (alg == CompressionAlgorithm.NONE) {
            return payload;
        }

        return decompress(payload, alg);
    }

    /**
     * Compresses data using the configured algorithm.
     * 使用配置的算法压缩数据。
     */
    private byte[] compress(byte[] data) {
        return switch (algorithm) {
            case GZIP -> compressGzip(data);
            case LZ4 -> compressLz4(data);
            case SNAPPY -> compressSnappy(data);
            case ZSTD -> compressZstd(data);
            default -> data;
        };
    }

    /**
     * Decompresses data using the specified algorithm.
     * 使用指定的算法解压数据。
     */
    private byte[] decompress(byte[] data, CompressionAlgorithm alg) {
        return switch (alg) {
            case GZIP -> decompressGzip(data);
            case LZ4 -> decompressLz4(data);
            case SNAPPY -> decompressSnappy(data);
            case ZSTD -> decompressZstd(data);
            default -> data;
        };
    }

    // ==================== GZIP Implementation | GZIP 实现 ====================

    private byte[] compressGzip(byte[] data) {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(bos)) {
            gzip.write(data);
            gzip.finish();
            return bos.toByteArray();
        } catch (IOException e) {
            throw OpenSerializationException.compressionFailed("GZIP", e);
        }
    }

    private byte[] decompressGzip(byte[] data) {
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             GZIPInputStream gzip = new GZIPInputStream(bis);
             ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int totalRead = 0;
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

    // ==================== LZ4 Implementation | LZ4 实现 ====================

    private byte[] compressLz4(byte[] data) {
        try {
            if (LZ4_FACTORY_CLASS == null) {
                throw new OpenSerializationException("LZ4 library (net.jpountz.lz4) is not available on the classpath");
            }

            Object factory = LZ4_FASTEST_INSTANCE.invoke(null);
            Object compressor = LZ4_FAST_COMPRESSOR.invoke(factory);

            int maxLen = (int) LZ4_MAX_COMPRESSED_LENGTH.invoke(compressor, data.length);

            byte[] compressed = new byte[maxLen + 4]; // 4 bytes for original length
            // Store original length
            compressed[0] = (byte) (data.length >> 24);
            compressed[1] = (byte) (data.length >> 16);
            compressed[2] = (byte) (data.length >> 8);
            compressed[3] = (byte) data.length;

            int compressedLen = (int) LZ4_COMPRESS_METHOD.invoke(compressor, data, 0, data.length, compressed, 4, maxLen);
            if (compressedLen < 0 || compressedLen > maxLen) {
                throw new OpenSerializationException("Invalid LZ4 compressed length: " + compressedLen);
            }

            byte[] result = new byte[compressedLen + 4];
            System.arraycopy(compressed, 0, result, 0, compressedLen + 4);
            return result;
        } catch (OpenSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw OpenSerializationException.compressionFailed("LZ4", e);
        }
    }

    private byte[] decompressLz4(byte[] data) {
        try {
            if (LZ4_FACTORY_CLASS == null) {
                throw new OpenSerializationException("LZ4 library (net.jpountz.lz4) is not available on the classpath");
            }

            // Read original length
            int originalLen = ((data[0] & 0xFF) << 24) |
                    ((data[1] & 0xFF) << 16) |
                    ((data[2] & 0xFF) << 8) |
                    (data[3] & 0xFF);

            if (originalLen < 0 || originalLen > MAX_DECOMPRESSED_SIZE) {
                throw new OpenSerializationException("LZ4 decompressed size exceeds limit: " + originalLen);
            }

            Object factory = LZ4_FASTEST_INSTANCE.invoke(null);
            Object decompressor = LZ4_FAST_DECOMPRESSOR.invoke(factory);

            byte[] result = new byte[originalLen];
            LZ4_DECOMPRESS_METHOD.invoke(decompressor, data, 4, result, 0, originalLen);

            return result;
        } catch (OpenSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw OpenSerializationException.decompressionFailed("LZ4", e);
        }
    }

    // ==================== Snappy Implementation | Snappy 实现 ====================

    private byte[] compressSnappy(byte[] data) {
        try {
            if (SNAPPY_COMPRESS == null) {
                throw new OpenSerializationException("Snappy library (org.xerial.snappy) is not available on the classpath");
            }
            return (byte[]) SNAPPY_COMPRESS.invoke(null, data);
        } catch (OpenSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw OpenSerializationException.compressionFailed("Snappy", e);
        }
    }

    private byte[] decompressSnappy(byte[] data) {
        try {
            if (SNAPPY_UNCOMPRESS == null) {
                throw new OpenSerializationException("Snappy library (org.xerial.snappy) is not available on the classpath");
            }
            return (byte[]) SNAPPY_UNCOMPRESS.invoke(null, data);
        } catch (OpenSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw OpenSerializationException.decompressionFailed("Snappy", e);
        }
    }

    // ==================== ZSTD Implementation | ZSTD 实现 ====================

    private byte[] compressZstd(byte[] data) {
        try {
            if (ZSTD_COMPRESS == null) {
                throw new OpenSerializationException("ZSTD library (com.github.luben.zstd) is not available on the classpath");
            }
            return (byte[]) ZSTD_COMPRESS.invoke(null, data);
        } catch (OpenSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw OpenSerializationException.compressionFailed("ZSTD", e);
        }
    }

    private byte[] decompressZstd(byte[] data) {
        try {
            if (ZSTD_DECOMPRESSED_SIZE == null || ZSTD_DECOMPRESS == null) {
                throw new OpenSerializationException("ZSTD library (com.github.luben.zstd) is not available on the classpath");
            }

            long decompressedSize = (long) ZSTD_DECOMPRESSED_SIZE.invoke(null, data);

            if (decompressedSize < 0 || decompressedSize > MAX_DECOMPRESSED_SIZE) {
                throw new OpenSerializationException("ZSTD decompressed size exceeds limit: " + decompressedSize);
            }

            return (byte[]) ZSTD_DECOMPRESS.invoke(null, data, (int) decompressedSize);
        } catch (OpenSerializationException e) {
            throw e;
        } catch (Exception e) {
            throw OpenSerializationException.decompressionFailed("ZSTD", e);
        }
    }
}

package cloud.opencode.base.cache.compression;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Value Compressor - Compresses and decompresses cache values
 * 值压缩器 - 压缩和解压缓存值
 *
 * <p>Provides pluggable compression for cache values to reduce memory footprint.</p>
 * <p>为缓存值提供可插拔的压缩，以减少内存占用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple algorithms - 多种算法支持</li>
 *   <li>Compression threshold - 压缩阈值</li>
 *   <li>Compression stats - 压缩统计</li>
 *   <li>Thread-safe - 线程安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create compressor
 * ValueCompressor compressor = ValueCompressor.gzip();
 *
 * // Compress data
 * byte[] compressed = compressor.compress(originalBytes);
 *
 * // Decompress data
 * byte[] decompressed = compressor.decompress(compressed);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cache V2.0.5
 */
public interface ValueCompressor {

    /**
     * Compress data
     * 压缩数据
     *
     * @param data original data | 原始数据
     * @return compressed data | 压缩后的数据
     * @throws CompressionException if compression fails | 压缩失败时抛出
     */
    byte[] compress(byte[] data) throws CompressionException;

    /**
     * Decompress data
     * 解压数据
     *
     * @param data compressed data | 压缩的数据
     * @return decompressed data | 解压后的数据
     * @throws CompressionException if decompression fails | 解压失败时抛出
     */
    byte[] decompress(byte[] data) throws CompressionException;

    /**
     * Get compression algorithm
     * 获取压缩算法
     *
     * @return algorithm | 算法
     */
    CompressionAlgorithm algorithm();

    /**
     * Get minimum size threshold for compression
     * 获取压缩的最小大小阈值
     *
     * @return threshold in bytes | 阈值（字节）
     */
    int compressionThreshold();

    /**
     * Check if data should be compressed based on size
     * 根据大小检查数据是否应该压缩
     *
     * @param dataSize data size in bytes | 数据大小（字节）
     * @return true if should compress | 如果应该压缩返回 true
     */
    default boolean shouldCompress(int dataSize) {
        return dataSize >= compressionThreshold();
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Create no-op compressor (pass-through)
     * 创建无操作压缩器（直通）
     *
     * @return no-op compressor | 无操作压缩器
     */
    static ValueCompressor none() {
        return new NoOpCompressor();
    }

    /**
     * Create GZIP compressor with default settings
     * 使用默认设置创建 GZIP 压缩器
     *
     * @return GZIP compressor | GZIP 压缩器
     */
    static ValueCompressor gzip() {
        return new GzipCompressor(1024);
    }

    /**
     * Create GZIP compressor with custom threshold
     * 使用自定义阈值创建 GZIP 压缩器
     *
     * @param threshold minimum bytes to trigger compression | 触发压缩的最小字节数
     * @return GZIP compressor | GZIP 压缩器
     */
    static ValueCompressor gzip(int threshold) {
        return new GzipCompressor(threshold);
    }

    /**
     * Create compressor builder
     * 创建压缩器构建器
     *
     * @return builder | 构建器
     */
    static Builder builder() {
        return new Builder();
    }

    // ==================== Implementations | 实现 ====================

    /**
     * No-op compressor that passes data through unchanged
     * 无操作压缩器，数据直接通过不变
     */
    class NoOpCompressor implements ValueCompressor {

        /** Creates a new NoOpCompressor instance | 创建新的 NoOpCompressor 实例 */
        public NoOpCompressor() {}
        @Override
        public byte[] compress(byte[] data) {
            return data;
        }

        @Override
        public byte[] decompress(byte[] data) {
            return data;
        }

        @Override
        public CompressionAlgorithm algorithm() {
            return CompressionAlgorithm.NONE;
        }

        @Override
        public int compressionThreshold() {
            return Integer.MAX_VALUE;
        }
    }

    /**
     * Maximum decompressed size limit to prevent decompression bombs (100 MB).
     * 最大解压大小限制，防止解压炸弹（100 MB）。
     */
    long MAX_DECOMPRESSED_SIZE = 100L * 1024 * 1024;

    /**
     * GZIP compressor implementation
     * GZIP 压缩器实现
     */
    class GzipCompressor implements ValueCompressor {
        private final int threshold;

        GzipCompressor(int threshold) {
            this.threshold = threshold;
        }

        @Override
        public byte[] compress(byte[] data) throws CompressionException {
            if (data == null || data.length == 0) {
                return data;
            }
            if (!shouldCompress(data.length)) {
                return data;
            }
            try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
                 GZIPOutputStream gzip = new GZIPOutputStream(baos)) {
                gzip.write(data);
                gzip.finish();
                byte[] compressed = baos.toByteArray();
                // Only return compressed if it's actually smaller
                return compressed.length < data.length ? compressed : data;
            } catch (IOException e) {
                throw new CompressionException("GZIP compression failed", e);
            }
        }

        @Override
        public byte[] decompress(byte[] data) throws CompressionException {
            if (data == null || data.length == 0) {
                return data;
            }
            // Check GZIP magic number
            if (data.length < 2 || (data[0] & 0xff) != 0x1f || (data[1] & 0xff) != 0x8b) {
                return data; // Not compressed
            }
            try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
                 GZIPInputStream gzip = new GZIPInputStream(bais);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int len;
                long totalRead = 0;
                while ((len = gzip.read(buffer)) != -1) {
                    totalRead += len;
                    if (totalRead > MAX_DECOMPRESSED_SIZE) {
                        throw new CompressionException(
                                "Decompressed data exceeds maximum allowed size of "
                                        + MAX_DECOMPRESSED_SIZE + " bytes (possible decompression bomb)");
                    }
                    baos.write(buffer, 0, len);
                }
                return baos.toByteArray();
            } catch (IOException e) {
                throw new CompressionException("GZIP decompression failed", e);
            }
        }

        @Override
        public CompressionAlgorithm algorithm() {
            return CompressionAlgorithm.GZIP;
        }

        @Override
        public int compressionThreshold() {
            return threshold;
        }
    }

    // ==================== Builder | 构建器 ====================

    /**
     * Builder for ValueCompressor
     * ValueCompressor 构建器
     */
    class Builder {

        /** Creates a new Builder instance | 创建新的 Builder 实例 */
        public Builder() {}
        private CompressionAlgorithm algorithm = CompressionAlgorithm.GZIP;
        private int threshold = 1024;
        private int level = -1; // -1 means use default

        /**
         * Set compression algorithm
         * 设置压缩算法
         *
         * @param algorithm the algorithm | 算法
         * @return this builder | 此构建器
         */
        public Builder algorithm(CompressionAlgorithm algorithm) {
            this.algorithm = algorithm;
            return this;
        }

        /**
         * Set compression threshold
         * 设置压缩阈值
         *
         * @param threshold minimum bytes to compress | 压缩的最小字节数
         * @return this builder | 此构建器
         */
        public Builder threshold(int threshold) {
            this.threshold = threshold;
            return this;
        }

        /**
         * Set compression level
         * 设置压缩级别
         *
         * @param level compression level (algorithm specific) | 压缩级别（算法特定）
         * @return this builder | 此构建器
         */
        public Builder level(int level) {
            this.level = level;
            return this;
        }

        /**
         * Build the compressor
         * 构建压缩器
         *
         * @return compressor | 压缩器
         */
        public ValueCompressor build() {
            return switch (algorithm) {
                case NONE -> new NoOpCompressor();
                case GZIP -> new GzipCompressor(threshold);
                case LZ4, ZSTD, SNAPPY -> new GzipCompressor(threshold); // Fallback to GZIP
            };
        }
    }

    // ==================== Exception | 异常 ====================

    /**
     * Exception thrown when compression/decompression fails
     * 压缩/解压失败时抛出的异常
     */
    class CompressionException extends RuntimeException {
        /**
         * CompressionException | CompressionException
         * @param message the message | message
         */
        public CompressionException(String message) {
            super(message);
        }

        /**
         * CompressionException | CompressionException
         * @param message the message | message
         * @param cause the cause | cause
         */
        public CompressionException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

package cloud.opencode.base.io.compress;

import cloud.opencode.base.io.exception.OpenIOOperationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Gzip Compression/Decompression Utility
 * Gzip压缩/解压缩工具类
 *
 * <p>Provides static utility methods for compressing and decompressing data
 * using the Gzip algorithm. Supports byte arrays, files, and streams.</p>
 * <p>提供使用Gzip算法压缩和解压缩数据的静态工具方法。
 * 支持字节数组、文件和流。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Byte array compression/decompression - 字节数组压缩/解压缩</li>
 *   <li>File compression/decompression - 文件压缩/解压缩</li>
 *   <li>Stream-based compression/decompression - 基于流的压缩/解压缩</li>
 *   <li>Gzip format detection - Gzip格式检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Compress byte array
 * byte[] compressed = GzipUtil.compress(data);
 * byte[] original = GzipUtil.decompress(compressed);
 *
 * // Compress file
 * GzipUtil.compress(sourcePath, targetPath);
 * GzipUtil.decompress(gzipPath, outputPath);
 *
 * // Check if data is gzipped
 * boolean isGzip = GzipUtil.isGzipped(data);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes, null inputs throw NullPointerException - 空值安全: 是，null输入抛出NullPointerException</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-io V1.0.3
 */
public final class GzipUtil {

    /**
     * Buffer size for stream operations (8KB)
     * 流操作的缓冲区大小（8KB）
     */
    private static final int BUFFER_SIZE = 8192;

    /**
     * Default maximum decompressed size (256 MB) to guard against gzip bombs
     * 默认最大解压大小（256 MB），防止gzip炸弹
     */
    public static final long DEFAULT_MAX_DECOMPRESSED_SIZE = 256L * 1024 * 1024;

    /**
     * First byte of Gzip magic number
     * Gzip魔数的第一个字节
     */
    private static final int GZIP_MAGIC_BYTE1 = 0x1f;

    /**
     * Second byte of Gzip magic number
     * Gzip魔数的第二个字节
     */
    private static final int GZIP_MAGIC_BYTE2 = 0x8b;

    private GzipUtil() {
        throw new AssertionError("No GzipUtil instances for you!");
    }

    // ==================== Compress | 压缩 ====================

    /**
     * Compresses a byte array using Gzip
     * 使用Gzip压缩字节数组
     *
     * @param data the data to compress | 要压缩的数据
     * @return compressed data | 压缩后的数据
     * @throws NullPointerException      if data is null | 当data为null时抛出
     * @throws OpenIOOperationException  if compression fails | 当压缩失败时抛出
     */
    public static byte[] compress(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        try (var baos = new ByteArrayOutputStream(Math.max(data.length / 2, 16));
             var gzos = new GZIPOutputStream(baos)) {
            gzos.write(data);
            gzos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new OpenIOOperationException("compress", null, "Failed to compress data with Gzip", e);
        }
    }

    /**
     * Compresses a file using Gzip
     * 使用Gzip压缩文件
     *
     * @param source the source file path | 源文件路径
     * @param target the target file path | 目标文件路径
     * @throws NullPointerException      if source or target is null | 当source或target为null时抛出
     * @throws OpenIOOperationException  if compression fails | 当压缩失败时抛出
     */
    public static void compress(Path source, Path target) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        try (var is = Files.newInputStream(source);
             var os = Files.newOutputStream(target);
             var gzos = new GZIPOutputStream(os)) {
            transferTo(is, gzos);
            gzos.finish();
        } catch (IOException e) {
            throw new OpenIOOperationException("compress", source.toString(),
                    String.format("Failed to compress file: %s", source), e);
        }
    }

    /**
     * Compresses data from an InputStream using Gzip
     * 使用Gzip压缩输入流中的数据
     *
     * @param input the input stream to compress | 要压缩的输入流
     * @return compressed data | 压缩后的数据
     * @throws NullPointerException      if input is null | 当input为null时抛出
     * @throws OpenIOOperationException  if compression fails | 当压缩失败时抛出
     */
    public static byte[] compress(InputStream input) {
        Objects.requireNonNull(input, "input must not be null");
        try (var baos = new ByteArrayOutputStream();
             var gzos = new GZIPOutputStream(baos)) {
            transferTo(input, gzos);
            gzos.finish();
            return baos.toByteArray();
        } catch (IOException e) {
            throw new OpenIOOperationException("compress", null, "Failed to compress stream with Gzip", e);
        }
    }

    // ==================== Decompress | 解压缩 ====================

    /**
     * Decompresses a Gzip-compressed byte array
     * 解压缩Gzip压缩的字节数组
     *
     * @param data the compressed data | 压缩的数据
     * @return decompressed data | 解压缩后的数据
     * @throws NullPointerException      if data is null | 当data为null时抛出
     * @throws OpenIOOperationException  if decompression fails | 当解压缩失败时抛出
     */
    public static byte[] decompress(byte[] data) {
        return decompress(data, DEFAULT_MAX_DECOMPRESSED_SIZE);
    }

    /**
     * Decompresses a Gzip-compressed byte array with a size limit
     * 带大小限制解压缩Gzip压缩的字节数组
     *
     * @param data    the compressed data | 压缩的数据
     * @param maxSize the maximum decompressed size in bytes | 最大解压大小（字节）
     * @return decompressed data | 解压缩后的数据
     * @throws NullPointerException      if data is null | 当data为null时抛出
     * @throws IllegalArgumentException  if maxSize is not positive | 当maxSize非正数时抛出
     * @throws OpenIOOperationException  if decompression fails or size limit exceeded | 当解压缩失败或超出大小限制时抛出
     */
    public static byte[] decompress(byte[] data, long maxSize) {
        Objects.requireNonNull(data, "data must not be null");
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }
        try (var bais = new ByteArrayInputStream(data);
             var gzis = new GZIPInputStream(bais);
             var baos = new ByteArrayOutputStream()) {
            boundedTransferTo(gzis, baos, maxSize);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new OpenIOOperationException("decompress", null, "Failed to decompress Gzip data", e);
        }
    }

    /**
     * Decompresses a Gzip file
     * 解压缩Gzip文件
     *
     * @param source the Gzip file path | Gzip文件路径
     * @param target the target file path | 目标文件路径
     * @throws NullPointerException      if source or target is null | 当source或target为null时抛出
     * @throws OpenIOOperationException  if decompression fails | 当解压缩失败时抛出
     */
    public static void decompress(Path source, Path target) {
        decompress(source, target, DEFAULT_MAX_DECOMPRESSED_SIZE);
    }

    /**
     * Decompresses a Gzip file with a size limit
     * 带大小限制解压缩Gzip文件
     *
     * @param source  the Gzip file path | Gzip文件路径
     * @param target  the target file path | 目标文件路径
     * @param maxSize the maximum decompressed size in bytes | 最大解压大小（字节）
     * @throws NullPointerException      if source or target is null | 当source或target为null时抛出
     * @throws IllegalArgumentException  if maxSize is not positive | 当maxSize非正数时抛出
     * @throws OpenIOOperationException  if decompression fails or size limit exceeded | 当解压缩失败或超出大小限制时抛出
     */
    public static void decompress(Path source, Path target, long maxSize) {
        Objects.requireNonNull(source, "source must not be null");
        Objects.requireNonNull(target, "target must not be null");
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }
        try (var is = Files.newInputStream(source);
             var gzis = new GZIPInputStream(is);
             var os = Files.newOutputStream(target)) {
            boundedTransferTo(gzis, os, maxSize);
        } catch (IOException e) {
            throw new OpenIOOperationException("decompress", source.toString(),
                    String.format("Failed to decompress file: %s", source), e);
        }
    }

    /**
     * Decompresses data from a Gzip-compressed InputStream
     * 从Gzip压缩的输入流中解压缩数据
     *
     * @param input the compressed input stream | 压缩的输入流
     * @return decompressed data | 解压缩后的数据
     * @throws NullPointerException      if input is null | 当input为null时抛出
     * @throws OpenIOOperationException  if decompression fails | 当解压缩失败时抛出
     */
    public static byte[] decompress(InputStream input) {
        return decompress(input, DEFAULT_MAX_DECOMPRESSED_SIZE);
    }

    /**
     * Decompresses data from a Gzip-compressed InputStream with a size limit
     * 带大小限制从Gzip压缩的输入流中解压缩数据
     *
     * @param input   the compressed input stream | 压缩的输入流
     * @param maxSize the maximum decompressed size in bytes | 最大解压大小（字节）
     * @return decompressed data | 解压缩后的数据
     * @throws NullPointerException      if input is null | 当input为null时抛出
     * @throws IllegalArgumentException  if maxSize is not positive | 当maxSize非正数时抛出
     * @throws OpenIOOperationException  if decompression fails or size limit exceeded | 当解压缩失败或超出大小限制时抛出
     */
    public static byte[] decompress(InputStream input, long maxSize) {
        Objects.requireNonNull(input, "input must not be null");
        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive: " + maxSize);
        }
        try (var gzis = new GZIPInputStream(input);
             var baos = new ByteArrayOutputStream()) {
            boundedTransferTo(gzis, baos, maxSize);
            return baos.toByteArray();
        } catch (IOException e) {
            throw new OpenIOOperationException("decompress", null, "Failed to decompress Gzip stream", e);
        }
    }

    // ==================== Stream Wrappers | 流包装 ====================

    /**
     * Returns a compressed InputStream that wraps the given input
     * 返回一个包装给定输入的压缩InputStream
     *
     * <p>The returned stream contains gzip-compressed data read from the input.
     * The input data is compressed in memory for simplicity and safety.</p>
     * <p>返回的流包含从输入读取的gzip压缩数据。
     * 为了简便和安全，输入数据在内存中压缩。</p>
     *
     * @param input the input stream to compress | 要压缩的输入流
     * @return an InputStream of compressed data | 压缩数据的InputStream
     * @throws NullPointerException      if input is null | 当input为null时抛出
     * @throws OpenIOOperationException  if compression fails | 当压缩失败时抛出
     */
    public static InputStream compressStream(InputStream input) {
        Objects.requireNonNull(input, "input must not be null");
        byte[] compressed = compress(input);
        return new ByteArrayInputStream(compressed);
    }

    /**
     * Returns a decompressed InputStream that wraps the given compressed input
     * 返回一个包装给定压缩输入的解压缩InputStream
     *
     * <p>The returned stream provides decompressed data on-the-fly using GZIPInputStream.</p>
     * <p>返回的流使用GZIPInputStream即时提供解压缩数据。</p>
     *
     * @param input the compressed input stream | 压缩的输入流
     * @return an InputStream of decompressed data | 解压缩数据的InputStream
     * @throws NullPointerException      if input is null | 当input为null时抛出
     * @throws OpenIOOperationException  if wrapping fails | 当包装失败时抛出
     */
    public static InputStream decompressStream(InputStream input) {
        Objects.requireNonNull(input, "input must not be null");
        try {
            return new GZIPInputStream(input);
        } catch (IOException e) {
            throw new OpenIOOperationException("decompress", null,
                    "Failed to create decompression stream", e);
        }
    }

    // ==================== Detection | 检测 ====================

    /**
     * Checks if the given byte array starts with Gzip magic bytes
     * 检查给定的字节数组是否以Gzip魔数开头
     *
     * @param data the data to check | 要检查的数据
     * @return true if data is Gzip compressed | 如果数据是Gzip压缩的返回true
     * @throws NullPointerException if data is null | 当data为null时抛出
     */
    public static boolean isGzipped(byte[] data) {
        Objects.requireNonNull(data, "data must not be null");
        return data.length >= 2
                && (data[0] & 0xFF) == GZIP_MAGIC_BYTE1
                && (data[1] & 0xFF) == GZIP_MAGIC_BYTE2;
    }

    /**
     * Checks if the file at the given path starts with Gzip magic bytes
     * 检查给定路径的文件是否以Gzip魔数开头
     *
     * @param path the file path to check | 要检查的文件路径
     * @return true if file is Gzip compressed | 如果文件是Gzip压缩的返回true
     * @throws NullPointerException      if path is null | 当path为null时抛出
     * @throws OpenIOOperationException  if reading the file fails | 当读取文件失败时抛出
     */
    public static boolean isGzipped(Path path) {
        Objects.requireNonNull(path, "path must not be null");
        try (var is = Files.newInputStream(path)) {
            int byte1 = is.read();
            int byte2 = is.read();
            return byte1 == GZIP_MAGIC_BYTE1 && byte2 == GZIP_MAGIC_BYTE2;
        } catch (IOException e) {
            throw new OpenIOOperationException("read", path.toString(),
                    String.format("Failed to read file header: %s", path), e);
        }
    }

    // ==================== Internal | 内部方法 ====================

    /**
     * Transfers all bytes from input to output using a buffer
     * 使用缓冲区将所有字节从输入传输到输出
     */
    private static void transferTo(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
    }

    /**
     * Transfers bytes with a size limit (gzip bomb protection)
     * 带大小限制的字节传输（gzip炸弹防护）
     */
    private static void boundedTransferTo(InputStream in, OutputStream out, long maxSize) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        long totalWritten = 0;
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            totalWritten += bytesRead;
            if (totalWritten > maxSize) {
                throw new OpenIOOperationException("decompress", null,
                        String.format("Decompressed data exceeds maximum size limit: %d bytes", maxSize));
            }
            out.write(buffer, 0, bytesRead);
        }
    }
}

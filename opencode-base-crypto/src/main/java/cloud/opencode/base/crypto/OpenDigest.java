package cloud.opencode.base.crypto;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.enums.DigestAlgorithm;
import cloud.opencode.base.crypto.exception.OpenCryptoException;
import cloud.opencode.base.crypto.hash.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Message digest facade for computing hash digests - Provides convenient API for various hash algorithms
 * 消息摘要门面类 - 为各种哈希算法提供便捷的 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SHA-2 family (SHA-256, SHA-384, SHA-512) - SHA-2 系列（SHA-256、SHA-384、SHA-512）</li>
 *   <li>SHA-3 family (SHA3-256, SHA3-512) - SHA-3 系列（SHA3-256、SHA3-512）</li>
 *   <li>SM3, BLAKE2b, BLAKE3 support - SM3、BLAKE2b、BLAKE3 支持</li>
 *   <li>File and stream digesting - 文件和流摘要</li>
 *   <li>Streaming (incremental) computation - 流式（增量）计算</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // One-shot hash
 * String hex = OpenDigest.sha256().digestHex("Hello");
 * 
 * // Streaming hash
 * String hash = OpenDigest.sha512()
 *     .update("part1")
 *     .update("part2")
 *     .doFinalHex();
 * 
 * // File hash
 * String fileHash = OpenDigest.sha256().digestFileHex(Path.of("file.txt"));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)，n为数据长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class OpenDigest {

    private final MessageDigest digest;
    private final String algorithm;

    private OpenDigest(String algorithm) {
        try {
            this.digest = MessageDigest.getInstance(algorithm);
            this.algorithm = algorithm;
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(algorithm);
        }
    }

    private OpenDigest(HashFunction hashFunction) {
        this.algorithm = hashFunction.getAlgorithm();
        try {
            this.digest = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(algorithm);
        }
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create SHA-256 digester
     * 创建 SHA-256 摘要器
     *
     * @return OpenDigest instance
     */
    public static OpenDigest sha256() {
        return new OpenDigest("SHA-256");
    }

    /**
     * Create SHA-384 digester
     * 创建 SHA-384 摘要器
     *
     * @return OpenDigest instance
     */
    public static OpenDigest sha384() {
        return new OpenDigest("SHA-384");
    }

    /**
     * Create SHA-512 digester
     * 创建 SHA-512 摘要器
     *
     * @return OpenDigest instance
     */
    public static OpenDigest sha512() {
        return new OpenDigest("SHA-512");
    }

    /**
     * Create SHA3-256 digester
     * 创建 SHA3-256 摘要器
     *
     * @return OpenDigest instance
     */
    public static OpenDigest sha3_256() {
        return new OpenDigest("SHA3-256");
    }

    /**
     * Create SHA3-512 digester
     * 创建 SHA3-512 摘要器
     *
     * @return OpenDigest instance
     */
    public static OpenDigest sha3_512() {
        return new OpenDigest("SHA3-512");
    }

    /**
     * Create SM3 digester (requires Bouncy Castle)
     * 创建 SM3 摘要器（需要 Bouncy Castle）
     *
     * @return OpenDigest instance
     */
    public static OpenDigest sm3() {
        return new OpenDigest("SM3");
    }

    /**
     * Create BLAKE2b digester (requires Bouncy Castle)
     * 创建 BLAKE2b 摘要器（需要 Bouncy Castle）
     *
     * @param digestLength digest length in bytes
     * @return OpenDigest instance
     */
    public static OpenDigest blake2b(int digestLength) {
        return new OpenDigest("BLAKE2B-" + (digestLength * 8));
    }

    /**
     * Create BLAKE3 digester (requires Bouncy Castle)
     * 创建 BLAKE3 摘要器（需要 Bouncy Castle）
     *
     * @return OpenDigest instance
     */
    public static OpenDigest blake3() {
        return new OpenDigest("BLAKE3-256");
    }

    /**
     * Create digester by algorithm enum
     * 根据算法枚举创建摘要器
     *
     * @param algorithm digest algorithm
     * @return OpenDigest instance
     */
    public static OpenDigest of(DigestAlgorithm algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm cannot be null");
        }
        return new OpenDigest(algorithm.getAlgorithmName());
    }

    // ==================== One-shot Computation ====================

    /**
     * Compute digest of byte array
     * 计算字节数组的摘要
     *
     * @param data input data
     * @return digest bytes
     */
    public byte[] digest(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return digest.digest(data);
    }

    /**
     * Compute digest of string (UTF-8)
     * 计算字符串的摘要（UTF-8）
     *
     * @param data input string
     * @return digest bytes
     */
    public byte[] digest(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return digest(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Compute digest and return as hex string
     * 计算摘要并返回十六进制字符串
     *
     * @param data input data
     * @return hex string
     */
    public String digestHex(byte[] data) {
        return HexCodec.encode(digest(data));
    }

    /**
     * Compute digest of string and return as hex string
     * 计算字符串摘要并返回十六进制字符串
     *
     * @param data input string
     * @return hex string
     */
    public String digestHex(String data) {
        return HexCodec.encode(digest(data));
    }

    /**
     * Compute digest and return as Base64 string
     * 计算摘要并返回 Base64 字符串
     *
     * @param data input data
     * @return Base64 string
     */
    public String digestBase64(byte[] data) {
        return OpenBase64.encode(digest(data));
    }

    /**
     * Compute digest of string and return as Base64 string
     * 计算字符串摘要并返回 Base64 字符串
     *
     * @param data input string
     * @return Base64 string
     */
    public String digestBase64(String data) {
        return OpenBase64.encode(digest(data));
    }

    /**
     * Compute digest of file
     * 计算文件摘要
     *
     * @param file file path
     * @return digest bytes
     */
    public byte[] digestFile(Path file) {
        if (file == null) {
            throw new NullPointerException("File path cannot be null");
        }
        try (InputStream is = Files.newInputStream(file)) {
            return digest(is);
        } catch (IOException e) {
            throw new OpenCryptoException(algorithm, "digestFile", "Failed to read file: " + file, e);
        }
    }

    /**
     * Compute digest of file and return as hex string
     * 计算文件摘要并返回十六进制字符串
     *
     * @param file file path
     * @return hex string
     */
    public String digestFileHex(Path file) {
        return HexCodec.encode(digestFile(file));
    }

    /**
     * Compute digest of input stream
     * 计算输入流摘要
     *
     * @param input input stream
     * @return digest bytes
     */
    public byte[] digest(InputStream input) {
        if (input == null) {
            throw new NullPointerException("InputStream cannot be null");
        }
        try {
            digest.reset();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) != -1) {
                digest.update(buffer, 0, read);
            }
            return digest.digest();
        } catch (IOException e) {
            throw new OpenCryptoException(algorithm, "digest", "Failed to read stream", e);
        }
    }

    /**
     * Compute digest of input stream and return as hex string
     * 计算输入流摘要并返回十六进制字符串
     *
     * @param input input stream
     * @return hex string
     */
    public String digestHex(InputStream input) {
        return HexCodec.encode(digest(input));
    }

    // ==================== Streaming Computation ====================

    /**
     * Update digest with data
     * 更新摘要数据
     *
     * @param data data to add
     * @return this instance for chaining
     */
    public OpenDigest update(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        digest.update(data);
        return this;
    }

    /**
     * Update digest with partial data
     * 更新摘要部分数据
     *
     * @param data data array
     * @param offset start offset
     * @param length number of bytes
     * @return this instance for chaining
     */
    public OpenDigest update(byte[] data, int offset, int length) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        digest.update(data, offset, length);
        return this;
    }

    /**
     * Update digest with string (UTF-8)
     * 更新摘要字符串（UTF-8）
     *
     * @param data string to add
     * @return this instance for chaining
     */
    public OpenDigest update(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return update(data.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Update digest with ByteBuffer
     * 更新摘要 ByteBuffer
     *
     * @param buffer ByteBuffer to add
     * @return this instance for chaining
     */
    public OpenDigest update(ByteBuffer buffer) {
        if (buffer == null) {
            throw new NullPointerException("Buffer cannot be null");
        }
        digest.update(buffer);
        return this;
    }

    /**
     * Complete computation and return digest
     * 完成计算并返回摘要
     *
     * @return digest bytes
     */
    public byte[] doFinal() {
        return digest.digest();
    }

    /**
     * Complete computation and return as hex string
     * 完成计算并返回十六进制字符串
     *
     * @return hex string
     */
    public String doFinalHex() {
        return HexCodec.encode(doFinal());
    }

    /**
     * Complete computation and return as Base64 string
     * 完成计算并返回 Base64 字符串
     *
     * @return Base64 string
     */
    public String doFinalBase64() {
        return OpenBase64.encode(doFinal());
    }

    /**
     * Reset digest state
     * 重置摘要状态
     *
     * @return this instance for chaining
     */
    public OpenDigest reset() {
        digest.reset();
        return this;
    }

    // ==================== Info Methods ====================

    /**
     * Get algorithm name
     * 获取算法名称
     *
     * @return algorithm name
     */
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Get digest length in bytes
     * 获取摘要长度（字节）
     *
     * @return digest length
     */
    public int getDigestLength() {
        return digest.getDigestLength();
    }
}

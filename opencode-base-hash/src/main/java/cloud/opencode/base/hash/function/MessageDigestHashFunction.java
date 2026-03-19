package cloud.opencode.base.hash.function;

import cloud.opencode.base.hash.HashCode;
import cloud.opencode.base.hash.Hasher;
import cloud.opencode.base.hash.exception.OpenHashException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MessageDigest-based hash function implementation
 * 基于 MessageDigest 的哈希函数实现
 *
 * <p>Wraps JDK's MessageDigest to provide cryptographic hash functions
 * with a unified API. Supports MD5, SHA-1, SHA-256, SHA-512, and SHA-3.</p>
 * <p>封装 JDK 的 MessageDigest 以提供统一 API 的加密哈希函数。
 * 支持 MD5、SHA-1、SHA-256、SHA-512 和 SHA-3。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>MD5 (128-bit, not for security) - MD5（128位，不用于安全）</li>
 *   <li>SHA-1 (160-bit, not for security) - SHA-1（160位，不用于安全）</li>
 *   <li>SHA-256 (256-bit) - SHA-256（256位）</li>
 *   <li>SHA-512 (512-bit) - SHA-512（512位）</li>
 *   <li>SHA3-256 (256-bit) - SHA3-256（256位）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // SHA-256
 * HashCode hash = MessageDigestHashFunction.sha256().hashUtf8("Hello");
 *
 * // SHA3-256
 * HashCode sha3 = MessageDigestHashFunction.sha3_256().hashBytes(data);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>MD5/SHA-1 for checksums only - MD5/SHA-1 仅用于校验</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = input size - O(n), n为输入大小</li>
 *   <li>Space complexity: O(1) for digest state - 摘要状态 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-hash V1.0.0
 */
public final class MessageDigestHashFunction extends AbstractHashFunction {

    private final String algorithm;

    private MessageDigestHashFunction(String algorithm, int bits) {
        super(bits, algorithm);
        this.algorithm = algorithm;
        // Validate algorithm exists
        try {
            MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw OpenHashException.algorithmNotSupported(algorithm);
        }
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an MD5 function (128-bit)
     * 创建 MD5 函数（128位）
     *
     * <p><b>Warning:</b> MD5 is cryptographically broken. Use only for checksums.</p>
     * <p><b>警告：</b> MD5 已被破解。仅用于校验。</p>
     *
     * @return hash function | 哈希函数
     */
    public static MessageDigestHashFunction md5() {
        return new MessageDigestHashFunction("MD5", 128);
    }

    /**
     * Creates a SHA-1 function (160-bit)
     * 创建 SHA-1 函数（160位）
     *
     * <p><b>Warning:</b> SHA-1 is cryptographically weak. Use only for checksums.</p>
     * <p><b>警告：</b> SHA-1 较弱。仅用于校验。</p>
     *
     * @return hash function | 哈希函数
     */
    public static MessageDigestHashFunction sha1() {
        return new MessageDigestHashFunction("SHA-1", 160);
    }

    /**
     * Creates a SHA-256 function (256-bit)
     * 创建 SHA-256 函数（256位）
     *
     * @return hash function | 哈希函数
     */
    public static MessageDigestHashFunction sha256() {
        return new MessageDigestHashFunction("SHA-256", 256);
    }

    /**
     * Creates a SHA-512 function (512-bit)
     * 创建 SHA-512 函数（512位）
     *
     * @return hash function | 哈希函数
     */
    public static MessageDigestHashFunction sha512() {
        return new MessageDigestHashFunction("SHA-512", 512);
    }

    /**
     * Creates a SHA3-256 function (256-bit)
     * 创建 SHA3-256 函数（256位）
     *
     * @return hash function | 哈希函数
     */
    public static MessageDigestHashFunction sha3_256() {
        return new MessageDigestHashFunction("SHA3-256", 256);
    }

    /**
     * Creates a SHA3-512 function (512-bit)
     * 创建 SHA3-512 函数（512位）
     *
     * @return hash function | 哈希函数
     */
    public static MessageDigestHashFunction sha3_512() {
        return new MessageDigestHashFunction("SHA3-512", 512);
    }

    /**
     * Creates a hash function for a custom algorithm
     * 为自定义算法创建哈希函数
     *
     * @param algorithm algorithm name (must be supported by JDK) | 算法名称（必须被JDK支持）
     * @param bits      output bits | 输出位数
     * @return hash function | 哈希函数
     * @throws OpenHashException if algorithm is not supported | 如果算法不支持
     */
    public static MessageDigestHashFunction create(String algorithm, int bits) {
        return new MessageDigestHashFunction(algorithm, bits);
    }

    @Override
    public Hasher newHasher() {
        return new MessageDigestHasher(algorithm);
    }

    @Override
    public HashCode hashBytes(byte[] input, int offset, int length) {
        try {
            MessageDigest md = MessageDigest.getInstance(algorithm);
            md.update(input, offset, length);
            return HashCode.fromBytes(md.digest());
        } catch (NoSuchAlgorithmException e) {
            throw OpenHashException.algorithmNotSupported(algorithm);
        }
    }

    // ==================== Hasher Implementation | Hasher实现 ====================

    private static class MessageDigestHasher extends AbstractHasher {
        private final MessageDigest digest;

        MessageDigestHasher(String algorithm) {
            try {
                this.digest = MessageDigest.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                throw OpenHashException.algorithmNotSupported(algorithm);
            }
        }

        @Override
        public Hasher putByte(byte b) {
            digest.update(b);
            return this;
        }

        @Override
        public Hasher putBytes(byte[] bytes, int offset, int length) {
            digest.update(bytes, offset, length);
            return this;
        }

        @Override
        protected HashCode doHash() {
            return HashCode.fromBytes(digest.digest());
        }
    }
}

package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * BLAKE2 hash function implementation - High-speed cryptographic hash function (BLAKE2b, BLAKE2s)
 * BLAKE2 哈希函数实现 - 高速加密哈希函数（BLAKE2b、BLAKE2s）
 *
 * <p>Requires Bouncy Castle provider to be available in the classpath.
 * 需要 Bouncy Castle 提供者在类路径中可用。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>BLAKE2b and BLAKE2s algorithms - BLAKE2b 和 BLAKE2s 算法</li>
 *   <li>Configurable digest length - 可配置的摘要长度</li>
 *   <li>Requires Bouncy Castle provider - 需要 Bouncy Castle 提供者</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Blake2Hash blake2 = Blake2Hash.blake2b256();
 * String hex = blake2.hashHex("data");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
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
public final class Blake2Hash implements HashFunction {

    private static final String BC_PROVIDER = "BC";
    private static final int BLAKE2B_MAX_LENGTH = 64; // 512 bits
    private static final int BLAKE2S_MAX_LENGTH = 32; // 256 bits

    private final String algorithm;
    private final int digestLength;

    private Blake2Hash(String algorithm, int digestLength) {
        ensureBouncyCastleAvailable();
        this.algorithm = algorithm;
        this.digestLength = digestLength;
    }

    /**
     * Create BLAKE2b hash function with custom digest length
     * 创建指定摘要长度的 BLAKE2b 哈希函数
     *
     * @param digestLength digest length in bytes (1-64)
     * @return BLAKE2b hash function instance
     * @throws IllegalArgumentException if digest length is invalid
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Blake2Hash blake2b(int digestLength) {
        if (digestLength < 1 || digestLength > BLAKE2B_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "BLAKE2b digest length must be between 1 and " + BLAKE2B_MAX_LENGTH + " bytes");
        }
        return new Blake2Hash("BLAKE2B-" + (digestLength * 8), digestLength);
    }

    /**
     * Create BLAKE2b-256 hash function (256-bit digest)
     * 创建 BLAKE2b-256 哈希函数（256 位摘要）
     *
     * @return BLAKE2b-256 hash function instance
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Blake2Hash blake2b256() {
        return blake2b(32);
    }

    /**
     * Create BLAKE2b-512 hash function (512-bit digest)
     * 创建 BLAKE2b-512 哈希函数（512 位摘要）
     *
     * @return BLAKE2b-512 hash function instance
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Blake2Hash blake2b512() {
        return blake2b(64);
    }

    /**
     * Create BLAKE2s hash function with custom digest length
     * 创建指定摘要长度的 BLAKE2s 哈希函数
     *
     * @param digestLength digest length in bytes (1-32)
     * @return BLAKE2s hash function instance
     * @throws IllegalArgumentException if digest length is invalid
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Blake2Hash blake2s(int digestLength) {
        if (digestLength < 1 || digestLength > BLAKE2S_MAX_LENGTH) {
            throw new IllegalArgumentException(
                "BLAKE2s digest length must be between 1 and " + BLAKE2S_MAX_LENGTH + " bytes");
        }
        return new Blake2Hash("BLAKE2S-" + (digestLength * 8), digestLength);
    }

    /**
     * Create BLAKE2s-256 hash function (256-bit digest)
     * 创建 BLAKE2s-256 哈希函数（256 位摘要）
     *
     * @return BLAKE2s-256 hash function instance
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Blake2Hash blake2s256() {
        return blake2s(32);
    }

    @Override
    public byte[] hash(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        try {
            MessageDigest digest = getMessageDigest();
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(algorithm);
        }
    }

    @Override
    public byte[] hash(String data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        return hash(data.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String hashHex(byte[] data) {
        return HexCodec.encode(hash(data));
    }

    @Override
    public String hashHex(String data) {
        return HexCodec.encode(hash(data));
    }

    @Override
    public String hashBase64(byte[] data) {
        return OpenBase64.encode(hash(data));
    }

    @Override
    public int getDigestLength() {
        return digestLength;
    }

    @Override
    public String getAlgorithm() {
        return algorithm;
    }

    /**
     * Ensure Bouncy Castle provider is available
     * 确保 Bouncy Castle 提供者可用
     *
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    private void ensureBouncyCastleAvailable() {
        if (Security.getProvider(BC_PROVIDER) == null) {
            throw new OpenCryptoException(algorithm, "initialization",
                "BLAKE2 requires Bouncy Castle provider. Add 'org.bouncycastle:bcprov-jdk18on' to dependencies.");
        }
    }

    /**
     * Get MessageDigest instance for BLAKE2
     * 获取 BLAKE2 的 MessageDigest 实例
     *
     * @return MessageDigest instance
     * @throws NoSuchAlgorithmException if algorithm is not available
     */
    private MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        try {
            return MessageDigest.getInstance(algorithm, BC_PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to default provider
            return MessageDigest.getInstance(algorithm);
        } catch (Exception e) {
            throw new NoSuchAlgorithmException(algorithm + " not available", e);
        }
    }
}

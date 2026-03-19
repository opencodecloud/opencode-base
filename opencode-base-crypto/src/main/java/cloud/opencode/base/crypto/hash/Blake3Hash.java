package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * BLAKE3 hash function implementation - Next-generation cryptographic hash function
 * BLAKE3 哈希函数实现 - 下一代加密哈希函数
 *
 * <p>Requires Bouncy Castle provider to be available in the classpath.
 * 需要 Bouncy Castle 提供者在类路径中可用。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>BLAKE3 hash algorithm - BLAKE3 哈希算法</li>
 *   <li>256-bit digest output - 256 位摘要输出</li>
 *   <li>Requires Bouncy Castle provider - 需要 Bouncy Castle 提供者</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Blake3Hash blake3 = Blake3Hash.create();
 * String hex = blake3.hashHex("data");
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
public final class Blake3Hash implements HashFunction {

    private static final String ALGORITHM = "BLAKE3";
    private static final String BC_PROVIDER = "BC";
    private static final int DEFAULT_DIGEST_LENGTH = 32; // 256 bits

    private final int digestLength;

    private Blake3Hash(int digestLength) {
        ensureBouncyCastleAvailable();
        this.digestLength = digestLength;
    }

    /**
     * Create BLAKE3 hash function with default digest length (256-bit)
     * 创建 BLAKE3 哈希函数（默认 256 位摘要）
     *
     * @return BLAKE3 hash function instance
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Blake3Hash create() {
        return new Blake3Hash(DEFAULT_DIGEST_LENGTH);
    }

    /**
     * Create BLAKE3 hash function with custom digest length
     * 创建指定摘要长度的 BLAKE3 哈希函数
     *
     * <p>BLAKE3 supports arbitrary output lengths.
     * BLAKE3 支持任意输出长度。
     *
     * @param digestLength digest length in bytes (must be positive)
     * @return BLAKE3 hash function instance
     * @throws IllegalArgumentException if digest length is not positive
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Blake3Hash create(int digestLength) {
        if (digestLength < 1) {
            throw new IllegalArgumentException("Digest length must be positive");
        }
        return new Blake3Hash(digestLength);
    }

    @Override
    public byte[] hash(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        try {
            MessageDigest digest = getMessageDigest();
            byte[] fullHash = digest.digest(data);

            // BLAKE3 may return default length, truncate if needed
            if (fullHash.length > digestLength) {
                byte[] truncated = new byte[digestLength];
                System.arraycopy(fullHash, 0, truncated, 0, digestLength);
                return truncated;
            }
            return fullHash;
        } catch (NoSuchAlgorithmException e) {
            throw OpenCryptoException.algorithmNotAvailable(ALGORITHM);
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
        return ALGORITHM;
    }

    /**
     * Ensure Bouncy Castle provider is available
     * 确保 Bouncy Castle 提供者可用
     *
     * @throws OpenCryptoException if Bouncy Castle is not available
     */
    private void ensureBouncyCastleAvailable() {
        if (Security.getProvider(BC_PROVIDER) == null) {
            throw new OpenCryptoException(ALGORITHM, "initialization",
                "BLAKE3 requires Bouncy Castle provider. Add 'org.bouncycastle:bcprov-jdk18on' to dependencies.");
        }
    }

    /**
     * Get MessageDigest instance for BLAKE3
     * 获取 BLAKE3 的 MessageDigest 实例
     *
     * @return MessageDigest instance
     * @throws NoSuchAlgorithmException if algorithm is not available
     */
    private MessageDigest getMessageDigest() throws NoSuchAlgorithmException {
        try {
            return MessageDigest.getInstance(ALGORITHM, BC_PROVIDER);
        } catch (NoSuchAlgorithmException e) {
            // Fallback to default provider
            return MessageDigest.getInstance(ALGORITHM);
        } catch (Exception e) {
            throw new NoSuchAlgorithmException(ALGORITHM + " not available", e);
        }
    }
}

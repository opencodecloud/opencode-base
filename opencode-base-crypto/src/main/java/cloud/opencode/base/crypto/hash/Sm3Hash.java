package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;

/**
 * SM3 hash function implementation - Chinese national cryptographic standard hash algorithm
 * SM3 哈希函数实现 - 中国国密标准哈希算法
 *
 * <p>Requires Bouncy Castle provider to be available in the classpath.
 * 需要 Bouncy Castle 提供者在类路径中可用。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Chinese national standard hash (GB/T 32905-2016) - 中国国密标准哈希（GB/T 32905-2016）</li>
 *   <li>256-bit digest output - 256 位摘要输出</li>
 *   <li>Requires Bouncy Castle provider - 需要 Bouncy Castle 提供者</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Sm3Hash sm3 = Sm3Hash.create();
 * String hex = sm3.hashHex("data");
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
public final class Sm3Hash implements HashFunction {

    private static final String ALGORITHM = "SM3";
    private static final int DIGEST_LENGTH = 32; // 256 bits
    private static final String BC_PROVIDER = "BC";

    private Sm3Hash() {
        ensureBouncyCastleAvailable();
    }

    /**
     * Create SM3 hash function (256-bit digest)
     * 创建 SM3 哈希函数（256 位摘要）
     *
     * @return SM3 hash function instance
     * @throws OpenCryptoException if Bouncy Castle provider is not available
     */
    public static Sm3Hash create() {
        return new Sm3Hash();
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
        return DIGEST_LENGTH;
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
                "SM3 requires Bouncy Castle provider. Add 'org.bouncycastle:bcprov-jdk18on' to dependencies.");
        }
    }

    /**
     * Get MessageDigest instance for SM3
     * 获取 SM3 的 MessageDigest 实例
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

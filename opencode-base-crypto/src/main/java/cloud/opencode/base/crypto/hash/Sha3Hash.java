package cloud.opencode.base.crypto.hash;

import cloud.opencode.base.core.OpenBase64;
import cloud.opencode.base.crypto.codec.HexCodec;
import cloud.opencode.base.crypto.exception.OpenCryptoException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * SHA-3 family hash function implementation - Secure Hash Algorithm 3 (SHA3-224, SHA3-256, SHA3-384, SHA3-512)
 * SHA-3 系列哈希函数实现 - 安全哈希算法 3（SHA3-224、SHA3-256、SHA3-384、SHA3-512）
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>SHA3-224, SHA3-256, SHA3-384, SHA3-512 algorithms - SHA3-224、SHA3-256、SHA3-384、SHA3-512 算法</li>
 *   <li>Hex and Base64 output encoding - 十六进制和 Base64 输出编码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Sha3Hash sha3 = Sha3Hash.sha3_256();
 * String hex = sha3.hashHex("data");
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
public final class Sha3Hash implements HashFunction {

    private final String algorithm;
    private final int digestLength;

    private Sha3Hash(String algorithm, int digestLength) {
        this.algorithm = algorithm;
        this.digestLength = digestLength;
    }

    /**
     * Create SHA3-224 hash function (224-bit digest)
     * 创建 SHA3-224 哈希函数（224 位摘要）
     *
     * @return SHA3-224 hash function instance
     */
    public static Sha3Hash sha3_224() {
        return new Sha3Hash("SHA3-224", 28);
    }

    /**
     * Create SHA3-256 hash function (256-bit digest)
     * 创建 SHA3-256 哈希函数（256 位摘要）
     *
     * @return SHA3-256 hash function instance
     */
    public static Sha3Hash sha3_256() {
        return new Sha3Hash("SHA3-256", 32);
    }

    /**
     * Create SHA3-384 hash function (384-bit digest)
     * 创建 SHA3-384 哈希函数（384 位摘要）
     *
     * @return SHA3-384 hash function instance
     */
    public static Sha3Hash sha3_384() {
        return new Sha3Hash("SHA3-384", 48);
    }

    /**
     * Create SHA3-512 hash function (512-bit digest)
     * 创建 SHA3-512 哈希函数（512 位摘要）
     *
     * @return SHA3-512 hash function instance
     */
    public static Sha3Hash sha3_512() {
        return new Sha3Hash("SHA3-512", 64);
    }

    @Override
    public byte[] hash(byte[] data) {
        if (data == null) {
            throw new NullPointerException("Data cannot be null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
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
}

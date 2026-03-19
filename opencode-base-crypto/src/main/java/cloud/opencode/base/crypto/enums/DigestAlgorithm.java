package cloud.opencode.base.crypto.enums;

/**
 * Digest algorithm enumeration for cryptographic hash functions - 摘要算法枚举
 * 加密哈希函数的摘要算法枚举
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All supported digest algorithm definitions - 所有支持的摘要算法定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DigestAlgorithm alg = DigestAlgorithm.SHA_256;
 * String name = alg.getAlgorithmName();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Partial - 空值安全: 部分</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - 时间复杂度: O(1)</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public enum DigestAlgorithm {
    /**
     * MD5 algorithm - 128-bit digest (Not recommended for security)
     * @deprecated MD5 is cryptographically broken and should not be used for security purposes.
     *             Use SHA-256 or SHA3-256 instead.
     */
    @Deprecated(since = "1.0.0", forRemoval = false)
    MD5("MD5", 128),

    /**
     * SHA-1 algorithm - 160-bit digest (Not recommended for security)
     * @deprecated SHA-1 is cryptographically weak and should not be used for security purposes.
     *             Use SHA-256 or SHA3-256 instead.
     */
    @Deprecated(since = "1.0.0", forRemoval = false)
    SHA1("SHA-1", 160),

    /**
     * SHA-224 algorithm - 224-bit digest
     */
    SHA224("SHA-224", 224),

    /**
     * SHA-256 algorithm - 256-bit digest (Recommended)
     */
    SHA256("SHA-256", 256),

    /**
     * SHA-384 algorithm - 384-bit digest
     */
    SHA384("SHA-384", 384),

    /**
     * SHA-512 algorithm - 512-bit digest
     */
    SHA512("SHA-512", 512),

    /**
     * SHA3-224 algorithm - 224-bit digest
     */
    SHA3_224("SHA3-224", 224),

    /**
     * SHA3-256 algorithm - 256-bit digest (Recommended)
     */
    SHA3_256("SHA3-256", 256),

    /**
     * SHA3-384 algorithm - 384-bit digest
     */
    SHA3_384("SHA3-384", 384),

    /**
     * SHA3-512 algorithm - 512-bit digest
     */
    SHA3_512("SHA3-512", 512),

    /**
     * SM3 algorithm - 256-bit digest (Chinese national standard)
     */
    SM3("SM3", 256),

    /**
     * BLAKE2B-256 algorithm - 256-bit digest
     */
    BLAKE2B_256("BLAKE2B-256", 256),

    /**
     * BLAKE2B-512 algorithm - 512-bit digest
     */
    BLAKE2B_512("BLAKE2B-512", 512),

    /**
     * BLAKE3 algorithm - 256-bit digest
     */
    BLAKE3("BLAKE3", 256);

    private final String algorithmName;
    private final int digestLength;

    /**
     * Constructor for DigestAlgorithm
     *
     * @param algorithmName the standard algorithm name
     * @param digestLength  the digest length in bits
     */
    DigestAlgorithm(String algorithmName, int digestLength) {
        this.algorithmName = algorithmName;
        this.digestLength = digestLength;
    }

    /**
     * Gets the standard algorithm name
     * 获取标准算法名称
     *
     * @return the algorithm name
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Gets the digest length in bits
     * 获取摘要长度（位）
     *
     * @return the digest length
     */
    public int getDigestLength() {
        return digestLength;
    }

    /**
     * Checks if the algorithm is considered secure
     * 检查算法是否被认为是安全的
     * <p>
     * MD5 and SHA-1 are considered insecure due to collision vulnerabilities
     *
     * @return true if the algorithm is secure, false otherwise
     */
    public boolean isSecure() {
        return this != MD5 && this != SHA1;
    }
}

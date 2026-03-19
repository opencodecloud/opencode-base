package cloud.opencode.base.crypto.enums;

/**
 * Digital signature algorithm enumeration - 数字签名算法枚举
 * 数字签名算法的枚举定义
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All supported signature algorithm definitions - 所有支持的签名算法定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SignatureAlgorithm alg = SignatureAlgorithm.ED25519;
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
public enum SignatureAlgorithm {
    /**
     * Ed25519 signature algorithm (Recommended, EdDSA)
     */
    ED25519("Ed25519", "EdDSA"),

    /**
     * Ed448 signature algorithm (Recommended, EdDSA)
     */
    ED448("Ed448", "EdDSA"),

    /**
     * ECDSA with P-256 curve and SHA-256
     */
    ECDSA_P256_SHA256("SHA256withECDSA", "EC"),

    /**
     * ECDSA with P-384 curve and SHA-384
     */
    ECDSA_P384_SHA384("SHA384withECDSA", "EC"),

    /**
     * ECDSA with P-521 curve and SHA-512
     */
    ECDSA_P521_SHA512("SHA512withECDSA", "EC"),

    /**
     * RSA signature with SHA-256
     */
    RSA_SHA256("SHA256withRSA", "RSA"),

    /**
     * RSA signature with SHA-384
     */
    RSA_SHA384("SHA384withRSA", "RSA"),

    /**
     * RSA signature with SHA-512
     */
    RSA_SHA512("SHA512withRSA", "RSA"),

    /**
     * RSA-PSS signature with SHA-256 (Recommended)
     */
    RSA_PSS_SHA256("SHA256withRSA/PSS", "RSA"),

    /**
     * RSA-PSS signature with SHA-384 (Recommended)
     */
    RSA_PSS_SHA384("SHA384withRSA/PSS", "RSA"),

    /**
     * RSA-PSS signature with SHA-512 (Recommended)
     */
    RSA_PSS_SHA512("SHA512withRSA/PSS", "RSA"),

    /**
     * SM2 signature with SM3 hash (Chinese national standard)
     */
    SM2("SM3withSM2", "SM2");

    private final String algorithmName;
    private final String keyAlgorithm;

    /**
     * Constructor for SignatureAlgorithm
     *
     * @param algorithmName the signature algorithm name
     * @param keyAlgorithm  the key algorithm type
     */
    SignatureAlgorithm(String algorithmName, String keyAlgorithm) {
        this.algorithmName = algorithmName;
        this.keyAlgorithm = keyAlgorithm;
    }

    /**
     * Gets the signature algorithm name
     * 获取签名算法名称
     *
     * @return the algorithm name
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Gets the key algorithm type
     * 获取密钥算法类型
     *
     * @return the key algorithm (e.g., "RSA", "EC", "EdDSA")
     */
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    /**
     * Checks if this algorithm is recommended for use
     * 检查算法是否推荐使用
     * <p>
     * EdDSA and RSA-PSS variants are generally recommended for modern applications
     *
     * @return true if the algorithm is recommended, false otherwise
     */
    public boolean isRecommended() {
        return this == ED25519 ||
               this == ED448 ||
               this == RSA_PSS_SHA256 ||
               this == RSA_PSS_SHA384 ||
               this == RSA_PSS_SHA512;
    }
}

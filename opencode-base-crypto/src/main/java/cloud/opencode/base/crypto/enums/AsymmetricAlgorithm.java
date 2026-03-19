package cloud.opencode.base.crypto.enums;

/**
 * Asymmetric encryption algorithm enumeration - 非对称加密算法枚举
 * 非对称加密算法的枚举定义
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All supported asymmetric algorithm definitions - 所有支持的非对称算法定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AsymmetricAlgorithm alg = AsymmetricAlgorithm.RSA_OAEP_SHA256;
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
public enum AsymmetricAlgorithm {
    /**
     * RSA with OAEP padding and SHA-256 (Recommended)
     */
    RSA_OAEP_SHA256("RSA/ECB/OAEPWithSHA-256AndMGF1Padding", 2048),

    /**
     * RSA with OAEP padding and SHA-384 (Recommended)
     */
    RSA_OAEP_SHA384("RSA/ECB/OAEPWithSHA-384AndMGF1Padding", 2048),

    /**
     * RSA with OAEP padding and SHA-512 (Recommended)
     */
    RSA_OAEP_SHA512("RSA/ECB/OAEPWithSHA-512AndMGF1Padding", 2048),

    /**
     * RSA with PKCS1 padding (Legacy, not recommended for new applications)
     */
    RSA_PKCS1("RSA/ECB/PKCS1Padding", 2048),

    /**
     * SM2 algorithm with 256-bit key (Chinese national standard)
     */
    SM2("SM2", 256);

    private final String transformation;
    private final int minKeySize;

    /**
     * Constructor for AsymmetricAlgorithm
     *
     * @param transformation the cipher transformation string
     * @param minKeySize     the minimum recommended key size in bits
     */
    AsymmetricAlgorithm(String transformation, int minKeySize) {
        this.transformation = transformation;
        this.minKeySize = minKeySize;
    }

    /**
     * Gets the cipher transformation string
     * 获取密码转换字符串
     *
     * @return the transformation string
     */
    public String getTransformation() {
        return transformation;
    }

    /**
     * Gets the minimum recommended key size in bits
     * 获取最小推荐密钥大小（位）
     *
     * @return the minimum key size
     */
    public int getMinKeySize() {
        return minKeySize;
    }

    /**
     * Checks if this algorithm is recommended for use
     * 检查算法是否推荐使用
     * <p>
     * OAEP padding variants are recommended over PKCS1 for better security
     *
     * @return true if the algorithm is recommended, false otherwise
     */
    public boolean isRecommended() {
        return this != RSA_PKCS1;
    }
}

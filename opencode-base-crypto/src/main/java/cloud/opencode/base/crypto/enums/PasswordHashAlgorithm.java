package cloud.opencode.base.crypto.enums;

/**
 * Password hashing algorithm enumeration - 密码哈希算法枚举
 * 密码哈希算法的枚举定义
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>All supported password hash algorithm definitions - 所有支持的密码哈希算法定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PasswordHashAlgorithm alg = PasswordHashAlgorithm.ARGON2ID;
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
public enum PasswordHashAlgorithm {
    /**
     * Argon2id - Hybrid mode combining Argon2i and Argon2d (Recommended)
     */
    ARGON2ID("argon2id", true),

    /**
     * Argon2d - Data-dependent mode, resistant to GPU attacks
     */
    ARGON2D("argon2d", false),

    /**
     * Argon2i - Data-independent mode, resistant to side-channel attacks
     */
    ARGON2I("argon2i", false),

    /**
     * BCrypt - Adaptive hash function based on Blowfish (Recommended)
     */
    BCRYPT("bcrypt", true),

    /**
     * SCrypt - Password-based key derivation function (Recommended)
     */
    SCRYPT("scrypt", true),

    /**
     * PBKDF2 with HMAC-SHA256
     */
    PBKDF2_SHA256("pbkdf2-sha256", false),

    /**
     * PBKDF2 with HMAC-SHA512
     */
    PBKDF2_SHA512("pbkdf2-sha512", false);

    private final String name;
    private final boolean recommended;

    /**
     * Constructor for PasswordHashAlgorithm
     *
     * @param name        the algorithm name
     * @param recommended whether this algorithm is recommended for new applications
     */
    PasswordHashAlgorithm(String name, boolean recommended) {
        this.name = name;
        this.recommended = recommended;
    }

    /**
     * Gets the algorithm name
     * 获取算法名称
     *
     * @return the algorithm name
     */
    public String getName() {
        return name;
    }

    /**
     * Checks if this algorithm is recommended for use
     * 检查算法是否推荐使用
     * <p>
     * Argon2id, BCrypt, and SCrypt are recommended for modern password hashing
     * due to their resistance to various attacks and tunable cost parameters
     *
     * @return true if the algorithm is recommended, false otherwise
     */
    public boolean isRecommended() {
        return recommended;
    }
}

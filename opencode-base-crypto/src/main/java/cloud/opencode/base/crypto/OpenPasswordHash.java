package cloud.opencode.base.crypto;

import cloud.opencode.base.crypto.enums.PasswordHashAlgorithm;
import cloud.opencode.base.crypto.password.*;

/**
 * Password hashing facade for secure password storage - Provides convenient API for various password hashing algorithms
 * 密码哈希门面类 - 为各种密码哈希算法提供便捷的 API
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Argon2id password hashing (recommended) - Argon2id 密码哈希（推荐）</li>
 *   <li>BCrypt, SCrypt, PBKDF2 support - BCrypt、SCrypt、PBKDF2 支持</li>
 *   <li>Password verification with constant-time comparison - 使用常量时间比较的密码验证</li>
 *   <li>Hash upgrade detection - 哈希升级检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Hash a password
 * OpenPasswordHash hasher = OpenPasswordHash.argon2();
 * String hash = hasher.hash("myPassword");
 * 
 * // Verify a password
 * boolean valid = hasher.verify("myPassword", hash);
 * 
 * // Check if rehash is needed
 * boolean needsRehash = hasher.needsRehash(hash);
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
 *   <li>Time complexity: O(cost) - 时间复杂度: O(cost)，cost为算法参数</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class OpenPasswordHash {

    private final PasswordHash hasher;

    private OpenPasswordHash(PasswordHash hasher) {
        this.hasher = hasher;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Create Argon2id password hasher (recommended)
     * 创建 Argon2id 密码哈希器（推荐）
     *
     * @return OpenPasswordHash instance
     */
    public static OpenPasswordHash argon2() {
        return new OpenPasswordHash(Argon2Hash.argon2id());
    }

    /**
     * Create BCrypt password hasher
     * 创建 BCrypt 密码哈希器
     *
     * @return OpenPasswordHash instance
     */
    public static OpenPasswordHash bcrypt() {
        return new OpenPasswordHash(BCryptHash.create());
    }

    /**
     * Create BCrypt password hasher with custom cost
     * 创建自定义代价的 BCrypt 密码哈希器
     *
     * @param cost cost factor (4-31)
     * @return OpenPasswordHash instance
     */
    public static OpenPasswordHash bcrypt(int cost) {
        return new OpenPasswordHash(BCryptHash.withCost(cost));
    }

    /**
     * Create SCrypt password hasher
     * 创建 SCrypt 密码哈希器
     *
     * @return OpenPasswordHash instance
     */
    public static OpenPasswordHash scrypt() {
        return new OpenPasswordHash(SCryptHash.of());
    }

    /**
     * Create PBKDF2 password hasher
     * 创建 PBKDF2 密码哈希器
     *
     * @return OpenPasswordHash instance
     */
    public static OpenPasswordHash pbkdf2() {
        return new OpenPasswordHash(Pbkdf2Hash.sha256());
    }

    /**
     * Create PBKDF2 password hasher with custom iterations
     * 创建自定义迭代次数的 PBKDF2 密码哈希器
     *
     * @param iterations number of iterations
     * @return OpenPasswordHash instance
     */
    public static OpenPasswordHash pbkdf2(int iterations) {
        return new OpenPasswordHash(Pbkdf2Hash.builder().iterations(iterations).build());
    }

    /**
     * Create password hasher by algorithm enum
     * 根据算法枚举创建密码哈希器
     *
     * @param algorithm password hash algorithm
     * @return OpenPasswordHash instance
     */
    public static OpenPasswordHash of(PasswordHashAlgorithm algorithm) {
        if (algorithm == null) {
            throw new NullPointerException("Algorithm cannot be null");
        }
        return switch (algorithm) {
            case ARGON2ID, ARGON2I, ARGON2D -> argon2();
            case BCRYPT -> bcrypt();
            case SCRYPT -> scrypt();
            case PBKDF2_SHA256, PBKDF2_SHA512 -> pbkdf2();
        };
    }

    // ==================== Password Hashing ====================

    /**
     * Hash password
     * 哈希密码
     *
     * @param password password to hash
     * @return hash string (includes algorithm info and salt)
     */
    public String hash(String password) {
        if (password == null) {
            throw new NullPointerException("Password cannot be null");
        }
        return hasher.hash(password);
    }

    /**
     * Hash password from char array (more secure)
     * 从字符数组哈希密码（更安全）
     *
     * @param password password to hash
     * @return hash string (includes algorithm info and salt)
     */
    public String hash(char[] password) {
        if (password == null) {
            throw new NullPointerException("Password cannot be null");
        }
        return hasher.hash(password);
    }

    // ==================== Password Verification ====================

    /**
     * Verify password against hash
     * 验证密码与哈希
     *
     * @param password password to verify
     * @param hash stored hash
     * @return true if password matches
     */
    public boolean verify(String password, String hash) {
        if (password == null) {
            throw new NullPointerException("Password cannot be null");
        }
        if (hash == null) {
            throw new NullPointerException("Hash cannot be null");
        }
        return hasher.verify(password, hash);
    }

    /**
     * Verify password from char array against hash
     * 验证字符数组密码与哈希
     *
     * @param password password to verify
     * @param hash stored hash
     * @return true if password matches
     */
    public boolean verify(char[] password, String hash) {
        if (password == null) {
            throw new NullPointerException("Password cannot be null");
        }
        if (hash == null) {
            throw new NullPointerException("Hash cannot be null");
        }
        return hasher.verify(password, hash);
    }

    // ==================== Utility Methods ====================

    /**
     * Check if hash needs to be upgraded
     * 检查哈希是否需要升级
     *
     * @param hash hash to check
     * @return true if hash needs to be rehashed with current settings
     */
    public boolean needsRehash(String hash) {
        if (hash == null) {
            throw new NullPointerException("Hash cannot be null");
        }
        return hasher.needsRehash(hash);
    }

    /**
     * Get default password policy
     * 获取默认密码策略
     *
     * @return default password policy
     */
    public static PasswordPolicy defaultPolicy() {
        return PasswordPolicy.defaultPolicy();
    }

    /**
     * Get strong password policy
     * 获取强密码策略
     *
     * @return strong password policy
     */
    public static PasswordPolicy strongPolicy() {
        return PasswordPolicy.strong();
    }

    // ==================== Info Methods ====================

    /**
     * Get algorithm name
     * 获取算法名称
     *
     * @return algorithm name
     */
    public String getAlgorithm() {
        return hasher.getAlgorithm();
    }
}

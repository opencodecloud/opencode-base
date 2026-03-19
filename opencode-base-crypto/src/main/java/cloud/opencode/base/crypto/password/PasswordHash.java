package cloud.opencode.base.crypto.password;

/**
 * Password hashing interface for secure password storage - Provides methods for hashing and verifying passwords
 * 密码哈希接口，用于安全存储密码 - 提供密码哈希和验证方法
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Password hashing and verification - 密码哈希和验证</li>
 *   <li>Support for String and char[] inputs - 支持 String 和 char[] 输入</li>
 *   <li>Hash upgrade detection - 哈希升级检测</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PasswordHash hasher = Argon2Hash.argon2id();
 * String hash = hasher.hash("password");
 * boolean valid = hasher.verify("password", hash);
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
 *   <li>Time complexity: O(cost) - 时间复杂度: O(cost)，cost为算法参数</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public interface PasswordHash {

    /**
     * Hash a password from a character array
     * 从字符数组哈希密码
     *
     * @param password the password to hash (will not be modified)
     * @return the hash string (self-describing format)
     * @throws NullPointerException if password is null
     */
    String hash(char[] password);

    /**
     * Hash a password from a string
     * 从字符串哈希密码
     *
     * @param password the password to hash
     * @return the hash string (self-describing format)
     * @throws NullPointerException if password is null
     */
    String hash(String password);

    /**
     * Verify a password against a hash using character array
     * 使用字符数组验证密码与哈希值
     *
     * @param password the password to verify (will not be modified)
     * @param hash the hash to verify against
     * @return true if password matches the hash, false otherwise
     * @throws NullPointerException if password or hash is null
     */
    boolean verify(char[] password, String hash);

    /**
     * Verify a password against a hash using string
     * 使用字符串验证密码与哈希值
     *
     * @param password the password to verify
     * @param hash the hash to verify against
     * @return true if password matches the hash, false otherwise
     * @throws NullPointerException if password or hash is null
     */
    boolean verify(String password, String hash);

    /**
     * Check if a hash needs to be rehashed with current parameters
     * 检查哈希值是否需要使用当前参数重新哈希
     *
     * <p>Returns true if the hash was created with different parameters
     * than the current instance, indicating the password should be rehashed
     * on next successful authentication.
     *
     * @param hash the hash to check
     * @return true if rehashing is needed, false otherwise
     * @throws NullPointerException if hash is null
     */
    boolean needsRehash(String hash);

    /**
     * Securely erase a password character array by overwriting with zeros
     * 通过用零覆盖来安全擦除密码字符数组
     *
     * @param password the password array to erase (can be null)
     */
    static void secureErase(char[] password) {
        if (password != null) {
            java.util.Arrays.fill(password, '\0');
        }
    }

    /**
     * Get the algorithm name for this password hash implementation
     * 获取此密码哈希实现的算法名称
     *
     * @return algorithm name
     */
    String getAlgorithm();
}

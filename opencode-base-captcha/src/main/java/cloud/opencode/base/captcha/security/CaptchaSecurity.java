package cloud.opencode.base.captcha.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Captcha Security - Security utilities for CAPTCHA
 * 验证码安全 - 验证码安全工具
 *
 * <p>This class provides security-related utilities for CAPTCHA operations.</p>
 * <p>此类提供验证码操作的安全相关工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Secure random ID generation - 安全随机ID生成</li>
 *   <li>Hash-based answer comparison - 基于哈希的答案比较</li>
 *   <li>Timing-safe comparison - 时间安全的比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String id = CaptchaSecurity.generateId();
 * String hash = CaptchaSecurity.hashAnswer("abc123");
 * boolean match = CaptchaSecurity.verifyAnswer(hash, "abc123");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility, uses static SecureRandom) - 线程安全: 是（无状态工具，使用静态SecureRandom）</li>
 *   <li>Null-safe: No (arguments must not be null) - 空值安全: 否（参数不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class CaptchaSecurity {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private CaptchaSecurity() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Generates a secure random ID.
     * 生成安全随机 ID。
     *
     * @return the random ID | 随机 ID
     */
    public static String generateSecureId() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Generates a secure random token.
     * 生成安全随机令牌。
     *
     * @param length the token length in bytes | 令牌字节长度
     * @return the random token | 随机令牌
     */
    public static String generateSecureToken(int length) {
        byte[] bytes = new byte[length];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    /**
     * Hashes an answer for secure storage.
     * 对答案进行哈希以安全存储。
     *
     * @param answer the answer | 答案
     * @param salt   the salt | 盐
     * @return the hashed answer | 哈希后的答案
     */
    public static String hashAnswer(String answer, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt.getBytes());
            byte[] hash = md.digest(answer.toLowerCase().getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    /**
     * Verifies a hashed answer.
     * 验证哈希后的答案。
     *
     * @param answer       the answer to verify | 要验证的答案
     * @param hashedAnswer the hashed answer | 哈希后的答案
     * @param salt         the salt | 盐
     * @return true if matches | 如果匹配返回 true
     */
    public static boolean verifyHashedAnswer(String answer, String hashedAnswer, String salt) {
        String computed = hashAnswer(answer, salt);
        return constantTimeEquals(computed, hashedAnswer);
    }

    /**
     * Constant-time string comparison to prevent timing attacks.
     * 常量时间字符串比较以防止时序攻击。
     *
     * @param a first string | 第一个字符串
     * @param b second string | 第二个字符串
     * @return true if equal | 如果相等返回 true
     */
    public static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) {
            return a == b;
        }
        if (a.length() != b.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    /**
     * Generates a random salt.
     * 生成随机盐。
     *
     * @return the salt | 盐
     */
    public static String generateSalt() {
        byte[] bytes = new byte[16];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}

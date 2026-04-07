package cloud.opencode.base.oauth2.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;

/**
 * OAuth2 State Parameter Utility
 * OAuth2 State 参数工具类
 *
 * <p>Generates and validates cryptographically secure state parameters for OAuth2
 * authorization flows, preventing CSRF attacks as specified in RFC 6749 Section 10.12.</p>
 * <p>生成和验证用于 OAuth2 授权流程的加密安全 state 参数，
 * 防止 RFC 6749 第 10.12 节中规定的 CSRF 攻击。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Cryptographically secure random state generation - 加密安全的随机 state 生成</li>
 *   <li>URL-safe Base64 encoding - URL 安全的 Base64 编码</li>
 *   <li>Constant-time validation to prevent timing attacks - 恒定时间验证防止时序攻击</li>
 *   <li>Timestamped state with expiration support - 带时间戳的 state 及过期支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Generate a state parameter
 * String state = StateParameter.generate();
 *
 * // Generate with custom size
 * String longState = StateParameter.generate(64);
 *
 * // Validate state from callback
 * boolean valid = StateParameter.validate(expectedState, actualState);
 *
 * // Generate with timestamp for expiration checks
 * StateParameter.StateData data = StateParameter.generateWithTimestamp();
 * if (data.isExpired(Duration.ofMinutes(10))) {
 *     // state has expired
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Uses {@link SecureRandom} for cryptographic randomness - 使用 SecureRandom 生成加密随机数</li>
 *   <li>Constant-time comparison via {@link MessageDigest#isEqual} - 通过 MessageDigest.isEqual 进行恒定时间比较</li>
 *   <li>Minimum 16 bytes (128 bits) of entropy - 最少 16 字节（128 位）熵</li>
 * </ul>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>This class is thread-safe. All methods are stateless or use thread-safe components.</p>
 * <p>此类是线程安全的。所有方法都是无状态的或使用线程安全的组件。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-10.12">RFC 6749 Section 10.12</a>
 * @since JDK 25, opencode-base-oauth2 V1.0.3
 */
public final class StateParameter {

    /**
     * Default state parameter byte length (32 bytes = 256 bits).
     * 默认 state 参数字节长度（32 字节 = 256 位）。
     */
    private static final int DEFAULT_BYTES = 32;

    /**
     * Minimum state parameter byte length (16 bytes = 128 bits).
     * 最小 state 参数字节长度（16 字节 = 128 位）。
     */
    private static final int MIN_BYTES = 16;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private StateParameter() {
        // utility class
    }

    /**
     * Generate a 32-byte cryptographically random state parameter, URL-safe Base64 encoded.
     * 生成 32 字节加密随机 state 参数，URL 安全 Base64 编码。
     *
     * @return the URL-safe Base64 encoded state string | URL 安全 Base64 编码的 state 字符串
     */
    public static String generate() {
        return generate(DEFAULT_BYTES);
    }

    /**
     * Generate a cryptographically random state parameter with custom size, URL-safe Base64 encoded.
     * 使用自定义大小生成加密随机 state 参数，URL 安全 Base64 编码。
     *
     * @param bytes the number of random bytes (minimum 16) | 随机字节数（最少 16）
     * @return the URL-safe Base64 encoded state string | URL 安全 Base64 编码的 state 字符串
     * @throws IllegalArgumentException if bytes is less than 16 | 如果 bytes 小于 16
     */
    public static String generate(int bytes) {
        if (bytes < MIN_BYTES) {
            throw new IllegalArgumentException(
                    "State parameter must be at least " + MIN_BYTES + " bytes for security, got: " + bytes);
        }
        byte[] randomBytes = new byte[bytes];
        SECURE_RANDOM.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    /**
     * Validate a state parameter using constant-time comparison to prevent timing attacks.
     * 使用恒定时间比较验证 state 参数，防止时序攻击。
     *
     * @param expected the expected state value | 期望的 state 值
     * @param actual   the actual state value from callback | 回调中的实际 state 值
     * @return true if the values match | 如果值匹配返回 true
     */
    public static boolean validate(String expected, String actual) {
        if (expected == null || actual == null) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generate a state parameter with a creation timestamp for expiration checks.
     * 生成带有创建时间戳的 state 参数，用于过期检查。
     *
     * @return the state data containing state string and creation timestamp |
     *         包含 state 字符串和创建时间戳的 state 数据
     */
    public static StateData generateWithTimestamp() {
        return new StateData(generate(), Instant.now());
    }

    /**
     * State Data Record
     * State 数据记录
     *
     * <p>Holds a state parameter together with its creation timestamp,
     * allowing expiration-based validation.</p>
     * <p>将 state 参数与其创建时间戳一起保存，允许基于过期的验证。</p>
     *
     * @param state     the state parameter value | state 参数值
     * @param createdAt the creation timestamp | 创建时间戳
     */
    public record StateData(String state, Instant createdAt) {

        /**
         * Compact constructor with validation.
         * 带验证的紧凑构造器。
         */
        public StateData {
            Objects.requireNonNull(state, "state cannot be null");
            Objects.requireNonNull(createdAt, "createdAt cannot be null");
        }

        /**
         * Check if this state data has expired based on the given maximum age.
         * 检查此 state 数据是否已根据给定的最大年龄过期。
         *
         * @param maxAge the maximum allowed age | 允许的最大年龄
         * @return true if the state has expired | 如果 state 已过期返回 true
         * @throws NullPointerException if maxAge is null | 如果 maxAge 为 null
         */
        public boolean isExpired(Duration maxAge) {
            Objects.requireNonNull(maxAge, "maxAge cannot be null");
            return Instant.now().isAfter(createdAt.plus(maxAge));
        }
    }
}

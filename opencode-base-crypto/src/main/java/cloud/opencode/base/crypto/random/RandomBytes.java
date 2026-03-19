package cloud.opencode.base.crypto.random;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Utility class for generating random bytes with various encoding options.
 * 随机字节生成工具类，支持多种编码选项。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Random byte array generation utilities - 随机字节数组生成工具</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * byte[] random = RandomBytes.generate(32);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-crypto V1.0.0
 */
public final class RandomBytes {

    private static final SecureRandom DEFAULT_RANDOM = new SecureRandom();
    private static final HexFormat HEX_FORMAT = HexFormat.of();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder();

    /**
     * Private constructor to prevent instantiation.
     * 私有构造函数，防止实例化。
     */
    private RandomBytes() {
        throw new AssertionError("No RandomBytes instances for you!");
    }

    /**
     * Generates random bytes using the default SecureRandom instance.
     * 使用默认 SecureRandom 实例生成随机字节。
     *
     * @param length the number of random bytes to generate
     * @return array of random bytes
     * @throws IllegalArgumentException if length is not positive
     */
    public static byte[] generate(int length) {
        return generate(length, DEFAULT_RANDOM);
    }

    /**
     * Generates random bytes using the specified SecureRandom instance.
     * 使用指定的 SecureRandom 实例生成随机字节。
     *
     * @param length the number of random bytes to generate
     * @param random the SecureRandom instance to use
     * @return array of random bytes
     * @throws IllegalArgumentException if length is not positive or random is null
     */
    public static byte[] generate(int length, SecureRandom random) {
        if (length <= 0) {
            throw new IllegalArgumentException("Length must be positive");
        }
        if (random == null) {
            throw new IllegalArgumentException("SecureRandom must not be null");
        }

        byte[] bytes = new byte[length];
        random.nextBytes(bytes);
        return bytes;
    }

    /**
     * Generates random bytes and returns them as a hexadecimal string.
     * 生成随机字节并以十六进制字符串返回。
     *
     * @param length the number of random bytes to generate
     * @return hexadecimal string representation of the random bytes
     * @throws IllegalArgumentException if length is not positive
     */
    public static String generateHex(int length) {
        return generateHex(length, DEFAULT_RANDOM);
    }

    /**
     * Generates random bytes using the specified SecureRandom and returns them as a hexadecimal string.
     * 使用指定的 SecureRandom 生成随机字节并以十六进制字符串返回。
     *
     * @param length the number of random bytes to generate
     * @param random the SecureRandom instance to use
     * @return hexadecimal string representation of the random bytes
     * @throws IllegalArgumentException if length is not positive or random is null
     */
    public static String generateHex(int length, SecureRandom random) {
        byte[] bytes = generate(length, random);
        return HEX_FORMAT.formatHex(bytes);
    }

    /**
     * Generates random bytes and returns them as a Base64 encoded string.
     * 生成随机字节并以 Base64 编码字符串返回。
     *
     * @param length the number of random bytes to generate
     * @return Base64 encoded string representation of the random bytes
     * @throws IllegalArgumentException if length is not positive
     */
    public static String generateBase64(int length) {
        return generateBase64(length, DEFAULT_RANDOM);
    }

    /**
     * Generates random bytes using the specified SecureRandom and returns them as a Base64 encoded string.
     * 使用指定的 SecureRandom 生成随机字节并以 Base64 编码字符串返回。
     *
     * @param length the number of random bytes to generate
     * @param random the SecureRandom instance to use
     * @return Base64 encoded string representation of the random bytes
     * @throws IllegalArgumentException if length is not positive or random is null
     */
    public static String generateBase64(int length, SecureRandom random) {
        byte[] bytes = generate(length, random);
        return BASE64_ENCODER.encodeToString(bytes);
    }

    /**
     * Generates random bytes and returns them as a URL-safe Base64 encoded string.
     * 生成随机字节并以 URL 安全的 Base64 编码字符串返回。
     *
     * @param length the number of random bytes to generate
     * @return URL-safe Base64 encoded string representation of the random bytes
     * @throws IllegalArgumentException if length is not positive
     */
    public static String generateBase64Url(int length) {
        return generateBase64Url(length, DEFAULT_RANDOM);
    }

    /**
     * Generates random bytes using the specified SecureRandom and returns them as a URL-safe Base64 encoded string.
     * 使用指定的 SecureRandom 生成随机字节并以 URL 安全的 Base64 编码字符串返回。
     *
     * @param length the number of random bytes to generate
     * @param random the SecureRandom instance to use
     * @return URL-safe Base64 encoded string representation of the random bytes
     * @throws IllegalArgumentException if length is not positive or random is null
     */
    public static String generateBase64Url(int length, SecureRandom random) {
        byte[] bytes = generate(length, random);
        return BASE64_URL_ENCODER.encodeToString(bytes);
    }
}

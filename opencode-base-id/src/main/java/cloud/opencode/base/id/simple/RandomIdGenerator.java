package cloud.opencode.base.id.simple;

import cloud.opencode.base.id.IdGenerator;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Random ID Generator
 * 随机ID生成器
 *
 * <p>Generates random string IDs using specified character set.
 * Uses SecureRandom by default for cryptographic security.</p>
 * <p>使用指定字符集生成随机字符串ID。
 * 默认使用SecureRandom以确保加密安全。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Configurable length - 可配置长度</li>
 *   <li>Custom character set - 自定义字符集</li>
 *   <li>Cryptographically secure - 加密安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Default alphanumeric
 * RandomIdGenerator gen = RandomIdGenerator.create(16);
 * String id = gen.generate();
 * // -> "aB3xK9mNp2QrStUv"
 *
 * // Numeric only
 * RandomIdGenerator numGen = RandomIdGenerator.numeric(8);
 * String numId = numGen.generate();
 * // -> "12345678"
 *
 * // Hex
 * RandomIdGenerator hexGen = RandomIdGenerator.hex(32);
 * String hexId = hexGen.generate();
 * // -> "a1b2c3d4e5f6..."
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public final class RandomIdGenerator implements IdGenerator<String> {

    /**
     * Alphanumeric characters
     * 字母数字字符
     */
    public static final String ALPHANUMERIC =
            "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * Numeric characters
     * 数字字符
     */
    public static final String NUMERIC = "0123456789";

    /**
     * Hex lowercase characters
     * 十六进制小写字符
     */
    public static final String HEX_LOWER = "0123456789abcdef";

    /**
     * Hex uppercase characters
     * 十六进制大写字符
     */
    public static final String HEX_UPPER = "0123456789ABCDEF";

    private final int length;
    private final String chars;
    private final Random random;

    /**
     * Creates a generator
     * 创建生成器
     *
     * @param length the ID length | ID长度
     * @param chars  the character set | 字符集
     * @param random the random source | 随机源
     */
    private RandomIdGenerator(int length, String chars, Random random) {
        this.length = length;
        this.chars = chars;
        this.random = random;
    }

    /**
     * Creates an alphanumeric generator
     * 创建字母数字生成器
     *
     * @param length the ID length | ID长度
     * @return generator | 生成器
     */
    public static RandomIdGenerator create(int length) {
        return new RandomIdGenerator(length, ALPHANUMERIC, new SecureRandom());
    }

    /**
     * Creates a numeric only generator
     * 创建仅数字生成器
     *
     * @param length the ID length | ID长度
     * @return generator | 生成器
     */
    public static RandomIdGenerator numeric(int length) {
        return new RandomIdGenerator(length, NUMERIC, new SecureRandom());
    }

    /**
     * Creates a hex generator
     * 创建十六进制生成器
     *
     * @param length the ID length | ID长度
     * @return generator | 生成器
     */
    public static RandomIdGenerator hex(int length) {
        return new RandomIdGenerator(length, HEX_LOWER, new SecureRandom());
    }

    /**
     * Creates a hex uppercase generator
     * 创建十六进制大写生成器
     *
     * @param length the ID length | ID长度
     * @return generator | 生成器
     */
    public static RandomIdGenerator hexUpper(int length) {
        return new RandomIdGenerator(length, HEX_UPPER, new SecureRandom());
    }

    /**
     * Creates a generator with custom characters
     * 使用自定义字符创建生成器
     *
     * @param length the ID length | ID长度
     * @param chars  the character set | 字符集
     * @return generator | 生成器
     */
    public static RandomIdGenerator custom(int length, String chars) {
        if (chars == null || chars.isEmpty()) {
            throw new IllegalArgumentException("Characters cannot be null or empty");
        }
        return new RandomIdGenerator(length, chars, new SecureRandom());
    }

    @Override
    public String generate() {
        int charLen = chars.length();
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(charLen)));
        }
        return sb.toString();
    }

    /**
     * Gets the ID length
     * 获取ID长度
     *
     * @return length | 长度
     */
    public int getLength() {
        return length;
    }

    /**
     * Gets the character set
     * 获取字符集
     *
     * @return character set | 字符集
     */
    public String getChars() {
        return chars;
    }

    @Override
    public String getType() {
        return "Random";
    }
}

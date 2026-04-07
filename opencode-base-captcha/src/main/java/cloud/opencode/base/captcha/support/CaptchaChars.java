package cloud.opencode.base.captcha.support;

import cloud.opencode.base.captcha.CaptchaType;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Captcha Characters - Character generation utilities
 * 验证码字符 - 字符生成工具
 *
 * <p>This class provides character sets and generation utilities for CAPTCHA.</p>
 * <p>此类提供验证码的字符集和生成工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Multiple character set support (numeric, alpha, alphanumeric, Chinese) - 多种字符集支持</li>
 *   <li>Arithmetic expression generation - 算术表达式生成</li>
 *   <li>SecureRandom-based generation - 基于SecureRandom的生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String code = CaptchaChars.generate(CaptchaType.ALPHANUMERIC, 4);
 * String chinese = CaptchaChars.generateChinese(4);
 * String[] arithmetic = CaptchaChars.generateArithmetic();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (uses SecureRandom) - 线程安全: 是（使用SecureRandom）</li>
 *   <li>Null-safe: No (type must not be null) - 空值安全: 否（类型不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-captcha V1.0.0
 */
public final class CaptchaChars {

    /** Numeric characters (0-9) | 数字字符 */
    public static final char[] NUMERIC = "0123456789".toCharArray();

    /** Alphabetic lowercase characters | 字母小写字符 */
    public static final char[] ALPHA_LOWER = "abcdefghijkmnpqrstuvwxyz".toCharArray();

    /** Alphabetic uppercase characters | 字母大写字符 */
    public static final char[] ALPHA_UPPER = "ABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

    /** Alphabetic mixed case characters | 字母混合大小写字符 */
    public static final char[] ALPHA = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

    /** Alphanumeric characters (avoiding similar characters like 0/O, 1/l/I) | 字母数字字符（避免相似字符） */
    public static final char[] ALPHANUMERIC = "23456789abcdefghjkmnpqrstuvwxyzABCDEFGHJKMNPQRSTUVWXYZ".toCharArray();

    /** Common Chinese characters for CAPTCHA | 常用中文验证码字符 */
    public static final String[] CHINESE = {
        "的", "一", "是", "不", "了", "在", "人", "有", "我", "他",
        "这", "个", "们", "中", "来", "上", "大", "为", "和", "国",
        "地", "到", "以", "说", "时", "要", "就", "出", "会", "可",
        "也", "你", "对", "生", "能", "而", "子", "那", "得", "于",
        "着", "下", "自", "之", "年", "过", "发", "后", "作", "里"
    };

    private static final Random RANDOM = new SecureRandom();

    private CaptchaChars() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Generates random characters for the specified type.
     * 为指定类型生成随机字符。
     *
     * @param type   the CAPTCHA type | 验证码类型
     * @param length the number of characters | 字符数量
     * @return the generated characters | 生成的字符
     */
    public static String generate(CaptchaType type, int length) {
        return switch (type) {
            case NUMERIC -> generateFromChars(NUMERIC, length);
            case ALPHA -> generateFromChars(ALPHA, length);
            case ALPHANUMERIC -> generateFromChars(ALPHANUMERIC, length);
            case CHINESE -> generateChinese(length);
            default -> generateFromChars(ALPHANUMERIC, length);
        };
    }

    /**
     * Generates random characters from the specified character set.
     * 从指定字符集生成随机字符。
     *
     * @param chars  the character set | 字符集
     * @param length the number of characters | 字符数量
     * @return the generated string | 生成的字符串
     */
    public static String generateFromChars(char[] chars, int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(chars[RANDOM.nextInt(chars.length)]);
        }
        return sb.toString();
    }

    /**
     * Generates random Chinese characters.
     * 生成随机中文字符。
     *
     * @param length the number of characters | 字符数量
     * @return the generated string | 生成的字符串
     */
    public static String generateChinese(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHINESE[RANDOM.nextInt(CHINESE.length)]);
        }
        return sb.toString();
    }

    /**
     * Generates a random arithmetic expression.
     * 生成随机算术表达式。
     *
     * @return an array containing [expression, answer] | 包含 [表达式, 答案] 的数组
     */
    public static String[] generateArithmetic() {
        int a = RANDOM.nextInt(10) + 1;
        int b = RANDOM.nextInt(10) + 1;
        int op = RANDOM.nextInt(3);

        String expression;
        int answer;

        switch (op) {
            case 0 -> {
                expression = a + " + " + b + " = ?";
                answer = a + b;
            }
            case 1 -> {
                if (a < b) {
                    int temp = a;
                    a = b;
                    b = temp;
                }
                expression = a + " - " + b + " = ?";
                answer = a - b;
            }
            default -> {
                expression = a + " × " + b + " = ?";
                answer = a * b;
            }
        }

        return new String[] { expression, String.valueOf(answer) };
    }

    /**
     * Gets the Random instance.
     * 获取 Random 实例。
     *
     * @return the Random | Random 实例
     */
    public static Random getRandom() {
        return RANDOM;
    }

    /**
     * Generates a random integer within range.
     * 在范围内生成随机整数。
     *
     * @param bound the upper bound (exclusive) | 上限（不包含）
     * @return the random integer | 随机整数
     */
    public static int randomInt(int bound) {
        return RANDOM.nextInt(bound);
    }

    /**
     * Generates a random integer within range.
     * 在范围内生成随机整数。
     *
     * @param min the minimum (inclusive) | 最小值（包含）
     * @param max the maximum (exclusive) | 最大值（不包含）
     * @return the random integer | 随机整数
     */
    public static int randomInt(int min, int max) {
        if (max <= min) return min;
        return min + RANDOM.nextInt(max - min);
    }
}

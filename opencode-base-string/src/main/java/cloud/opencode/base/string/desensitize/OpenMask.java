package cloud.opencode.base.string.desensitize;

import cloud.opencode.base.string.desensitize.strategy.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Data Masking Utility - Provides data desensitization and masking methods.
 * 数据脱敏工具 - 提供数据脱敏和掩码方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Mobile phone masking - 手机号脱敏</li>
 *   <li>ID card masking - 身份证号脱敏</li>
 *   <li>Email masking - 邮箱脱敏</li>
 *   <li>Bank card masking - 银行卡号脱敏</li>
 *   <li>Chinese name masking - 中文姓名脱敏</li>
 *   <li>Custom pattern masking - 自定义模式脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String phone = OpenMask.mobile("13812345678");    // "138****5678"
 * String email = OpenMask.email("test@example.com"); // "t***t@example.com"
 * String name = OpenMask.chineseName("张三丰");       // "张**"
 * String card = OpenMask.bankCard("6222021234567890"); // "6222****7890"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenMask {
    private OpenMask() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String mobile(String mobile) {
        return maskMobile(mobile);
    }

    public static String maskMobile(String mobile) {
        return mask(mobile, 3, 4, '*');
    }

    public static String idCard(String idCard) {
        return maskIdCard(idCard);
    }

    public static String maskIdCard(String idCard) {
        return mask(idCard, 6, 4, '*');
    }

    public static String email(String email) {
        return maskEmail(email);
    }

    public static String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@", 2);
        String local = parts[0];
        String domain = parts.length > 1 ? parts[1] : "";
        if (local.isEmpty()) return email;
        // Always mask: show only first char + "***" (+ last char if length > 2)
        if (local.length() == 1) {
            return "*@" + domain;
        }
        if (local.length() == 2) {
            return local.charAt(0) + "*@" + domain;
        }
        return local.charAt(0) + "***" + local.charAt(local.length() - 1) + "@" + domain;
    }

    public static String bankCard(String cardNo) {
        return maskBankCard(cardNo);
    }

    public static String maskBankCard(String cardNo) {
        return mask(cardNo, 4, 4, '*');
    }

    public static String chineseName(String name) {
        return maskName(name);
    }

    public static String maskName(String name) {
        if (name == null || name.length() <= 1) return name;
        if (name.length() == 2) return name.charAt(0) + "*";
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    public static String maskAddress(String address) {
        return mask(address, 6, 0, '*');
    }

    public static String mask(String str, int startKeep, int endKeep, char maskChar) {
        if (str == null || str.length() <= startKeep + endKeep) return str;
        int maskLen = str.length() - startKeep - endKeep;
        return str.substring(0, startKeep) + 
               String.valueOf(maskChar).repeat(Math.max(0, maskLen)) + 
               str.substring(str.length() - endKeep);
    }

    public static String maskMiddle(String str, int keepLen, char maskChar) {
        return mask(str, keepLen, keepLen, maskChar);
    }

    /**
     * Masks all occurrences of a regex pattern in a string.
     * 使用正则表达式模式掩码字符串中所有匹配项。
     *
     * <p><strong>Security | 安全性:</strong>
     * The {@code pattern} parameter is compiled as a regular expression.
     * To prevent ReDoS, patterns longer than 200 characters are rejected.
     * The mask replacement is treated as a literal string (not a regex replacement),
     * so backreferences such as {@code $1} in {@code maskChar} have no effect.
     * 参数 {@code pattern} 被编译为正则表达式。为防止 ReDoS，长度超过 200 字符的模式会被拒绝。
     * 掩码替换视为字面量（非正则替换），{@code maskChar} 中的反向引用（如 {@code $1}）不生效。
     * </p>
     *
     * @param str      the string to mask | 要掩码的字符串
     * @param pattern  the regex pattern to match | 要匹配的正则表达式模式
     * @param maskChar the character to replace each match | 替换每个匹配的字符
     * @return the masked string, or {@code null} if input is null |
     *         掩码后的字符串，如果输入为null则返回null
     * @throws IllegalArgumentException if pattern exceeds 200 characters |
     *                                  如果模式超过200字符
     */
    public static String maskByPattern(String str, String pattern, char maskChar) {
        if (str == null) return null;
        if (pattern != null && pattern.length() > 200) {
            throw new IllegalArgumentException(
                    "Mask pattern exceeds maximum length of 200 characters (length: " + pattern.length() + ")");
        }
        return Pattern.compile(pattern).matcher(str)
                .replaceAll(Matcher.quoteReplacement(String.valueOf(maskChar)));
    }

    public static String desensitize(String value, DesensitizeType type) {
        if (value == null) return null;
        return switch (type) {
            case MOBILE_PHONE -> maskMobile(value);
            case ID_CARD -> maskIdCard(value);
            case EMAIL -> maskEmail(value);
            case BANK_CARD -> maskBankCard(value);
            case CHINESE_NAME -> maskName(value);
            case ADDRESS -> maskAddress(value);
            case PASSWORD -> "******";
            case CUSTOM -> value;
        };
    }
}

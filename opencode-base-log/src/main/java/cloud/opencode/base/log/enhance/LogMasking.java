package cloud.opencode.base.log.enhance;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Log Masking - Sensitive Data Masking Utility
 * 日志脱敏 - 敏感数据脱敏工具
 *
 * <p>LogMasking provides utilities for masking sensitive data in log messages,
 * such as passwords, phone numbers, and ID cards.</p>
 * <p>LogMasking 提供在日志消息中脱敏敏感数据的工具，如密码、手机号和身份证。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Register masking rules
 * LogMasking.registerRule("password", MaskingStrategy.PASSWORD);
 * LogMasking.registerRule("phone", MaskingStrategy.PHONE);
 *
 * // Mask values
 * String masked = LogMasking.mask("13812345678", MaskingStrategy.PHONE);
 * // Output: 138****5678
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Built-in strategies for phone, email, ID card, bank card, password - 内置手机号、邮箱、身份证、银行卡、密码脱敏策略</li>
 *   <li>Field-name-based masking rules - 基于字段名的脱敏规则</li>
 *   <li>Pattern-based custom masking rules - 基于模式的自定义脱敏规则</li>
 *   <li>@Mask annotation support - @Mask 注解支持</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap for rules) - 线程安全: 是（ConcurrentHashMap 存储规则）</li>
 *   <li>Null-safe: Yes (returns null/empty as-is) - 空值安全: 是（null/空值原样返回）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class LogMasking {

    private static final Map<String, MaskingStrategy> RULES = new ConcurrentHashMap<>();
    private static final Map<Pattern, Function<String, String>> PATTERN_RULES = new ConcurrentHashMap<>();

    private LogMasking() {
        // Utility class
    }

    // ==================== Rule Registration ====================

    /**
     * Registers a masking rule for a field name.
     * 为字段名注册脱敏规则。
     *
     * @param fieldName the field name pattern - 字段名模式
     * @param strategy  the masking strategy - 脱敏策略
     */
    public static void registerRule(String fieldName, MaskingStrategy strategy) {
        RULES.put(fieldName.toLowerCase(), strategy);
    }

    /**
     * Registers a pattern-based masking rule.
     * 注册基于模式的脱敏规则。
     *
     * @param pattern the regex pattern - 正则模式
     * @param masker  the masking function - 脱敏函数
     */
    public static void registerPatternRule(String pattern, Function<String, String> masker) {
        PATTERN_RULES.put(Pattern.compile(pattern), masker);
    }

    /**
     * Clears all masking rules.
     * 清除所有脱敏规则。
     */
    public static void clearRules() {
        RULES.clear();
        PATTERN_RULES.clear();
    }

    // ==================== Masking Methods ====================

    /**
     * Masks a value using the specified strategy.
     * 使用指定策略脱敏值。
     *
     * @param value    the value to mask - 要脱敏的值
     * @param strategy the strategy - 策略
     * @return the masked value - 脱敏后的值
     */
    public static String mask(String value, MaskingStrategy strategy) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return strategy.mask(value);
    }

    /**
     * Masks a value based on the field name.
     * 根据字段名脱敏值。
     *
     * @param fieldName the field name - 字段名
     * @param value     the value - 值
     * @return the masked value - 脱敏后的值
     */
    public static String maskByField(String fieldName, String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        MaskingStrategy strategy = RULES.get(fieldName.toLowerCase());
        return strategy != null ? strategy.mask(value) : value;
    }

    /**
     * Checks if a field should be masked.
     * 检查字段是否应该被脱敏。
     *
     * @param fieldName the field name - 字段名
     * @return true if should be masked - 如果应该脱敏返回 true
     */
    public static boolean shouldMask(String fieldName) {
        return RULES.containsKey(fieldName.toLowerCase());
    }

    // ==================== Masking Strategy Enum ====================

    /**
     * Masking strategies for different data types.
     * 不同数据类型的脱敏策略。
     */
    public enum MaskingStrategy {
        /**
         * Full masking: ****** - 全部隐藏
         */
        FULL {
            @Override
            public String mask(String value) {
                return "******";
            }
        },

        /**
         * Phone masking: 138****5678 - 手机号脱敏
         */
        PHONE {
            @Override
            public String mask(String value) {
                if (value.length() < 7) return FULL.mask(value);
                return value.substring(0, 3) + "****" + value.substring(value.length() - 4);
            }
        },

        /**
         * Email masking: u***@example.com - 邮箱脱敏
         */
        EMAIL {
            @Override
            public String mask(String value) {
                int atIndex = value.indexOf('@');
                if (atIndex <= 1) return FULL.mask(value);
                return value.charAt(0) + "***" + value.substring(atIndex);
            }
        },

        /**
         * ID card masking: 110***********1234 - 身份证脱敏
         */
        ID_CARD {
            @Override
            public String mask(String value) {
                if (value.length() < 8) return FULL.mask(value);
                return value.substring(0, 3) + "***********" + value.substring(value.length() - 4);
            }
        },

        /**
         * Bank card masking: ************1234 - 银行卡脱敏
         */
        BANK_CARD {
            @Override
            public String mask(String value) {
                if (value.length() < 4) return FULL.mask(value);
                return "************" + value.substring(value.length() - 4);
            }
        },

        /**
         * Password masking: [PROTECTED] - 密码脱敏
         */
        PASSWORD {
            @Override
            public String mask(String value) {
                return "[PROTECTED]";
            }
        },

        /**
         * Name masking: 张*三 - 姓名脱敏
         */
        NAME {
            @Override
            public String mask(String value) {
                if (value.length() <= 1) return "*";
                if (value.length() == 2) return value.charAt(0) + "*";
                return value.charAt(0) + "*".repeat(value.length() - 2) + value.charAt(value.length() - 1);
            }
        },

        /**
         * Address masking - 地址脱敏
         */
        ADDRESS {
            @Override
            public String mask(String value) {
                if (value.length() <= 6) return FULL.mask(value);
                return value.substring(0, 6) + "****";
            }
        },

        /**
         * Custom masking - requires pattern rule - 自定义脱敏
         */
        CUSTOM {
            @Override
            public String mask(String value) {
                return value; // No-op, use registerPatternRule
            }
        };

        /**
         * Masks the value.
         * 脱敏值。
         *
         * @param value the value to mask - 要脱敏的值
         * @return the masked value - 脱敏后的值
         */
        public abstract String mask(String value);
    }

    // ==================== Mask Annotation ====================

    /**
     * Annotation for marking fields that should be masked in logs.
     * 用于标记应在日志中脱敏的字段的注解。
     */
    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Mask {
        /**
         * The masking strategy.
         * 脱敏策略。
         *
         * @return the strategy - 策略
         */
        MaskingStrategy value() default MaskingStrategy.FULL;
    }
}

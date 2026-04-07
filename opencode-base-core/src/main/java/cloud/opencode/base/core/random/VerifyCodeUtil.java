package cloud.opencode.base.core.random;

import java.security.SecureRandom;

/**
 * Verification Code Utility - Generate secure verification codes
 * 验证码工具类 - 生成安全验证码
 *
 * <p>Provides secure verification code generation using SecureRandom.</p>
 * <p>使用 SecureRandom 提供安全的验证码生成。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Numeric codes (6 digits default) - 数字验证码</li>
 *   <li>Alphanumeric codes - 字母数字混合验证码</li>
 *   <li>No-confusing codes (no 0, O, 1, I, L) - 无混淆字符验证码</li>
 *   <li>Builder pattern for custom codes - 构建器模式自定义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String code = VerifyCodeUtil.numeric();       // 6-digit
 * String code = VerifyCodeUtil.numeric(4);      // 4-digit
 * String code = VerifyCodeUtil.noConfusing(6);  // no confusing chars
 *
 * // Builder pattern - 构建器模式
 * String code = VerifyCodeUtil.builder()
 *     .length(8)
 *     .alphanumeric()
 *     .excludeConfusing()
 *     .build();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (SecureRandom) - 线程安全: 是</li>
 *   <li>Cryptographically secure - 加密安全</li>
 * </ul>
 *
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n = code length - O(n), n为验证码长度</li>
 *   <li>Space complexity: O(n) for generated code - 生成的验证码 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class VerifyCodeUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private static final String NUMERIC = "0123456789";
    private static final String ALPHABETIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String ALPHANUMERIC = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final String NUMERIC_NO_CONFUSING = "23456789";
    private static final String ALPHABETIC_NO_CONFUSING = "ABCDEFGHJKMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
    private static final String ALPHANUMERIC_NO_CONFUSING = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";

    private VerifyCodeUtil() {
    }

    /**
     * Generates a 6-digit numeric verification code
     * 生成 6 位数字验证码
     */
    public static String numeric() {
        return numeric(6);
    }

    /**
     * Generates a numeric verification code of specified length
     * 生成指定长度数字验证码
     */
    public static String numeric(int length) {
        return generate(length, NUMERIC);
    }

    /**
     * Generates an alphabetic verification code
     * 生成字母验证码
     */
    public static String alphabetic(int length) {
        return generate(length, ALPHABETIC);
    }

    /**
     * Generates an alphanumeric verification code
     * 生成字母数字混合验证码
     */
    public static String alphanumeric(int length) {
        return generate(length, ALPHANUMERIC);
    }

    /**
     * Generates a non-confusing verification code (excludes 0, O, 1, I, L, etc.)
     * 生成无混淆字符的验证码（排除 0, O, 1, I, L 等）
     */
    public static String noConfusing(int length) {
        return generate(length, ALPHANUMERIC_NO_CONFUSING);
    }

    /**
     * Generates a numeric verification code within a range
     * 生成范围内的数字验证码
     */
    public static String numericRange(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException("min must be <= max, got min=" + min + ", max=" + max);
        }
        int value = SECURE_RANDOM.nextInt((int) Math.min((long) max - min + 1, Integer.MAX_VALUE)) + min;
        int length = String.valueOf(max).length();
        return String.format("%0" + length + "d", value);
    }

    /**
     * Generates a verification code with a custom character set
     * 生成自定义字符集验证码
     */
    public static String generate(int length, String chars) {
        if (length <= 0 || chars == null || chars.isEmpty()) {
            return "";
        }
        char[] result = new char[length];
        for (int i = 0; i < length; i++) {
            result[i] = chars.charAt(SECURE_RANDOM.nextInt(chars.length()));
        }
        return new String(result);
    }

    /**
     * Gets the verification code builder
     * 获取验证码构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Verification code builder
     * 验证码构建器
     */
    public static class Builder {
        private int length = 6;
        private CodeType type = CodeType.NUMERIC;
        private String customChars;
        private boolean excludeConfusing = false;

        public Builder length(int length) {
            this.length = length;
            return this;
        }

        public Builder numeric() {
            this.type = CodeType.NUMERIC;
            return this;
        }

        public Builder alphabetic() {
            this.type = CodeType.ALPHABETIC;
            return this;
        }

        public Builder alphanumeric() {
            this.type = CodeType.ALPHANUMERIC;
            return this;
        }

        public Builder custom(String chars) {
            this.type = CodeType.CUSTOM;
            this.customChars = chars;
            return this;
        }

        public Builder excludeConfusing() {
            this.excludeConfusing = true;
            return this;
        }

        public String build() {
            String chars = switch (type) {
                case NUMERIC -> excludeConfusing ? NUMERIC_NO_CONFUSING : NUMERIC;
                case ALPHABETIC -> excludeConfusing ? ALPHABETIC_NO_CONFUSING : ALPHABETIC;
                case ALPHANUMERIC -> excludeConfusing ? ALPHANUMERIC_NO_CONFUSING : ALPHANUMERIC;
                case CUSTOM -> customChars != null ? customChars : NUMERIC;
            };
            return generate(length, chars);
        }
    }

    private enum CodeType {
        NUMERIC,
        ALPHABETIC,
        ALPHANUMERIC,
        CUSTOM
    }
}

package cloud.opencode.base.crypto.password;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Password policy for validating password strength - Configurable rules for password complexity
 * 密码策略用于验证密码强度 - 可配置的密码复杂度规则
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Minimum length and complexity rules - 最小长度和复杂度规则</li>
 *   <li>Character class requirements - 字符类别要求</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PasswordPolicy policy = PasswordPolicy.defaultPolicy();
 * boolean valid = policy.validate("MyP@ssw0rd");
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
public final class PasswordPolicy {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]");

    private final int minLength;
    private final int maxLength;
    private final boolean requireUppercase;
    private final boolean requireLowercase;
    private final boolean requireDigit;
    private final boolean requireSpecial;
    private final int minCharacterTypes;

    private PasswordPolicy(Builder builder) {
        this.minLength = builder.minLength;
        this.maxLength = builder.maxLength;
        this.requireUppercase = builder.requireUppercase;
        this.requireLowercase = builder.requireLowercase;
        this.requireDigit = builder.requireDigit;
        this.requireSpecial = builder.requireSpecial;
        this.minCharacterTypes = builder.minCharacterTypes;
    }

    /**
     * Create a default password policy (OWASP recommended)
     * 创建默认密码策略（OWASP 推荐）
     *
     * @return default password policy
     */
    public static PasswordPolicy defaultPolicy() {
        return builder()
                .minLength(12)
                .maxLength(128)
                .requireUppercase(true)
                .requireLowercase(true)
                .requireDigit(true)
                .requireSpecial(true)
                .build();
    }

    /**
     * Create a strong password policy
     * 创建强密码策略
     *
     * @return strong password policy
     */
    public static PasswordPolicy strong() {
        return builder()
                .minLength(16)
                .maxLength(128)
                .requireUppercase(true)
                .requireLowercase(true)
                .requireDigit(true)
                .requireSpecial(true)
                .minCharacterTypes(4)
                .build();
    }

    /**
     * Create a basic password policy
     * 创建基本密码策略
     *
     * @return basic password policy
     */
    public static PasswordPolicy basic() {
        return builder()
                .minLength(8)
                .maxLength(128)
                .minCharacterTypes(2)
                .build();
    }

    /**
     * Create a new builder
     * 创建新的构建器
     *
     * @return builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Validate password against this policy
     * 根据此策略验证密码
     *
     * @param password password to validate
     * @return validation result
     */
    public ValidationResult validate(String password) {
        List<String> violations = new ArrayList<>();

        if (password == null) {
            violations.add("Password cannot be null");
            return new ValidationResult(false, violations);
        }

        // Length checks
        if (password.length() < minLength) {
            violations.add("Password must be at least " + minLength + " characters");
        }
        if (password.length() > maxLength) {
            violations.add("Password must be at most " + maxLength + " characters");
        }

        // Character type checks
        boolean hasUppercase = UPPERCASE_PATTERN.matcher(password).find();
        boolean hasLowercase = LOWERCASE_PATTERN.matcher(password).find();
        boolean hasDigit = DIGIT_PATTERN.matcher(password).find();
        boolean hasSpecial = SPECIAL_PATTERN.matcher(password).find();

        if (requireUppercase && !hasUppercase) {
            violations.add("Password must contain at least one uppercase letter");
        }
        if (requireLowercase && !hasLowercase) {
            violations.add("Password must contain at least one lowercase letter");
        }
        if (requireDigit && !hasDigit) {
            violations.add("Password must contain at least one digit");
        }
        if (requireSpecial && !hasSpecial) {
            violations.add("Password must contain at least one special character");
        }

        // Minimum character types check
        int characterTypes = 0;
        if (hasUppercase) characterTypes++;
        if (hasLowercase) characterTypes++;
        if (hasDigit) characterTypes++;
        if (hasSpecial) characterTypes++;

        if (characterTypes < minCharacterTypes) {
            violations.add("Password must contain at least " + minCharacterTypes + " different character types");
        }

        return new ValidationResult(violations.isEmpty(), violations);
    }

    /**
     * Check if password is valid
     * 检查密码是否有效
     *
     * @param password password to check
     * @return true if valid
     */
    public boolean isValid(String password) {
        return validate(password).isValid();
    }

    /**
     * Get minimum length
     * 获取最小长度
     *
     * @return minimum length
     */
    public int getMinLength() {
        return minLength;
    }

    /**
     * Get maximum length
     * 获取最大长度
     *
     * @return maximum length
     */
    public int getMaxLength() {
        return maxLength;
    }

    /**
     * Validation result containing validity and violations
     * 验证结果，包含有效性和违规信息
     */
    public static final class ValidationResult {
        private final boolean valid;
        private final List<String> violations;

        private ValidationResult(boolean valid, List<String> violations) {
            this.valid = valid;
            this.violations = List.copyOf(violations);
        }

        /**
         * Check if password is valid
         * 检查密码是否有效
         *
         * @return true if valid
         */
        public boolean isValid() {
            return valid;
        }

        /**
         * Get list of violations
         * 获取违规列表
         *
         * @return immutable list of violations
         */
        public List<String> getViolations() {
            return violations;
        }

        /**
         * Get first violation message
         * 获取第一条违规消息
         *
         * @return first violation or null if valid
         */
        public String getFirstViolation() {
            return violations.isEmpty() ? null : violations.getFirst();
        }
    }

    /**
     * Builder for PasswordPolicy
     * PasswordPolicy 构建器
     */
    public static final class Builder {
        private int minLength = 8;
        private int maxLength = 128;
        private boolean requireUppercase = false;
        private boolean requireLowercase = false;
        private boolean requireDigit = false;
        private boolean requireSpecial = false;
        private int minCharacterTypes = 1;

        private Builder() {}

        /**
         * Set minimum length
         * 设置最小长度
         *
         * @param minLength minimum length
         * @return this builder
         */
        public Builder minLength(int minLength) {
            if (minLength < 1) {
                throw new IllegalArgumentException("Minimum length must be at least 1");
            }
            this.minLength = minLength;
            return this;
        }

        /**
         * Set maximum length
         * 设置最大长度
         *
         * @param maxLength maximum length
         * @return this builder
         */
        public Builder maxLength(int maxLength) {
            if (maxLength < 1) {
                throw new IllegalArgumentException("Maximum length must be at least 1");
            }
            this.maxLength = maxLength;
            return this;
        }

        /**
         * Require uppercase letters
         * 要求大写字母
         *
         * @param require true to require
         * @return this builder
         */
        public Builder requireUppercase(boolean require) {
            this.requireUppercase = require;
            return this;
        }

        /**
         * Require lowercase letters
         * 要求小写字母
         *
         * @param require true to require
         * @return this builder
         */
        public Builder requireLowercase(boolean require) {
            this.requireLowercase = require;
            return this;
        }

        /**
         * Require digits
         * 要求数字
         *
         * @param require true to require
         * @return this builder
         */
        public Builder requireDigit(boolean require) {
            this.requireDigit = require;
            return this;
        }

        /**
         * Require special characters
         * 要求特殊字符
         *
         * @param require true to require
         * @return this builder
         */
        public Builder requireSpecial(boolean require) {
            this.requireSpecial = require;
            return this;
        }

        /**
         * Set minimum number of character types
         * 设置最少字符类型数
         *
         * @param minTypes minimum types (1-4)
         * @return this builder
         */
        public Builder minCharacterTypes(int minTypes) {
            if (minTypes < 1 || minTypes > 4) {
                throw new IllegalArgumentException("Minimum character types must be between 1 and 4");
            }
            this.minCharacterTypes = minTypes;
            return this;
        }

        /**
         * Build the policy
         * 构建策略
         *
         * @return password policy
         */
        public PasswordPolicy build() {
            if (minLength > maxLength) {
                throw new IllegalArgumentException("Minimum length cannot exceed maximum length");
            }
            return new PasswordPolicy(this);
        }
    }
}

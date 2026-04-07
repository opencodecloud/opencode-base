package cloud.opencode.base.test.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assertion Message Masker - Masks sensitive data in assertion messages
 * 断言消息脱敏 - 在断言消息中脱敏敏感数据
 *
 * <p>This utility class masks sensitive data patterns in assertion messages
 * to prevent accidental exposure in test logs or reports.</p>
 * <p>此工具类在断言消息中脱敏敏感数据模式，以防止在测试日志或报告中意外暴露。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Built-in patterns for emails, phones, credit cards, SSNs, API keys, passwords, IPs - 内置邮箱、电话、信用卡、SSN、API密钥、密码、IP模式</li>
 *   <li>Custom pattern support via Builder API - 通过Builder API支持自定义模式</li>
 *   <li>Configurable masking functions - 可配置脱敏函数</li>
 * </ul>
 *
 * <p><strong>Masked patterns | 脱敏模式:</strong></p>
 * <ul>
 *   <li>Email addresses - 电子邮件地址</li>
 *   <li>Phone numbers - 电话号码</li>
 *   <li>Credit card numbers - 信用卡号</li>
 *   <li>Social security numbers - 社会安全号码</li>
 *   <li>API keys and tokens - API密钥和令牌</li>
 *   <li>Passwords - 密码</li>
 *   <li>IP addresses - IP地址</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * String message = "Expected user email: john@example.com, actual: jane@example.com";
 * String masked = AssertionMessageMasker.mask(message);
 * // Result: "Expected user email: j***@***.com, actual: j***@***.com"
 *
 * // Custom masker
 * AssertionMessageMasker masker = AssertionMessageMasker.builder()
 *     .addPattern(Pattern.compile("secret-\\w+"), "***SECRET***")
 *     .build();
 * String result = masker.apply("Token: secret-abc123");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable rules, stateless masking) - 线程安全: 是（不可变规则，无状态脱敏）</li>
 *   <li>Null-safe: Yes (handles null/empty messages) - 空值安全: 是（处理空消息）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class AssertionMessageMasker {

    // Email pattern
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b"
    );

    // Phone pattern (various formats)
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "\\b(?:\\+?\\d{1,3}[-.\\s]?)?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}\\b"
    );

    // Credit card pattern (16 digits with optional separators)
    private static final Pattern CREDIT_CARD_PATTERN = Pattern.compile(
            "\\b(?:\\d{4}[-.\\s]?){3}\\d{4}\\b"
    );

    // SSN pattern
    private static final Pattern SSN_PATTERN = Pattern.compile(
            "\\b\\d{3}[-.\\s]?\\d{2}[-.\\s]?\\d{4}\\b"
    );

    // API key/token pattern (long alphanumeric strings)
    private static final Pattern API_KEY_PATTERN = Pattern.compile(
            "\\b(?:api[_-]?key|token|bearer|auth)[=:\\s]+[A-Za-z0-9_-]{20,}\\b",
            Pattern.CASE_INSENSITIVE
    );

    // Password pattern
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "\\b(?:password|passwd|pwd)[=:\\s]+[^\\s,;]+",
            Pattern.CASE_INSENSITIVE
    );

    // IPv4 pattern
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "\\b(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\b"
    );

    // Pattern for extracting digits
    private static final Pattern NON_DIGIT_PATTERN = Pattern.compile("\\D");

    // Pre-compiled split pattern for API key/password masking
    private static final Pattern SPLIT_PATTERN = Pattern.compile("[=:\\s]+");

    // Default instance with all standard patterns
    private static final AssertionMessageMasker DEFAULT_INSTANCE = new AssertionMessageMasker(List.of(
            new MaskRule(EMAIL_PATTERN, AssertionMessageMasker::maskEmail),
            new MaskRule(PHONE_PATTERN, s -> maskWithPrefix(s, 3)),
            new MaskRule(CREDIT_CARD_PATTERN, s -> maskWithSuffix(s, 4)),
            new MaskRule(SSN_PATTERN, s -> "***-**-" + s.substring(s.length() - 4)),
            new MaskRule(API_KEY_PATTERN, s -> SPLIT_PATTERN.split(s, 2)[0] + "=***MASKED***"),
            new MaskRule(PASSWORD_PATTERN, s -> SPLIT_PATTERN.split(s, 2)[0] + "=********"),
            new MaskRule(IPV4_PATTERN, s -> maskIp(s))
    ));

    private final List<MaskRule> rules;

    private AssertionMessageMasker(List<MaskRule> rules) {
        this.rules = List.copyOf(rules);
    }

    /**
     * Masks sensitive data in the message using default patterns.
     * 使用默认模式脱敏消息中的敏感数据。
     *
     * @param message the message | 消息
     * @return the masked message | 脱敏后的消息
     */
    public static String mask(String message) {
        return DEFAULT_INSTANCE.apply(message);
    }

    /**
     * Applies masking rules to the message.
     * 将脱敏规则应用于消息。
     *
     * @param message the message | 消息
     * @return the masked message | 脱敏后的消息
     */
    public String apply(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        String result = message;
        for (MaskRule rule : rules) {
            result = applyRule(result, rule);
        }
        return result;
    }

    private String applyRule(String message, MaskRule rule) {
        Matcher matcher = rule.pattern.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String match = matcher.group();
            String masked = rule.masker.apply(match);
            matcher.appendReplacement(sb, Matcher.quoteReplacement(masked));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    /**
     * Creates a builder for custom masker.
     * 创建自定义脱敏器的构建器。
     *
     * @return the builder | 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a builder with default rules included.
     * 创建包含默认规则的构建器。
     *
     * @return the builder | 构建器
     */
    public static Builder builderWithDefaults() {
        Builder builder = new Builder();
        DEFAULT_INSTANCE.rules.forEach(rule ->
                builder.addRule(rule.pattern, rule.masker));
        return builder;
    }

    // Helper methods for masking

    private static String maskEmail(String email) {
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***@***.***";
        }
        String local = email.substring(0, atIndex);
        String domain = email.substring(atIndex + 1);
        int dotIndex = domain.lastIndexOf('.');
        String tld = dotIndex > 0 ? domain.substring(dotIndex) : "";

        String maskedLocal = local.length() > 1 ? local.charAt(0) + "***" : "***";
        return maskedLocal + "@***" + tld;
    }

    private static String maskWithPrefix(String value, int prefixLen) {
        String digits = NON_DIGIT_PATTERN.matcher(value).replaceAll("");
        if (digits.length() <= prefixLen) {
            return "****";
        }
        return digits.substring(0, prefixLen) + "****";
    }

    private static String maskWithSuffix(String value, int suffixLen) {
        String digits = NON_DIGIT_PATTERN.matcher(value).replaceAll("");
        if (digits.length() <= suffixLen) {
            return "****";
        }
        return "****" + digits.substring(digits.length() - suffixLen);
    }

    private static String maskIp(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + ".***.***." + parts[3];
        }
        return "***.***.***.***";
    }

    /**
     * Mask rule record.
     */
    private record MaskRule(Pattern pattern, java.util.function.Function<String, String> masker) {}

    /**
     * Builder for AssertionMessageMasker.
     * AssertionMessageMasker的构建器。
     */
    public static class Builder {
        private final List<MaskRule> rules = new ArrayList<>();

        /**
         * Adds a pattern with replacement string.
         * 添加带替换字符串的模式。
         *
         * @param pattern     the pattern | 模式
         * @param replacement the replacement | 替换
         * @return this builder | 此构建器
         */
        public Builder addPattern(Pattern pattern, String replacement) {
            Objects.requireNonNull(pattern, "pattern cannot be null");
            Objects.requireNonNull(replacement, "replacement cannot be null");
            rules.add(new MaskRule(pattern, s -> replacement));
            return this;
        }

        /**
         * Adds a pattern with masker function.
         * 添加带脱敏函数的模式。
         *
         * @param pattern the pattern | 模式
         * @param masker  the masker function | 脱敏函数
         * @return this builder | 此构建器
         */
        public Builder addRule(Pattern pattern, java.util.function.Function<String, String> masker) {
            Objects.requireNonNull(pattern, "pattern cannot be null");
            Objects.requireNonNull(masker, "masker cannot be null");
            rules.add(new MaskRule(pattern, masker));
            return this;
        }

        /**
         * Adds a regex pattern with replacement string.
         * 添加带替换字符串的正则表达式模式。
         *
         * @param regex       the regex | 正则表达式
         * @param replacement the replacement | 替换
         * @return this builder | 此构建器
         */
        public Builder addPattern(String regex, String replacement) {
            return addPattern(Pattern.compile(regex), replacement);
        }

        /**
         * Includes email masking.
         * 包含电子邮件脱敏。
         *
         * @return this builder | 此构建器
         */
        public Builder maskEmails() {
            rules.add(new MaskRule(EMAIL_PATTERN, AssertionMessageMasker::maskEmail));
            return this;
        }

        /**
         * Includes phone masking.
         * 包含电话脱敏。
         *
         * @return this builder | 此构建器
         */
        public Builder maskPhones() {
            rules.add(new MaskRule(PHONE_PATTERN, s -> maskWithPrefix(s, 3)));
            return this;
        }

        /**
         * Includes credit card masking.
         * 包含信用卡脱敏。
         *
         * @return this builder | 此构建器
         */
        public Builder maskCreditCards() {
            rules.add(new MaskRule(CREDIT_CARD_PATTERN, s -> maskWithSuffix(s, 4)));
            return this;
        }

        /**
         * Includes IP address masking.
         * 包含IP地址脱敏。
         *
         * @return this builder | 此构建器
         */
        public Builder maskIpAddresses() {
            rules.add(new MaskRule(IPV4_PATTERN, AssertionMessageMasker::maskIp));
            return this;
        }

        /**
         * Builds the masker.
         * 构建脱敏器。
         *
         * @return the masker | 脱敏器
         */
        public AssertionMessageMasker build() {
            return new AssertionMessageMasker(rules);
        }
    }
}

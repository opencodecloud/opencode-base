package cloud.opencode.base.config.validation;

import cloud.opencode.base.config.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Pattern Validator - Validates configuration values match specified regex pattern
 * 模式验证器 - 验证配置值匹配指定的正则表达式模式
 *
 * <p>Validates that configuration string values match a specified regular expression pattern.
 * Useful for validating formats like emails, URLs, phone numbers, etc.</p>
 * <p>验证配置字符串值匹配指定的正则表达式模式。适用于验证如邮箱、URL、电话号码等格式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Regex pattern matching - 正则表达式模式匹配</li>
 *   <li>Pre-compiled patterns for performance - 预编译模式提高性能</li>
 *   <li>Custom error messages - 自定义错误消息</li>
 *   <li>Common pattern constants - 常用模式常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Email validation
 * PatternValidator emailValidator = new PatternValidator(
 *     "user.email",
 *     "^[A-Za-z0-9+_.-]+@(.+)$",
 *     "Invalid email format"
 * );
 * ValidationResult result = emailValidator.validate(config);
 *
 * // URL validation with factory method
 * PatternValidator urlValidator = PatternValidator.url("website.url");
 * result = urlValidator.validate(config);
 * }</pre>
 *
 * <p><strong>Common Patterns | 常用模式:</strong></p>
 * <ul>
 *   <li>EMAIL_PATTERN - Email address validation - 邮箱地址验证</li>
 *   <li>URL_PATTERN - HTTP/HTTPS URL validation - HTTP/HTTPS URL验证</li>
 *   <li>IPV4_PATTERN - IPv4 address validation - IPv4地址验证</li>
 *   <li>PHONE_PATTERN - Phone number validation - 电话号码验证</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) where n is value length - 时间复杂度: O(n) n为值长度</li>
 *   <li>Space complexity: O(1) - 空间复杂度: O(1)</li>
 *   <li>Pattern compiled once at construction - 模式在构造时编译一次</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 *   <li>ReDoS protection: Use simple patterns - ReDoS保护: 使用简单模式</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see ConfigValidator
 * @see RequiredValidator
 * @see RangeValidator
 * @since JDK 25, opencode-base-config V1.0.0
 */
public class PatternValidator implements ConfigValidator {

    /** Email pattern | 邮箱模式 */
    public static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";

    /** URL pattern (HTTP/HTTPS) | URL模式 */
    public static final String URL_PATTERN = "^https?://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]$";

    /** IPv4 pattern | IPv4模式 */
    public static final String IPV4_PATTERN = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    /** Phone number pattern (international format) | 电话号码模式（国际格式） */
    public static final String PHONE_PATTERN = "^\\+?[1-9]\\d{1,14}$";

    private final String key;
    private final Pattern pattern;
    private final String errorMessage;

    /**
     * Create pattern validator with default error message
     * 创建带默认错误消息的模式验证器
     *
     * @param key   configuration key | 配置键
     * @param regex regular expression pattern | 正则表达式模式
     * @throws PatternSyntaxException if regex is invalid | 如果正则表达式无效
     */
    public PatternValidator(String key, String regex) {
        this(key, regex, "Value does not match required pattern: " + regex);
    }

    /**
     * Create pattern validator with custom error message
     * 创建带自定义错误消息的模式验证器
     *
     * @param key          configuration key | 配置键
     * @param regex        regular expression pattern | 正则表达式模式
     * @param errorMessage custom error message | 自定义错误消息
     * @throws PatternSyntaxException if regex is invalid | 如果正则表达式无效
     */
    public PatternValidator(String key, String regex, String errorMessage) {
        this.key = key;
        this.pattern = Pattern.compile(regex);
        this.errorMessage = errorMessage;
    }

    /**
     * Create pattern validator from pre-compiled Pattern
     * 从预编译的Pattern创建模式验证器
     *
     * @param key          configuration key | 配置键
     * @param pattern      pre-compiled pattern | 预编译的模式
     * @param errorMessage custom error message | 自定义错误消息
     */
    public PatternValidator(String key, Pattern pattern, String errorMessage) {
        this.key = key;
        this.pattern = pattern;
        this.errorMessage = errorMessage;
    }

    @Override
    public ValidationResult validate(Config config) {
        if (!config.hasKey(key)) {
            return ValidationResult.valid();
        }

        String value = config.getString(key);
        if (value == null || value.isEmpty()) {
            return ValidationResult.valid();
        }

        if (!pattern.matcher(value).matches()) {
            return ValidationResult.invalid("Value for key '" + key + "' is invalid: " + errorMessage + " (got: '" + value + "')");
        }

        return ValidationResult.valid();
    }

    /**
     * Get the compiled pattern
     * 获取编译的模式
     *
     * @return compiled pattern | 编译的模式
     */
    public Pattern getPattern() {
        return pattern;
    }

    /**
     * Get the error message
     * 获取错误消息
     *
     * @return error message | 错误消息
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    // ==================== Factory Methods ====================

    /**
     * Create email validator
     * 创建邮箱验证器
     *
     * @param key configuration key | 配置键
     * @return email pattern validator | 邮箱模式验证器
     */
    public static PatternValidator email(String key) {
        return new PatternValidator(key, EMAIL_PATTERN, "Invalid email address format");
    }

    /**
     * Create URL validator
     * 创建URL验证器
     *
     * @param key configuration key | 配置键
     * @return URL pattern validator | URL模式验证器
     */
    public static PatternValidator url(String key) {
        return new PatternValidator(key, URL_PATTERN, "Invalid URL format");
    }

    /**
     * Create IPv4 validator
     * 创建IPv4验证器
     *
     * @param key configuration key | 配置键
     * @return IPv4 pattern validator | IPv4模式验证器
     */
    public static PatternValidator ipv4(String key) {
        return new PatternValidator(key, IPV4_PATTERN, "Invalid IPv4 address format");
    }

    /**
     * Create phone number validator
     * 创建电话号码验证器
     *
     * @param key configuration key | 配置键
     * @return phone pattern validator | 电话号码模式验证器
     */
    public static PatternValidator phone(String key) {
        return new PatternValidator(key, PHONE_PATTERN, "Invalid phone number format");
    }
}

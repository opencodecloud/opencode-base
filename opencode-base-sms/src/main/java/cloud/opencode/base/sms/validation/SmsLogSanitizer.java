package cloud.opencode.base.sms.validation;

import cloud.opencode.base.sms.message.SmsMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * SMS Log Sanitizer
 * 短信日志脱敏工具
 *
 * <p>Sanitizes SMS data for logging.</p>
 * <p>为日志记录脱敏短信数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Phone number masking in log output - 日志中手机号脱敏</li>
 *   <li>Sensitive parameter masking (code, password, otp) - 敏感参数脱敏</li>
 *   <li>Config key sanitization - 配置键脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String safe = SmsLogSanitizer.sanitize(message);
 * // "SmsMessage{phone=138****8000, ...}"
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
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class SmsLogSanitizer {

    private static final Pattern CHINA_PHONE_PATTERN = Pattern.compile("1[3-9]\\d{9}");

    private static final Set<String> SENSITIVE_KEYS = Set.of(
        "appkey", "appsecret", "accesskeysecret", "secretkey", "password",
        "apikey", "token", "credential", "secret"
    );

    private static final Set<String> SENSITIVE_PARAM_KEYS = Set.of(
        "code", "password", "pwd", "pin", "otp", "verification"
    );

    private SmsLogSanitizer() {
        // Utility class
    }

    /**
     * Sanitize SMS message for logging
     * 脱敏短信消息用于日志
     *
     * @param message the message | 消息
     * @return the sanitized string | 脱敏后的字符串
     */
    public static String sanitize(SmsMessage message) {
        if (message == null) {
            return "null";
        }

        String maskedPhone = PhoneValidator.mask(message.phoneNumber());
        Map<String, String> sanitizedVars = sanitizeParams(message.variables());

        return String.format(
            "SmsMessage{phone=%s, templateId='%s', content=%s, variables=%s}",
            maskedPhone,
            message.templateId(),
            message.content() != null ? "[CONTENT]" : "null",
            sanitizedVars
        );
    }

    /**
     * Sanitize config map for logging
     * 脱敏配置映射用于日志
     *
     * @param config the config | 配置
     * @return the sanitized map | 脱敏后的映射
     */
    public static Map<String, String> sanitizeConfig(Map<String, String> config) {
        if (config == null) {
            return Map.of();
        }

        Map<String, String> sanitized = new HashMap<>();
        config.forEach((k, v) -> {
            if (isSensitiveKey(k)) {
                sanitized.put(k, "***");
            } else {
                sanitized.put(k, v);
            }
        });
        return sanitized;
    }

    /**
     * Sanitize params for logging
     * 脱敏参数用于日志
     *
     * @param params the params | 参数
     * @return the sanitized map | 脱敏后的映射
     */
    public static Map<String, String> sanitizeParams(Map<String, String> params) {
        if (params == null) {
            return Map.of();
        }

        Map<String, String> sanitized = new HashMap<>();
        params.forEach((k, v) -> {
            if (isSensitiveParamKey(k)) {
                sanitized.put(k, maskValue(v));
            } else {
                sanitized.put(k, v);
            }
        });
        return sanitized;
    }

    /**
     * Sanitize error message
     * 脱敏错误消息
     *
     * @param error the error | 错误
     * @return the sanitized error | 脱敏后的错误
     */
    public static String sanitizeError(String error) {
        if (error == null) {
            return null;
        }
        // Remove potential phone numbers from error messages
        return CHINA_PHONE_PATTERN.matcher(error).replaceAll("1**********");
    }

    /**
     * Check if key is sensitive
     * 检查键是否敏感
     */
    private static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        String lower = key.toLowerCase();
        return SENSITIVE_KEYS.stream().anyMatch(lower::contains);
    }

    /**
     * Check if param key is sensitive
     * 检查参数键是否敏感
     */
    private static boolean isSensitiveParamKey(String key) {
        if (key == null) {
            return false;
        }
        String lower = key.toLowerCase();
        return SENSITIVE_PARAM_KEYS.stream().anyMatch(lower::contains);
    }

    /**
     * Mask sensitive value
     * 脱敏敏感值
     */
    private static String maskValue(String value) {
        if (value == null || value.length() <= 2) {
            return "***";
        }
        int showLen = Math.min(2, value.length() / 3);
        return value.substring(0, showLen) + "***";
    }

    /**
     * Create safe log message
     * 创建安全的日志消息
     *
     * @param template the template | 模板
     * @param args the args | 参数
     * @return the formatted message | 格式化的消息
     */
    public static String format(String template, Object... args) {
        if (args == null || args.length == 0) {
            return template;
        }

        Object[] sanitizedArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            sanitizedArgs[i] = sanitizeArg(args[i]);
        }
        return String.format(template, sanitizedArgs);
    }

    /**
     * Sanitize argument
     * 脱敏参数
     */
    private static Object sanitizeArg(Object arg) {
        if (arg == null) {
            return null;
        }
        if (arg instanceof SmsMessage msg) {
            return sanitize(msg);
        }
        if (arg instanceof Map<?, ?> map) {
            @SuppressWarnings("unchecked")
            Map<String, String> stringMap = (Map<String, String>) map;
            return sanitizeConfig(stringMap);
        }
        return arg;
    }
}

package cloud.opencode.base.sms.validation;

import cloud.opencode.base.sms.exception.SmsErrorCode;
import cloud.opencode.base.sms.exception.SmsException;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Phone Validator
 * 手机号验证器
 *
 * <p>Validates phone numbers.</p>
 * <p>验证手机号码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>China mobile number validation - 中国手机号验证</li>
 *   <li>E.164 international format validation - E.164国际格式验证</li>
 *   <li>Phone number masking for security - 手机号脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean valid = PhoneValidator.isValidChinaMobile("13800138000"); // true
 * boolean e164 = PhoneValidator.isValidE164("+8613800138000");     // true
 * String masked = PhoneValidator.mask("13800138000");               // "138****8000"
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
public final class PhoneValidator {

    // China mobile phone number pattern
    private static final Pattern CHINA_MOBILE_PATTERN =
        Pattern.compile("^1[3-9]\\d{9}$");

    // E.164 international format pattern
    private static final Pattern E164_PATTERN =
        Pattern.compile("^\\+[1-9]\\d{1,14}$");

    private static final Pattern PHONE_SEPARATOR_PATTERN = Pattern.compile("[\\s\\-()]");

    private PhoneValidator() {
        // Utility class
    }

    /**
     * Validate China mobile phone number
     * 验证中国大陆手机号
     *
     * @param phone the phone number | 手机号
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidChinaMobile(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return CHINA_MOBILE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate E.164 format phone number
     * 验证E.164格式手机号
     *
     * @param phone the phone number | 手机号
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValidE164(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        return E164_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate phone number (any format)
     * 验证手机号（任意格式）
     *
     * @param phone the phone number | 手机号
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String phone) {
        return isValidChinaMobile(phone) || isValidE164(phone);
    }

    /**
     * Validate phone number and throw if invalid
     * 验证手机号，无效则抛出异常
     *
     * @param phone the phone number | 手机号
     * @throws SmsException if invalid | 如果无效
     */
    public static void validate(String phone) {
        if (!isValid(phone)) {
            throw new SmsException(
                SmsErrorCode.INVALID_PHONE_NUMBER,
                "Invalid phone number: " + mask(phone)
            );
        }
    }

    /**
     * Validate all phone numbers
     * 验证所有手机号
     *
     * @param phones the phone numbers | 手机号列表
     * @throws SmsException if any invalid | 如果有无效的
     */
    public static void validateAll(List<String> phones) {
        List<String> invalid = phones.stream()
            .filter(p -> !isValid(p))
            .toList();

        if (!invalid.isEmpty()) {
            throw new SmsException(
                SmsErrorCode.INVALID_PHONE_NUMBER,
                "Invalid phone numbers: " + invalid.size()
            );
        }
    }

    /**
     * Mask phone number for logging
     * 脱敏手机号用于日志
     *
     * @param phone the phone number | 手机号
     * @return the masked phone | 脱敏后的手机号
     */
    public static String mask(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /**
     * Normalize phone number
     * 标准化手机号
     *
     * @param phone the phone number | 手机号
     * @return the normalized phone | 标准化后的手机号
     */
    public static String normalize(String phone) {
        if (phone == null) {
            return null;
        }
        // Remove spaces, dashes, parentheses
        String normalized = PHONE_SEPARATOR_PATTERN.matcher(phone).replaceAll("");
        // If starts with 86, add +
        if (normalized.startsWith("86") && normalized.length() == 13) {
            return "+" + normalized;
        }
        return normalized;
    }

    /**
     * Extract country code
     * 提取国家代码
     *
     * @param phone the phone number | 手机号
     * @return the country code | 国家代码
     */
    public static String extractCountryCode(String phone) {
        if (phone == null || !phone.startsWith("+")) {
            return null;
        }
        // Simple extraction - find the first sequence of digits
        StringBuilder code = new StringBuilder();
        for (int i = 1; i < phone.length() && i < 5; i++) {
            char c = phone.charAt(i);
            if (Character.isDigit(c)) {
                code.append(c);
            } else {
                break;
            }
        }
        return code.isEmpty() ? null : code.toString();
    }
}

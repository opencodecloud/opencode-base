package cloud.opencode.base.sms.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Phone Number Utility
 * 手机号码工具类
 *
 * <p>Utilities for phone number validation and formatting.</p>
 * <p>手机号码验证和格式化工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Phone number validation (China, international) - 手机号验证（中国、国际）</li>
 *   <li>Phone number normalization - 手机号标准化</li>
 *   <li>Country code mapping - 国家代码映射</li>
 *   <li>Phone number masking - 手机号脱敏</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean valid = PhoneNumberUtil.isValid("+8613800138000");
 * String normalized = PhoneNumberUtil.normalize("138-0013-8000");
 * String masked = PhoneNumberUtil.mask("13800138000"); // "138****8000"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - phone numbers have bounded length (max ~15 digits) - 时间复杂度: O(1)，手机号长度有限（最多约 15 位）</li>
 *   <li>Space complexity: O(1) - pre-compiled regex patterns - 空间复杂度: O(1) 预编译正则表达式</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-sms V1.0.0
 */
public final class PhoneNumberUtil {

    private static final Pattern CHINA_MOBILE = Pattern.compile("^1[3-9]\\d{9}$");
    private static final Pattern INTERNATIONAL = Pattern.compile("^\\+?[1-9]\\d{6,14}$");
    private static final Pattern PHONE_SEPARATOR_PATTERN = Pattern.compile("[\\s\\-().]+");

    private static final Map<String, String> COUNTRY_CODES = Map.ofEntries(
        Map.entry("CN", "+86"),
        Map.entry("US", "+1"),
        Map.entry("UK", "+44"),
        Map.entry("JP", "+81"),
        Map.entry("KR", "+82"),
        Map.entry("HK", "+852"),
        Map.entry("TW", "+886"),
        Map.entry("SG", "+65"),
        Map.entry("AU", "+61"),
        Map.entry("DE", "+49"),
        Map.entry("FR", "+33")
    );

    private PhoneNumberUtil() {
        // Utility class
    }

    /**
     * Validate phone number
     * 验证手机号码
     *
     * @param phoneNumber the phone number | 手机号码
     * @return true if valid | 如果有效返回true
     */
    public static boolean isValid(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        String normalized = normalize(phoneNumber);
        return INTERNATIONAL.matcher(normalized).matches();
    }

    /**
     * Validate China mobile number
     * 验证中国手机号码
     *
     * @param phoneNumber the phone number | 手机号码
     * @return true if valid | 如果有效返回true
     */
    public static boolean isChinaMobile(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return false;
        }
        String normalized = normalize(phoneNumber);
        // Remove +86 prefix if present
        if (normalized.startsWith("+86")) {
            normalized = normalized.substring(3);
        } else if (normalized.startsWith("86")) {
            normalized = normalized.substring(2);
        }
        return CHINA_MOBILE.matcher(normalized).matches();
    }

    /**
     * Normalize phone number
     * 标准化手机号码
     *
     * @param phoneNumber the phone number | 手机号码
     * @return the normalized number | 标准化的号码
     */
    public static String normalize(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }
        return PHONE_SEPARATOR_PATTERN.matcher(phoneNumber).replaceAll("");
    }

    /**
     * Format with country code
     * 使用国家代码格式化
     *
     * @param phoneNumber the phone number | 手机号码
     * @param countryCode the country code (e.g., "CN", "US") | 国家代码
     * @return the formatted number | 格式化的号码
     */
    public static String formatWithCountryCode(String phoneNumber, String countryCode) {
        String normalized = normalize(phoneNumber);
        if (normalized == null) {
            return null;
        }
        String prefix = COUNTRY_CODES.get(countryCode.toUpperCase());
        if (prefix == null) {
            return normalized;
        }
        if (normalized.startsWith("+")) {
            return normalized;
        }
        return prefix + normalized;
    }

    /**
     * Format China mobile number
     * 格式化中国手机号码
     *
     * @param phoneNumber the phone number | 手机号码
     * @return the formatted number | 格式化的号码
     */
    public static String formatChina(String phoneNumber) {
        return formatWithCountryCode(phoneNumber, "CN");
    }

    /**
     * Mask phone number for display
     * 掩码手机号码用于显示
     *
     * @param phoneNumber the phone number | 手机号码
     * @return the masked number | 掩码后的号码
     */
    public static String mask(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 7) {
            return phoneNumber;
        }
        String normalized = normalize(phoneNumber);
        int len = normalized.length();
        return normalized.substring(0, 3) + "****" + normalized.substring(len - 4);
    }

    /**
     * Get country code
     * 获取国家代码
     *
     * @param countryCode the country code (e.g., "CN", "US") | 国家代码
     * @return the dialing prefix | 拨号前缀
     */
    public static String getCountryDialingCode(String countryCode) {
        return COUNTRY_CODES.get(countryCode.toUpperCase());
    }

    /**
     * Extract country code from phone number
     * 从手机号码提取国家代码
     *
     * @param phoneNumber the phone number | 手机号码
     * @return the country code or null | 国家代码或null
     */
    public static String extractCountryCode(String phoneNumber) {
        if (phoneNumber == null || !phoneNumber.startsWith("+")) {
            return null;
        }
        for (Map.Entry<String, String> entry : COUNTRY_CODES.entrySet()) {
            if (phoneNumber.startsWith(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
}

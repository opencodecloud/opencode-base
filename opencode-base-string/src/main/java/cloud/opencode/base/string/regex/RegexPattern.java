package cloud.opencode.base.string.regex;

import java.util.regex.Pattern;

/**
 * Regex Pattern Constants - Provides pre-compiled common regex patterns.
 * 正则模式常量 - 提供预编译的常用正则表达式模式。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pre-compiled patterns for numbers, strings, identifiers - 预编译数字、字符串、标识符模式</li>
 *   <li>Network patterns (email, URL, IPv4, IPv6) - 网络模式</li>
 *   <li>China-specific patterns (phone, ID card) - 中国特定模式（手机号、身份证）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean isEmail = RegexPattern.EMAIL.matcher("test@example.com").matches();
 * boolean isNum = RegexPattern.INTEGER.matcher("-123").matches();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable constants) - 线程安全: 是（不可变常量）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class RegexPattern {
    private RegexPattern() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // Numbers
    public static final Pattern INTEGER = Pattern.compile("^-?\\d+$");
    public static final Pattern POSITIVE_INTEGER = Pattern.compile("^\\d+$");
    public static final Pattern DECIMAL = Pattern.compile("^-?\\d+\\.\\d+$");
    public static final Pattern NUMBER = Pattern.compile("^-?\\d+(\\.\\d+)?$");

    // Strings
    public static final Pattern LETTERS = Pattern.compile("^[a-zA-Z]+$");
    public static final Pattern LOWER_LETTERS = Pattern.compile("^[a-z]+$");
    public static final Pattern UPPER_LETTERS = Pattern.compile("^[A-Z]+$");
    public static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]+$");
    public static final Pattern CHINESE = Pattern.compile("^[\\u4e00-\\u9fa5]+$");

    // Identifiers
    public static final Pattern IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");
    public static final Pattern UUID = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    // Network
    public static final Pattern EMAIL = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    public static final Pattern URL = Pattern.compile("^https?://[\\w.-]+(?:/[\\w./?%&=-]*)?$");
    public static final Pattern IPV4 = Pattern.compile("^(?:(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$");
    public static final Pattern IPV6 = Pattern.compile(
        "^([0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}$|" +
        "^([0-9a-fA-F]{1,4}:){1,7}:$|" +
        "^([0-9a-fA-F]{1,4}:){1,6}:[0-9a-fA-F]{1,4}$|" +
        "^([0-9a-fA-F]{1,4}:){1,5}(:[0-9a-fA-F]{1,4}){1,2}$|" +
        "^([0-9a-fA-F]{1,4}:){1,4}(:[0-9a-fA-F]{1,4}){1,3}$|" +
        "^([0-9a-fA-F]{1,4}:){1,3}(:[0-9a-fA-F]{1,4}){1,4}$|" +
        "^([0-9a-fA-F]{1,4}:){1,2}(:[0-9a-fA-F]{1,4}){1,5}$|" +
        "^[0-9a-fA-F]{1,4}:((:[0-9a-fA-F]{1,4}){1,6})$|" +
        "^:((:[0-9a-fA-F]{1,4}){1,7}|:)$"
    );
    public static final Pattern DOMAIN = Pattern.compile("^[a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+$");

    // China specific
    public static final Pattern MOBILE_CN = Pattern.compile("^1[3-9]\\d{9}$");
    public static final Pattern MOBILE_HK = Pattern.compile("^[5-9]\\d{7}$");
    public static final Pattern MOBILE_TW = Pattern.compile("^09\\d{8}$");
    public static final Pattern ID_CARD_CN = Pattern.compile("^\\d{17}[0-9Xx]$");
    public static final Pattern POSTAL_CODE_CN = Pattern.compile("^\\d{6}$");
    public static final Pattern ID_CARD_HK = Pattern.compile("^[A-Z]{1,2}\\d{6}\\(?[0-9A]\\)?$");
    public static final Pattern ID_CARD_TW = Pattern.compile("^[A-Z]\\d{9}$");
    public static final Pattern CREDIT_CODE_CN = Pattern.compile("^[0-9A-HJ-NPQRTUWXY]{2}\\d{6}[0-9A-HJ-NPQRTUWXY]{10}$");

    // International phone
    public static final Pattern PHONE_US = Pattern.compile("^\\d{10}$");
    public static final Pattern PHONE_INTL = Pattern.compile("^\\+?[1-9]\\d{6,14}$");

    // Date/Time
    public static final Pattern DATE = Pattern.compile("^\\d{4}-\\d{2}-\\d{2}$");
    public static final Pattern TIME = Pattern.compile("^\\d{2}:\\d{2}:\\d{2}$");
    public static final Pattern DATETIME = Pattern.compile("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}$");

    // Validation methods
    public static boolean isEmail(String str) { return str != null && EMAIL.matcher(str).matches(); }
    public static boolean isUrl(String str) { return str != null && URL.matcher(str).matches(); }
    public static boolean isMobile(String str) { return str != null && MOBILE_CN.matcher(str).matches(); }
    public static boolean isMobileHK(String str) { return str != null && MOBILE_HK.matcher(str).matches(); }
    public static boolean isMobileTW(String str) { return str != null && MOBILE_TW.matcher(str).matches(); }
    public static boolean isPhoneUS(String str) { return str != null && PHONE_US.matcher(str).matches(); }
    public static boolean isPhoneIntl(String str) { return str != null && PHONE_INTL.matcher(str).matches(); }
    public static boolean isIdCard(String str) { return str != null && ID_CARD_CN.matcher(str).matches(); }
    public static boolean isIdCardHK(String str) { return str != null && ID_CARD_HK.matcher(str).matches(); }
    public static boolean isIdCardTW(String str) { return str != null && ID_CARD_TW.matcher(str).matches(); }
    public static boolean isIpv4(String str) { return str != null && IPV4.matcher(str).matches(); }
    public static boolean isIpv6(String str) { return str != null && IPV6.matcher(str).matches(); }
    public static boolean isUuid(String str) { return str != null && UUID.matcher(str).matches(); }
}

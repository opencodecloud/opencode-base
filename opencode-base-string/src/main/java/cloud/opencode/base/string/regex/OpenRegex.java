package cloud.opencode.base.string.regex;

import java.util.*;
import java.util.regex.*;

/**
 * Regex Facade - Unified entry point for regex matching and validation operations.
 * 正则门面 - 正则匹配和验证操作的统一入口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pattern matching and extraction - 模式匹配和提取</li>
 *   <li>ReDoS protection via pattern length limit - 通过模式长度限制防止ReDoS</li>
 *   <li>Convenient static match/find/replace methods - 便捷的静态匹配/查找/替换方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * boolean matches = OpenRegex.matches("\\d+", "12345");
 * List<String> found = OpenRegex.findAll("\\d+", "a1b2c3");
 * String replaced = OpenRegex.replaceAll("\\d", "abc123", "*");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (input must not be null) - 空值安全: 否（输入不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenRegex {

    /**
     * Maximum allowed regex pattern length to prevent ReDoS attacks.
     * 最大允许的正则表达式模式长度，防止 ReDoS 攻击。
     */
    private static final int MAX_PATTERN_LENGTH = 1000;

    private OpenRegex() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Validates that a regex pattern does not exceed the maximum allowed length.
     * 验证正则表达式模式不超过最大允许长度。
     *
     * @param regex the regex pattern | 正则表达式模式
     * @throws IllegalArgumentException if the pattern exceeds the limit | 如果模式超过限制
     */
    private static void validatePatternLength(String regex) {
        if (regex != null && regex.length() > MAX_PATTERN_LENGTH) {
            throw new IllegalArgumentException(
                    "Regex pattern exceeds maximum length of " + MAX_PATTERN_LENGTH
                            + " characters (length: " + regex.length() + ")");
        }
    }

    /**
     * Compiles a regex pattern with length validation.
     * 编译正则表达式模式并进行长度验证。
     */
    private static Pattern safeCompile(String regex) {
        validatePatternLength(regex);
        return Pattern.compile(regex);
    }

    /**
     * Compiles a regex pattern with length validation and flags.
     * 编译正则表达式模式并进行长度验证和标志设置。
     */
    private static Pattern safeCompile(String regex, int flags) {
        validatePatternLength(regex);
        return Pattern.compile(regex, flags);
    }

    public static boolean matches(String str, String regex) {
        validatePatternLength(regex);
        return str != null && Pattern.matches(regex, str);
    }

    public static boolean matches(String str, Pattern pattern) {
        return str != null && pattern.matcher(str).matches();
    }

    public static boolean contains(String str, String regex) {
        return str != null && safeCompile(regex).matcher(str).find();
    }

    public static String findFirst(String str, String regex) {
        if (str == null) return null;
        Matcher m = safeCompile(regex).matcher(str);
        return m.find() ? m.group() : null;
    }

    public static List<String> findAll(String str, String regex) {
        if (str == null) return List.of();
        List<String> results = new ArrayList<>();
        Matcher m = safeCompile(regex).matcher(str);
        while (m.find()) {
            results.add(m.group());
        }
        return results;
    }

    public static String findGroup(String str, String regex, int group) {
        if (str == null) return null;
        Matcher m = safeCompile(regex).matcher(str);
        return m.find() && group <= m.groupCount() ? m.group(group) : null;
    }

    public static List<String[]> findAllGroups(String str, String regex) {
        if (str == null) return List.of();
        List<String[]> results = new ArrayList<>();
        Matcher m = safeCompile(regex).matcher(str);
        while (m.find()) {
            String[] groups = new String[m.groupCount() + 1];
            for (int i = 0; i <= m.groupCount(); i++) {
                groups[i] = m.group(i);
            }
            results.add(groups);
        }
        return results;
    }

    public static Map<String, String> findNamedGroups(String str, String regex) {
        if (str == null) return Map.of();
        Pattern p = safeCompile(regex);
        Matcher m = p.matcher(str);
        if (!m.find()) return Map.of();

        Map<String, String> result = new LinkedHashMap<>();
        try {
            for (String name : p.namedGroups().keySet()) {
                result.put(name, m.group(name));
            }
        } catch (Exception e) {
            // Named groups not supported in this pattern
        }
        return result;
    }

    public static String replaceFirst(String str, String regex, String replacement) {
        validatePatternLength(regex);
        return str != null ? str.replaceFirst(regex, replacement) : null;
    }

    public static String replaceAll(String str, String regex, String replacement) {
        validatePatternLength(regex);
        return str != null ? str.replaceAll(regex, replacement) : null;
    }

    public static String replaceAll(String str, String regex, java.util.function.Function<String, String> replacer) {
        if (str == null) return null;
        StringBuffer sb = new StringBuffer();
        Matcher m = safeCompile(regex).matcher(str);
        while (m.find()) {
            // quoteReplacement prevents $ and \ in the replacer's result from being
            // interpreted as backreferences by appendReplacement.
            // 使用 quoteReplacement 防止替换结果中的 $ 和 \ 被解析为反向引用。
            m.appendReplacement(sb, Matcher.quoteReplacement(replacer.apply(m.group())));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String[] split(String str, String regex) {
        validatePatternLength(regex);
        return str != null ? str.split(regex) : new String[0];
    }

    public static String[] split(String str, String regex, int limit) {
        validatePatternLength(regex);
        return str != null ? str.split(regex, limit) : new String[0];
    }

    public static String escape(String str) {
        return Pattern.quote(str);
    }

    public static Pattern compile(String regex) {
        return safeCompile(regex);
    }

    public static Pattern compileIgnoreCase(String regex) {
        return safeCompile(regex, Pattern.CASE_INSENSITIVE);
    }

    public static int countMatches(String str, String regex) {
        if (str == null) return 0;
        Matcher m = safeCompile(regex).matcher(str);
        int count = 0;
        while (m.find()) count++;
        return count;
    }

    public static List<int[]> findPositions(String str, String regex) {
        if (str == null) return List.of();
        List<int[]> positions = new ArrayList<>();
        Matcher m = safeCompile(regex).matcher(str);
        while (m.find()) {
            positions.add(new int[]{m.start(), m.end()});
        }
        return positions;
    }

    // ==================== Validation Methods | 验证方法 ====================

    /**
     * Checks if string is a valid email address.
     * 检查字符串是否为有效的电子邮件地址。
     *
     * @param str the string to check | 要检查的字符串
     * @return true if valid email | 如果是有效邮箱返回true
     */
    public static boolean isEmail(String str) {
        return RegexPattern.isEmail(str);
    }

    /**
     * Checks if string is a valid URL.
     * 检查字符串是否为有效的URL。
     *
     * @param str the string to check | 要检查的字符串
     * @return true if valid URL | 如果是有效URL返回true
     */
    public static boolean isUrl(String str) {
        return RegexPattern.isUrl(str);
    }

    /**
     * Checks if string is a valid mobile phone number (China).
     * 检查字符串是否为有效的手机号码（中国）。
     *
     * @param str the string to check | 要检查的字符串
     * @return true if valid mobile number | 如果是有效手机号返回true
     */
    public static boolean isMobile(String str) {
        return RegexPattern.isMobile(str);
    }

    /**
     * Checks if string is a valid ID card number (China).
     * 检查字符串是否为有效的身份证号码（中国）。
     *
     * @param str the string to check | 要检查的字符串
     * @return true if valid ID card | 如果是有效身份证号返回true
     */
    public static boolean isIdCard(String str) {
        return RegexPattern.isIdCard(str);
    }

    /**
     * Checks if string is a valid IPv4 address.
     * 检查字符串是否为有效的IPv4地址。
     *
     * @param str the string to check | 要检查的字符串
     * @return true if valid IPv4 | 如果是有效IPv4返回true
     */
    public static boolean isIpv4(String str) {
        return RegexPattern.isIpv4(str);
    }

    /**
     * Checks if string is a valid UUID format.
     * 检查字符串是否为有效的UUID格式。
     *
     * @param str the string to check | 要检查的字符串
     * @return true if valid UUID | 如果是有效UUID返回true
     */
    public static boolean isUuid(String str) {
        return RegexPattern.isUuid(str);
    }
}

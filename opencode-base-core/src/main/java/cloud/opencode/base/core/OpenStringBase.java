package cloud.opencode.base.core;

import java.util.Locale;

/**
 * Basic String Utility Class - Minimal string operations for core module
 * 基础字符串工具类 - 核心模块的最小化字符串操作
 *
 * <p>Provides minimal string methods for core module. For full string functionality, use OpenString from String component.</p>
 * <p>提供最小化的基础字符串方法集。完整字符串功能请使用 String 组件的 OpenString。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Empty/Blank checking (isEmpty, isBlank, hasLength, hasText) - 空值检查</li>
 *   <li>Default value handling (defaultIfEmpty, defaultIfBlank, nullToEmpty) - 默认值处理</li>
 *   <li>Trim operations (trim, trimToNull, trimToEmpty) - 裁剪操作</li>
 *   <li>Comparison (equals, equalsIgnoreCase) - 比较</li>
 *   <li>Case conversion (toLowerCase, toUpperCase) - 大小写转换</li>
 *   <li>Pattern matching (startsWith, endsWith, contains) - 模式匹配</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Empty checking - 空值检查
 * boolean empty = OpenStringBase.isEmpty(str);
 * boolean blank = OpenStringBase.isBlank("  ");  // true
 *
 * // Default value - 默认值
 * String value = OpenStringBase.defaultIfBlank(str, "default");
 * String trimmed = OpenStringBase.trimToEmpty(str);
 *
 * // Comparison - 比较
 * boolean eq = OpenStringBase.equals(s1, s2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
public final class OpenStringBase {

    private OpenStringBase() {
    }

    /**
     * Empty string constant
     * 空字符串常量
     */
    public static final String EMPTY = "";

    /**
     * Space string constant
     * 空格字符串常量
     */
    public static final String SPACE = " ";

    // ==================== 空值判断 ====================

    /**
     * Checks if empty (null or length is 0)
     * 检查是否为空（null 或长度为 0）
     */
    public static boolean isEmpty(CharSequence cs) {
        return cs == null || cs.isEmpty();
    }

    /**
     * Checks if not empty
     * 检查是否非空
     */
    public static boolean isNotEmpty(CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * Checks if blank (null, empty, or contains only whitespace)
     * 检查是否为空白（null、空或只包含空白字符）
     */
    public static boolean isBlank(CharSequence cs) {
        if (cs == null) {
            return true;
        }
        int len = cs.length();
        if (len == 0) {
            return true;
        }
        for (int i = 0; i < len; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Checks if not blank
     * 检查是否非空白
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * Checks if it has length (non-null and length > 0)
     * 检查是否有长度（非 null 且长度 > 0）
     */
    public static boolean hasLength(CharSequence cs) {
        return cs != null && !cs.isEmpty();
    }

    /**
     * Checks if it has text (non-blank)
     * 检查是否有文本（非空白）
     */
    public static boolean hasText(CharSequence cs) {
        return isNotBlank(cs);
    }

    // ==================== 长度 ====================

    /**
     * Gets length in a null-safe manner
     * null 安全获取长度
     */
    public static int length(CharSequence cs) {
        return cs == null ? 0 : cs.length();
    }

    // ==================== 默认值 ====================

    /**
     * Returns default value when empty
     * 空时返回默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * Returns default value when blank
     * 空白时返回默认值
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * Converts null to empty string
     * null 转为空字符串
     */
    public static String nullToEmpty(String str) {
        return str == null ? EMPTY : str;
    }

    /**
     * Converts empty string to null
     * 空字符串转为 null
     */
    public static String emptyToNull(String str) {
        return isEmpty(str) ? null : str;
    }

    /**
     * Returns null if empty after trim
     * trim 后为空则返回 null
     */
    public static String trimToNull(String str) {
        if (str == null) {
            return null;
        }
        String trimmed = str.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    /**
     * Returns empty string if empty after trim
     * trim 后为空则返回空字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? EMPTY : str.trim();
    }

    // ==================== 比较 ====================

    /**
     * Null-safe string comparison
     * null 安全的字符串比较
     */
    public static boolean equals(CharSequence cs1, CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String s1 && cs2 instanceof String s2) {
            return s1.equals(s2);
        }
        int len = cs1.length();
        for (int i = 0; i < len; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Null-safe case-insensitive comparison
     * null 安全的忽略大小写比较
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    // ==================== 简单操作 ====================

    /**
     * Safe trim
     * 安全 trim
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Safe lowercase conversion
     * 安全转小写
     */
    public static String toLowerCase(String str) {
        return str == null ? null : str.toLowerCase(Locale.ROOT);
    }

    /**
     * Safe uppercase conversion
     * 安全转大写
     */
    public static String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase(Locale.ROOT);
    }

    /**
     * Checks if the string starts with the specified prefix
     * 检查是否以指定前缀开始
     */
    public static boolean startsWith(String str, String prefix) {
        if (str == null || prefix == null) {
            return str == null && prefix == null;
        }
        return str.startsWith(prefix);
    }

    /**
     * Checks if the string ends with the specified suffix
     * 检查是否以指定后缀结束
     */
    public static boolean endsWith(String str, String suffix) {
        if (str == null || suffix == null) {
            return str == null && suffix == null;
        }
        return str.endsWith(suffix);
    }

    /**
     * Checks if the string contains the specified substring
     * 检查是否包含指定子串
     */
    public static boolean contains(CharSequence str, CharSequence searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.toString().contains(searchStr);
    }
}

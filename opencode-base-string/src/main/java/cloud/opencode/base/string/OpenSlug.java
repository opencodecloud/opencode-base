package cloud.opencode.base.string;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * URL Slug Generator - Converts strings to URL-friendly slugs.
 * URL别名生成器 - 将字符串转换为URL友好的别名。
 *
 * <p>A slug is a URL-friendly string derived from a human-readable text. This utility
 * strips accents, lowercases, replaces non-alphanumeric characters with a separator,
 * and collapses consecutive separators.</p>
 * <p>别名是从人类可读文本派生的URL友好字符串。本工具会去除变音符号、转为小写、
 * 将非字母数字字符替换为分隔符，并折叠连续的分隔符。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Accent stripping via NFD normalization - 通过NFD标准化去除变音符号</li>
 *   <li>Configurable separator - 可配置的分隔符</li>
 *   <li>Max length with word-boundary awareness - 最大长度且不在分隔符中间截断</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * OpenSlug.toSlug("Hello World!")          // "hello-world"
 * OpenSlug.toSlug("Cr\u00e8me Br\u00fbl\u00e9e")        // "creme-brulee"
 * OpenSlug.toSlug("foo bar", "_")          // "foo_bar"
 * OpenSlug.toSlug("a very long title", "-", 10)  // "a-very"
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) - 时间复杂度: O(n)</li>
 *   <li>Space complexity: O(n) - 空间复杂度: O(n)</li>
 * </ul>
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
 * @since JDK 25, opencode-base-string V1.0.3
 */
public final class OpenSlug {

    /** Pattern matching Unicode combining marks (diacritics). */
    private static final Pattern DIACRITICS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    /** Pattern matching non-alphanumeric characters. */
    private static final Pattern NON_ALNUM = Pattern.compile("[^a-z0-9]+");

    private OpenSlug() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    /**
     * Convert a string to a URL-friendly slug using "-" as separator.
     * 将字符串转换为URL友好的别名，使用"-"作为分隔符。
     *
     * <p><strong>Algorithm | 算法:</strong></p>
     * <ol>
     *   <li>Strip accents (NFD normalization) - 去除变音符号</li>
     *   <li>Convert to lowercase - 转为小写</li>
     *   <li>Replace non-alphanumeric with separator - 替换非字母数字字符</li>
     *   <li>Collapse consecutive separators - 折叠连续分隔符</li>
     *   <li>Trim leading/trailing separators - 去除首尾分隔符</li>
     * </ol>
     *
     * @param str the input string | 输入字符串
     * @return the slug | 别名
     */
    public static String toSlug(String str) {
        return toSlug(str, "-");
    }

    /**
     * Convert a string to a URL-friendly slug using a custom separator.
     * 将字符串转换为URL友好的别名，使用自定义分隔符。
     *
     * @param str       the input string | 输入字符串
     * @param separator the separator to use | 要使用的分隔符
     * @return the slug | 别名
     */
    public static String toSlug(String str, String separator) {
        if (str == null || str.isEmpty()) {
            return "";
        }
        if (separator == null) {
            separator = "-";
        }

        // Step 1: NFD normalize and strip diacritics
        String normalized = Normalizer.normalize(str, Normalizer.Form.NFD);
        String stripped = DIACRITICS.matcher(normalized).replaceAll("");

        // Step 2: Lowercase
        String lower = stripped.toLowerCase(java.util.Locale.ROOT);

        // Step 3: Replace non-alphanumeric with separator (quote replacement to prevent regex injection)
        String replaced = NON_ALNUM.matcher(lower).replaceAll(Matcher.quoteReplacement(separator));

        // Step 4: Collapse consecutive separators (use Pattern.quote for literal separator)
        if (!separator.isEmpty()) {
            String quotedSep = Pattern.quote(separator);
            replaced = Pattern.compile("(" + quotedSep + "){2,}").matcher(replaced)
                    .replaceAll(Matcher.quoteReplacement(separator));
        }

        // Step 5: Trim leading/trailing separators
        if (!separator.isEmpty()) {
            int start = 0;
            int end = replaced.length();
            while (start < end && replaced.startsWith(separator, start)) {
                start += separator.length();
            }
            while (end > start && replaced.startsWith(separator, end - separator.length())) {
                end -= separator.length();
            }
            replaced = replaced.substring(start, end);
        }

        return replaced;
    }

    /**
     * Convert a string to a URL-friendly slug with a maximum length.
     * 将字符串转换为URL友好的别名，带最大长度限制。
     *
     * <p>The slug is truncated at a separator boundary so that no partial words remain.
     * If no separator is found within the limit, the slug is truncated at maxLength.</p>
     * <p>别名会在分隔符边界处截断，不留部分单词。如果在限制范围内找不到分隔符，
     * 则在maxLength处截断。</p>
     *
     * @param str       the input string | 输入字符串
     * @param separator the separator to use | 要使用的分隔符
     * @param maxLength the maximum length of the slug | 别名的最大长度
     * @return the slug | 别名
     * @throws IllegalArgumentException if maxLength is negative | 如果maxLength为负数
     */
    public static String toSlug(String str, String separator, int maxLength) {
        if (maxLength < 0) {
            throw new IllegalArgumentException("maxLength must not be negative: " + maxLength);
        }
        String slug = toSlug(str, separator);
        if (slug.length() <= maxLength) {
            return slug;
        }
        if (separator == null || separator.isEmpty()) {
            return slug.substring(0, maxLength);
        }

        // Try to truncate at a separator boundary
        String truncated = slug.substring(0, maxLength);
        int lastSep = truncated.lastIndexOf(separator);
        if (lastSep > 0) {
            return truncated.substring(0, lastSep);
        }
        // No separator found; hard truncate
        return truncated;
    }
}

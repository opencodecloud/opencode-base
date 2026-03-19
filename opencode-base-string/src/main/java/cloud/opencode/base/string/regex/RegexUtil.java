package cloud.opencode.base.string.regex;

import java.util.regex.Pattern;

/**
 * Regex Utility - Provides regex manipulation helper methods.
 * 正则工具 - 提供正则表达式操作辅助方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Pattern compilation with case-insensitive option - 模式编译支持忽略大小写</li>
 *   <li>String escaping for regex literals - 正则字面量字符串转义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Pattern p = RegexUtil.compile("\\d+");
 * Pattern ci = RegexUtil.compileIgnoreCase("hello");
 * String escaped = RegexUtil.escape("a.b"); // "\\Qa.b\\E"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) per match where n = input length - 每次匹配 O(n), n为输入长度</li>
 *   <li>Space complexity: O(1) with compiled pattern cache - 编译模式缓存 O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class RegexUtil {
    private RegexUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static Pattern compile(String regex) {
        return Pattern.compile(regex);
    }

    public static Pattern compileIgnoreCase(String regex) {
        return Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
    }

    public static String escape(String str) {
        return Pattern.quote(str);
    }
}

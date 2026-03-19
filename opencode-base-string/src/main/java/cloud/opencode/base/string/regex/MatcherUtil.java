package cloud.opencode.base.string.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Matcher Utility - Provides regex matcher helper methods.
 * 匹配器工具 - 提供正则匹配器辅助方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Create Matcher from regex string or Pattern - 从正则字符串或Pattern创建Matcher</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Matcher m = MatcherUtil.create("\\d+", "abc123");
 * if (m.find()) { String digits = m.group(); }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (input must not be null) - 空值安全: 否（输入不能为空）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) per match where n = input length - 每次匹配 O(n), n为输入长度</li>
 *   <li>Space complexity: O(m) where m = number of matches - O(m), m为匹配数</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class MatcherUtil {
    private MatcherUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static Matcher create(String regex, String input) {
        return Pattern.compile(regex).matcher(input);
    }

    public static Matcher create(Pattern pattern, String input) {
        return pattern.matcher(input);
    }
}

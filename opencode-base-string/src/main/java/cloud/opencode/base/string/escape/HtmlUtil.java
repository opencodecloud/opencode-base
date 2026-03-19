package cloud.opencode.base.string.escape;

import java.util.Map;

/**
 * HTML Escape Utility - Provides HTML string escaping methods.
 * HTML转义工具 - 提供HTML字符串转义方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HTML entity escaping (&amp;, &lt;, &gt;, &quot;, &#39;) - HTML实体转义</li>
 *   <li>HTML entity unescaping - HTML实体反转义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String escaped = HtmlUtil.escape("<script>alert('xss')</script>");
 * // "&lt;script&gt;alert(&#39;xss&#39;)&lt;/script&gt;"
 *
 * String unescaped = HtmlUtil.unescape("&lt;b&gt;bold&lt;/b&gt;");
 * // "<b>bold</b>"
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
 *   <li>Time complexity: O(n) where n is the string length - 时间复杂度: O(n)，n为字符串长度</li>
 *   <li>Space complexity: O(n) for the output string - 空间复杂度: O(n)，存储输出字符串</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class HtmlUtil {
    private HtmlUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    private static final Map<String, String> HTML_ESCAPES = Map.of(
        "&", "&amp;",
        "<", "&lt;",
        ">", "&gt;",
        "\"", "&quot;",
        "'", "&#39;"
    );

    public static String escape(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder(str.length() + 16);
        for (char c : str.toCharArray()) {
            String escaped = HTML_ESCAPES.get(String.valueOf(c));
            sb.append(escaped != null ? escaped : c);
        }
        return sb.toString();
    }

    public static String unescape(String str) {
        if (str == null) return null;
        return str.replace("&amp;", "&")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&#39;", "'");
    }
}

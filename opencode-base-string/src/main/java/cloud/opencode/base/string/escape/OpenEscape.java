package cloud.opencode.base.string.escape;

import java.net.URLEncoder;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * String Escape Facade - Unified entry point for string escaping operations.
 * 字符串转义门面 - 字符串转义操作的统一入口。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HTML/XML escaping - HTML/XML转义</li>
 *   <li>Java/JSON escaping - Java/JSON转义</li>
 *   <li>SQL escaping - SQL转义</li>
 *   <li>URL encoding/decoding - URL编码/解码</li>
 *   <li>CSV escaping - CSV转义</li>
 *   <li>Regex and shell escaping - 正则和Shell转义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String html = OpenEscape.escapeHtml("<script>"); // "&lt;script&gt;"
 * String sql = OpenEscape.escapeSql("O'Brien");    // "O''Brien"
 * String url = OpenEscape.encodeUrl("hello world"); // "hello+world"
 * String csv = OpenEscape.escapeCsv("a,b");        // "\"a,b\""
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
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenEscape {
    private OpenEscape() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    // HTML
    public static String escapeHtml(String str) { return HtmlUtil.escape(str); }
    public static String unescapeHtml(String str) { return HtmlUtil.unescape(str); }

    // XML
    public static String escapeXml(String str) { return escapeHtml(str); }
    public static String unescapeXml(String str) { return unescapeHtml(str); }

    // Java
    public static String escapeJava(String str) { return JavaUtil.escape(str); }
    public static String unescapeJava(String str) { return JavaUtil.unescape(str); }

    // JSON
    public static String escapeJson(String str) { return escapeJava(str); }
    public static String unescapeJson(String str) { return unescapeJava(str); }

    // SQL
    public static String escapeSql(String str) { return SqlUtil.escape(str); }

    // URL
    public static String encodeUrl(String str) {
        return str != null ? URLEncoder.encode(str, StandardCharsets.UTF_8) : null;
    }

    public static String decodeUrl(String str) {
        return str != null ? URLDecoder.decode(str, StandardCharsets.UTF_8) : null;
    }

    // CSV
    public static String escapeCsv(String str) {
        if (str == null) return null;
        if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            return "\"" + str.replace("\"", "\"\"") + "\"";
        }
        return str;
    }

    public static String unescapeCsv(String str) {
        if (str == null) return null;
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1).replace("\"\"", "\"");
        }
        return str;
    }

    // Regex
    public static String escapeRegex(String str) {
        return str != null ? java.util.regex.Pattern.quote(str) : null;
    }

    // Shell
    public static String escapeShell(String str) {
        if (str == null) return null;
        return "'" + str.replace("'", "'\\''") + "'";
    }
}

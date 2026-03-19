package cloud.opencode.base.string.unicode;

/**
 * Full-Width Character Utility - Converts between full-width and half-width characters.
 * 全角字符工具 - 提供全角和半角字符之间的转换方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Full-width to half-width conversion - 全角转半角</li>
 *   <li>Half-width to full-width conversion - 半角转全角</li>
 *   <li>Full-width space handling - 全角空格处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String half = OpenFullWidth.toHalfWidth("ABC"); // "ABC"
 * String full = OpenFullWidth.toFullWidth("ABC");
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
public final class OpenFullWidth {
    private OpenFullWidth() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String toHalfWidth(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (c >= 0xFF01 && c <= 0xFF5E) {
                sb.append((char) (c - 0xFEE0));
            } else if (c == 0x3000) {
                sb.append(' ');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String toFullWidth(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder(str.length());
        for (char c : str.toCharArray()) {
            if (c >= 0x21 && c <= 0x7E) {
                sb.append((char) (c + 0xFEE0));
            } else if (c == 0x20) {
                sb.append((char) 0x3000);
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

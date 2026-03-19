package cloud.opencode.base.string.abbr;

/**
 * Abbreviation Utility - Provides string abbreviation and shortening methods.
 * 缩写工具 - 提供字符串缩写和缩短方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Head abbreviation with ellipsis - 头部缩写带省略号</li>
 *   <li>Middle abbreviation - 中间缩写</li>
 *   <li>Configurable offset and ellipsis - 可配置偏移和省略符</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String abbr = OpenAbbreviation.abbreviate("Hello World", 8); // "Hello..."
 * String mid = OpenAbbreviation.abbreviateMiddle("abcdefgh", "...", 7); // "ab...gh"
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
public final class OpenAbbreviation {
    private OpenAbbreviation() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String abbreviate(String str, int maxLength) {
        return abbreviate(str, 0, maxLength, "...");
    }

    public static String abbreviate(String str, int offset, int maxLength, String ellipsis) {
        if (str == null || str.length() <= maxLength) return str;
        if (maxLength < ellipsis.length()) return str.substring(0, maxLength);
        
        if (offset > 0) {
            if (offset + maxLength > str.length()) {
                return ellipsis + str.substring(str.length() - maxLength + ellipsis.length());
            }
            return ellipsis + str.substring(offset, Math.min(offset + maxLength - ellipsis.length(), str.length())) + ellipsis;
        }
        
        return str.substring(0, maxLength - ellipsis.length()) + ellipsis;
    }

    public static String abbreviateMiddle(String str, String middle, int maxLength) {
        if (str == null || str.length() <= maxLength) return str;
        if (maxLength < middle.length()) return str.substring(0, maxLength);
        
        int targetLen = maxLength - middle.length();
        int startLen = targetLen / 2;
        int endLen = targetLen - startLen;
        
        return str.substring(0, startLen) + middle + str.substring(str.length() - endLen);
    }
}

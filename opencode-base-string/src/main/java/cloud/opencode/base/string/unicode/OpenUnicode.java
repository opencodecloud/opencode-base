package cloud.opencode.base.string.unicode;

/**
 * Unicode Utility - Provides Unicode character manipulation methods.
 * Unicode工具 - 提供Unicode字符操作方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>String to Unicode escape conversion - 字符串到Unicode转义转换</li>
 *   <li>Unicode escape to string conversion - Unicode转义到字符串转换</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String unicode = OpenUnicode.toUnicode("AB"); // "\\u0041\\u0042"
 * String str = OpenUnicode.fromUnicode("\\u0041\\u0042"); // "AB"
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
public final class OpenUnicode {

    private static final java.util.HexFormat HEX = java.util.HexFormat.of();
    private static final java.util.regex.Pattern UNICODE_ESCAPE_PATTERN = java.util.regex.Pattern.compile("\\\\u");

    private OpenUnicode() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String toUnicode(String str) {
        if (str == null) return null;
        StringBuilder sb = new StringBuilder(str.length() * 6);
        for (int i = 0; i < str.length(); i++) {
            sb.append("\\u").append(HEX.toHexDigits(str.charAt(i)));
        }
        return sb.toString();
    }

    public static String fromUnicode(String unicode) {
        if (unicode == null) return null;
        StringBuilder sb = new StringBuilder();
        String[] parts = UNICODE_ESCAPE_PATTERN.split(unicode);
        for (String part : parts) {
            if (!part.isEmpty()) {
                try {
                    int code = Integer.parseInt(part.substring(0, Math.min(4, part.length())), 16);
                    sb.append((char) code);
                    if (part.length() > 4) {
                        sb.append(part.substring(4));
                    }
                } catch (NumberFormatException e) {
                    sb.append(part);
                }
            }
        }
        return sb.toString();
    }

    public static String toHalfWidth(String str) {
        return OpenFullWidth.toHalfWidth(str);
    }

    public static String toFullWidth(String str) {
        return OpenFullWidth.toFullWidth(str);
    }

    public static String toTraditional(String str) {
        return OpenChinese.toTraditional(str);
    }

    public static String toSimplified(String str) {
        return OpenChinese.toSimplified(str);
    }

    public static int codePoint(char ch) {
        return (int) ch;
    }

    public static int[] codePoints(String str) {
        if (str == null) return new int[0];
        return str.codePoints().toArray();
    }

    public static String fromCodePoints(int... codePoints) {
        if (codePoints == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int cp : codePoints) {
            sb.appendCodePoint(cp);
        }
        return sb.toString();
    }

    public static boolean containsEmoji(String str) {
        if (str == null) return false;
        return str.codePoints().anyMatch(cp -> 
            (cp >= 0x1F600 && cp <= 0x1F64F) ||
            (cp >= 0x1F300 && cp <= 0x1F5FF) ||
            (cp >= 0x1F680 && cp <= 0x1F6FF) ||
            (cp >= 0x2600 && cp <= 0x26FF)
        );
    }

    public static String removeEmoji(String str) {
        if (str == null) return null;
        return str.codePoints()
            .filter(cp -> !((cp >= 0x1F600 && cp <= 0x1F64F) ||
                           (cp >= 0x1F300 && cp <= 0x1F5FF) ||
                           (cp >= 0x1F680 && cp <= 0x1F6FF) ||
                           (cp >= 0x2600 && cp <= 0x26FF)))
            .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
            .toString();
    }

    public static int displayWidth(String str) {
        if (str == null) return 0;
        int width = 0;
        int i = 0;
        while (i < str.length()) {
            int cp = str.codePointAt(i);
            if (Character.isSupplementaryCodePoint(cp)) {
                // Supplementary characters (emojis etc.) are typically displayed as width 2
                width += 2;
                i += 2; // skip surrogate pair
            } else {
                char c = (char) cp;
                if (OpenChinese.isChinese(c) || (c >= 0xFF01 && c <= 0xFF5E)) {
                    width += 2; // Full-width character
                } else {
                    width += 1;
                }
                i++;
            }
        }
        return width;
    }
}

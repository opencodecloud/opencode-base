package cloud.opencode.base.string.text;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Text Highlight Utility - Provides text highlighting methods.
 * 文本高亮工具 - 提供文本高亮方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>HTML span highlighting with CSS class - HTML span高亮带CSS类</li>
 *   <li>ANSI console color highlighting - ANSI控制台颜色高亮</li>
 *   <li>Custom prefix/suffix highlighting - 自定义前缀/后缀高亮</li>
 *   <li>Multi-keyword highlighting - 多关键词高亮</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String html = OpenHighlight.highlightHtml("Hello World", "World");
 * // "Hello <span class=\"highlight\">World</span>"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (returns input for null keyword) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class OpenHighlight {
    private OpenHighlight() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String highlight(String text, String keyword, String prefix, String suffix) {
        if (text == null || keyword == null) return text;
        return text.replace(keyword, prefix + keyword + suffix);
    }

    public static String highlightHtml(String text, String keyword) {
        return highlightHtml(text, keyword, "highlight");
    }

    public static String highlightHtml(String text, String keyword, String cssClass) {
        return highlight(text, keyword, "<span class=\"" + cssClass + "\">", "</span>");
    }

    public static String highlightConsole(String text, String keyword, AnsiColor color) {
        String colorCode = switch (color) {
            case RED -> "\u001B[31m";
            case GREEN -> "\u001B[32m";
            case YELLOW -> "\u001B[33m";
            case BLUE -> "\u001B[34m";
            case MAGENTA -> "\u001B[35m";
            case CYAN -> "\u001B[36m";
            case WHITE -> "\u001B[37m";
        };
        String reset = "\u001B[0m";
        return highlight(text, keyword, colorCode, reset);
    }

    public static String highlight(String text, List<String> keywords, String prefix, String suffix) {
        if (text == null || keywords == null) return text;
        String result = text;
        for (String keyword : keywords) {
            result = highlight(result, keyword, prefix, suffix);
        }
        return result;
    }

    public static String highlightByPattern(String text, String pattern, String prefix, String suffix) {
        if (text == null || pattern == null) return text;
        return Pattern.compile(pattern).matcher(text)
            .replaceAll(m -> prefix + m.group() + suffix);
    }

    public enum AnsiColor {
        RED, GREEN, YELLOW, BLUE, MAGENTA, CYAN, WHITE
    }
}

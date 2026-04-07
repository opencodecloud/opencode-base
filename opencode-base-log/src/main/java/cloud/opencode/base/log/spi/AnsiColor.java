package cloud.opencode.base.log.spi;

import java.util.Objects;

/**
 * ANSI Color Code Enumeration - Defines Terminal Color Escape Sequences
 * ANSI 颜色代码枚举 - 定义终端颜色转义序列
 *
 * <p>This enum provides standard ANSI escape codes for terminal text coloring,
 * including normal colors, bright variants, and bold styles.</p>
 * <p>此枚举提供标准 ANSI 转义码用于终端文本着色，
 * 包括普通颜色、亮色变体和粗体样式。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>16 standard and bright foreground colors - 16 种标准和亮色前景色</li>
 *   <li>Bold style variants - 粗体样式变体</li>
 *   <li>Convenient text wrapping with automatic reset - 便捷的文本包装并自动重置</li>
 *   <li>Safe for terminals that do not support ANSI - 对不支持 ANSI 的终端安全</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Wrap text with color
 * String red = AnsiColor.RED.wrap("Error message");
 *
 * // Get raw escape code
 * String code = AnsiColor.GREEN.getCode();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public enum AnsiColor {

    /** Reset all attributes - 重置所有属性 */
    RESET("\033[0m"),

    /** Black foreground - 黑色前景 */
    BLACK("\033[30m"),

    /** Red foreground - 红色前景 */
    RED("\033[31m"),

    /** Green foreground - 绿色前景 */
    GREEN("\033[32m"),

    /** Yellow foreground - 黄色前景 */
    YELLOW("\033[33m"),

    /** Blue foreground - 蓝色前景 */
    BLUE("\033[34m"),

    /** Magenta foreground - 洋红色前景 */
    MAGENTA("\033[35m"),

    /** Cyan foreground - 青色前景 */
    CYAN("\033[36m"),

    /** White foreground - 白色前景 */
    WHITE("\033[37m"),

    /** Bright black (gray) foreground - 亮黑色（灰色）前景 */
    BRIGHT_BLACK("\033[90m"),

    /** Bright red foreground - 亮红色前景 */
    BRIGHT_RED("\033[91m"),

    /** Bright green foreground - 亮绿色前景 */
    BRIGHT_GREEN("\033[92m"),

    /** Bright yellow foreground - 亮黄色前景 */
    BRIGHT_YELLOW("\033[93m"),

    /** Bright blue foreground - 亮蓝色前景 */
    BRIGHT_BLUE("\033[94m"),

    /** Bright magenta foreground - 亮洋红色前景 */
    BRIGHT_MAGENTA("\033[95m"),

    /** Bright cyan foreground - 亮青色前景 */
    BRIGHT_CYAN("\033[96m"),

    /** Bright white foreground - 亮白色前景 */
    BRIGHT_WHITE("\033[97m"),

    /** Bold style - 粗体样式 */
    BOLD("\033[1m"),

    /** Bold red - 粗体红色 */
    BOLD_RED("\033[1;31m"),

    /** Bold yellow - 粗体黄色 */
    BOLD_YELLOW("\033[1;33m"),

    /** Bold green - 粗体绿色 */
    BOLD_GREEN("\033[1;32m");

    private final String code;

    AnsiColor(String code) {
        this.code = code;
    }

    /**
     * Returns the ANSI escape code.
     * 返回 ANSI 转义码。
     *
     * @return the escape code string - 转义码字符串
     */
    public String getCode() {
        return code;
    }

    /**
     * Wraps the given text with this color code and a trailing RESET.
     * 使用此颜色代码包装给定文本，并在末尾添加 RESET。
     *
     * @param text the text to wrap - 要包装的文本
     * @return the colored text - 着色后的文本
     * @throws NullPointerException if text is null - 如果 text 为 null
     */
    public String wrap(String text) {
        Objects.requireNonNull(text, "text must not be null");
        return code + text + RESET.code;
    }

    /**
     * Returns the ANSI escape code.
     * 返回 ANSI 转义码。
     *
     * @return the escape code string - 转义码字符串
     */
    @Override
    public String toString() {
        return code;
    }
}

package cloud.opencode.base.log.spi;

import cloud.opencode.base.log.LogLevel;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Console Log Formatter with Optional ANSI Color Support
 * 控制台日志格式化器（可选 ANSI 颜色支持）
 *
 * <p>Formats log lines for console output with optional ANSI color codes.
 * When color is enabled, the log level portion is colorized according to severity;
 * when disabled, plain text is produced.</p>
 * <p>将日志行格式化为控制台输出，可选使用 ANSI 颜色代码。
 * 启用颜色时，日志级别部分根据严重程度着色；
 * 禁用时，生成纯文本。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auto-detection of ANSI terminal support - 自动检测 ANSI 终端支持</li>
 *   <li>Respects NO_COLOR environment variable (https://no-color.org/) - 遵循 NO_COLOR 环境变量</li>
 *   <li>Configurable via system property "opencode.log.color" - 通过系统属性配置</li>
 *   <li>Severity-based color mapping for log levels - 基于严重程度的级别颜色映射</li>
 * </ul>
 *
 * <p><strong>Color Mapping | 颜色映射:</strong></p>
 * <ul>
 *   <li>ERROR: Bold Red - 粗体红色</li>
 *   <li>WARN: Bold Yellow - 粗体黄色</li>
 *   <li>INFO: Bold Green - 粗体绿色</li>
 *   <li>DEBUG: Bright Blue - 亮蓝色</li>
 *   <li>TRACE: Bright Black (Gray) - 亮黑色（灰色）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Auto-detect color support
 * ConsoleFormatter formatter = new ConsoleFormatter();
 *
 * // Explicit color control
 * ConsoleFormatter colored = new ConsoleFormatter(true);
 * ConsoleFormatter plain = new ConsoleFormatter(false);
 *
 * // Format a log line
 * String line = formatter.format(LogLevel.INFO, "2026-04-03 10:00:00",
 *         "main", "com.example.App", "Application started");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see AnsiColor
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public final class ConsoleFormatter {

    /**
     * Pre-computed padded level names to avoid per-call String.repeat() allocation.
     * 预计算的填充级别名称，避免每次调用时 String.repeat() 分配。
     */
    private static final Map<LogLevel, String> PADDED_LEVELS;

    static {
        Map<LogLevel, String> m = new java.util.EnumMap<>(LogLevel.class);
        for (LogLevel level : LogLevel.values()) {
            String name = level.getName();
            m.put(level, name.length() < 5 ? name + " ".repeat(5 - name.length()) : name);
        }
        PADDED_LEVELS = m;
    }

    private final boolean colorEnabled;

    /**
     * Creates a formatter with auto-detected ANSI color support.
     * 创建自动检测 ANSI 颜色支持的格式化器。
     */
    public ConsoleFormatter() {
        this.colorEnabled = isAnsiSupported();
    }

    /**
     * Creates a formatter with explicit color control.
     * 创建显式控制颜色的格式化器。
     *
     * @param colorEnabled true to enable ANSI colors - true 启用 ANSI 颜色
     */
    public ConsoleFormatter(boolean colorEnabled) {
        this.colorEnabled = colorEnabled;
    }

    /**
     * Detects whether the current environment supports ANSI escape codes.
     * 检测当前环境是否支持 ANSI 转义码。
     *
     * <p>Detection order | 检测顺序:</p>
     * <ol>
     *   <li>System property "opencode.log.color" (explicit override) - 系统属性显式覆盖</li>
     *   <li>Environment variable "NO_COLOR" (if set, disable) - 环境变量（设置则禁用）</li>
     *   <li>System.console() presence (terminal detection) - 终端检测</li>
     *   <li>OS detection: macOS/Linux default true, others check TERM - 操作系统检测</li>
     * </ol>
     *
     * @return true if ANSI is supported - 如果支持 ANSI 则返回 true
     */
    public static boolean isAnsiSupported() {
        // 1. Check system property override
        String prop = System.getProperty("opencode.log.color");
        if (prop != null) {
            return "true".equalsIgnoreCase(prop);
        }

        // 2. Respect NO_COLOR convention (https://no-color.org/)
        String noColor = System.getenv("NO_COLOR");
        if (noColor != null) {
            return false;
        }

        // 3. Check COLORTERM (survives redirect in CI/Docker)
        String colorTerm = System.getenv("COLORTERM");
        if (colorTerm != null && !colorTerm.isEmpty()) {
            return true;
        }

        // 4. Check if running in a terminal
        if (System.console() == null) {
            return false;
        }

        // 5. OS detection
        String os = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        if (os.contains("mac") || os.contains("linux") || os.contains("unix")) {
            return true;
        }

        // 6. For other OSes (Windows, etc.), check TERM variable
        String term = System.getenv("TERM");
        return term != null && !term.equals("dumb");
    }

    /**
     * Formats a log line with the standard pattern.
     * 使用标准模式格式化日志行。
     *
     * <p>Pattern: {@code timestamp [threadName] LEVEL loggerName - message}</p>
     *
     * @param level      the log level - 日志级别
     * @param timestamp  the formatted timestamp - 格式化的时间戳
     * @param threadName the thread name - 线程名称
     * @param loggerName the logger name - 日志器名称
     * @param message    the log message - 日志消息
     * @return the formatted log line - 格式化后的日志行
     * @throws NullPointerException if any argument is null - 如果任何参数为 null
     */
    public String format(LogLevel level, String timestamp, String threadName,
                         String loggerName, String message) {
        Objects.requireNonNull(level, "level must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
        Objects.requireNonNull(threadName, "threadName must not be null");
        Objects.requireNonNull(loggerName, "loggerName must not be null");
        Objects.requireNonNull(message, "message must not be null");

        String levelText = PADDED_LEVELS.getOrDefault(level, level.getName());
        if (colorEnabled) {
            AnsiColor color = colorForLevel(level);
            levelText = color.wrap(levelText);
        }

        return timestamp + " [" + threadName + "] " + levelText + " " + loggerName + " - " + message;
    }

    /**
     * Wraps text with the given ANSI color code.
     * 使用给定的 ANSI 颜色代码包装文本。
     *
     * <p>If color is disabled, returns the text unchanged.</p>
     * <p>如果颜色禁用，返回未更改的文本。</p>
     *
     * @param text  the text to colorize - 要着色的文本
     * @param color the ANSI color - ANSI 颜色
     * @return the colorized text, or plain text if color is disabled - 着色后的文本，或禁用颜色时返回纯文本
     * @throws NullPointerException if text or color is null - 如果 text 或 color 为 null
     */
    public String formatWithColor(String text, AnsiColor color) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(color, "color must not be null");
        if (!colorEnabled) {
            return text;
        }
        return color.wrap(text);
    }

    /**
     * Returns whether ANSI color output is enabled.
     * 返回是否启用 ANSI 颜色输出。
     *
     * @return true if color is enabled - 如果启用颜色则返回 true
     */
    public boolean isColorEnabled() {
        return colorEnabled;
    }

    /**
     * Returns the ANSI color for the given log level.
     * 返回给定日志级别的 ANSI 颜色。
     *
     * @param level the log level - 日志级别
     * @return the corresponding color - 对应的颜色
     */
    private static AnsiColor colorForLevel(LogLevel level) {
        return switch (level) {
            case ERROR -> AnsiColor.BOLD_RED;
            case WARN -> AnsiColor.BOLD_YELLOW;
            case INFO -> AnsiColor.BOLD_GREEN;
            case DEBUG -> AnsiColor.BRIGHT_BLUE;
            case TRACE -> AnsiColor.BRIGHT_BLACK;
            case OFF -> AnsiColor.RESET;
        };
    }

}

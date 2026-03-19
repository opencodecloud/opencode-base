package cloud.opencode.base.cron;

import java.util.Map;

/**
 * Cron Macro - Predefined Cron Expression Shortcuts
 * Cron宏 - 预定义的Cron表达式快捷方式
 *
 * <p>Resolves standard cron macros to their equivalent 5-field expressions.
 * Macros are case-insensitive and start with {@code @}.</p>
 * <p>将标准Cron宏解析为等效的5字段表达式。
 * 宏不区分大小写，以 {@code @} 开头。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@code @yearly} / {@code @annually} — {@code 0 0 1 1 *} (January 1st at midnight - 每年1月1日午夜)</li>
 *   <li>{@code @monthly} — {@code 0 0 1 * *} (1st of each month - 每月1号午夜)</li>
 *   <li>{@code @weekly} — {@code 0 0 * * 0} (Sunday at midnight - 每周日午夜)</li>
 *   <li>{@code @daily} / {@code @midnight} — {@code 0 0 * * *} (every day at midnight - 每天午夜)</li>
 *   <li>{@code @hourly} — {@code 0 * * * *} (every hour - 每小时)</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CronMacro.resolve("@daily")    // "0 0 * * *"
 * CronMacro.resolve("@yearly")   // "0 0 1 1 *"
 * CronMacro.isMacro("@hourly")   // true
 * CronMacro.isMacro("0 * * * *") // false
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods, immutable map) - 线程安全: 是</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-cron V1.0.0
 */
public final class CronMacro {

    private CronMacro() {
    }

    private static final Map<String, String> MACROS = Map.of(
            "@yearly", "0 0 1 1 *",
            "@annually", "0 0 1 1 *",
            "@monthly", "0 0 1 * *",
            "@weekly", "0 0 * * 0",
            "@daily", "0 0 * * *",
            "@midnight", "0 0 * * *",
            "@hourly", "0 * * * *"
    );

    /**
     * Resolves a macro to its cron expression
     * 将宏解析为Cron表达式
     *
     * @param input the input string | 输入字符串
     * @return the expanded expression, or null if not a macro | 展开的表达式，如果不是宏则返回null
     */
    public static String resolve(String input) {
        if (input == null || !input.startsWith("@")) {
            return null;
        }
        return MACROS.get(input.toLowerCase().trim());
    }

    /**
     * Checks if the input is a known macro
     * 检查输入是否是已知宏
     *
     * @param input the input string | 输入字符串
     * @return true if it is a macro | 如果是宏返回true
     */
    public static boolean isMacro(String input) {
        return resolve(input) != null;
    }
}

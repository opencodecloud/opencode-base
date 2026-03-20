package cloud.opencode.base.string.escape;

/**
 * SQL Escape Utility - Provides SQL string escaping methods.
 * SQL转义工具 - 提供SQL字符串转义方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single-quote escaping for SQL literals - SQL字面量单引号转义</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String escaped = SqlUtil.escape("O'Brien"); // "O''Brien"
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
 *   <li>Time complexity: O(n) where n = string length - O(n), n为字符串长度</li>
 *   <li>Space complexity: O(n) for escaped output - 转义输出 O(n)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class SqlUtil {
    private SqlUtil() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static String escape(String str) {
        if (str == null) return null;
        return str.replace("\\", "\\\\").replace("'", "''");
    }
}

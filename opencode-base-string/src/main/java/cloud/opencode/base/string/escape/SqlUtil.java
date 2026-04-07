package cloud.opencode.base.string.escape;

/**
 * SQL Escape Utility - Provides SQL string escaping methods.
 * SQL转义工具 - 提供SQL字符串转义方法。
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Single-quote escaping for SQL string literals - SQL字符串字面量单引号转义</li>
 * </ul>
 *
 * <p><strong>⚠️ Security Warning | 安全警告:</strong></p>
 * <p>This utility is a <strong>last-resort fallback</strong> for legacy code that cannot use
 * parameterized queries. Prefer {@code PreparedStatement} with bind parameters for all
 * SQL interactions. This escape covers <em>string literal context only</em>; it must NOT
 * be used to escape SQL identifiers (table/column names) or numeric values.</p>
 * <p>本工具是无法使用参数化查询的遗留代码的<strong>最后手段</strong>。所有 SQL 交互
 * 请优先使用带绑定参数的 {@code PreparedStatement}。本转义<em>仅适用于字符串字面量上下文</em>；
 * 禁止用于转义 SQL 标识符（表名/列名）或数值。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // PREFERRED: parameterized query | 推荐：参数化查询
 * PreparedStatement ps = conn.prepareStatement("SELECT * FROM t WHERE name = ?");
 * ps.setString(1, userInput);
 *
 * // FALLBACK ONLY: manual escape | 仅作备选：手动转义
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

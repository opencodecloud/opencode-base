package cloud.opencode.base.string.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * NamedParameterParser - Parses :paramName style SQL parameters
 * 命名参数解析器 - 解析 :paramName 风格的 SQL 参数
 *
 * <p>Converts named-parameter SQL (e.g. {@code WHERE id = :id AND name = :name})
 * into positional-parameter SQL ({@code WHERE id = ? AND name = ?})
 * and extracts values from a {@code Map<String,Object>} in the correct order.</p>
 * <p>将命名参数 SQL（如 {@code WHERE id = :id AND name = :name}）
 * 转换为位置参数 SQL（{@code WHERE id = ? AND name = ?}），
 * 并按正确顺序从 {@code Map<String,Object>} 中提取值。</p>
 *
 * <p>Parameter names must match {@code [a-zA-Z_][a-zA-Z0-9_]*}.
 * Colons inside string literals or cast expressions ({@code ::}) are ignored.</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Named parameter to positional parameter conversion - 命名参数到位置参数转换</li>
 *   <li>Value extraction by parameter order - 按参数顺序提取值</li>
 *   <li>String literal and cast expression awareness - 字符串字面量和类型转换感知</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Map<String, Object> params = Map.of("id", 42, "name", "Alice");
 * var parsed = NamedParameterParser.parse("SELECT * FROM t WHERE id = :id AND name = :name");
 * Object[] values = NamedParameterParser.extractValues(parsed, params);
 * // parsed.sql()  → "SELECT * FROM t WHERE id = ? AND name = ?"
 * // values        → [42, "Alice"]
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (input SQL must not be null) - 空值安全: 否（输入SQL不能为空）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-string V1.0.0
 */
public final class NamedParameterParser {

    private static final Pattern NAMED_PARAM =
        Pattern.compile(":([a-zA-Z_][a-zA-Z0-9_]*)");

    private NamedParameterParser() {}

    /**
     * Result of parsing a named-parameter string.
     *
     * @param sql        the translated string with {@code ?} placeholders
     * @param paramNames ordered list of parameter names (matching {@code ?} positions)
     */
    public record ParsedSql(String sql, List<String> paramNames) {}

    /**
     * Parses a named-parameter SQL string into a {@link ParsedSql}.
     * 将命名参数 SQL 解析为 {@link ParsedSql}。
     *
     * @param namedSql the SQL with :paramName placeholders | 含 :paramName 占位符的 SQL
     * @return parsed result with positional SQL and ordered param names
     */
    public static ParsedSql parse(String namedSql) {
        if (namedSql == null || namedSql.isBlank()) {
            return new ParsedSql(namedSql, List.of());
        }

        List<String> paramNames = new ArrayList<>();
        StringBuilder result = new StringBuilder(namedSql.length());
        int pos = 0;
        int len = namedSql.length();

        while (pos < len) {
            char c = namedSql.charAt(pos);

            // Skip single-quoted string literals unchanged
            if (c == '\'') {
                int end = skipSingleQuotedString(namedSql, pos);
                result.append(namedSql, pos, end);
                pos = end;
                continue;
            }

            // Check for :: (PostgreSQL cast — skip as-is)
            if (c == ':' && pos + 1 < len && namedSql.charAt(pos + 1) == ':') {
                result.append("::");
                pos += 2;
                continue;
            }

            // Named parameter?
            if (c == ':' && pos + 1 < len) {
                Matcher m = NAMED_PARAM.matcher(namedSql).region(pos, len);
                if (m.lookingAt()) {
                    paramNames.add(m.group(1));
                    result.append('?');
                    pos += m.group().length();
                    continue;
                }
            }

            result.append(c);
            pos++;
        }

        return new ParsedSql(result.toString(), List.copyOf(paramNames));
    }

    /**
     * Extracts parameter values from a map in the order required by the parsed SQL.
     * 按解析 SQL 所需的顺序从 Map 中提取参数值。
     *
     * @param parsed the result from {@link #parse}
     * @param params the named parameter values | 命名参数值
     * @return ordered value array suitable for positional binding | 适合位置绑定的有序值数组
     * @throws IllegalArgumentException if a required parameter is missing
     */
    public static Object[] extractValues(ParsedSql parsed, Map<String, Object> params) {
        if (params == null) params = Map.of();
        Object[] values = new Object[parsed.paramNames().size()];
        for (int i = 0; i < parsed.paramNames().size(); i++) {
            String name = parsed.paramNames().get(i);
            if (!params.containsKey(name)) {
                throw new IllegalArgumentException("Missing named parameter: '" + name + "'");
            }
            values[i] = params.get(name);
        }
        return values;
    }

    private static int skipSingleQuotedString(String sql, int start) {
        int i = start + 1;
        int len = sql.length();
        while (i < len) {
            char c = sql.charAt(i++);
            if (c == '\'' && i < len && sql.charAt(i) == '\'') {
                i++; // escaped quote
            } else if (c == '\'') {
                break;
            }
        }
        return i;
    }
}

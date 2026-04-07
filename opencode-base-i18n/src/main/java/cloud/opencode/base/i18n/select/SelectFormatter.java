package cloud.opencode.base.i18n.select;

import cloud.opencode.base.i18n.exception.OpenI18nException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ICU select format parser and formatter
 * ICU 选择格式解析器和格式化器
 *
 * <p>Parses and formats ICU select syntax for gender-based and other categorical
 * message formatting. Selects a sub-message based on an exact string match of
 * the keyword argument.</p>
 * <p>解析和格式化 ICU 选择语法，用于基于性别和其他分类的消息格式化。
 * 根据关键字参数的精确字符串匹配来选择子消息。</p>
 *
 * <p><strong>Syntax | 语法:</strong></p>
 * <pre>
 * {varName, select, male{He} female{She} other{They}}
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ICU select syntax parsing - ICU 选择语法解析</li>
 *   <li>Exact keyword matching - 精确关键字匹配</li>
 *   <li>Required 'other' fallback - 必须的 'other' 兜底</li>
 *   <li>Thread-safe with pattern cache - 线程安全，带模式缓存</li>
 *   <li>Nested brace support - 嵌套花括号支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SelectFormatter formatter = new SelectFormatter();
 * // Simple gender select
 * String pattern = "male{He likes} female{She likes} other{They like}";
 * String result = formatter.format(pattern, "female"); // "She likes"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Null-safe: validates inputs - 空值安全: 校验输入</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class SelectFormatter {

    /**
     * Maximum cache size to prevent memory leaks
     * 最大缓存大小，防止内存泄漏
     */
    private static final int MAX_CACHE_SIZE = 1024;

    /**
     * Pattern cache: pattern string -> parsed branches
     * 模式缓存：模式字符串 -> 解析后的分支
     */
    private final ConcurrentHashMap<String, Map<String, String>> cache = new ConcurrentHashMap<>();

    // ==================== 公共方法 ====================

    /**
     * Formats a select pattern with the given keyword value
     * 使用给定的关键字值格式化选择模式
     *
     * <p>The pattern should contain keyword-message pairs in the form:
     * {@code keyword1{message1} keyword2{message2} other{default}}</p>
     * <p>模式应包含关键字-消息对，格式为：
     * {@code keyword1{message1} keyword2{message2} other{default}}</p>
     *
     * @param pattern the select pattern (without the outer {varName, select, ...} wrapper)
     *                选择模式（不含外层 {varName, select, ...} 包装）
     * @param value   the keyword value to match | 要匹配的关键字值
     * @return the selected message | 选中的消息
     * @throws OpenI18nException if the pattern is invalid or no 'other' branch exists
     *                           如果模式无效或不存在 'other' 分支
     * @throws NullPointerException if pattern or value is null | 如果模式或值为null
     */
    public String format(String pattern, String value) {
        Objects.requireNonNull(pattern, "Select pattern must not be null");
        Objects.requireNonNull(value, "Select value must not be null");

        Map<String, String> branches = parse(pattern);
        String result = branches.get(value);
        if (result != null) {
            return result;
        }
        result = branches.get("other");
        if (result != null) {
            return result;
        }
        throw new OpenI18nException("SELECT_ERROR",
                String.format("No matching branch for value '%s' and no 'other' fallback in select pattern", value));
    }

    /**
     * Clears the pattern cache
     * 清除模式缓存
     */
    public void clearCache() {
        cache.clear();
    }

    // ==================== 内部解析方法 ====================

    /**
     * Parses a select pattern into a map of keyword -> message
     * 将选择模式解析为关键字 -> 消息的映射
     */
    private Map<String, String> parse(String pattern) {
        Map<String, String> cached = cache.get(pattern);
        if (cached != null) {
            return cached;
        }

        Map<String, String> branches = doParse(pattern);

        if (cache.size() < MAX_CACHE_SIZE) {
            cache.putIfAbsent(pattern, branches);
        }
        return branches;
    }

    /**
     * Performs the actual parsing of the select pattern
     * 执行选择模式的实际解析
     */
    private static Map<String, String> doParse(String pattern) {
        Map<String, String> branches = new LinkedHashMap<>();
        int len = pattern.length();
        int pos = 0;

        while (pos < len) {
            // Skip whitespace
            while (pos < len && Character.isWhitespace(pattern.charAt(pos))) {
                pos++;
            }
            if (pos >= len) {
                break;
            }

            // Read keyword
            int keyStart = pos;
            while (pos < len && !Character.isWhitespace(pattern.charAt(pos)) && pattern.charAt(pos) != '{') {
                pos++;
            }
            if (pos == keyStart) {
                throw OpenI18nException.parseError(pattern, "Expected keyword at position " + pos);
            }
            String keyword = pattern.substring(keyStart, pos);

            // Skip whitespace between keyword and '{'
            while (pos < len && Character.isWhitespace(pattern.charAt(pos))) {
                pos++;
            }
            if (pos >= len || pattern.charAt(pos) != '{') {
                throw OpenI18nException.parseError(pattern, "Expected '{' after keyword '" + keyword + "' at position " + pos);
            }

            // Read brace-delimited message body
            String body = readBraceBlock(pattern, pos);
            pos += body.length() + 2; // +2 for the opening and closing braces

            branches.put(keyword, body);
        }

        return Map.copyOf(branches);
    }

    /**
     * Reads a brace-delimited block starting at position pos (which must be '{')
     * 从位置 pos 开始读取花括号分隔的块（pos 位置必须是 '{'）
     *
     * @return the content between the braces (not including the outer braces)
     *         花括号之间的内容（不包括外层花括号）
     */
    private static String readBraceBlock(String pattern, int pos) {
        if (pos >= pattern.length() || pattern.charAt(pos) != '{') {
            throw OpenI18nException.parseError(pattern, "Expected '{' at position " + pos);
        }
        int depth = 0;
        int start = pos + 1;
        for (int i = pos; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            if (c == '{') {
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0) {
                    return pattern.substring(start, i);
                }
            }
        }
        throw OpenI18nException.parseError(pattern, "Unclosed '{' at position " + pos);
    }
}

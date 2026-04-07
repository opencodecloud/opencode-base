package cloud.opencode.base.i18n.plural;

import cloud.opencode.base.i18n.exception.OpenI18nException;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ICU-style plural message formatter using CLDR plural rules
 * 使用 CLDR 复数规则的 ICU 风格复数消息格式化器
 *
 * <p>Parses and formats ICU plural syntax, supporting exact number matches ({@code =N}),
 * CLDR category keywords, and the {@code #} shorthand for the count value.</p>
 * <p>解析并格式化 ICU 复数语法，支持精确数字匹配（{@code =N}）、CLDR 类别关键字
 * 和 {@code #} 简写来表示计数值。</p>
 *
 * <p><strong>Syntax | 语法:</strong></p>
 * <pre>
 * =0{no files} one{1 file} other{# files}
 * =1{just one} few{a few} other{# total}
 * </pre>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exact match ({@code =N}) takes priority over category - 精确匹配优先于类别</li>
 *   <li>{@code #} is replaced by the count number - {@code #} 替换为计数值</li>
 *   <li>Falls back to "other" if no category matches - 无匹配时回退到 "other"</li>
 *   <li>Thread-safe with pattern cache - 线程安全，带模式缓存</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * PluralFormatter pf = new PluralFormatter();
 * // English: 1 file / 3 files
 * pf.selectBranch("=0{no files} one{1 file} other{# files}", 1L, Locale.ENGLISH);
 * // → "1 file"
 * pf.selectBranch("=0{no files} one{1 file} other{# files}", 5L, Locale.ENGLISH);
 * // → "5 files"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class PluralFormatter {

    private static final int MAX_CACHE_SIZE = 512;

    /** Parsed branch cache: casesBody → {keyword → message} | 解析缓存 */
    private final ConcurrentHashMap<String, Map<String, String>> branchCache = new ConcurrentHashMap<>();

    // ==================== Public API | 公开方法 ====================

    /**
     * Selects the appropriate branch for the given count and locale
     * 为给定计数和区域选择适当的分支
     *
     * <p>Resolves in priority order: exact match ({@code =N}) → CLDR category →
     * {@code other} fallback. Replaces {@code #} with the count value in the
     * selected branch.</p>
     * <p>按优先顺序解析：精确匹配（{@code =N}）→ CLDR 类别 → {@code other} 兜底。
     * 在选中分支中将 {@code #} 替换为计数值。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * selectBranch("=0{none} one{1 item} other{# items}", 0, ENGLISH) = "none"
     * selectBranch("=0{none} one{1 item} other{# items}", 1, ENGLISH) = "1 item"
     * selectBranch("=0{none} one{1 item} other{# items}", 5, ENGLISH) = "5 items"
     * </pre>
     *
     * @param casesBody the plural cases body (without the outer {@code {var, plural, ...}} wrapper)
     *                  复数分支体（不含外层 {@code {var, plural, ...}} 包装）
     * @param count     the count value | 计数值
     * @param locale    the locale for CLDR rules | 用于 CLDR 规则的区域
     * @return the selected branch text with {@code #} replaced | 已替换 {@code #} 的选中分支文本
     * @throws OpenI18nException if no branch can be selected | 如果无法选择分支
     */
    public String selectBranch(String casesBody, long count, Locale locale) {
        Map<String, String> branches = parseBranches(casesBody);

        // 1. Try exact match =N
        String branch = branches.get("=" + count);

        // 2. Try CLDR category
        if (branch == null) {
            PluralCategory cat = PluralRules.forLocale(locale).select(count);
            branch = branches.get(cat.keyword());
        }

        // 3. Fall back to "other"
        if (branch == null) {
            branch = branches.get("other");
        }

        if (branch == null) {
            throw new OpenI18nException("PLURAL_ERROR",
                    "No matching branch for count " + count + " in plural pattern: " + casesBody);
        }

        // Replace # with the count
        return branch.replace("#", String.valueOf(count));
    }

    /**
     * Parses plural cases body into a map of keyword → branch content
     * 将复数分支体解析为关键字 → 分支内容的映射（供外部复用）
     *
     * @param casesBody the cases body | 分支体
     * @return immutable map of keyword → content | 关键字 → 内容的不可变映射
     */
    public Map<String, String> parseBranches(String casesBody) {
        Map<String, String> cached = branchCache.get(casesBody);
        if (cached != null) {
            return cached;
        }
        Map<String, String> branches = doParseBranches(casesBody);
        if (branchCache.size() < MAX_CACHE_SIZE) {
            branchCache.putIfAbsent(casesBody, branches);
        }
        return branches;
    }

    /**
     * Clears the parsed branch cache
     * 清除解析缓存
     */
    public void clearCache() {
        branchCache.clear();
    }

    // ==================== Internal Methods | 内部方法 ====================

    private static Map<String, String> doParseBranches(String casesBody) {
        Map<String, String> branches = new LinkedHashMap<>();
        int len = casesBody.length();
        int pos = 0;

        while (pos < len) {
            // Skip whitespace
            while (pos < len && Character.isWhitespace(casesBody.charAt(pos))) pos++;
            if (pos >= len) break;

            // Read keyword: either "=N" or a CLDR category word
            int keyStart = pos;
            while (pos < len && !Character.isWhitespace(casesBody.charAt(pos))
                    && casesBody.charAt(pos) != '{') {
                pos++;
            }
            if (pos == keyStart) {
                throw OpenI18nException.parseError(casesBody, "Expected keyword at position " + pos);
            }
            String keyword = casesBody.substring(keyStart, pos);

            // Skip whitespace before '{'
            while (pos < len && Character.isWhitespace(casesBody.charAt(pos))) pos++;

            if (pos >= len || casesBody.charAt(pos) != '{') {
                throw OpenI18nException.parseError(casesBody,
                        "Expected '{' after keyword '" + keyword + "' at position " + pos);
            }

            // Read brace-balanced body
            String body = readBraceBlock(casesBody, pos);
            pos += body.length() + 2; // +2 for '{' and '}'

            branches.put(keyword, body);
        }

        return Map.copyOf(branches);
    }

    /**
     * Reads the content of a brace-balanced block starting at pos (which must be '{')
     * 从 pos（必须为 '{'）开始读取括号平衡块的内容
     *
     * @return content between the outer braces (not including them) | 外层括号之间的内容
     */
    private static String readBraceBlock(String s, int pos) {
        if (pos >= s.length() || s.charAt(pos) != '{') {
            throw OpenI18nException.parseError(s, "Expected '{' at position " + pos);
        }
        int depth = 0;
        int start = pos + 1;
        for (int i = pos; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return s.substring(start, i);
            }
        }
        throw OpenI18nException.parseError(s, "Unclosed '{' at position " + pos);
    }
}

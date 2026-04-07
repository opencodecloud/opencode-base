package cloud.opencode.base.i18n.formatter;

import cloud.opencode.base.i18n.exception.OpenI18nException;
import cloud.opencode.base.i18n.plural.PluralFormatter;
import cloud.opencode.base.i18n.select.SelectFormatter;
import cloud.opencode.base.i18n.spi.MessageFormatter;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ICU-like unified message formatter without single-quote escaping
 * 无需单引号转义的 ICU 风格统一消息格式化器
 *
 * <p>Provides ICU MessageFormat-compatible syntax with the following improvements over
 * {@code java.text.MessageFormat}:</p>
 * <p>提供与 ICU MessageFormat 兼容的语法，相比 {@code java.text.MessageFormat} 有以下改进：</p>
 * <ul>
 *   <li>No single-quote escaping: {@code It's {name}'s turn} works as-is
 *       - 无需单引号转义：{@code It's {name}'s turn} 直接可用</li>
 *   <li>CLDR plural rules: {@code {count, plural, one{# item} other{# items}}}
 *       - CLDR 复数规则</li>
 *   <li>Select/gender: {@code {gender, select, male{He} female{She} other{They}}}
 *       - 选择/性别格式化</li>
 *   <li>Number formatting: {@code {amount, number, #,##0.00}}
 *       - 数字格式化</li>
 *   <li>Date/time: {@code {date, date, yyyy-MM-dd}} and {@code {time, time, HH:mm}}
 *       - 日期/时间格式化</li>
 *   <li>Named parameters via {@code Map} and positional via {@code Object...}
 *       - 命名参数和位置参数</li>
 * </ul>
 *
 * <p><strong>Syntax Reference | 语法参考:</strong></p>
 * <pre>
 * Simple:   Hello, {name}!
 * Positional: Hello, {0}!
 * Number:   Balance: {amount, number, #,##0.00}
 * Date:     Date: {date, date, yyyy-MM-dd}
 * Plural:   {count, plural, =0{no files} one{# file} other{# files}}
 * Select:   {gender, select, male{He} female{She} other{They}}
 * Escaped:  Use \{ and \} for literal braces
 * </pre>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * IcuLikeFormatter fmt = new IcuLikeFormatter();
 *
 * // Named parameters
 * fmt.format("Hello, {name}!", Locale.ENGLISH, Map.of("name", "Alice"));
 * // → "Hello, Alice!"
 *
 * // Plural (English)
 * fmt.format("You have {count, plural, one{# item} other{# items}}.",
 *            Locale.ENGLISH, Map.of("count", 5));
 * // → "You have 5 items."
 *
 * // Positional
 * fmt.format("{0} has {1, plural, one{# point} other{# points}}.",
 *            Locale.ENGLISH, "Alice", 3);
 * // → "Alice has 3 points."
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n) per format call - 时间复杂度: O(n)</li>
 *   <li>Max recursion depth: 8 - 最大递归深度: 8</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Recursion bounded: Yes (depth limit 8) - 递归有界: 是（深度限制 8）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.3
 */
public final class IcuLikeFormatter implements MessageFormatter {

    private static final int MAX_DEPTH      = 8;
    private static final int MAX_CACHE_SIZE = 1024;

    private final PluralFormatter pluralFormatter = new PluralFormatter();
    private final SelectFormatter selectFormatter = new SelectFormatter();

    /** Date/time formatter cache | 日期格式化器缓存 */
    private final ConcurrentHashMap<String, DateTimeFormatter> dateCache = new ConcurrentHashMap<>();
    /** DecimalFormat prototype cache (clone before use — DecimalFormat is not thread-safe) */
    private final ConcurrentHashMap<String, DecimalFormat> numberCache = new ConcurrentHashMap<>();

    /** Positional key cache (avoids String.valueOf(i) on hot path) | 位置键缓存 */
    private static final String[] POS_KEYS = new String[16];
    static {
        for (int i = 0; i < POS_KEYS.length; i++) POS_KEYS[i] = String.valueOf(i);
    }

    // ==================== MessageFormatter SPI ====================

    /**
     * Formats a message with positional parameters (indexed 0, 1, 2, ...)
     * 使用位置参数格式化消息（索引 0、1、2 ...）
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * format("{0} has {1, plural, one{# pt} other{# pts}}", ENGLISH, "Alice", 3)
     * = "Alice has 3 pts"
     * </pre>
     *
     * @param template the message template | 消息模板
     * @param locale   the locale | 区域
     * @param args     positional arguments | 位置参数
     * @return formatted message | 格式化消息
     */
    @Override
    public String format(String template, Locale locale, Object... args) {
        if (template == null || template.isEmpty()) return template == null ? "" : template;
        Map<String, Object> params;
        if (args == null || args.length == 0) {
            params = Map.of();
        } else {
            params = HashMap.newHashMap(args.length);
            for (int i = 0; i < args.length; i++) {
                params.put(i < POS_KEYS.length ? POS_KEYS[i] : String.valueOf(i), args[i]);
            }
        }
        return process(template, locale, params, 0);
    }

    /**
     * Formats a message with named parameters
     * 使用命名参数格式化消息
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>
     * format("Hello, {name}! You have {count, plural, one{# item} other{# items}}.",
     *        ENGLISH, Map.of("name", "Bob", "count", 5))
     * = "Hello, Bob! You have 5 items."
     * </pre>
     *
     * @param template the message template | 消息模板
     * @param locale   the locale | 区域
     * @param params   named parameters | 命名参数
     * @return formatted message | 格式化消息
     */
    @Override
    public String format(String template, Locale locale, Map<String, Object> params) {
        if (template == null || template.isEmpty()) return template == null ? "" : template;
        return process(template, locale, params != null ? params : Map.of(), 0);
    }

    /**
     * Clears all internal caches
     * 清除所有内部缓存
     */
    @Override
    public void clearCache() {
        pluralFormatter.clearCache();
        selectFormatter.clearCache();
        dateCache.clear();
        numberCache.clear();
    }

    // ==================== Internal Processing | 内部处理 ====================

    private String process(String template, Locale locale, Map<String, Object> params, int depth) {
        if (depth > MAX_DEPTH) {
            throw new OpenI18nException("Recursion depth exceeded in message template");
        }
        if (template.isEmpty()) return template;

        StringBuilder sb = new StringBuilder(template.length() + 16);
        int len = template.length();
        int pos = 0;

        while (pos < len) {
            char c = template.charAt(pos);

            // Escaped braces: \{ → { and \} → }
            if (c == '\\' && pos + 1 < len) {
                char next = template.charAt(pos + 1);
                if (next == '{' || next == '}') {
                    sb.append(next);
                    pos += 2;
                    continue;
                }
            }

            if (c == '{') {
                int end = findMatchingBrace(template, pos);
                String content = template.substring(pos + 1, end);
                sb.append(resolveExpression(content, locale, params, depth));
                pos = end + 1;
            } else {
                sb.append(c);
                pos++;
            }
        }

        return sb.toString();
    }

    /**
     * Finds the index of the closing brace matching the opening brace at {@code start}
     * 查找与 {@code start} 处开括号匹配的闭括号索引
     */
    private static int findMatchingBrace(String s, int start) {
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        throw OpenI18nException.parseError(s.substring(start), "Unclosed '{' at position " + start);
    }

    /**
     * Resolves a single expression content (the text between { and })
     * 解析单个表达式内容（{ 和 } 之间的文本）
     */
    private String resolveExpression(String content, Locale locale,
                                      Map<String, Object> params, int depth) {
        String[] parts = splitContent(content);
        String varName = parts[0];

        // Simple substitution: {name}
        if (parts.length == 1) {
            Object val = params.get(varName);
            return val != null ? String.valueOf(val) : "";
        }

        String type = parts[1].toLowerCase(Locale.ROOT).strip();
        String rest = parts.length > 2 ? parts[2].strip() : "";
        Object value = params.get(varName);

        return switch (type) {
            case "number" -> formatNumber(value, locale, rest);
            case "date"   -> formatDateTime(value, locale, rest, false);
            case "time"   -> formatDateTime(value, locale, rest, true);
            case "plural" -> formatPlural(varName, value, rest, locale, params, depth);
            case "select" -> formatSelect(value, rest, locale, params, depth);
            default -> value != null ? String.valueOf(value) : "";
        };
    }

    /**
     * Splits expression content into [varName, type, rest] parts
     * Split by comma, but only the first two commas (rest may contain commas in patterns)
     * 将表达式内容拆分为 [变量名, 类型, 其余] 部分
     */
    private static String[] splitContent(String content) {
        int first = content.indexOf(',');
        if (first < 0) return new String[]{stripFast(content)};

        String varName = stripFast(content.substring(0, first));
        String after   = content.substring(first + 1);

        int second = after.indexOf(',');
        if (second < 0) return new String[]{varName, stripFast(after)};

        String type = stripFast(after.substring(0, second));
        String rest = stripFast(after.substring(second + 1));
        return new String[]{varName, type, rest};
    }

    /** strip() that returns the same instance when no whitespace present */
    private static String stripFast(String s) {
        int len = s.length();
        if (len == 0) return s;
        if (s.charAt(0) > ' ' && s.charAt(len - 1) > ' ') return s;
        return s.strip();
    }

    // ==================== Plural | 复数格式化 ====================

    private String formatPlural(String varName, Object value, String casesBody,
                                 Locale locale, Map<String, Object> params, int depth) {
        long count = toLong(value);
        String branch = pluralFormatter.selectBranch(casesBody, count, locale);
        // Replace # with count then recursively process inner expressions
        String withHash = branch.replace("#", String.valueOf(count));
        return process(withHash, locale, params, depth + 1);
    }

    // ==================== Select | 选择格式化 ====================

    private String formatSelect(Object value, String casesBody,
                                 Locale locale, Map<String, Object> params, int depth) {
        String key = value != null ? String.valueOf(value) : "other";
        String branch = selectFormatter.format(casesBody, key);
        return process(branch, locale, params, depth + 1);
    }

    // ==================== Number | 数字格式化 ====================

    private String formatNumber(Object value, Locale locale, String pattern) {
        if (value == null) return "";
        double d = toDouble(value);
        if (pattern.isEmpty()) {
            return java.text.NumberFormat.getNumberInstance(locale).format(d);
        }
        // Cache DecimalFormat prototype, clone per call (clone ~100ns vs new ~500ns)
        String cacheKey = pattern + '\0' + locale.toLanguageTag();
        DecimalFormat prototype = numberCache.get(cacheKey);
        if (prototype == null) {
            prototype = new DecimalFormat(pattern, DecimalFormatSymbols.getInstance(locale));
            if (numberCache.size() < MAX_CACHE_SIZE) {
                numberCache.putIfAbsent(cacheKey, prototype);
            }
        }
        return ((DecimalFormat) prototype.clone()).format(d);
    }

    // ==================== Date/Time | 日期/时间格式化 ====================

    private String formatDateTime(Object value, Locale locale, String pattern, boolean timeOnly) {
        if (value == null) return "";
        DateTimeFormatter dtf = resolveFormatter(pattern, locale, timeOnly);
        return switch (value) {
            case LocalDate ld       -> dtf.format(ld);
            case LocalDateTime ldt  -> dtf.format(ldt);
            case LocalTime lt       -> dtf.format(lt);
            case ZonedDateTime zdt  -> dtf.format(zdt);
            case Instant inst       -> dtf.format(inst.atZone(ZoneId.systemDefault()));
            case java.util.Date d   -> dtf.format(d.toInstant().atZone(ZoneId.systemDefault()));
            default                 -> String.valueOf(value);
        };
    }

    private DateTimeFormatter resolveFormatter(String pattern, Locale locale, boolean timeOnly) {
        String cacheKey = pattern + "|" + locale.toLanguageTag() + "|" + timeOnly;
        DateTimeFormatter cached = dateCache.get(cacheKey);
        if (cached != null) return cached;
        DateTimeFormatter built = buildFormatter(pattern, locale, timeOnly);
        if (dateCache.size() < MAX_CACHE_SIZE) {
            dateCache.putIfAbsent(cacheKey, built);
        }
        return built;
    }

    private static DateTimeFormatter buildFormatter(String pattern, Locale locale, boolean timeOnly) {
        if (pattern.isEmpty()) {
            return timeOnly
                    ? DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale)
                    : DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
        }
        return switch (pattern.toLowerCase(Locale.ROOT)) {
            case "short"  -> timeOnly
                    ? DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale)
                    : DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(locale);
            case "medium" -> timeOnly
                    ? DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale)
                    : DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale);
            case "long"   -> timeOnly
                    ? DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG).withLocale(locale)
                    : DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale);
            default       -> DateTimeFormatter.ofPattern(pattern, locale);
        };
    }

    // ==================== Conversion Helpers | 转换辅助方法 ====================

    private static long toLong(Object value) {
        if (value == null) return 0L;
        return switch (value) {
            case Long l    -> l;
            case Integer i -> i.longValue();
            case Short s   -> s.longValue();
            case Byte b    -> b.longValue();
            case Number n  -> n.longValue();
            case String s  -> {
                try { yield Long.parseLong(s.strip()); }
                catch (NumberFormatException e) { yield 0L; }
            }
            default -> 0L;
        };
    }

    private static double toDouble(Object value) {
        if (value == null) return 0.0;
        return switch (value) {
            case Number n -> n.doubleValue();
            case String s -> {
                try { yield Double.parseDouble(s.strip()); }
                catch (NumberFormatException e) { yield 0.0; }
            }
            default -> 0.0;
        };
    }
}

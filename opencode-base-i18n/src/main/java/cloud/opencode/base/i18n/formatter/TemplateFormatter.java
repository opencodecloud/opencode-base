package cloud.opencode.base.i18n.formatter;

import cloud.opencode.base.i18n.spi.MessageFormatter;

import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.DateFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Advanced template formatter with expression support
 * 带表达式支持的高级模板格式化器
 *
 * <p>Supports advanced formatting features including type specifiers,
 * conditional expressions, and pluralization.</p>
 * <p>支持高级格式化功能，包括类型指定符、条件表达式和复数化。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type specifiers - 类型指定符</li>
 *   <li>Date/time formatting - 日期/时间格式化</li>
 *   <li>Number formatting - 数字格式化</li>
 *   <li>Pluralization support - 复数化支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TemplateFormatter formatter = new TemplateFormatter();
 *
 * // Type specifiers
 * formatter.format("Price: ${price:number:0.00}", locale, Map.of("price", 99.5));
 * // result: "Price: 99.50"
 *
 * // Date formatting
 * formatter.format("Date: ${date:date:yyyy-MM-dd}", locale, Map.of("date", LocalDate.now()));
 * // result: "Date: 2024-01-15"
 * }</pre>
 *
 * <p><strong>Supported type specifiers | 支持的类型指定符:</strong></p>
 * <ul>
 *   <li>${name:string} - String (default) | 字符串（默认）</li>
 *   <li>${name:number:pattern} - Number formatting | 数字格式化</li>
 *   <li>${name:date:pattern} - Date formatting | 日期格式化</li>
 *   <li>${name:upper} - Uppercase | 大写</li>
 *   <li>${name:lower} - Lowercase | 小写</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(n×p) where n is template length and p is number of parameters - 时间复杂度: O(n×p)，n 为模板长度，p 为参数数量</li>
 *   <li>Space complexity: O(c) for format caches where c is number of distinct patterns - 空间复杂度: O(c) 格式化缓存，c 为不同模式数量</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-i18n V1.0.0
 */
public class TemplateFormatter implements MessageFormatter {

    private static final Pattern EXPR_PATTERN = Pattern.compile("\\$\\{([^}]+)}");
    private static final Pattern TYPE_PATTERN = Pattern.compile("([^:]+)(?::([^:]+))?(?::(.+))?");
    private static final int MAX_CACHE_SIZE = 1024;

    private final Map<String, DateTimeFormatter> dateFormatCache = new ConcurrentHashMap<>();
    private final Map<String, java.text.DecimalFormat> numberFormatCache = new ConcurrentHashMap<>();

    /**
     * Creates a template formatter
     * 创建模板格式化器
     */
    public TemplateFormatter() {
    }

    @Override
    public String format(String template, Locale locale, Object... args) {
        if (template == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }

        // Convert to map with indexed keys
        Map<String, Object> params = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            params.put(String.valueOf(i), args[i]);
        }
        return format(template, locale, params);
    }

    @Override
    public String format(String template, Locale locale, Map<String, Object> params) {
        if (template == null) {
            return null;
        }
        if (params == null || params.isEmpty()) {
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = EXPR_PATTERN.matcher(template);

        while (matcher.find()) {
            String expression = matcher.group(1);
            String replacement = evaluateExpression(expression, locale, params);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    @Override
    public void clearCache() {
        dateFormatCache.clear();
        numberFormatCache.clear();
    }

    private String evaluateExpression(String expression, Locale locale, Map<String, Object> params) {
        Matcher typeMatcher = TYPE_PATTERN.matcher(expression);
        if (!typeMatcher.matches()) {
            return "${" + expression + "}";
        }

        String paramName = typeMatcher.group(1).trim();
        String type = typeMatcher.group(2);
        String format = typeMatcher.group(3);

        Object value = params.get(paramName);
        if (value == null) {
            return "";
        }

        if (type == null) {
            return value.toString();
        }

        return switch (type.toLowerCase()) {
            case "number" -> formatNumber(value, format, locale);
            case "date" -> formatDate(value, format, locale);
            case "time" -> formatTime(value, format, locale);
            case "upper" -> value.toString().toUpperCase(locale);
            case "lower" -> value.toString().toLowerCase(locale);
            case "string" -> value.toString();
            case "trim" -> value.toString().trim();
            case "length" -> String.valueOf(value.toString().length());
            default -> value.toString();
        };
    }

    private String formatNumber(Object value, String pattern, Locale locale) {
        if (value == null) {
            return "";
        }

        Number number;
        if (value instanceof Number n) {
            number = n;
        } else {
            try {
                number = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                return value.toString();
            }
        }

        if (pattern == null || pattern.isEmpty()) {
            return NumberFormat.getInstance(locale).format(number);
        }

        String cacheKey = pattern + '\0' + locale.toLanguageTag();
        java.text.DecimalFormat df = numberFormatCache.get(cacheKey);
        if (df == null) {
            df = (java.text.DecimalFormat) NumberFormat.getInstance(locale);
            df.applyPattern(pattern);
            if (numberFormatCache.size() < MAX_CACHE_SIZE) {
                numberFormatCache.putIfAbsent(cacheKey, df);
            }
        }

        return ((java.text.DecimalFormat) df.clone()).format(number);
    }

    private String formatDate(Object value, String pattern, Locale locale) {
        if (value == null) {
            return "";
        }

        if (value instanceof TemporalAccessor temporal) {
            DateTimeFormatter formatter;
            if (pattern == null || pattern.isEmpty()) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", locale);
            } else {
                String cacheKey = pattern + '\0' + locale.toLanguageTag();
                formatter = dateFormatCache.get(cacheKey);
                if (formatter == null) {
                    formatter = DateTimeFormatter.ofPattern(pattern, locale);
                    if (dateFormatCache.size() < MAX_CACHE_SIZE) {
                        dateFormatCache.putIfAbsent(cacheKey, formatter);
                    }
                }
            }
            return formatter.format(temporal);
        }

        if (value instanceof Date date) {
            DateFormat df;
            if (pattern == null || pattern.isEmpty()) {
                df = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
            } else {
                df = new java.text.SimpleDateFormat(pattern, locale);
            }
            return df.format(date);
        }

        return value.toString();
    }

    private String formatTime(Object value, String pattern, Locale locale) {
        if (value == null) {
            return "";
        }

        if (value instanceof TemporalAccessor temporal) {
            DateTimeFormatter formatter;
            if (pattern == null || pattern.isEmpty()) {
                formatter = DateTimeFormatter.ofPattern("HH:mm:ss", locale);
            } else {
                String cacheKey = "time\0" + pattern + '\0' + locale.toLanguageTag();
                formatter = dateFormatCache.get(cacheKey);
                if (formatter == null) {
                    formatter = DateTimeFormatter.ofPattern(pattern, locale);
                    if (dateFormatCache.size() < MAX_CACHE_SIZE) {
                        dateFormatCache.putIfAbsent(cacheKey, formatter);
                    }
                }
            }
            return formatter.format(temporal);
        }

        if (value instanceof Date date) {
            DateFormat df;
            if (pattern == null || pattern.isEmpty()) {
                df = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
            } else {
                df = new java.text.SimpleDateFormat(pattern, locale);
            }
            return df.format(date);
        }

        return value.toString();
    }

    /**
     * Gets the date format cache size
     * 获取日期格式缓存大小
     *
     * @return cache size | 缓存大小
     */
    public int getDateFormatCacheSize() {
        return dateFormatCache.size();
    }

    /**
     * Gets the number format cache size
     * 获取数字格式缓存大小
     *
     * @return cache size | 缓存大小
     */
    public int getNumberFormatCacheSize() {
        return numberFormatCache.size();
    }
}

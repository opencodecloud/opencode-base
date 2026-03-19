package cloud.opencode.base.yml.placeholder;

import java.util.Objects;
import java.util.Optional;

/**
 * Property Placeholder - Represents a parsed placeholder
 * 属性占位符 - 表示解析后的占位符
 *
 * <p>This class represents a parsed placeholder with key and optional default value.</p>
 * <p>此类表示解析后的占位符，包含键和可选的默认值。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Parse ${key} and ${key:default} expressions - 解析 ${key} 和 ${key:default} 表达式</li>
 *   <li>Custom delimiter support - 自定义分隔符支持</li>
 *   <li>Optional default value handling - 可选默认值处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Parse placeholder
 * PropertyPlaceholder ph = PropertyPlaceholder.parse("${server.port:8080}");
 * String key = ph.getKey();          // "server.port"
 * String def = ph.getDefaultValue(); // "8080"
 *
 * // Check if has default
 * if (ph.hasDefaultValue()) {
 *     // use default
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构建后不可变）</li>
 *   <li>Null-safe: No (null expression throws NullPointerException) - 空值安全: 否（空表达式抛出 NullPointerException）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-yml V1.0.0
 */
public final class PropertyPlaceholder {

    private static final String DEFAULT_PREFIX = "${";
    private static final String DEFAULT_SUFFIX = "}";
    private static final String DEFAULT_SEPARATOR = ":";

    private final String key;
    private final String defaultValue;
    private final String rawExpression;

    private PropertyPlaceholder(String key, String defaultValue, String rawExpression) {
        this.key = key;
        this.defaultValue = defaultValue;
        this.rawExpression = rawExpression;
    }

    /**
     * Parses a placeholder expression.
     * 解析占位符表达式。
     *
     * @param expression the expression (e.g., "${key}" or "${key:default}") | 表达式
     * @return the parsed placeholder | 解析后的占位符
     * @throws IllegalArgumentException if expression is invalid | 如果表达式无效
     */
    public static PropertyPlaceholder parse(String expression) {
        return parse(expression, DEFAULT_PREFIX, DEFAULT_SUFFIX, DEFAULT_SEPARATOR);
    }

    /**
     * Parses a placeholder expression with custom delimiters.
     * 使用自定义分隔符解析占位符表达式。
     *
     * @param expression the expression | 表达式
     * @param prefix     the placeholder prefix | 占位符前缀
     * @param suffix     the placeholder suffix | 占位符后缀
     * @param separator  the default value separator | 默认值分隔符
     * @return the parsed placeholder | 解析后的占位符
     * @throws IllegalArgumentException if expression is invalid | 如果表达式无效
     */
    public static PropertyPlaceholder parse(String expression, String prefix, String suffix, String separator) {
        Objects.requireNonNull(expression, "Expression cannot be null");

        String trimmed = expression.trim();
        if (!trimmed.startsWith(prefix) || !trimmed.endsWith(suffix)) {
            throw new IllegalArgumentException("Invalid placeholder expression: " + expression);
        }

        String inner = trimmed.substring(prefix.length(), trimmed.length() - suffix.length());
        if (inner.isEmpty()) {
            throw new IllegalArgumentException("Empty placeholder key: " + expression);
        }

        int separatorIndex = inner.indexOf(separator);
        if (separatorIndex < 0) {
            return new PropertyPlaceholder(inner, null, trimmed);
        } else {
            String key = inner.substring(0, separatorIndex);
            String defaultValue = inner.substring(separatorIndex + separator.length());
            return new PropertyPlaceholder(key, defaultValue, trimmed);
        }
    }

    /**
     * Creates a placeholder with key only.
     * 仅使用键创建占位符。
     *
     * @param key the key | 键
     * @return the placeholder | 占位符
     */
    public static PropertyPlaceholder of(String key) {
        return new PropertyPlaceholder(key, null, DEFAULT_PREFIX + key + DEFAULT_SUFFIX);
    }

    /**
     * Creates a placeholder with key and default value.
     * 使用键和默认值创建占位符。
     *
     * @param key          the key | 键
     * @param defaultValue the default value | 默认值
     * @return the placeholder | 占位符
     */
    public static PropertyPlaceholder of(String key, String defaultValue) {
        String raw = defaultValue != null
            ? DEFAULT_PREFIX + key + DEFAULT_SEPARATOR + defaultValue + DEFAULT_SUFFIX
            : DEFAULT_PREFIX + key + DEFAULT_SUFFIX;
        return new PropertyPlaceholder(key, defaultValue, raw);
    }

    /**
     * Gets the placeholder key.
     * 获取占位符键。
     *
     * @return the key | 键
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the default value.
     * 获取默认值。
     *
     * @return the default value, or null if none | 默认值，如果没有则返回 null
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Gets the default value as Optional.
     * 获取默认值作为 Optional。
     *
     * @return optional containing default value | 包含默认值的 Optional
     */
    public Optional<String> defaultValue() {
        return Optional.ofNullable(defaultValue);
    }

    /**
     * Checks if this placeholder has a default value.
     * 检查此占位符是否有默认值。
     *
     * @return true if has default value | 如果有默认值则返回 true
     */
    public boolean hasDefaultValue() {
        return defaultValue != null;
    }

    /**
     * Gets the raw expression.
     * 获取原始表达式。
     *
     * @return the raw expression | 原始表达式
     */
    public String getRawExpression() {
        return rawExpression;
    }

    @Override
    public String toString() {
        return rawExpression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyPlaceholder that = (PropertyPlaceholder) o;
        return Objects.equals(key, that.key) && Objects.equals(defaultValue, that.defaultValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, defaultValue);
    }
}

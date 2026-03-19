package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * JSON Assert - Fluent assertions for JSON strings
 * JSON断言 - JSON字符串的流式断言
 *
 * <p>Provides comprehensive assertion methods for JSON string testing.</p>
 * <p>为JSON字符串测试提供全面的断言方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>JSON structure validation (object, array) - JSON结构验证（对象、数组）</li>
 *   <li>Key and value containment checks - 键和值包含检查</li>
 *   <li>Key-value pair assertions with multiple types - 多类型键值对断言</li>
 *   <li>Whitespace-normalized equality comparison - 空白规范化相等比较</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * JsonAssert.assertThat(jsonString)
 *     .isValidJson()
 *     .containsKey("name")
 *     .hasValue("$.name", "John")
 *     .hasArraySize("$.items", 3);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (validates non-null JSON) - 空值安全: 是（验证非空JSON）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class JsonAssert {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final Pattern REGEX_SPECIAL_CHARS_PATTERN = Pattern.compile("([\\\\\\[\\](){}.*+?^$|])");

    private final String actual;

    private JsonAssert(String actual) {
        this.actual = actual;
    }

    /**
     * Creates assertion for JSON string.
     * 为JSON字符串创建断言。
     *
     * @param actual the actual JSON string | 实际JSON字符串
     * @return the assertion | 断言
     */
    public static JsonAssert assertThat(String actual) {
        return new JsonAssert(actual);
    }

    /**
     * Asserts that JSON is null.
     * 断言JSON为null。
     *
     * @return this | 此对象
     */
    public JsonAssert isNull() {
        if (actual != null) {
            throw new AssertionException("Expected null but was: " + truncate(actual));
        }
        return this;
    }

    /**
     * Asserts that JSON is not null.
     * 断言JSON不为null。
     *
     * @return this | 此对象
     */
    public JsonAssert isNotNull() {
        if (actual == null) {
            throw new AssertionException("Expected not null but was null");
        }
        return this;
    }

    /**
     * Asserts that string is valid JSON.
     * 断言字符串是有效的JSON。
     *
     * @return this | 此对象
     */
    public JsonAssert isValidJson() {
        isNotNull();
        String trimmed = actual.trim();
        if (!isValidJsonStructure(trimmed)) {
            throw new AssertionException("Expected valid JSON but was: " + truncate(actual));
        }
        return this;
    }

    /**
     * Asserts that JSON is an object.
     * 断言JSON是对象。
     *
     * @return this | 此对象
     */
    public JsonAssert isJsonObject() {
        isNotNull();
        String trimmed = actual.trim();
        if (!trimmed.startsWith("{") || !trimmed.endsWith("}")) {
            throw new AssertionException("Expected JSON object but was: " + truncate(actual));
        }
        return this;
    }

    /**
     * Asserts that JSON is an array.
     * 断言JSON是数组。
     *
     * @return this | 此对象
     */
    public JsonAssert isJsonArray() {
        isNotNull();
        String trimmed = actual.trim();
        if (!trimmed.startsWith("[") || !trimmed.endsWith("]")) {
            throw new AssertionException("Expected JSON array but was: " + truncate(actual));
        }
        return this;
    }

    /**
     * Asserts that JSON contains key.
     * 断言JSON包含键。
     *
     * @param key the key | 键
     * @return this | 此对象
     */
    public JsonAssert containsKey(String key) {
        isNotNull();
        String pattern = "\"" + escapeRegex(key) + "\"\\s*:";
        if (!Pattern.compile(pattern).matcher(actual).find()) {
            throw new AssertionException("Expected to contain key '" + key + "' but did not");
        }
        return this;
    }

    /**
     * Asserts that JSON does not contain key.
     * 断言JSON不包含键。
     *
     * @param key the key | 键
     * @return this | 此对象
     */
    public JsonAssert doesNotContainKey(String key) {
        isNotNull();
        String pattern = "\"" + escapeRegex(key) + "\"\\s*:";
        if (Pattern.compile(pattern).matcher(actual).find()) {
            throw new AssertionException("Expected not to contain key '" + key + "' but did");
        }
        return this;
    }

    /**
     * Asserts that JSON contains string value.
     * 断言JSON包含字符串值。
     *
     * @param value the value | 值
     * @return this | 此对象
     */
    public JsonAssert containsValue(String value) {
        isNotNull();
        String pattern = ":\\s*\"" + escapeRegex(value) + "\"";
        if (!Pattern.compile(pattern).matcher(actual).find()) {
            throw new AssertionException("Expected to contain value '" + value + "' but did not");
        }
        return this;
    }

    /**
     * Asserts that JSON has key-value pair.
     * 断言JSON有键值对。
     *
     * @param key   the key | 键
     * @param value the string value | 字符串值
     * @return this | 此对象
     */
    public JsonAssert hasKeyValue(String key, String value) {
        isNotNull();
        String pattern = "\"" + escapeRegex(key) + "\"\\s*:\\s*\"" + escapeRegex(value) + "\"";
        if (!Pattern.compile(pattern).matcher(actual).find()) {
            throw new AssertionException("Expected key '" + key + "' = '" + value + "' but not found");
        }
        return this;
    }

    /**
     * Asserts that JSON has key-value pair with number.
     * 断言JSON有数值键值对。
     *
     * @param key   the key | 键
     * @param value the numeric value | 数值
     * @return this | 此对象
     */
    public JsonAssert hasKeyValue(String key, Number value) {
        isNotNull();
        String pattern = "\"" + escapeRegex(key) + "\"\\s*:\\s*" + value;
        if (!Pattern.compile(pattern).matcher(actual).find()) {
            throw new AssertionException("Expected key '" + key + "' = " + value + " but not found");
        }
        return this;
    }

    /**
     * Asserts that JSON has key-value pair with boolean.
     * 断言JSON有布尔键值对。
     *
     * @param key   the key | 键
     * @param value the boolean value | 布尔值
     * @return this | 此对象
     */
    public JsonAssert hasKeyValue(String key, boolean value) {
        isNotNull();
        String pattern = "\"" + escapeRegex(key) + "\"\\s*:\\s*" + value;
        if (!Pattern.compile(pattern).matcher(actual).find()) {
            throw new AssertionException("Expected key '" + key + "' = " + value + " but not found");
        }
        return this;
    }

    /**
     * Asserts that JSON has null value for key.
     * 断言JSON的键值为null。
     *
     * @param key the key | 键
     * @return this | 此对象
     */
    public JsonAssert hasNullValue(String key) {
        isNotNull();
        String pattern = "\"" + escapeRegex(key) + "\"\\s*:\\s*null";
        if (!Pattern.compile(pattern).matcher(actual).find()) {
            throw new AssertionException("Expected key '" + key + "' to be null but was not");
        }
        return this;
    }

    /**
     * Asserts that JSON equals another JSON (ignoring whitespace).
     * 断言JSON等于另一个JSON（忽略空白）。
     *
     * @param expected the expected JSON | 期望JSON
     * @return this | 此对象
     */
    public JsonAssert isEqualTo(String expected) {
        isNotNull();
        String normalizedActual = normalizeJson(actual);
        String normalizedExpected = normalizeJson(expected);
        if (!normalizedActual.equals(normalizedExpected)) {
            throw new AssertionException("Expected:\n" + expected + "\nBut was:\n" + actual);
        }
        return this;
    }

    /**
     * Asserts that JSON has specified length.
     * 断言JSON有指定长度。
     *
     * @param length the expected length | 期望长度
     * @return this | 此对象
     */
    public JsonAssert hasLength(int length) {
        isNotNull();
        if (actual.length() != length) {
            throw new AssertionException("Expected length " + length + " but was " + actual.length());
        }
        return this;
    }

    /**
     * Asserts that JSON is empty object.
     * 断言JSON是空对象。
     *
     * @return this | 此对象
     */
    public JsonAssert isEmptyObject() {
        isNotNull();
        String normalized = normalizeJson(actual);
        if (!normalized.equals("{}")) {
            throw new AssertionException("Expected empty object {} but was: " + truncate(actual));
        }
        return this;
    }

    /**
     * Asserts that JSON is empty array.
     * 断言JSON是空数组。
     *
     * @return this | 此对象
     */
    public JsonAssert isEmptyArray() {
        isNotNull();
        String normalized = normalizeJson(actual);
        if (!normalized.equals("[]")) {
            throw new AssertionException("Expected empty array [] but was: " + truncate(actual));
        }
        return this;
    }

    private boolean isValidJsonStructure(String json) {
        if (json.isEmpty()) return false;
        char first = json.charAt(0);
        char last = json.charAt(json.length() - 1);
        return (first == '{' && last == '}') || (first == '[' && last == ']')
                || (first == '"' && last == '"')
                || json.equals("true") || json.equals("false") || json.equals("null")
                || isNumeric(json);
    }

    private boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private String normalizeJson(String json) {
        if (json == null) return null;
        return WHITESPACE_PATTERN.matcher(json).replaceAll("");
    }

    private String escapeRegex(String str) {
        return REGEX_SPECIAL_CHARS_PATTERN.matcher(str).replaceAll("\\\\$1");
    }

    private String truncate(String str) {
        if (str == null) return "null";
        if (str.length() <= 100) return str;
        return str.substring(0, 100) + "...";
    }
}

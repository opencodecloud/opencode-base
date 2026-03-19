package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * String Assert - Fluent assertions for strings
 * 字符串断言 - 字符串的流式断言
 *
 * <p>Provides comprehensive assertion methods for String types.</p>
 * <p>为String类型提供全面的断言方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Empty, blank, length checks - 空、空白、长度检查</li>
 *   <li>Content checks (contains, startsWith, endsWith) - 内容检查（包含、前缀、后缀）</li>
 *   <li>Regex and pattern matching - 正则和模式匹配</li>
 *   <li>Case and character type checks - 大小写和字符类型检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * StringAssert.assertThat(str)
 *     .isNotBlank()
 *     .startsWith("Hello")
 *     .endsWith("World")
 *     .contains("lo Wo")
 *     .hasLength(11);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (validates non-null string) - 空值安全: 是（验证非空字符串）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class StringAssert {

    private final String actual;

    private StringAssert(String actual) {
        this.actual = actual;
    }

    /**
     * Creates assertion for string.
     * 为字符串创建断言。
     *
     * @param actual the actual string | 实际字符串
     * @return the assertion | 断言
     */
    public static StringAssert assertThat(String actual) {
        return new StringAssert(actual);
    }

    /**
     * Asserts that string is null.
     * 断言字符串为null。
     *
     * @return this | 此对象
     */
    public StringAssert isNull() {
        if (actual != null) {
            throw new AssertionException("Expected null but was: '" + actual + "'");
        }
        return this;
    }

    /**
     * Asserts that string is not null.
     * 断言字符串不为null。
     *
     * @return this | 此对象
     */
    public StringAssert isNotNull() {
        if (actual == null) {
            throw new AssertionException("Expected not null but was null");
        }
        return this;
    }

    /**
     * Asserts that string is empty.
     * 断言字符串为空。
     *
     * @return this | 此对象
     */
    public StringAssert isEmpty() {
        isNotNull();
        if (!actual.isEmpty()) {
            throw new AssertionException("Expected empty but was: '" + actual + "'");
        }
        return this;
    }

    /**
     * Asserts that string is not empty.
     * 断言字符串不为空。
     *
     * @return this | 此对象
     */
    public StringAssert isNotEmpty() {
        isNotNull();
        if (actual.isEmpty()) {
            throw new AssertionException("Expected not empty but was empty");
        }
        return this;
    }

    /**
     * Asserts that string is blank.
     * 断言字符串为空白。
     *
     * @return this | 此对象
     */
    public StringAssert isBlank() {
        isNotNull();
        if (!actual.isBlank()) {
            throw new AssertionException("Expected blank but was: '" + actual + "'");
        }
        return this;
    }

    /**
     * Asserts that string is not blank.
     * 断言字符串不为空白。
     *
     * @return this | 此对象
     */
    public StringAssert isNotBlank() {
        isNotNull();
        if (actual.isBlank()) {
            throw new AssertionException("Expected not blank but was blank");
        }
        return this;
    }

    /**
     * Asserts that string has specified length.
     * 断言字符串有指定长度。
     *
     * @param length the expected length | 期望长度
     * @return this | 此对象
     */
    public StringAssert hasLength(int length) {
        isNotNull();
        if (actual.length() != length) {
            throw new AssertionException("Expected length " + length + " but was " + actual.length());
        }
        return this;
    }

    /**
     * Asserts that string has length between.
     * 断言字符串长度在范围内。
     *
     * @param min minimum length | 最小长度
     * @param max maximum length | 最大长度
     * @return this | 此对象
     */
    public StringAssert hasLengthBetween(int min, int max) {
        isNotNull();
        int len = actual.length();
        if (len < min || len > max) {
            throw new AssertionException("Expected length between " + min + " and " + max + " but was " + len);
        }
        return this;
    }

    /**
     * Asserts that string equals another.
     * 断言字符串等于另一个。
     *
     * @param expected the expected string | 期望字符串
     * @return this | 此对象
     */
    public StringAssert isEqualTo(String expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionException("Expected '" + expected + "' but was '" + actual + "'");
        }
        return this;
    }

    /**
     * Asserts that string equals another ignoring case.
     * 断言字符串等于另一个（忽略大小写）。
     *
     * @param expected the expected string | 期望字符串
     * @return this | 此对象
     */
    public StringAssert isEqualToIgnoringCase(String expected) {
        isNotNull();
        if (expected == null || !actual.equalsIgnoreCase(expected)) {
            throw new AssertionException("Expected '" + expected + "' (ignoring case) but was '" + actual + "'");
        }
        return this;
    }

    /**
     * Asserts that string contains substring.
     * 断言字符串包含子字符串。
     *
     * @param substring the substring | 子字符串
     * @return this | 此对象
     */
    public StringAssert contains(String substring) {
        isNotNull();
        if (!actual.contains(substring)) {
            throw new AssertionException("Expected to contain '" + substring + "' but did not");
        }
        return this;
    }

    /**
     * Asserts that string does not contain substring.
     * 断言字符串不包含子字符串。
     *
     * @param substring the substring | 子字符串
     * @return this | 此对象
     */
    public StringAssert doesNotContain(String substring) {
        isNotNull();
        if (actual.contains(substring)) {
            throw new AssertionException("Expected not to contain '" + substring + "' but did");
        }
        return this;
    }

    /**
     * Asserts that string starts with prefix.
     * 断言字符串以前缀开始。
     *
     * @param prefix the prefix | 前缀
     * @return this | 此对象
     */
    public StringAssert startsWith(String prefix) {
        isNotNull();
        if (!actual.startsWith(prefix)) {
            throw new AssertionException("Expected to start with '" + prefix + "' but did not");
        }
        return this;
    }

    /**
     * Asserts that string ends with suffix.
     * 断言字符串以后缀结束。
     *
     * @param suffix the suffix | 后缀
     * @return this | 此对象
     */
    public StringAssert endsWith(String suffix) {
        isNotNull();
        if (!actual.endsWith(suffix)) {
            throw new AssertionException("Expected to end with '" + suffix + "' but did not");
        }
        return this;
    }

    /**
     * Asserts that string matches regex.
     * 断言字符串匹配正则表达式。
     *
     * @param regex the regex | 正则表达式
     * @return this | 此对象
     */
    public StringAssert matches(String regex) {
        isNotNull();
        if (!actual.matches(regex)) {
            throw new AssertionException("Expected to match '" + regex + "' but did not");
        }
        return this;
    }

    /**
     * Asserts that string matches pattern.
     * 断言字符串匹配模式。
     *
     * @param pattern the pattern | 模式
     * @return this | 此对象
     */
    public StringAssert matches(Pattern pattern) {
        isNotNull();
        if (!pattern.matcher(actual).matches()) {
            throw new AssertionException("Expected to match pattern but did not");
        }
        return this;
    }

    /**
     * Asserts that string contains only digits.
     * 断言字符串只包含数字。
     *
     * @return this | 此对象
     */
    public StringAssert containsOnlyDigits() {
        isNotNull();
        for (char c : actual.toCharArray()) {
            if (!Character.isDigit(c)) {
                throw new AssertionException("Expected only digits but found: '" + c + "'");
            }
        }
        return this;
    }

    /**
     * Asserts that string contains only letters.
     * 断言字符串只包含字母。
     *
     * @return this | 此对象
     */
    public StringAssert containsOnlyLetters() {
        isNotNull();
        for (char c : actual.toCharArray()) {
            if (!Character.isLetter(c)) {
                throw new AssertionException("Expected only letters but found: '" + c + "'");
            }
        }
        return this;
    }

    /**
     * Asserts that string is all uppercase.
     * 断言字符串全部大写。
     *
     * @return this | 此对象
     */
    public StringAssert isUpperCase() {
        isNotNull();
        if (!actual.equals(actual.toUpperCase())) {
            throw new AssertionException("Expected uppercase but was: '" + actual + "'");
        }
        return this;
    }

    /**
     * Asserts that string is all lowercase.
     * 断言字符串全部小写。
     *
     * @return this | 此对象
     */
    public StringAssert isLowerCase() {
        isNotNull();
        if (!actual.equals(actual.toLowerCase())) {
            throw new AssertionException("Expected lowercase but was: '" + actual + "'");
        }
        return this;
    }

    /**
     * Asserts that trimmed strings are equal.
     * 断言去除空白后的字符串相等。
     *
     * @param expected the expected string | 期望字符串
     * @return this | 此对象
     */
    public StringAssert isEqualToIgnoringWhitespace(String expected) {
        isNotNull();
        if (expected == null || !actual.trim().equals(expected.trim())) {
            throw new AssertionException("Expected '" + expected + "' (ignoring whitespace) but was '" + actual + "'");
        }
        return this;
    }
}

/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.test.exception;

import java.util.Objects;

/**
 * Equals Assertion Exception - Exception for equality assertion failures
 * 相等断言异常 - 相等断言失败时抛出的异常
 *
 * <p>This specialized exception is thrown when an equality assertion fails,
 * providing detailed information about expected and actual values.</p>
 * <p>此专门异常在相等断言失败时抛出，提供期望值和实际值的详细信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Expected/actual value tracking - 期望值/实际值跟踪</li>
 *   <li>Detailed comparison info with type and diff index - 带类型和差异索引的详细比较信息</li>
 *   <li>Factory methods and assertion helpers - 工厂方法和断言辅助</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Basic usage
 * throw EqualsAssertionException.of("expected", "actual");
 *
 * // With custom message
 * throw EqualsAssertionException.of("expected", "actual", "Values should match");
 *
 * // Check values and throw if not equal
 * EqualsAssertionException.assertEqualsOrThrow(expected, actual);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (handles null expected/actual values) - 空值安全: 是（处理空的期望值/实际值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see AssertionException
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class EqualsAssertionException extends AssertionException {

    private final Object expected;
    private final Object actual;

    // ==================== Constructors | 构造器 ====================

    /**
     * Creates equals assertion exception with expected and actual values.
     * 使用期望值和实际值创建相等断言异常。
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     */
    public EqualsAssertionException(Object expected, Object actual) {
        super(TestErrorCode.ASSERTION_EQUALS, formatMessage(expected, actual, null));
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * Creates equals assertion exception with expected, actual values and message.
     * 使用期望值、实际值和消息创建相等断言异常。
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @param message  additional message | 附加消息
     */
    public EqualsAssertionException(Object expected, Object actual, String message) {
        super(TestErrorCode.ASSERTION_EQUALS, formatMessage(expected, actual, message));
        this.expected = expected;
        this.actual = actual;
    }

    /**
     * Creates equals assertion exception with message only.
     * 仅使用消息创建相等断言异常。
     *
     * @param message the message | 消息
     */
    public EqualsAssertionException(String message) {
        super(message);
        this.expected = null;
        this.actual = null;
    }

    /**
     * Creates equals assertion exception with message and cause.
     * 使用消息和原因创建相等断言异常。
     *
     * @param message the message | 消息
     * @param cause   the cause | 原因
     */
    public EqualsAssertionException(String message, Throwable cause) {
        super(message, cause);
        this.expected = null;
        this.actual = null;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for mismatched values.
     * 为不匹配的值创建异常。
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @return the exception | 异常
     */
    public static EqualsAssertionException of(Object expected, Object actual) {
        return new EqualsAssertionException(expected, actual);
    }

    /**
     * Creates exception for mismatched values with message.
     * 为不匹配的值创建异常（带消息）。
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @param message  additional message | 附加消息
     * @return the exception | 异常
     */
    public static EqualsAssertionException of(Object expected, Object actual, String message) {
        return new EqualsAssertionException(expected, actual, message);
    }

    /**
     * Creates exception for null expected value.
     * 为空期望值创建异常。
     *
     * @param actual the actual value | 实际值
     * @return the exception | 异常
     */
    public static EqualsAssertionException expectedNull(Object actual) {
        return new EqualsAssertionException(null, actual, "Expected null but was: " + actual);
    }

    /**
     * Creates exception for null actual value.
     * 为空实际值创建异常。
     *
     * @param expected the expected value | 期望值
     * @return the exception | 异常
     */
    public static EqualsAssertionException actualNull(Object expected) {
        return new EqualsAssertionException(expected, null, "Expected: " + expected + " but was null");
    }

    // ==================== Assertion Methods | 断言方法 ====================

    /**
     * Assert equality and throw if not equal.
     * 断言相等，如果不相等则抛出异常。
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @throws EqualsAssertionException if not equal | 如果不相等
     */
    public static void assertEqualsOrThrow(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            throw new EqualsAssertionException(expected, actual);
        }
    }

    /**
     * Assert equality and throw if not equal, with message.
     * 断言相等，如果不相等则抛出异常（带消息）。
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @param message  the message | 消息
     * @throws EqualsAssertionException if not equal | 如果不相等
     */
    public static void assertEqualsOrThrow(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new EqualsAssertionException(expected, actual, message);
        }
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Get the expected value.
     * 获取期望值。
     *
     * @return the expected value | 期望值
     */
    public Object getExpected() {
        return expected;
    }

    /**
     * Get the actual value.
     * 获取实际值。
     *
     * @return the actual value | 实际值
     */
    public Object getActual() {
        return actual;
    }

    /**
     * Get the expected value as string.
     * 获取期望值的字符串表示。
     *
     * @return expected as string | 期望值字符串
     */
    public String getExpectedString() {
        return String.valueOf(expected);
    }

    /**
     * Get the actual value as string.
     * 获取实际值的字符串表示。
     *
     * @return actual as string | 实际值字符串
     */
    public String getActualString() {
        return String.valueOf(actual);
    }

    // ==================== Comparison Info | 比较信息 ====================

    /**
     * Get detailed comparison info.
     * 获取详细比较信息。
     *
     * @return comparison info | 比较信息
     */
    public String getComparisonInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Equality Assertion Failed\n");
        sb.append("  Expected: ").append(formatValue(expected)).append("\n");
        sb.append("  Actual:   ").append(formatValue(actual)).append("\n");

        if (expected != null && actual != null) {
            sb.append("  Expected Type: ").append(expected.getClass().getName()).append("\n");
            sb.append("  Actual Type:   ").append(actual.getClass().getName()).append("\n");

            if (expected instanceof String && actual instanceof String) {
                String expectedStr = (String) expected;
                String actualStr = (String) actual;
                sb.append("  Expected Length: ").append(expectedStr.length()).append("\n");
                sb.append("  Actual Length:   ").append(actualStr.length()).append("\n");

                int diffIndex = findFirstDifferenceIndex(expectedStr, actualStr);
                if (diffIndex >= 0) {
                    sb.append("  First Difference at index: ").append(diffIndex).append("\n");
                }
            }
        }

        return sb.toString();
    }

    // ==================== Helper Methods | 辅助方法 ====================

    private static String formatMessage(Object expected, Object actual, String message) {
        StringBuilder sb = new StringBuilder();
        sb.append("expected: <").append(expected).append(">");
        sb.append(" but was: <").append(actual).append(">");

        if (message != null && !message.isEmpty()) {
            sb.append(" - ").append(message);
        }

        return sb.toString();
    }

    private static String formatValue(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String) {
            return "\"" + value + "\"";
        }
        if (value instanceof Character) {
            return "'" + value + "'";
        }
        return String.valueOf(value);
    }

    private static int findFirstDifferenceIndex(String s1, String s2) {
        int minLength = Math.min(s1.length(), s2.length());
        for (int i = 0; i < minLength; i++) {
            if (s1.charAt(i) != s2.charAt(i)) {
                return i;
            }
        }
        if (s1.length() != s2.length()) {
            return minLength;
        }
        return -1;
    }
}

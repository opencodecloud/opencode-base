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
package cloud.opencode.base.test.assertion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Soft Assert - Collects assertion failures without throwing immediately
 * 软断言 - 收集断言失败而不立即抛出
 *
 * <p>A soft assertion class that accumulates assertion failures during test execution
 * and throws all collected errors at once when {@link #assertAll()} is called.</p>
 * <p>软断言类，在测试执行期间累积断言失败，并在调用 {@link #assertAll()} 时一次性抛出所有收集的错误。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Collect multiple assertion failures without stopping test execution - 收集多个断言失败而不停止测试执行</li>
 *   <li>Fluent API for chained assertions - 流式API用于链式断言</li>
 *   <li>Thread-safe using CopyOnWriteArrayList - 使用CopyOnWriteArrayList实现线程安全</li>
 *   <li>Detailed failure reporting with all collected errors - 详细的失败报告，包含所有收集的错误</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SoftAssert softAssert = new SoftAssert();
 * softAssert.isNotNull(value1)
 *           .isEqualTo(expected, actual)
 *           .isTrue(condition);
 * softAssert.assertAll(); // Throws if any assertion failed
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (CopyOnWriteArrayList for failures) - 线程安全: 是（失败列表使用CopyOnWriteArrayList）</li>
 *   <li>Null-safe: Yes (handles null values gracefully) - 空值安全: 是（优雅处理空值）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see OpenAssertions
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class SoftAssert {

    /**
     * Thread-safe list to store assertion failures
     * 线程安全列表，用于存储断言失败
     */
    private final List<AssertionError> failures = new ArrayList<>();

    /**
     * Creates a new SoftAssert instance
     * 创建新的SoftAssert实例
     */
    public SoftAssert() {
        // Default constructor
    }

    // ==================== Null Assertions | 空值断言 ====================

    /**
     * Asserts that the object is null
     * 断言对象为null
     *
     * @param actual the actual value | 实际值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNull(Object actual) {
        if (actual != null) {
            addFailure("Expected null but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that the object is null with custom message
     * 断言对象为null（带自定义消息）
     *
     * @param actual  the actual value | 实际值
     * @param message the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNull(Object actual, String message) {
        if (actual != null) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the object is null with lazy message
     * 断言对象为null（带延迟消息）
     *
     * @param actual          the actual value | 实际值
     * @param messageSupplier the message supplier | 消息提供者
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNull(Object actual, Supplier<String> messageSupplier) {
        if (actual != null) {
            addFailure(messageSupplier.get());
        }
        return this;
    }

    /**
     * Asserts that the object is not null
     * 断言对象不为null
     *
     * @param actual the actual value | 实际值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotNull(Object actual) {
        return isNotNull(actual, "Expected non-null value but was null");
    }

    /**
     * Asserts that the object is not null with custom message
     * 断言对象不为null（带自定义消息）
     *
     * @param actual  the actual value | 实际值
     * @param message the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotNull(Object actual, String message) {
        if (actual == null) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the object is not null with lazy message
     * 断言对象不为null（带延迟消息）
     *
     * @param actual          the actual value | 实际值
     * @param messageSupplier the message supplier | 消息提供者
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotNull(Object actual, Supplier<String> messageSupplier) {
        if (actual == null) {
            addFailure(messageSupplier.get());
        }
        return this;
    }

    // ==================== Equality Assertions | 相等断言 ====================

    /**
     * Asserts that two objects are equal
     * 断言两个对象相等
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isEqualTo(Object expected, Object actual) {
        if (!Objects.equals(expected, actual)) {
            addFailure("Expected: " + expected + " but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that two objects are equal with custom message
     * 断言两个对象相等（带自定义消息）
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @param message  the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isEqualTo(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that two objects are equal with lazy message
     * 断言两个对象相等（带延迟消息）
     *
     * @param expected        the expected value | 期望值
     * @param actual          the actual value | 实际值
     * @param messageSupplier the message supplier | 消息提供者
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isEqualTo(Object expected, Object actual, Supplier<String> messageSupplier) {
        if (!Objects.equals(expected, actual)) {
            addFailure(messageSupplier.get());
        }
        return this;
    }

    /**
     * Asserts that two objects are not equal
     * 断言两个对象不相等
     *
     * @param unexpected the unexpected value | 不期望值
     * @param actual     the actual value | 实际值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotEqualTo(Object unexpected, Object actual) {
        if (Objects.equals(unexpected, actual)) {
            addFailure("Expected not equal to: " + unexpected + " but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that two objects are not equal with custom message
     * 断言两个对象不相等（带自定义消息）
     *
     * @param unexpected the unexpected value | 不期望值
     * @param actual     the actual value | 实际值
     * @param message    the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotEqualTo(Object unexpected, Object actual, String message) {
        if (Objects.equals(unexpected, actual)) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that two objects are not equal with lazy message
     * 断言两个对象不相等（带延迟消息）
     *
     * @param unexpected      the unexpected value | 不期望值
     * @param actual          the actual value | 实际值
     * @param messageSupplier the message supplier | 消息提供者
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotEqualTo(Object unexpected, Object actual, Supplier<String> messageSupplier) {
        if (Objects.equals(unexpected, actual)) {
            addFailure(messageSupplier.get());
        }
        return this;
    }

    // ==================== Boolean Assertions | 布尔断言 ====================

    /**
     * Asserts that the condition is true
     * 断言条件为真
     *
     * @param condition the condition | 条件
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isTrue(boolean condition) {
        return isTrue(condition, "Expected true but was false");
    }

    /**
     * Asserts that the condition is true with custom message
     * 断言条件为真（带自定义消息）
     *
     * @param condition the condition | 条件
     * @param message   the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isTrue(boolean condition, String message) {
        if (!condition) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the condition is true with lazy message
     * 断言条件为真（带延迟消息）
     *
     * @param condition       the condition | 条件
     * @param messageSupplier the message supplier | 消息提供者
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isTrue(boolean condition, Supplier<String> messageSupplier) {
        if (!condition) {
            addFailure(messageSupplier.get());
        }
        return this;
    }

    /**
     * Asserts that the condition is false
     * 断言条件为假
     *
     * @param condition the condition | 条件
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isFalse(boolean condition) {
        return isFalse(condition, "Expected false but was true");
    }

    /**
     * Asserts that the condition is false with custom message
     * 断言条件为假（带自定义消息）
     *
     * @param condition the condition | 条件
     * @param message   the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isFalse(boolean condition, String message) {
        if (condition) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the condition is false with lazy message
     * 断言条件为假（带延迟消息）
     *
     * @param condition       the condition | 条件
     * @param messageSupplier the message supplier | 消息提供者
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isFalse(boolean condition, Supplier<String> messageSupplier) {
        if (condition) {
            addFailure(messageSupplier.get());
        }
        return this;
    }

    // ==================== String Assertions | 字符串断言 ====================

    /**
     * Asserts that the string is empty
     * 断言字符串为空
     *
     * @param actual the actual string | 实际字符串
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isEmpty(String actual) {
        if (actual == null || !actual.isEmpty()) {
            addFailure("Expected empty string but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that the string is empty with custom message
     * 断言字符串为空（带自定义消息）
     *
     * @param actual  the actual string | 实际字符串
     * @param message the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isEmpty(String actual, String message) {
        if (actual == null || !actual.isEmpty()) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the string is not empty
     * 断言字符串不为空
     *
     * @param actual the actual string | 实际字符串
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotEmpty(String actual) {
        return isNotEmpty(actual, "Expected non-empty string but was empty or null");
    }

    /**
     * Asserts that the string is not empty with custom message
     * 断言字符串不为空（带自定义消息）
     *
     * @param actual  the actual string | 实际字符串
     * @param message the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isNotEmpty(String actual, String message) {
        if (actual == null || actual.isEmpty()) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the string contains the substring
     * 断言字符串包含子串
     *
     * @param actual    the actual string | 实际字符串
     * @param substring the expected substring | 期望子串
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert contains(String actual, String substring) {
        if (actual == null || !actual.contains(substring)) {
            addFailure("Expected string to contain: " + substring + " but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that the string contains the substring with custom message
     * 断言字符串包含子串（带自定义消息）
     *
     * @param actual    the actual string | 实际字符串
     * @param substring the expected substring | 期望子串
     * @param message   the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert contains(String actual, String substring, String message) {
        if (actual == null || !actual.contains(substring)) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the string starts with the prefix
     * 断言字符串以前缀开始
     *
     * @param actual the actual string | 实际字符串
     * @param prefix the expected prefix | 期望前缀
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert startsWith(String actual, String prefix) {
        if (actual == null || !actual.startsWith(prefix)) {
            addFailure("Expected string to start with: " + prefix + " but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that the string starts with the prefix with custom message
     * 断言字符串以前缀开始（带自定义消息）
     *
     * @param actual  the actual string | 实际字符串
     * @param prefix  the expected prefix | 期望前缀
     * @param message the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert startsWith(String actual, String prefix, String message) {
        if (actual == null || !actual.startsWith(prefix)) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the string ends with the suffix
     * 断言字符串以后缀结束
     *
     * @param actual the actual string | 实际字符串
     * @param suffix the expected suffix | 期望后缀
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert endsWith(String actual, String suffix) {
        if (actual == null || !actual.endsWith(suffix)) {
            addFailure("Expected string to end with: " + suffix + " but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that the string ends with the suffix with custom message
     * 断言字符串以后缀结束（带自定义消息）
     *
     * @param actual  the actual string | 实际字符串
     * @param suffix  the expected suffix | 期望后缀
     * @param message the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert endsWith(String actual, String suffix, String message) {
        if (actual == null || !actual.endsWith(suffix)) {
            addFailure(message);
        }
        return this;
    }

    // ==================== Number Assertions | 数值断言 ====================

    /**
     * Asserts that the actual number is greater than the expected
     * 断言实际数值大于期望值
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isGreaterThan(Number actual, Number expected) {
        if (actual == null || expected == null || actual.doubleValue() <= expected.doubleValue()) {
            addFailure("Expected " + actual + " > " + expected);
        }
        return this;
    }

    /**
     * Asserts that the actual number is greater than the expected with custom message
     * 断言实际数值大于期望值（带自定义消息）
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @param message  the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isGreaterThan(Number actual, Number expected, String message) {
        if (actual == null || expected == null || actual.doubleValue() <= expected.doubleValue()) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the actual number is greater than or equal to the expected
     * 断言实际数值大于或等于期望值
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isGreaterThanOrEqualTo(Number actual, Number expected) {
        if (actual == null || expected == null || actual.doubleValue() < expected.doubleValue()) {
            addFailure("Expected " + actual + " >= " + expected);
        }
        return this;
    }

    /**
     * Asserts that the actual number is greater than or equal to the expected with custom message
     * 断言实际数值大于或等于期望值（带自定义消息）
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @param message  the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isGreaterThanOrEqualTo(Number actual, Number expected, String message) {
        if (actual == null || expected == null || actual.doubleValue() < expected.doubleValue()) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the actual number is less than the expected
     * 断言实际数值小于期望值
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isLessThan(Number actual, Number expected) {
        if (actual == null || expected == null || actual.doubleValue() >= expected.doubleValue()) {
            addFailure("Expected " + actual + " < " + expected);
        }
        return this;
    }

    /**
     * Asserts that the actual number is less than the expected with custom message
     * 断言实际数值小于期望值（带自定义消息）
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @param message  the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isLessThan(Number actual, Number expected, String message) {
        if (actual == null || expected == null || actual.doubleValue() >= expected.doubleValue()) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the actual number is less than or equal to the expected
     * 断言实际数值小于或等于期望值
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isLessThanOrEqualTo(Number actual, Number expected) {
        if (actual == null || expected == null || actual.doubleValue() > expected.doubleValue()) {
            addFailure("Expected " + actual + " <= " + expected);
        }
        return this;
    }

    /**
     * Asserts that the actual number is less than or equal to the expected with custom message
     * 断言实际数值小于或等于期望值（带自定义消息）
     *
     * @param actual   the actual number | 实际数值
     * @param expected the expected number | 期望数值
     * @param message  the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isLessThanOrEqualTo(Number actual, Number expected, String message) {
        if (actual == null || expected == null || actual.doubleValue() > expected.doubleValue()) {
            addFailure(message);
        }
        return this;
    }

    /**
     * Asserts that the actual number is between min and max (inclusive)
     * 断言实际数值在最小值和最大值之间（包含）
     *
     * @param actual the actual number | 实际数值
     * @param min    the minimum value | 最小值
     * @param max    the maximum value | 最大值
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isBetween(Number actual, Number min, Number max) {
        if (actual == null || min == null || max == null ||
                actual.doubleValue() < min.doubleValue() || actual.doubleValue() > max.doubleValue()) {
            addFailure("Expected " + actual + " between " + min + " and " + max);
        }
        return this;
    }

    /**
     * Asserts that the actual number is between min and max (inclusive) with custom message
     * 断言实际数值在最小值和最大值之间（包含）（带自定义消息）
     *
     * @param actual  the actual number | 实际数值
     * @param min     the minimum value | 最小值
     * @param max     the maximum value | 最大值
     * @param message the custom message | 自定义消息
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert isBetween(Number actual, Number min, Number max, String message) {
        if (actual == null || min == null || max == null ||
                actual.doubleValue() < min.doubleValue() || actual.doubleValue() > max.doubleValue()) {
            addFailure(message);
        }
        return this;
    }

    // ==================== Failure Management | 失败管理 ====================

    /**
     * Asserts all collected assertions, throwing if any failed
     * 断言所有收集的断言，如果有任何失败则抛出
     *
     * <p>Throws an AssertionError if any assertions have failed, with a message
     * containing all failure details.</p>
     * <p>如果有任何断言失败，则抛出AssertionError，消息中包含所有失败详情。</p>
     *
     * @throws AssertionError if any assertions have failed | 如果有任何断言失败
     */
    public void assertAll() {
        if (!failures.isEmpty()) {
            AssertionError multipleFailures = new AssertionError(buildFailureMessage());
            failures.forEach(multipleFailures::addSuppressed);
            throw multipleFailures;
        }
    }

    /**
     * Asserts all collected assertions with a custom header message
     * 断言所有收集的断言（带自定义头消息）
     *
     * @param heading the heading for the failure message | 失败消息的标题
     * @throws AssertionError if any assertions have failed | 如果有任何断言失败
     */
    public void assertAll(String heading) {
        if (!failures.isEmpty()) {
            AssertionError multipleFailures = new AssertionError(heading + "\n" + buildFailureMessage());
            failures.forEach(multipleFailures::addSuppressed);
            throw multipleFailures;
        }
    }

    /**
     * Checks if there are any collected failures
     * 检查是否有任何收集的失败
     *
     * @return true if there are failures | 如果有失败返回true
     */
    public boolean hasFailures() {
        return !failures.isEmpty();
    }

    /**
     * Gets the count of collected failures
     * 获取收集的失败数量
     *
     * @return the number of failures | 失败数量
     */
    public int getFailureCount() {
        return failures.size();
    }

    /**
     * Gets an unmodifiable list of collected failures
     * 获取收集的失败的不可修改列表
     *
     * @return unmodifiable list of assertion errors | 不可修改的断言错误列表
     */
    public List<AssertionError> getFailures() {
        return List.copyOf(failures);
    }

    /**
     * Resets the soft assert by clearing all collected failures
     * 通过清除所有收集的失败来重置软断言
     *
     * @return this instance for chaining | 返回实例用于链式调用
     */
    public SoftAssert reset() {
        failures.clear();
        return this;
    }

    // ==================== Internal Methods | 内部方法 ====================

    /**
     * Adds a failure to the collection
     * 向集合添加失败
     *
     * @param message the failure message | 失败消息
     */
    private void addFailure(String message) {
        failures.add(new AssertionError(message));
    }

    /**
     * Builds the failure message containing all collected failures
     * 构建包含所有收集的失败的失败消息
     *
     * @return the formatted failure message | 格式化的失败消息
     */
    private String buildFailureMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Multiple assertions failed (").append(failures.size()).append(" failures):\n");
        int index = 1;
        for (AssertionError failure : failures) {
            sb.append("\t").append(index++).append(") ").append(failure.getMessage()).append("\n");
        }
        return sb.toString();
    }

    /**
     * Returns a string representation of this SoftAssert
     * 返回此SoftAssert的字符串表示
     *
     * @return string representation | 字符串表示
     */
    @Override
    public String toString() {
        return "SoftAssert{failures=" + failures.size() + "}";
    }
}

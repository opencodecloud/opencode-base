package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Number Assert - Fluent assertions for numbers
 * 数值断言 - 数值的流式断言
 *
 * <p>Provides comprehensive assertion methods for numeric types.</p>
 * <p>为数值类型提供全面的断言方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Positivity/negativity/zero checks - 正/负/零检查</li>
 *   <li>Comparison assertions (greater, less, between) - 比较断言（大于、小于、范围内）</li>
 *   <li>Proximity assertions (closeTo) - 近似断言</li>
 *   <li>Parity checks (even, odd) - 奇偶检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * NumberAssert.assertThat(42)
 *     .isPositive()
 *     .isGreaterThan(0)
 *     .isLessThan(100)
 *     .isBetween(1, 50);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No (not designed for concurrent use) - 线程安全: 否（非设计用于并发使用）</li>
 *   <li>Null-safe: Yes (validates non-null number) - 空值安全: 是（验证非空数值）</li>
 * </ul>
 *
 * @param <T> the number type | 数值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class NumberAssert<T extends Number & Comparable<T>> {

    private final T actual;

    private NumberAssert(T actual) {
        this.actual = actual;
    }

    /**
     * Creates assertion for number.
     * 为数值创建断言。
     *
     * @param actual the actual number | 实际数值
     * @param <T>    the number type | 数值类型
     * @return the assertion | 断言
     */
    public static <T extends Number & Comparable<T>> NumberAssert<T> assertThat(T actual) {
        return new NumberAssert<>(actual);
    }

    /**
     * Creates assertion for int.
     * 为int创建断言。
     *
     * @param actual the actual int | 实际int
     * @return the assertion | 断言
     */
    public static NumberAssert<Integer> assertThat(int actual) {
        return new NumberAssert<>(actual);
    }

    /**
     * Creates assertion for long.
     * 为long创建断言。
     *
     * @param actual the actual long | 实际long
     * @return the assertion | 断言
     */
    public static NumberAssert<Long> assertThat(long actual) {
        return new NumberAssert<>(actual);
    }

    /**
     * Creates assertion for double.
     * 为double创建断言。
     *
     * @param actual the actual double | 实际double
     * @return the assertion | 断言
     */
    public static NumberAssert<Double> assertThat(double actual) {
        return new NumberAssert<>(actual);
    }

    /**
     * Asserts that number is null.
     * 断言数值为null。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isNull() {
        if (actual != null) {
            throw new AssertionException("Expected null but was: " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is not null.
     * 断言数值不为null。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isNotNull() {
        if (actual == null) {
            throw new AssertionException("Expected not null but was null");
        }
        return this;
    }

    /**
     * Asserts that number equals another.
     * 断言数值等于另一个。
     *
     * @param expected the expected number | 期望数值
     * @return this | 此对象
     */
    public NumberAssert<T> isEqualTo(T expected) {
        if (!Objects.equals(actual, expected)) {
            throw new AssertionException("Expected " + expected + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is zero.
     * 断言数值为零。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isZero() {
        isNotNull();
        if (actual.doubleValue() != 0.0) {
            throw new AssertionException("Expected zero but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is not zero.
     * 断言数值不为零。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isNotZero() {
        isNotNull();
        if (actual.doubleValue() == 0.0) {
            throw new AssertionException("Expected not zero but was zero");
        }
        return this;
    }

    /**
     * Asserts that number is positive.
     * 断言数值为正。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isPositive() {
        isNotNull();
        if (actual.doubleValue() <= 0) {
            throw new AssertionException("Expected positive but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is negative.
     * 断言数值为负。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isNegative() {
        isNotNull();
        if (actual.doubleValue() >= 0) {
            throw new AssertionException("Expected negative but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is not negative.
     * 断言数值非负。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isNotNegative() {
        isNotNull();
        if (actual.doubleValue() < 0) {
            throw new AssertionException("Expected not negative but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is not positive.
     * 断言数值非正。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isNotPositive() {
        isNotNull();
        if (actual.doubleValue() > 0) {
            throw new AssertionException("Expected not positive but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is greater than.
     * 断言数值大于。
     *
     * @param other the other number | 其他数值
     * @return this | 此对象
     */
    public NumberAssert<T> isGreaterThan(T other) {
        isNotNull();
        if (actual.compareTo(other) <= 0) {
            throw new AssertionException("Expected > " + other + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is greater than or equal to.
     * 断言数值大于等于。
     *
     * @param other the other number | 其他数值
     * @return this | 此对象
     */
    public NumberAssert<T> isGreaterThanOrEqualTo(T other) {
        isNotNull();
        if (actual.compareTo(other) < 0) {
            throw new AssertionException("Expected >= " + other + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is less than.
     * 断言数值小于。
     *
     * @param other the other number | 其他数值
     * @return this | 此对象
     */
    public NumberAssert<T> isLessThan(T other) {
        isNotNull();
        if (actual.compareTo(other) >= 0) {
            throw new AssertionException("Expected < " + other + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is less than or equal to.
     * 断言数值小于等于。
     *
     * @param other the other number | 其他数值
     * @return this | 此对象
     */
    public NumberAssert<T> isLessThanOrEqualTo(T other) {
        isNotNull();
        if (actual.compareTo(other) > 0) {
            throw new AssertionException("Expected <= " + other + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is between (inclusive).
     * 断言数值在范围内（包含边界）。
     *
     * @param start the start | 开始
     * @param end   the end | 结束
     * @return this | 此对象
     */
    public NumberAssert<T> isBetween(T start, T end) {
        isNotNull();
        if (actual.compareTo(start) < 0 || actual.compareTo(end) > 0) {
            throw new AssertionException("Expected between " + start + " and " + end + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is strictly between (exclusive).
     * 断言数值在范围内（不包含边界）。
     *
     * @param start the start | 开始
     * @param end   the end | 结束
     * @return this | 此对象
     */
    public NumberAssert<T> isStrictlyBetween(T start, T end) {
        isNotNull();
        if (actual.compareTo(start) <= 0 || actual.compareTo(end) >= 0) {
            throw new AssertionException("Expected strictly between " + start + " and " + end + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is close to with offset.
     * 断言数值接近（带偏移量）。
     *
     * @param expected the expected number | 期望数值
     * @param offset   the offset | 偏移量
     * @return this | 此对象
     */
    public NumberAssert<T> isCloseTo(T expected, T offset) {
        isNotNull();
        double diff = Math.abs(actual.doubleValue() - expected.doubleValue());
        if (diff > offset.doubleValue()) {
            throw new AssertionException("Expected close to " + expected + " within " + offset + " but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is even.
     * 断言数值为偶数。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isEven() {
        isNotNull();
        if (actual.longValue() % 2 != 0) {
            throw new AssertionException("Expected even but was " + actual);
        }
        return this;
    }

    /**
     * Asserts that number is odd.
     * 断言数值为奇数。
     *
     * @return this | 此对象
     */
    public NumberAssert<T> isOdd() {
        isNotNull();
        if (actual.longValue() % 2 == 0) {
            throw new AssertionException("Expected odd but was " + actual);
        }
        return this;
    }
}

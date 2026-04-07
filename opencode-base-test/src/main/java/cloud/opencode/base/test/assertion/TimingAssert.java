package cloud.opencode.base.test.assertion;

import cloud.opencode.base.test.exception.AssertionException;
import cloud.opencode.base.test.exception.TestErrorCode;
import cloud.opencode.base.test.exception.TestException;

import java.time.Duration;
import java.util.concurrent.Callable;

/**
 * Timing Assert - Performance timing assertions
 * 计时断言 - 性能计时断言
 *
 * <p>Provides assertion methods for verifying that operations complete within
 * specified time limits.</p>
 * <p>提供用于验证操作在指定时间限制内完成的断言方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Assert Runnable completes within duration - 断言 Runnable 在指定时间内完成</li>
 *   <li>Assert Callable completes within duration and return result - 断言 Callable 在指定时间内完成并返回结果</li>
 *   <li>Nanosecond precision timing - 纳秒精度计时</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Assert a Runnable completes within 100ms
 * TimingAssert.assertCompletesWithin(Duration.ofMillis(100), () -> {
 *     doSomeWork();
 * });
 *
 * // Assert a Callable completes within 200ms and get result
 * String result = TimingAssert.assertCompletesWithin(Duration.ofMillis(200), () -> {
 *     return computeValue();
 * });
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 线程安全: 是（无状态工具类）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.3
 */
public final class TimingAssert {

    private TimingAssert() {
        // utility class
    }

    /**
     * Asserts that the given runnable completes within the specified duration.
     * If the operation both exceeds the timeout and throws an exception, the
     * timeout violation is reported (since it is likely the root cause).
     * 断言给定的Runnable在指定时间内完成。
     * 如果操作既超时又抛出异常，将报告超时违规（因为它可能是根本原因）。
     *
     * @param timeout  the maximum allowed duration | 最大允许时间
     * @param runnable the operation to execute | 要执行的操作
     * @throws AssertionException if the operation exceeds the timeout | 如果操作超过超时时间
     * @throws TestException      if the operation throws an exception within the timeout | 如果操作在超时内抛出异常
     * @since V1.0.3
     */
    public static void assertCompletesWithin(Duration timeout, Runnable runnable) {
        if (timeout == null) {
            throw new AssertionException("timeout must not be null");
        }
        if (runnable == null) {
            throw new AssertionException("runnable must not be null");
        }

        long timeoutNanos = timeout.toNanos();
        long startNanos = System.nanoTime();
        Throwable caught = null;
        try {
            runnable.run();
        } catch (TestException e) {
            caught = e;
        } catch (Exception e) {
            caught = new TestException(TestErrorCode.UNEXPECTED_EXCEPTION, e);
        } finally {
            long elapsedNanos = System.nanoTime() - startNanos;
            if (elapsedNanos > timeoutNanos) {
                AssertionException te = timeoutException(timeoutNanos, elapsedNanos);
                if (caught != null) {
                    te.addSuppressed(caught);
                }
                throw te;
            }
        }
        if (caught instanceof RuntimeException re) {
            throw re;
        }
    }

    /**
     * Asserts that the given callable completes within the specified duration
     * and returns its result.
     * If the operation both exceeds the timeout and throws an exception, the
     * timeout violation is reported (since it is likely the root cause).
     * 断言给定的Callable在指定时间内完成并返回结果。
     * 如果操作既超时又抛出异常，将报告超时违规（因为它可能是根本原因）。
     *
     * @param <T>      the result type | 结果类型
     * @param timeout  the maximum allowed duration | 最大允许时间
     * @param callable the operation to execute | 要执行的操作
     * @return the result of the callable | Callable的结果
     * @throws AssertionException if the operation exceeds the timeout | 如果操作超过超时时间
     * @throws TestException      if the operation throws an exception within the timeout | 如果操作在超时内抛出异常
     * @since V1.0.3
     */
    public static <T> T assertCompletesWithin(Duration timeout, Callable<T> callable) {
        if (timeout == null) {
            throw new AssertionException("timeout must not be null");
        }
        if (callable == null) {
            throw new AssertionException("callable must not be null");
        }

        long timeoutNanos = timeout.toNanos();
        long startNanos = System.nanoTime();
        T result = null;
        Throwable caught = null;
        try {
            result = callable.call();
        } catch (TestException e) {
            caught = e;
        } catch (Exception e) {
            caught = new TestException(TestErrorCode.UNEXPECTED_EXCEPTION, e);
        } finally {
            long elapsedNanos = System.nanoTime() - startNanos;
            if (elapsedNanos > timeoutNanos) {
                AssertionException te = timeoutException(timeoutNanos, elapsedNanos);
                if (caught != null) {
                    te.addSuppressed(caught);
                }
                throw te;
            }
        }
        if (caught instanceof RuntimeException re) {
            throw re;
        }
        return result;
    }

    // ==================== Helper Methods | 辅助方法 ====================

    /**
     * Creates a timeout assertion exception with human-readable duration formatting.
     * Uses microseconds/nanoseconds for sub-millisecond values to avoid misleading "0ms" messages.
     * 创建超时断言异常，使用人类可读的持续时间格式。
     * 对于亚毫秒值使用微秒/纳秒，避免误导性的"0ms"消息。
     *
     * @param timeoutNanos the timeout in nanoseconds | 超时纳秒数
     * @param elapsedNanos the elapsed time in nanoseconds | 已用时间纳秒数
     * @return the timeout assertion exception | 超时断言异常
     */
    private static AssertionException timeoutException(long timeoutNanos, long elapsedNanos) {
        String timeoutStr = formatNanos(timeoutNanos);
        String elapsedStr = formatNanos(elapsedNanos);
        return new AssertionException(TestErrorCode.ASSERTION_TIMEOUT,
                "timeout: " + timeoutStr + ", actual: " + elapsedStr);
    }

    /**
     * Formats a nanosecond value to a human-readable string.
     * Uses the most appropriate unit (ns, us, ms) to avoid misleading truncation.
     * 将纳秒值格式化为人类可读的字符串。
     * 使用最合适的单位（ns, us, ms）以避免误导性截断。
     *
     * @param nanos the value in nanoseconds | 纳秒值
     * @return the formatted string | 格式化的字符串
     */
    private static String formatNanos(long nanos) {
        if (nanos < 1_000) {
            return nanos + "ns";
        } else if (nanos < 1_000_000) {
            return (nanos / 1_000) + "us";
        } else {
            return (nanos / 1_000_000) + "ms";
        }
    }
}

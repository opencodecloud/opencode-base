package cloud.opencode.base.test.wait;

import cloud.opencode.base.test.exception.TestErrorCode;
import cloud.opencode.base.test.exception.TestException;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Wait
 * 等待
 *
 * <p>Utility for waiting on conditions.</p>
 * <p>用于等待条件的工具。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Condition waiting utilities - 条件等待工具</li>
 *   <li>Timeout and polling support - 超时和轮询支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Wait.until(() -> condition, Duration.ofSeconds(10));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是（无状态）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public final class Wait {

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
    private static final Duration DEFAULT_POLL_INTERVAL = Duration.ofMillis(100);

    private Wait() {
        // Utility class
    }

    /**
     * Wait until condition is true
     * 等待直到条件为真
     *
     * @param condition the condition | 条件
     * @param timeout the timeout | 超时时间
     */
    public static void until(BooleanSupplier condition, Duration timeout) {
        until(condition, timeout, DEFAULT_POLL_INTERVAL);
    }

    /**
     * Wait until condition is true with poll interval
     * 等待直到条件为真，带轮询间隔
     *
     * @param condition the condition | 条件
     * @param timeout the timeout | 超时时间
     * @param pollInterval the poll interval | 轮询间隔
     */
    public static void until(BooleanSupplier condition, Duration timeout, Duration pollInterval) {
        long startNanos = System.nanoTime();
        long timeoutNanos = timeout.toNanos();

        while (System.nanoTime() - startNanos < timeoutNanos) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TestException(TestErrorCode.TIMEOUT, "Wait interrupted");
            }
        }

        throw new TestException(TestErrorCode.TIMEOUT, "Condition not met within " + timeout);
    }

    /**
     * Wait until condition is true with default timeout
     * 使用默认超时等待直到条件为真
     *
     * @param condition the condition | 条件
     */
    public static void until(BooleanSupplier condition) {
        until(condition, DEFAULT_TIMEOUT);
    }

    /**
     * Wait until value is not null
     * 等待直到值非空
     *
     * @param supplier the value supplier | 值供应者
     * @param timeout the timeout | 超时时间
     * @param <T> the value type | 值类型
     * @return the value | 值
     */
    public static <T> T untilNotNull(Supplier<T> supplier, Duration timeout) {
        return untilNotNull(supplier, timeout, DEFAULT_POLL_INTERVAL);
    }

    /**
     * Wait until value is not null with poll interval
     * 等待直到值非空，带轮询间隔
     *
     * @param supplier the value supplier | 值供应者
     * @param timeout the timeout | 超时时间
     * @param pollInterval the poll interval | 轮询间隔
     * @param <T> the value type | 值类型
     * @return the value | 值
     */
    public static <T> T untilNotNull(Supplier<T> supplier, Duration timeout, Duration pollInterval) {
        long startNanos = System.nanoTime();
        long timeoutNanos = timeout.toNanos();

        while (System.nanoTime() - startNanos < timeoutNanos) {
            T value = supplier.get();
            if (value != null) {
                return value;
            }
            try {
                Thread.sleep(pollInterval.toMillis());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TestException(TestErrorCode.TIMEOUT, "Wait interrupted");
            }
        }

        throw new TestException(TestErrorCode.TIMEOUT, "Value not available within " + timeout);
    }

    /**
     * Wait for duration
     * 等待指定时长
     *
     * @param duration the duration | 时长
     */
    public static void forDuration(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Wait for milliseconds
     * 等待指定毫秒
     *
     * @param millis the milliseconds | 毫秒
     */
    public static void forMillis(long millis) {
        forDuration(Duration.ofMillis(millis));
    }

    /**
     * Wait for seconds
     * 等待指定秒数
     *
     * @param seconds the seconds | 秒
     */
    public static void forSeconds(long seconds) {
        forDuration(Duration.ofSeconds(seconds));
    }
}

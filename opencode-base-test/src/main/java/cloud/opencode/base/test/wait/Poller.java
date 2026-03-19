package cloud.opencode.base.test.wait;

import cloud.opencode.base.test.exception.TestErrorCode;
import cloud.opencode.base.test.exception.TestException;

import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Poller
 * 轮询器
 *
 * <p>Fluent API for polling conditions.</p>
 * <p>用于轮询条件的流式API。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Polling-based condition waiting - 基于轮询的条件等待</li>
 *   <li>Configurable interval and timeout - 可配置的间隔和超时</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Poller.pollUntil(() -> service.isReady(),
 *     Duration.ofSeconds(30), Duration.ofMillis(500));
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
public final class Poller {

    private Duration timeout = Duration.ofSeconds(30);
    private Duration pollInterval = Duration.ofMillis(100);
    private String description = "condition";

    private Poller() {
        // Use static factory
    }

    /**
     * Create new poller
     * 创建新的轮询器
     *
     * @return the poller | 轮询器
     */
    public static Poller await() {
        return new Poller();
    }

    /**
     * Set timeout
     * 设置超时时间
     *
     * @param timeout the timeout | 超时时间
     * @return this poller | 此轮询器
     */
    public Poller timeout(Duration timeout) {
        this.timeout = timeout;
        return this;
    }

    /**
     * Set poll interval
     * 设置轮询间隔
     *
     * @param interval the interval | 间隔
     * @return this poller | 此轮询器
     */
    public Poller pollInterval(Duration interval) {
        this.pollInterval = interval;
        return this;
    }

    /**
     * Set description for error messages
     * 设置错误消息的描述
     *
     * @param description the description | 描述
     * @return this poller | 此轮询器
     */
    public Poller describedAs(String description) {
        this.description = description;
        return this;
    }

    /**
     * Wait until condition is true
     * 等待直到条件为真
     *
     * @param condition the condition | 条件
     */
    public void until(BooleanSupplier condition) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                if (condition.getAsBoolean()) {
                    return;
                }
            } catch (Exception e) {
                // Continue polling on exception
            }
            sleep();
        }

        throw new TestException(
            TestErrorCode.TIMEOUT,
            "Condition '" + description + "' not met within " + timeout
        );
    }

    /**
     * Wait until value satisfies predicate
     * 等待直到值满足谓词
     *
     * @param supplier the value supplier | 值供应者
     * @param predicate the predicate | 谓词
     * @param <T> the value type | 值类型
     * @return the value | 值
     */
    public <T> T until(Supplier<T> supplier, Predicate<T> predicate) {
        long startTime = System.currentTimeMillis();
        long timeoutMillis = timeout.toMillis();
        T lastValue = null;

        while (System.currentTimeMillis() - startTime < timeoutMillis) {
            try {
                lastValue = supplier.get();
                if (predicate.test(lastValue)) {
                    return lastValue;
                }
            } catch (Exception e) {
                // Continue polling on exception
            }
            sleep();
        }

        throw new TestException(
            TestErrorCode.TIMEOUT,
            "Condition '" + description + "' not met within " + timeout + ", last value: " + lastValue
        );
    }

    /**
     * Wait until value is not null
     * 等待直到值非空
     *
     * @param supplier the value supplier | 值供应者
     * @param <T> the value type | 值类型
     * @return the value | 值
     */
    public <T> T untilNotNull(Supplier<T> supplier) {
        return until(supplier, value -> value != null);
    }

    /**
     * Wait until value equals expected
     * 等待直到值等于期望值
     *
     * @param supplier the value supplier | 值供应者
     * @param expected the expected value | 期望值
     * @param <T> the value type | 值类型
     * @return the value | 值
     */
    public <T> T untilEquals(Supplier<T> supplier, T expected) {
        return until(supplier, value -> expected.equals(value));
    }

    private void sleep() {
        try {
            Thread.sleep(pollInterval.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TestException(TestErrorCode.TIMEOUT, "Polling interrupted");
        }
    }
}

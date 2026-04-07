package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.Objects;

/**
 * CheckedRunnable - Runnable that may throw checked exceptions
 * CheckedRunnable - 可能抛出受检异常的可运行接口
 *
 * <p>Extends the concept of JDK {@link Runnable} to allow throwing checked exceptions.
 * Provides safe conversion to standard Runnable via {@link #unchecked()} and
 * silent execution via {@link #runQuietly()}.</p>
 * <p>扩展 JDK {@link Runnable} 的概念，支持抛出受检异常。
 * 通过 {@link #unchecked()} 安全转换为标准 Runnable，
 * 通过 {@link #runQuietly()} 静默执行。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Runnable (unchecked) - 转换为标准 Runnable</li>
 *   <li>Silent execution - 静默执行</li>
 *   <li>Chaining support (andThen) - 支持链式调用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedRunnable closeResources = () -> connection.close();
 *
 * Runnable wrapped = closeResources.unchecked();
 * closeResources.runQuietly();
 *
 * // Chaining
 * closeResources.andThen(() -> log.info("Resources closed"))
 *     .run();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.3
 */
@FunctionalInterface
public interface CheckedRunnable {

    /**
     * Run the operation, may throw checked exception
     * 运行操作，可能抛出受检异常
     *
     * @throws Exception if operation fails - 如果操作失败
     */
    void run() throws Exception;

    /**
     * Convert to standard Runnable, wrapping checked exceptions in OpenFunctionalException
     * 转换为标准 Runnable，将受检异常包装为 OpenFunctionalException
     *
     * <p>Runtime exceptions are rethrown as-is; checked exceptions are wrapped
     * in {@link OpenFunctionalException}.</p>
     * <p>运行时异常原样抛出；受检异常被包装为 {@link OpenFunctionalException}。</p>
     *
     * @return Runnable that wraps checked exceptions - 包装受检异常的 Runnable
     */
    default Runnable unchecked() {
        return () -> {
            try {
                run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenFunctionalException("Checked runnable failed", e);
            }
        };
    }

    /**
     * Run silently, ignoring any checked or unchecked exception (but not {@link Error})
     * 静默运行，忽略任何受检或非受检异常（不包括 {@link Error}）
     */
    default void runQuietly() {
        try {
            run();
        } catch (Exception ignored) {
            // Silently ignore
        }
    }

    /**
     * Chain with another runnable (execute this, then after)
     * 与另一个可运行接口链接（先执行本操作，再执行 after）
     *
     * @param after runnable to execute after this - 在本操作之后执行的可运行接口
     * @return chained runnable - 链式可运行接口
     * @throws NullPointerException if after is null - 如果 after 为 null
     */
    default CheckedRunnable andThen(CheckedRunnable after) {
        Objects.requireNonNull(after, "after must not be null");
        return () -> {
            run();
            after.run();
        };
    }

    /**
     * Wrap a standard Runnable as CheckedRunnable
     * 将标准 Runnable 包装为 CheckedRunnable
     *
     * @param runnable standard Runnable - 标准 Runnable
     * @return CheckedRunnable wrapper - CheckedRunnable 包装器
     */
    static CheckedRunnable of(Runnable runnable) {
        java.util.Objects.requireNonNull(runnable, "runnable must not be null");
        return runnable::run;
    }
}

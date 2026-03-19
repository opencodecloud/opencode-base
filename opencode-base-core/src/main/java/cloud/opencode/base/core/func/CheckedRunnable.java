package cloud.opencode.base.core.func;

import cloud.opencode.base.core.exception.OpenException;

/**
 * Checked Runnable - Runnable that can throw checked exceptions
 * 可抛出受检异常的 Runnable - 扩展 JDK Runnable 支持受检异常
 *
 * <p>Extends JDK {@link Runnable} to allow throwing checked exceptions in lambdas.</p>
 * <p>扩展 JDK {@link Runnable}，支持在 lambda 中抛出受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Runnable (unchecked) - 转换为标准 Runnable</li>
 *   <li>Silent execution (runQuietly) - 静默执行</li>
 *   <li>Composition with andThen - 组合操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedRunnable task = () -> Files.delete(tempFile);
 * Runnable wrapped = task.unchecked();
 * task.runQuietly();  // ignores exceptions
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface CheckedRunnable {

    /**
     * Executes an operation that may throw a checked exception
     * 执行操作，可能抛出受检异常
     *
     * @throws Exception if the condition is not met | 如果操作失败
     */
    void run() throws Exception;

    /**
     * Converts
     * 转换为标准 Runnable，受检异常包装为 RuntimeException
     *
     * @return Runnable
     */
    default Runnable unchecked() {
        return () -> {
            try {
                run();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenException("Checked runnable failed", e);
            }
        };
    }

    /**
     * Silently executes, ignoring exceptions
     * 静默执行，忽略异常
     */
    default void runQuietly() {
        try {
            run();
        } catch (Exception ignored) {
        }
    }

    /**
     * Composes two CheckedRunnables
     * 组合两个 CheckedRunnable
     *
     * @param after the value | 后续操作
     * @return the result | 组合后的 CheckedRunnable
     */
    default CheckedRunnable andThen(CheckedRunnable after) {
        return () -> {
            run();
            after.run();
        };
    }

    /**
     * Wraps a standard Runnable as a CheckedRunnable
     * 将普通 Runnable 包装为 CheckedRunnable
     *
     * @param runnable the value | 普通 Runnable
     * @return CheckedRunnable
     */
    static CheckedRunnable of(Runnable runnable) {
        return runnable::run;
    }
}

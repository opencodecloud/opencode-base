package cloud.opencode.base.core.func;

import java.util.concurrent.Callable;

/**
 * Checked Callable - Enhanced Callable with convenience methods
 * 可抛出受检异常的 Callable - 增强 JDK Callable 提供便捷方法
 *
 * <p>Extends JDK {@link Callable} with additional convenience methods.</p>
 * <p>扩展 JDK {@link Callable}，提供额外的便捷方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Convert to JDK Callable (toCallable) - 转换为 JDK Callable</li>
 *   <li>Silent execution (callQuietly/callOrDefault) - 静默执行</li>
 *   <li>Interop with CheckedSupplier - 与 CheckedSupplier 互操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedCallable<String> task = () -> Files.readString(path);
 * Future<String> future = executor.submit(task.toCallable());
 * String result = task.callOrDefault("fallback");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <V> return type - 返回值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface CheckedCallable<V> {

    /**
     * Executes and returns a result, may throw a checked exception
     * 执行并返回结果，可能抛出受检异常
     *
     * @return the result | 结果
     * @throws Exception if the condition is not met | 如果操作失败
     */
    V call() throws Exception;

    /**
     * Converts
     * 转换为 JDK Callable
     *
     * @return Callable
     */
    default Callable<V> toCallable() {
        return this::call;
    }

    /**
     * Silently calls, returning null on exception
     * 静默调用，异常时返回 null
     *
     * @return the result | 结果或 null
     */
    default V callQuietly() {
        try {
            return call();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Silently calls, returning the default value on exception
     * 静默调用，异常时返回默认值
     *
     * @param defaultValue the default value | 默认值
     * @return the result | 结果或默认值
     */
    default V callOrDefault(V defaultValue) {
        try {
            return call();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Wraps a JDK Callable as a CheckedCallable
     * 将 JDK Callable 包装为 CheckedCallable
     *
     * @param callable JDK Callable
     * @param <V> the value | 返回值类型
     * @return CheckedCallable
     */
    static <V> CheckedCallable<V> of(Callable<V> callable) {
        return callable::call;
    }

    /**
     * Converts a CheckedSupplier to a CheckedCallable
     * 将 CheckedSupplier 转换为 CheckedCallable
     *
     * @param supplier CheckedSupplier
     * @param <V> the value | 返回值类型
     * @return CheckedCallable
     */
    static <V> CheckedCallable<V> from(CheckedSupplier<V> supplier) {
        return supplier::get;
    }
}

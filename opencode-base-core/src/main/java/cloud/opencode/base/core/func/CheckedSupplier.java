package cloud.opencode.base.core.func;

import cloud.opencode.base.core.exception.OpenException;

import java.util.function.Supplier;

/**
 * Checked Supplier - Supplier that can throw checked exceptions
 * 可抛出受检异常的 Supplier - 扩展 JDK Supplier 支持受检异常
 *
 * <p>Extends JDK {@link Supplier} to allow throwing checked exceptions in lambdas.</p>
 * <p>扩展 JDK {@link Supplier}，支持在 lambda 中抛出受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Supplier (unchecked) - 转换为标准 Supplier</li>
 *   <li>Silent execution (getQuietly/getOrDefault) - 静默执行</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedSupplier<String> supplier = () -> Files.readString(path);
 * Supplier<String> wrapped = supplier.unchecked();
 * String result = supplier.getOrDefault("fallback");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T> return type - 返回值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface CheckedSupplier<T> {

    /**
     * Gets
     * 获取结果，可能抛出受检异常
     *
     * @return the result | 结果
     * @throws Exception if the condition is not met | 如果操作失败
     */
    T get() throws Exception;

    /**
     * Converts
     * 转换为标准 Supplier，受检异常包装为 RuntimeException
     *
     * @return Supplier
     */
    default Supplier<T> unchecked() {
        return () -> {
            try {
                return get();
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenException("Checked supplier failed", e);
            }
        };
    }

    /**
     * Silently executes, returning null on exception
     * 静默执行，异常时返回 null
     *
     * @return the result | 结果或 null
     */
    default T getQuietly() {
        try {
            return get();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Silently executes, returning the default value on exception
     * 静默执行，异常时返回默认值
     *
     * @param defaultValue the default value | 默认值
     * @return the result | 结果或默认值
     */
    default T getOrDefault(T defaultValue) {
        try {
            return get();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Wraps a standard Supplier as a CheckedSupplier
     * 将普通 Supplier 包装为 CheckedSupplier
     *
     * @param supplier the value | 普通 Supplier
     * @param <T> the value | 返回值类型
     * @return CheckedSupplier
     */
    static <T> CheckedSupplier<T> of(Supplier<T> supplier) {
        return supplier::get;
    }
}

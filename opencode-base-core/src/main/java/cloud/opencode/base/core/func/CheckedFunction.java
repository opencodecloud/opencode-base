package cloud.opencode.base.core.func;

import cloud.opencode.base.core.exception.OpenException;

import java.util.function.Function;

/**
 * Checked Function - Function that can throw checked exceptions
 * 可抛出受检异常的 Function - 扩展 JDK Function 支持受检异常
 *
 * <p>Extends JDK {@link Function} to allow throwing checked exceptions in lambdas.</p>
 * <p>扩展 JDK {@link Function}，支持在 lambda 中抛出受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Function (unchecked) - 转换为标准 Function</li>
 *   <li>Silent execution (applyQuietly/applyOrDefault) - 静默执行</li>
 *   <li>Composition (andThen/compose) - 函数组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedFunction<Path, String> reader = path -> Files.readString(path);
 * paths.stream().map(reader.unchecked()).toList();
 * String result = reader.applyOrDefault(path, "fallback");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T> input type - 输入类型
 * @param <R> return type - 返回值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Applies the function, may throw a checked exception
     * 应用函数，可能抛出受检异常
     *
     * @param t the value | 输入值
     * @return the result | 结果
     * @throws Exception if the condition is not met | 如果操作失败
     */
    R apply(T t) throws Exception;

    /**
     * Converts
     * 转换为标准 Function，受检异常包装为 RuntimeException
     *
     * @return Function
     */
    default Function<T, R> unchecked() {
        return t -> {
            try {
                return apply(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenException("Checked function failed", e);
            }
        };
    }

    /**
     * Silently applies, returning null on exception
     * 静默应用，异常时返回 null
     *
     * @param t the value | 输入值
     * @return the result | 结果或 null
     */
    default R applyQuietly(T t) {
        try {
            return apply(t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Silently applies, returning the default value on exception
     * 静默应用，异常时返回默认值
     *
     * @param t the value | 输入值
     * @param defaultValue the default value | 默认值
     * @return the result | 结果或默认值
     */
    default R applyOrDefault(T t, R defaultValue) {
        try {
            return apply(t);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Composes functions (executes this function first, then after)
     * 组合函数（先执行本函数，再执行 after）
     *
     * @param after the value | 后续函数
     * @param <V> the value | 最终返回类型
     * @return the result | 组合后的函数
     */
    default <V> CheckedFunction<T, V> andThen(CheckedFunction<? super R, ? extends V> after) {
        return t -> after.apply(apply(t));
    }

    /**
     * Composes functions (executes before first, then this function)
     * 组合函数（先执行 before，再执行本函数）
     *
     * @param before the value | 前置函数
     * @param <V> the value | 输入类型
     * @return the result | 组合后的函数
     */
    default <V> CheckedFunction<V, R> compose(CheckedFunction<? super V, ? extends T> before) {
        return v -> apply(before.apply(v));
    }

    /**
     * Wraps a standard Function as a CheckedFunction
     * 将普通 Function 包装为 CheckedFunction
     *
     * @param function the value | 普通 Function
     * @param <T> the value | 输入类型
     * @param <R> the value | 返回值类型
     * @return CheckedFunction
     */
    static <T, R> CheckedFunction<T, R> of(Function<T, R> function) {
        return function::apply;
    }

    /**
     * Creates
     * 创建恒等函数
     *
     * @param <T> the type | 类型
     * @return the result | 恒等函数
     */
    static <T> CheckedFunction<T, T> identity() {
        return t -> t;
    }
}

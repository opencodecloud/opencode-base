package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.function.Function;

/**
 * CheckedFunction - Function that can throw checked exceptions
 * CheckedFunction - 可抛出受检异常的函数
 *
 * <p>Extends the concept of JDK {@link Function} to allow throwing checked exceptions.
 * This is a local interface for the functional module that mirrors Core's CheckedFunction.</p>
 * <p>扩展 JDK {@link Function} 的概念，支持抛出受检异常。
 * 这是函数式模块的本地接口，与 Core 的 CheckedFunction 对应。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard Function (unchecked) - 转换为标准 Function</li>
 *   <li>Silent execution with defaults - 静默执行并返回默认值</li>
 *   <li>Composition support (andThen/compose) - 支持函数组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedFunction<Path, String> readFile = path -> Files.readString(path);
 *
 * Function<Path, String> wrapped = readFile.unchecked();
 * String content = readFile.applyOrDefault(path, "fallback");
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
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@FunctionalInterface
public interface CheckedFunction<T, R> {

    /**
     * Apply function to argument, may throw checked exception
     * 应用函数到参数，可能抛出受检异常
     *
     * @param t input - 输入
     * @return result - 结果
     * @throws Exception if operation fails - 如果操作失败
     */
    R apply(T t) throws Exception;

    /**
     * Convert to standard Function, wrapping checked exceptions
     * 转换为标准 Function，包装受检异常
     *
     * @return Function that wraps checked exceptions - 包装受检异常的 Function
     */
    default Function<T, R> unchecked() {
        return t -> {
            try {
                return apply(t);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenFunctionalException("Checked function failed", e);
            }
        };
    }

    /**
     * Apply silently, returning null on exception
     * 静默应用，异常时返回 null
     *
     * @param t input - 输入
     * @return result or null - 结果或 null
     */
    default R applyQuietly(T t) {
        try {
            return apply(t);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Apply silently, returning default value on exception
     * 静默应用，异常时返回默认值
     *
     * @param t            input - 输入
     * @param defaultValue default value on error - 错误时的默认值
     * @return result or default value - 结果或默认值
     */
    default R applyOrDefault(T t, R defaultValue) {
        try {
            return apply(t);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Compose with another function (execute this, then after)
     * 与另一个函数组合（先执行本函数，再执行 after）
     *
     * @param after function to apply after this - 在本函数之后应用的函数
     * @param <V>   result type of after function - after 函数的结果类型
     * @return composed function - 组合后的函数
     */
    default <V> CheckedFunction<T, V> andThen(CheckedFunction<? super R, ? extends V> after) {
        return t -> after.apply(apply(t));
    }

    /**
     * Compose with another function (execute before, then this)
     * 与另一个函数组合（先执行 before，再执行本函数）
     *
     * @param before function to apply before this - 在本函数之前应用的函数
     * @param <V>    input type of before function - before 函数的输入类型
     * @return composed function - 组合后的函数
     */
    default <V> CheckedFunction<V, R> compose(CheckedFunction<? super V, ? extends T> before) {
        return v -> apply(before.apply(v));
    }

    /**
     * Create identity function
     * 创建恒等函数
     *
     * @param <T> type - 类型
     * @return identity function - 恒等函数
     */
    static <T> CheckedFunction<T, T> identity() {
        return t -> t;
    }

    /**
     * Wrap a standard Function as CheckedFunction
     * 将标准 Function 包装为 CheckedFunction
     *
     * @param function standard Function - 标准 Function
     * @param <T>      input type - 输入类型
     * @param <R>      return type - 返回值类型
     * @return CheckedFunction wrapper - CheckedFunction 包装器
     */
    static <T, R> CheckedFunction<T, R> of(Function<T, R> function) {
        return function::apply;
    }
}

package cloud.opencode.base.functional.function;

import cloud.opencode.base.functional.exception.OpenFunctionalException;

import java.util.function.BiFunction;

/**
 * CheckedBiFunction - BiFunction that can throw checked exceptions
 * CheckedBiFunction - 可抛出受检异常的双参函数
 *
 * <p>Extends the concept of JDK {@link BiFunction} to allow throwing checked exceptions.</p>
 * <p>扩展 JDK {@link BiFunction} 的概念，支持抛出受检异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Throw checked exceptions in lambda - 在 lambda 中抛出受检异常</li>
 *   <li>Convert to standard BiFunction (unchecked) - 转换为标准 BiFunction</li>
 *   <li>Silent execution with defaults - 静默执行并返回默认值</li>
 *   <li>Composition support - 支持函数组合</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CheckedBiFunction<Path, Charset, String> readFile =
 *     (path, charset) -> Files.readString(path, charset);
 *
 * BiFunction<Path, Charset, String> wrapped = readFile.unchecked();
 * String content = readFile.applyOrDefault(path, charset, "fallback");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T> first input type - 第一个输入类型
 * @param <U> second input type - 第二个输入类型
 * @param <R> return type - 返回值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@FunctionalInterface
public interface CheckedBiFunction<T, U, R> {

    /**
     * Apply function to arguments, may throw checked exception
     * 应用函数到参数，可能抛出受检异常
     *
     * @param t first input - 第一个输入
     * @param u second input - 第二个输入
     * @return result - 结果
     * @throws Exception if operation fails - 如果操作失败
     */
    R apply(T t, U u) throws Exception;

    /**
     * Convert to standard BiFunction, wrapping checked exceptions
     * 转换为标准 BiFunction，包装受检异常
     *
     * @return BiFunction that wraps checked exceptions - 包装受检异常的 BiFunction
     */
    default BiFunction<T, U, R> unchecked() {
        return (t, u) -> {
            try {
                return apply(t, u);
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new OpenFunctionalException("Checked bi-function failed", e);
            }
        };
    }

    /**
     * Apply silently, returning null on exception
     * 静默应用，异常时返回 null
     *
     * @param t first input - 第一个输入
     * @param u second input - 第二个输入
     * @return result or null - 结果或 null
     */
    default R applyQuietly(T t, U u) {
        try {
            return apply(t, u);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Apply silently, returning default value on exception
     * 静默应用，异常时返回默认值
     *
     * @param t            first input - 第一个输入
     * @param u            second input - 第二个输入
     * @param defaultValue default value on error - 错误时的默认值
     * @return result or default value - 结果或默认值
     */
    default R applyOrDefault(T t, U u, R defaultValue) {
        try {
            return apply(t, u);
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
    default <V> CheckedBiFunction<T, U, V> andThen(
            CheckedFunction<? super R, ? extends V> after) {
        return (t, u) -> after.apply(apply(t, u));
    }

    /**
     * Wrap a standard BiFunction as CheckedBiFunction
     * 将标准 BiFunction 包装为 CheckedBiFunction
     *
     * @param function standard BiFunction - 标准 BiFunction
     * @param <T>      first input type - 第一个输入类型
     * @param <U>      second input type - 第二个输入类型
     * @param <R>      return type - 返回值类型
     * @return CheckedBiFunction wrapper - CheckedBiFunction 包装器
     */
    static <T, U, R> CheckedBiFunction<T, U, R> of(BiFunction<T, U, R> function) {
        return function::apply;
    }
}

package cloud.opencode.base.reflect.lambda;

import java.io.Serializable;
import java.util.function.BiFunction;

/**
 * Serializable BiFunction Interface
 * 可序列化BiFunction接口
 *
 * <p>A BiFunction that is also Serializable, enabling lambda metadata extraction.</p>
 * <p>一个同时也是Serializable的BiFunction，可以提取lambda元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serializable BiFunction for lambda metadata extraction - 可序列化BiFunction用于lambda元数据提取</li>
 *   <li>Composable with andThen - 可通过andThen组合</li>
 *   <li>Factory methods for common patterns - 常用模式的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializableBiFunction<String, Integer, String> func = (s, i) -> s.repeat(i);
 * String result = func.apply("ab", 3); // "ababab"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the first input type | 第一个输入类型
 * @param <U> the second input type | 第二个输入类型
 * @param <R> the output type | 输出类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@FunctionalInterface
public interface SerializableBiFunction<T, U, R> extends BiFunction<T, U, R>, Serializable {

    /**
     * Creates a SerializableBiFunction
     * 创建SerializableBiFunction
     *
     * @param function the function | 函数
     * @param <T>      the first input type | 第一个输入类型
     * @param <U>      the second input type | 第二个输入类型
     * @param <R>      the output type | 输出类型
     * @return the serializable function | 可序列化函数
     */
    static <T, U, R> SerializableBiFunction<T, U, R> of(SerializableBiFunction<T, U, R> function) {
        return function;
    }

    /**
     * Creates a function that always returns a constant
     * 创建总是返回常量的函数
     *
     * @param value the value | 值
     * @param <T>   the first input type | 第一个输入类型
     * @param <U>   the second input type | 第二个输入类型
     * @param <R>   the output type | 输出类型
     * @return the constant function | 常量函数
     */
    static <T, U, R> SerializableBiFunction<T, U, R> constant(R value) {
        return (t, u) -> value;
    }

    /**
     * Creates a function that returns the first argument
     * 创建返回第一个参数的函数
     *
     * @param <T> the first input type | 第一个输入类型
     * @param <U> the second input type | 第二个输入类型
     * @return the first-returning function | 返回第一个参数的函数
     */
    static <T, U> SerializableBiFunction<T, U, T> first() {
        return (t, u) -> t;
    }

    /**
     * Creates a function that returns the second argument
     * 创建返回第二个参数的函数
     *
     * @param <T> the first input type | 第一个输入类型
     * @param <U> the second input type | 第二个输入类型
     * @return the second-returning function | 返回第二个参数的函数
     */
    static <T, U> SerializableBiFunction<T, U, U> second() {
        return (t, u) -> u;
    }

    /**
     * Composes this function with another (after)
     * 将此函数与另一个组合（之后）
     *
     * @param after the function to apply after | 之后应用的函数
     * @param <V>   the output type of after | after的输出类型
     * @return the composed function | 组合后的函数
     */
    default <V> SerializableBiFunction<T, U, V> andThen(SerializableFunction<? super R, ? extends V> after) {
        return (T t, U u) -> after.apply(apply(t, u));
    }
}

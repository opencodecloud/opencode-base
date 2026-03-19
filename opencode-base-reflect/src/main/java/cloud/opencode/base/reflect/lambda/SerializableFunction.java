package cloud.opencode.base.reflect.lambda;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Serializable Function Interface
 * 可序列化Function接口
 *
 * <p>A Function that is also Serializable, enabling lambda metadata extraction.</p>
 * <p>一个同时也是Serializable的Function，可以提取lambda元数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Serializable Function for lambda metadata extraction - 可序列化Function用于lambda元数据提取</li>
 *   <li>Composable with compose and andThen - 可通过compose和andThen组合</li>
 *   <li>Identity factory method - 恒等函数工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * SerializableFunction<User, String> getName = User::getName;
 * String name = getName.apply(user);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Depends on implementation - 线程安全: 取决于实现</li>
 *   <li>Null-safe: Depends on implementation - 空值安全: 取决于实现</li>
 * </ul>
 *
 * @param <T> the input type | 输入类型
 * @param <R> the output type | 输出类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-reflect V1.0.0
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {

    /**
     * Creates a SerializableFunction from a regular Function
     * 从普通Function创建SerializableFunction
     *
     * @param function the function | 函数
     * @param <T>      the input type | 输入类型
     * @param <R>      the output type | 输出类型
     * @return the serializable function | 可序列化函数
     */
    static <T, R> SerializableFunction<T, R> of(SerializableFunction<T, R> function) {
        return function;
    }

    /**
     * Creates an identity function
     * 创建恒等函数
     *
     * @param <T> the type | 类型
     * @return the identity function | 恒等函数
     */
    static <T> SerializableFunction<T, T> identity() {
        return t -> t;
    }

    /**
     * Composes this function with another (before)
     * 将此函数与另一个组合（之前）
     *
     * @param before the function to apply before | 之前应用的函数
     * @param <V>    the input type of before | before的输入类型
     * @return the composed function | 组合后的函数
     */
    default <V> SerializableFunction<V, R> compose(SerializableFunction<? super V, ? extends T> before) {
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Composes this function with another (after)
     * 将此函数与另一个组合（之后）
     *
     * @param after the function to apply after | 之后应用的函数
     * @param <V>   the output type of after | after的输出类型
     * @return the composed function | 组合后的函数
     */
    default <V> SerializableFunction<T, V> andThen(SerializableFunction<? super R, ? extends V> after) {
        return (T t) -> after.apply(apply(t));
    }
}

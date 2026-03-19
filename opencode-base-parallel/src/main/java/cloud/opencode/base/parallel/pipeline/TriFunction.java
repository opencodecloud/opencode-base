package cloud.opencode.base.parallel.pipeline;

import java.util.Objects;
import java.util.function.Function;

/**
 * Tri Function - Three-Argument Function Interface
 * 三元函数 - 三参数函数接口
 *
 * <p>Represents a function that accepts three arguments and produces a result.
 * This is the three-arity specialization of {@link Function}.</p>
 * <p>表示接受三个参数并产生结果的函数。这是 {@link Function} 的三元特化版本。</p>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * TriFunction<String, Integer, Boolean, String> fn =
 *     (name, age, active) -> name + ":" + age + ":" + active;
 * String result = fn.apply("John", 25, true);
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Three-argument function interface - 三参数函数接口</li>
 *   <li>andThen composition support - andThen组合支持</li>
 *   <li>Functional interface (lambda compatible) - 函数式接口（兼容lambda）</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 * </ul>
 * @param <T1> the type of the first argument - 第一个参数的类型
 * @param <T2> the type of the second argument - 第二个参数的类型
 * @param <T3> the type of the third argument - 第三个参数的类型
 * @param <R>  the type of the result - 结果的类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
@FunctionalInterface
public interface TriFunction<T1, T2, T3, R> {

    /**
     * Applies this function to the given arguments.
     * 将此函数应用于给定的参数。
     *
     * @param t1 the first function argument - 第一个函数参数
     * @param t2 the second function argument - 第二个函数参数
     * @param t3 the third function argument - 第三个函数参数
     * @return the function result - 函数结果
     */
    R apply(T1 t1, T2 t2, T3 t3);

    /**
     * Returns a composed function that first applies this function to its input,
     * and then applies the {@code after} function to the result.
     * 返回一个组合函数，首先将此函数应用于其输入，然后将 after 函数应用于结果。
     *
     * @param <V>   the type of output of the {@code after} function - after 函数输出的类型
     * @param after the function to apply after this function - 在此函数之后应用的函数
     * @return a composed function - 组合函数
     * @throws NullPointerException if after is null - 如果 after 为 null
     */
    default <V> TriFunction<T1, T2, T3, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (t1, t2, t3) -> after.apply(apply(t1, t2, t3));
    }
}

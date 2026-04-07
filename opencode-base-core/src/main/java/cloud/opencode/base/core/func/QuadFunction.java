package cloud.opencode.base.core.func;

/**
 * Four-argument function interface
 * 四元函数接口
 *
 * <p>Represents a function that accepts four arguments and produces a result.</p>
 * <p>表示接受四个参数并产生结果的函数。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * QuadFunction<String, String, Integer, Boolean, String> format =
 *     (first, last, age, active) -> first + " " + last + "(" + age + ", " + active + ")";
 * String result = format.apply("Alice", "Smith", 30, true);
 * }</pre>
 *
 * @param <A> first argument type - 第一个参数类型
 * @param <B> second argument type - 第二个参数类型
 * @param <C> third argument type - 第三个参数类型
 * @param <D> fourth argument type - 第四个参数类型
 * @param <R> return type - 返回值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see TriFunction
 * @see java.util.function.BiFunction
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface QuadFunction<A, B, C, D, R> {

    /**
     * Applies this function to the given arguments.
     * 将此函数应用于给定的参数。
     *
     * @param a the first argument - 第一个参数
     * @param b the second argument - 第二个参数
     * @param c the third argument - 第三个参数
     * @param d the fourth argument - 第四个参数
     * @return the function result - 函数结果
     */
    R apply(A a, B b, C c, D d);
}

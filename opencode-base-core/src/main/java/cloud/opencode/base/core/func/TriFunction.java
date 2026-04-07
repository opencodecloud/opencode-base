package cloud.opencode.base.core.func;

/**
 * Three-argument function interface
 * 三元函数接口
 *
 * <p>Represents a function that accepts three arguments and produces a result.</p>
 * <p>表示接受三个参数并产生结果的函数。</p>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TriFunction<String, Integer, Boolean, String> format =
 *     (name, age, active) -> name + "(" + age + ", " + active + ")";
 * String result = format.apply("Alice", 30, true);
 * }</pre>
 *
 * @param <A> first argument type - 第一个参数类型
 * @param <B> second argument type - 第二个参数类型
 * @param <C> third argument type - 第三个参数类型
 * @param <R> return type - 返回值类型
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see java.util.function.BiFunction
 * @since JDK 25, opencode-base-core V1.0.0
 */
@FunctionalInterface
public interface TriFunction<A, B, C, R> {

    /**
     * Applies this function to the given arguments.
     * 将此函数应用于给定的参数。
     *
     * @param a the first argument - 第一个参数
     * @param b the second argument - 第二个参数
     * @param c the third argument - 第三个参数
     * @return the function result - 函数结果
     */
    R apply(A a, B b, C c);
}

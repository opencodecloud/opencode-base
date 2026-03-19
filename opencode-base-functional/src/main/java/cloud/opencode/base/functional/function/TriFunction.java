package cloud.opencode.base.functional.function;

import java.util.function.Function;

/**
 * TriFunction - Three-argument function
 * TriFunction - 三参函数
 *
 * <p>Represents a function that accepts three arguments and produces a result.</p>
 * <p>表示接受三个参数并产生结果的函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Three input parameters - 三个输入参数</li>
 *   <li>Composition support (andThen) - 支持函数组合</li>
 *   <li>Works with Validation.combine - 与 Validation.combine 配合使用</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TriFunction<String, Integer, Boolean, User> createUser =
 *     (name, age, active) -> new User(name, age, active);
 *
 * User user = createUser.apply("Alice", 25, true);
 *
 * // With composition
 * TriFunction<String, Integer, Boolean, String> createAndFormat =
 *     createUser.andThen(User::toString);
 *
 * // With Validation
 * Validation<String, User> result = Validation.combine(
 *     validateName(name),
 *     validateAge(age),
 *     validateActive(active),
 *     createUser
 * );
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless) - 线程安全: 是 (无状态)</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @param <T1> first input type - 第一个输入类型
 * @param <T2> second input type - 第二个输入类型
 * @param <T3> third input type - 第三个输入类型
 * @param <R>  return type - 返回值类型
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
@FunctionalInterface
public interface TriFunction<T1, T2, T3, R> {

    /**
     * Apply function to three arguments
     * 应用函数到三个参数
     *
     * @param t1 first input - 第一个输入
     * @param t2 second input - 第二个输入
     * @param t3 third input - 第三个输入
     * @return result - 结果
     */
    R apply(T1 t1, T2 t2, T3 t3);

    /**
     * Compose with another function (execute this, then after)
     * 与另一个函数组合（先执行本函数，再执行 after）
     *
     * @param after function to apply after this - 在本函数之后应用的函数
     * @param <V>   result type of after function - after 函数的结果类型
     * @return composed function - 组合后的函数
     */
    default <V> TriFunction<T1, T2, T3, V> andThen(Function<? super R, ? extends V> after) {
        return (t1, t2, t3) -> after.apply(apply(t1, t2, t3));
    }
}

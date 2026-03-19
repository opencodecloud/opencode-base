/**
 * Function Utilities - Advanced function operations
 * 函数工具 - 高级函数操作
 *
 * <p>Provides function composition, currying, memoization, and additional
 * checked functional interfaces extending Core module.</p>
 * <p>提供函数组合、柯里化、记忆化以及扩展 Core 模块的可抛异常函数接口。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.function.FunctionUtil} - Function utilities</li>
 *   <li>{@link cloud.opencode.base.functional.function.CheckedBiFunction} - Checked BiFunction</li>
 *   <li>{@link cloud.opencode.base.functional.function.CheckedBiConsumer} - Checked BiConsumer</li>
 *   <li>{@link cloud.opencode.base.functional.function.TriFunction} - Three-argument function</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Composition
 * var composed = FunctionUtil.compose(f, g);  // g(f(x))
 *
 * // Currying
 * BiFunction<Integer, Integer, Integer> add = (a, b) -> a + b;
 * Function<Integer, Integer> add5 = FunctionUtil.curry(add).apply(5);
 *
 * // Memoization
 * Function<Integer, Integer> fib = FunctionUtil.memoize(n ->
 *     n <= 1 ? n : fib.apply(n-1) + fib.apply(n-2));
 *
 * // Partial application
 * Function<String, String> greet = FunctionUtil.partial(
 *     (greeting, name) -> greeting + ", " + name, "Hello");
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.function;

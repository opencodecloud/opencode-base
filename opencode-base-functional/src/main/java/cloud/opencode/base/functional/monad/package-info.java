/**
 * Monad Types - Functional containers for computation
 * Monad 类型 - 计算的函数式容器
 *
 * <p>Provides standard Monad types for functional programming including
 * Try, Either, Option, Validation, and Lazy evaluation.</p>
 * <p>提供函数式编程的标准 Monad 类型，包括 Try、Either、Option、Validation 和惰性求值。</p>
 *
 * <p><strong>Monad Types | Monad 类型:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.monad.Try} - Exception handling monad</li>
 *   <li>{@link cloud.opencode.base.functional.monad.Either} - Either/Or type (Left/Right)</li>
 *   <li>{@link cloud.opencode.base.functional.monad.Option} - Optional value (Some/None)</li>
 *   <li>{@link cloud.opencode.base.functional.monad.Validation} - Accumulating validation</li>
 *   <li>{@link cloud.opencode.base.functional.monad.Lazy} - Lazy evaluation container</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Try - Handle exceptions functionally
 * Try<Integer> result = Try.of(() -> Integer.parseInt(input))
 *     .map(n -> n * 2)
 *     .recover(e -> 0);
 *
 * // Either - Express two possible outcomes
 * Either<Error, User> user = findUser(id)
 *     .map(User::activate);
 *
 * // Validation - Accumulate multiple errors
 * Validation<String, User> validated = Validation.combine(
 *     validateName(name),
 *     validateAge(age),
 *     validateEmail(email),
 *     User::new
 * );
 * }</pre>
 *
 * <p><strong>Thread Safety | 线程安全:</strong></p>
 * <p>All Monad types are immutable and thread-safe.</p>
 * <p>所有 Monad 类型都是不可变且线程安全的。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.monad;

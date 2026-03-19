/**
 * OpenCode Functional - Functional Programming Utilities for JDK 25+
 * OpenCode 函数式编程工具库
 *
 * <p>Provides comprehensive functional programming utilities including Monads,
 * Pattern Matching, Pipeline operations, and Lens optics for immutable data.</p>
 * <p>提供全面的函数式编程工具，包括 Monad 类型、模式匹配、管道操作和用于不可变数据的 Lens 光学类型。</p>
 *
 * <p><strong>Core Features | 核心功能:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.monad} - Monad types (Try/Either/Option/Validation/Lazy)</li>
 *   <li>{@link cloud.opencode.base.functional.function} - Function utilities (compose/curry/memoize)</li>
 *   <li>{@link cloud.opencode.base.functional.pattern} - Pattern matching (OpenMatch)</li>
 *   <li>{@link cloud.opencode.base.functional.pipeline} - Data pipeline operations</li>
 *   <li>{@link cloud.opencode.base.functional.optics} - Lens for immutable updates</li>
 *   <li>{@link cloud.opencode.base.functional.async} - Virtual thread async utilities</li>
 * </ul>
 *
 * <p><strong>Quick Start | 快速开始:</strong></p>
 * <pre>{@code
 * // Try - Exception handling
 * Try<User> user = Try.of(() -> userService.findById(id))
 *     .map(this::enrichUser)
 *     .recover(e -> User.DEFAULT);
 *
 * // Either - Error handling
 * Either<Error, User> result = validateUser(input)
 *     .map(this::saveUser);
 *
 * // Pipeline - Data transformation
 * String result = Pipeline.<String>start()
 *     .then(String::trim)
 *     .then(String::toUpperCase)
 *     .execute(input);
 *
 * // Function composition
 * var addThenDouble = FunctionUtil.compose(x -> x + 1, x -> x * 2);
 * }</pre>
 *
 * <p><strong>Dependencies | 依赖:</strong></p>
 * <ul>
 *   <li>opencode-base-core - Tuple, basic functional interfaces</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional;

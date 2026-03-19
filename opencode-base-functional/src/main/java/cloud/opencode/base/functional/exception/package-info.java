/**
 * Functional Exceptions - Exception types for functional operations
 * 函数式异常 - 函数式操作的异常类型
 *
 * <p>Provides exception types specific to functional programming operations
 * including pattern matching failures and functional computation errors.</p>
 * <p>提供函数式编程操作特定的异常类型，包括模式匹配失败和函数式计算错误。</p>
 *
 * <p><strong>Exception Types | 异常类型:</strong></p>
 * <ul>
 *   <li>{@link cloud.opencode.base.functional.exception.OpenFunctionalException} - Base functional exception</li>
 *   <li>{@link cloud.opencode.base.functional.exception.OpenMatchException} - Pattern matching exception</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Pattern matching failure
 * try {
 *     String result = OpenMatch.of(value)
 *         .caseOf(String.class, s -> "String")
 *         .orElseThrow();  // Throws OpenMatchException if no match
 * } catch (OpenMatchException e) {
 *     Object unmatched = e.unmatchedValue();
 * }
 *
 * // Functional computation failure
 * try {
 *     User user = Try.of(() -> findUser(id)).get();
 * } catch (OpenFunctionalException e) {
 *     // Handle functional error
 * }
 * }</pre>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-functional V1.0.0
 */
package cloud.opencode.base.functional.exception;

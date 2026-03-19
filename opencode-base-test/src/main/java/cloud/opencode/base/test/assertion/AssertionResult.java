package cloud.opencode.base.test.assertion;

/**
 * Assertion Result - Sealed interface for assertion results
 * 断言结果 - 断言结果的密封接口
 *
 * <p>Represents the result of an assertion, used for soft assertions where
 * failures are collected rather than immediately thrown.</p>
 * <p>表示断言的结果，用于软断言场景，失败被收集而非立即抛出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Sealed interface with Success and Failure implementations - 密封接口，包含Success和Failure实现</li>
 *   <li>Immutable assertion results - 不可变断言结果</li>
 *   <li>Optional expected/actual value tracking in failures - 失败中可选的期望值/实际值跟踪</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * AssertionResult result = checkCondition()
 *     ? AssertionResult.success()
 *     : AssertionResult.failure("Condition failed", expected, actual);
 *
 * if (!result.passed()) {
 *     failures.add(result);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public sealed interface AssertionResult permits AssertionResult.Success, AssertionResult.Failure {

    /**
     * Whether the assertion passed.
     * 断言是否通过。
     *
     * @return true if passed | 如果通过返回 true
     */
    boolean passed();

    /**
     * Creates a success result.
     * 创建成功结果。
     *
     * @return success result | 成功结果
     */
    static AssertionResult success() {
        return Success.INSTANCE;
    }

    /**
     * Creates a failure result.
     * 创建失败结果。
     *
     * @param message the failure message | 失败消息
     * @return failure result | 失败结果
     */
    static AssertionResult failure(String message) {
        return new Failure(message, null, null);
    }

    /**
     * Creates a failure result with expected and actual values.
     * 创建带期望值和实际值的失败结果。
     *
     * @param message  the failure message | 失败消息
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @return failure result | 失败结果
     */
    static AssertionResult failure(String message, Object expected, Object actual) {
        return new Failure(message, expected, actual);
    }

    // ==================== Success Result | 成功结果 ====================

    /**
     * Success assertion result.
     * 成功断言结果。
     */
    final class Success implements AssertionResult {

        private static final Success INSTANCE = new Success();

        private Success() {
        }

        @Override
        public boolean passed() {
            return true;
        }

        @Override
        public String toString() {
            return "Success";
        }
    }

    // ==================== Failure Result | 失败结果 ====================

    /**
     * Failure assertion result.
     * 失败断言结果。
     *
     * @param message  the failure message | 失败消息
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     */
    record Failure(String message, Object expected, Object actual) implements AssertionResult {

        @Override
        public boolean passed() {
            return false;
        }

        /**
         * Whether this failure has expected/actual values.
         * 此失败是否有期望值/实际值。
         *
         * @return true if has values | 如果有值返回 true
         */
        public boolean hasValues() {
            return expected != null || actual != null;
        }

        @Override
        public String toString() {
            if (hasValues()) {
                return String.format("Failure[%s, expected=%s, actual=%s]", message, expected, actual);
            }
            return String.format("Failure[%s]", message);
        }
    }
}

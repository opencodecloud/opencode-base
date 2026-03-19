package cloud.opencode.base.test.exception;

/**
 * Assertion Exception - Exception thrown when an assertion fails
 * 断言异常 - 断言失败时抛出的异常
 *
 * <p>This exception is thrown when an assertion fails during testing.</p>
 * <p>此异常在测试期间断言失败时抛出。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Typed assertion failures with error codes - 带错误码的类型化断言失败</li>
 *   <li>Factory methods for common assertion failures - 常见断言失败的工厂方法</li>
 *   <li>Support for message, cause, and error code construction - 支持消息、原因和错误码构造</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw AssertionException.failed("Expected value not found");
 * throw AssertionException.notEqual("expected", "actual");
 * throw AssertionException.timeout(5000, 6200);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class AssertionException extends TestException {

    /**
     * Creates assertion exception with error code.
     * 使用错误码创建断言异常。
     *
     * @param errorCode the error code | 错误码
     */
    public AssertionException(TestErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Creates assertion exception with error code and detail.
     * 使用错误码和详情创建断言异常。
     *
     * @param errorCode the error code | 错误码
     * @param detail    the detail message | 详细消息
     */
    public AssertionException(TestErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    /**
     * Creates assertion exception with message.
     * 使用消息创建断言异常。
     *
     * @param message the message | 消息
     */
    public AssertionException(String message) {
        super(message);
    }

    /**
     * Creates assertion exception with message and cause.
     * 使用消息和原因创建断言异常。
     *
     * @param message the message | 消息
     * @param cause   the cause | 原因
     */
    public AssertionException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for assertion failed.
     * 为断言失败创建异常。
     *
     * @param message the message | 消息
     * @return the exception | 异常
     */
    public static AssertionException failed(String message) {
        return new AssertionException(TestErrorCode.ASSERTION_FAILED, message);
    }

    /**
     * Creates exception for null assertion.
     * 为空值断言创建异常。
     *
     * @return the exception | 异常
     */
    public static AssertionException nullAssertion() {
        return new AssertionException(TestErrorCode.ASSERTION_NULL);
    }

    /**
     * Creates exception for equality assertion.
     * 为相等断言创建异常。
     *
     * @param expected the expected value | 期望值
     * @param actual   the actual value | 实际值
     * @return the exception | 异常
     */
    public static AssertionException notEqual(Object expected, Object actual) {
        return new AssertionException(TestErrorCode.ASSERTION_EQUALS,
            "expected: " + expected + ", actual: " + actual);
    }

    /**
     * Creates exception for timeout.
     * 为超时创建异常。
     *
     * @param timeoutMs the timeout in milliseconds | 超时毫秒数
     * @param actualMs  the actual time in milliseconds | 实际毫秒数
     * @return the exception | 异常
     */
    public static AssertionException timeout(long timeoutMs, long actualMs) {
        return new AssertionException(TestErrorCode.ASSERTION_TIMEOUT,
            "timeout: " + timeoutMs + "ms, actual: " + actualMs + "ms");
    }
}

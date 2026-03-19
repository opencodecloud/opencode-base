package cloud.opencode.base.test.exception;

/**
 * Test Exception
 * 测试异常
 *
 * <p>Exception thrown during test execution.</p>
 * <p>测试执行期间抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Test execution exception handling - 测试执行异常处理</li>
 *   <li>Error code support - 错误码支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new TestException(TestErrorCode.ASSERTION_FAILED, "Values differ");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: No - 线程安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class TestException extends RuntimeException {

    private final TestErrorCode errorCode;

    /**
     * Create test exception
     * 创建测试异常
     *
     * @param errorCode the error code | 错误码
     */
    public TestException(TestErrorCode errorCode) {
        super(errorCode.message());
        this.errorCode = errorCode;
    }

    /**
     * Create test exception with detail
     * 创建带详情的测试异常
     *
     * @param errorCode the error code | 错误码
     * @param detail the detail message | 详细消息
     */
    public TestException(TestErrorCode errorCode, String detail) {
        super(errorCode.message() + ": " + detail);
        this.errorCode = errorCode;
    }

    /**
     * Create test exception with cause
     * 创建带原因的测试异常
     *
     * @param errorCode the error code | 错误码
     * @param cause the cause | 原因
     */
    public TestException(TestErrorCode errorCode, Throwable cause) {
        super(errorCode.message(), cause);
        this.errorCode = errorCode;
    }

    /**
     * Create test exception with message
     * 创建带消息的测试异常
     *
     * @param message the message | 消息
     */
    public TestException(String message) {
        super(message);
        this.errorCode = TestErrorCode.GENERAL_ERROR;
    }

    /**
     * Create test exception with message and cause
     * 创建带消息和原因的测试异常
     *
     * @param message the message | 消息
     * @param cause the cause | 原因
     */
    public TestException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = TestErrorCode.GENERAL_ERROR;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return the error code | 错误码
     */
    public TestErrorCode getErrorCode() {
        return errorCode;
    }
}

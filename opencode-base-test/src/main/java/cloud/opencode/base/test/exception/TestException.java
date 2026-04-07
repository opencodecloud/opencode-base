package cloud.opencode.base.test.exception;

import cloud.opencode.base.core.exception.OpenException;

import java.io.Serial;

/**
 * Test Exception - Base exception for test operations
 * 测试异常 - 测试操作的基础异常
 *
 * <p>Base exception for all test-related errors, extending {@link OpenException}
 * with test-specific error codes.</p>
 * <p>所有测试相关错误的基础异常，继承 {@link OpenException} 并提供测试专用错误码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Extends OpenException for unified error handling - 继承 OpenException 统一异常处理</li>
 *   <li>Test error code support - 测试错误码支持</li>
 *   <li>Formatted message: [Test] (TEST-xxxx) message - 格式化消息</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw new TestException(TestErrorCode.ASSERTION_FAILED, "Values differ");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public class TestException extends OpenException {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final String COMPONENT = "Test";

    private final TestErrorCode testErrorCode;

    /**
     * Create test exception with error code
     * 使用错误码创建测试异常
     *
     * @param errorCode the error code | 错误码
     */
    public TestException(TestErrorCode errorCode) {
        super(COMPONENT, errorCode.code(), errorCode.message());
        this.testErrorCode = errorCode;
    }

    /**
     * Create test exception with error code and detail
     * 使用错误码和详情创建测试异常
     *
     * @param errorCode the error code | 错误码
     * @param detail the detail message | 详细消息
     */
    public TestException(TestErrorCode errorCode, String detail) {
        super(COMPONENT, errorCode.code(), errorCode.message() + ": " + detail);
        this.testErrorCode = errorCode;
    }

    /**
     * Create test exception with error code and cause
     * 使用错误码和原因创建测试异常
     *
     * @param errorCode the error code | 错误码
     * @param cause the cause | 原因
     */
    public TestException(TestErrorCode errorCode, Throwable cause) {
        super(COMPONENT, errorCode.code(), errorCode.message(), cause);
        this.testErrorCode = errorCode;
    }

    /**
     * Create test exception with message
     * 使用消息创建测试异常
     *
     * @param message the message | 消息
     */
    public TestException(String message) {
        super(COMPONENT, TestErrorCode.GENERAL_ERROR.code(), message);
        this.testErrorCode = TestErrorCode.GENERAL_ERROR;
    }

    /**
     * Create test exception with message and cause
     * 使用消息和原因创建测试异常
     *
     * @param message the message | 消息
     * @param cause the cause | 原因
     */
    public TestException(String message, Throwable cause) {
        super(COMPONENT, TestErrorCode.GENERAL_ERROR.code(), message, cause);
        this.testErrorCode = TestErrorCode.GENERAL_ERROR;
    }

    /**
     * Get the typed test error code
     * 获取类型化的测试错误码
     *
     * @return the test error code | 测试错误码
     */
    public TestErrorCode getTestErrorCode() {
        return testErrorCode;
    }
}

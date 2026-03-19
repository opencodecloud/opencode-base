package cloud.opencode.base.test.exception;

/**
 * Mock Exception - Exception thrown during mock operations
 * Mock异常 - Mock操作期间抛出的异常
 *
 * <p>This exception is thrown when mock creation, stubbing, or verification fails.</p>
 * <p>当Mock创建、存根或验证失败时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Mock creation failure reporting - Mock创建失败报告</li>
 *   <li>Non-interface mock attempt detection - 非接口Mock尝试检测</li>
 *   <li>Verification failure reporting - 验证失败报告</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw MockException.creationFailed(MyClass.class);
 * throw MockException.notInterface(MyClass.class);
 * throw MockException.verificationFailed("save called 1 time(s)", 0);
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
public class MockException extends TestException {

    /**
     * Creates mock exception with error code.
     * 使用错误码创建Mock异常。
     *
     * @param errorCode the error code | 错误码
     */
    public MockException(TestErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Creates mock exception with error code and detail.
     * 使用错误码和详情创建Mock异常。
     *
     * @param errorCode the error code | 错误码
     * @param detail    the detail message | 详细消息
     */
    public MockException(TestErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    /**
     * Creates mock exception with message.
     * 使用消息创建Mock异常。
     *
     * @param message the message | 消息
     */
    public MockException(String message) {
        super(message);
    }

    /**
     * Creates mock exception with message and cause.
     * 使用消息和原因创建Mock异常。
     *
     * @param message the message | 消息
     * @param cause   the cause | 原因
     */
    public MockException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for mock creation failure.
     * 为Mock创建失败创建异常。
     *
     * @param type the type that failed to mock | 无法Mock的类型
     * @return the exception | 异常
     */
    public static MockException creationFailed(Class<?> type) {
        return new MockException(TestErrorCode.MOCK_CREATION_FAILED,
            "Failed to create mock for: " + type.getName());
    }

    /**
     * Creates exception for non-interface mock attempt.
     * 为非接口Mock尝试创建异常。
     *
     * @param type the non-interface type | 非接口类型
     * @return the exception | 异常
     */
    public static MockException notInterface(Class<?> type) {
        return new MockException(TestErrorCode.MOCK_NOT_INTERFACE,
            "Only interfaces can be mocked: " + type.getName());
    }

    /**
     * Creates exception for verification failure.
     * 为验证失败创建异常。
     *
     * @param expected the expected invocation | 期望的调用
     * @param actual   the actual invocation count | 实际调用次数
     * @return the exception | 异常
     */
    public static MockException verificationFailed(String expected, int actual) {
        return new MockException(TestErrorCode.MOCK_VERIFICATION_FAILED,
            "Expected: " + expected + ", actual invocations: " + actual);
    }
}

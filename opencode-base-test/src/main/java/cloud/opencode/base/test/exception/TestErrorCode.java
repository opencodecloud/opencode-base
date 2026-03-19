package cloud.opencode.base.test.exception;

/**
 * Test Error Code
 * 测试错误码
 *
 * <p>Error codes for test operations (TEST-1xxx ~ 4xxx).</p>
 * <p>测试操作的错误码（TEST-1xxx ~ 4xxx）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Categorized test error codes - 分类测试错误码</li>
 *   <li>Assertion, setup, execution error codes - 断言、设置、执行错误码</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TestErrorCode code = TestErrorCode.ASSERTION_FAILED;
 * System.out.println(code.code() + ": " + code.message());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-test V1.0.0
 */
public enum TestErrorCode {

    // 1xxx - Assertion errors | 断言错误
    ASSERTION_FAILED("TEST-1001", "Assertion failed"),
    EXPECTED_EXCEPTION_NOT_THROWN("TEST-1002", "Expected exception was not thrown"),
    UNEXPECTED_EXCEPTION("TEST-1003", "Unexpected exception thrown"),
    VALUE_MISMATCH("TEST-1004", "Value mismatch"),
    ASSERTION_NULL("TEST-1005", "Assertion null check failed"),
    ASSERTION_EQUALS("TEST-1006", "Assertion equals check failed"),
    ASSERTION_TIMEOUT("TEST-1007", "Assertion timeout"),

    // 2xxx - Setup errors | 设置错误
    FIXTURE_NOT_FOUND("TEST-2001", "Fixture not found"),
    FIXTURE_INIT_FAILED("TEST-2002", "Fixture initialization failed"),
    MOCK_SETUP_FAILED("TEST-2003", "Mock setup failed"),
    MOCK_CREATION_FAILED("TEST-2004", "Mock creation failed"),
    MOCK_NOT_INTERFACE("TEST-2005", "Mock target is not an interface"),
    MOCK_VERIFICATION_FAILED("TEST-2006", "Mock verification failed"),

    // 3xxx - Execution errors | 执行错误
    TIMEOUT("TEST-3001", "Test timeout"),
    CONCURRENT_ERROR("TEST-3002", "Concurrent execution error"),
    BENCHMARK_FAILED("TEST-3003", "Benchmark failed"),
    BENCHMARK_TIMEOUT("TEST-3004", "Benchmark timeout"),
    DATA_GENERATION_FAILED("TEST-3005", "Data generation failed"),
    DATA_RANGE_INVALID("TEST-3006", "Data range invalid"),

    // 4xxx - General errors | 一般错误
    GENERAL_ERROR("TEST-4001", "General test error"),
    INVALID_CONFIGURATION("TEST-4002", "Invalid test configuration");

    private final String code;
    private final String message;

    TestErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * Get error code
     * 获取错误码
     *
     * @return the code | 错误码
     */
    public String code() {
        return code;
    }

    /**
     * Get error message
     * 获取错误消息
     *
     * @return the message | 错误消息
     */
    public String message() {
        return message;
    }

    @Override
    public String toString() {
        return code + ": " + message;
    }
}

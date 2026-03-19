package cloud.opencode.base.test.exception;

/**
 * Benchmark Exception - Exception thrown during benchmark execution
 * 基准测试异常 - 基准测试执行期间抛出的异常
 *
 * <p>This exception is thrown when benchmark execution fails.</p>
 * <p>当基准测试执行失败时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Benchmark execution failure reporting - 基准测试执行失败报告</li>
 *   <li>Benchmark timeout reporting - 基准测试超时报告</li>
 *   <li>Factory methods for common benchmark errors - 常见基准测试错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw BenchmarkException.executionFailed("sortBenchmark", cause);
 * throw BenchmarkException.timeout("sortBenchmark", 30000);
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
public class BenchmarkException extends TestException {

    /**
     * Creates benchmark exception with error code.
     * 使用错误码创建基准测试异常。
     *
     * @param errorCode the error code | 错误码
     */
    public BenchmarkException(TestErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Creates benchmark exception with error code and detail.
     * 使用错误码和详情创建基准测试异常。
     *
     * @param errorCode the error code | 错误码
     * @param detail    the detail message | 详细消息
     */
    public BenchmarkException(TestErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    /**
     * Creates benchmark exception with message.
     * 使用消息创建基准测试异常。
     *
     * @param message the message | 消息
     */
    public BenchmarkException(String message) {
        super(message);
    }

    /**
     * Creates benchmark exception with message and cause.
     * 使用消息和原因创建基准测试异常。
     *
     * @param message the message | 消息
     * @param cause   the cause | 原因
     */
    public BenchmarkException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for benchmark execution failure.
     * 为基准测试执行失败创建异常。
     *
     * @param benchmarkName the benchmark name | 基准测试名称
     * @param cause         the cause | 原因
     * @return the exception | 异常
     */
    public static BenchmarkException executionFailed(String benchmarkName, Throwable cause) {
        return new BenchmarkException("Benchmark '" + benchmarkName + "' failed", cause);
    }

    /**
     * Creates exception for benchmark timeout.
     * 为基准测试超时创建异常。
     *
     * @param benchmarkName the benchmark name | 基准测试名称
     * @param timeoutMs     the timeout in milliseconds | 超时毫秒数
     * @return the exception | 异常
     */
    public static BenchmarkException timeout(String benchmarkName, long timeoutMs) {
        return new BenchmarkException(TestErrorCode.BENCHMARK_TIMEOUT,
            "Benchmark '" + benchmarkName + "' exceeded timeout of " + timeoutMs + "ms");
    }
}

package cloud.opencode.base.test.exception;

/**
 * Data Generation Exception - Exception thrown during test data generation
 * 数据生成异常 - 测试数据生成期间抛出的异常
 *
 * <p>This exception is thrown when test data generation fails.</p>
 * <p>当测试数据生成失败时抛出此异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Data generation failure reporting - 数据生成失败报告</li>
 *   <li>Invalid range error reporting - 无效范围错误报告</li>
 *   <li>Factory methods for common generation errors - 常见生成错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * throw DataGenerationException.generationFailed("email");
 * throw DataGenerationException.invalidRange(100, 10);
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
public class DataGenerationException extends TestException {

    /**
     * Creates data generation exception with error code.
     * 使用错误码创建数据生成异常。
     *
     * @param errorCode the error code | 错误码
     */
    public DataGenerationException(TestErrorCode errorCode) {
        super(errorCode);
    }

    /**
     * Creates data generation exception with error code and detail.
     * 使用错误码和详情创建数据生成异常。
     *
     * @param errorCode the error code | 错误码
     * @param detail    the detail message | 详细消息
     */
    public DataGenerationException(TestErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }

    /**
     * Creates data generation exception with message.
     * 使用消息创建数据生成异常。
     *
     * @param message the message | 消息
     */
    public DataGenerationException(String message) {
        super(message);
    }

    /**
     * Creates data generation exception with message and cause.
     * 使用消息和原因创建数据生成异常。
     *
     * @param message the message | 消息
     * @param cause   the cause | 原因
     */
    public DataGenerationException(String message, Throwable cause) {
        super(message, cause);
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates exception for general generation failure.
     * 为一般生成失败创建异常。
     *
     * @param dataType the type of data | 数据类型
     * @return the exception | 异常
     */
    public static DataGenerationException generationFailed(String dataType) {
        return new DataGenerationException(TestErrorCode.DATA_GENERATION_FAILED,
            "Failed to generate: " + dataType);
    }

    /**
     * Creates exception for invalid range.
     * 为无效范围创建异常。
     *
     * @param min the minimum value | 最小值
     * @param max the maximum value | 最大值
     * @return the exception | 异常
     */
    public static DataGenerationException invalidRange(Number min, Number max) {
        return new DataGenerationException(TestErrorCode.DATA_RANGE_INVALID,
            "Invalid range: min=" + min + ", max=" + max);
    }
}

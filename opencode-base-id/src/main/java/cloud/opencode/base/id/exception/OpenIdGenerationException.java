package cloud.opencode.base.id.exception;

import cloud.opencode.base.core.exception.OpenException;

/**
 * ID Generation Exception
 * ID生成异常
 *
 * <p>Exception thrown when ID generation fails due to various reasons
 * such as clock backward, segment exhaustion, or invalid parameters.</p>
 * <p>当ID生成由于各种原因（如时钟回拨、号段耗尽或参数无效）失败时抛出的异常。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Generator type tracking - 生成器类型追踪</li>
 *   <li>Business tag support - 业务标识支持</li>
 *   <li>Factory methods for common errors - 常见错误的工厂方法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try {
 *     long id = generator.generate();
 * } catch (OpenIdGenerationException e) {
 *     log.error("ID generation failed: type={}, bizTag={}",
 *         e.generatorType(), e.bizTag());
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable exception) - 线程安全: 是（不可变异常）</li>
 *   <li>Null-safe: No - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-id V1.0.0
 */
public class OpenIdGenerationException extends OpenException {

    private static final String COMPONENT = "id";

    /**
     * Generator type
     * 生成器类型
     */
    private final String generatorType;

    /**
     * Business tag
     * 业务标识
     */
    private final String bizTag;

    // ==================== Constructors | 构造方法 ====================

    /**
     * Creates an exception with message
     * 使用消息创建异常
     *
     * @param message the error message | 错误消息
     */
    public OpenIdGenerationException(String message) {
        super(COMPONENT, null, message, null);
        this.generatorType = null;
        this.bizTag = null;
    }

    /**
     * Creates an exception with message and cause
     * 使用消息和原因创建异常
     *
     * @param message the error message | 错误消息
     * @param cause   the cause | 原因
     */
    public OpenIdGenerationException(String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.generatorType = null;
        this.bizTag = null;
    }

    /**
     * Creates an exception with generator type, bizTag and message
     * 使用生成器类型、业务标识和消息创建异常
     *
     * @param generatorType the generator type | 生成器类型
     * @param bizTag        the business tag | 业务标识
     * @param message       the error message | 错误消息
     */
    public OpenIdGenerationException(String generatorType, String bizTag, String message) {
        super(COMPONENT, null, message, null);
        this.generatorType = generatorType;
        this.bizTag = bizTag;
    }

    /**
     * Creates an exception with generator type, bizTag, message and cause
     * 使用生成器类型、业务标识、消息和原因创建异常
     *
     * @param generatorType the generator type | 生成器类型
     * @param bizTag        the business tag | 业务标识
     * @param message       the error message | 错误消息
     * @param cause         the cause | 原因
     */
    public OpenIdGenerationException(String generatorType, String bizTag, String message, Throwable cause) {
        super(COMPONENT, null, message, cause);
        this.generatorType = generatorType;
        this.bizTag = bizTag;
    }

    // ==================== Accessors | 访问方法 ====================

    /**
     * Gets the generator type
     * 获取生成器类型
     *
     * @return generator type | 生成器类型
     */
    public String generatorType() {
        return generatorType;
    }

    /**
     * Gets the business tag
     * 获取业务标识
     *
     * @return business tag | 业务标识
     */
    public String bizTag() {
        return bizTag;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates a clock backward exception
     * 创建时钟回拨异常
     *
     * @param lastTimestamp    the last timestamp | 上次时间戳
     * @param currentTimestamp the current timestamp | 当前时间戳
     * @return exception | 异常
     */
    public static OpenIdGenerationException clockBackward(long lastTimestamp, long currentTimestamp) {
        return new OpenIdGenerationException("snowflake", null,
                String.format("Clock moved backwards. Last timestamp: %d, current: %d, diff: %d ms",
                        lastTimestamp, currentTimestamp, lastTimestamp - currentTimestamp));
    }

    /**
     * Creates an invalid parameter exception
     * 创建参数无效异常
     *
     * @param param the parameter name | 参数名称
     * @param value the parameter value | 参数值
     * @param range the valid range | 有效范围
     * @return exception | 异常
     */
    public static OpenIdGenerationException invalidParameter(String param, long value, String range) {
        return new OpenIdGenerationException(null, null,
                String.format("Invalid parameter '%s': %d, valid range: %s", param, value, range));
    }

    /**
     * Creates a segment exhausted exception
     * 创建号段耗尽异常
     *
     * @param bizTag the business tag | 业务标识
     * @return exception | 异常
     */
    public static OpenIdGenerationException segmentExhausted(String bizTag) {
        return new OpenIdGenerationException("segment", bizTag,
                String.format("Segment exhausted for bizTag: %s", bizTag));
    }

    /**
     * Creates a segment allocation failed exception
     * 创建号段分配失败异常
     *
     * @param bizTag the business tag | 业务标识
     * @param cause  the cause | 原因
     * @return exception | 异常
     */
    public static OpenIdGenerationException segmentAllocationFailed(String bizTag, Throwable cause) {
        return new OpenIdGenerationException("segment", bizTag,
                String.format("Failed to allocate segment for bizTag: %s", bizTag), cause);
    }

    /**
     * Creates an invalid ID format exception
     * 创建ID格式无效异常
     *
     * @param generatorType the generator type | 生成器类型
     * @param id            the invalid ID | 无效的ID
     * @return exception | 异常
     */
    public static OpenIdGenerationException invalidIdFormat(String generatorType, String id) {
        return new OpenIdGenerationException(generatorType, null,
                String.format("Invalid ID format for %s: %s", generatorType, id));
    }

    /**
     * Creates a sequence overflow exception
     * 创建序列号溢出异常
     *
     * @param maxSequence the maximum sequence value | 最大序列号值
     * @return exception | 异常
     */
    public static OpenIdGenerationException sequenceOverflow(long maxSequence) {
        return new OpenIdGenerationException("snowflake", null,
                String.format("Sequence overflow, max sequence: %d", maxSequence));
    }
}

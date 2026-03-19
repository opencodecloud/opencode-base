package cloud.opencode.base.web;

import cloud.opencode.base.web.exception.OpenBizException;
import cloud.opencode.base.web.internal.TraceIdResolver;

import java.time.Instant;
import java.util.function.Function;

/**
 * Result
 * 统一响应结果
 *
 * <p>Standard API response wrapper with trace support.</p>
 * <p>带追踪支持的标准API响应包装器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Immutable record - 不可变记录</li>
 *   <li>Trace ID support - 追踪ID支持</li>
 *   <li>Fluent API - 流式API</li>
 *   <li>Functional operations - 函数式操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Result<User> ok = Result.ok(user);
 * Result<?> fail = Result.fail("E001", "Not found");
 * T data = result.getDataOrThrow();
 * Result<DTO> mapped = result.map(user -> toDTO(user));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 是（不可变记录）</li>
 *   <li>Null-safe: Yes (data can be null) - 是（数据可以为null）</li>
 * </ul>
 * @param <T> the data type | 数据类型
 * @param code the result code | 响应码
 * @param message the result message | 响应消息
 * @param data the result data | 响应数据
 * @param success whether the operation was successful | 操作是否成功
 * @param timestamp the response timestamp | 响应时间戳
 * @param traceId the trace ID for request tracking | 请求追踪ID
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public record Result<T>(
    String code,
    String message,
    T data,
    boolean success,
    Instant timestamp,
    String traceId
) {

    // === Static Factory Methods ===

    /**
     * Create success result
     * 创建成功结果
     *
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok() {
        return Results.ok();
    }

    /**
     * Create success result with data
     * 创建带数据的成功结果
     *
     * @param data the data | 数据
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok(T data) {
        return Results.ok(data);
    }

    /**
     * Create success result with message and data
     * 创建带消息和数据的成功结果
     *
     * @param message the message | 消息
     * @param data the data | 数据
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok(String message, T data) {
        return Results.ok(message, data);
    }

    /**
     * Create failure result
     * 创建失败结果
     *
     * @param code the code | 代码
     * @param message the message | 消息
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(String code, String message) {
        return Results.fail(code, message);
    }

    /**
     * Create failure result with result code
     * 使用响应码创建失败结果
     *
     * @param resultCode the result code | 响应码
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        return Results.fail(resultCode);
    }

    /**
     * Create failure result with result code and custom message
     * 使用响应码和自定义消息创建失败结果
     *
     * @param resultCode the result code | 响应码
     * @param message the custom message | 自定义消息
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(ResultCode resultCode, String message) {
        return Results.fail(resultCode, message);
    }

    /**
     * Create failure result from exception
     * 从异常创建失败结果
     *
     * @param throwable the exception | 异常
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(Throwable throwable) {
        return Results.fail(throwable);
    }

    // === Instance Methods ===

    /**
     * Check if failed
     * 检查是否失败
     *
     * @return true if failed | 如果失败返回true
     */
    public boolean isFailed() {
        return !success;
    }

    /**
     * Get data or throw exception
     * 获取数据或抛出异常
     *
     * @return the data | 数据
     * @throws OpenBizException if failed | 如果失败抛出异常
     */
    public T getDataOrThrow() {
        if (success) {
            return data;
        }
        throw new OpenBizException(code, message);
    }

    /**
     * Get data or default value
     * 获取数据或默认值
     *
     * @param defaultValue the default value | 默认值
     * @return the data or default | 数据或默认值
     */
    public T getDataOrDefault(T defaultValue) {
        return success ? data : defaultValue;
    }

    /**
     * Map the data to another type
     * 将数据映射为另一种类型
     *
     * @param mapper the mapper function | 映射函数
     * @param <R> the result type | 结果类型
     * @return the mapped result | 映射后的结果
     */
    public <R> Result<R> map(Function<T, R> mapper) {
        if (success && data != null) {
            return new Result<>(code, message, mapper.apply(data), true, timestamp, traceId);
        }
        return new Result<>(code, message, null, success, timestamp, traceId);
    }

    /**
     * FlatMap the data
     * 扁平映射数据
     *
     * @param mapper the mapper function | 映射函数
     * @param <R> the result type | 结果类型
     * @return the flat mapped result | 扁平映射后的结果
     */
    public <R> Result<R> flatMap(Function<T, Result<R>> mapper) {
        if (success) {
            return mapper.apply(data);
        }
        return new Result<>(code, message, null, false, timestamp, traceId);
    }

    /**
     * Execute action if success
     * 如果成功则执行操作
     *
     * @param action the action to execute | 要执行的操作
     * @return this result | 此结果
     */
    public Result<T> onSuccess(java.util.function.Consumer<T> action) {
        if (success) {
            action.accept(data);
        }
        return this;
    }

    /**
     * Execute action if failed
     * 如果失败则执行操作
     *
     * @param action the action to execute | 要执行的操作
     * @return this result | 此结果
     */
    public Result<T> onFailure(java.util.function.BiConsumer<String, String> action) {
        if (!success) {
            action.accept(code, message);
        }
        return this;
    }

    /**
     * Create new result with different trace ID
     * 创建具有不同追踪ID的新结果
     *
     * @param newTraceId the new trace ID | 新追踪ID
     * @return the new result | 新结果
     */
    public Result<T> withTraceId(String newTraceId) {
        return new Result<>(code, message, data, success, timestamp, newTraceId);
    }

    /**
     * Create new result with different message
     * 创建具有不同消息的新结果
     *
     * @param newMessage the new message | 新消息
     * @return the new result | 新结果
     */
    public Result<T> withMessage(String newMessage) {
        return new Result<>(code, newMessage, data, success, timestamp, traceId);
    }
}

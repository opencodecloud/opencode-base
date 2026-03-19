package cloud.opencode.base.web;

import cloud.opencode.base.web.exception.ExceptionConverter;
import cloud.opencode.base.web.internal.TraceIdResolver;
import cloud.opencode.base.web.page.PageInfo;
import cloud.opencode.base.web.page.PageResult;

import java.time.Instant;
import java.util.List;

/**
 * Results
 * 响应构建工具类
 *
 * <p>Factory class for creating Result objects.</p>
 * <p>创建Result对象的工厂类。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Factory methods for success and failure results - 成功和失败结果的工厂方法</li>
 *   <li>Page result creation - 分页结果创建</li>
 *   <li>Exception to Result conversion - 异常到 Result 的转换</li>
 *   <li>Builder pattern support - 构建器模式支持</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Success results
 * Result<String> result = Results.ok("Hello");
 * Result<User> result = Results.ok("Operation successful", user);
 *
 * // Failure results
 * Result<?> result = Results.fail("B1001", "Business error");
 * Result<?> result = Results.fail(CommonResultCode.NOT_FOUND);
 *
 * // Page results
 * Result<PageResult<User>> result = Results.page(users, 100, 1, 10);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (data can be null) - 空值安全: 是（数据可以为 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class Results {

    private static final String DEFAULT_SUCCESS_MESSAGE = "Success";

    private Results() {
        // Utility class
    }

    // === Success Results ===

    /**
     * Create success result
     * 创建成功结果
     *
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok() {
        return new Result<>(
            CommonResultCode.SUCCESS.getCode(),
            DEFAULT_SUCCESS_MESSAGE,
            null,
            true,
            Instant.now(),
            TraceIdResolver.resolve()
        );
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
        return ok(DEFAULT_SUCCESS_MESSAGE, data);
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
        return new Result<>(
            CommonResultCode.SUCCESS.getCode(),
            message,
            data,
            true,
            Instant.now(),
            TraceIdResolver.resolve()
        );
    }

    /**
     * Create success result with result code
     * 使用响应码创建成功结果
     *
     * @param resultCode the result code | 响应码
     * @param data the data | 数据
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> ok(ResultCode resultCode, T data) {
        return new Result<>(
            resultCode.getCode(),
            resultCode.getMessage(),
            data,
            true,
            Instant.now(),
            TraceIdResolver.resolve()
        );
    }

    // === Failure Results ===

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
        return new Result<>(
            code,
            message,
            null,
            false,
            Instant.now(),
            TraceIdResolver.resolve()
        );
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
        return fail(resultCode.getCode(), resultCode.getMessage());
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
        return fail(resultCode.getCode(), message);
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
        return ExceptionConverter.toResult(throwable);
    }

    /**
     * Create failure result with data
     * 创建带数据的失败结果
     *
     * @param code the code | 代码
     * @param message the message | 消息
     * @param data the data | 数据
     * @param <T> the data type | 数据类型
     * @return the result | 结果
     */
    public static <T> Result<T> fail(String code, String message, T data) {
        return new Result<>(
            code,
            message,
            data,
            false,
            Instant.now(),
            TraceIdResolver.resolve()
        );
    }

    // === Page Results ===

    /**
     * Create page result
     * 创建分页结果
     *
     * @param items the items | 项列表
     * @param pageInfo the page info | 分页信息
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> Result<PageResult<T>> page(List<T> items, PageInfo pageInfo) {
        return ok(PageResult.of(items, pageInfo));
    }

    /**
     * Create page result
     * 创建分页结果
     *
     * @param items the items | 项列表
     * @param total the total count | 总数
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> Result<PageResult<T>> page(List<T> items, long total, int page, int size) {
        return ok(PageResult.of(items, total, page, size));
    }

    /**
     * Create empty page result
     * 创建空分页结果
     *
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> Result<PageResult<T>> emptyPage() {
        return ok(PageResult.empty());
    }

    /**
     * Create empty page result with page info
     * 创建带分页信息的空分页结果
     *
     * @param page the page number | 页码
     * @param size the page size | 页大小
     * @param <T> the item type | 项类型
     * @return the result | 结果
     */
    public static <T> Result<PageResult<T>> emptyPage(int page, int size) {
        return ok(PageResult.empty(page, size));
    }

    // === Builder ===

    /**
     * Create result builder
     * 创建结果构建器
     *
     * @param <T> the data type | 数据类型
     * @return the builder | 构建器
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Result Builder
     * 结果构建器
     *
     * @param <T> the data type | 数据类型
     */
    public static final class Builder<T> {
        private String code = CommonResultCode.SUCCESS.getCode();
        private String message = DEFAULT_SUCCESS_MESSAGE;
        private T data;
        private boolean success = true;
        private String traceId;

        private Builder() {}

        /**
         * Set code
         * 设置代码
         *
         * @param code the code | 代码
         * @return this builder | 此构建器
         */
        public Builder<T> code(String code) {
            this.code = code;
            return this;
        }

        /**
         * Set code from result code
         * 从响应码设置代码
         *
         * @param resultCode the result code | 响应码
         * @return this builder | 此构建器
         */
        public Builder<T> code(ResultCode resultCode) {
            this.code = resultCode.getCode();
            this.message = resultCode.getMessage();
            this.success = resultCode.isSuccess();
            return this;
        }

        /**
         * Set message
         * 设置消息
         *
         * @param message the message | 消息
         * @return this builder | 此构建器
         */
        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Set data
         * 设置数据
         *
         * @param data the data | 数据
         * @return this builder | 此构建器
         */
        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        /**
         * Set success
         * 设置成功标志
         *
         * @param success the success flag | 成功标志
         * @return this builder | 此构建器
         */
        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        /**
         * Set trace ID
         * 设置追踪ID
         *
         * @param traceId the trace ID | 追踪ID
         * @return this builder | 此构建器
         */
        public Builder<T> traceId(String traceId) {
            this.traceId = traceId;
            return this;
        }

        /**
         * Build the result
         * 构建结果
         *
         * @return the result | 结果
         */
        public Result<T> build() {
            return new Result<>(
                code,
                message,
                data,
                success,
                Instant.now(),
                traceId != null ? traceId : TraceIdResolver.resolve()
            );
        }
    }
}

package cloud.opencode.base.web.exception;

import cloud.opencode.base.web.Result;
import cloud.opencode.base.web.CommonResultCode;
import cloud.opencode.base.web.internal.TraceIdResolver;

import java.time.Instant;

/**
 * Exception Converter
 * 异常转换器
 *
 * <p>Converts exceptions to Result objects.</p>
 * <p>将异常转换为Result对象。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Exception to Result conversion - 异常到 Result 的转换</li>
 *   <li>HTTP status mapping from exceptions - 从异常映射 HTTP 状态码</li>
 *   <li>Support for OpenWebException, OpenBizException hierarchy - 支持 OpenWebException、OpenBizException 层次</li>
 *   <li>Standard exception type handling (IllegalArgument, SecurityException, etc.) - 标准异常类型处理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Convert exception to Result
 * Result<?> result = ExceptionConverter.toResult(exception);
 *
 * // Get error code from exception
 * String code = ExceptionConverter.getCode(exception);
 *
 * // Get HTTP status from exception
 * int status = ExceptionConverter.getHttpStatus(exception);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: Yes (handles null exceptions) - 空值安全: 是（处理 null 异常）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) - fixed number of instanceof checks regardless of input - 时间复杂度: O(1) - 固定数量的 instanceof 检查，与输入无关</li>
 *   <li>Space complexity: O(1) - creates a single Result object per call - 空间复杂度: O(1) - 每次调用创建一个 Result 对象</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class ExceptionConverter {

    private ExceptionConverter() {
        // Utility class
    }

    /**
     * Convert exception to Result
     * 将异常转换为Result
     *
     * @param throwable the exception | 异常
     * @param <T> the result type | 结果类型
     * @return the result | 结果
     */
    public static <T> Result<T> toResult(Throwable throwable) {
        if (throwable == null) {
            return new Result<>(
                CommonResultCode.INTERNAL_ERROR.getCode(),
                "Unknown error",
                null,
                false,
                Instant.now(),
                TraceIdResolver.resolve()
            );
        }

        // Handle OpenWebException
        if (throwable instanceof OpenWebException webEx) {
            return new Result<>(
                webEx.getCode(),
                webEx.getMessage(),
                null,
                false,
                Instant.now(),
                TraceIdResolver.resolve()
            );
        }

        // Handle OpenBizException with data
        if (throwable instanceof OpenBizException bizEx && bizEx.getData() != null) {
            @SuppressWarnings("unchecked")
            T data = (T) bizEx.getData();
            return new Result<>(
                bizEx.getCode(),
                bizEx.getMessage(),
                data,
                false,
                Instant.now(),
                TraceIdResolver.resolve()
            );
        }

        // Handle IllegalArgumentException
        if (throwable instanceof IllegalArgumentException) {
            return new Result<>(
                CommonResultCode.PARAM_INVALID.getCode(),
                throwable.getMessage(),
                null,
                false,
                Instant.now(),
                TraceIdResolver.resolve()
            );
        }

        // Handle IllegalStateException
        if (throwable instanceof IllegalStateException) {
            return new Result<>(
                CommonResultCode.BUSINESS_ERROR.getCode(),
                throwable.getMessage(),
                null,
                false,
                Instant.now(),
                TraceIdResolver.resolve()
            );
        }

        // Handle NullPointerException
        if (throwable instanceof NullPointerException) {
            return new Result<>(
                CommonResultCode.PARAM_MISSING.getCode(),
                "Required parameter is null",
                null,
                false,
                Instant.now(),
                TraceIdResolver.resolve()
            );
        }

        // Handle SecurityException
        if (throwable instanceof SecurityException) {
            return new Result<>(
                CommonResultCode.FORBIDDEN.getCode(),
                throwable.getMessage(),
                null,
                false,
                Instant.now(),
                TraceIdResolver.resolve()
            );
        }

        // Default: Internal error
        return new Result<>(
            CommonResultCode.INTERNAL_ERROR.getCode(),
            throwable.getMessage() != null ? throwable.getMessage() : "Internal server error",
            null,
            false,
            Instant.now(),
            TraceIdResolver.resolve()
        );
    }

    /**
     * Get error code from exception
     * 从异常获取错误代码
     *
     * @param throwable the exception | 异常
     * @return the error code | 错误代码
     */
    public static String getCode(Throwable throwable) {
        if (throwable instanceof OpenWebException webEx) {
            return webEx.getCode();
        }
        if (throwable instanceof IllegalArgumentException) {
            return CommonResultCode.PARAM_INVALID.getCode();
        }
        if (throwable instanceof IllegalStateException) {
            return CommonResultCode.BUSINESS_ERROR.getCode();
        }
        if (throwable instanceof NullPointerException) {
            return CommonResultCode.PARAM_MISSING.getCode();
        }
        if (throwable instanceof SecurityException) {
            return CommonResultCode.FORBIDDEN.getCode();
        }
        return CommonResultCode.INTERNAL_ERROR.getCode();
    }

    /**
     * Get HTTP status from exception
     * 从异常获取HTTP状态
     *
     * @param throwable the exception | 异常
     * @return the HTTP status | HTTP状态
     */
    public static int getHttpStatus(Throwable throwable) {
        if (throwable instanceof OpenWebException webEx) {
            return webEx.getHttpStatus();
        }
        if (throwable instanceof IllegalArgumentException) {
            return CommonResultCode.PARAM_INVALID.getHttpStatus();
        }
        if (throwable instanceof IllegalStateException) {
            return CommonResultCode.BUSINESS_ERROR.getHttpStatus();
        }
        if (throwable instanceof NullPointerException) {
            return CommonResultCode.PARAM_MISSING.getHttpStatus();
        }
        if (throwable instanceof SecurityException) {
            return CommonResultCode.FORBIDDEN.getHttpStatus();
        }
        return CommonResultCode.INTERNAL_ERROR.getHttpStatus();
    }
}

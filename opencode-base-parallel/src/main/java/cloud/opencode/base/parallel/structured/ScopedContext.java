package cloud.opencode.base.parallel.structured;

import cloud.opencode.base.parallel.exception.OpenParallelException;

import java.util.concurrent.Callable;

/**
 * Scoped Context - Scoped Values Context Management (JDK 25 JEP 501)
 * 作用域上下文 - 作用域值上下文管理 (JDK 25 JEP 501)
 *
 * <p>Provides scoped values for context propagation in structured concurrency.
 * ScopedValue is a modern replacement for ThreadLocal with better performance
 * and automatic cleanup.</p>
 * <p>为结构化并发中的上下文传播提供作用域值。ScopedValue 是 ThreadLocal 的现代替代，
 * 具有更好的性能和自动清理。</p>
 *
 * <p><strong>Features | 特性:</strong></p>
 * <ul>
 *   <li>Immutable values within scope - 作用域内值不可变</li>
 *   <li>Automatic inheritance by child virtual threads - 子虚拟线程自动继承</li>
 *   <li>No memory leaks - scope ends, value is released - 无内存泄漏 - 作用域结束，值释放</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Bind trace ID and execute
 * ScopedContext.runWithTraceId("trace-123", () -> {
 *     String traceId = ScopedContext.getTraceId();
 *     // All child threads inherit the trace ID
 * });
 *
 * // Multiple bindings
 * ScopedContext.run("trace-123", "user-456", () -> {
 *     // Both trace ID and user ID available
 * });
 * }</pre>
 *
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ScopedValue is inherently thread-safe) - 线程安全: 是（ScopedValue天然线程安全）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-parallel V1.0.0
 */
public final class ScopedContext {

    /** Trace ID scoped value - 追踪 ID 作用域值 */
    public static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();

    /** User ID scoped value - 用户 ID 作用域值 */
    public static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

    /** Tenant ID scoped value - 租户 ID 作用域值 */
    public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    /** Request ID scoped value - 请求 ID 作用域值 */
    public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    private ScopedContext() {
        // Static utility class
    }

    // ==================== Trace ID ====================

    /**
     * Runs a task with trace ID bound.
     * 使用绑定的追踪 ID 运行任务。
     *
     * @param traceId the trace ID - 追踪 ID
     * @param task    the task - 任务
     */
    public static void runWithTraceId(String traceId, Runnable task) {
        ScopedValue.where(TRACE_ID, traceId).run(task);
    }

    /**
     * Calls a task with trace ID bound.
     * 使用绑定的追踪 ID 调用任务。
     *
     * @param traceId the trace ID - 追踪 ID
     * @param task    the task - 任务
     * @param <T>     the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> T callWithTraceId(String traceId, Callable<T> task) {
        try {
            return ScopedValue.where(TRACE_ID, traceId).call(task::call);
        } catch (Exception e) {
            throw new OpenParallelException("Scoped execution failed", e);
        }
    }

    /**
     * Gets the current trace ID.
     * 获取当前追踪 ID。
     *
     * @return the trace ID or null - 追踪 ID 或 null
     */
    public static String getTraceId() {
        return TRACE_ID.orElse(null);
    }

    /**
     * Gets the current trace ID or default.
     * 获取当前追踪 ID 或默认值。
     *
     * @param defaultValue the default value - 默认值
     * @return the trace ID or default - 追踪 ID 或默认值
     */
    public static String getTraceIdOrDefault(String defaultValue) {
        return TRACE_ID.orElse(defaultValue);
    }

    // ==================== User ID ====================

    /**
     * Runs a task with user ID bound.
     * 使用绑定的用户 ID 运行任务。
     *
     * @param userId the user ID - 用户 ID
     * @param task   the task - 任务
     */
    public static void runWithUserId(String userId, Runnable task) {
        ScopedValue.where(USER_ID, userId).run(task);
    }

    /**
     * Calls a task with user ID bound.
     * 使用绑定的用户 ID 调用任务。
     *
     * @param userId the user ID - 用户 ID
     * @param task   the task - 任务
     * @param <T>    the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> T callWithUserId(String userId, Callable<T> task) {
        try {
            return ScopedValue.where(USER_ID, userId).call(task::call);
        } catch (Exception e) {
            throw new OpenParallelException("Scoped execution failed", e);
        }
    }

    /**
     * Gets the current user ID.
     * 获取当前用户 ID。
     *
     * @return the user ID or null - 用户 ID 或 null
     */
    public static String getUserId() {
        return USER_ID.orElse(null);
    }

    // ==================== Tenant ID ====================

    /**
     * Runs a task with tenant ID bound.
     * 使用绑定的租户 ID 运行任务。
     *
     * @param tenantId the tenant ID - 租户 ID
     * @param task     the task - 任务
     */
    public static void runWithTenantId(String tenantId, Runnable task) {
        ScopedValue.where(TENANT_ID, tenantId).run(task);
    }

    /**
     * Gets the current tenant ID.
     * 获取当前租户 ID。
     *
     * @return the tenant ID or null - 租户 ID 或 null
     */
    public static String getTenantId() {
        return TENANT_ID.orElse(null);
    }

    // ==================== Request ID ====================

    /**
     * Runs a task with request ID bound.
     * 使用绑定的请求 ID 运行任务。
     *
     * @param requestId the request ID - 请求 ID
     * @param task      the task - 任务
     */
    public static void runWithRequestId(String requestId, Runnable task) {
        ScopedValue.where(REQUEST_ID, requestId).run(task);
    }

    /**
     * Gets the current request ID.
     * 获取当前请求 ID。
     *
     * @return the request ID or null - 请求 ID 或 null
     */
    public static String getRequestId() {
        return REQUEST_ID.orElse(null);
    }

    // ==================== Multiple Bindings ====================

    /**
     * Runs a task with trace ID and user ID bound.
     * 使用绑定的追踪 ID 和用户 ID 运行任务。
     *
     * @param traceId the trace ID - 追踪 ID
     * @param userId  the user ID - 用户 ID
     * @param task    the task - 任务
     */
    public static void run(String traceId, String userId, Runnable task) {
        ScopedValue.where(TRACE_ID, traceId)
                .where(USER_ID, userId)
                .run(task);
    }

    /**
     * Calls a task with trace ID and user ID bound.
     * 使用绑定的追踪 ID 和用户 ID 调用任务。
     *
     * @param traceId the trace ID - 追踪 ID
     * @param userId  the user ID - 用户 ID
     * @param task    the task - 任务
     * @param <T>     the result type - 结果类型
     * @return the result - 结果
     */
    public static <T> T call(String traceId, String userId, Callable<T> task) {
        try {
            return ScopedValue.where(TRACE_ID, traceId)
                    .where(USER_ID, userId)
                    .call(task::call);
        } catch (Exception e) {
            throw new OpenParallelException("Scoped execution failed", e);
        }
    }

    /**
     * Runs a task with trace ID, user ID, and tenant ID bound.
     * 使用绑定的追踪 ID、用户 ID 和租户 ID 运行任务。
     *
     * @param traceId  the trace ID - 追踪 ID
     * @param userId   the user ID - 用户 ID
     * @param tenantId the tenant ID - 租户 ID
     * @param task     the task - 任务
     */
    public static void run(String traceId, String userId, String tenantId, Runnable task) {
        ScopedValue.where(TRACE_ID, traceId)
                .where(USER_ID, userId)
                .where(TENANT_ID, tenantId)
                .run(task);
    }

    // ==================== Custom Scoped Values ====================

    /**
     * Creates a new scoped value.
     * 创建新的作用域值。
     *
     * @param <T> the value type - 值类型
     * @return the scoped value - 作用域值
     */
    public static <T> ScopedValue<T> newScopedValue() {
        return ScopedValue.newInstance();
    }

    /**
     * Runs a task with a custom scoped value bound.
     * 使用绑定的自定义作用域值运行任务。
     *
     * @param scopedValue the scoped value - 作用域值
     * @param value       the value to bind - 要绑定的值
     * @param task        the task - 任务
     * @param <T>         the value type - 值类型
     */
    public static <T> void runWith(ScopedValue<T> scopedValue, T value, Runnable task) {
        ScopedValue.where(scopedValue, value).run(task);
    }

    /**
     * Calls a task with a custom scoped value bound.
     * 使用绑定的自定义作用域值调用任务。
     *
     * @param scopedValue the scoped value - 作用域值
     * @param value       the value to bind - 要绑定的值
     * @param task        the task - 任务
     * @param <T>         the value type - 值类型
     * @param <R>         the result type - 结果类型
     * @return the result - 结果
     */
    public static <T, R> R callWith(ScopedValue<T> scopedValue, T value, Callable<R> task) {
        try {
            return ScopedValue.where(scopedValue, value).call(task::call);
        } catch (Exception e) {
            throw new OpenParallelException("Scoped execution failed", e);
        }
    }

    /**
     * Checks if a scoped value is bound.
     * 检查作用域值是否已绑定。
     *
     * @param scopedValue the scoped value - 作用域值
     * @param <T>         the value type - 值类型
     * @return true if bound - 如果已绑定返回 true
     */
    public static <T> boolean isBound(ScopedValue<T> scopedValue) {
        return scopedValue.isBound();
    }

    /**
     * Gets a scoped value or null.
     * 获取作用域值或 null。
     *
     * @param scopedValue the scoped value - 作用域值
     * @param <T>         the value type - 值类型
     * @return the value or null - 值或 null
     */
    public static <T> T getOrNull(ScopedValue<T> scopedValue) {
        return scopedValue.orElse(null);
    }
}

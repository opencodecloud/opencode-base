package cloud.opencode.base.log.context;

import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Log Context - Unified Context Management
 * 日志上下文 - 统一上下文管理
 *
 * <p>LogContext provides a unified API for managing log context information
 * such as trace IDs, request IDs, and user IDs. It integrates with MDC
 * and provides convenient methods for common context operations.</p>
 * <p>LogContext 提供统一的 API 来管理日志上下文信息，
 * 如追踪 ID、请求 ID 和用户 ID。它与 MDC 集成，并为常见上下文操作提供便捷方法。</p>
 *
 * <p><strong>Standard Keys | 标准键:</strong></p>
 * <ul>
 *   <li>traceId - Distributed trace ID - 分布式追踪 ID</li>
 *   <li>requestId - HTTP request ID - HTTP 请求 ID</li>
 *   <li>userId - User identifier - 用户标识符</li>
 *   <li>tenantId - Multi-tenant ID - 多租户 ID</li>
 * </ul>
 *
 * <p><strong>Example | 示例:</strong></p>
 * <pre>{@code
 * // Set standard context
 * LogContext.setTraceId("trace-12345");
 * LogContext.setUserId("user-001");
 *
 * try {
 *     OpenLog.info("Processing request");  // Includes traceId and userId
 * } finally {
 *     LogContext.clear();
 * }
 *
 * // Snapshot for async propagation
 * ContextSnapshot snapshot = LogContext.snapshot();
 * executor.submit(() -> {
 *     snapshot.runWith(() -> {
 *         OpenLog.info("Async task");  // Preserves context
 *     });
 * });
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Unified context management for trace/request/user/tenant IDs - 统一的追踪/请求/用户/租户 ID 上下文管理</li>
 *   <li>Context snapshot for async propagation - 用于异步传播的上下文快照</li>
 *   <li>Automatic context restore after async execution - 异步执行后自动恢复上下文</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (delegates to thread-safe MDC) - 线程安全: 是（委托给线程安全的 MDC）</li>
 *   <li>Null-safe: Yes (returns null for missing keys) - 空值安全: 是（缺少的键返回 null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class LogContext {

    // Standard context keys
    public static final String KEY_TRACE_ID = "traceId";
    public static final String KEY_REQUEST_ID = "requestId";
    public static final String KEY_USER_ID = "userId";
    public static final String KEY_TENANT_ID = "tenantId";
    public static final String KEY_SPAN_ID = "spanId";
    public static final String KEY_PARENT_SPAN_ID = "parentSpanId";

    private LogContext() {
        // Utility class
    }

    // ==================== Standard Context Operations ====================

    /**
     * Sets the trace ID.
     * 设置追踪 ID。
     *
     * @param traceId the trace ID - 追踪 ID
     */
    public static void setTraceId(String traceId) {
        MDC.put(KEY_TRACE_ID, traceId);
    }

    /**
     * Gets the trace ID.
     * 获取追踪 ID。
     *
     * @return the trace ID - 追踪 ID
     */
    public static String getTraceId() {
        return MDC.get(KEY_TRACE_ID);
    }

    /**
     * Sets the request ID.
     * 设置请求 ID。
     *
     * @param requestId the request ID - 请求 ID
     */
    public static void setRequestId(String requestId) {
        MDC.put(KEY_REQUEST_ID, requestId);
    }

    /**
     * Gets the request ID.
     * 获取请求 ID。
     *
     * @return the request ID - 请求 ID
     */
    public static String getRequestId() {
        return MDC.get(KEY_REQUEST_ID);
    }

    /**
     * Sets the user ID.
     * 设置用户 ID。
     *
     * @param userId the user ID - 用户 ID
     */
    public static void setUserId(String userId) {
        MDC.put(KEY_USER_ID, userId);
    }

    /**
     * Gets the user ID.
     * 获取用户 ID。
     *
     * @return the user ID - 用户 ID
     */
    public static String getUserId() {
        return MDC.get(KEY_USER_ID);
    }

    /**
     * Sets the tenant ID.
     * 设置租户 ID。
     *
     * @param tenantId the tenant ID - 租户 ID
     */
    public static void setTenantId(String tenantId) {
        MDC.put(KEY_TENANT_ID, tenantId);
    }

    /**
     * Gets the tenant ID.
     * 获取租户 ID。
     *
     * @return the tenant ID - 租户 ID
     */
    public static String getTenantId() {
        return MDC.get(KEY_TENANT_ID);
    }

    /**
     * Sets a custom context value.
     * 设置自定义上下文值。
     *
     * @param key   the key - 键
     * @param value the value - 值
     */
    public static void set(String key, String value) {
        MDC.put(key, value);
    }

    /**
     * Gets a custom context value.
     * 获取自定义上下文值。
     *
     * @param key the key - 键
     * @return the value - 值
     */
    public static String get(String key) {
        return MDC.get(key);
    }

    /**
     * Clears all context.
     * 清空所有上下文。
     */
    public static void clear() {
        MDC.clear();
    }

    /**
     * Gets all context values.
     * 获取所有上下文值。
     *
     * @return the context map - 上下文映射
     */
    public static Map<String, String> getAll() {
        return MDC.getCopyOfContextMap();
    }

    // ==================== Snapshot Operations ====================

    /**
     * Creates a snapshot of the current context.
     * 创建当前上下文的快照。
     *
     * @return the context snapshot - 上下文快照
     */
    public static ContextSnapshot snapshot() {
        return new ContextSnapshot(MDC.getCopyOfContextMap());
    }

    /**
     * Applies a context snapshot.
     * 应用上下文快照。
     *
     * @param snapshot the snapshot to apply - 要应用的快照
     */
    public static void apply(ContextSnapshot snapshot) {
        if (snapshot != null) {
            MDC.setContextMap(snapshot.mdcContext());
        }
    }

    /**
     * Context Snapshot - Immutable snapshot for async propagation.
     * 上下文快照 - 用于异步传播的不可变快照。
     */
    public static final class ContextSnapshot {
        private final Map<String, String> mdcContext;

        /**
         * Creates a new context snapshot.
         * 创建新的上下文快照。
         *
         * @param mdcContext the MDC context - MDC 上下文
         */
        public ContextSnapshot(Map<String, String> mdcContext) {
            this.mdcContext = mdcContext != null ? Map.copyOf(mdcContext) : Map.of();
        }

        /**
         * Returns the MDC context.
         * 返回 MDC 上下文。
         *
         * @return the MDC context - MDC 上下文
         */
        public Map<String, String> mdcContext() {
            return mdcContext;
        }

        /**
         * Executes a task with this context.
         * 使用此上下文执行任务。
         *
         * @param runnable the task - 任务
         */
        public void runWith(Runnable runnable) {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                MDC.setContextMap(mdcContext);
                runnable.run();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        }

        /**
         * Executes a task with this context and returns the result.
         * 使用此上下文执行任务并返回结果。
         *
         * @param supplier the task - 任务
         * @param <T>      the result type - 结果类型
         * @return the result - 结果
         */
        public <T> T callWith(Supplier<T> supplier) {
            Map<String, String> previous = MDC.getCopyOfContextMap();
            try {
                MDC.setContextMap(mdcContext);
                return supplier.get();
            } finally {
                if (previous != null) {
                    MDC.setContextMap(previous);
                } else {
                    MDC.clear();
                }
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof ContextSnapshot that)) return false;
            return Objects.equals(mdcContext, that.mdcContext);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mdcContext);
        }

        @Override
        public String toString() {
            return "ContextSnapshot[mdcContext=" + mdcContext + "]";
        }
    }
}

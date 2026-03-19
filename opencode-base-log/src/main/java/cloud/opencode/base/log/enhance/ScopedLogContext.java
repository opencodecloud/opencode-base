/*
 * Copyright 2025 OpenCode Cloud Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.log.enhance;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * Scoped Log Context - JDK 25+ ScopedValue-based Context
 * 作用域日志上下文 - 基于 JDK 25+ ScopedValue 的上下文
 *
 * <p>Provides log context management using ScopedValue, which is more efficient
 * and suitable for Virtual Threads than traditional ThreadLocal-based MDC.</p>
 * <p>使用 ScopedValue 提供日志上下文管理，比传统基于 ThreadLocal 的 MDC
 * 更高效且更适合虚拟线程。</p>
 *
 * <p><strong>Advantages over ThreadLocal | 相比 ThreadLocal 的优势:</strong></p>
 * <ul>
 *   <li>Automatic inheritance to child Virtual Threads - 自动继承到子虚拟线程</li>
 *   <li>Immutable within scope - 作用域内不可变</li>
 *   <li>No explicit cleanup needed - 无需显式清理</li>
 *   <li>Better performance with Virtual Threads - 虚拟线程性能更好</li>
 * </ul>
 *
 * <p><strong>Usage Example | 使用示例:</strong></p>
 * <pre>{@code
 * // Bind and run
 * ScopedLogContext.where(ScopedLogContext.TRACE_ID, "trace-123")
 *     .and(ScopedLogContext.USER_ID, "user-456")
 *     .run(() -> {
 *         // Context is available here
 *         OpenLog.info("Processing request");
 *
 *         // Nested scope
 *         ScopedLogContext.where(ScopedLogContext.REQUEST_ID, "req-789")
 *             .run(() -> {
 *                 OpenLog.info("Nested processing");
 *             });
 *     });
 * }</pre>
 *
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>ScopedValue-based context (JDK 25+) - 基于 ScopedValue 的上下文（JDK 25+）</li>
 *   <li>Automatic inheritance to child virtual threads - 自动继承到子虚拟线程</li>
 *   <li>Immutable within scope, no cleanup needed - 作用域内不可变，无需清理</li>
 *   <li>Fluent carrier API for multiple bindings - 流式载体 API 支持多个绑定</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ScopedValue is inherently thread-safe) - 线程安全: 是（ScopedValue 本身线程安全）</li>
 *   <li>Null-safe: No (throws on null bindings) - 空值安全: 否（null 绑定抛异常）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see java.lang.ScopedValue
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.0
 */
public final class ScopedLogContext {

    // ==================== Standard Context Keys | 标准上下文键 ====================

    /**
     * Trace ID for distributed tracing
     * 分布式追踪的追踪 ID
     */
    public static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();

    /**
     * User ID for user identification
     * 用户标识的用户 ID
     */
    public static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

    /**
     * Request ID for request tracking
     * 请求追踪的请求 ID
     */
    public static final ScopedValue<String> REQUEST_ID = ScopedValue.newInstance();

    /**
     * Tenant ID for multi-tenant applications
     * 多租户应用的租户 ID
     */
    public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();

    /**
     * Session ID for session tracking
     * 会话追踪的会话 ID
     */
    public static final ScopedValue<String> SESSION_ID = ScopedValue.newInstance();

    /**
     * Span ID for distributed tracing spans
     * 分布式追踪跨度的跨度 ID
     */
    public static final ScopedValue<String> SPAN_ID = ScopedValue.newInstance();

    /**
     * Operation name for current operation
     * 当前操作的操作名
     */
    public static final ScopedValue<String> OPERATION = ScopedValue.newInstance();

    private ScopedLogContext() {
    }

    // ==================== Context Binding | 上下文绑定 ====================

    /**
     * Creates a carrier with a single binding
     * 创建带单个绑定的载体
     *
     * @param key   the scoped value key | 作用域值键
     * @param value the value to bind | 要绑定的值
     * @return carrier for chaining | 用于链式调用的载体
     */
    public static Carrier where(ScopedValue<String> key, String value) {
        return new Carrier(ScopedValue.where(key, value));
    }

    /**
     * Creates a carrier with multiple bindings from a map
     * 从映射创建带多个绑定的载体
     *
     * @param bindings the key-value bindings | 键值绑定
     * @return carrier for chaining | 用于链式调用的载体
     */
    @SuppressWarnings("unchecked")
    public static Carrier where(Map<ScopedValue<String>, String> bindings) {
        Objects.requireNonNull(bindings, "bindings must not be null");
        if (bindings.isEmpty()) {
            throw new IllegalArgumentException("bindings must not be empty");
        }

        ScopedValue.Carrier carrier = null;
        for (Map.Entry<ScopedValue<String>, String> entry : bindings.entrySet()) {
            if (carrier == null) {
                carrier = ScopedValue.where(entry.getKey(), entry.getValue());
            } else {
                carrier = carrier.where(entry.getKey(), entry.getValue());
            }
        }
        return new Carrier(carrier);
    }

    // ==================== Convenience Methods | 便捷方法 ====================

    /**
     * Runs a task with trace ID and user ID
     * 使用追踪 ID 和用户 ID 运行任务
     *
     * @param traceId the trace ID | 追踪 ID
     * @param userId  the user ID | 用户 ID
     * @param task    the task to run | 要运行的任务
     */
    public static void run(String traceId, String userId, Runnable task) {
        where(TRACE_ID, traceId)
                .and(USER_ID, userId)
                .run(task);
    }

    /**
     * Calls a task with trace ID and user ID
     * 使用追踪 ID 和用户 ID 调用任务
     *
     * @param traceId the trace ID | 追踪 ID
     * @param userId  the user ID | 用户 ID
     * @param task    the task to call | 要调用的任务
     * @param <T>     the result type | 结果类型
     * @return the result | 结果
     * @throws Exception if task fails | 如果任务失败
     */
    public static <T> T call(String traceId, String userId, Callable<T> task) throws Exception {
        return where(TRACE_ID, traceId)
                .and(USER_ID, userId)
                .call(task);
    }

    // ==================== Context Access | 上下文访问 ====================

    /**
     * Gets the current trace ID
     * 获取当前追踪 ID
     *
     * @return trace ID or null if not set | 追踪 ID，未设置时返回 null
     */
    public static String getTraceId() {
        return TRACE_ID.isBound() ? TRACE_ID.get() : null;
    }

    /**
     * Gets the current user ID
     * 获取当前用户 ID
     *
     * @return user ID or null if not set | 用户 ID，未设置时返回 null
     */
    public static String getUserId() {
        return USER_ID.isBound() ? USER_ID.get() : null;
    }

    /**
     * Gets the current request ID
     * 获取当前请求 ID
     *
     * @return request ID or null if not set | 请求 ID，未设置时返回 null
     */
    public static String getRequestId() {
        return REQUEST_ID.isBound() ? REQUEST_ID.get() : null;
    }

    /**
     * Gets the current tenant ID
     * 获取当前租户 ID
     *
     * @return tenant ID or null if not set | 租户 ID，未设置时返回 null
     */
    public static String getTenantId() {
        return TENANT_ID.isBound() ? TENANT_ID.get() : null;
    }

    /**
     * Gets the current session ID
     * 获取当前会话 ID
     *
     * @return session ID or null if not set | 会话 ID，未设置时返回 null
     */
    public static String getSessionId() {
        return SESSION_ID.isBound() ? SESSION_ID.get() : null;
    }

    /**
     * Gets the current span ID
     * 获取当前跨度 ID
     *
     * @return span ID or null if not set | 跨度 ID，未设置时返回 null
     */
    public static String getSpanId() {
        return SPAN_ID.isBound() ? SPAN_ID.get() : null;
    }

    /**
     * Gets the current operation name
     * 获取当前操作名
     *
     * @return operation name or null if not set | 操作名，未设置时返回 null
     */
    public static String getOperation() {
        return OPERATION.isBound() ? OPERATION.get() : null;
    }

    /**
     * Gets a value as Optional
     * 获取值作为 Optional
     *
     * @param key the scoped value key | 作用域值键
     * @return optional containing value or empty | 包含值的 Optional 或空
     */
    public static Optional<String> get(ScopedValue<String> key) {
        return key.isBound() ? Optional.of(key.get()) : Optional.empty();
    }

    // ==================== Carrier | 载体 ====================

    /**
     * Carrier for binding multiple values and executing tasks
     * 用于绑定多个值和执行任务的载体
     */
    public static final class Carrier {
        private final ScopedValue.Carrier carrier;

        Carrier(ScopedValue.Carrier carrier) {
            this.carrier = carrier;
        }

        /**
         * Adds another binding
         * 添加另一个绑定
         *
         * @param key   the scoped value key | 作用域值键
         * @param value the value to bind | 要绑定的值
         * @return this carrier for chaining | 用于链式调用的此载体
         */
        public Carrier and(ScopedValue<String> key, String value) {
            return new Carrier(carrier.where(key, value));
        }

        /**
         * Runs a task in this context
         * 在此上下文中运行任务
         *
         * @param task the task to run | 要运行的任务
         */
        public void run(Runnable task) {
            carrier.run(task);
        }

        /**
         * Calls a task in this context
         * 在此上下文中调用任务
         *
         * @param task the task to call | 要调用的任务
         * @param <T>  the result type | 结果类型
         * @return the result | 结果
         * @throws Exception if task fails | 如果任务失败
         */
        public <T> T call(Callable<T> task) throws Exception {
            return carrier.call(task::call);
        }

        /**
         * Gets the underlying carrier
         * 获取底层载体
         *
         * @return the ScopedValue carrier | ScopedValue 载体
         */
        public ScopedValue.Carrier underlying() {
            return carrier;
        }
    }
}

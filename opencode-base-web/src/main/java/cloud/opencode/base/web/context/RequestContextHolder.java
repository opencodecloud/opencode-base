package cloud.opencode.base.web.context;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * Request Context Holder
 * 请求上下文持有者
 *
 * <p>ThreadLocal-based holder for request context.</p>
 * <p>基于ThreadLocal的请求上下文持有者。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe context storage - 线程安全的上下文存储</li>
 *   <li>Async task support - 异步任务支持</li>
 *   <li>Auto cleanup - 自动清理</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Set context
 * RequestContextHolder.setContext(context);
 *
 * // Get context
 * RequestContext ctx = RequestContextHolder.getContext();
 * String traceId = RequestContextHolder.getTraceId();
 *
 * // Wrap async task with context
 * Runnable wrapped = RequestContextHolder.wrap(task);
 * }</pre>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class RequestContextHolder {

    private static final ThreadLocal<RequestContext> CONTEXT_HOLDER = new ThreadLocal<>();

    private RequestContextHolder() {
        // Utility class
    }

    /**
     * Set the request context
     * 设置请求上下文
     *
     * @param context the context to set | 要设置的上下文
     */
    public static void setContext(RequestContext context) {
        if (context == null) {
            CONTEXT_HOLDER.remove();
        } else {
            CONTEXT_HOLDER.set(context);
        }
    }

    /**
     * Get the request context
     * 获取请求上下文
     *
     * @return the current context or null | 当前上下文或null
     */
    public static RequestContext getContext() {
        return CONTEXT_HOLDER.get();
    }

    /**
     * Get the request context, creating if absent
     * 获取请求上下文，如果不存在则创建
     *
     * @param supplier the supplier to create context | 创建上下文的供应者
     * @return the context | 上下文
     */
    public static RequestContext getOrCreate(Supplier<RequestContext> supplier) {
        RequestContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            context = supplier.get();
            CONTEXT_HOLDER.set(context);
        }
        return context;
    }

    /**
     * Clear the request context
     * 清除请求上下文
     */
    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    /**
     * Check if context exists
     * 检查上下文是否存在
     *
     * @return true if exists | 如果存在返回true
     */
    public static boolean hasContext() {
        return CONTEXT_HOLDER.get() != null;
    }

    /**
     * Get trace ID from current context
     * 从当前上下文获取追踪ID
     *
     * @return the trace ID or null | 追踪ID或null
     */
    public static String getTraceId() {
        RequestContext context = CONTEXT_HOLDER.get();
        return context != null ? context.traceId() : null;
    }

    /**
     * Get user context from current context
     * 从当前上下文获取用户上下文
     *
     * @return the user context or null | 用户上下文或null
     */
    public static UserContext getUser() {
        RequestContext context = CONTEXT_HOLDER.get();
        return context != null ? context.user() : null;
    }

    /**
     * Get user ID from current context
     * 从当前上下文获取用户ID
     *
     * @return the user ID or null | 用户ID或null
     */
    public static String getUserId() {
        UserContext user = getUser();
        return user != null ? user.userId() : null;
    }

    // === Async Support ===

    /**
     * Wrap runnable with current context
     * 使用当前上下文包装Runnable
     *
     * @param runnable the runnable to wrap | 要包装的Runnable
     * @return the wrapped runnable | 包装后的Runnable
     */
    public static Runnable wrap(Runnable runnable) {
        RequestContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            return runnable;
        }
        return () -> {
            RequestContext previous = CONTEXT_HOLDER.get();
            try {
                CONTEXT_HOLDER.set(context);
                runnable.run();
            } finally {
                if (previous == null) {
                    CONTEXT_HOLDER.remove();
                } else {
                    CONTEXT_HOLDER.set(previous);
                }
            }
        };
    }

    /**
     * Wrap callable with current context
     * 使用当前上下文包装Callable
     *
     * @param callable the callable to wrap | 要包装的Callable
     * @param <T> the result type | 结果类型
     * @return the wrapped callable | 包装后的Callable
     */
    public static <T> Callable<T> wrap(Callable<T> callable) {
        RequestContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            return callable;
        }
        return () -> {
            RequestContext previous = CONTEXT_HOLDER.get();
            try {
                CONTEXT_HOLDER.set(context);
                return callable.call();
            } finally {
                if (previous == null) {
                    CONTEXT_HOLDER.remove();
                } else {
                    CONTEXT_HOLDER.set(previous);
                }
            }
        };
    }

    /**
     * Wrap supplier with current context
     * 使用当前上下文包装Supplier
     *
     * @param supplier the supplier to wrap | 要包装的Supplier
     * @param <T> the result type | 结果类型
     * @return the wrapped supplier | 包装后的Supplier
     */
    public static <T> Supplier<T> wrap(Supplier<T> supplier) {
        RequestContext context = CONTEXT_HOLDER.get();
        if (context == null) {
            return supplier;
        }
        return () -> {
            RequestContext previous = CONTEXT_HOLDER.get();
            try {
                CONTEXT_HOLDER.set(context);
                return supplier.get();
            } finally {
                if (previous == null) {
                    CONTEXT_HOLDER.remove();
                } else {
                    CONTEXT_HOLDER.set(previous);
                }
            }
        };
    }

    /**
     * Execute runnable with context
     * 使用上下文执行Runnable
     *
     * @param context the context to use | 要使用的上下文
     * @param runnable the runnable to execute | 要执行的Runnable
     */
    public static void execute(RequestContext context, Runnable runnable) {
        RequestContext previous = CONTEXT_HOLDER.get();
        try {
            CONTEXT_HOLDER.set(context);
            runnable.run();
        } finally {
            if (previous == null) {
                CONTEXT_HOLDER.remove();
            } else {
                CONTEXT_HOLDER.set(previous);
            }
        }
    }

    /**
     * Execute supplier with context
     * 使用上下文执行Supplier
     *
     * @param context the context to use | 要使用的上下文
     * @param supplier the supplier to execute | 要执行的Supplier
     * @param <T> the result type | 结果类型
     * @return the result | 结果
     */
    public static <T> T execute(RequestContext context, Supplier<T> supplier) {
        RequestContext previous = CONTEXT_HOLDER.get();
        try {
            CONTEXT_HOLDER.set(context);
            return supplier.get();
        } finally {
            if (previous == null) {
                CONTEXT_HOLDER.remove();
            } else {
                CONTEXT_HOLDER.set(previous);
            }
        }
    }
}

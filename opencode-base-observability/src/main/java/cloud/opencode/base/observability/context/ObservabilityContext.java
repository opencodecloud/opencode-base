package cloud.opencode.base.observability.context;

import cloud.opencode.base.observability.exception.ObservabilityException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Thread-local observability context carrying trace ID, span ID, and baggage.
 * 携带 trace ID、span ID 和 baggage 的线程局部可观测性上下文。
 *
 * <p>Provides context propagation across threads via {@link #wrap(Runnable)} and
 * {@link #wrap(Callable)}. The {@link #attach()} method sets this context as the
 * current thread's context, returning a {@link Scope} that restores the previous
 * context when closed. Contexts are immutable; methods like {@link #withSpanId} and
 * {@link #withBaggage} return new instances.</p>
 * <p>通过 {@link #wrap(Runnable)} 和 {@link #wrap(Callable)} 提供跨线程的上下文传播。
 * {@link #attach()} 方法将此上下文设置为当前线程的上下文，
 * 返回一个在关闭时恢复先前上下文的 {@link Scope}。上下文是不可变的；
 * {@link #withSpanId} 和 {@link #withBaggage} 等方法返回新实例。</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.3
 */
public final class ObservabilityContext {

    private static final ThreadLocal<ObservabilityContext> CURRENT = new ThreadLocal<>();

    private final String traceId;
    private final String spanId;
    private final Map<String, String> baggage;

    private ObservabilityContext(String traceId, String spanId, Map<String, String> baggage) {
        if (traceId == null || traceId.isBlank()) {
            throw new ObservabilityException("INVALID_CONTEXT", "traceId must not be null or blank");
        }
        if (spanId == null || spanId.isBlank()) {
            throw new ObservabilityException("INVALID_CONTEXT", "spanId must not be null or blank");
        }
        this.traceId = traceId;
        this.spanId = spanId;
        this.baggage = baggage == null ? Map.of() : Map.copyOf(baggage);
    }

    /**
     * Returns the current thread's observability context, or {@code null} if none is attached.
     * 返回当前线程的可观测性上下文，如果没有附加则返回 {@code null}。
     *
     * <p><strong>Null-safety | 空值安全:</strong> This method may return {@code null}.
     * Always check before use to avoid {@link NullPointerException}:</p>
     * <pre>{@code
     * ObservabilityContext ctx = ObservabilityContext.current();
     * if (ctx != null) {
     *     String traceId = ctx.traceId();
     * }
     * }</pre>
     * <p>此方法可能返回 {@code null}，使用前请检查。</p>
     *
     * @return the current context, or null | 当前上下文，或 null
     */
    public static ObservabilityContext current() {
        return CURRENT.get();
    }

    /**
     * Creates a new context with the given trace ID and an auto-generated span ID.
     * 使用给定的 trace ID 和自动生成的 span ID 创建新上下文。
     *
     * @param traceId the trace identifier | 追踪标识符
     * @return a new context | 新上下文
     * @throws ObservabilityException if traceId is null or blank | 如果 traceId 为 null 或空白
     */
    public static ObservabilityContext create(String traceId) {
        return new ObservabilityContext(traceId, generateSpanId(), Map.of());
    }

    /**
     * Creates a new context with the given trace ID and span ID.
     * 使用给定的 trace ID 和 span ID 创建新上下文。
     *
     * @param traceId the trace identifier | 追踪标识符
     * @param spanId  the span identifier | span 标识符
     * @return a new context | 新上下文
     * @throws ObservabilityException if traceId or spanId is null or blank |
     *                                如果 traceId 或 spanId 为 null 或空白
     */
    public static ObservabilityContext create(String traceId, String spanId) {
        return new ObservabilityContext(traceId, spanId, Map.of());
    }

    /**
     * Clears the current thread's observability context.
     * 清除当前线程的可观测性上下文。
     */
    public static void clear() {
        CURRENT.remove();
    }

    private static String generateSpanId() {
        // ThreadLocalRandom.nextLong() + Long.toHexString: 1 string allocation, no contention.
        // vs UUID.randomUUID().toString().replace().substring(): 4 string allocations + synchronized SecureRandom.
        // ThreadLocalRandom.nextLong() + Long.toHexString：1 次字符串分配，无竞争。
        // vs UUID.randomUUID()：4 次字符串分配 + 同步 SecureRandom。
        //
        // Security note: ThreadLocalRandom is NOT cryptographically secure. Span IDs generated
        // here are for diagnostic tracing only and MUST NOT be used for authentication,
        // authorization, or session binding. If cryptographic unpredictability is required
        // (e.g., multi-tenant trace isolation), use create(traceId, spanId) with a
        // SecureRandom-generated value instead.
        // 安全说明：ThreadLocalRandom 不是密码学安全的。此处生成的 Span ID 仅用于诊断追踪，
        // 不得用于认证、授权或会话绑定。
        long val = ThreadLocalRandom.current().nextLong();
        return Long.toHexString(val);
    }

    /**
     * Returns the trace ID.
     * 返回 trace ID。
     *
     * @return the trace identifier | 追踪标识符
     */
    public String traceId() {
        return traceId;
    }

    /**
     * Returns the span ID.
     * 返回 span ID。
     *
     * @return the span identifier | span 标识符
     */
    public String spanId() {
        return spanId;
    }

    /**
     * Returns the baggage value for the given key.
     * 返回给定键的 baggage 值。
     *
     * @param key the baggage key (null returns empty) | baggage 键（null 返回空）
     * @return an Optional containing the value, or empty | 包含值的 Optional，或空
     */
    public Optional<String> baggage(String key) {
        if (key == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(baggage.get(key));
    }

    /**
     * Returns all baggage as an unmodifiable map.
     * 以不可修改映射返回所有 baggage。
     *
     * @return the baggage map | baggage 映射
     */
    public Map<String, String> allBaggage() {
        return baggage;
    }

    /**
     * Returns a new context with the same trace ID and baggage but a different span ID.
     * 返回具有相同 trace ID 和 baggage 但不同 span ID 的新上下文。
     *
     * @param newSpanId the new span identifier | 新的 span 标识符
     * @return a new context with the updated span ID | 带有更新后 span ID 的新上下文
     * @throws ObservabilityException if newSpanId is null or blank | 如果 newSpanId 为 null 或空白
     */
    public ObservabilityContext withSpanId(String newSpanId) {
        return new ObservabilityContext(this.traceId, newSpanId, this.baggage);
    }

    /**
     * Returns a new context with an additional baggage entry.
     * 返回带有额外 baggage 条目的新上下文。
     *
     * @param key   the baggage key | baggage 键
     * @param value the baggage value | baggage 值
     * @return a new context with the added baggage | 带有添加的 baggage 的新上下文
     * @throws ObservabilityException if key is null/blank or value is null |
     *                                如果 key 为 null/空白或 value 为 null
     */
    public ObservabilityContext withBaggage(String key, String value) {
        if (key == null || key.isBlank()) {
            throw new ObservabilityException("INVALID_CONTEXT", "Baggage key must not be null or blank");
        }
        if (value == null) {
            throw new ObservabilityException("INVALID_CONTEXT", "Baggage value must not be null");
        }
        Map<String, String> newBaggage = new HashMap<>(this.baggage);
        newBaggage.put(key, value);
        return new ObservabilityContext(this.traceId, this.spanId, newBaggage);
    }

    /**
     * Attaches this context to the current thread, returning a {@link Scope} that
     * restores the previous context when closed.
     * 将此上下文附加到当前线程，返回一个在关闭时恢复先前上下文的 {@link Scope}。
     *
     * <p><strong>Security note | 安全注意事项:</strong> The returned {@link Scope} <em>must</em>
     * be closed (preferably via try-with-resources) to prevent context leakage across requests
     * in thread pools. Failing to close the Scope leaves sensitive baggage values (e.g.,
     * {@code user.id}, {@code session.token}) on the thread, where they may be inherited by
     * the next task reusing the same thread.</p>
     * <p>返回的 {@link Scope} <em>必须</em>关闭（推荐使用 try-with-resources），以防止在线程池中
     * 上下文跨请求泄漏。未关闭 Scope 会导致敏感 baggage 值（如 user.id、session.token）
     * 残留在线程上，可能被复用该线程的下一个任务继承。</p>
     *
     * <pre>{@code
     * // Correct usage | 正确用法:
     * try (ObservabilityContext.Scope scope = ctx.attach()) {
     *     // ... work ...
     * } // context automatically restored | 上下文自动恢复
     * }</pre>
     *
     * @return a scope that must be closed to restore the previous context |
     * 必须关闭以恢复先前上下文的 scope
     */
    public Scope attach() {
        ObservabilityContext previous = CURRENT.get();
        CURRENT.set(this);
        return new Scope(previous);
    }

    /**
     * Wraps a {@link Runnable} to propagate this context when executed.
     * 包装 {@link Runnable} 以在执行时传播此上下文。
     *
     * @param task the task to wrap | 要包装的任务
     * @return a wrapped runnable that propagates this context | 传播此上下文的包装 runnable
     * @throws ObservabilityException if task is null | 如果 task 为 null
     */
    public Runnable wrap(Runnable task) {
        if (task == null) {
            throw new ObservabilityException("INVALID_CONTEXT", "Task must not be null");
        }
        ObservabilityContext captured = this;
        return () -> {
            try (Scope ignored = captured.attach()) {
                task.run();
            }
        };
    }

    /**
     * Wraps a {@link Callable} to propagate this context when executed.
     * 包装 {@link Callable} 以在执行时传播此上下文。
     *
     * @param <T>  the callable return type | callable 返回类型
     * @param task the task to wrap | 要包装的任务
     * @return a wrapped callable that propagates this context | 传播此上下文的包装 callable
     * @throws ObservabilityException if task is null | 如果 task 为 null
     */
    public <T> Callable<T> wrap(Callable<T> task) {
        if (task == null) {
            throw new ObservabilityException("INVALID_CONTEXT", "Task must not be null");
        }
        ObservabilityContext captured = this;
        return () -> {
            try (Scope ignored = captured.attach()) {
                return task.call();
            }
        };
    }

    @Override
    public String toString() {
        // Baggage values are omitted intentionally to prevent accidental sensitive-data disclosure in logs.
        // baggage 值被有意省略，防止敏感数据意外写入日志。
        return "ObservabilityContext{traceId='" + traceId + "', spanId='" + spanId
                + "', baggageKeys=" + baggage.keySet() + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ObservabilityContext that)) {
            return false;
        }
        return traceId.equals(that.traceId) && spanId.equals(that.spanId) && baggage.equals(that.baggage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId, spanId, baggage);
    }

    /**
     * A scope that restores the previous {@link ObservabilityContext} when closed.
     * 关闭时恢复先前 {@link ObservabilityContext} 的 scope。
     *
     * <p>Intended for use with try-with-resources to ensure proper context cleanup.</p>
     * <p>旨在与 try-with-resources 一起使用以确保正确的上下文清理。</p>
     *
     * @author Leon Soo
     * <a href="https://leonsoo.com">www.LeonSoo.com</a>
     * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
     * @since JDK 25, opencode-base-observability V1.0.3
     */
    public static final class Scope implements AutoCloseable {

        private final ObservabilityContext previous;
        private final long attachThreadId;

        Scope(ObservabilityContext previous) {
            this.previous = previous;
            this.attachThreadId = Thread.currentThread().threadId();
        }

        /**
         * Restores the previous context (or removes the current one if there was no previous).
         * 恢复先前的上下文（如果没有先前的上下文则移除当前的）。
         *
         * <p>Must be called on the same thread that created this Scope via {@link #attach()}.
         * Cross-thread close is silently ignored to prevent context corruption.</p>
         * <p>必须在通过 {@link #attach()} 创建此 Scope 的同一线程上调用。
         * 跨线程 close 被静默忽略以防止上下文错乱。</p>
         */
        @Override
        public void close() {
            if (Thread.currentThread().threadId() != attachThreadId) {
                // Cross-thread close would corrupt the wrong thread's context — silently ignore.
                // 跨线程 close 会破坏错误线程的上下文 — 静默忽略。
                return;
            }
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }
}

package cloud.opencode.base.observability;

/**
 * Represents a single trace span for an operation, supporting {@link AutoCloseable} for try-with-resources.
 * 表示单个操作的追踪 span，支持 {@link AutoCloseable} 以便使用 try-with-resources。
 *
 * <p>Provides methods to record hit/miss status, errors, and custom attributes. Implementations
 * must be thread-safe and must tolerate multiple calls to {@link #end()} or {@link #close()} (idempotent).</p>
 * <p>提供记录命中/未命中状态、错误和自定义属性的方法。
 * 实现必须是线程安全的，并且必须容忍对 {@link #end()} 或 {@link #close()} 的多次调用（幂等）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Hit/miss recording - 命中/未命中记录</li>
 *   <li>Error capture - 错误捕获</li>
 *   <li>Custom string attribute support - 自定义字符串属性支持</li>
 *   <li>Idempotent end/close semantics - 幂等的 end/close 语义</li>
 *   <li>Shared NOOP singleton for zero-overhead fallback - 共享 NOOP 单例</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * try (Span span = tracer.startSpan("GET", "user:123")) {
 *     Object value = cache.get("user:123");
 *     span.setHit(value != null);
 *     span.setAttribute("cache.tier", "L1");
 * } catch (Exception e) {
 *     span.setError(e);
 *     throw e;
 * }
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>NOOP: zero allocation, constant singleton - NOOP: 零分配，常量单例</li>
 *   <li>All operations: O(1) - 所有操作: O(1)</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (implementations must be thread-safe, NOOP is stateless) - 线程安全: 是</li>
 *   <li>Null-safe: No (attribute key/value must not be null) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.0
 */
public interface Span extends AutoCloseable {

    /**
     * A shared no-op Span instance with zero overhead.
     * 共享的零开销空操作 Span 实例。
     */
    Span NOOP = new Span() {
        @Override public void setHit(boolean hit) {}
        @Override public void setError(Throwable error) {}
        @Override public void setAttribute(String key, String value) {}
        @Override public void end() {}
        @Override public void close() {}
    };

    /**
     * Records whether this operation was a hit or a miss.
     * 记录此操作是命中还是未命中。
     *
     * @param hit true if the key was found, false otherwise | 如果键被找到则为 true
     */
    void setHit(boolean hit);

    /**
     * Records an error that occurred during this operation.
     * 记录此操作期间发生的错误。
     *
     * @param error the throwable that occurred | 发生的异常
     */
    void setError(Throwable error);

    /**
     * Sets a custom string attribute on this span.
     * 在此 span 上设置自定义字符串属性。
     *
     * @param key   the attribute key | 属性键
     * @param value the attribute value | 属性值
     */
    void setAttribute(String key, String value);

    /**
     * Ends this span, recording its duration. Must be idempotent.
     * 结束此 span，记录其持续时间。必须是幂等的。
     */
    void end();

    /**
     * Closes this span by calling {@link #end()}.
     * 通过调用 {@link #end()} 关闭此 span。
     */
    @Override
    void close();
}

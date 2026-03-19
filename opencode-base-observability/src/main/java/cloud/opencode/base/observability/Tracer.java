package cloud.opencode.base.observability;

/**
 * Framework-agnostic tracing abstraction for creating {@link Span} instances around operations.
 * 框架无关的追踪抽象，用于围绕操作创建 {@link Span} 实例。
 *
 * <p>Provides a uniform API for operation tracing. Implementations may delegate to OpenTelemetry,
 * other tracing frameworks, or a no-op fallback. The sealed interface restricts permitted
 * implementations to known, auditable types.</p>
 * <p>提供统一的操作追踪 API。实现可以委托给 OpenTelemetry、其他追踪框架或空操作回退。
 * 密封接口将允许的实现限制为已知的、可审计的类型。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Framework-agnostic tracing API - 框架无关的追踪 API</li>
 *   <li>OpenTelemetry integration via reflection (no hard dependency) - 通过反射集成 OpenTelemetry</li>
 *   <li>Zero-overhead no-op implementation - 零开销的空操作实现</li>
 *   <li>try-with-resources support via AutoCloseable spans - 通过 AutoCloseable span 支持 try-with-resources</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Tracer tracer = OpenTelemetryTracer.create("my-service");
 * try (Span span = tracer.startSpan("GET", "user:42")) {
 *     Object value = cache.get("user:42");
 *     span.setHit(value != null);
 * }
 * tracer.close();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>No-op: zero allocation, zero overhead - 空操作: 零分配，零开销</li>
 *   <li>OTel: reflection-based with cached method handles - OTel: 基于反射，缓存方法句柄</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (all implementations are thread-safe) - 线程安全: 是</li>
 *   <li>Null-safe: No (operationName and key must not be null) - 空值安全: 否</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.0
 */
public sealed interface Tracer permits OpenTelemetryTracer, Tracer.NoopTracer {

    /**
     * Starts a new trace span for the given operation.
     * 为给定的操作启动一个新的追踪 span。
     *
     * @param operationName the name of the operation (e.g., "GET", "PUT") | 操作名称（例如 "GET"、"PUT"）
     * @param key           the key or resource being operated on | 被操作的键或资源
     * @return a new {@link Span} representing this operation | 代表此操作的新 {@link Span}
     */
    Span startSpan(String operationName, String key);

    /**
     * Closes this tracer and releases any underlying resources.
     * 关闭此追踪器并释放所有底层资源。
     */
    void close();

    /**
     * Returns a no-op Tracer that creates no-op spans with zero overhead.
     * 返回一个空操作 Tracer，以零开销创建空操作 span。
     *
     * @return the no-op tracer singleton | 空操作追踪器单例
     */
    static Tracer noop() {
        return NoopTracer.INSTANCE;
    }

    /**
     * No-op implementation of {@link Tracer} with zero overhead.
     * 零开销的 {@link Tracer} 空操作实现。
     */
    final class NoopTracer implements Tracer {

        static final NoopTracer INSTANCE = new NoopTracer();

        private NoopTracer() {}

        @Override
        public Span startSpan(String operationName, String key) {
            return Span.NOOP;
        }

        @Override
        public void close() {
            // no-op
        }

        @Override
        public String toString() {
            return "Tracer.noop()";
        }
    }
}

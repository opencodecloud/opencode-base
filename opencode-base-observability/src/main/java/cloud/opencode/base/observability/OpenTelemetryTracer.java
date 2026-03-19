package cloud.opencode.base.observability;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * OpenTelemetry integration for operation tracing with graceful no-op fallback.
 * 用于操作追踪的 OpenTelemetry 集成，支持优雅的空操作回退。
 *
 * <p>Provides a lightweight abstraction over the OpenTelemetry tracing API that does not
 * directly depend on the OTel SDK. If the OTel API is present on the classpath, it uses
 * reflection to create real spans; otherwise it falls back to no-op transparently.</p>
 * <p>提供对 OpenTelemetry 追踪 API 的轻量级抽象，不直接依赖 OTel SDK。
 * 如果 OTel API 存在于类路径中，则使用反射创建真实的 span；否则透明地回退到空操作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Zero-dependency abstraction over OpenTelemetry - 对 OpenTelemetry 的零依赖抽象</li>
 *   <li>Automatic classpath detection via reflection - 通过反射自动检测类路径</li>
 *   <li>No-op fallback when OTel is not available - OTel 不可用时的空操作回退</li>
 *   <li>Thread-safe span creation and attribute setting - 线程安全的 span 创建和属性设置</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Auto-detects OTel on classpath; falls back to noop if absent
 * Tracer tracer = OpenTelemetryTracer.create("my-service");
 *
 * try (Span span = tracer.startSpan("GET", "user:123")) {
 *     span.setHit(true);
 *     span.setAttribute("cache.tier", "L1");
 * }
 *
 * tracer.close();
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>No-op path has near-zero overhead - 空操作路径几乎零开销</li>
 *   <li>Reflection-based method handles cached per class+method - 反射方法句柄按类+方法缓存</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (ConcurrentHashMap for cache, AtomicBoolean for state) - 线程安全: 是</li>
 *   <li>Null-safe: Yes (rejects null parameters, noop fallback) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-observability V1.0.0
 */
public final class OpenTelemetryTracer implements Tracer {

    private static final System.Logger LOGGER = System.getLogger(OpenTelemetryTracer.class.getName());

    private static final String GLOBAL_OTEL_CLASS = "io.opentelemetry.api.GlobalOpenTelemetry";
    private static final String OTEL_SPAN_BUILDER_CLASS = "io.opentelemetry.api.trace.SpanBuilder";
    private static final String OTEL_SPAN_CLASS = "io.opentelemetry.api.trace.Span";
    private static final String OTEL_STATUS_CODE_CLASS = "io.opentelemetry.api.trace.StatusCode";

    private final String serviceName;
    private final Object otelTracer;
    private final boolean otelAvailable;
    private final ConcurrentHashMap<String, Method> methodCache = new ConcurrentHashMap<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    private OpenTelemetryTracer(String serviceName, Object otelTracer, boolean otelAvailable) {
        this.serviceName = serviceName;
        this.otelTracer = otelTracer;
        this.otelAvailable = otelAvailable;
    }

    // ==================== Factory Methods | 工厂方法 ====================

    /**
     * Creates an {@code OpenTelemetryTracer} for the given service name.
     * 为给定的服务名称创建 {@code OpenTelemetryTracer}。
     *
     * <p>Attempts to locate the OpenTelemetry API on the classpath via reflection.
     * If unavailable, the returned tracer silently delegates to a no-op implementation.</p>
     * <p>尝试通过反射在类路径上定位 OpenTelemetry API。如果不可用，返回的追踪器静默委托给空操作实现。</p>
     *
     * @param serviceName the logical service name for tracing | 用于追踪的逻辑服务名称
     * @return a new tracer instance (may be backed by noop) | 新的追踪器实例（可能由 noop 支持）
     */
    public static OpenTelemetryTracer create(String serviceName) {
        Objects.requireNonNull(serviceName, "serviceName must not be null");
        try {
            Class<?> globalOTelClass = Class.forName(GLOBAL_OTEL_CLASS);
            Method getMethod = globalOTelClass.getMethod("get");
            Object globalOTel = getMethod.invoke(null);
            Method getTracerMethod = globalOTel.getClass().getMethod("getTracer", String.class);
            Object tracer = getTracerMethod.invoke(globalOTel, serviceName);
            LOGGER.log(System.Logger.Level.INFO,
                    "OpenTelemetry API detected. Tracing enabled for service: {0}", serviceName);
            return new OpenTelemetryTracer(serviceName, tracer, true);
        } catch (ClassNotFoundException e) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "OpenTelemetry API not on classpath. Using noop tracer for: {0}", serviceName);
            return new OpenTelemetryTracer(serviceName, null, false);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.WARNING,
                    "Failed to init OpenTelemetry tracer for {0}: {1}", serviceName, e.getMessage());
            return new OpenTelemetryTracer(serviceName, null, false);
        }
    }

    // ==================== Tracer API | 追踪器 API ====================

    @Override
    public Span startSpan(String operationName, String key) {
        Objects.requireNonNull(operationName, "operationName must not be null");
        Objects.requireNonNull(key, "key must not be null");

        if (closed.get() || !otelAvailable || otelTracer == null) {
            return Span.NOOP;
        }
        try {
            return createOtelSpan(operationName, key);
        } catch (Exception e) {
            LOGGER.log(System.Logger.Level.DEBUG,
                    "Failed to create OTel span, falling back to noop: {0}", e.getMessage());
            return Span.NOOP;
        }
    }

    @Override
    public void close() {
        if (closed.compareAndSet(false, true)) {
            methodCache.clear();
            LOGGER.log(System.Logger.Level.DEBUG,
                    "OpenTelemetryTracer closed for service: {0}", serviceName);
        }
    }

    // ==================== Metrics | 指标 ====================

    /**
     * Returns whether OpenTelemetry API was detected on the classpath.
     * 返回是否在类路径上检测到 OpenTelemetry API。
     *
     * @return true if OTel is available | 如果 OTel 可用则返回 true
     */
    public boolean isOtelAvailable() {
        return otelAvailable;
    }

    /**
     * Returns the configured service name.
     * 返回配置的服务名称。
     *
     * @return the service name | 服务名称
     */
    public String serviceName() {
        return serviceName;
    }

    // ==================== Internal | 内部实现 ====================

    private Span createOtelSpan(String operationName, String key) throws Exception {
        Method spanBuilderMethod = cachedMethod(otelTracer.getClass(), "spanBuilder", String.class);
        Object spanBuilder = spanBuilderMethod.invoke(otelTracer, operationName);

        Class<?> builderClass = Class.forName(OTEL_SPAN_BUILDER_CLASS);
        Method setAttribute = cachedMethod(builderClass, "setAttribute", String.class, String.class);
        setAttribute.invoke(spanBuilder, "operation", operationName);
        setAttribute.invoke(spanBuilder, "key", key);
        setAttribute.invoke(spanBuilder, "service.name", serviceName);

        Method startSpan = cachedMethod(builderClass, "startSpan");
        Object otelSpan = startSpan.invoke(spanBuilder);

        return new ReflectiveSpan(otelSpan);
    }

    private Method cachedMethod(Class<?> clazz, String name, Class<?>... params) throws NoSuchMethodException {
        String cacheKey = clazz.getName() + "#" + name + "#" + params.length;
        return methodCache.computeIfAbsent(cacheKey, _ -> {
            try {
                return clazz.getMethod(name, params);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Method not found: " + cacheKey, e);
            }
        });
    }

    /**
     * Reflective {@link Span} implementation delegating to a real OTel Span via reflection.
     * 通过反射委托给真实 OTel Span 的反射 {@link Span} 实现。
     */
    private final class ReflectiveSpan implements Span {

        private final Object otelSpan;
        private final AtomicBoolean ended = new AtomicBoolean(false);

        ReflectiveSpan(Object otelSpan) {
            this.otelSpan = otelSpan;
        }

        @Override
        public void setHit(boolean hit) {
            setAttribute("hit", String.valueOf(hit));
        }

        @Override
        public void setError(Throwable error) {
            if (error == null || ended.get()) return;
            try {
                Method recordEx = cachedMethod(Class.forName(OTEL_SPAN_CLASS), "recordException", Throwable.class);
                recordEx.invoke(otelSpan, error);
                Class<?> statusCodeClass = Class.forName(OTEL_STATUS_CODE_CLASS);
                Object errorStatus = statusCodeClass.getField("ERROR").get(null);
                Method setStatus = cachedMethod(Class.forName(OTEL_SPAN_CLASS), "setStatus", statusCodeClass, String.class);
                setStatus.invoke(otelSpan, errorStatus, error.getMessage());
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.DEBUG, "setError failed: {0}", e.getMessage());
            }
        }

        @Override
        public void setAttribute(String key, String value) {
            if (key == null || value == null || ended.get()) return;
            try {
                Method m = cachedMethod(Class.forName(OTEL_SPAN_CLASS), "setAttribute", String.class, String.class);
                m.invoke(otelSpan, key, value);
            } catch (Exception e) {
                LOGGER.log(System.Logger.Level.DEBUG, "setAttribute failed: {0}", e.getMessage());
            }
        }

        @Override
        public void end() {
            if (ended.compareAndSet(false, true)) {
                try {
                    Method m = cachedMethod(Class.forName(OTEL_SPAN_CLASS), "end");
                    m.invoke(otelSpan);
                } catch (Exception e) {
                    LOGGER.log(System.Logger.Level.DEBUG, "span.end() failed: {0}", e.getMessage());
                }
            }
        }

        @Override
        public void close() {
            end();
        }
    }
}

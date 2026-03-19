package cloud.opencode.base.web.internal;

import cloud.opencode.base.web.context.RequestContextHolder;

import java.util.UUID;

/**
 * Trace ID Resolver
 * 追踪ID解析器
 *
 * <p>Resolves trace ID for request tracking.</p>
 * <p>解析请求追踪ID。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Trace ID resolution from request context - 从请求上下文解析追踪ID</li>
 *   <li>UUID-based trace ID generation - 基于UUID的追踪ID生成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * String traceId = TraceIdResolver.resolve();
 * String newId = TraceIdResolver.generateTraceId();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility) - 是（无状态工具）</li>
 *   <li>Null-safe: Yes (returns generated ID if context is null) - 是（上下文为null时返回生成的ID）</li>
 * </ul>
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-web V1.0.0
 */
public final class TraceIdResolver {

    private TraceIdResolver() {
        // Utility class
    }

    /**
     * Resolve trace ID
     * 解析追踪ID
     *
     * <p>Resolution order | 解析顺序:</p>
     * <ol>
     *   <li>From RequestContext - 从请求上下文获取</li>
     *   <li>Generate new UUID - 生成新的UUID</li>
     * </ol>
     *
     * @return the trace ID | 追踪ID
     */
    public static String resolve() {
        // Try to get from RequestContext
        var context = RequestContextHolder.getContext();
        if (context != null && context.traceId() != null) {
            return context.traceId();
        }

        // Generate new trace ID
        return generateTraceId();
    }

    /**
     * Generate trace ID
     * 生成追踪ID
     *
     * @return the generated trace ID | 生成的追踪ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

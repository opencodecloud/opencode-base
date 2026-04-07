package cloud.opencode.base.event.monitor;

/**
 * Event Bus Metrics - Snapshot of event bus statistics
 * 事件总线指标 - 事件总线统计快照
 *
 * <p>Immutable snapshot of event bus operational metrics, including event counts,
 * error rates, and listener statistics.</p>
 * <p>事件总线运行指标的不可变快照，包括事件计数、错误率和监听器统计。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Total published/delivered/error counts - 总发布/投递/错误计数</li>
 *   <li>Dead event tracking - 死事件跟踪</li>
 *   <li>Listener count - 监听器计数</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * EventBusMetrics metrics = eventBus.getMetrics();
 * System.out.println("Published: " + metrics.totalPublished());
 * System.out.println("Dead events: " + metrics.totalDeadEvents());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 * </ul>
 *
 * @param totalPublished  total number of events published | 总发布事件数
 * @param totalDelivered  total number of listener invocations | 总监听器调用次数
 * @param totalErrors     total number of listener errors | 总监听器错误数
 * @param totalDeadEvents total number of dead events (no listeners) | 总死事件数（无监听器）
 * @param listenerCount   current number of registered listeners | 当前注册监听器数
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-event V1.0.3
 */
public record EventBusMetrics(
        long totalPublished,
        long totalDelivered,
        long totalErrors,
        long totalDeadEvents,
        int listenerCount
) {
}

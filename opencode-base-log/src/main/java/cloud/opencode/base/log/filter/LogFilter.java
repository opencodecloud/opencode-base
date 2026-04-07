package cloud.opencode.base.log.filter;

import cloud.opencode.base.log.LogEvent;

/**
 * Log Filter Interface - Functional Filter for Log Events
 * 日志过滤器接口 - 日志事件的函数式过滤器
 *
 * <p>A functional interface that evaluates a {@link LogEvent} and returns a
 * {@link FilterAction} indicating whether the event should be accepted, denied,
 * or passed to the next filter.</p>
 * <p>一个函数式接口，评估 {@link LogEvent} 并返回 {@link FilterAction}，
 * 指示事件应被接受、拒绝还是传递给下一个过滤器。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Functional interface for lambda-based filters - 函数式接口支持 Lambda 过滤器</li>
 *   <li>Ordered evaluation via getOrder() - 通过 getOrder() 进行有序评估</li>
 *   <li>Three-state filter result (ACCEPT/DENY/NEUTRAL) - 三态过滤结果</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Lambda-based filter
 * LogFilter filter = event -> event.level().getLevel() >= LogLevel.WARN.getLevel()
 *     ? FilterAction.ACCEPT : FilterAction.NEUTRAL;
 *
 * // With custom order
 * LogFilter priorityFilter = new LogFilter() {
 *     public FilterAction filter(LogEvent event) { return FilterAction.NEUTRAL; }
 *     public int getOrder() { return -10; }  // higher priority
 * };
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation-dependent - 线程安全: 取决于实现</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
@FunctionalInterface
public interface LogFilter {

    /**
     * Evaluates the given log event and returns a filter action.
     * 评估给定的日志事件并返回过滤器动作。
     *
     * @param event the log event to evaluate | 要评估的日志事件
     * @return the filter action | 过滤器动作
     */
    FilterAction filter(LogEvent event);

    /**
     * Returns the order of this filter. Lower values indicate higher priority.
     * 返回此过滤器的顺序。较低的值表示较高的优先级。
     *
     * @return the order value (default 0) | 顺序值（默认 0）
     */
    default int getOrder() {
        return 0;
    }
}

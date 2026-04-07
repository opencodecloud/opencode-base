package cloud.opencode.base.log.filter;

import cloud.opencode.base.log.LogEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Log Filter Chain - Ordered Chain of Log Filters
 * 日志过滤器链 - 有序的日志过滤器链
 *
 * <p>Manages an ordered collection of {@link LogFilter} instances and evaluates
 * them sequentially against a {@link LogEvent}. Filters are sorted by their
 * {@link LogFilter#getOrder()} value (lower = higher priority).</p>
 * <p>管理 {@link LogFilter} 实例的有序集合，并按顺序对 {@link LogEvent} 进行评估。
 * 过滤器按 {@link LogFilter#getOrder()} 值排序（较低 = 较高优先级）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Thread-safe filter chain with copy-on-write semantics - 具有写时复制语义的线程安全过滤器链</li>
 *   <li>Short-circuit on ACCEPT or DENY - 遇到 ACCEPT 或 DENY 时短路</li>
 *   <li>Ordered filter evaluation - 有序的过滤器评估</li>
 *   <li>Dynamic filter addition and removal - 动态添加和移除过滤器</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LogFilterChain chain = new LogFilterChain();
 * chain.addFilter(new LevelFilter(LogLevel.WARN));
 * chain.addFilter(new ThrottleFilter(Duration.ofSeconds(5)));
 *
 * FilterAction result = chain.apply(logEvent);
 * if (result != FilterAction.DENY) {
 *     // process the event
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (volatile + synchronized for mutations) - 线程安全: 是（volatile + synchronized 用于变更）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public final class LogFilterChain {

    private volatile List<LogFilter> filters = List.of();

    /**
     * Adds a filter to this chain, sorted by order.
     * 向此链添加过滤器，按顺序排序。
     *
     * @param filter the filter to add | 要添加的过滤器
     * @throws NullPointerException if filter is null | 如果过滤器为 null
     */
    public synchronized void addFilter(LogFilter filter) {
        Objects.requireNonNull(filter, "filter must not be null");
        List<LogFilter> newFilters = new ArrayList<>(filters);
        newFilters.add(filter);
        newFilters.sort(Comparator.comparingInt(LogFilter::getOrder));
        this.filters = Collections.unmodifiableList(newFilters);
    }

    /**
     * Removes a filter from this chain.
     * 从此链中移除过滤器。
     *
     * @param filter the filter to remove | 要移除的过滤器
     * @throws NullPointerException if filter is null | 如果过滤器为 null
     */
    public synchronized void removeFilter(LogFilter filter) {
        Objects.requireNonNull(filter, "filter must not be null");
        List<LogFilter> newFilters = new ArrayList<>(filters);
        newFilters.remove(filter);
        this.filters = Collections.unmodifiableList(newFilters);
    }

    /**
     * Applies all filters in order to the given log event.
     * 按顺序将所有过滤器应用于给定的日志事件。
     *
     * <p>Evaluation stops on the first ACCEPT or DENY result. If all filters
     * return NEUTRAL, this method returns NEUTRAL.</p>
     * <p>评估在第一个 ACCEPT 或 DENY 结果时停止。如果所有过滤器都返回 NEUTRAL，
     * 此方法返回 NEUTRAL。</p>
     *
     * @param event the log event to evaluate | 要评估的日志事件
     * @return the filter action | 过滤器动作
     */
    public FilterAction apply(LogEvent event) {
        List<LogFilter> snapshot = this.filters;
        for (LogFilter filter : snapshot) {
            try {
                FilterAction action = filter.filter(event);
                if (action == FilterAction.DENY || action == FilterAction.ACCEPT) {
                    return action;
                }
            } catch (RuntimeException e) {
                // Isolate filter exceptions to prevent log system instability
                System.err.println("[LogFilterChain] Filter " + filter.getClass().getName()
                        + " threw exception: " + e.getClass().getName() + ": " + e.getMessage());
                e.printStackTrace(System.err);
                // Treat as NEUTRAL and continue to next filter
            }
        }
        return FilterAction.NEUTRAL;
    }

    /**
     * Clears all filters from this chain.
     * 清除此链中的所有过滤器。
     */
    public synchronized void clear() {
        this.filters = List.of();
    }

    /**
     * Returns an unmodifiable view of the current filters.
     * 返回当前过滤器的不可修改视图。
     *
     * @return unmodifiable list of filters | 不可修改的过滤器列表
     */
    public List<LogFilter> getFilters() {
        return this.filters;
    }
}

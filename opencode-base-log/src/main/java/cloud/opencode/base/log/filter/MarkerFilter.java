package cloud.opencode.base.log.filter;

import cloud.opencode.base.log.LogEvent;

import java.util.Objects;

/**
 * Marker Filter - Filters Log Events by Marker Name
 * 标记过滤器 - 按标记名称过滤日志事件
 *
 * <p>Evaluates whether a log event has a marker matching the configured name,
 * returning the configured action for match and mismatch cases.</p>
 * <p>评估日志事件是否具有与配置名称匹配的标记，
 * 为匹配和不匹配情况返回配置的动作。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Marker name matching - 标记名称匹配</li>
 *   <li>Configurable match and mismatch actions - 可配置的匹配和不匹配动作</li>
 *   <li>Supports marker hierarchy via contains() - 通过 contains() 支持标记层次结构</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Accept SECURITY markers, neutral otherwise
 * MarkerFilter filter = new MarkerFilter("SECURITY", FilterAction.ACCEPT, FilterAction.NEUTRAL);
 *
 * // Deny non-AUDIT events
 * MarkerFilter auditOnly = new MarkerFilter("AUDIT", FilterAction.NEUTRAL, FilterAction.DENY);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-log V1.0.3
 */
public final class MarkerFilter implements LogFilter {

    private final String markerName;
    private final FilterAction onMatch;
    private final FilterAction onMismatch;

    /**
     * Creates a marker filter with the specified name and actions.
     * 使用指定的名称和动作创建标记过滤器。
     *
     * @param markerName the marker name to match | 要匹配的标记名称
     * @param onMatch    the action when marker matches | 标记匹配时的动作
     * @param onMismatch the action when marker does not match | 标记不匹配时的动作
     * @throws NullPointerException if any argument is null | 如果任何参数为 null
     */
    public MarkerFilter(String markerName, FilterAction onMatch, FilterAction onMismatch) {
        this.markerName = Objects.requireNonNull(markerName, "markerName must not be null");
        this.onMatch = Objects.requireNonNull(onMatch, "onMatch must not be null");
        this.onMismatch = Objects.requireNonNull(onMismatch, "onMismatch must not be null");
    }

    /**
     * Filters the event based on its marker.
     * 根据事件标记进行过滤。
     *
     * @param event the log event | 日志事件
     * @return onMatch if marker matches, onMismatch otherwise | 如果标记匹配返回 onMatch，否则返回 onMismatch
     */
    @Override
    public FilterAction filter(LogEvent event) {
        if (event.hasMarker() && event.marker().contains(markerName)) {
            return onMatch;
        }
        return onMismatch;
    }
}

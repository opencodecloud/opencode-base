package cloud.opencode.base.date.range;

import cloud.opencode.base.date.extra.LocalTimeRange;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Optional;

/**
 * TimeRange - Compatibility wrapper for LocalTimeRange
 * 时间范围 - LocalTimeRange的兼容性包装器
 *
 * <p>This class provides a compatibility layer for the extra.LocalTimeRange class,
 * offering the same functionality with a simpler name for legacy code compatibility.</p>
 * <p>此类为extra.LocalTimeRange类提供兼容层，使用更简单的名称以兼容遗留代码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compatibility wrapper for LocalTimeRange - LocalTimeRange的兼容性包装器</li>
 *   <li>Containment and overlap checking - 包含和重叠检查</li>
 *   <li>Duration calculation - 持续时间计算</li>
 *   <li>Intersection and span operations - 交集和跨度操作</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeRange range = TimeRange.of(LocalTime.of(9, 0), LocalTime.of(17, 0));
 * Duration duration = range.toDuration();
 * boolean contains = range.contains(LocalTime.of(12, 0));
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable, delegates to immutable LocalTimeRange) - 线程安全: 是（不可变，委托给不可变的LocalTimeRange）</li>
 *   <li>Null-safe: No (delegates null handling to LocalTimeRange) - 空值安全: 否（空值处理委托给LocalTimeRange）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see LocalTimeRange
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 * @deprecated Use {@link LocalTimeRange} instead
 */
@Deprecated(since = "1.0.0", forRemoval = false)
public final class TimeRange {

    private final LocalTimeRange delegate;

    private TimeRange(LocalTimeRange delegate) {
        this.delegate = delegate;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a TimeRange
     * 创建时间范围
     *
     * @param start the start time | 起始时间
     * @param end the end time | 结束时间
     * @return the TimeRange | 时间范围
     */
    public static TimeRange of(LocalTime start, LocalTime end) {
        return new TimeRange(LocalTimeRange.of(start, end));
    }

    /**
     * Creates a TimeRange for business hours (9:00 - 17:00)
     * 创建工作时间范围（9:00 - 17:00）
     *
     * @return the business hours TimeRange | 工作时间范围
     */
    public static TimeRange businessHours() {
        return of(LocalTime.of(9, 0), LocalTime.of(17, 0));
    }

    /**
     * Creates a TimeRange from a LocalTimeRange
     * 从LocalTimeRange创建TimeRange
     *
     * @param range the LocalTimeRange | LocalTimeRange
     * @return the TimeRange | 时间范围
     */
    public static TimeRange from(LocalTimeRange range) {
        return new TimeRange(range);
    }

    // ==================== Getter Methods ====================

    public LocalTime getStart() {
        return delegate.getStart();
    }

    public LocalTime getEnd() {
        return delegate.getEnd();
    }

    public Duration toDuration() {
        return delegate.getDuration();
    }

    public boolean isEmpty() {
        return delegate.getStart().equals(delegate.getEnd());
    }

    // ==================== Query Methods ====================

    public boolean contains(LocalTime time) {
        return delegate.contains(time);
    }

    public boolean overlaps(TimeRange other) {
        return delegate.overlaps(other.delegate);
    }

    public boolean encloses(TimeRange other) {
        return delegate.contains(other.delegate);
    }

    // ==================== Operation Methods ====================

    public Optional<TimeRange> intersection(TimeRange other) {
        LocalTimeRange result = delegate.intersection(other.delegate);
        return result != null ? Optional.of(new TimeRange(result)) : Optional.empty();
    }

    public TimeRange span(TimeRange other) {
        // Create a span by taking min start and max end
        LocalTime minStart = delegate.getStart().isBefore(other.delegate.getStart())
            ? delegate.getStart() : other.delegate.getStart();
        LocalTime maxEnd = delegate.getEnd().isAfter(other.delegate.getEnd())
            ? delegate.getEnd() : other.delegate.getEnd();
        return new TimeRange(LocalTimeRange.of(minStart, maxEnd));
    }

    // ==================== Conversion ====================

    /**
     * Converts to LocalTimeRange
     * 转换为LocalTimeRange
     *
     * @return the LocalTimeRange | LocalTimeRange
     */
    public LocalTimeRange toLocalTimeRange() {
        return delegate;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof TimeRange other) {
            return delegate.equals(other.delegate);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}

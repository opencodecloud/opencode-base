package cloud.opencode.base.date.range;

import cloud.opencode.base.date.extra.LocalDateTimeRange;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * DateTimeRange - Compatibility wrapper for LocalDateTimeRange
 * 日期时间范围 - LocalDateTimeRange的兼容性包装器
 *
 * <p>This class provides a compatibility layer for the extra.LocalDateTimeRange class,
 * offering the same functionality with a simpler name for legacy code compatibility.</p>
 * <p>此类为extra.LocalDateTimeRange类提供兼容层，使用更简单的名称以兼容遗留代码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compatibility wrapper for LocalDateTimeRange - LocalDateTimeRange的兼容性包装器</li>
 *   <li>Containment and overlap checking - 包含和重叠检查</li>
 *   <li>Intersection, span, and gap operations - 交集、跨度和间隙操作</li>
 *   <li>Conversion to other range types - 转换为其他范围类型</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DateTimeRange range = DateTimeRange.of(
 *     LocalDateTime.of(2024, 1, 1, 0, 0),
 *     LocalDateTime.of(2024, 12, 31, 23, 59, 59)
 * );
 * Duration duration = range.toDuration();
 * boolean contains = range.contains(LocalDateTime.now());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable, delegates to immutable LocalDateTimeRange) - 线程安全: 是（不可变，委托给不可变的LocalDateTimeRange）</li>
 *   <li>Null-safe: No (delegates null handling to LocalDateTimeRange) - 空值安全: 否（空值处理委托给LocalDateTimeRange）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see LocalDateTimeRange
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 * @deprecated Use {@link LocalDateTimeRange} instead
 */
@Deprecated(since = "1.0.0", forRemoval = false)
public final class DateTimeRange {

    private final LocalDateTimeRange delegate;

    private DateTimeRange(LocalDateTimeRange delegate) {
        this.delegate = delegate;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a DateTimeRange
     * 创建日期时间范围
     *
     * @param start the start datetime | 起始日期时间
     * @param end the end datetime | 结束日期时间
     * @return the DateTimeRange | 日期时间范围
     */
    public static DateTimeRange of(LocalDateTime start, LocalDateTime end) {
        return new DateTimeRange(LocalDateTimeRange.of(start, end));
    }

    /**
     * Creates a DateTimeRange with a duration
     * 使用时长创建日期时间范围
     *
     * @param start the start datetime | 起始日期时间
     * @param duration the duration | 时长
     * @return the DateTimeRange | 日期时间范围
     */
    public static DateTimeRange of(LocalDateTime start, Duration duration) {
        return new DateTimeRange(LocalDateTimeRange.of(start, duration));
    }

    /**
     * Creates an empty DateTimeRange at the current time
     * 在当前时间创建空日期时间范围
     *
     * @return empty DateTimeRange | 空日期时间范围
     */
    public static DateTimeRange empty() {
        return new DateTimeRange(LocalDateTimeRange.empty(LocalDateTime.now()));
    }

    /**
     * Creates an empty DateTimeRange at the specified time
     * 在指定时间创建空日期时间范围
     *
     * @param dateTime the datetime | 日期时间
     * @return empty DateTimeRange | 空日期时间范围
     */
    public static DateTimeRange empty(LocalDateTime dateTime) {
        return new DateTimeRange(LocalDateTimeRange.empty(dateTime));
    }

    /**
     * Creates a DateTimeRange from a LocalDateTimeRange
     * 从LocalDateTimeRange创建DateTimeRange
     *
     * @param range the LocalDateTimeRange | LocalDateTimeRange
     * @return the DateTimeRange | 日期时间范围
     */
    public static DateTimeRange from(LocalDateTimeRange range) {
        return new DateTimeRange(range);
    }

    // ==================== Getter Methods ====================

    public LocalDateTime getStart() {
        return delegate.getStart();
    }

    public LocalDateTime getEnd() {
        return delegate.getEnd();
    }

    public Duration toDuration() {
        return delegate.toDuration();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    // ==================== Query Methods ====================

    public boolean contains(LocalDateTime dateTime) {
        return delegate.contains(dateTime);
    }

    public boolean overlaps(DateTimeRange other) {
        return delegate.overlaps(other.delegate);
    }

    public boolean encloses(DateTimeRange other) {
        return delegate.encloses(other.delegate);
    }

    public boolean abuts(DateTimeRange other) {
        return delegate.abuts(other.delegate);
    }

    // ==================== Operation Methods ====================

    public Optional<DateTimeRange> intersection(DateTimeRange other) {
        return delegate.intersection(other.delegate).map(DateTimeRange::new);
    }

    public DateTimeRange span(DateTimeRange other) {
        // If overlapping or abutting, use union; otherwise create spanning range
        if (delegate.overlaps(other.delegate) || delegate.abuts(other.delegate)) {
            return new DateTimeRange(delegate.union(other.delegate));
        }
        // Create spanning range manually
        LocalDateTime minStart = delegate.getStart().isBefore(other.delegate.getStart())
            ? delegate.getStart() : other.delegate.getStart();
        LocalDateTime maxEnd = delegate.getEnd().isAfter(other.delegate.getEnd())
            ? delegate.getEnd() : other.delegate.getEnd();
        return new DateTimeRange(LocalDateTimeRange.of(minStart, maxEnd));
    }

    public Optional<DateTimeRange> gap(DateTimeRange other) {
        return delegate.gap(other.delegate).map(DateTimeRange::new);
    }

    // ==================== Conversion ====================

    /**
     * Converts to LocalDateTimeRange
     * 转换为LocalDateTimeRange
     *
     * @return the LocalDateTimeRange | LocalDateTimeRange
     */
    public LocalDateTimeRange toLocalDateTimeRange() {
        return delegate;
    }

    /**
     * Converts to DateRange (date portion only)
     * 转换为DateRange（仅日期部分）
     *
     * @return the DateRange | 日期范围
     */
    public DateRange toDateRange() {
        return DateRange.from(delegate.toLocalDateRange());
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DateTimeRange other) {
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

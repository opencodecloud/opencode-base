package cloud.opencode.base.date.range;

import cloud.opencode.base.date.extra.LocalDateRange;

import java.time.LocalDate;
import java.time.Period;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * DateRange - Compatibility wrapper for LocalDateRange
 * 日期范围 - LocalDateRange的兼容性包装器
 *
 * <p>This class provides a compatibility layer for the extra.LocalDateRange class,
 * offering the same functionality with a simpler name for legacy code compatibility.</p>
 * <p>此类为extra.LocalDateRange类提供兼容层，使用更简单的名称以兼容遗留代码。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Compatibility wrapper for LocalDateRange - LocalDateRange的兼容性包装器</li>
 *   <li>Date iteration and streaming - 日期迭代和流式处理</li>
 *   <li>Containment and overlap checking - 包含和重叠检查</li>
 *   <li>Split by week/month - 按周/月分割</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * DateRange range = DateRange.of(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));
 * long days = range.lengthInDays();
 * for (LocalDate date : range) {
 *     System.out.println(date);
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable, delegates to immutable LocalDateRange) - 线程安全: 是（不可变，委托给不可变的LocalDateRange）</li>
 *   <li>Null-safe: No (delegates null handling to LocalDateRange) - 空值安全: 否（空值处理委托给LocalDateRange）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see LocalDateRange
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 * @deprecated Use {@link LocalDateRange} instead
 */
@Deprecated(since = "1.0.0", forRemoval = false)
public final class DateRange implements Iterable<LocalDate> {

    private final LocalDateRange delegate;

    private DateRange(LocalDateRange delegate) {
        this.delegate = delegate;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Creates a DateRange (inclusive on both ends)
     * 创建日期范围（两端都包含）
     *
     * @param start the start date | 起始日期
     * @param end the end date | 结束日期
     * @return the DateRange | 日期范围
     */
    public static DateRange of(LocalDate start, LocalDate end) {
        return new DateRange(LocalDateRange.of(start, end));
    }

    /**
     * Creates a DateRange (exclusive end)
     * 创建日期范围（不包含结束日期）
     *
     * @param start the start date | 起始日期
     * @param endExclusive the end date (exclusive) | 结束日期（不包含）
     * @return the DateRange | 日期范围
     */
    public static DateRange ofExclusive(LocalDate start, LocalDate endExclusive) {
        return new DateRange(LocalDateRange.ofExclusive(start, endExclusive));
    }

    /**
     * Creates an empty DateRange
     * 创建空日期范围
     *
     * @return empty DateRange | 空日期范围
     */
    public static DateRange empty() {
        return new DateRange(LocalDateRange.empty());
    }

    /**
     * Creates a DateRange from a LocalDateRange
     * 从LocalDateRange创建DateRange
     *
     * @param range the LocalDateRange | LocalDateRange
     * @return the DateRange | 日期范围
     */
    public static DateRange from(LocalDateRange range) {
        return new DateRange(range);
    }

    // ==================== Getter Methods ====================

    public LocalDate getStart() {
        return delegate.getStart();
    }

    public LocalDate getEnd() {
        return delegate.getEnd();
    }

    public long lengthInDays() {
        return delegate.lengthInDays();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    // ==================== Query Methods ====================

    public boolean contains(LocalDate date) {
        return delegate.contains(date);
    }

    public boolean encloses(DateRange other) {
        return delegate.encloses(other.delegate);
    }

    public boolean overlaps(DateRange other) {
        return delegate.overlaps(other.delegate);
    }

    public boolean isConnected(DateRange other) {
        return delegate.isConnected(other.delegate);
    }

    // ==================== Operation Methods ====================

    public Optional<DateRange> intersection(DateRange other) {
        return delegate.intersection(other.delegate).map(DateRange::new);
    }

    public DateRange span(DateRange other) {
        return new DateRange(delegate.span(other.delegate));
    }

    // ==================== Iteration Methods ====================

    @Override
    public Iterator<LocalDate> iterator() {
        return delegate.iterator();
    }

    public Stream<LocalDate> stream() {
        return delegate.stream();
    }

    public Stream<LocalDate> stream(Period step) {
        return delegate.stream(step);
    }

    public List<LocalDate> toList() {
        return delegate.toList();
    }

    public List<DateRange> splitByWeek() {
        return delegate.splitByWeek().stream().map(DateRange::new).toList();
    }

    public List<DateRange> splitByMonth() {
        return delegate.splitByMonth().stream().map(DateRange::new).toList();
    }

    // ==================== Conversion ====================

    /**
     * Converts to LocalDateRange
     * 转换为LocalDateRange
     *
     * @return the LocalDateRange | LocalDateRange
     */
    public LocalDateRange toLocalDateRange() {
        return delegate;
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof DateRange other) {
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

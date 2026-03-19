package cloud.opencode.base.date.extra;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

/**
 * Hours unit class representing a number of hours
 * 小时单位类，表示小时数
 *
 * <p>This class represents a number of hours, similar to Duration but focused
 * on the hours unit. It implements TemporalAmount for use with java.time.</p>
 * <p>此类表示小时数，类似于Duration但专注于小时单位。实现TemporalAmount以与java.time配合使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe hours representation - 类型安全的小时数表示</li>
 *   <li>Arithmetic operations (plus, minus, multiply, divide) - 算术操作（加、减、乘、除）</li>
 *   <li>Conversion to Duration, Seconds, Minutes, Days - 转换为Duration、Seconds、Minutes、Days</li>
 *   <li>TemporalAmount implementation for java.time integration - 实现TemporalAmount以集成java.time</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Hours h = Hours.of(8);
 * Hours h2 = Hours.ofDays(2);  // 48 hours
 * Duration d = h.toDuration();
 * LocalDateTime dt = LocalDateTime.now().plus(h);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Overflow-safe: Yes (uses Math.addExact/multiplyExact) - 溢出安全: 是（使用Math.addExact/multiplyExact）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class Hours implements TemporalAmount, Comparable<Hours>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Hours ZERO = new Hours(0);

    private final long hours;

    private Hours(long hours) {
        this.hours = hours;
    }

    // ==================== Static Factory Methods ====================

    public static Hours of(long hours) {
        if (hours == 0) {
            return ZERO;
        }
        return new Hours(hours);
    }

    public static Hours ofMinutes(long minutes) {
        return of(minutes / 60);
    }

    public static Hours ofDays(long days) {
        return of(Math.multiplyExact(days, 24));
    }

    public static Hours from(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return of(duration.toHours());
    }

    public static Hours between(Temporal startInclusive, Temporal endExclusive) {
        return of(ChronoUnit.HOURS.between(startInclusive, endExclusive));
    }

    // ==================== Getter Methods ====================

    public long getAmount() {
        return hours;
    }

    public boolean isZero() {
        return hours == 0;
    }

    public boolean isNegative() {
        return hours < 0;
    }

    public boolean isPositive() {
        return hours > 0;
    }

    // ==================== Calculation Methods ====================

    public Hours plus(Hours other) {
        return of(Math.addExact(hours, other.hours));
    }

    public Hours plus(long hoursToAdd) {
        return of(Math.addExact(hours, hoursToAdd));
    }

    public Hours minus(Hours other) {
        return of(Math.subtractExact(hours, other.hours));
    }

    public Hours minus(long hoursToSubtract) {
        return of(Math.subtractExact(hours, hoursToSubtract));
    }

    public Hours multipliedBy(long multiplicand) {
        return of(Math.multiplyExact(hours, multiplicand));
    }

    public Hours dividedBy(long divisor) {
        return of(hours / divisor);
    }

    public Hours negated() {
        return of(Math.negateExact(hours));
    }

    public Hours abs() {
        return isNegative() ? negated() : this;
    }

    // ==================== Conversion Methods ====================

    public Duration toDuration() {
        return Duration.ofHours(hours);
    }

    public Seconds toSeconds() {
        return Seconds.of(Math.multiplyExact(hours, 3600));
    }

    public Minutes toMinutes() {
        return Minutes.of(Math.multiplyExact(hours, 60));
    }

    public Days toDays() {
        return Days.of(hours / 24);
    }

    // ==================== TemporalAmount Implementation ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.HOURS) {
            return hours;
        }
        throw new UnsupportedOperationException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.HOURS);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(hours, ChronoUnit.HOURS);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(hours, ChronoUnit.HOURS);
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Hours other) {
        return Long.compare(hours, other.hours);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Hours other) {
            return hours == other.hours;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(hours);
    }

    @Override
    public String toString() {
        return "PT" + hours + "H";
    }
}

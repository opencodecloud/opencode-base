package cloud.opencode.base.date.extra;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

/**
 * Days unit class representing a number of days
 * 天数单位类，表示天数
 *
 * <p>This class represents a number of days, similar to Period but focused
 * on the days unit. It implements TemporalAmount for use with java.time.</p>
 * <p>此类表示天数，类似于Period但专注于天单位。实现TemporalAmount以与java.time配合使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe days representation - 类型安全的天数表示</li>
 *   <li>Arithmetic operations (plus, minus, multiply, divide) - 算术操作（加、减、乘、除）</li>
 *   <li>Conversion to Period, Duration, Hours, Weeks - 转换为Period、Duration、Hours、Weeks</li>
 *   <li>TemporalAmount implementation for java.time integration - 实现TemporalAmount以集成java.time</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Days d = Days.of(7);
 * Days d2 = Days.ofWeeks(2);  // 14 days
 * Period p = d.toPeriod();
 * LocalDate date = LocalDate.now().plus(d);
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
public final class Days implements TemporalAmount, Comparable<Days>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Days ZERO = new Days(0);
    public static final Days ONE = new Days(1);

    private final int days;

    private Days(int days) {
        this.days = days;
    }

    // ==================== Static Factory Methods ====================

    public static Days of(int days) {
        if (days == 0) {
            return ZERO;
        }
        if (days == 1) {
            return ONE;
        }
        return new Days(days);
    }

    public static Days of(long days) {
        return of(Math.toIntExact(days));
    }

    public static Days ofWeeks(int weeks) {
        return of(Math.multiplyExact(weeks, 7));
    }

    public static Days from(Period period) {
        Objects.requireNonNull(period, "period must not be null");
        return of(period.getDays());
    }

    public static Days between(Temporal startInclusive, Temporal endExclusive) {
        return of((int) ChronoUnit.DAYS.between(startInclusive, endExclusive));
    }

    // ==================== Getter Methods ====================

    public int getAmount() {
        return days;
    }

    public boolean isZero() {
        return days == 0;
    }

    public boolean isNegative() {
        return days < 0;
    }

    public boolean isPositive() {
        return days > 0;
    }

    // ==================== Calculation Methods ====================

    public Days plus(Days other) {
        return of(Math.addExact(days, other.days));
    }

    public Days plus(int daysToAdd) {
        return of(Math.addExact(days, daysToAdd));
    }

    public Days minus(Days other) {
        return of(Math.subtractExact(days, other.days));
    }

    public Days minus(int daysToSubtract) {
        return of(Math.subtractExact(days, daysToSubtract));
    }

    public Days multipliedBy(int multiplicand) {
        return of(Math.multiplyExact(days, multiplicand));
    }

    public Days dividedBy(int divisor) {
        return of(days / divisor);
    }

    public Days negated() {
        return of(Math.negateExact(days));
    }

    public Days abs() {
        return isNegative() ? negated() : this;
    }

    // ==================== Conversion Methods ====================

    public Period toPeriod() {
        return Period.ofDays(days);
    }

    public Duration toDuration() {
        return Duration.ofDays(days);
    }

    public Hours toHours() {
        return Hours.of(Math.multiplyExact(days, 24L));
    }

    public Weeks toWeeks() {
        return Weeks.of(days / 7);
    }

    // ==================== TemporalAmount Implementation ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.DAYS) {
            return days;
        }
        throw new UnsupportedOperationException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.DAYS);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(days, ChronoUnit.DAYS);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(days, ChronoUnit.DAYS);
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Days other) {
        return Integer.compare(days, other.days);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Days other) {
            return days == other.days;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(days);
    }

    @Override
    public String toString() {
        return "P" + days + "D";
    }
}

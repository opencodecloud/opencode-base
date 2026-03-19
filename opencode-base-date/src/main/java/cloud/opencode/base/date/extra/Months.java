package cloud.opencode.base.date.extra;

import java.io.Serial;
import java.io.Serializable;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.Objects;

/**
 * Months unit class representing a number of months
 * 月数单位类，表示月数
 *
 * <p>This class represents a number of months, similar to Period but focused
 * on the months unit. It implements TemporalAmount for use with java.time.</p>
 * <p>此类表示月数，类似于Period但专注于月单位。实现TemporalAmount以与java.time配合使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe months representation - 类型安全的月数表示</li>
 *   <li>Arithmetic operations (plus, minus, multiply, divide) - 算术操作（加、减、乘、除）</li>
 *   <li>Conversion to Period, Years - 转换为Period、Years</li>
 *   <li>TemporalAmount implementation for java.time integration - 实现TemporalAmount以集成java.time</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Months m = Months.of(6);
 * Months m2 = Months.ofYears(2);  // 24 months
 * Period p = m.toPeriod();
 * LocalDate date = LocalDate.now().plus(m);
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
public final class Months implements TemporalAmount, Comparable<Months>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Months ZERO = new Months(0);
    public static final Months ONE = new Months(1);

    private final int months;

    private Months(int months) {
        this.months = months;
    }

    // ==================== Static Factory Methods ====================

    public static Months of(int months) {
        if (months == 0) {
            return ZERO;
        }
        if (months == 1) {
            return ONE;
        }
        return new Months(months);
    }

    public static Months of(long months) {
        return of(Math.toIntExact(months));
    }

    public static Months ofYears(int years) {
        return of(Math.multiplyExact(years, 12));
    }

    public static Months from(Period period) {
        Objects.requireNonNull(period, "period must not be null");
        return of(period.toTotalMonths());
    }

    public static Months between(Temporal startInclusive, Temporal endExclusive) {
        return of((int) ChronoUnit.MONTHS.between(startInclusive, endExclusive));
    }

    // ==================== Getter Methods ====================

    public int getAmount() {
        return months;
    }

    public boolean isZero() {
        return months == 0;
    }

    public boolean isNegative() {
        return months < 0;
    }

    public boolean isPositive() {
        return months > 0;
    }

    // ==================== Calculation Methods ====================

    public Months plus(Months other) {
        return of(Math.addExact(months, other.months));
    }

    public Months plus(int monthsToAdd) {
        return of(Math.addExact(months, monthsToAdd));
    }

    public Months minus(Months other) {
        return of(Math.subtractExact(months, other.months));
    }

    public Months minus(int monthsToSubtract) {
        return of(Math.subtractExact(months, monthsToSubtract));
    }

    public Months multipliedBy(int multiplicand) {
        return of(Math.multiplyExact(months, multiplicand));
    }

    public Months dividedBy(int divisor) {
        return of(months / divisor);
    }

    public Months negated() {
        return of(Math.negateExact(months));
    }

    public Months abs() {
        return isNegative() ? negated() : this;
    }

    // ==================== Conversion Methods ====================

    public Period toPeriod() {
        return Period.ofMonths(months);
    }

    public Years toYears() {
        return Years.of(months / 12);
    }

    // ==================== TemporalAmount Implementation ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.MONTHS) {
            return months;
        }
        throw new UnsupportedOperationException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.MONTHS);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(months, ChronoUnit.MONTHS);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(months, ChronoUnit.MONTHS);
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Months other) {
        return Integer.compare(months, other.months);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Months other) {
            return months == other.months;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(months);
    }

    @Override
    public String toString() {
        return "P" + months + "M";
    }
}

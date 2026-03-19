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
 * Years unit class representing a number of years
 * 年数单位类，表示年数
 *
 * <p>This class represents a number of years, similar to Period but focused
 * on the years unit. It implements TemporalAmount for use with java.time.</p>
 * <p>此类表示年数，类似于Period但专注于年单位。实现TemporalAmount以与java.time配合使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe years representation - 类型安全的年数表示</li>
 *   <li>Arithmetic operations (plus, minus, multiply, divide) - 算术操作（加、减、乘、除）</li>
 *   <li>Conversion to Period, Months - 转换为Period、Months</li>
 *   <li>TemporalAmount implementation for java.time integration - 实现TemporalAmount以集成java.time</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Years y = Years.of(5);
 * Months m = y.toMonths();  // 60 months
 * Period p = y.toPeriod();
 * LocalDate date = LocalDate.now().plus(y);
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
public final class Years implements TemporalAmount, Comparable<Years>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Years ZERO = new Years(0);
    public static final Years ONE = new Years(1);

    private final int years;

    private Years(int years) {
        this.years = years;
    }

    // ==================== Static Factory Methods ====================

    public static Years of(int years) {
        if (years == 0) {
            return ZERO;
        }
        if (years == 1) {
            return ONE;
        }
        return new Years(years);
    }

    public static Years of(long years) {
        return of(Math.toIntExact(years));
    }

    public static Years from(Period period) {
        Objects.requireNonNull(period, "period must not be null");
        return of(period.getYears());
    }

    public static Years between(Temporal startInclusive, Temporal endExclusive) {
        return of((int) ChronoUnit.YEARS.between(startInclusive, endExclusive));
    }

    // ==================== Getter Methods ====================

    public int getAmount() {
        return years;
    }

    public boolean isZero() {
        return years == 0;
    }

    public boolean isNegative() {
        return years < 0;
    }

    public boolean isPositive() {
        return years > 0;
    }

    public boolean isLeap() {
        return years > 0 && java.time.Year.isLeap(years);
    }

    // ==================== Calculation Methods ====================

    public Years plus(Years other) {
        return of(Math.addExact(years, other.years));
    }

    public Years plus(int yearsToAdd) {
        return of(Math.addExact(years, yearsToAdd));
    }

    public Years minus(Years other) {
        return of(Math.subtractExact(years, other.years));
    }

    public Years minus(int yearsToSubtract) {
        return of(Math.subtractExact(years, yearsToSubtract));
    }

    public Years multipliedBy(int multiplicand) {
        return of(Math.multiplyExact(years, multiplicand));
    }

    public Years dividedBy(int divisor) {
        return of(years / divisor);
    }

    public Years negated() {
        return of(Math.negateExact(years));
    }

    public Years abs() {
        return isNegative() ? negated() : this;
    }

    // ==================== Conversion Methods ====================

    public Period toPeriod() {
        return Period.ofYears(years);
    }

    public Months toMonths() {
        return Months.of(Math.multiplyExact(years, 12));
    }

    // ==================== TemporalAmount Implementation ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.YEARS) {
            return years;
        }
        throw new UnsupportedOperationException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.YEARS);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(years, ChronoUnit.YEARS);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(years, ChronoUnit.YEARS);
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Years other) {
        return Integer.compare(years, other.years);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Years other) {
            return years == other.years;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(years);
    }

    @Override
    public String toString() {
        return "P" + years + "Y";
    }
}

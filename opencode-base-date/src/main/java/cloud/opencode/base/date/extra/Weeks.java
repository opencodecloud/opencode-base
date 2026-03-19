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
 * Weeks unit class representing a number of weeks
 * 周数单位类，表示周数
 *
 * <p>This class represents a number of weeks. It implements TemporalAmount
 * for use with java.time.</p>
 * <p>此类表示周数。实现TemporalAmount以与java.time配合使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe weeks representation - 类型安全的周数表示</li>
 *   <li>Arithmetic operations (plus, minus, multiply, divide) - 算术操作（加、减、乘、除）</li>
 *   <li>Conversion to Period, Days - 转换为Period、Days</li>
 *   <li>TemporalAmount implementation for java.time integration - 实现TemporalAmount以集成java.time</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Weeks w = Weeks.of(4);
 * Days d = w.toDays();  // 28 days
 * LocalDate date = LocalDate.now().plus(w);
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
public final class Weeks implements TemporalAmount, Comparable<Weeks>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Weeks ZERO = new Weeks(0);
    public static final Weeks ONE = new Weeks(1);

    private final int weeks;

    private Weeks(int weeks) {
        this.weeks = weeks;
    }

    // ==================== Static Factory Methods ====================

    public static Weeks of(int weeks) {
        if (weeks == 0) {
            return ZERO;
        }
        if (weeks == 1) {
            return ONE;
        }
        return new Weeks(weeks);
    }

    public static Weeks of(long weeks) {
        return of(Math.toIntExact(weeks));
    }

    public static Weeks ofDays(int days) {
        return of(days / 7);
    }

    public static Weeks between(Temporal startInclusive, Temporal endExclusive) {
        return of((int) ChronoUnit.WEEKS.between(startInclusive, endExclusive));
    }

    // ==================== Getter Methods ====================

    public int getAmount() {
        return weeks;
    }

    public boolean isZero() {
        return weeks == 0;
    }

    public boolean isNegative() {
        return weeks < 0;
    }

    public boolean isPositive() {
        return weeks > 0;
    }

    // ==================== Calculation Methods ====================

    public Weeks plus(Weeks other) {
        return of(Math.addExact(weeks, other.weeks));
    }

    public Weeks plus(int weeksToAdd) {
        return of(Math.addExact(weeks, weeksToAdd));
    }

    public Weeks minus(Weeks other) {
        return of(Math.subtractExact(weeks, other.weeks));
    }

    public Weeks minus(int weeksToSubtract) {
        return of(Math.subtractExact(weeks, weeksToSubtract));
    }

    public Weeks multipliedBy(int multiplicand) {
        return of(Math.multiplyExact(weeks, multiplicand));
    }

    public Weeks dividedBy(int divisor) {
        return of(weeks / divisor);
    }

    public Weeks negated() {
        return of(Math.negateExact(weeks));
    }

    public Weeks abs() {
        return isNegative() ? negated() : this;
    }

    // ==================== Conversion Methods ====================

    public Period toPeriod() {
        return Period.ofWeeks(weeks);
    }

    public Days toDays() {
        return Days.of(Math.multiplyExact(weeks, 7));
    }

    // ==================== TemporalAmount Implementation ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.WEEKS) {
            return weeks;
        }
        throw new UnsupportedOperationException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.WEEKS);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(weeks, ChronoUnit.WEEKS);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(weeks, ChronoUnit.WEEKS);
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Weeks other) {
        return Integer.compare(weeks, other.weeks);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Weeks other) {
            return weeks == other.weeks;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(weeks);
    }

    @Override
    public String toString() {
        return "P" + weeks + "W";
    }
}

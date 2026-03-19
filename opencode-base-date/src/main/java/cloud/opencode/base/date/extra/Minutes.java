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
 * Minutes unit class representing a number of minutes
 * 分钟单位类，表示分钟数
 *
 * <p>This class represents a number of minutes, similar to Duration but focused
 * on the minutes unit. It implements TemporalAmount for use with java.time.</p>
 * <p>此类表示分钟数，类似于Duration但专注于分钟单位。实现TemporalAmount以与java.time配合使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe minutes representation - 类型安全的分钟数表示</li>
 *   <li>Arithmetic operations (plus, minus, multiply, divide) - 算术操作（加、减、乘、除）</li>
 *   <li>Conversion to Duration, Seconds, Hours - 转换为Duration、Seconds、Hours</li>
 *   <li>TemporalAmount implementation for java.time integration - 实现TemporalAmount以集成java.time</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Minutes m = Minutes.of(30);
 * Minutes m2 = Minutes.ofHours(2);  // 120 minutes
 * Duration d = m.toDuration();
 * LocalDateTime dt = LocalDateTime.now().plus(m);
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
public final class Minutes implements TemporalAmount, Comparable<Minutes>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Minutes ZERO = new Minutes(0);

    private final long minutes;

    private Minutes(long minutes) {
        this.minutes = minutes;
    }

    // ==================== Static Factory Methods ====================

    public static Minutes of(long minutes) {
        if (minutes == 0) {
            return ZERO;
        }
        return new Minutes(minutes);
    }

    public static Minutes ofSeconds(long seconds) {
        return of(seconds / 60);
    }

    public static Minutes ofHours(long hours) {
        return of(Math.multiplyExact(hours, 60));
    }

    public static Minutes ofDays(long days) {
        return of(Math.multiplyExact(days, 1440));
    }

    public static Minutes from(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return of(duration.toMinutes());
    }

    public static Minutes between(Temporal startInclusive, Temporal endExclusive) {
        return of(ChronoUnit.MINUTES.between(startInclusive, endExclusive));
    }

    // ==================== Getter Methods ====================

    public long getAmount() {
        return minutes;
    }

    public boolean isZero() {
        return minutes == 0;
    }

    public boolean isNegative() {
        return minutes < 0;
    }

    public boolean isPositive() {
        return minutes > 0;
    }

    // ==================== Calculation Methods ====================

    public Minutes plus(Minutes other) {
        return of(Math.addExact(minutes, other.minutes));
    }

    public Minutes plus(long minutesToAdd) {
        return of(Math.addExact(minutes, minutesToAdd));
    }

    public Minutes minus(Minutes other) {
        return of(Math.subtractExact(minutes, other.minutes));
    }

    public Minutes minus(long minutesToSubtract) {
        return of(Math.subtractExact(minutes, minutesToSubtract));
    }

    public Minutes multipliedBy(long multiplicand) {
        return of(Math.multiplyExact(minutes, multiplicand));
    }

    public Minutes dividedBy(long divisor) {
        return of(minutes / divisor);
    }

    public Minutes negated() {
        return of(Math.negateExact(minutes));
    }

    public Minutes abs() {
        return isNegative() ? negated() : this;
    }

    // ==================== Conversion Methods ====================

    public Duration toDuration() {
        return Duration.ofMinutes(minutes);
    }

    public Seconds toSeconds() {
        return Seconds.of(Math.multiplyExact(minutes, 60));
    }

    public Hours toHours() {
        return Hours.of(minutes / 60);
    }

    // ==================== TemporalAmount Implementation ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.MINUTES) {
            return minutes;
        }
        throw new UnsupportedOperationException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.MINUTES);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(minutes, ChronoUnit.MINUTES);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(minutes, ChronoUnit.MINUTES);
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Minutes other) {
        return Long.compare(minutes, other.minutes);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Minutes other) {
            return minutes == other.minutes;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(minutes);
    }

    @Override
    public String toString() {
        return "PT" + minutes + "M";
    }
}

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
 * Seconds unit class representing a number of seconds
 * 秒数单位类，表示秒数
 *
 * <p>This class represents a number of seconds, similar to Duration but focused
 * on the seconds unit. It implements TemporalAmount for use with java.time.</p>
 * <p>此类表示秒数，类似于Duration但专注于秒单位。实现TemporalAmount以与java.time配合使用。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Type-safe seconds representation - 类型安全的秒数表示</li>
 *   <li>Arithmetic operations (plus, minus, multiply, divide) - 算术操作（加、减、乘、除）</li>
 *   <li>Conversion to Duration, Minutes, Hours - 转换为Duration、Minutes、Hours</li>
 *   <li>TemporalAmount implementation for java.time integration - 实现TemporalAmount以集成java.time</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Seconds s = Seconds.of(30);
 * Seconds s2 = Seconds.ofMinutes(2);  // 120 seconds
 * Duration d = s.toDuration();
 * LocalDateTime dt = LocalDateTime.now().plus(s);
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
public final class Seconds implements TemporalAmount, Comparable<Seconds>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Seconds ZERO = new Seconds(0);

    private final long seconds;

    private Seconds(long seconds) {
        this.seconds = seconds;
    }

    // ==================== Static Factory Methods ====================

    public static Seconds of(long seconds) {
        if (seconds == 0) {
            return ZERO;
        }
        return new Seconds(seconds);
    }

    public static Seconds ofMinutes(long minutes) {
        return of(Math.multiplyExact(minutes, 60));
    }

    public static Seconds ofHours(long hours) {
        return of(Math.multiplyExact(hours, 3600));
    }

    public static Seconds ofDays(long days) {
        return of(Math.multiplyExact(days, 86400));
    }

    public static Seconds from(Duration duration) {
        Objects.requireNonNull(duration, "duration must not be null");
        return of(duration.getSeconds());
    }

    public static Seconds between(Temporal startInclusive, Temporal endExclusive) {
        return of(ChronoUnit.SECONDS.between(startInclusive, endExclusive));
    }

    // ==================== Getter Methods ====================

    public long getAmount() {
        return seconds;
    }

    public boolean isZero() {
        return seconds == 0;
    }

    public boolean isNegative() {
        return seconds < 0;
    }

    public boolean isPositive() {
        return seconds > 0;
    }

    // ==================== Calculation Methods ====================

    public Seconds plus(Seconds other) {
        return of(Math.addExact(seconds, other.seconds));
    }

    public Seconds plus(long secondsToAdd) {
        return of(Math.addExact(seconds, secondsToAdd));
    }

    public Seconds minus(Seconds other) {
        return of(Math.subtractExact(seconds, other.seconds));
    }

    public Seconds minus(long secondsToSubtract) {
        return of(Math.subtractExact(seconds, secondsToSubtract));
    }

    public Seconds multipliedBy(long multiplicand) {
        return of(Math.multiplyExact(seconds, multiplicand));
    }

    public Seconds dividedBy(long divisor) {
        return of(seconds / divisor);
    }

    public Seconds negated() {
        return of(Math.negateExact(seconds));
    }

    public Seconds abs() {
        return isNegative() ? negated() : this;
    }

    // ==================== Conversion Methods ====================

    public Duration toDuration() {
        return Duration.ofSeconds(seconds);
    }

    public Minutes toMinutes() {
        return Minutes.of(seconds / 60);
    }

    public Hours toHours() {
        return Hours.of(seconds / 3600);
    }

    // ==================== TemporalAmount Implementation ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.SECONDS) {
            return seconds;
        }
        throw new UnsupportedOperationException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.SECONDS);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        return temporal.plus(seconds, ChronoUnit.SECONDS);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        return temporal.minus(seconds, ChronoUnit.SECONDS);
    }

    // ==================== Comparable Implementation ====================

    @Override
    public int compareTo(Seconds other) {
        return Long.compare(seconds, other.seconds);
    }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof Seconds other) {
            return seconds == other.seconds;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(seconds);
    }

    @Override
    public String toString() {
        return "PT" + seconds + "S";
    }
}

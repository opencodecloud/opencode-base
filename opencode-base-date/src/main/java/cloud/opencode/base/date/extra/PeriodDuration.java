package cloud.opencode.base.date.extra;

import cloud.opencode.base.date.exception.OpenDateException;

import java.io.Serial;
import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAmount;
import java.time.temporal.TemporalUnit;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.util.List;
import java.util.Objects;

/**
 * Period and Duration combination representing both date-based and time-based amounts
 * 周期和时长的组合，表示基于日期和基于时间的数量
 *
 * <p>This class represents a combination of Period (years, months, days) and Duration
 * (hours, minutes, seconds, nanos). It is modeled after ThreeTen-Extra's PeriodDuration.</p>
 * <p>此类表示Period（年、月、日）和Duration（小时、分钟、秒、纳秒）的组合。
 * 设计参考ThreeTen-Extra的PeriodDuration。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Combine Period and Duration - 组合Period和Duration</li>
 *   <li>Parse ISO 8601 format - 解析ISO 8601格式</li>
 *   <li>Calculate between two DateTimes - 计算两个日期时间之间的差</li>
 *   <li>Add/subtract from Temporal - 对Temporal进行加减</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create PeriodDuration
 * PeriodDuration pd = PeriodDuration.of(Period.ofMonths(2), Duration.ofHours(5));
 *
 * // Parse from ISO format
 * PeriodDuration pd2 = PeriodDuration.parse("P1Y2M3DT4H5M6S");
 *
 * // Calculate between dates
 * LocalDateTime start = LocalDateTime.of(2024, 1, 1, 9, 0);
 * LocalDateTime end = LocalDateTime.of(2024, 3, 15, 14, 30);
 * PeriodDuration between = PeriodDuration.between(start, end);
 *
 * // Apply to temporal
 * LocalDateTime result = pd.addTo(start);
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Immutable and thread-safe - 不可变且线程安全</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes - 线程安全: 是</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class PeriodDuration implements TemporalAmount, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Zero constant
     */
    public static final PeriodDuration ZERO = new PeriodDuration(Period.ZERO, Duration.ZERO);

    /**
     * The period part (years, months, days)
     */
    private final Period period;

    /**
     * The duration part (hours, minutes, seconds, nanos)
     */
    private final Duration duration;

    // ==================== Constructors | 构造函数 ====================

    private PeriodDuration(Period period, Duration duration) {
        this.period = period;
        this.duration = duration;
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a PeriodDuration from Period and Duration
     * 从Period和Duration创建PeriodDuration
     *
     * @param period   the period | 周期
     * @param duration the duration | 时长
     * @return the PeriodDuration instance | PeriodDuration实例
     * @throws NullPointerException if period or duration is null | 如果周期或时长为null则抛出异常
     */
    public static PeriodDuration of(Period period, Duration duration) {
        Objects.requireNonNull(period, "period must not be null");
        Objects.requireNonNull(duration, "duration must not be null");
        if (period.isZero() && duration.isZero()) {
            return ZERO;
        }
        return new PeriodDuration(period, duration);
    }

    /**
     * Creates a PeriodDuration from Period only
     * 仅从Period创建PeriodDuration
     *
     * @param period the period | 周期
     * @return the PeriodDuration instance | PeriodDuration实例
     */
    public static PeriodDuration ofPeriod(Period period) {
        return of(period, Duration.ZERO);
    }

    /**
     * Creates a PeriodDuration from Duration only
     * 仅从Duration创建PeriodDuration
     *
     * @param duration the duration | 时长
     * @return the PeriodDuration instance | PeriodDuration实例
     */
    public static PeriodDuration ofDuration(Duration duration) {
        return of(Period.ZERO, duration);
    }

    /**
     * Parses an ISO 8601 duration string
     * 解析ISO 8601时长字符串
     *
     * <p>Format: P[n]Y[n]M[n]DT[n]H[n]M[n]S (e.g., "P1Y2M3DT4H5M6S")</p>
     * <p>格式：P[n]Y[n]M[n]DT[n]H[n]M[n]S（例如："P1Y2M3DT4H5M6S"）</p>
     *
     * @param text the text to parse | 要解析的文本
     * @return the parsed PeriodDuration | 解析后的PeriodDuration
     * @throws OpenDateException if the text cannot be parsed | 如果文本无法解析则抛出异常
     */
    public static PeriodDuration parse(CharSequence text) {
        Objects.requireNonNull(text, "text must not be null");
        String str = text.toString().toUpperCase();

        if (!str.startsWith("P") && !str.startsWith("-P")) {
            throw OpenDateException.parseError(str, "ISO 8601 duration (P[n]Y[n]M[n]DT[n]H[n]M[n]S)");
        }

        try {
            boolean negative = str.startsWith("-");
            if (negative) {
                str = str.substring(1);
            }

            int tIndex = str.indexOf('T');
            Period period;
            Duration duration;

            if (tIndex < 0) {
                // Only period part
                period = Period.parse(str);
                duration = Duration.ZERO;
            } else if (tIndex == 1) {
                // Only duration part (PT...)
                period = Period.ZERO;
                duration = Duration.parse(str);
            } else {
                // Both period and duration parts
                String periodPart = str.substring(0, tIndex);
                String durationPart = "PT" + str.substring(tIndex + 1);
                period = Period.parse(periodPart);
                duration = Duration.parse(durationPart);
            }

            PeriodDuration result = of(period, duration);
            return negative ? result.negated() : result;
        } catch (DateTimeParseException e) {
            throw OpenDateException.parseError(str, "ISO 8601 duration", e);
        }
    }

    /**
     * Calculates the PeriodDuration between two LocalDateTimes
     * 计算两个LocalDateTime之间的PeriodDuration
     *
     * @param start the start datetime | 起始日期时间
     * @param end   the end datetime | 结束日期时间
     * @return the PeriodDuration between them | 它们之间的PeriodDuration
     */
    public static PeriodDuration between(LocalDateTime start, LocalDateTime end) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        Period period = Period.between(start.toLocalDate(), end.toLocalDate());
        Duration duration = Duration.between(
                start.toLocalTime(),
                end.toLocalTime()
        );

        // Adjust if duration is negative (end time is before start time)
        if (duration.isNegative() && !period.isNegative()) {
            period = period.minusDays(1);
            duration = duration.plusDays(1);
        }

        return of(period, duration);
    }

    // ==================== Getter Methods | 获取方法 ====================

    /**
     * Gets the period part
     * 获取周期部分
     *
     * @return the period | 周期
     */
    public Period getPeriod() {
        return period;
    }

    /**
     * Gets the duration part
     * 获取时长部分
     *
     * @return the duration | 时长
     */
    public Duration getDuration() {
        return duration;
    }

    /**
     * Checks if this is zero
     * 检查是否为零
     *
     * @return true if zero | 如果为零返回true
     */
    public boolean isZero() {
        return period.isZero() && duration.isZero();
    }

    /**
     * Checks if this is negative
     * 检查是否为负
     *
     * @return true if negative | 如果为负返回true
     */
    public boolean isNegative() {
        return period.isNegative() || duration.isNegative();
    }

    // ==================== Calculation Methods | 计算方法 ====================

    /**
     * Adds another PeriodDuration to this
     * 加上另一个PeriodDuration
     *
     * @param other the other PeriodDuration | 另一个PeriodDuration
     * @return a new PeriodDuration | 新的PeriodDuration
     */
    public PeriodDuration plus(PeriodDuration other) {
        Objects.requireNonNull(other, "other must not be null");
        return of(period.plus(other.period), duration.plus(other.duration));
    }

    /**
     * Subtracts another PeriodDuration from this
     * 减去另一个PeriodDuration
     *
     * @param other the other PeriodDuration | 另一个PeriodDuration
     * @return a new PeriodDuration | 新的PeriodDuration
     */
    public PeriodDuration minus(PeriodDuration other) {
        Objects.requireNonNull(other, "other must not be null");
        return of(period.minus(other.period), duration.minus(other.duration));
    }

    /**
     * Negates this PeriodDuration
     * 取负
     *
     * @return a new negated PeriodDuration | 新的取负后的PeriodDuration
     */
    public PeriodDuration negated() {
        return of(period.negated(), duration.negated());
    }

    /**
     * Multiplies this PeriodDuration by a scalar
     * 乘以标量
     *
     * @param scalar the scalar | 标量
     * @return a new PeriodDuration | 新的PeriodDuration
     */
    public PeriodDuration multipliedBy(int scalar) {
        if (scalar == 0) {
            return ZERO;
        }
        if (scalar == 1) {
            return this;
        }
        return of(period.multipliedBy(scalar), duration.multipliedBy(scalar));
    }

    /**
     * Normalizes this PeriodDuration
     * 标准化此PeriodDuration
     *
     * @return a new normalized PeriodDuration | 新的标准化后的PeriodDuration
     */
    public PeriodDuration normalized() {
        Period normalizedPeriod = period.normalized();
        // Duration is already normalized
        return of(normalizedPeriod, duration);
    }

    // ==================== TemporalAmount Implementation | TemporalAmount实现 ====================

    @Override
    public long get(TemporalUnit unit) {
        if (unit == ChronoUnit.YEARS) {
            return period.getYears();
        }
        if (unit == ChronoUnit.MONTHS) {
            return period.getMonths();
        }
        if (unit == ChronoUnit.DAYS) {
            return period.getDays();
        }
        if (unit == ChronoUnit.SECONDS) {
            return duration.getSeconds();
        }
        if (unit == ChronoUnit.NANOS) {
            return duration.getNano();
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public List<TemporalUnit> getUnits() {
        return List.of(ChronoUnit.YEARS, ChronoUnit.MONTHS, ChronoUnit.DAYS, ChronoUnit.SECONDS, ChronoUnit.NANOS);
    }

    @Override
    public Temporal addTo(Temporal temporal) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        return temporal.plus(period).plus(duration);
    }

    @Override
    public Temporal subtractFrom(Temporal temporal) {
        Objects.requireNonNull(temporal, "temporal must not be null");
        return temporal.minus(period).minus(duration);
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof PeriodDuration other) {
            return period.equals(other.period) && duration.equals(other.duration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(period, duration);
    }

    @Override
    public String toString() {
        if (isZero()) {
            return "PT0S";
        }
        if (duration.isZero()) {
            return period.toString();
        }
        if (period.isZero()) {
            return duration.toString();
        }
        // Combine: P1Y2M3DT4H5M6S
        String p = period.toString();
        String d = duration.toString().substring(1); // Remove leading 'P'
        return p + d;
    }
}

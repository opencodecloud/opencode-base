package cloud.opencode.base.date.extra;

import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQueries;
import java.time.temporal.TemporalQuery;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A year-half representation (e.g., 2024-H1, 2024-H2)
 * 年-半年表示（如2024-H1、2024-H2）
 *
 * <p>This class represents a half of a year, useful for bi-annual reporting
 * and financial period calculations.</p>
 * <p>此类表示一年中的半年，适用于半年度报告和财务周期计算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Year-half representation - 年-半年表示</li>
 *   <li>Parsing and formatting - 解析和格式化</li>
 *   <li>Date range operations - 日期范围操作</li>
 *   <li>Half-year arithmetic - 半年算术</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * YearHalf yh = YearHalf.of(2024, Half.H1);
 * YearHalf next = yh.plusHalves(1);  // 2024-H2
 *
 * LocalDate start = yh.atStart();    // 2024-01-01
 * LocalDate end = yh.atEnd();        // 2024-06-30
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable) - 线程安全: 是（不可变）</li>
 *   <li>Null-safe: Yes (with explicit null checks) - 空值安全: 是（有明确的空值检查）</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public final class YearHalf implements Temporal, TemporalAdjuster, Comparable<YearHalf>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final Pattern YEAR_HALF_PATTERN = Pattern.compile("\\d{4}-H[12]");

    /**
     * The year
     */
    private final int year;

    /**
     * The half (1 or 2)
     */
    private final Half half;

    // ==================== Half Enum | 半年枚举 ====================

    /**
     * Enum representing the two halves of a year
     * 表示一年两半的枚举
     */
    public enum Half {
        /**
         * First half (January - June)
         * 上半年（1月-6月）
         */
        H1(1),

        /**
         * Second half (July - December)
         * 下半年（7月-12月）
         */
        H2(2);

        private final int value;

        Half(int value) {
            this.value = value;
        }

        /**
         * Gets the half value (1 or 2)
         * 获取半年值（1或2）
         *
         * @return the value | 值
         */
        public int getValue() {
            return value;
        }

        /**
         * Gets the first month of this half
         * 获取此半年的第一个月
         *
         * @return the first month | 第一个月
         */
        public Month firstMonth() {
            return this == H1 ? Month.JANUARY : Month.JULY;
        }

        /**
         * Gets the last month of this half
         * 获取此半年的最后一个月
         *
         * @return the last month | 最后一个月
         */
        public Month lastMonth() {
            return this == H1 ? Month.JUNE : Month.DECEMBER;
        }

        /**
         * Gets a Half from value
         * 从值获取Half
         *
         * @param value the value (1 or 2) | 值（1或2）
         * @return the Half | Half
         */
        public static Half of(int value) {
            return switch (value) {
                case 1 -> H1;
                case 2 -> H2;
                default -> throw new DateTimeException("Invalid half value: " + value);
            };
        }

        /**
         * Gets the Half containing the specified month
         * 获取包含指定月份的Half
         *
         * @param month the month | 月份
         * @return the Half | Half
         */
        public static Half ofMonth(Month month) {
            return month.getValue() <= 6 ? H1 : H2;
        }

        /**
         * Gets the Half containing the specified month
         * 获取包含指定月份的Half
         *
         * @param month the month (1-12) | 月份（1-12）
         * @return the Half | Half
         */
        public static Half ofMonth(int month) {
            if (month < 1 || month > 12) {
                throw new DateTimeException("Invalid month value: " + month);
            }
            return month <= 6 ? H1 : H2;
        }
    }

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private YearHalf(int year, Half half) {
        this.year = year;
        this.half = Objects.requireNonNull(half, "half must not be null");
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates a YearHalf from year and half
     * 从年份和半年创建YearHalf
     *
     * @param year the year | 年份
     * @param half the half | 半年
     * @return the YearHalf | YearHalf
     */
    public static YearHalf of(int year, Half half) {
        return new YearHalf(year, half);
    }

    /**
     * Creates a YearHalf from year and half value
     * 从年份和半年值创建YearHalf
     *
     * @param year the year | 年份
     * @param half the half value (1 or 2) | 半年值（1或2）
     * @return the YearHalf | YearHalf
     */
    public static YearHalf of(int year, int half) {
        return new YearHalf(year, Half.of(half));
    }

    /**
     * Gets the current YearHalf
     * 获取当前YearHalf
     *
     * @return the current YearHalf | 当前YearHalf
     */
    public static YearHalf now() {
        return from(LocalDate.now());
    }

    /**
     * Creates a YearHalf from a TemporalAccessor
     * 从TemporalAccessor创建YearHalf
     *
     * @param temporal the temporal | 时间
     * @return the YearHalf | YearHalf
     */
    public static YearHalf from(TemporalAccessor temporal) {
        if (temporal instanceof YearHalf yh) {
            return yh;
        }
        int year = temporal.get(ChronoField.YEAR);
        int month = temporal.get(ChronoField.MONTH_OF_YEAR);
        return new YearHalf(year, Half.ofMonth(month));
    }

    /**
     * Parses a YearHalf from a string
     * 从字符串解析YearHalf
     *
     * @param text the text to parse (e.g., "2024-H1") | 要解析的文本
     * @return the YearHalf | YearHalf
     */
    public static YearHalf parse(CharSequence text) {
        String str = text.toString().toUpperCase();
        if (YEAR_HALF_PATTERN.matcher(str).matches()) {
            int year = Integer.parseInt(str.substring(0, 4));
            int half = Integer.parseInt(str.substring(6, 7));
            return of(year, half);
        }
        throw new DateTimeException("Cannot parse YearHalf: " + text);
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the year
     * 获取年份
     *
     * @return the year | 年份
     */
    public int getYear() {
        return year;
    }

    /**
     * Gets the half
     * 获取半年
     *
     * @return the half | 半年
     */
    public Half getHalf() {
        return half;
    }

    /**
     * Gets the half value (1 or 2)
     * 获取半年值（1或2）
     *
     * @return the half value | 半年值
     */
    public int getHalfValue() {
        return half.getValue();
    }

    // ==================== Date Operations | 日期操作 ====================

    /**
     * Gets the start date of this year-half
     * 获取此年-半年的开始日期
     *
     * @return the start date | 开始日期
     */
    public LocalDate atStart() {
        return LocalDate.of(year, half.firstMonth(), 1);
    }

    /**
     * Gets the end date of this year-half
     * 获取此年-半年的结束日期
     *
     * @return the end date | 结束日期
     */
    public LocalDate atEnd() {
        Month lastMonth = half.lastMonth();
        return LocalDate.of(year, lastMonth, lastMonth.length(Year.isLeap(year)));
    }

    /**
     * Gets a specific date in this year-half
     * 获取此年-半年中的特定日期
     *
     * @param month      the month (1-6 for H1, 7-12 for H2) | 月份
     * @param dayOfMonth the day of month | 日期
     * @return the date | 日期
     */
    public LocalDate atDay(int month, int dayOfMonth) {
        if (half == Half.H1 && (month < 1 || month > 6)) {
            throw new DateTimeException("Month must be 1-6 for H1");
        }
        if (half == Half.H2 && (month < 7 || month > 12)) {
            throw new DateTimeException("Month must be 7-12 for H2");
        }
        return LocalDate.of(year, month, dayOfMonth);
    }

    // ==================== Arithmetic | 算术 ====================

    /**
     * Adds halves to this year-half
     * 向此年-半年添加半年
     *
     * @param halves the halves to add | 要添加的半年数
     * @return the resulting year-half | 结果年-半年
     */
    public YearHalf plusHalves(long halves) {
        if (halves == 0) {
            return this;
        }
        long total = year * 2L + half.getValue() - 1 + halves;
        int newYear = (int) Math.floorDiv(total, 2);
        int newHalf = (int) Math.floorMod(total, 2) + 1;
        return of(newYear, newHalf);
    }

    /**
     * Subtracts halves from this year-half
     * 从此年-半年减去半年
     *
     * @param halves the halves to subtract | 要减去的半年数
     * @return the resulting year-half | 结果年-半年
     */
    public YearHalf minusHalves(long halves) {
        return plusHalves(-halves);
    }

    /**
     * Adds years to this year-half
     * 向此年-半年添加年
     *
     * @param years the years to add | 要添加的年数
     * @return the resulting year-half | 结果年-半年
     */
    public YearHalf plusYears(long years) {
        return of(Math.toIntExact(year + years), half);
    }

    /**
     * Subtracts years from this year-half
     * 从此年-半年减去年
     *
     * @param years the years to subtract | 要减去的年数
     * @return the resulting year-half | 结果年-半年
     */
    public YearHalf minusYears(long years) {
        return plusYears(-years);
    }

    // ==================== Length | 长度 ====================

    /**
     * Gets the length of this year-half in days
     * 获取此年-半年的天数
     *
     * @return the length in days | 天数
     */
    public int lengthInDays() {
        if (half == Half.H1) {
            return Year.isLeap(year) ? 182 : 181;
        }
        return 184;
    }

    /**
     * Gets the length of this year-half in months
     * 获取此年-半年的月数
     *
     * @return the length in months (always 6) | 月数（始终为6）
     */
    public int lengthInMonths() {
        return 6;
    }

    // ==================== Temporal Implementation | Temporal实现 ====================

    @Override
    public boolean isSupported(TemporalField field) {
        if (field instanceof ChronoField cf) {
            return cf == ChronoField.YEAR || cf == ChronoField.MONTH_OF_YEAR;
        }
        return field != null && field.isSupportedBy(this);
    }

    @Override
    public long getLong(TemporalField field) {
        if (field instanceof ChronoField cf) {
            return switch (cf) {
                case YEAR -> year;
                case MONTH_OF_YEAR -> half == Half.H1 ? 1 : 7;
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return field.getFrom(this);
    }

    @Override
    public ValueRange range(TemporalField field) {
        return Temporal.super.range(field);
    }

    @Override
    public int get(TemporalField field) {
        return Temporal.super.get(field);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <R> R query(TemporalQuery<R> query) {
        if (query == TemporalQueries.precision()) {
            return (R) java.time.temporal.ChronoUnit.MONTHS;
        }
        return Temporal.super.query(query);
    }

    @Override
    public Temporal with(TemporalField field, long newValue) {
        if (field instanceof ChronoField cf) {
            return switch (cf) {
                case YEAR -> of((int) newValue, half);
                default -> throw new UnsupportedTemporalTypeException("Unsupported field: " + field);
            };
        }
        return field.adjustInto(this, newValue);
    }

    @Override
    public Temporal plus(long amountToAdd, java.time.temporal.TemporalUnit unit) {
        if (unit == java.time.temporal.ChronoUnit.YEARS) {
            return plusYears(amountToAdd);
        }
        if (unit == java.time.temporal.ChronoUnit.MONTHS) {
            return plusHalves(amountToAdd / 6);
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public long until(Temporal endExclusive, java.time.temporal.TemporalUnit unit) {
        YearHalf end = from(endExclusive);
        if (unit == java.time.temporal.ChronoUnit.YEARS) {
            return end.year - year;
        }
        if (unit == java.time.temporal.ChronoUnit.MONTHS) {
            return (end.year - year) * 12L + (end.half.getValue() - half.getValue()) * 6L;
        }
        throw new UnsupportedTemporalTypeException("Unsupported unit: " + unit);
    }

    @Override
    public boolean isSupported(java.time.temporal.TemporalUnit unit) {
        return unit == java.time.temporal.ChronoUnit.YEARS ||
                unit == java.time.temporal.ChronoUnit.MONTHS;
    }

    // ==================== TemporalAdjuster Implementation | TemporalAdjuster实现 ====================

    @Override
    public Temporal adjustInto(Temporal temporal) {
        return temporal.with(ChronoField.YEAR, year)
                .with(ChronoField.MONTH_OF_YEAR, half.firstMonth().getValue())
                .with(ChronoField.DAY_OF_MONTH, 1);
    }

    // ==================== Comparable Implementation | Comparable实现 ====================

    @Override
    public int compareTo(YearHalf other) {
        int cmp = Integer.compare(year, other.year);
        if (cmp != 0) {
            return cmp;
        }
        return Integer.compare(half.getValue(), other.half.getValue());
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof YearHalf other)) return false;
        return year == other.year && half == other.half;
    }

    @Override
    public int hashCode() {
        return Objects.hash(year, half);
    }

    @Override
    public String toString() {
        return String.format("%d-H%d", year, half.getValue());
    }
}

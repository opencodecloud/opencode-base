package cloud.opencode.base.date.between;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Detailed age information including statistics and milestones
 * 详细年龄信息，包括统计数据和里程碑
 *
 * <p>This class provides comprehensive age-related information including
 * total time lived in various units, life statistics, and milestone tracking.</p>
 * <p>此类提供全面的年龄相关信息，包括各种单位的生存时间总计、
 * 生活统计数据和里程碑跟踪。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Total time in various units - 各种单位的总时间</li>
 *   <li>Life percentage calculations - 生命百分比计算</li>
 *   <li>Milestone tracking - 里程碑跟踪</li>
 *   <li>Statistics (weekends, holidays lived) - 统计数据（周末、假期）</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate birth = LocalDate.of(1990, 5, 15);
 * AgeDetail detail = AgeDetail.of(birth);
 *
 * System.out.println(detail.getTotalDays());     // Total days lived
 * System.out.println(detail.getTotalWeekends()); // Total weekend days
 * System.out.println(detail.getNextMilestone()); // Next milestone age
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
public final class AgeDetail {

    /**
     * Common milestone ages
     */
    private static final int[] MILESTONES = {1, 5, 10, 16, 18, 21, 25, 30, 40, 50, 60, 65, 70, 80, 90, 100};

    /**
     * The age between calculation
     */
    private final AgeBetween ageBetween;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private AgeDetail(AgeBetween ageBetween) {
        this.ageBetween = Objects.requireNonNull(ageBetween, "ageBetween must not be null");
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates an AgeDetail from an AgeBetween
     * 从AgeBetween创建AgeDetail
     *
     * @param ageBetween the AgeBetween | AgeBetween
     * @return the AgeDetail instance | AgeDetail实例
     */
    public static AgeDetail of(AgeBetween ageBetween) {
        return new AgeDetail(ageBetween);
    }

    /**
     * Creates an AgeDetail from a birth date
     * 从出生日期创建AgeDetail
     *
     * @param birthDate the birth date | 出生日期
     * @return the AgeDetail instance | AgeDetail实例
     */
    public static AgeDetail of(LocalDate birthDate) {
        return new AgeDetail(AgeBetween.fromBirth(birthDate));
    }

    /**
     * Creates an AgeDetail from a birth date to a reference date
     * 从出生日期到参考日期创建AgeDetail
     *
     * @param birthDate     the birth date | 出生日期
     * @param referenceDate the reference date | 参考日期
     * @return the AgeDetail instance | AgeDetail实例
     */
    public static AgeDetail of(LocalDate birthDate, LocalDate referenceDate) {
        return new AgeDetail(AgeBetween.at(birthDate, referenceDate));
    }

    // ==================== Total Time Getters | 总时间获取器 ====================

    /**
     * Gets the total days lived
     * 获取生存的总天数
     *
     * @return the total days | 总天数
     */
    public long getTotalDays() {
        return ageBetween.getTotalDays();
    }

    /**
     * Gets the total weeks lived
     * 获取生存的总周数
     *
     * @return the total weeks | 总周数
     */
    public long getTotalWeeks() {
        return ageBetween.getTotalWeeks();
    }

    /**
     * Gets the total months lived
     * 获取生存的总月数
     *
     * @return the total months | 总月数
     */
    public long getTotalMonths() {
        return ageBetween.getTotalMonths();
    }

    /**
     * Gets the total hours lived (approximate)
     * 获取生存的总小时数（近似值）
     *
     * @return the total hours | 总小时数
     */
    public long getTotalHours() {
        return getTotalDays() * 24;
    }

    /**
     * Gets the total minutes lived (approximate)
     * 获取生存的总分钟数（近似值）
     *
     * @return the total minutes | 总分钟数
     */
    public long getTotalMinutes() {
        return getTotalHours() * 60;
    }

    /**
     * Gets the total seconds lived (approximate)
     * 获取生存的总秒数（近似值）
     *
     * @return the total seconds | 总秒数
     */
    public long getTotalSeconds() {
        return getTotalMinutes() * 60;
    }

    // ==================== Statistics | 统计数据 ====================

    /**
     * Gets the total number of weekend days lived
     * 获取生存的周末天数总数
     *
     * @return the total weekend days | 周末天数
     */
    public long getTotalWeekends() {
        LocalDate start = ageBetween.getBirthDate();
        LocalDate end = ageBetween.getReferenceDate();
        long totalDays = ChronoUnit.DAYS.between(start, end) + 1;
        if (totalDays <= 0) return 0;
        long fullWeeks = totalDays / 7;
        long weekendDays = fullWeeks * 2;
        int remainder = (int) (totalDays % 7);
        DayOfWeek startDay = start.getDayOfWeek();
        for (int i = 0; i < remainder; i++) {
            DayOfWeek day = DayOfWeek.of(((startDay.getValue() - 1 + i) % 7) + 1);
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                weekendDays++;
            }
        }
        return weekendDays;
    }

    /**
     * Gets the estimated total weekend days lived (faster calculation)
     * 获取估计的周末天数（更快的计算）
     *
     * @return the estimated weekend days | 估计的周末天数
     */
    public long getEstimatedWeekends() {
        long totalDays = getTotalDays();
        // Approximately 2/7 of days are weekends
        return (totalDays * 2) / 7;
    }

    /**
     * Gets the number of leap years lived through
     * 获取经历的闰年数
     *
     * @return the leap year count | 闰年数
     */
    public int getLeapYearsLived() {
        LocalDate start = ageBetween.getBirthDate();
        LocalDate end = ageBetween.getReferenceDate();
        int count = 0;

        for (int year = start.getYear(); year <= end.getYear(); year++) {
            if (LocalDate.of(year, 1, 1).isLeapYear()) {
                count++;
            }
        }

        return count;
    }

    /**
     * Gets the number of birthdays celebrated
     * 获取庆祝的生日数
     *
     * @return the birthday count | 生日数
     */
    public int getBirthdaysCelebrated() {
        return ageBetween.getYears();
    }

    // ==================== Milestone Methods | 里程碑方法 ====================

    /**
     * Gets the next milestone age
     * 获取下一个里程碑年龄
     *
     * @return the next milestone age, or -1 if none | 下一个里程碑年龄，如果没有则返回-1
     */
    public int getNextMilestone() {
        int currentAge = ageBetween.getYears();
        for (int milestone : MILESTONES) {
            if (milestone > currentAge) {
                return milestone;
            }
        }
        return -1;
    }

    /**
     * Gets the date of the next milestone birthday
     * 获取下一个里程碑生日的日期
     *
     * @return the next milestone date, or null if none | 下一个里程碑日期，如果没有则返回null
     */
    public LocalDate getNextMilestoneDate() {
        int nextMilestone = getNextMilestone();
        if (nextMilestone < 0) {
            return null;
        }
        return ageBetween.getBirthDate().plusYears(nextMilestone);
    }

    /**
     * Gets the days until the next milestone birthday
     * 获取距离下一个里程碑生日的天数
     *
     * @return the days until next milestone, or -1 if none | 距离下一个里程碑的天数，如果没有则返回-1
     */
    public long getDaysUntilNextMilestone() {
        LocalDate nextMilestoneDate = getNextMilestoneDate();
        if (nextMilestoneDate == null) {
            return -1;
        }
        return ChronoUnit.DAYS.between(ageBetween.getReferenceDate(), nextMilestoneDate);
    }

    /**
     * Gets the last passed milestone age
     * 获取上一个通过的里程碑年龄
     *
     * @return the last milestone age, or -1 if none | 上一个里程碑年龄，如果没有则返回-1
     */
    public int getLastMilestone() {
        int currentAge = ageBetween.getYears();
        int lastMilestone = -1;
        for (int milestone : MILESTONES) {
            if (milestone <= currentAge) {
                lastMilestone = milestone;
            } else {
                break;
            }
        }
        return lastMilestone;
    }

    // ==================== Season Statistics | 季节统计 ====================

    /**
     * Gets the season the person was born in
     * 获取出生的季节
     *
     * @return the birth season | 出生季节
     */
    public String getBirthSeason() {
        Month month = ageBetween.getBirthDate().getMonth();
        return switch (month) {
            case MARCH, APRIL, MAY -> "Spring";
            case JUNE, JULY, AUGUST -> "Summer";
            case SEPTEMBER, OCTOBER, NOVEMBER -> "Autumn";
            case DECEMBER, JANUARY, FEBRUARY -> "Winter";
        };
    }

    /**
     * Gets the season the person was born in (Chinese)
     * 获取出生的季节（中文）
     *
     * @return the birth season in Chinese | 出生季节（中文）
     */
    public String getBirthSeasonChinese() {
        return switch (getBirthSeason()) {
            case "Spring" -> "春季";
            case "Summer" -> "夏季";
            case "Autumn" -> "秋季";
            case "Winter" -> "冬季";
            default -> "";
        };
    }

    // ==================== Life Expectancy | 预期寿命 ====================

    /**
     * Gets the percentage of life lived based on average life expectancy
     * 基于平均预期寿命获取已生存的百分比
     *
     * @param lifeExpectancy the life expectancy in years | 预期寿命（年）
     * @return the percentage of life lived | 已生存的百分比
     */
    public double getLifePercentage(int lifeExpectancy) {
        if (lifeExpectancy <= 0) {
            throw new IllegalArgumentException("lifeExpectancy must be positive");
        }
        return (ageBetween.getYears() * 100.0) / lifeExpectancy;
    }

    /**
     * Gets the estimated remaining years based on life expectancy
     * 基于预期寿命获取估计的剩余年数
     *
     * @param lifeExpectancy the life expectancy in years | 预期寿命（年）
     * @return the estimated remaining years | 估计的剩余年数
     */
    public int getEstimatedRemainingYears(int lifeExpectancy) {
        return Math.max(0, lifeExpectancy - ageBetween.getYears());
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the underlying AgeBetween
     * 获取底层的AgeBetween
     *
     * @return the AgeBetween | AgeBetween
     */
    public AgeBetween getAgeBetween() {
        return ageBetween;
    }

    // ==================== Formatting Methods | 格式化方法 ====================

    /**
     * Creates a detailed summary
     * 创建详细摘要
     *
     * @return the summary string | 摘要字符串
     */
    public String toSummary() {
        return String.format("""
                        Age Detail Summary
                        ==================
                        Age: %s
                        Birth Date: %s
                        Reference Date: %s

                        Time Lived:
                        - Total Days: %,d
                        - Total Weeks: %,d
                        - Total Months: %,d
                        - Total Hours: %,d

                        Statistics:
                        - Birthdays Celebrated: %d
                        - Leap Years Lived: %d
                        - Weekend Days: ~%,d

                        Milestones:
                        - Last Milestone: %d
                        - Next Milestone: %d
                        - Days Until Next: %,d

                        Zodiac:
                        - Western: %s (%s)
                        - Chinese: %s (%s)
                        - Birth Season: %s (%s)
                        """,
                ageBetween.format(),
                ageBetween.getBirthDate(),
                ageBetween.getReferenceDate(),
                getTotalDays(),
                getTotalWeeks(),
                getTotalMonths(),
                getTotalHours(),
                getBirthdaysCelebrated(),
                getLeapYearsLived(),
                getEstimatedWeekends(),
                getLastMilestone(),
                getNextMilestone(),
                getDaysUntilNextMilestone(),
                ageBetween.getZodiacSign(),
                ageBetween.getZodiacSignChinese(),
                ageBetween.getChineseZodiac(),
                ageBetween.getChineseZodiacChinese(),
                getBirthSeason(),
                getBirthSeasonChinese());
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgeDetail ageDetail)) return false;
        return Objects.equals(ageBetween, ageDetail.ageBetween);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ageBetween);
    }

    @Override
    public String toString() {
        return "AgeDetail[" + ageBetween.format() + "]";
    }
}

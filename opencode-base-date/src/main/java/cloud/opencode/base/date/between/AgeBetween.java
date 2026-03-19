package cloud.opencode.base.date.between;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Calculates age from a birth date to a reference date
 * 从出生日期到参考日期计算年龄
 *
 * <p>This class provides methods to calculate age in various formats,
 * supporting different calendar systems and precision levels.</p>
 * <p>此类提供多种格式计算年龄的方法，支持不同的日历系统和精度级别。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Age in years, months, days - 年龄（年、月、日）</li>
 *   <li>Total age in different units - 不同单位的总年龄</li>
 *   <li>Chinese zodiac and Western zodiac support - 生肖和星座支持</li>
 *   <li>Birthday checking - 生日检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * LocalDate birth = LocalDate.of(1990, 5, 15);
 *
 * // Calculate age from birth to today
 * AgeBetween age = AgeBetween.fromBirth(birth);
 * System.out.println(age.getYears());           // 34
 * System.out.println(age.isBirthdayToday());    // false
 *
 * // Calculate age at a specific date
 * LocalDate refDate = LocalDate.of(2024, 5, 15);
 * AgeBetween ageAtDate = AgeBetween.at(birth, refDate);
 * System.out.println(ageAtDate.isBirthdayToday()); // true
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
public final class AgeBetween {

    /**
     * The birth date
     */
    private final LocalDate birthDate;

    /**
     * The reference date for age calculation
     */
    private final LocalDate referenceDate;

    /**
     * The calculated period
     */
    private final Period period;

    // ==================== Constructors | 构造函数 ====================

    /**
     * Private constructor
     */
    private AgeBetween(LocalDate birthDate, LocalDate referenceDate) {
        this.birthDate = Objects.requireNonNull(birthDate, "birthDate must not be null");
        this.referenceDate = Objects.requireNonNull(referenceDate, "referenceDate must not be null");

        if (birthDate.isAfter(referenceDate)) {
            throw new IllegalArgumentException("birthDate cannot be after referenceDate");
        }

        this.period = Period.between(birthDate, referenceDate);
    }

    // ==================== Static Factory Methods | 静态工厂方法 ====================

    /**
     * Creates an AgeBetween from birth date to today
     * 从出生日期到今天创建AgeBetween
     *
     * @param birthDate the birth date | 出生日期
     * @return the AgeBetween instance | AgeBetween实例
     */
    public static AgeBetween fromBirth(LocalDate birthDate) {
        return new AgeBetween(birthDate, LocalDate.now());
    }

    /**
     * Creates an AgeBetween from birth date to today (from LocalDateTime)
     * 从出生日期时间到今天创建AgeBetween
     *
     * @param birthDateTime the birth date-time | 出生日期时间
     * @return the AgeBetween instance | AgeBetween实例
     */
    public static AgeBetween fromBirth(LocalDateTime birthDateTime) {
        return new AgeBetween(birthDateTime.toLocalDate(), LocalDate.now());
    }

    /**
     * Creates an AgeBetween from birth date to a specific reference date
     * 从出生日期到指定参考日期创建AgeBetween
     *
     * @param birthDate     the birth date | 出生日期
     * @param referenceDate the reference date | 参考日期
     * @return the AgeBetween instance | AgeBetween实例
     */
    public static AgeBetween at(LocalDate birthDate, LocalDate referenceDate) {
        return new AgeBetween(birthDate, referenceDate);
    }

    /**
     * Calculates age in years from birth date to today
     * 从出生日期到今天计算年龄（年）
     *
     * @param birthDate the birth date | 出生日期
     * @return the age in years | 年龄（年）
     */
    public static int ageInYears(LocalDate birthDate) {
        return fromBirth(birthDate).getYears();
    }

    // ==================== Age Getters | 年龄获取器 ====================

    /**
     * Gets the age in complete years
     * 获取完整年数的年龄
     *
     * @return the age in years | 年龄（年）
     */
    public int getYears() {
        return period.getYears();
    }

    /**
     * Gets the months component (0-11)
     * 获取月份组件（0-11）
     *
     * @return the months | 月数
     */
    public int getMonths() {
        return period.getMonths();
    }

    /**
     * Gets the days component (0-30)
     * 获取天数组件（0-30）
     *
     * @return the days | 天数
     */
    public int getDays() {
        return period.getDays();
    }

    /**
     * Gets the total age in months
     * 获取总月数的年龄
     *
     * @return the total months | 总月数
     */
    public long getTotalMonths() {
        return period.toTotalMonths();
    }

    /**
     * Gets the total age in days
     * 获取总天数的年龄
     *
     * @return the total days | 总天数
     */
    public long getTotalDays() {
        return ChronoUnit.DAYS.between(birthDate, referenceDate);
    }

    /**
     * Gets the total age in weeks
     * 获取总周数的年龄
     *
     * @return the total weeks | 总周数
     */
    public long getTotalWeeks() {
        return ChronoUnit.WEEKS.between(birthDate, referenceDate);
    }

    // ==================== Birthday Methods | 生日方法 ====================

    /**
     * Checks if today is the birthday
     * 检查今天是否是生日
     *
     * @return true if today is birthday | 如果今天是生日返回true
     */
    public boolean isBirthdayToday() {
        return birthDate.getMonthValue() == referenceDate.getMonthValue() &&
                birthDate.getDayOfMonth() == referenceDate.getDayOfMonth();
    }

    /**
     * Gets the next birthday date
     * 获取下一个生日日期
     *
     * @return the next birthday | 下一个生日
     */
    public LocalDate getNextBirthday() {
        LocalDate thisYearBirthday = adjustBirthdayToYear(referenceDate.getYear());
        if (thisYearBirthday.isAfter(referenceDate)) {
            return thisYearBirthday;
        }
        return adjustBirthdayToYear(referenceDate.getYear() + 1);
    }

    /**
     * Adjusts a birthday to a specific year, handling Feb 29 birthdays in non-leap years
     * by falling back to Feb 28.
     * 将生日调整到指定年份，闰年2月29日生日在非闰年时回退到2月28日。
     */
    private LocalDate adjustBirthdayToYear(int year) {
        if (birthDate.getMonthValue() == 2 && birthDate.getDayOfMonth() == 29
                && !java.time.Year.of(year).isLeap()) {
            return LocalDate.of(year, 2, 28);
        }
        return birthDate.withYear(year);
    }

    /**
     * Gets the number of days until the next birthday
     * 获取距离下一个生日的天数
     *
     * @return the days until next birthday | 距离下一个生日的天数
     */
    public long getDaysUntilNextBirthday() {
        return ChronoUnit.DAYS.between(referenceDate, getNextBirthday());
    }

    /**
     * Gets the last birthday date
     * 获取上一个生日日期
     *
     * @return the last birthday | 上一个生日
     */
    public LocalDate getLastBirthday() {
        LocalDate thisYearBirthday = adjustBirthdayToYear(referenceDate.getYear());
        if (thisYearBirthday.isAfter(referenceDate)) {
            return adjustBirthdayToYear(referenceDate.getYear() - 1);
        }
        return thisYearBirthday;
    }

    // ==================== Zodiac Methods | 星座方法 ====================

    /**
     * Gets the Western zodiac sign
     * 获取西方星座
     *
     * @return the zodiac sign | 星座
     */
    public String getZodiacSign() {
        int month = birthDate.getMonthValue();
        int day = birthDate.getDayOfMonth();

        return switch (month) {
            case 1 -> day <= 19 ? "Capricorn" : "Aquarius";
            case 2 -> day <= 18 ? "Aquarius" : "Pisces";
            case 3 -> day <= 20 ? "Pisces" : "Aries";
            case 4 -> day <= 19 ? "Aries" : "Taurus";
            case 5 -> day <= 20 ? "Taurus" : "Gemini";
            case 6 -> day <= 20 ? "Gemini" : "Cancer";
            case 7 -> day <= 22 ? "Cancer" : "Leo";
            case 8 -> day <= 22 ? "Leo" : "Virgo";
            case 9 -> day <= 22 ? "Virgo" : "Libra";
            case 10 -> day <= 22 ? "Libra" : "Scorpio";
            case 11 -> day <= 21 ? "Scorpio" : "Sagittarius";
            case 12 -> day <= 21 ? "Sagittarius" : "Capricorn";
            default -> throw new IllegalStateException("Invalid month: " + month);
        };
    }

    /**
     * Gets the Western zodiac sign in Chinese
     * 获取西方星座（中文）
     *
     * @return the zodiac sign in Chinese | 星座（中文）
     */
    public String getZodiacSignChinese() {
        return switch (getZodiacSign()) {
            case "Capricorn" -> "摩羯座";
            case "Aquarius" -> "水瓶座";
            case "Pisces" -> "双鱼座";
            case "Aries" -> "白羊座";
            case "Taurus" -> "金牛座";
            case "Gemini" -> "双子座";
            case "Cancer" -> "巨蟹座";
            case "Leo" -> "狮子座";
            case "Virgo" -> "处女座";
            case "Libra" -> "天秤座";
            case "Scorpio" -> "天蝎座";
            case "Sagittarius" -> "射手座";
            default -> "";
        };
    }

    /**
     * Gets the Chinese zodiac animal
     * 获取中国生肖
     *
     * @return the Chinese zodiac | 生肖
     */
    public String getChineseZodiac() {
        String[] animals = {"Monkey", "Rooster", "Dog", "Pig", "Rat", "Ox",
                "Tiger", "Rabbit", "Dragon", "Snake", "Horse", "Goat"};
        int year = birthDate.getYear();
        return animals[year % 12];
    }

    /**
     * Gets the Chinese zodiac animal in Chinese
     * 获取中国生肖（中文）
     *
     * @return the Chinese zodiac in Chinese | 生肖（中文）
     */
    public String getChineseZodiacChinese() {
        String[] animals = {"猴", "鸡", "狗", "猪", "鼠", "牛",
                "虎", "兔", "龙", "蛇", "马", "羊"};
        int year = birthDate.getYear();
        return animals[year % 12];
    }

    // ==================== Detail Methods | 详细方法 ====================

    /**
     * Gets a detailed age breakdown
     * 获取详细的年龄分解
     *
     * @return the AgeDetail | 年龄详情
     */
    public AgeDetail toDetail() {
        return AgeDetail.of(this);
    }

    // ==================== Getters | 获取器 ====================

    /**
     * Gets the birth date
     * 获取出生日期
     *
     * @return the birth date | 出生日期
     */
    public LocalDate getBirthDate() {
        return birthDate;
    }

    /**
     * Gets the reference date
     * 获取参考日期
     *
     * @return the reference date | 参考日期
     */
    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    /**
     * Gets the period
     * 获取Period
     *
     * @return the period | Period
     */
    public Period getPeriod() {
        return period;
    }

    // ==================== Formatting Methods | 格式化方法 ====================

    /**
     * Formats the age as a string
     * 将年龄格式化为字符串
     *
     * @return the formatted age | 格式化的年龄
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        int years = getYears();
        int months = getMonths();
        int days = getDays();

        if (years > 0) {
            sb.append(years).append(" year").append(years > 1 ? "s" : "");
        }
        if (months > 0) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(months).append(" month").append(months > 1 ? "s" : "");
        }
        if (days > 0) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append(days).append(" day").append(days > 1 ? "s" : "");
        }

        if (sb.isEmpty()) {
            return "0 days";
        }

        return sb.toString();
    }

    /**
     * Formats the age as a Chinese string
     * 将年龄格式化为中文字符串
     *
     * @return the formatted age in Chinese | 格式化的年龄（中文）
     */
    public String formatChinese() {
        StringBuilder sb = new StringBuilder();
        int years = getYears();
        int months = getMonths();
        int days = getDays();

        if (years > 0) {
            sb.append(years).append("岁");
        }
        if (months > 0) {
            sb.append(months).append("个月");
        }
        if (days > 0) {
            sb.append(days).append("天");
        }

        if (sb.isEmpty()) {
            return "0天";
        }

        return sb.toString();
    }

    // ==================== Object Methods | Object方法 ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AgeBetween that)) return false;
        return Objects.equals(birthDate, that.birthDate) &&
                Objects.equals(referenceDate, that.referenceDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(birthDate, referenceDate);
    }

    @Override
    public String toString() {
        return "Age: " + format() + " (born " + birthDate + ")";
    }
}

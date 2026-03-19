package cloud.opencode.base.lunar.calendar;

import java.time.LocalDate;
import java.util.List;

/**
 * Holiday
 * 法定假日
 *
 * <p>Chinese legal holidays with vacation days.</p>
 * <p>中国法定假日及放假天数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Legal holiday definitions - 法定假日定义</li>
 *   <li>Vacation day tracking - 放假天数跟踪</li>
 *   <li>Solar/lunar holiday distinction - 公历/农历假日区分</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Holiday spring = Holiday.SPRING_FESTIVAL;
 * int days = spring.vacationDays();  // 3
 * boolean isLunar = spring.isLunar();  // true
 * List<Holiday> all = Holiday.getAllHolidays();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (name fields must not be null) - 空值安全: 否（名称字段不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public record Holiday(
    String name,
    String englishName,
    int vacationDays,
    boolean isLunar
) {

    // ============ Legal Holidays | 法定假日 ============

    /**
     * New Year's Day | 元旦 (1 day)
     */
    public static final Holiday NEW_YEAR = new Holiday("元旦", "New Year's Day", 1, false);

    /**
     * Spring Festival | 春节 (3 days)
     */
    public static final Holiday SPRING_FESTIVAL = new Holiday("春节", "Spring Festival", 3, true);

    /**
     * Qingming Festival | 清明节 (1 day)
     */
    public static final Holiday QINGMING = new Holiday("清明节", "Qingming Festival", 1, false);

    /**
     * Labor Day | 劳动节 (1 day)
     */
    public static final Holiday LABOR_DAY = new Holiday("劳动节", "Labor Day", 1, false);

    /**
     * Dragon Boat Festival | 端午节 (1 day)
     */
    public static final Holiday DRAGON_BOAT = new Holiday("端午节", "Dragon Boat Festival", 1, true);

    /**
     * Mid-Autumn Festival | 中秋节 (1 day)
     */
    public static final Holiday MID_AUTUMN = new Holiday("中秋节", "Mid-Autumn Festival", 1, true);

    /**
     * National Day | 国庆节 (3 days)
     */
    public static final Holiday NATIONAL_DAY = new Holiday("国庆节", "National Day", 3, false);

    /**
     * All legal holidays
     */
    private static final List<Holiday> ALL_HOLIDAYS = List.of(
        NEW_YEAR, SPRING_FESTIVAL, QINGMING, LABOR_DAY,
        DRAGON_BOAT, MID_AUTUMN, NATIONAL_DAY
    );

    /**
     * Get all legal holidays
     * 获取所有法定假日
     *
     * @return list of holidays | 假日列表
     */
    public static List<Holiday> getAll() {
        return ALL_HOLIDAYS;
    }

    /**
     * Get total vacation days per year
     * 获取每年总法定假日天数
     *
     * @return total days | 总天数
     */
    public static int getTotalVacationDays() {
        return ALL_HOLIDAYS.stream()
            .mapToInt(Holiday::vacationDays)
            .sum();
    }

    /**
     * Check if this is a lunar-based holiday
     * 检查是否为农历节日
     *
     * @return true if lunar | 如果是农历返回true
     */
    public boolean isLunarBased() {
        return isLunar;
    }

    /**
     * Check if this is a solar-based holiday
     * 检查是否为公历节日
     *
     * @return true if solar | 如果是公历返回true
     */
    public boolean isSolarBased() {
        return !isLunar;
    }

    @Override
    public String toString() {
        return name + " (" + vacationDays + "天)";
    }
}

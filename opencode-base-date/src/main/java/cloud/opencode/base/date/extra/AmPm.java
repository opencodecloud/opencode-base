package cloud.opencode.base.date.extra;

import java.time.LocalTime;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalQuery;
import java.time.temporal.ValueRange;

/**
 * AmPm enum representing AM (ante meridiem) and PM (post meridiem)
 * 上午/下午枚举，表示上午（AM）和下午（PM）
 *
 * <p>This enum represents the two halves of the day: AM (00:00-11:59) and PM (12:00-23:59).</p>
 * <p>此枚举表示一天的两半：上午（00:00-11:59）和下午（12:00-23:59）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>AM/PM representation with bilingual names - 上午/下午表示，支持双语名称</li>
 *   <li>Create from hour, value, or TemporalAccessor - 从小时、值或TemporalAccessor创建</li>
 *   <li>TemporalAccessor and TemporalQuery implementations - 实现TemporalAccessor和TemporalQuery</li>
 *   <li>Hour range queries (firstHour, lastHour) - 小时范围查询</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * AmPm am = AmPm.AM;
 * AmPm pm = AmPm.PM;
 * AmPm current = AmPm.from(LocalTime.now());
 * boolean isAm = current.isAm();
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (enum is inherently thread-safe) - 线程安全: 是（枚举本身是线程安全的）</li>
 *   <li>Immutable: Yes - 不可变: 是</li>
 * </ul>
 *
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-date V1.0.0
 */
public enum AmPm implements TemporalAccessor, TemporalQuery<AmPm> {

    /**
     * AM - Ante Meridiem (before noon, 00:00-11:59)
     * 上午（00:00-11:59）
     */
    AM(0, "AM", "上午"),

    /**
     * PM - Post Meridiem (after noon, 12:00-23:59)
     * 下午（12:00-23:59）
     */
    PM(1, "PM", "下午");

    private final int value;
    private final String shortName;
    private final String chineseName;

    AmPm(int value, String shortName, String chineseName) {
        this.value = value;
        this.shortName = shortName;
        this.chineseName = chineseName;
    }

    // ==================== Static Factory Methods ====================

    /**
     * Gets the AmPm from a value (0=AM, 1=PM)
     * 从值获取AmPm（0=AM，1=PM）
     *
     * @param value the value (0 or 1) | 值（0或1）
     * @return the AmPm | 上午/下午
     */
    public static AmPm of(int value) {
        return value == 0 ? AM : PM;
    }

    /**
     * Gets the AmPm from an hour (0-23)
     * 从小时获取AmPm（0-23）
     *
     * @param hourOfDay the hour of day (0-23) | 一天中的小时（0-23）
     * @return the AmPm | 上午/下午
     */
    public static AmPm ofHour(int hourOfDay) {
        return hourOfDay < 12 ? AM : PM;
    }

    /**
     * Gets the AmPm from a TemporalAccessor
     * 从TemporalAccessor获取AmPm
     *
     * @param temporal the temporal accessor | 时间访问器
     * @return the AmPm | 上午/下午
     */
    public static AmPm from(TemporalAccessor temporal) {
        if (temporal instanceof AmPm amPm) {
            return amPm;
        }
        int hour = temporal.get(ChronoField.HOUR_OF_DAY);
        return ofHour(hour);
    }

    /**
     * Gets the current AmPm
     * 获取当前的上午/下午
     *
     * @return the current AmPm | 当前的上午/下午
     */
    public static AmPm now() {
        return from(LocalTime.now());
    }

    // ==================== Getter Methods ====================

    /**
     * Gets the value (0=AM, 1=PM)
     * 获取值（0=AM，1=PM）
     *
     * @return the value | 值
     */
    public int getValue() {
        return value;
    }

    /**
     * Gets the short name (AM/PM)
     * 获取短名称（AM/PM）
     *
     * @return the short name | 短名称
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Gets the Chinese name (上午/下午)
     * 获取中文名称（上午/下午）
     *
     * @return the Chinese name | 中文名称
     */
    public String getChineseName() {
        return chineseName;
    }

    /**
     * Checks if this is AM
     * 检查是否为上午
     *
     * @return true if AM | 如果是上午返回true
     */
    public boolean isAm() {
        return this == AM;
    }

    /**
     * Checks if this is PM
     * 检查是否为下午
     *
     * @return true if PM | 如果是下午返回true
     */
    public boolean isPm() {
        return this == PM;
    }

    /**
     * Gets the first hour of this period (0 for AM, 12 for PM)
     * 获取此时段的第一个小时（AM为0，PM为12）
     *
     * @return the first hour | 第一个小时
     */
    public int firstHour() {
        return this == AM ? 0 : 12;
    }

    /**
     * Gets the last hour of this period (11 for AM, 23 for PM)
     * 获取此时段的最后一个小时（AM为11，PM为23）
     *
     * @return the last hour | 最后一个小时
     */
    public int lastHour() {
        return this == AM ? 11 : 23;
    }

    // ==================== Calculation Methods ====================

    /**
     * Gets the opposite period
     * 获取相反的时段
     *
     * @return AM if this is PM, PM if this is AM | 如果是PM返回AM，如果是AM返回PM
     */
    public AmPm opposite() {
        return this == AM ? PM : AM;
    }

    // ==================== TemporalAccessor Implementation ====================

    @Override
    public boolean isSupported(TemporalField field) {
        return field == ChronoField.AMPM_OF_DAY;
    }

    @Override
    public long getLong(TemporalField field) {
        if (field == ChronoField.AMPM_OF_DAY) {
            return value;
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ChronoField.AMPM_OF_DAY) {
            return ValueRange.of(0, 1);
        }
        throw new UnsupportedOperationException("Unsupported field: " + field);
    }

    // ==================== TemporalQuery Implementation ====================

    @Override
    public AmPm queryFrom(TemporalAccessor temporal) {
        return from(temporal);
    }

    /**
     * Gets a query for extracting the AmPm from a temporal
     * 获取从时间对象提取AmPm的查询
     *
     * @return the AmPm query | AmPm查询
     */
    public static TemporalQuery<AmPm> query() {
        return AmPm::from;
    }

    // ==================== Display Methods ====================

    /**
     * Returns the display name
     * 返回显示名称
     *
     * @return the display name | 显示名称
     */
    public String getDisplayName() {
        return shortName;
    }
}

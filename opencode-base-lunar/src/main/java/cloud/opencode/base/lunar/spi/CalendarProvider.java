/*
 * Copyright 2025 OpenCode Cloud Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cloud.opencode.base.lunar.spi;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.calendar.SolarTerm;

import java.time.LocalDate;
import java.util.List;

/**
 * Calendar Provider SPI - Interface for calendar data providers
 * 日历提供者 SPI - 日历数据提供者接口
 *
 * <p>Allows custom implementations for lunar-solar date conversions and
 * calendar data retrieval. Implementations can provide different algorithms
 * or data sources for calendar calculations.</p>
 * <p>允许自定义农历-公历日期转换和日历数据获取的实现。
 * 实现可以提供不同的算法或数据源用于日历计算。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom date conversion algorithms - 自定义日期转换算法</li>
 *   <li>Solar term data provision - 节气数据提供</li>
 *   <li>ServiceLoader-based discovery - 基于ServiceLoader的发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register via META-INF/services/cloud.opencode.base.lunar.spi.CalendarProvider
 * public class MyCalendarProvider implements CalendarProvider {
 *     // implement methods
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Implementation dependent - 线程安全: 取决于实现</li>
 *   <li>Null-safe: No (return values must not be null) - 空值安全: 否（返回值不能为null）</li>
 * </ul>
 *
 * <p><strong>SPI Registration | SPI注册:</strong></p>
 * <p>Create file: META-INF/services/cloud.opencode.base.lunar.spi.CalendarProvider</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public interface CalendarProvider {

    /**
     * Get provider name
     * 获取提供者名称
     *
     * @return the provider name | 提供者名称
     */
    String getName();

    /**
     * Convert solar date to lunar date
     * 将公历日期转换为农历日期
     *
     * @param solar the solar date | 公历日期
     * @return the lunar date | 农历日期
     */
    LunarDate solarToLunar(SolarDate solar);

    /**
     * Convert lunar date to solar date
     * 将农历日期转换为公历日期
     *
     * @param lunar the lunar date | 农历日期
     * @return the solar date | 公历日期
     */
    SolarDate lunarToSolar(LunarDate lunar);

    /**
     * Get solar term for date
     * 获取日期的节气
     *
     * @param date the date | 日期
     * @return the solar term, or null if not a solar term day | 节气
     */
    SolarTerm getSolarTerm(LocalDate date);

    /**
     * Get all solar terms for a year
     * 获取一年的所有节气
     *
     * @param year the year | 年
     * @return list of solar term dates | 节气日期列表
     */
    List<LocalDate> getSolarTermDates(int year);

    /**
     * Check if date is leap month
     * 检查日期是否为闰月
     *
     * @param year  the lunar year | 农历年
     * @param month the lunar month | 农历月
     * @return true if leap month | 如果是闰月返回true
     */
    boolean isLeapMonth(int year, int month);

    /**
     * Get leap month for year
     * 获取年份的闰月
     *
     * @param year the lunar year | 农历年
     * @return the leap month (1-12), or 0 if no leap month | 闰月
     */
    int getLeapMonth(int year);

    /**
     * Get days in lunar month
     * 获取农历月的天数
     *
     * @param year        the lunar year | 农历年
     * @param month       the lunar month | 农历月
     * @param isLeapMonth whether leap month | 是否闰月
     * @return the number of days | 天数
     */
    int getDaysInLunarMonth(int year, int month, boolean isLeapMonth);

    /**
     * Get provider priority
     * 获取提供者优先级
     *
     * @return priority (lower is higher) | 优先级
     */
    default int getPriority() {
        return 100;
    }

    /**
     * Get supported year range start
     * 获取支持的年份范围起始
     *
     * @return start year | 起始年份
     */
    default int getSupportedYearStart() {
        return 1900;
    }

    /**
     * Get supported year range end
     * 获取支持的年份范围结束
     *
     * @return end year | 结束年份
     */
    default int getSupportedYearEnd() {
        return 2100;
    }
}

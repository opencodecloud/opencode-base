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

package cloud.opencode.base.lunar.calendar;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.internal.LunarCalculator;

import java.time.LocalDate;

/**
 * Date Converter - Converts between lunar and solar dates
 * 日期转换器 - 农历和公历日期之间的转换
 *
 * <p>Provides utility methods for converting between lunar (Chinese) calendar
 * dates and solar (Gregorian) calendar dates.</p>
 * <p>提供农历（中国历法）日期和公历（格里高利历）日期之间转换的工具方法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar to lunar conversion - 公历转农历</li>
 *   <li>Lunar to solar conversion - 农历转公历</li>
 *   <li>LocalDate integration - LocalDate集成</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Solar to Lunar
 * LunarDate lunar = DateConverter.solarToLunar(2024, 2, 10);
 *
 * // Lunar to Solar
 * SolarDate solar = DateConverter.lunarToSolar(2024, 1, 1, false);
 *
 * // Using LocalDate
 * LunarDate lunar = DateConverter.toLunar(LocalDate.now());
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (date arguments must not be null) - 空值安全: 否（日期参数不能为null）</li>
 * </ul>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Time complexity: O(1) per conversion using lookup tables - 每次转换 O(1), 使用查找表</li>
 *   <li>Space complexity: O(1) - O(1)</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public final class DateConverter {

    private DateConverter() {
    }

    /**
     * Converts solar date to lunar date
     * 将公历日期转换为农历日期
     *
     * @param year  the solar year | 公历年
     * @param month the solar month | 公历月
     * @param day   the solar day | 公历日
     * @return the lunar date | 农历日期
     */
    public static LunarDate solarToLunar(int year, int month, int day) {
        return LunarCalculator.solarToLunar(new SolarDate(year, month, day).toLocalDate());
    }

    /**
     * Converts solar date to lunar date
     * 将公历日期转换为农历日期
     *
     * @param solar the solar date | 公历日期
     * @return the lunar date | 农历日期
     */
    public static LunarDate solarToLunar(SolarDate solar) {
        return LunarCalculator.solarToLunar(solar.toLocalDate());
    }

    /**
     * Converts LocalDate to lunar date
     * 将LocalDate转换为农历日期
     *
     * @param date the local date | 本地日期
     * @return the lunar date | 农历日期
     */
    public static LunarDate toLunar(LocalDate date) {
        return solarToLunar(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    /**
     * Converts lunar date to solar date
     * 将农历日期转换为公历日期
     *
     * @param year        the lunar year | 农历年
     * @param month       the lunar month | 农历月
     * @param day         the lunar day | 农历日
     * @param isLeapMonth whether leap month | 是否闰月
     * @return the solar date | 公历日期
     */
    public static SolarDate lunarToSolar(int year, int month, int day, boolean isLeapMonth) {
        return LunarCalculator.lunarToSolar(new LunarDate(year, month, day, isLeapMonth));
    }

    /**
     * Converts lunar date to solar date
     * 将农历日期转换为公历日期
     *
     * @param lunar the lunar date | 农历日期
     * @return the solar date | 公历日期
     */
    public static SolarDate lunarToSolar(LunarDate lunar) {
        return LunarCalculator.lunarToSolar(lunar);
    }

    /**
     * Converts lunar date to LocalDate
     * 将农历日期转换为LocalDate
     *
     * @param lunar the lunar date | 农历日期
     * @return the local date | 本地日期
     */
    public static LocalDate toLocalDate(LunarDate lunar) {
        SolarDate solar = lunarToSolar(lunar);
        return LocalDate.of(solar.year(), solar.month(), solar.day());
    }

    /**
     * Gets the lunar date for today
     * 获取今天的农历日期
     *
     * @return today's lunar date | 今天的农历日期
     */
    public static LunarDate today() {
        return toLunar(LocalDate.now());
    }
}

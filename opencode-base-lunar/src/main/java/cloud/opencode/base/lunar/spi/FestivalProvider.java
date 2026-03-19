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
import cloud.opencode.base.lunar.calendar.Festival;

import java.time.LocalDate;
import java.util.List;

/**
 * Festival Provider SPI - Interface for festival data providers
 * 节日提供者 SPI - 节日数据提供者接口
 *
 * <p>Allows custom implementations for festival data retrieval. Providers can
 * supply region-specific festivals, custom holidays, or additional festival data.</p>
 * <p>允许自定义节日数据获取的实现。提供者可以提供特定地区的节日、自定义假日或额外的节日数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom festival data provision - 自定义节日数据提供</li>
 *   <li>Region-specific holiday support - 地区特定假日支持</li>
 *   <li>ServiceLoader-based discovery - 基于ServiceLoader的发现</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register via META-INF/services/cloud.opencode.base.lunar.spi.FestivalProvider
 * public class MyFestivalProvider implements FestivalProvider {
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
 * <p>Create file: META-INF/services/cloud.opencode.base.lunar.spi.FestivalProvider</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public interface FestivalProvider {

    /**
     * Get provider name
     * 获取提供者名称
     *
     * @return the provider name | 提供者名称
     */
    String getName();

    /**
     * Get region code
     * 获取地区代码
     *
     * @return the region code (e.g., "CN", "TW", "HK") | 地区代码
     */
    default String getRegion() {
        return "CN";
    }

    /**
     * Get lunar festivals for date
     * 获取农历日期的节日
     *
     * @param month the lunar month | 农历月
     * @param day   the lunar day | 农历日
     * @return list of festivals | 节日列表
     */
    List<Festival> getLunarFestivals(int month, int day);

    /**
     * Get solar festivals for date
     * 获取公历日期的节日
     *
     * @param month the solar month | 公历月
     * @param day   the solar day | 公历日
     * @return list of festivals | 节日列表
     */
    List<Festival> getSolarFestivals(int month, int day);

    /**
     * Get all festivals for a lunar date
     * 获取农历日期的所有节日
     *
     * @param lunar the lunar date | 农历日期
     * @return list of festivals | 节日列表
     */
    List<Festival> getFestivals(LunarDate lunar);

    /**
     * Get all festivals for a solar date
     * 获取公历日期的所有节日
     *
     * @param solar the solar date | 公历日期
     * @return list of festivals | 节日列表
     */
    List<Festival> getFestivals(SolarDate solar);

    /**
     * Get all festivals in a year
     * 获取一年的所有节日
     *
     * @param year the year | 年
     * @return list of festival dates | 节日日期列表
     */
    List<LocalDate> getAllFestivalDates(int year);

    /**
     * Check if date is a festival
     * 检查日期是否为节日
     *
     * @param date the date | 日期
     * @return true if festival | 如果是节日返回true
     */
    boolean isFestival(LocalDate date);

    /**
     * Get provider priority
     * 获取提供者优先级
     *
     * @return priority (lower is higher) | 优先级
     */
    default int getPriority() {
        return 100;
    }
}

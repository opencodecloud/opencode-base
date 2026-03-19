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
import cloud.opencode.base.lunar.divination.TimeSlot;
import cloud.opencode.base.lunar.divination.YiJi;

import java.time.LocalDate;
import java.util.List;

/**
 * Divination Provider SPI - Interface for divination/almanac data providers
 * 占卜提供者 SPI - 占卜/黄历数据提供者接口
 *
 * <p>Allows custom implementations for Chinese almanac (黄历) data including
 * auspicious/inauspicious activities, time slots, and fortune telling data.</p>
 * <p>允许自定义中国黄历数据的实现，包括宜忌活动、时辰和算命数据。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Custom almanac data provision - 自定义黄历数据提供</li>
 *   <li>YiJi (auspicious/inauspicious) data - 宜忌数据</li>
 *   <li>Time slot data - 时辰数据</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Register via META-INF/services/cloud.opencode.base.lunar.spi.DivinationProvider
 * public class MyDivinationProvider implements DivinationProvider {
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
 * <p>Create file: META-INF/services/cloud.opencode.base.lunar.spi.DivinationProvider</p>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public interface DivinationProvider {

    /**
     * Get provider name
     * 获取提供者名称
     *
     * @return the provider name | 提供者名称
     */
    String getName();

    /**
     * Get YiJi for lunar date
     * 获取农历日期的宜忌
     *
     * @param lunar the lunar date | 农历日期
     * @return the YiJi | 宜忌
     */
    YiJi getYiJi(LunarDate lunar);

    /**
     * Get YiJi for date
     * 获取日期的宜忌
     *
     * @param date the date | 日期
     * @return the YiJi | 宜忌
     */
    YiJi getYiJi(LocalDate date);

    /**
     * Get suitable activities for date
     * 获取日期适宜的活动
     *
     * @param date the date | 日期
     * @return list of suitable activities | 适宜活动列表
     */
    List<String> getSuitable(LocalDate date);

    /**
     * Get activities to avoid for date
     * 获取日期应避免的活动
     *
     * @param date the date | 日期
     * @return list of activities to avoid | 应避免活动列表
     */
    List<String> getAvoid(LocalDate date);

    /**
     * Check if date is auspicious for activity
     * 检查日期是否为活动吉日
     *
     * @param activity the activity | 活动
     * @param date     the date | 日期
     * @return true if auspicious | 如果是吉日返回true
     */
    boolean isAuspicious(String activity, LocalDate date);

    /**
     * Find next auspicious day for activity
     * 查找活动的下一个吉日
     *
     * @param activity the activity | 活动
     * @param from     the start date | 开始日期
     * @param maxDays  maximum days to search | 最大搜索天数
     * @return the next auspicious day, or null if not found | 下一个吉日
     */
    LocalDate findNextAuspicious(String activity, LocalDate from, int maxDays);

    /**
     * Get auspicious time slots for date
     * 获取日期的吉时
     *
     * @param date the date | 日期
     * @return list of auspicious time slots | 吉时列表
     */
    List<TimeSlot> getAuspiciousTimeSlots(LocalDate date);

    /**
     * Get daily fortune summary
     * 获取每日运势摘要
     *
     * @param date the date | 日期
     * @return the fortune summary | 运势摘要
     */
    default String getDailyFortune(LocalDate date) {
        YiJi yiji = getYiJi(date);
        return yiji.toString();
    }

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

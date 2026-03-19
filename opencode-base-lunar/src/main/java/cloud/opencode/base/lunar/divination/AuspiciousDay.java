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

package cloud.opencode.base.lunar.divination;

import cloud.opencode.base.lunar.LunarDate;
import cloud.opencode.base.lunar.SolarDate;
import cloud.opencode.base.lunar.calendar.DateConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Auspicious Day - Finder for auspicious dates
 * 吉日 - 吉日查找器
 *
 * <p>Finds auspicious dates for specific activities according to
 * traditional Chinese almanac rules.</p>
 * <p>根据中国传统黄历规则查找特定活动的吉日。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Next auspicious day search - 查找下一个吉日</li>
 *   <li>Monthly auspicious day listing - 月度吉日列表</li>
 *   <li>Activity-based filtering - 基于活动类型的筛选</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Find next auspicious day for marriage
 * LocalDate weddingDay = AuspiciousDay.findNext(YiJi.JIE_HUN, LocalDate.now());
 *
 * // Find all auspicious days in a month
 * List<LocalDate> days = AuspiciousDay.findInMonth(YiJi.KAI_SHI, 2024, 2);
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless utility class) - 线程安全: 是（无状态工具类）</li>
 *   <li>Null-safe: No (activity and date must not be null) - 空值安全: 否（活动和日期不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public final class AuspiciousDay {

    private AuspiciousDay() {
    }

    /**
     * Find next auspicious day for activity
     * 查找下一个活动吉日
     *
     * @param activity the activity | 活动
     * @param from     the start date | 开始日期
     * @return the next auspicious day, or null if not found within 60 days | 下一个吉日
     */
    public static LocalDate findNext(String activity, LocalDate from) {
        return findNext(activity, from, 60);
    }

    /**
     * Find next auspicious day for activity within limit
     * 在限制范围内查找下一个活动吉日
     *
     * @param activity the activity | 活动
     * @param from     the start date | 开始日期
     * @param maxDays  maximum days to search | 最大搜索天数
     * @return the next auspicious day, or null if not found | 下一个吉日
     */
    public static LocalDate findNext(String activity, LocalDate from, int maxDays) {
        for (int i = 0; i < maxDays; i++) {
            LocalDate date = from.plusDays(i);
            if (isAuspicious(activity, date)) {
                return date;
            }
        }
        return null;
    }

    /**
     * Find all auspicious days in a month
     * 查找一个月内所有吉日
     *
     * @param activity the activity | 活动
     * @param year     the year | 年
     * @param month    the month | 月
     * @return list of auspicious days | 吉日列表
     */
    public static List<LocalDate> findInMonth(String activity, int year, int month) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate date = LocalDate.of(year, month, 1);
        int daysInMonth = date.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate current = LocalDate.of(year, month, day);
            if (isAuspicious(activity, current)) {
                result.add(current);
            }
        }

        return result;
    }

    /**
     * Find all auspicious days in a range
     * 在范围内查找所有吉日
     *
     * @param activity the activity | 活动
     * @param from     the start date | 开始日期
     * @param to       the end date | 结束日期
     * @return list of auspicious days | 吉日列表
     */
    public static List<LocalDate> findInRange(String activity, LocalDate from, LocalDate to) {
        List<LocalDate> result = new ArrayList<>();
        LocalDate current = from;

        while (!current.isAfter(to)) {
            if (isAuspicious(activity, current)) {
                result.add(current);
            }
            current = current.plusDays(1);
        }

        return result;
    }

    /**
     * Check if date is auspicious for activity
     * 检查日期是否为活动吉日
     *
     * @param activity the activity | 活动
     * @param date     the date | 日期
     * @return true if auspicious | 如果是吉日返回true
     */
    public static boolean isAuspicious(String activity, LocalDate date) {
        LunarDate lunar = DateConverter.toLunar(date);
        YiJi yiji = YiJi.of(lunar);
        return yiji.isSuitable(activity) && !yiji.shouldAvoid(activity);
    }

    /**
     * Check if date should be avoided for activity
     * 检查日期是否应避免进行活动
     *
     * @param activity the activity | 活动
     * @param date     the date | 日期
     * @return true if should avoid | 如果应避免返回true
     */
    public static boolean shouldAvoid(String activity, LocalDate date) {
        LunarDate lunar = DateConverter.toLunar(date);
        YiJi yiji = YiJi.of(lunar);
        return yiji.shouldAvoid(activity);
    }

    /**
     * Get YiJi for a date
     * 获取日期的宜忌
     *
     * @param date the date | 日期
     * @return the YiJi | 宜忌
     */
    public static YiJi getYiJi(LocalDate date) {
        LunarDate lunar = DateConverter.toLunar(date);
        return YiJi.of(lunar);
    }

    /**
     * Get today's YiJi
     * 获取今天的宜忌
     *
     * @return today's YiJi | 今天的宜忌
     */
    public static YiJi today() {
        return getYiJi(LocalDate.now());
    }
}

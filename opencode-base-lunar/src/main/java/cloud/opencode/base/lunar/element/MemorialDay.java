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

package cloud.opencode.base.lunar.element;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Memorial Day - Represents a commemorative or anniversary date
 * 纪念日 - 表示纪念或周年日期
 *
 * <p>Used to track anniversaries, birthdays, and other memorable dates
 * with support for both solar and lunar calendars.</p>
 * <p>用于跟踪周年纪念、生日和其他值得纪念的日期，支持公历和农历。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Anniversary and birthday tracking - 周年纪念和生日跟踪</li>
 *   <li>Days-until calculation - 距离天数计算</li>
 *   <li>Today check - 今天检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Create a memorial day
 * MemorialDay birthday = MemorialDay.of("Birthday", LocalDate.of(1990, 5, 15));
 *
 * // Calculate days until
 * long daysUntil = birthday.daysUntilNextOccurrence();
 *
 * // Check if it's today
 * if (birthday.isToday()) {
 *     System.out.println("Happy " + birthday.name() + "!");
 * }
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (name and date must not be null) - 空值安全: 否（名称和日期不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public record MemorialDay(
        String name,
        LocalDate date,
        boolean isLunar,
        String description
) {

    /**
     * Creates a solar calendar memorial day
     * 创建公历纪念日
     *
     * @param name the name | 名称
     * @param date the date | 日期
     * @return the memorial day | 纪念日
     */
    public static MemorialDay of(String name, LocalDate date) {
        return new MemorialDay(name, date, false, "");
    }

    /**
     * Creates a memorial day with description
     * 创建带描述的纪念日
     *
     * @param name        the name | 名称
     * @param date        the date | 日期
     * @param description the description | 描述
     * @return the memorial day | 纪念日
     */
    public static MemorialDay of(String name, LocalDate date, String description) {
        return new MemorialDay(name, date, false, description);
    }

    /**
     * Creates a lunar calendar memorial day
     * 创建农历纪念日
     *
     * @param name the name | 名称
     * @param date the date (will be treated as lunar) | 日期（将被视为农历）
     * @return the memorial day | 纪念日
     */
    public static MemorialDay ofLunar(String name, LocalDate date) {
        return new MemorialDay(name, date, true, "");
    }

    /**
     * Calculates years since the memorial date
     * 计算距离纪念日的年数
     *
     * @return years since | 年数
     */
    public long yearsSince() {
        return ChronoUnit.YEARS.between(date, LocalDate.now());
    }

    /**
     * Calculates days until the next occurrence
     * 计算距离下次纪念日的天数
     *
     * @return days until next occurrence | 距离下次的天数
     */
    public long daysUntilNextOccurrence() {
        LocalDate today = LocalDate.now();
        LocalDate thisYear = adjustToYear(today.getYear());

        if (thisYear.isBefore(today) || thisYear.isEqual(today)) {
            thisYear = adjustToYear(today.getYear() + 1);
        }

        return ChronoUnit.DAYS.between(today, thisYear);
    }

    /**
     * Checks if today is the memorial day
     * 检查今天是否是纪念日
     *
     * @return true if today | 如果是今天返回true
     */
    public boolean isToday() {
        LocalDate today = LocalDate.now();
        return date.getMonth() == today.getMonth() &&
               date.getDayOfMonth() == today.getDayOfMonth();
    }

    /**
     * Gets the anniversary number for this year
     * 获取今年的周年数
     *
     * @return the anniversary number | 周年数
     */
    public int getAnniversaryNumber() {
        return (int) yearsSince();
    }

    /**
     * Gets the next occurrence date
     * 获取下次纪念日日期
     *
     * @return the next occurrence | 下次日期
     */
    public LocalDate getNextOccurrence() {
        LocalDate today = LocalDate.now();
        LocalDate thisYear = adjustToYear(today.getYear());

        if (thisYear.isBefore(today) || thisYear.isEqual(today)) {
            return adjustToYear(today.getYear() + 1);
        }
        return thisYear;
    }

    /**
     * Adjusts the memorial date to a specific year, handling Feb 29 dates in non-leap years
     * by falling back to Feb 28.
     * 将纪念日调整到指定年份，闰年2月29日在非闰年时回退到2月28日。
     */
    private LocalDate adjustToYear(int year) {
        if (date.getMonthValue() == 2 && date.getDayOfMonth() == 29
                && !java.time.Year.of(year).isLeap()) {
            return LocalDate.of(year, 2, 28);
        }
        return date.withYear(year);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemorialDay that)) return false;
        return Objects.equals(name, that.name) && Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, date);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", name, date);
    }
}

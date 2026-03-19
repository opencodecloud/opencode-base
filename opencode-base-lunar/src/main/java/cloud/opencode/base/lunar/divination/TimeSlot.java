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

import java.time.LocalTime;

/**
 * TimeSlot (Shichen) - Traditional Chinese time division
 * 时辰 - 中国传统时间划分
 *
 * <p>Represents the 12 two-hour periods in traditional Chinese timekeeping,
 * each corresponding to an Earthly Branch (地支).</p>
 * <p>表示中国传统计时中的12个双时辰，每个对应一个地支。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>12 traditional time period definitions - 12个传统时辰定义</li>
 *   <li>Time-to-slot conversion - 时间到时辰转换</li>
 *   <li>Earthly Branch association - 地支关联</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * TimeSlot slot = TimeSlot.fromTime(LocalTime.of(14, 30));  // WEI (未时)
 * String name = slot.getChineseName();  // 未
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable enum) - 线程安全: 是（不可变枚举）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * <p><strong>Time Divisions | 时辰划分:</strong></p>
 * <ul>
 *   <li>子时 (ZI): 23:00-01:00</li>
 *   <li>丑时 (CHOU): 01:00-03:00</li>
 *   <li>寅时 (YIN): 03:00-05:00</li>
 *   <li>...and so on</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public enum TimeSlot {

    /**
     * Zi (Rat Hour) 23:00-01:00 | 子时 23:00-01:00
     */
    ZI("子", 23, 1, "夜半", "鼠"),

    /**
     * Chou (Ox Hour) 01:00-03:00 | 丑时 01:00-03:00
     */
    CHOU("丑", 1, 3, "鸡鸣", "牛"),

    /**
     * Yin (Tiger Hour) 03:00-05:00 | 寅时 03:00-05:00
     */
    YIN("寅", 3, 5, "平旦", "虎"),

    /**
     * Mao (Rabbit Hour) 05:00-07:00 | 卯时 05:00-07:00
     */
    MAO("卯", 5, 7, "日出", "兔"),

    /**
     * Chen (Dragon Hour) 07:00-09:00 | 辰时 07:00-09:00
     */
    CHEN("辰", 7, 9, "食时", "龙"),

    /**
     * Si (Snake Hour) 09:00-11:00 | 巳时 09:00-11:00
     */
    SI("巳", 9, 11, "隅中", "蛇"),

    /**
     * Wu (Horse Hour) 11:00-13:00 | 午时 11:00-13:00
     */
    WU("午", 11, 13, "日中", "马"),

    /**
     * Wei (Goat Hour) 13:00-15:00 | 未时 13:00-15:00
     */
    WEI("未", 13, 15, "日昳", "羊"),

    /**
     * Shen (Monkey Hour) 15:00-17:00 | 申时 15:00-17:00
     */
    SHEN("申", 15, 17, "晡时", "猴"),

    /**
     * You (Rooster Hour) 17:00-19:00 | 酉时 17:00-19:00
     */
    YOU("酉", 17, 19, "日入", "鸡"),

    /**
     * Xu (Dog Hour) 19:00-21:00 | 戌时 19:00-21:00
     */
    XU("戌", 19, 21, "黄昏", "狗"),

    /**
     * Hai (Pig Hour) 21:00-23:00 | 亥时 21:00-23:00
     */
    HAI("亥", 21, 23, "人定", "猪");

    private final String chinese;
    private final int startHour;
    private final int endHour;
    private final String ancientName;
    private final String zodiac;

    TimeSlot(String chinese, int startHour, int endHour, String ancientName, String zodiac) {
        this.chinese = chinese;
        this.startHour = startHour;
        this.endHour = endHour;
        this.ancientName = ancientName;
        this.zodiac = zodiac;
    }

    /**
     * Get Chinese name
     * 获取中文名
     *
     * @return Chinese name | 中文名
     */
    public String getChinese() {
        return chinese;
    }

    /**
     * Get start hour
     * 获取开始小时
     *
     * @return start hour | 开始小时
     */
    public int getStartHour() {
        return startHour;
    }

    /**
     * Get end hour
     * 获取结束小时
     *
     * @return end hour | 结束小时
     */
    public int getEndHour() {
        return endHour;
    }

    /**
     * Get ancient name
     * 获取古称
     *
     * @return ancient name | 古称
     */
    public String getAncientName() {
        return ancientName;
    }

    /**
     * Get associated zodiac
     * 获取对应生肖
     *
     * @return zodiac | 生肖
     */
    public String getZodiac() {
        return zodiac;
    }

    /**
     * Get full name (Chinese + 时)
     * 获取完整名称
     *
     * @return full name | 完整名称
     */
    public String getFullName() {
        return chinese + "时";
    }

    /**
     * Get time range string
     * 获取时间范围字符串
     *
     * @return time range | 时间范围
     */
    public String getTimeRange() {
        return String.format("%02d:00-%02d:00", startHour, endHour);
    }

    /**
     * Get TimeSlot from hour
     * 从小时获取时辰
     *
     * @param hour the hour (0-23) | 小时
     * @return the TimeSlot | 时辰
     */
    public static TimeSlot fromHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be 0-23: " + hour);
        }

        // Handle special case for ZI (spans midnight)
        if (hour >= 23 || hour < 1) return ZI;
        if (hour < 3) return CHOU;
        if (hour < 5) return YIN;
        if (hour < 7) return MAO;
        if (hour < 9) return CHEN;
        if (hour < 11) return SI;
        if (hour < 13) return WU;
        if (hour < 15) return WEI;
        if (hour < 17) return SHEN;
        if (hour < 19) return YOU;
        if (hour < 21) return XU;
        return HAI;
    }

    /**
     * Get TimeSlot from LocalTime
     * 从LocalTime获取时辰
     *
     * @param time the time | 时间
     * @return the TimeSlot | 时辰
     */
    public static TimeSlot fromTime(LocalTime time) {
        return fromHour(time.getHour());
    }

    /**
     * Get current TimeSlot
     * 获取当前时辰
     *
     * @return current TimeSlot | 当前时辰
     */
    public static TimeSlot now() {
        return fromTime(LocalTime.now());
    }

    /**
     * Get index (0-11)
     * 获取索引
     *
     * @return the index | 索引
     */
    public int getIndex() {
        return ordinal();
    }

    @Override
    public String toString() {
        return getFullName();
    }
}

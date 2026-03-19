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
import cloud.opencode.base.lunar.ganzhi.GanZhi;

import java.util.List;

/**
 * YiJi - Auspicious and Inauspicious Activities
 * 宜忌 - 黄历宜忌事项
 *
 * <p>Provides information about activities that are considered auspicious (宜)
 * or inauspicious (忌) for a given date according to traditional Chinese almanac.</p>
 * <p>根据中国传统黄历，提供特定日期适宜（宜）或不宜（忌）的活动信息。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Auspicious activity listing - 宜事列表</li>
 *   <li>Inauspicious activity listing - 忌事列表</li>
 *   <li>Common activity constants - 常见活动常量</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * YiJi yiji = YiJi.of(lunarDate);
 * List<String> suitable = yiji.getSuitable();   // 宜
 * List<String> avoid = yiji.getAvoid();         // 忌
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable record) - 线程安全: 是（不可变记录）</li>
 *   <li>Null-safe: No (lists must not be null) - 空值安全: 否（列表不能为null）</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public record YiJi(
        List<String> suitable,
        List<String> avoid
) {

    /**
     * Common activities | 常见活动
     */
    public static final String JI_SI = "祭祀";        // Worship
    public static final String QI_FU = "祈福";        // Pray for blessing
    public static final String QIU_SI = "求嗣";       // Pray for offspring
    public static final String KAI_GUANG = "开光";    // Consecration
    public static final String SU_YUAN = "塑绘";      // Sculpture
    public static final String CHU_HUAN = "出行";     // Travel
    public static final String JIE_HUN = "结婚";      // Marriage
    public static final String QI_JI = "动土";        // Break ground
    public static final String AN_CHUANG = "安床";    // Install bed
    public static final String ZAO_WU = "造屋";       // Build house
    public static final String RU_ZHAI = "入宅";      // Move into house
    public static final String KAI_SHI = "开市";      // Open business
    public static final String AN_ZANG = "安葬";      // Burial
    public static final String PO_TU = "破土";        // Break ground for burial
    public static final String XIU_ZENG = "修造";     // Renovation
    public static final String ZAI_ZHONG = "栽种";    // Planting
    public static final String MU_YU = "沐浴";        // Bathing
    public static final String JIAN_FA = "剃头";      // Haircut
    public static final String CHU_HUO = "出火";      // Move fire/stove
    public static final String NA_CAI = "纳采";       // Betrothal

    /**
     * Create YiJi from lunar date
     * 从农历日期创建宜忌
     *
     * @param lunar the lunar date | 农历日期
     * @return the YiJi | 宜忌
     */
    public static YiJi of(LunarDate lunar) {
        return calculate(lunar);
    }

    /**
     * Calculate YiJi based on GanZhi
     * 根据干支计算宜忌
     *
     * @param lunar the lunar date | 农历日期
     * @return the YiJi | 宜忌
     */
    private static YiJi calculate(LunarDate lunar) {
        GanZhi dayGanZhi = GanZhi.ofDay(lunar.toSolar().toLocalDate());
        int ganIndex = dayGanZhi.gan().ordinal();
        int zhiIndex = dayGanZhi.zhi().ordinal();

        // Simplified calculation based on GanZhi
        List<String> suitable = calculateSuitable(ganIndex, zhiIndex, lunar);
        List<String> avoid = calculateAvoid(ganIndex, zhiIndex, lunar);

        return new YiJi(suitable, avoid);
    }

    private static List<String> calculateSuitable(int gan, int zhi, LunarDate lunar) {
        // Simplified logic - in production would use complex almanac rules
        int dayType = (gan + zhi) % 6;

        return switch (dayType) {
            case 0 -> List.of(JI_SI, QI_FU, CHU_HUAN, JIE_HUN, AN_CHUANG);
            case 1 -> List.of(JI_SI, QI_FU, KAI_SHI, QI_JI, ZAI_ZHONG);
            case 2 -> List.of(JI_SI, CHU_HUAN, RU_ZHAI, XIU_ZENG, MU_YU);
            case 3 -> List.of(QI_FU, QIU_SI, NA_CAI, JIE_HUN, AN_CHUANG);
            case 4 -> List.of(JI_SI, KAI_GUANG, CHU_HUAN, KAI_SHI, ZAO_WU);
            case 5 -> List.of(QI_FU, MU_YU, JIAN_FA, AN_ZANG, PO_TU);
            default -> List.of(JI_SI, QI_FU);
        };
    }

    private static List<String> calculateAvoid(int gan, int zhi, LunarDate lunar) {
        // Simplified logic - in production would use complex almanac rules
        int dayType = (gan + zhi) % 6;

        return switch (dayType) {
            case 0 -> List.of(AN_ZANG, PO_TU, QI_JI);
            case 1 -> List.of(JIE_HUN, AN_CHUANG, RU_ZHAI);
            case 2 -> List.of(KAI_SHI, QI_JI, AN_ZANG);
            case 3 -> List.of(CHU_HUAN, QI_JI, PO_TU);
            case 4 -> List.of(AN_ZANG, JIE_HUN, AN_CHUANG);
            case 5 -> List.of(CHU_HUAN, KAI_SHI, RU_ZHAI);
            default -> List.of(AN_ZANG, PO_TU);
        };
    }

    /**
     * Check if activity is suitable today
     * 检查活动今天是否适宜
     *
     * @param activity the activity | 活动
     * @return true if suitable | 如果适宜返回true
     */
    public boolean isSuitable(String activity) {
        return suitable.contains(activity);
    }

    /**
     * Check if activity should be avoided today
     * 检查活动今天是否应避免
     *
     * @param activity the activity | 活动
     * @return true if should avoid | 如果应避免返回true
     */
    public boolean shouldAvoid(String activity) {
        return avoid.contains(activity);
    }

    /**
     * Get suitable activities as string
     * 获取宜的活动字符串
     *
     * @return suitable string | 宜字符串
     */
    public String getSuitableString() {
        return String.join(" ", suitable);
    }

    /**
     * Get avoid activities as string
     * 获取忌的活动字符串
     *
     * @return avoid string | 忌字符串
     */
    public String getAvoidString() {
        return String.join(" ", avoid);
    }

    @Override
    public String toString() {
        return "宜: " + getSuitableString() + " | 忌: " + getAvoidString();
    }
}

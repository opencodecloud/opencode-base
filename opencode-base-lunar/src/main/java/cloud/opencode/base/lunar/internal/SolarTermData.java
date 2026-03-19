package cloud.opencode.base.lunar.internal;

import java.time.LocalDate;

/**
 * Solar Term Data
 * 节气数据
 *
 * <p>Internal data and algorithms for calculating solar terms.</p>
 * <p>计算节气的内部数据和算法。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Solar term date calculation - 节气日期计算</li>
 *   <li>Century-adjusted algorithms - 世纪校正算法</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Internal use only
 * LocalDate date = SolarTermData.getTermDate(2024, 2); // Start of Spring
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable static data) - 线程安全: 是（不可变静态数据）</li>
 *   <li>Null-safe: N/A - 空值安全: 不适用</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-lunar V1.0.0
 */
public final class SolarTermData {

    /**
     * Solar term base days (from year start)
     * 节气基准日期（从年初算起的天数）
     */
    private static final int[][] TERM_DAYS = {
        // 小寒, 大寒, 立春, 雨水, 惊蛰, 春分, 清明, 谷雨, 立夏, 小满, 芒种, 夏至
        // 小暑, 大暑, 立秋, 处暑, 白露, 秋分, 寒露, 霜降, 立冬, 小雪, 大雪, 冬至
        {6, 20, 4, 19, 6, 21, 5, 20, 6, 21, 6, 21, 7, 23, 8, 23, 8, 23, 8, 24, 8, 22, 7, 22}, // Typical days
    };

    /**
     * Century adjustment values
     * 世纪修正值
     */
    private static final double[] C_20TH = {
        6.11, 20.84, 4.6295, 19.4599, 6.3826, 21.4155, 5.59, 20.888, 6.318, 21.86, 6.5, 22.2,
        7.928, 23.65, 8.35, 23.95, 8.44, 23.822, 9.098, 24.218, 8.218, 23.08, 7.9, 22.6
    };

    private static final double[] C_21ST = {
        5.4055, 20.12, 3.87, 18.73, 5.63, 20.646, 4.81, 20.1, 5.52, 21.04, 5.678, 21.37,
        7.108, 22.83, 7.5, 23.13, 7.646, 23.042, 8.318, 23.438, 7.438, 22.36, 7.18, 21.94
    };

    private SolarTermData() {
        // Utility class
    }

    /**
     * Get date for solar term
     * 获取节气日期
     *
     * @param year the year | 年份
     * @param termIndex the term index (0-23) | 节气索引
     * @return the date | 日期
     */
    public static LocalDate getDate(int year, int termIndex) {
        // Calculate month (0-11 -> 1-12)
        int month = (termIndex / 2) + 1;
        if (termIndex < 2) {
            // 小寒、大寒在1月
            month = 1;
        }

        // Get century coefficient
        double[] coefficients = year < 2000 ? C_20TH : C_21ST;
        double c = coefficients[termIndex];

        // Calculate day using formula
        int y = year % 100;
        int day = (int) (y * 0.2422 + c) - (y - 1) / 4;

        // Special adjustments for some years
        day = adjustDay(year, termIndex, day);

        return LocalDate.of(year, month, day);
    }

    /**
     * Adjust day for special cases
     * 调整特殊情况
     */
    private static int adjustDay(int year, int termIndex, int day) {
        // 特殊年份修正
        switch (termIndex) {
            case 0: // 小寒
                if (year == 2019) day = 5;
                break;
            case 2: // 立春
                if (year == 2026) day = 4;
                break;
            case 6: // 清明
                if (year == 2019) day = 5;
                break;
            case 10: // 芒种
                if (year == 2008) day = 5;
                break;
            case 14: // 立秋
                if (year == 2002) day = 8;
                break;
            case 18: // 寒露
                if (year == 2088) day = 8;
                break;
            case 22: // 大雪
                if (year == 1918) day = 8;
                break;
        }
        return day;
    }

    /**
     * Get all solar term dates for year
     * 获取某年所有节气日期
     *
     * @param year the year | 年份
     * @return array of dates | 日期数组
     */
    public static LocalDate[] getYearDates(int year) {
        LocalDate[] dates = new LocalDate[24];
        for (int i = 0; i < 24; i++) {
            dates[i] = getDate(year, i);
        }
        return dates;
    }
}

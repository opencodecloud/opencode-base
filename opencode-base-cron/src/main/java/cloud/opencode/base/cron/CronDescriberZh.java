package cloud.opencode.base.cron;

import java.util.BitSet;
import java.util.StringJoiner;

/**
 * Cron Describer (Chinese) - Chinese Cron Description Generator
 * Cron描述器（中文版） - 中文Cron描述生成器
 *
 * <p>Generates Chinese descriptions from parsed {@link CronExpression} instances.
 * Handles time patterns, day-of-month/week specials, and month restrictions.</p>
 * <p>从解析后的 {@link CronExpression} 实例生成中文描述。
 * 处理时间模式、月中日/星期特殊字符和月份限制。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time pattern detection (every N minutes/hours, specific time) - 时间模式检测</li>
 *   <li>Special character description (L, W, #) - 特殊字符描述</li>
 *   <li>Weekday/weekend detection - 工作日/周末检测</li>
 *   <li>Month name formatting - 月份名称格式化</li>
 *   <li>All output in Chinese - 全部输出为中文</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");
 * String desc = CronDescriberZh.describe(expr);
 *     // "在09:00，周一到周五"
 *
 * CronExpression expr2 = CronExpression.parse("*&#47;5 * * * *");
 * String desc2 = CronDescriberZh.describe(expr2);
 *     // "每5分钟"
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (stateless static methods) - 线程安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see CronDescriber
 * @see CronExpression
 * @since JDK 25, opencode-base-cron V1.0.3
 */
final class CronDescriberZh {

    private static final String[] DOW_NAMES = {"周日", "周一", "周二", "周三",
            "周四", "周五", "周六"};
    private static final String[] MONTH_NAMES = {"", "一月", "二月", "三月", "四月",
            "五月", "六月", "七月", "八月", "九月", "十月", "十一月", "十二月"};

    private CronDescriberZh() {
    }

    /**
     * Generates a Chinese human-readable description
     * 生成中文人类可读的描述
     *
     * @param expr the parsed cron expression | 解析后的Cron表达式
     * @return the Chinese description | 中文描述
     */
    static String describe(CronExpression expr) {
        StringBuilder sb = new StringBuilder();

        // Time part
        sb.append(describeTime(expr));

        // Day part
        String dayDesc = describeDay(expr);
        if (!dayDesc.isEmpty()) {
            sb.append("，").append(dayDesc);
        }

        // Month part
        String monthDesc = describeMonth(expr.months());
        if (!monthDesc.isEmpty()) {
            sb.append("，").append(monthDesc);
        }

        return sb.toString();
    }

    private static String describeTime(CronExpression expr) {
        BitSet sec = expr.seconds();
        BitSet min = expr.minutes();
        BitSet hr = expr.hours();

        boolean allSec = (sec.cardinality() == 60);
        boolean allMin = (min.cardinality() == 60);
        boolean allHr = (hr.cardinality() == 24);

        // Every second
        if (expr.hasSeconds() && allSec && allMin && allHr) {
            return "每秒";
        }

        // Every N seconds
        if (expr.hasSeconds() && !allSec && allMin && allHr) {
            int step = detectStep(sec, 0, 59);
            if (step > 0) {
                return "每" + step + "秒";
            }
        }

        // Every minute
        if (allMin && allHr) {
            return "每分钟";
        }

        // Every N minutes
        if (!allMin && allHr) {
            int step = detectStep(min, 0, 59);
            if (step > 0) {
                return "每" + step + "分钟";
            }
            if (min.cardinality() == 1) {
                return "在每小时的第" + min.nextSetBit(0) + "分钟";
            }
        }

        // Every N hours
        if (allMin && !allHr) {
            int step = detectStep(hr, 0, 23);
            if (step > 0) {
                return "每" + step + "小时";
            }
        }

        // Specific time
        if (min.cardinality() == 1 && hr.cardinality() == 1) {
            int h = hr.nextSetBit(0);
            int m = min.nextSetBit(0);
            return "在" + String.format("%02d:%02d", h, m);
        }

        // Multiple hours, specific minute
        if (min.cardinality() == 1 && !allHr) {
            return "在" + describeSet(hr, "小时") + "的第" + min.nextSetBit(0) + "分钟";
        }

        return "在" + describeSet(hr, "小时") + "的" + describeSet(min, "分钟");
    }

    private static String describeDay(CronExpression expr) {
        StringBuilder sb = new StringBuilder();

        // Day of month specials
        if (expr.lastDayOfMonth()) {
            if (expr.lastDayOffset() > 0) {
                sb.append("每月最后一天前").append(expr.lastDayOffset()).append("天");
            } else {
                sb.append("每月最后一天");
            }
        } else if (expr.lastWeekday()) {
            sb.append("每月最后一个工作日");
        } else if (expr.nearestWeekday() > 0) {
            sb.append("每月最接近").append(expr.nearestWeekday()).append("号的工作日");
        } else if (expr.domRestricted()) {
            BitSet dom = expr.daysOfMonth();
            if (dom.cardinality() == 1) {
                sb.append("在每月的第").append(dom.nextSetBit(1)).append("天");
            } else {
                int first = dom.nextSetBit(1);
                int last = -1;
                boolean contiguous = true;
                int count = 0;
                for (int i = dom.nextSetBit(1); i >= 0; i = dom.nextSetBit(i + 1)) {
                    if (last >= 0 && i != last + 1) {
                        contiguous = false;
                    }
                    last = i;
                    count++;
                }
                if (contiguous && count > 1) {
                    sb.append("在每月的第").append(first).append("-").append(last).append("天");
                } else {
                    sb.append("在每月的第").append(formatBitSet(dom, 1, 31)).append("天");
                }
            }
        }

        // Day of week specials
        String dowDesc = "";
        if (expr.lastDayOfWeek()) {
            dowDesc = "每月最后一个" + DOW_NAMES[expr.lastDayOfWeekValue()];
        } else if (expr.nthDayOfWeek() >= 0) {
            dowDesc = "每月第" + expr.nthDayOfWeekOrdinal() + "个"
                    + DOW_NAMES[expr.nthDayOfWeek()];
        } else if (expr.dowRestricted()) {
            BitSet dow = expr.daysOfWeek();
            dowDesc = describeDow(dow);
        }

        if (!sb.isEmpty() && !dowDesc.isEmpty()) {
            sb.append("或").append(dowDesc);
        } else if (!dowDesc.isEmpty()) {
            sb.append(dowDesc);
        }

        return sb.toString();
    }

    private static String describeDow(BitSet dow) {
        if (dow.cardinality() == 7) return "";
        // MON-FRI
        if (dow.cardinality() == 5 && dow.get(1) && dow.get(2) && dow.get(3) && dow.get(4) && dow.get(5)) {
            return "周一到周五";
        }
        // SAT-SUN
        if (dow.cardinality() == 2 && dow.get(0) && dow.get(6)) {
            return "周末";
        }
        if (dow.cardinality() == 1) {
            return DOW_NAMES[dow.nextSetBit(0)];
        }
        StringJoiner sj = new StringJoiner("、");
        for (int i = dow.nextSetBit(0); i >= 0 && i <= 6; i = dow.nextSetBit(i + 1)) {
            sj.add(DOW_NAMES[i]);
        }
        return sj.toString();
    }

    private static String describeMonth(BitSet months) {
        if (months.cardinality() == 12) return "";
        if (months.cardinality() == 1) {
            return "在" + MONTH_NAMES[months.nextSetBit(1)];
        }
        StringJoiner sj = new StringJoiner("、");
        for (int i = months.nextSetBit(1); i >= 0 && i <= 12; i = months.nextSetBit(i + 1)) {
            sj.add(MONTH_NAMES[i]);
        }
        return "在" + sj;
    }

    private static int detectStep(BitSet bits, int min, int max) {
        if (bits.cardinality() < 2) return 0;
        int first = bits.nextSetBit(min);
        if (first < 0) return 0;
        int second = bits.nextSetBit(first + 1);
        if (second < 0) return 0;
        int step = second - first;
        if (step <= 0) return 0;
        // Verify consistent stepping from first to max
        int expected = 0;
        for (int i = first; i <= max; i += step) {
            if (!bits.get(i)) return 0;
            expected++;
        }
        // Verify no extra bits outside the step pattern
        if (expected != bits.cardinality()) return 0;
        // Only report as "every N" if starting from 0/min
        if (first != min && first != 0) return 0;
        return step;
    }

    private static String describeSet(BitSet bits, String unit) {
        if (bits.cardinality() == 1) {
            return unit + bits.nextSetBit(0);
        }
        return unit + formatBitSet(bits, 0, 59);
    }

    private static String formatBitSet(BitSet bits, int min, int max) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = bits.nextSetBit(min); i >= 0 && i <= max; i = bits.nextSetBit(i + 1)) {
            sj.add(String.valueOf(i));
        }
        return sj.toString();
    }
}

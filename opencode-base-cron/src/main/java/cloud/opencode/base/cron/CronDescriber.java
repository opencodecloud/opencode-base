package cloud.opencode.base.cron;

import java.util.BitSet;
import java.util.StringJoiner;

/**
 * Cron Describer - Human-Readable Cron Description Generator
 * Cron描述器 - 人类可读的Cron描述生成器
 *
 * <p>Generates English descriptions from parsed {@link CronExpression} instances.
 * Handles time patterns, day-of-month/week specials, and month restrictions.</p>
 * <p>从解析后的 {@link CronExpression} 实例生成英文描述。
 * 处理时间模式、月中日/星期特殊字符和月份限制。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Time pattern detection (every N minutes/hours, specific time) - 时间模式检测</li>
 *   <li>Special character description (L, W, #) - 特殊字符描述</li>
 *   <li>Weekday/weekend detection - 工作日/周末检测</li>
 *   <li>Month name formatting - 月份名称格式化</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * CronExpression.parse("0 9 * * MON-FRI").describe()
 *     // "At 09:00, Monday through Friday"
 *
 * CronExpression.parse("*&#47;5 * * * *").describe()
 *     // "Every 5 minutes"
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
 * @since JDK 25, opencode-base-cron V1.0.0
 */
final class CronDescriber {

    private static final String[] DOW_NAMES = {"Sunday", "Monday", "Tuesday", "Wednesday",
            "Thursday", "Friday", "Saturday"};
    private static final String[] MONTH_NAMES = {"", "January", "February", "March", "April",
            "May", "June", "July", "August", "September", "October", "November", "December"};

    private CronDescriber() {
    }

    /**
     * Generates a human-readable description
     * 生成人类可读的描述
     *
     * @param expr the parsed cron expression | 解析后的Cron表达式
     * @return the description | 描述
     */
    static String describe(CronExpression expr) {
        StringBuilder sb = new StringBuilder();

        // Time part
        sb.append(describeTime(expr));

        // Day part
        String dayDesc = describeDay(expr);
        if (!dayDesc.isEmpty()) {
            sb.append(", ").append(dayDesc);
        }

        // Month part
        String monthDesc = describeMonth(expr.months());
        if (!monthDesc.isEmpty()) {
            sb.append(", ").append(monthDesc);
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
            return "Every second";
        }

        // Every N seconds
        if (expr.hasSeconds() && !allSec && allMin && allHr) {
            int step = detectStep(sec, 0, 59);
            if (step > 0) {
                return "Every " + step + " seconds";
            }
        }

        // Every minute
        if (allMin && allHr) {
            return "Every minute";
        }

        // Every N minutes
        if (!allMin && allHr) {
            int step = detectStep(min, 0, 59);
            if (step > 0) {
                return "Every " + step + " minutes";
            }
            if (min.cardinality() == 1) {
                return "At minute " + min.nextSetBit(0) + " of every hour";
            }
        }

        // Every N hours
        if (allMin && !allHr) {
            int step = detectStep(hr, 0, 23);
            if (step > 0) {
                return "Every " + step + " hours";
            }
        }

        // Specific time
        if (min.cardinality() == 1 && hr.cardinality() == 1) {
            int h = hr.nextSetBit(0);
            int m = min.nextSetBit(0);
            return "At " + String.format("%02d:%02d", h, m);
        }

        // Multiple hours, specific minute
        if (min.cardinality() == 1 && !allHr) {
            return "At minute " + min.nextSetBit(0) + " of " + describeSet(hr, "hour");
        }

        return "At " + describeSet(min, "minute") + " past " + describeSet(hr, "hour");
    }

    private static String describeDay(CronExpression expr) {
        StringBuilder sb = new StringBuilder();

        // Day of month specials
        if (expr.lastDayOfMonth()) {
            if (expr.lastDayOffset() > 0) {
                sb.append(expr.lastDayOffset()).append(" day(s) before the last day of the month");
            } else {
                sb.append("on the last day of the month");
            }
        } else if (expr.lastWeekday()) {
            sb.append("on the last weekday of the month");
        } else if (expr.nearestWeekday() > 0) {
            sb.append("on the nearest weekday to the ").append(ordinal(expr.nearestWeekday())).append(" of the month");
        } else if (expr.domRestricted()) {
            BitSet dom = expr.daysOfMonth();
            if (dom.cardinality() == 1) {
                sb.append("on day ").append(dom.nextSetBit(1)).append(" of the month");
            } else {
                sb.append("on days ").append(formatBitSet(dom, 1, 31));
            }
        }

        // Day of week specials
        String dowDesc = "";
        if (expr.lastDayOfWeek()) {
            dowDesc = "on the last " + DOW_NAMES[expr.lastDayOfWeekValue()] + " of the month";
        } else if (expr.nthDayOfWeek() >= 0) {
            dowDesc = "on the " + ordinal(expr.nthDayOfWeekOrdinal()) + " "
                    + DOW_NAMES[expr.nthDayOfWeek()] + " of the month";
        } else if (expr.dowRestricted()) {
            BitSet dow = expr.daysOfWeek();
            dowDesc = describeDow(dow);
        }

        if (!sb.isEmpty() && !dowDesc.isEmpty()) {
            // Both restricted → OR
            sb.append(" or ").append(dowDesc);
        } else if (!dowDesc.isEmpty()) {
            sb.append(dowDesc);
        }

        return sb.toString();
    }

    private static String describeDow(BitSet dow) {
        if (dow.cardinality() == 7) return "";
        // MON-FRI
        if (dow.cardinality() == 5 && dow.get(1) && dow.get(2) && dow.get(3) && dow.get(4) && dow.get(5)) {
            return "Monday through Friday";
        }
        // SAT-SUN
        if (dow.cardinality() == 2 && dow.get(0) && dow.get(6)) {
            return "on weekends";
        }
        if (dow.cardinality() == 1) {
            return "on " + DOW_NAMES[dow.nextSetBit(0)] + "s";
        }
        StringJoiner sj = new StringJoiner(", ");
        for (int i = dow.nextSetBit(0); i >= 0 && i <= 6; i = dow.nextSetBit(i + 1)) {
            sj.add(DOW_NAMES[i]);
        }
        return "on " + sj;
    }

    private static String describeMonth(BitSet months) {
        if (months.cardinality() == 12) return "";
        if (months.cardinality() == 1) {
            return "in " + MONTH_NAMES[months.nextSetBit(1)];
        }
        StringJoiner sj = new StringJoiner(", ");
        for (int i = months.nextSetBit(1); i >= 0 && i <= 12; i = months.nextSetBit(i + 1)) {
            sj.add(MONTH_NAMES[i]);
        }
        return "in " + sj;
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
        // Only report as "every N" if starting from 0/min (intuitive for users)
        // For non-zero starts like 5/10, describe as list instead
        if (first != min && first != 0) return 0;
        return step;
    }

    private static String describeSet(BitSet bits, String unit) {
        if (bits.cardinality() == 1) {
            return unit + " " + bits.nextSetBit(0);
        }
        return unit + "s " + formatBitSet(bits, 0, 59);
    }

    private static String formatBitSet(BitSet bits, int min, int max) {
        StringJoiner sj = new StringJoiner(",");
        for (int i = bits.nextSetBit(min); i >= 0 && i <= max; i = bits.nextSetBit(i + 1)) {
            sj.add(String.valueOf(i));
        }
        return sj.toString();
    }

    private static String ordinal(int n) {
        return switch (n) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> n + "th";
        };
    }
}

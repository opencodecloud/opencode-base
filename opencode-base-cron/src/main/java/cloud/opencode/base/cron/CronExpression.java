package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Cron Expression - Full-Featured Cron Expression Parser and Evaluator
 * Cron表达式 - 功能完整的Cron表达式解析器和评估器
 *
 * <p>Parses and evaluates cron expressions with full standard Unix cron semantics,
 * including OR logic for day-of-month/day-of-week, special characters (L, W, #),
 * name aliases (MON-FRI, JAN-DEC), and macro support (@daily, @yearly, etc.).</p>
 * <p>解析和评估具有完整标准Unix Cron语义的Cron表达式，
 * 包括月中日/星期几的OR逻辑、特殊字符（L、W、#）、
 * 名称别名（MON-FRI、JAN-DEC）和宏支持（@daily、@yearly等）。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>5-field and 6-field (with seconds) cron formats - 5字段和6字段（含秒）cron格式</li>
 *   <li>Standard OR semantics for day-of-month/day-of-week - 标准OR语义</li>
 *   <li>Special characters: L, L-N, LW, nW, n#m, nL - 特殊字符支持</li>
 *   <li>Name aliases: MON-FRI, JAN-DEC (case-insensitive) - 名称别名（不区分大小写）</li>
 *   <li>Macros: @yearly, @monthly, @weekly, @daily, @hourly - 预定义宏</li>
 *   <li>Range wrap-around: 22-2 for hours means 22,23,0,1,2 - 范围回绕</li>
 *   <li>Forward scheduling: {@link #nextExecution}, {@link #nextExecutions} - 正向调度</li>
 *   <li>Reverse scheduling: {@link #previousExecution} - 反向调度</li>
 *   <li>Human-readable description: {@link #describe()} - 人类可读描述</li>
 *   <li>Serializable with equals/hashCode - 可序列化，支持equals/hashCode</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * // Every day at 10:30 | 每天10:30
 * CronExpression.parse("30 10 * * *")
 *
 * // Every weekday at 9:00 | 工作日9:00
 * CronExpression.parse("0 9 * * MON-FRI")
 *
 * // 15th of month OR every Monday at noon | 15号或周一中午
 * CronExpression.parse("0 12 15 * MON")
 *
 * // Last day of every month at 18:00 | 每月最后一天18:00
 * CronExpression.parse("0 18 L * *")
 *
 * // 3rd Friday of every month at 10:00 | 每月第三个周五10:00
 * CronExpression.parse("0 10 * * FRI#3")
 *
 * // Next 5 executions | 下5次执行时间
 * expr.nextExecutions(ZonedDateTime.now(), 5)
 *
 * // Human-readable | 人类可读描述
 * expr.describe()  // "At 09:00, Monday through Friday"
 * }</pre>
 *
 * <p><strong>Performance | 性能特性:</strong></p>
 * <ul>
 *   <li>Parse: O(fields) - one-time cost - 解析: O(字段数) - 一次性开销</li>
 *   <li>nextExecution: O(1) amortized via field-jumping - 下次执行: 均摊O(1)，字段跳跃优化</li>
 * </ul>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes (immutable after construction) - 线程安全: 是（构造后不可变）</li>
 *   <li>Null-safe: Yes (rejects null inputs) - 空值安全: 是</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @see OpenCron
 * @see CronBuilder
 * @since JDK 25, opencode-base-cron V1.0.0
 */
public final class CronExpression implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private final String expression;
    private final BitSet seconds;
    private final BitSet minutes;
    private final BitSet hours;
    private final BitSet daysOfMonth;
    private final BitSet months;
    private final BitSet daysOfWeek;
    private final boolean hasSeconds;

    // Whether day-of-month / day-of-week was explicitly restricted (not * or ?)
    private final boolean domRestricted;
    private final boolean dowRestricted;

    // Special day-of-month fields: L, W
    private final boolean lastDayOfMonth;      // L
    private final int lastDayOffset;            // L-N → offset N
    private final boolean lastWeekday;          // LW
    private final int nearestWeekday;           // nW → day n, -1 if not set

    // Special day-of-week field: #
    private final int nthDayOfWeek;             // dow in 5#3 → 5, -1 if not set
    private final int nthDayOfWeekOrdinal;      // ordinal in 5#3 → 3
    private final boolean lastDayOfWeek;        // e.g., 5L → last Friday
    private final int lastDayOfWeekValue;       // the dow value for xL

    private CronExpression(String expression, BitSet seconds, BitSet minutes, BitSet hours,
                           BitSet daysOfMonth, BitSet months, BitSet daysOfWeek,
                           boolean hasSeconds, boolean domRestricted, boolean dowRestricted,
                           boolean lastDayOfMonth, int lastDayOffset,
                           boolean lastWeekday, int nearestWeekday,
                           int nthDayOfWeek, int nthDayOfWeekOrdinal,
                           boolean lastDayOfWeek, int lastDayOfWeekValue) {
        this.expression = expression;
        this.seconds = seconds;
        this.minutes = minutes;
        this.hours = hours;
        this.daysOfMonth = daysOfMonth;
        this.months = months;
        this.daysOfWeek = daysOfWeek;
        this.hasSeconds = hasSeconds;
        this.domRestricted = domRestricted;
        this.dowRestricted = dowRestricted;
        this.lastDayOfMonth = lastDayOfMonth;
        this.lastDayOffset = lastDayOffset;
        this.lastWeekday = lastWeekday;
        this.nearestWeekday = nearestWeekday;
        this.nthDayOfWeek = nthDayOfWeek;
        this.nthDayOfWeekOrdinal = nthDayOfWeekOrdinal;
        this.lastDayOfWeek = lastDayOfWeek;
        this.lastDayOfWeekValue = lastDayOfWeekValue;
    }

    // ==================== Parsing | 解析 ====================

    /**
     * Parse a cron expression
     * 解析Cron表达式
     *
     * <p>Supports 5-field, 6-field formats and macros (@yearly, @daily, etc.).</p>
     * <p>支持5字段、6字段格式和宏（@yearly、@daily 等）。</p>
     *
     * @param expression the cron expression | Cron表达式
     * @return the parsed expression | 解析后的表达式
     * @throws OpenCronException if the expression is invalid | 如果表达式无效
     */
    public static CronExpression parse(String expression) {
        if (expression == null || expression.isBlank()) {
            throw OpenCronException.parseError(expression, "expression must not be null or blank");
        }

        String trimmed = expression.trim();

        // Macro expansion
        String expanded = CronMacro.resolve(trimmed);
        if (expanded != null) {
            trimmed = expanded;
        }

        String[] parts = WHITESPACE.split(trimmed);
        if (parts.length != 5 && parts.length != 6) {
            throw OpenCronException.parseError(expression,
                    "expected 5 or 6 fields, got " + parts.length);
        }

        try {
            boolean hasSec = (parts.length == 6);
            int offset = hasSec ? 1 : 0;

            BitSet sec;
            if (hasSec) {
                sec = parseField(parts[0], CronField.SECOND);
            } else {
                sec = new BitSet(60);
                sec.set(0);
            }

            BitSet min = parseField(parts[offset], CronField.MINUTE);
            BitSet hr = parseField(parts[offset + 1], CronField.HOUR);
            BitSet mon = parseField(parts[offset + 3], CronField.MONTH);

            // Parse day-of-month (may contain L, W)
            String domField = parts[offset + 2];
            boolean domIsRestricted = !isWildcard(domField);
            DomParseResult domResult = parseDomField(domField);

            // Parse day-of-week (may contain #, L)
            String dowField = CronField.DAY_OF_WEEK.resolveAliases(parts[offset + 4]);
            boolean dowIsRestricted = !isWildcard(dowField);
            DowParseResult dowResult = parseDowField(dowField);

            return new CronExpression(expression, sec, min, hr,
                    domResult.bits, mon, dowResult.bits,
                    hasSec, domIsRestricted, dowIsRestricted,
                    domResult.lastDay, domResult.lastDayOffset,
                    domResult.lastWeekday, domResult.nearestWeekday,
                    dowResult.nthDow, dowResult.nthOrdinal,
                    dowResult.lastDow, dowResult.lastDowValue);
        } catch (OpenCronException e) {
            throw e;
        } catch (Exception e) {
            throw OpenCronException.parseError(expression, e.getMessage(), e);
        }
    }

    private static boolean isWildcard(String field) {
        return "*".equals(field) || "?".equals(field);
    }

    // ==================== Day-of-Month Parsing (L, W support) ====================

    private record DomParseResult(BitSet bits, boolean lastDay, int lastDayOffset,
                                  boolean lastWeekday, int nearestWeekday) {
    }

    private static DomParseResult parseDomField(String field) {
        CronField cf = CronField.DAY_OF_MONTH;

        // ? → wildcard
        if ("?".equals(field)) {
            BitSet bits = new BitSet(32);
            bits.set(cf.min(), cf.max() + 1);
            return new DomParseResult(bits, false, 0, false, -1);
        }

        // LW — last weekday of month
        if ("LW".equalsIgnoreCase(field)) {
            BitSet bits = new BitSet(32);
            bits.set(cf.min(), cf.max() + 1);
            return new DomParseResult(bits, false, 0, true, -1);
        }

        // L or L-N — last day of month
        if (field.toUpperCase().startsWith("L")) {
            BitSet bits = new BitSet(32);
            bits.set(cf.min(), cf.max() + 1);
            int offset = 0;
            if (field.length() > 1) {
                if (field.charAt(1) == '-') {
                    offset = Integer.parseInt(field.substring(2));
                    if (offset < 0 || offset > 30) {
                        throw OpenCronException.fieldError(cf.displayName(), field, "L offset must be 0-30");
                    }
                } else {
                    throw OpenCronException.fieldError(cf.displayName(), field, "invalid L expression");
                }
            }
            return new DomParseResult(bits, true, offset, false, -1);
        }

        // nW — nearest weekday to day n
        if (field.toUpperCase().endsWith("W")) {
            String dayStr = field.substring(0, field.length() - 1);
            int day = Integer.parseInt(dayStr);
            if (!cf.isInRange(day)) {
                throw OpenCronException.fieldError(cf.displayName(), day, cf.min(), cf.max());
            }
            BitSet bits = new BitSet(32);
            bits.set(cf.min(), cf.max() + 1);
            return new DomParseResult(bits, false, 0, false, day);
        }

        // Normal field
        BitSet bits = parseField(field, cf);
        return new DomParseResult(bits, false, 0, false, -1);
    }

    // ==================== Day-of-Week Parsing (# support) ====================

    private record DowParseResult(BitSet bits, int nthDow, int nthOrdinal,
                                  boolean lastDow, int lastDowValue) {
    }

    private static DowParseResult parseDowField(String field) {
        CronField cf = CronField.DAY_OF_WEEK;

        // ? → wildcard
        if ("?".equals(field)) {
            BitSet bits = new BitSet(7);
            bits.set(cf.min(), cf.max() + 1);
            return new DowParseResult(bits, -1, 0, false, -1);
        }

        // nL — last occurrence of day n in month (e.g., 5L = last Friday)
        if (field.toUpperCase().endsWith("L")) {
            String dayStr = field.substring(0, field.length() - 1);
            int dow = Integer.parseInt(dayStr);
            if (!cf.isInRange(dow)) {
                throw OpenCronException.fieldError(cf.displayName(), dow, cf.min(), cf.max());
            }
            BitSet bits = new BitSet(7);
            bits.set(cf.min(), cf.max() + 1);
            return new DowParseResult(bits, -1, 0, true, dow);
        }

        // n#m — nth occurrence of day n (e.g., 5#3 = 3rd Friday)
        if (field.contains("#")) {
            String[] parts = field.split("#");
            if (parts.length != 2) {
                throw OpenCronException.fieldError(cf.displayName(), field, "invalid # expression");
            }
            int dow = Integer.parseInt(parts[0]);
            int ordinal = Integer.parseInt(parts[1]);
            if (!cf.isInRange(dow)) {
                throw OpenCronException.fieldError(cf.displayName(), dow, cf.min(), cf.max());
            }
            if (ordinal < 1 || ordinal > 5) {
                throw OpenCronException.fieldError(cf.displayName(), field, "ordinal must be 1-5");
            }
            BitSet bits = new BitSet(7);
            bits.set(cf.min(), cf.max() + 1);
            return new DowParseResult(bits, dow, ordinal, false, -1);
        }

        // Normal field
        BitSet bits = parseField(field, cf);
        return new DowParseResult(bits, -1, 0, false, -1);
    }

    // ==================== Generic Field Parsing ====================

    private static BitSet parseField(String field, CronField cronField) {
        int min = cronField.min();
        int max = cronField.max();
        BitSet bits = new BitSet(max + 1);

        // Resolve aliases (MON→1, JAN→1, etc.)
        String resolved = cronField.resolveAliases(field);

        if ("*".equals(resolved) || "?".equals(resolved)) {
            bits.set(min, max + 1);
            return bits;
        }

        String[] parts = resolved.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.contains("/")) {
                String[] stepParts = part.split("/");
                if (stepParts.length != 2) {
                    throw OpenCronException.fieldError(cronField.displayName(), field, "invalid step expression");
                }
                int step = Integer.parseInt(stepParts[1]);
                if (step <= 0) {
                    throw OpenCronException.fieldError(cronField.displayName(), field, "step must be positive");
                }
                int start;
                if ("*".equals(stepParts[0])) {
                    start = min;
                } else if (stepParts[0].contains("-")) {
                    // range/step like 1-30/5
                    String[] rangeParts = stepParts[0].split("-");
                    if (rangeParts.length != 2) {
                        throw OpenCronException.fieldError(cronField.displayName(), field, "invalid range in step");
                    }
                    int from = Integer.parseInt(rangeParts[0]);
                    int to = Integer.parseInt(rangeParts[1]);
                    validateRange(from, min, max, cronField, field);
                    validateRange(to, min, max, cronField, field);
                    for (int i = from; i <= to; i += step) {
                        bits.set(i);
                    }
                    continue;
                } else {
                    start = Integer.parseInt(stepParts[0]);
                }
                validateRange(start, min, max, cronField, field);
                for (int i = start; i <= max; i += step) {
                    bits.set(i);
                }
            } else if (part.contains("-")) {
                String[] rangeParts = part.split("-");
                if (rangeParts.length != 2) {
                    throw OpenCronException.fieldError(cronField.displayName(), field, "invalid range expression");
                }
                int from = Integer.parseInt(rangeParts[0]);
                int to = Integer.parseInt(rangeParts[1]);
                validateRange(from, min, max, cronField, field);
                validateRange(to, min, max, cronField, field);
                if (from > to) {
                    // Wrap-around range: e.g., 22-2 for hours → 22,23,0,1,2
                    bits.set(from, max + 1);
                    bits.set(min, to + 1);
                } else {
                    bits.set(from, to + 1);
                }
            } else {
                int value = Integer.parseInt(part);
                validateRange(value, min, max, cronField, field);
                bits.set(value);
            }
        }

        return bits;
    }

    private static void validateRange(int value, int min, int max, CronField field, String expr) {
        if (value < min || value > max) {
            throw OpenCronException.fieldError(field.displayName(), value, min, max);
        }
    }

    // ==================== Matching | 匹配 ====================

    /**
     * Check if a time matches this cron expression
     * 检查时间是否匹配此Cron表达式
     *
     * @param time the time to check | 要检查的时间
     * @return true if the time matches | 如果匹配返回true
     */
    public boolean matches(ZonedDateTime time) {
        java.util.Objects.requireNonNull(time, "time must not be null");
        if (!seconds.get(time.getSecond())) return false;
        if (!minutes.get(time.getMinute())) return false;
        if (!hours.get(time.getHour())) return false;
        if (!months.get(time.getMonthValue())) return false;

        return matchesDay(time);
    }

    private boolean matchesDay(ZonedDateTime time) {
        boolean domMatch = matchesDayOfMonth(time);
        boolean dowMatch = matchesDayOfWeek(time);

        // OR semantics: when both dom and dow are restricted, match either
        if (domRestricted && dowRestricted) {
            return domMatch || dowMatch;
        }
        // Otherwise AND (one side is unrestricted = always true)
        return domMatch && dowMatch;
    }

    private boolean matchesDayOfMonth(ZonedDateTime time) {
        int dom = time.getDayOfMonth();
        int daysInMonth = time.toLocalDate().lengthOfMonth();

        // L — last day (with optional offset)
        if (lastDayOfMonth) {
            return dom == daysInMonth - lastDayOffset;
        }

        // LW — last weekday of month
        if (lastWeekday) {
            LocalDate last = time.toLocalDate().with(TemporalAdjusters.lastDayOfMonth());
            DayOfWeek dow = last.getDayOfWeek();
            if (dow == DayOfWeek.SATURDAY) {
                last = last.minusDays(1);
            } else if (dow == DayOfWeek.SUNDAY) {
                last = last.minusDays(2);
            }
            return dom == last.getDayOfMonth();
        }

        // nW — nearest weekday to day n
        // Finds the closest Monday-Friday to the target day, staying within the same month.
        // 找到距目标日最近的工作日（周一到周五），保持在同一个月内。
        if (nearestWeekday > 0) {
            int target = Math.min(nearestWeekday, daysInMonth);
            LocalDate targetDate = time.toLocalDate().withDayOfMonth(target);
            DayOfWeek dow = targetDate.getDayOfWeek();
            LocalDate nearest;
            if (dow == DayOfWeek.SATURDAY) {
                // Saturday: prefer Friday (day-1), but if day=1 use Monday (day+2)
                if (target > 1) {
                    nearest = targetDate.minusDays(1);
                } else {
                    nearest = targetDate.plusDays(2);
                }
            } else if (dow == DayOfWeek.SUNDAY) {
                // Sunday: prefer Monday (day+1), but if last day of month use Friday (day-2)
                if (target < daysInMonth) {
                    nearest = targetDate.plusDays(1);
                } else {
                    nearest = targetDate.minusDays(2);
                }
            } else {
                nearest = targetDate;
            }
            // Final safety: clamp to same month (should not happen with above logic, but defensive)
            if (nearest.getMonthValue() != time.getMonthValue()) {
                nearest = targetDate;
            }
            return dom == nearest.getDayOfMonth();
        }

        return daysOfMonth.get(dom);
    }

    private boolean matchesDayOfWeek(ZonedDateTime time) {
        int dow = time.getDayOfWeek().getValue() % 7; // MON=1..SUN=7 → 1..6,0
        int dom = time.getDayOfMonth();

        // nL — last occurrence of day n in month
        if (lastDayOfWeek) {
            if (dow != lastDayOfWeekValue) return false;
            // Check it's the last occurrence: no more of this dow in the month
            int daysInMonth = time.toLocalDate().lengthOfMonth();
            return dom + 7 > daysInMonth;
        }

        // n#m — nth occurrence
        if (nthDayOfWeek >= 0) {
            if (dow != nthDayOfWeek) return false;
            int ordinal = (dom - 1) / 7 + 1;
            return ordinal == nthDayOfWeekOrdinal;
        }

        return daysOfWeek.get(dow);
    }

    // ==================== Next Execution | 下次执行 ====================

    /**
     * Gets the next execution time after the given time
     * 获取给定时间之后的下次执行时间
     *
     * @param from the start time | 开始时间
     * @return the next execution time, or null if none within 4 years | 下次执行时间，如果4年内没有则返回null
     */
    public ZonedDateTime nextExecution(ZonedDateTime from) {
        java.util.Objects.requireNonNull(from, "from must not be null");
        ZonedDateTime next;
        if (hasSeconds) {
            next = from.plusSeconds(1).withNano(0);
        } else {
            next = from.plusMinutes(1).withSecond(0).withNano(0);
        }

        int maxYearAdvances = 4;
        int startYear = next.getYear();

        while (next.getYear() - startYear <= maxYearAdvances) {
            // 1. Month
            int nm = months.nextSetBit(next.getMonthValue());
            if (nm < 0) {
                next = next.plusYears(1).withMonth(1).withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0);
                continue;
            }
            if (nm != next.getMonthValue()) {
                next = next.withMonth(nm).withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0);
            }

            // 2. Day (with OR semantics)
            boolean dayFound = false;
            int daysInMonth = next.toLocalDate().lengthOfMonth();
            for (int d = next.getDayOfMonth(); d <= daysInMonth; d++) {
                ZonedDateTime candidate = next.withDayOfMonth(d);
                if (matchesDay(candidate)) {
                    if (d != next.getDayOfMonth()) {
                        next = candidate.withHour(0).withMinute(0).withSecond(0);
                    }
                    dayFound = true;
                    break;
                }
            }
            if (!dayFound) {
                next = next.plusMonths(1).withDayOfMonth(1)
                        .withHour(0).withMinute(0).withSecond(0);
                continue;
            }

            // 3. Hour
            int nh = hours.nextSetBit(next.getHour());
            if (nh < 0) {
                next = next.plusDays(1).withHour(0).withMinute(0).withSecond(0);
                continue;
            }
            if (nh != next.getHour()) {
                next = next.withHour(nh).withMinute(0).withSecond(0);
            }

            // 4. Minute
            int nmin = minutes.nextSetBit(next.getMinute());
            if (nmin < 0) {
                next = next.plusHours(1).withMinute(0).withSecond(0);
                continue;
            }
            if (nmin != next.getMinute()) {
                next = next.withMinute(nmin).withSecond(0);
            }

            // 5. Second
            int ns = seconds.nextSetBit(next.getSecond());
            if (ns < 0) {
                next = next.plusMinutes(1).withSecond(0);
                continue;
            }
            next = next.withSecond(ns);

            // Final validation
            if (matches(next)) {
                return next;
            }
            next = next.plusDays(1).withHour(0).withMinute(0).withSecond(0);
        }
        return null;
    }

    /**
     * Gets the next N execution times after the given time
     * 获取给定时间之后的下N次执行时间
     *
     * @param from  the start time | 开始时间
     * @param count the number of executions | 执行次数
     * @return the list of execution times | 执行时间列表
     * @throws IllegalArgumentException if count is not positive | 如果count不是正数
     */
    public List<ZonedDateTime> nextExecutions(ZonedDateTime from, int count) {
        java.util.Objects.requireNonNull(from, "from must not be null");
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive: " + count);
        }
        List<ZonedDateTime> result = new ArrayList<>(count);
        ZonedDateTime current = from;
        for (int i = 0; i < count; i++) {
            ZonedDateTime next = nextExecution(current);
            if (next == null) break;
            result.add(next);
            current = next;
        }
        return result;
    }

    // ==================== Previous Execution | 上次执行 ====================

    /**
     * Gets the previous execution time before the given time
     * 获取给定时间之前的上次执行时间
     *
     * @param from the reference time | 参考时间
     * @return the previous execution time, or null if none within 4 years | 上次执行时间，如果4年内没有则返回null
     */
    public ZonedDateTime previousExecution(ZonedDateTime from) {
        java.util.Objects.requireNonNull(from, "from must not be null");
        ZonedDateTime prev;
        if (hasSeconds) {
            prev = from.minusSeconds(1).withNano(0);
        } else {
            prev = from.minusMinutes(1).withSecond(0).withNano(0);
        }

        int maxYearAdvances = 4;
        int startYear = prev.getYear();

        while (startYear - prev.getYear() <= maxYearAdvances) {
            // 1. Month
            int pm = months.previousSetBit(prev.getMonthValue());
            if (pm < 0) {
                prev = prev.minusYears(1).withMonth(12).withDayOfMonth(31)
                        .withHour(23).withMinute(59).withSecond(59);
                // Adjust day to actual last day of December
                prev = prev.withDayOfMonth(prev.toLocalDate().lengthOfMonth());
                continue;
            }
            if (pm != prev.getMonthValue()) {
                prev = prev.withMonth(pm);
                int lastDay = prev.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
                prev = prev.withDayOfMonth(lastDay).withHour(23).withMinute(59).withSecond(59);
            }

            // 2. Day (reverse)
            boolean dayFound = false;
            for (int d = prev.getDayOfMonth(); d >= 1; d--) {
                ZonedDateTime candidate = prev.withDayOfMonth(d);
                if (matchesDay(candidate)) {
                    if (d != prev.getDayOfMonth()) {
                        prev = candidate.withHour(23).withMinute(59).withSecond(59);
                    }
                    dayFound = true;
                    break;
                }
            }
            if (!dayFound) {
                prev = prev.minusMonths(1);
                int lastDay = prev.toLocalDate().with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth();
                prev = prev.withDayOfMonth(lastDay).withHour(23).withMinute(59).withSecond(59);
                continue;
            }

            // 3. Hour
            int ph = hours.previousSetBit(prev.getHour());
            if (ph < 0) {
                prev = prev.minusDays(1).withHour(23).withMinute(59).withSecond(59);
                continue;
            }
            if (ph != prev.getHour()) {
                prev = prev.withHour(ph).withMinute(59).withSecond(59);
            }

            // 4. Minute
            int pmin = minutes.previousSetBit(prev.getMinute());
            if (pmin < 0) {
                prev = prev.minusHours(1).withMinute(59).withSecond(59);
                continue;
            }
            if (pmin != prev.getMinute()) {
                prev = prev.withMinute(pmin).withSecond(59);
            }

            // 5. Second
            int ps = seconds.previousSetBit(prev.getSecond());
            if (ps < 0) {
                prev = prev.minusMinutes(1).withSecond(59);
                continue;
            }
            prev = prev.withSecond(ps);

            if (matches(prev)) {
                return prev;
            }
            prev = prev.minusDays(1).withHour(23).withMinute(59).withSecond(59);
        }
        return null;
    }

    /**
     * Gets the previous N execution times before the given time
     * 获取给定时间之前的前N次执行时间
     *
     * @param from  the reference time | 参考时间
     * @param count the number of executions | 执行次数
     * @return the list of execution times (newest first) | 执行时间列表（最新在前）
     * @throws IllegalArgumentException if count is not positive | 如果count不是正数
     */
    public List<ZonedDateTime> previousExecutions(ZonedDateTime from, int count) {
        java.util.Objects.requireNonNull(from, "from must not be null");
        if (count <= 0) {
            throw new IllegalArgumentException("count must be positive: " + count);
        }
        List<ZonedDateTime> result = new ArrayList<>(count);
        ZonedDateTime current = from;
        for (int i = 0; i < count; i++) {
            ZonedDateTime prev = previousExecution(current);
            if (prev == null) break;
            result.add(prev);
            current = prev;
        }
        return result;
    }

    // ==================== Describe | 描述 ====================

    /**
     * Gets a human-readable description of this expression
     * 获取此表达式的人类可读描述
     *
     * @return the description in English | 英文描述
     */
    public String describe() {
        return CronDescriber.describe(this);
    }

    // ==================== Accessors | 访问器 ====================

    /**
     * Gets the original expression string
     * 获取原始表达式字符串
     *
     * @return the expression | 表达式
     */
    public String getExpression() {
        return expression;
    }

    /**
     * Checks if this expression uses 6-field format (with seconds)
     * 检查此表达式是否使用6字段格式（含秒）
     *
     * @return true if 6-field format | 如果是6字段格式返回true
     */
    public boolean hasSeconds() {
        return hasSeconds;
    }

    // Package-private accessors for CronDescriber
    BitSet seconds()     { return seconds; }
    BitSet minutes()     { return minutes; }
    BitSet hours()       { return hours; }
    BitSet daysOfMonth() { return daysOfMonth; }
    BitSet months()      { return months; }
    BitSet daysOfWeek()  { return daysOfWeek; }
    boolean domRestricted()  { return domRestricted; }
    boolean dowRestricted()  { return dowRestricted; }
    boolean lastDayOfMonth() { return lastDayOfMonth; }
    int lastDayOffset()      { return lastDayOffset; }
    boolean lastWeekday()    { return lastWeekday; }
    int nearestWeekday()     { return nearestWeekday; }
    int nthDayOfWeek()       { return nthDayOfWeek; }
    int nthDayOfWeekOrdinal(){ return nthDayOfWeekOrdinal; }
    boolean lastDayOfWeek()  { return lastDayOfWeek; }
    int lastDayOfWeekValue() { return lastDayOfWeekValue; }

    // ==================== Object Methods ====================

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CronExpression that)) return false;
        return Objects.equals(expression, that.expression);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(expression);
    }

    @Override
    public String toString() {
        return expression;
    }
}

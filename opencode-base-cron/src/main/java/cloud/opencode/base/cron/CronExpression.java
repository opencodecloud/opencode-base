package cloud.opencode.base.cron;

import cloud.opencode.base.cron.exception.OpenCronException;

import java.io.Serial;
import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

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
 *   <li>Lazy streams: {@link #stream}, {@link #reverseStream} - 惰性流式调度</li>
 *   <li>Filtered scheduling: {@link #nextExecution(ZonedDateTime, Predicate)} - 过滤调度（节假日排除等）</li>
 *   <li>Schedule overlap detection: {@link #nextOverlap}, {@link #hasOverlapBetween} - 调度重叠检测</li>
 *   <li>{@link TemporalAdjuster} integration: {@code zdt.with(cronExpr)} - 时间调节器集成</li>
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
public final class CronExpression implements Serializable, TemporalAdjuster {

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
                    if (from > to) {
                        // Wrap-around range with step: e.g., 22-2/2 for hours → 22,0,2
                        for (int i = from; i <= max; i += step) {
                            bits.set(i);
                        }
                        int offset = (max + 1 - from) % step;
                        int wrapStart = min + (step - offset) % step;
                        for (int i = wrapStart; i <= to; i += step) {
                            bits.set(i);
                        }
                    } else {
                        for (int i = from; i <= to; i += step) {
                            bits.set(i);
                        }
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
        Objects.requireNonNull(time, "time must not be null");
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
        return nextExecution(from, 4);
    }

    /**
     * Gets the next execution time with configurable search window
     * 获取可配置搜索窗口的下次执行时间
     *
     * @param from     the start time | 开始时间
     * @param maxYears the maximum years to search (1-100) | 最大搜索年数（1-100）
     * @return the next execution time, or null if none within maxYears | 下次执行时间
     * @throws IllegalArgumentException if maxYears is not in range 1-100 | 如果maxYears不在1-100范围内
     */
    public ZonedDateTime nextExecution(ZonedDateTime from, int maxYears) {
        Objects.requireNonNull(from, "from must not be null");
        if (maxYears < 1 || maxYears > 100) {
            throw new IllegalArgumentException("maxYears must be between 1 and 100: " + maxYears);
        }
        ZonedDateTime next;
        if (hasSeconds) {
            next = from.plusSeconds(1).withNano(0);
        } else {
            next = from.plusMinutes(1).withSecond(0).withNano(0);
        }

        int startYear = next.getYear();

        while (next.getYear() - startYear < maxYears) {
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
        Objects.requireNonNull(from, "from must not be null");
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
        return previousExecution(from, 4);
    }

    /**
     * Gets the previous execution time with configurable search window
     * 获取可配置搜索窗口的上次执行时间
     *
     * @param from     the reference time | 参考时间
     * @param maxYears the maximum years to search (1-100) | 最大搜索年数（1-100）
     * @return the previous execution time, or null if none within maxYears | 上次执行时间
     * @throws IllegalArgumentException if maxYears is not in range 1-100 | 如果maxYears不在1-100范围内
     */
    public ZonedDateTime previousExecution(ZonedDateTime from, int maxYears) {
        Objects.requireNonNull(from, "from must not be null");
        if (maxYears < 1 || maxYears > 100) {
            throw new IllegalArgumentException("maxYears must be between 1 and 100: " + maxYears);
        }
        ZonedDateTime prev;
        if (hasSeconds) {
            prev = from.minusSeconds(1).withNano(0);
        } else {
            prev = from.minusMinutes(1).withSecond(0).withNano(0);
        }

        int startYear = prev.getYear();

        while (startYear - prev.getYear() < maxYears) {
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
        Objects.requireNonNull(from, "from must not be null");
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

    // ==================== Duration Convenience | 时间间隔便利方法 ====================

    /**
     * Gets the duration until the next execution
     * 获取距下次执行的时间间隔
     *
     * @param from the reference time | 参考时间
     * @return the duration to next execution, or null if none within search window | 距下次执行的Duration
     */
    public Duration timeToNextExecution(ZonedDateTime from) {
        Objects.requireNonNull(from, "from must not be null");
        ZonedDateTime next = nextExecution(from);
        return next != null ? Duration.between(from, next) : null;
    }

    /**
     * Gets the duration since the last execution
     * 获取距上次执行的时间间隔
     *
     * @param from the reference time | 参考时间
     * @return the duration from last execution, or null if none within search window | 距上次执行的Duration
     */
    public Duration timeFromLastExecution(ZonedDateTime from) {
        Objects.requireNonNull(from, "from must not be null");
        ZonedDateTime prev = previousExecution(from);
        return prev != null ? Duration.between(prev, from) : null;
    }

    // ==================== Executions Between | 区间执行 ====================

    /**
     * Counts executions between two times
     * 计算两个时间点之间的执行次数
     *
     * <p>The maximum count is capped at 1,000,000 to prevent excessive computation.
     * If the count reaches this limit, 1,000,000 is returned (which may be an undercount).</p>
     * <p>最大计数上限为1,000,000以防止过度计算。如果达到此限制，返回1,000,000（可能是不完整的计数）。</p>
     *
     * @param from the start time (exclusive) | 开始时间（不包含）
     * @param to   the end time (inclusive) | 结束时间（包含）
     * @return the count of executions (capped at 1,000,000) | 执行次数（上限1,000,000）
     * @throws IllegalArgumentException if from is not before to | 如果from不在to之前
     */
    public long countExecutionsBetween(ZonedDateTime from, ZonedDateTime to) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        long count = 0;
        ZonedDateTime current = from;
        while (count < 1_000_000) {
            ZonedDateTime next = nextExecution(current);
            if (next == null || next.isAfter(to)) {
                break;
            }
            count++;
            current = next;
        }
        return count;
    }

    /**
     * Lists all executions between two times
     * 列出两个时间点之间的所有执行时间
     *
     * @param from the start time (exclusive) | 开始时间（不包含）
     * @param to   the end time (inclusive) | 结束时间（包含）
     * @return unmodifiable list of execution times | 不可修改的执行时间列表
     * @throws IllegalArgumentException if from is not before to | 如果from不在to之前
     */
    public List<ZonedDateTime> executionsBetween(ZonedDateTime from, ZonedDateTime to) {
        return executionsBetween(from, to, 100_000);
    }

    /**
     * Lists executions between two times with a limit
     * 列出两个时间点之间的执行时间（带限制）
     *
     * @param from  the start time (exclusive) | 开始时间（不包含）
     * @param to    the end time (inclusive) | 结束时间（包含）
     * @param limit the maximum number of results (1-1_000_000) | 最大结果数
     * @return unmodifiable list of execution times | 不可修改的执行时间列表
     * @throws IllegalArgumentException if from is not before to, or limit is out of range | 如果from不在to之前或limit超出范围
     */
    public List<ZonedDateTime> executionsBetween(ZonedDateTime from, ZonedDateTime to, int limit) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        if (limit < 1 || limit > 1_000_000) {
            throw new IllegalArgumentException("limit must be between 1 and 1_000_000: " + limit);
        }
        List<ZonedDateTime> result = new ArrayList<>();
        ZonedDateTime current = from;
        while (result.size() < limit) {
            ZonedDateTime next = nextExecution(current);
            if (next == null || next.isAfter(to)) {
                break;
            }
            result.add(next);
            current = next;
        }
        return List.copyOf(result);
    }

    // ==================== Equivalence | 等价性 ====================

    /**
     * Checks if this expression is structurally equivalent to another
     * 检查此表达式是否与另一个结构等价
     *
     * <p>Compares internal parsed state (BitSets, special fields, and field count),
     * not the original expression strings. Note: a 5-field expression and a 6-field
     * expression with second=0 produce the same schedule, but are considered
     * structurally different (hasSeconds differs).</p>
     * <p>比较内部解析状态（BitSet、特殊字段和字段数），而非原始表达式字符串。
     * 注意：5字段表达式和秒=0的6字段表达式产生相同调度，但被视为结构不同（hasSeconds不同）。</p>
     *
     * @param other the other expression | 另一个表达式
     * @return true if equivalent schedules | 如果调度等价返回true
     */
    public boolean isEquivalentTo(CronExpression other) {
        if (other == null) return false;
        if (this == other) return true;
        return this.seconds.equals(other.seconds)
                && this.minutes.equals(other.minutes)
                && this.hours.equals(other.hours)
                && this.daysOfMonth.equals(other.daysOfMonth)
                && this.months.equals(other.months)
                && this.daysOfWeek.equals(other.daysOfWeek)
                && this.hasSeconds == other.hasSeconds
                && this.domRestricted == other.domRestricted
                && this.dowRestricted == other.dowRestricted
                && this.lastDayOfMonth == other.lastDayOfMonth
                && this.lastDayOffset == other.lastDayOffset
                && this.lastWeekday == other.lastWeekday
                && this.nearestWeekday == other.nearestWeekday
                && this.nthDayOfWeek == other.nthDayOfWeek
                && this.nthDayOfWeekOrdinal == other.nthDayOfWeekOrdinal
                && this.lastDayOfWeek == other.lastDayOfWeek
                && this.lastDayOfWeekValue == other.lastDayOfWeekValue;
    }

    // ==================== Explain | 解释 ====================

    /**
     * Gets a comprehensive explanation for debugging
     * 获取用于调试的综合解释信息
     *
     * <p>The estimated interval is {@link Duration#ZERO} when fewer than 2 upcoming
     * executions can be found within the search window.</p>
     * <p>当搜索窗口内找到的即将执行次数少于2次时，预估间隔为 {@link Duration#ZERO}。</p>
     *
     * @param from the reference time for next executions | 用于计算下次执行的参考时间
     * @return the explanation | 解释信息
     */
    public CronExplanation explain(ZonedDateTime from) {
        Objects.requireNonNull(from, "from must not be null");
        String desc = describe();
        List<ZonedDateTime> nextExecs = nextExecutions(from, 5);
        Duration interval = Duration.ZERO;
        if (nextExecs.size() >= 2) {
            interval = Duration.between(nextExecs.get(0), nextExecs.get(1));
        }
        return new CronExplanation(expression, desc, nextExecs, interval);
    }

    // ==================== Stream | 流式调度 ====================

    /**
     * Returns a lazy, ordered stream of future execution times
     * 返回一个惰性的、有序的未来执行时间流
     *
     * <p>The stream is computed lazily — each element is calculated on demand.
     * Use {@code .limit()}, {@code .takeWhile()}, {@code .filter()} etc. to control iteration.</p>
     * <p>流是惰性计算的——每个元素按需计算。
     * 使用 {@code .limit()}、{@code .takeWhile()}、{@code .filter()} 等控制迭代。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * // Next 10 executions | 下10次执行
     * expr.stream(now).limit(10).toList();
     *
     * // All executions before deadline | deadline之前的所有执行
     * expr.stream(now).takeWhile(t -> t.isBefore(deadline)).toList();
     * }</pre>
     *
     * @param from the start time (exclusive) | 开始时间（不包含）
     * @return an ordered, sequential stream of execution times | 有序的顺序执行时间流
     */
    public Stream<ZonedDateTime> stream(ZonedDateTime from) {
        Objects.requireNonNull(from, "from must not be null");
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new java.util.Iterator<>() {
                            private ZonedDateTime current = from;
                            private ZonedDateTime lookahead;
                            private boolean lookaheadValid;

                            @Override
                            public boolean hasNext() {
                                if (!lookaheadValid) {
                                    lookahead = nextExecution(current);
                                    lookaheadValid = true;
                                }
                                return lookahead != null;
                            }

                            @Override
                            public ZonedDateTime next() {
                                if (!hasNext()) {
                                    throw new java.util.NoSuchElementException(
                                            "No more executions within search window");
                                }
                                lookaheadValid = false;
                                current = lookahead;
                                return lookahead;
                            }
                        },
                        Spliterator.ORDERED | Spliterator.NONNULL
                ),
                false
        );
    }

    /**
     * Returns a lazy, ordered stream of past execution times (newest first)
     * 返回一个惰性的、有序的过去执行时间流（最新在前）
     *
     * <p>The stream is computed lazily — each element is calculated on demand.</p>
     * <p>流是惰性计算的——每个元素按需计算。</p>
     *
     * @param from the reference time (exclusive) | 参考时间（不包含）
     * @return an ordered, sequential stream of past execution times | 有序的顺序过去执行时间流
     */
    public Stream<ZonedDateTime> reverseStream(ZonedDateTime from) {
        Objects.requireNonNull(from, "from must not be null");
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        new java.util.Iterator<>() {
                            private ZonedDateTime current = from;
                            private ZonedDateTime lookahead;
                            private boolean lookaheadValid;

                            @Override
                            public boolean hasNext() {
                                if (!lookaheadValid) {
                                    lookahead = previousExecution(current);
                                    lookaheadValid = true;
                                }
                                return lookahead != null;
                            }

                            @Override
                            public ZonedDateTime next() {
                                if (!hasNext()) {
                                    throw new java.util.NoSuchElementException(
                                            "No more executions within search window");
                                }
                                lookaheadValid = false;
                                current = lookahead;
                                return lookahead;
                            }
                        },
                        Spliterator.ORDERED | Spliterator.NONNULL
                ),
                false
        );
    }

    // ==================== Filtered Scheduling | 过滤调度 ====================

    /**
     * Gets the next execution time that satisfies a filter
     * 获取满足过滤条件的下次执行时间
     *
     * <p>Skips execution times that do not pass the filter predicate.
     * Stops after 10,000 skipped candidates to prevent infinite loops.</p>
     * <p>跳过不满足过滤谓词的执行时间。跳过10,000个候选后停止以防止无限循环。</p>
     *
     * <p><strong>Examples | 示例:</strong></p>
     * <pre>{@code
     * // Skip weekends | 跳过周末
     * expr.nextExecution(now, t -> t.getDayOfWeek().getValue() <= 5);
     *
     * // Skip holidays | 跳过节假日
     * Set<LocalDate> holidays = Set.of(LocalDate.of(2026, 1, 1));
     * expr.nextExecution(now, t -> !holidays.contains(t.toLocalDate()));
     * }</pre>
     *
     * @param from   the start time (exclusive) | 开始时间（不包含）
     * @param filter the filter predicate — returns true to accept, false to skip | 过滤谓词
     * @return the next matching execution time, or null if none found | 下次匹配的执行时间
     */
    public ZonedDateTime nextExecution(ZonedDateTime from, Predicate<ZonedDateTime> filter) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(filter, "filter must not be null");
        ZonedDateTime current = from;
        for (int i = 0; i < 10_000; i++) {
            ZonedDateTime next = nextExecution(current);
            if (next == null) {
                return null;
            }
            if (filter.test(next)) {
                return next;
            }
            current = next;
        }
        return null;
    }

    /**
     * Gets the previous execution time that satisfies a filter
     * 获取满足过滤条件的上次执行时间
     *
     * <p>Skips execution times that do not pass the filter predicate.
     * Stops after 10,000 skipped candidates to prevent infinite loops.</p>
     * <p>跳过不满足过滤谓词的执行时间。跳过10,000个候选后停止以防止无限循环。</p>
     *
     * @param from   the reference time (exclusive) | 参考时间（不包含）
     * @param filter the filter predicate — returns true to accept, false to skip | 过滤谓词
     * @return the previous matching execution time, or null if none found | 上次匹配的执行时间
     */
    public ZonedDateTime previousExecution(ZonedDateTime from, Predicate<ZonedDateTime> filter) {
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(filter, "filter must not be null");
        ZonedDateTime current = from;
        for (int i = 0; i < 10_000; i++) {
            ZonedDateTime prev = previousExecution(current);
            if (prev == null) {
                return null;
            }
            if (filter.test(prev)) {
                return prev;
            }
            current = prev;
        }
        return null;
    }

    // ==================== Overlap Detection | 重叠检测 ====================

    /**
     * Finds the next time both this and another expression fire simultaneously
     * 查找此表达式与另一个表达式同时触发的下一个时间
     *
     * <p>Iterates forward through both schedules to find the first overlap.
     * Returns null if no overlap is found within 4 years.</p>
     * <p>正向遍历两个调度以找到第一个重叠时间。如果4年内找不到重叠则返回null。</p>
     *
     * @param other the other cron expression | 另一个Cron表达式
     * @param from  the start time (exclusive) | 开始时间（不包含）
     * @return the next overlapping execution time, or null | 下一个重叠执行时间
     */
    public ZonedDateTime nextOverlap(CronExpression other, ZonedDateTime from) {
        Objects.requireNonNull(other, "other must not be null");
        Objects.requireNonNull(from, "from must not be null");
        ZonedDateTime deadline = from.plusYears(4);
        ZonedDateTime a = nextExecution(from);
        ZonedDateTime b = other.nextExecution(from);
        while (a != null && b != null && !a.isAfter(deadline) && !b.isAfter(deadline)) {
            int cmp = a.compareTo(b);
            if (cmp == 0) {
                return a;
            } else if (cmp < 0) {
                a = nextExecution(a);
            } else {
                b = other.nextExecution(b);
            }
        }
        return null;
    }

    /**
     * Checks if this and another expression have overlapping executions in a time range
     * 检查此表达式与另一个表达式在时间范围内是否有重叠执行
     *
     * @param other the other cron expression | 另一个Cron表达式
     * @param from  the start time (exclusive) | 开始时间（不包含）
     * @param to    the end time (inclusive) | 结束时间（包含）
     * @return true if there is at least one overlapping execution | 如果存在重叠返回true
     */
    public boolean hasOverlapBetween(CronExpression other, ZonedDateTime from, ZonedDateTime to) {
        Objects.requireNonNull(other, "other must not be null");
        Objects.requireNonNull(from, "from must not be null");
        Objects.requireNonNull(to, "to must not be null");
        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("from must be before to");
        }
        ZonedDateTime a = nextExecution(from);
        ZonedDateTime b = other.nextExecution(from);
        while (a != null && b != null && !a.isAfter(to) && !b.isAfter(to)) {
            int cmp = a.compareTo(b);
            if (cmp == 0) {
                return true;
            } else if (cmp < 0) {
                a = nextExecution(a);
            } else {
                b = other.nextExecution(b);
            }
        }
        return false;
    }

    // ==================== TemporalAdjuster | 时间调节器 ====================

    /**
     * Adjusts the temporal to the next execution time of this cron expression
     * 将时间调节为此Cron表达式的下次执行时间
     *
     * <p>Enables idiomatic usage with {@code java.time}:</p>
     * <p>支持 {@code java.time} 的惯用写法：</p>
     * <pre>{@code
     * ZonedDateTime next = ZonedDateTime.now().with(cronExpr);
     * }</pre>
     *
     * @param temporal the temporal to adjust | 要调节的时间
     * @return the next execution time | 下次执行时间
     * @throws java.time.DateTimeException if no execution found within search window | 如果搜索窗口内找不到执行时间
     */
    @Override
    public Temporal adjustInto(Temporal temporal) {
        if (!(temporal instanceof ZonedDateTime zdt)) {
            throw new java.time.DateTimeException(
                    "CronExpression requires a ZonedDateTime; got: "
                            + temporal.getClass().getSimpleName());
        }
        ZonedDateTime next = nextExecution(zdt);
        if (next == null) {
            throw new java.time.DateTimeException(
                    "No execution found for '" + expression + "' within search window");
        }
        return next;
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

    /**
     * Gets a human-readable description in the specified locale
     * 获取指定语言的人类可读描述
     *
     * @param locale the locale (supports CHINESE/SIMPLIFIED_CHINESE for Chinese, others default to English)
     *               语言（支持CHINESE/SIMPLIFIED_CHINESE返回中文，其他默认英文）
     * @return the localized description | 本地化描述
     */
    public String describe(Locale locale) {
        Objects.requireNonNull(locale, "locale must not be null");
        if ("zh".equals(locale.getLanguage())) {
            return CronDescriberZh.describe(this);
        }
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

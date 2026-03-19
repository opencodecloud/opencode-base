package cloud.opencode.base.expression.function;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Date Functions
 * 日期函数
 *
 * <p>Provides built-in date and time functions for expressions.</p>
 * <p>为表达式提供内置的日期时间函数。</p>
 *
 * <p><strong>Features | 主要功能:</strong></p>
 * <ul>
 *   <li>Current: now, today, currenttime, timestamp - 当前时间</li>
 *   <li>Creation: date, datetime, time - 创建</li>
 *   <li>Extraction: year, month, day, hour, minute, second, dayofweek, dayofyear - 提取</li>
 *   <li>Arithmetic: adddays, addmonths, addyears, addhours, addminutes, addseconds - 算术</li>
 *   <li>Difference: daysbetween, monthsbetween, yearsbetween, hoursbetween - 差值</li>
 *   <li>Formatting: formatdate, parsedate, parsedatetime - 格式化</li>
 *   <li>Boundaries: startofday, endofday, startofmonth, endofmonth - 边界</li>
 *   <li>Checks: isbefore, isafter, isweekend, isleapyear - 检查</li>
 * </ul>
 *
 * <p><strong>Usage Examples | 使用示例:</strong></p>
 * <pre>{@code
 * Object today = OpenExpression.eval("today()");
 * Object year = OpenExpression.eval("year(today())");
 * Object fmt = OpenExpression.eval("formatdate(today(), 'yyyy-MM-dd')");
 * }</pre>
 *
 * <p><strong>Security | 安全性:</strong></p>
 * <ul>
 *   <li>Thread-safe: Yes, stateless utility class - 线程安全: 是，无状态工具类</li>
 *   <li>Null-safe: Yes, null arguments return current date/time or defaults - 空值安全: 是，null参数返回当前日期/时间或默认值</li>
 * </ul>
 *
 * @author Leon Soo
 * <a href="https://leonsoo.com">www.LeonSoo.com</a>
 * @see <a href="https://opencode.cloud">OpenCode.cloud</a>
 * @since JDK 25, opencode-base-expression V1.0.0
 */
public final class DateFunctions {

    private DateFunctions() {
    }

    /**
     * Get all date functions
     * 获取所有日期函数
     *
     * @return the function map | 函数映射
     */
    public static Map<String, Function> getFunctions() {
        Map<String, Function> functions = new LinkedHashMap<>();

        // Current date/time
        functions.put("now", args -> LocalDateTime.now());
        functions.put("today", args -> LocalDate.now());
        functions.put("currenttime", args -> LocalTime.now());
        functions.put("timestamp", args -> System.currentTimeMillis());

        // Date creation
        functions.put("date", args -> {
            if (args.length >= 3) {
                return LocalDate.of(toInt(args[0]), toInt(args[1]), toInt(args[2]));
            } else if (args.length == 1 && args[0] instanceof String s) {
                return LocalDate.parse(s);
            }
            return LocalDate.now();
        });

        functions.put("datetime", args -> {
            if (args.length >= 6) {
                return LocalDateTime.of(
                        toInt(args[0]), toInt(args[1]), toInt(args[2]),
                        toInt(args[3]), toInt(args[4]), toInt(args[5])
                );
            } else if (args.length >= 3) {
                return LocalDate.of(toInt(args[0]), toInt(args[1]), toInt(args[2])).atStartOfDay();
            } else if (args.length == 1 && args[0] instanceof String s) {
                return LocalDateTime.parse(s);
            }
            return LocalDateTime.now();
        });

        functions.put("time", args -> {
            if (args.length >= 3) {
                return LocalTime.of(toInt(args[0]), toInt(args[1]), toInt(args[2]));
            } else if (args.length >= 2) {
                return LocalTime.of(toInt(args[0]), toInt(args[1]));
            } else if (args.length == 1 && args[0] instanceof String s) {
                return LocalTime.parse(s);
            }
            return LocalTime.now();
        });

        // Date extraction
        functions.put("year", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().getYear();
            return toLocalDate(args[0]).getYear();
        });

        functions.put("month", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().getMonthValue();
            return toLocalDate(args[0]).getMonthValue();
        });

        functions.put("day", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().getDayOfMonth();
            return toLocalDate(args[0]).getDayOfMonth();
        });

        functions.put("dayofweek", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().getDayOfWeek().getValue();
            return toLocalDate(args[0]).getDayOfWeek().getValue();
        });

        functions.put("dayofyear", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().getDayOfYear();
            return toLocalDate(args[0]).getDayOfYear();
        });

        functions.put("hour", args -> {
            if (args.length < 1 || args[0] == null) return LocalTime.now().getHour();
            return toLocalTime(args[0]).getHour();
        });

        functions.put("minute", args -> {
            if (args.length < 1 || args[0] == null) return LocalTime.now().getMinute();
            return toLocalTime(args[0]).getMinute();
        });

        functions.put("second", args -> {
            if (args.length < 1 || args[0] == null) return LocalTime.now().getSecond();
            return toLocalTime(args[0]).getSecond();
        });

        // Date arithmetic
        functions.put("adddays", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            return toLocalDate(args[0]).plusDays(toLong(args[1]));
        });

        functions.put("addmonths", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            return toLocalDate(args[0]).plusMonths(toLong(args[1]));
        });

        functions.put("addyears", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            return toLocalDate(args[0]).plusYears(toLong(args[1]));
        });

        functions.put("addhours", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            return toLocalDateTime(args[0]).plusHours(toLong(args[1]));
        });

        functions.put("addminutes", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            return toLocalDateTime(args[0]).plusMinutes(toLong(args[1]));
        });

        functions.put("addseconds", args -> {
            if (args.length < 2 || args[0] == null) return args.length > 0 ? args[0] : null;
            return toLocalDateTime(args[0]).plusSeconds(toLong(args[1]));
        });

        // Date difference
        functions.put("daysbetween", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return 0L;
            return ChronoUnit.DAYS.between(toLocalDate(args[0]), toLocalDate(args[1]));
        });

        functions.put("monthsbetween", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return 0L;
            return ChronoUnit.MONTHS.between(toLocalDate(args[0]), toLocalDate(args[1]));
        });

        functions.put("yearsbetween", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return 0L;
            return ChronoUnit.YEARS.between(toLocalDate(args[0]), toLocalDate(args[1]));
        });

        functions.put("hoursbetween", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return 0L;
            return ChronoUnit.HOURS.between(toLocalDateTime(args[0]), toLocalDateTime(args[1]));
        });

        functions.put("minutesbetween", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return 0L;
            return ChronoUnit.MINUTES.between(toLocalDateTime(args[0]), toLocalDateTime(args[1]));
        });

        functions.put("secondsbetween", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return 0L;
            return ChronoUnit.SECONDS.between(toLocalDateTime(args[0]), toLocalDateTime(args[1]));
        });

        // Date formatting
        functions.put("formatdate", args -> {
            if (args.length < 1 || args[0] == null) return "";
            String pattern = args.length >= 2 ? args[1].toString() : "yyyy-MM-dd";
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            Object date = args[0];
            if (date instanceof LocalDate ld) return ld.format(formatter);
            if (date instanceof LocalDateTime ldt) return ldt.format(formatter);
            if (date instanceof LocalTime lt) return lt.format(DateTimeFormatter.ofPattern(pattern));
            return date.toString();
        });

        // Date parsing
        functions.put("parsedate", args -> {
            if (args.length < 1 || args[0] == null) return null;
            String s = args[0].toString();
            if (args.length >= 2) {
                String pattern = args[1].toString();
                return LocalDate.parse(s, DateTimeFormatter.ofPattern(pattern));
            }
            return LocalDate.parse(s);
        });

        functions.put("parsedatetime", args -> {
            if (args.length < 1 || args[0] == null) return null;
            String s = args[0].toString();
            if (args.length >= 2) {
                String pattern = args[1].toString();
                return LocalDateTime.parse(s, DateTimeFormatter.ofPattern(pattern));
            }
            return LocalDateTime.parse(s);
        });

        // Special dates
        functions.put("startofday", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().atStartOfDay();
            return toLocalDate(args[0]).atStartOfDay();
        });

        functions.put("endofday", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().atTime(LocalTime.MAX);
            return toLocalDate(args[0]).atTime(LocalTime.MAX);
        });

        functions.put("startofmonth", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().withDayOfMonth(1);
            return toLocalDate(args[0]).withDayOfMonth(1);
        });

        functions.put("endofmonth", args -> {
            if (args.length < 1 || args[0] == null) {
                return LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
            }
            return toLocalDate(args[0]).with(TemporalAdjusters.lastDayOfMonth());
        });

        functions.put("startofyear", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().withDayOfYear(1);
            return toLocalDate(args[0]).withDayOfYear(1);
        });

        functions.put("endofyear", args -> {
            if (args.length < 1 || args[0] == null) {
                return LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
            }
            return toLocalDate(args[0]).with(TemporalAdjusters.lastDayOfYear());
        });

        // Comparison
        functions.put("isbefore", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return false;
            return toLocalDate(args[0]).isBefore(toLocalDate(args[1]));
        });

        functions.put("isafter", args -> {
            if (args.length < 2 || args[0] == null || args[1] == null) return false;
            return toLocalDate(args[0]).isAfter(toLocalDate(args[1]));
        });

        functions.put("isweekend", args -> {
            if (args.length < 1 || args[0] == null) return false;
            DayOfWeek dow = toLocalDate(args[0]).getDayOfWeek();
            return dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        });

        functions.put("isleapyear", args -> {
            if (args.length < 1 || args[0] == null) return LocalDate.now().isLeapYear();
            return toLocalDate(args[0]).isLeapYear();
        });

        return functions;
    }

    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) return ld;
        if (value instanceof LocalDateTime ldt) return ldt.toLocalDate();
        if (value instanceof Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        if (value instanceof Instant i) return i.atZone(ZoneId.systemDefault()).toLocalDate();
        if (value instanceof String s) return LocalDate.parse(s);
        if (value instanceof Number n) {
            return Instant.ofEpochMilli(n.longValue()).atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return LocalDate.now();
    }

    private static LocalDateTime toLocalDateTime(Object value) {
        if (value instanceof LocalDateTime ldt) return ldt;
        if (value instanceof LocalDate ld) return ld.atStartOfDay();
        if (value instanceof Date d) return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (value instanceof Instant i) return i.atZone(ZoneId.systemDefault()).toLocalDateTime();
        if (value instanceof String s) return LocalDateTime.parse(s);
        if (value instanceof Number n) {
            return Instant.ofEpochMilli(n.longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime();
        }
        return LocalDateTime.now();
    }

    private static LocalTime toLocalTime(Object value) {
        if (value instanceof LocalTime lt) return lt;
        if (value instanceof LocalDateTime ldt) return ldt.toLocalTime();
        if (value instanceof String s) return LocalTime.parse(s);
        return LocalTime.now();
    }

    private static int toInt(Object value) {
        if (value == null) return 0;
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static long toLong(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}

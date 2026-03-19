# Date 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-date` 模块提供现代化的日期时间处理能力，核心设计理念：

- **完全基于 java.time**：摒弃老旧的 Date 和 Calendar
- **智能解析**：自动识别 20+ 种常见日期格式
- **扩展类型**：提供 YearQuarter、YearWeek、YearHalf、Interval 等 ThreeTen-Extra 风格扩展
- **业务日历**：内置节假日、工作日、Cron 表达式支持
- **农历支持**：完整的农历转换、生肖、天干地支、节气
- **高性能**：DateTimeFormatter 缓存、时间缓存
- **人性化表达**：相对时间格式化（"3小时前"、"2天后"）

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                         应用层                                   │
│              (业务代码 / 其他组件)                                │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       门面层 (Facade)                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │  OpenDate   │  │  DateTimes  │  │     StopWatch           │  │
│  │  核心入口   │  │  静态工厂   │  │     性能计时            │  │
│  └─────────────┘  └─────────────┘  └─────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       功能层 (Feature)                           │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────────┐  │
│  │   formatter/  │  │   adjuster/   │  │      extra/         │  │
│  │DateFormatter  │  │TemporalAdj    │  │ YearQuarter/Week    │  │
│  │RelativeTime   │  │WorkdayAdj     │  │ Interval/Range      │  │
│  │PeriodFormatter│  │BusinessDayAdj │  │ Days/Hours/Months.. │  │
│  │DateParser     │  │DateAdjusters  │  │ AmPm/DayOfMonth/..  │  │
│  └───────────────┘  └───────────────┘  └─────────────────────┘  │
│  ┌───────────────┐  ┌───────────────┐  ┌─────────────────────┐  │
│  │   holiday/    │  │    lunar/     │  │     between/        │  │
│  │ HolidayUtil   │  │  LunarUtil    │  │ DateBetween/Age     │  │
│  │ HolidayCalendar│ │  SolarTerm    │  │ DateDiff/AgeDetail  │  │
│  └───────────────┘  └───────────────┘  └─────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       基础层 (Foundation)                        │
│  ┌───────────────────┐  ┌──────────────────┐  ┌──────────────┐  │
│  │    timezone/      │  │     range/       │  │    cron/     │  │
│  │  TimezoneUtil     │  │  DateRange       │  │ CronExpr     │  │
│  │ TimezoneConverter │  │  TimeRange       │  │  CronUtil    │  │
│  └───────────────────┘  └──────────────────┘  └──────────────┘  │
└─────────────────────────────────────────────────────────────────┘

设计特点:
├── java.time 优先：完全基于现代日期 API
├── 智能解析：自动识别 20+ 常见日期格式
├── 扩展类型：YearQuarter/YearWeek/YearHalf/Interval (ThreeTen-Extra 风格)
├── 时间单位：Seconds/Minutes/Hours/Days/Weeks/Months/Years
├── 业务支持：节假日/农历/工作日/Cron 表达式
└── 零依赖：仅依赖 Core 组件
```

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-date</artifactId>
    <version>${version}</version>
</dependency>
```

```
date 模块依赖:
├── opencode-base-core (必需，基础工具)
└── 无其他外部依赖

依赖 date 的组件:
├── tasker → date (Cron 表达式解析)
├── timeseries → date (时序数据时间戳)
├── resilience → date (时间窗口限流)
├── log → date (日志时间戳格式化)
└── json → date (日期序列化)
```

---

## 2. 包结构

```
cloud.opencode.base.date
├── OpenDate.java                      # 门面入口 - 核心日期操作
├── DateTimes.java                     # 日期时间静态工厂
├── TemporalUtil.java                  # 时间工具类
├── StopWatch.java                     # 高精度性能计时器
│
├── formatter/                         # 格式化与解析
│   ├── DateFormatter.java             # 预定义格式化器（含缓存）
│   ├── DateParser.java                # 智能日期解析器（20+格式自动识别）
│   ├── RelativeTimeFormatter.java     # 相对时间格式化（"3小时前"）
│   └── PeriodFormatter.java           # Period/Duration 格式化
│
├── adjuster/                          # 时间调整器
│   ├── TemporalAdjusters.java         # 预定义调整器（季度/半年/工作日等）
│   ├── DateAdjusters.java             # 扩展日期调整器
│   ├── WorkdayAdjuster.java           # 工作日调整器
│   └── BusinessDayAdjuster.java       # 业务日调整器（含节假日/调休）
│
├── extra/                             # 扩展类型（ThreeTen-Extra 风格）
│   ├── YearQuarter.java               # 年季度 (2024-Q1)
│   ├── YearWeek.java                  # 年周 (2024-W01)
│   ├── YearHalf.java                  # 年半期 (2024-H1)
│   ├── Quarter.java                   # 季度枚举 (Q1-Q4)
│   ├── Interval.java                  # 时间区间（基于 Instant）
│   ├── LocalDateRange.java            # 日期范围（可迭代/流式）
│   ├── LocalDateTimeRange.java        # 日期时间范围
│   ├── LocalTimeRange.java            # 时间范围（支持跨午夜）
│   ├── PeriodDuration.java            # 周期+时长组合 (P1Y2M3DT4H5M)
│   ├── Seconds.java                   # 秒数单位
│   ├── Minutes.java                   # 分钟单位
│   ├── Hours.java                     # 小时单位
│   ├── Days.java                      # 天数单位
│   ├── Weeks.java                     # 周数单位
│   ├── Months.java                    # 月数单位
│   ├── Years.java                     # 年数单位
│   ├── AmPm.java                      # 上午/下午枚举
│   ├── DayOfMonth.java                # 月中天 (1-31)
│   └── DayOfYear.java                 # 年中天 (1-366)
│
├── between/                           # 时间间隔计算
│   ├── DateBetween.java               # 日期间隔
│   ├── DateDiff.java                  # 详细日期差异分解
│   ├── AgeBetween.java                # 年龄计算
│   └── AgeDetail.java                 # 详细年龄统计
│
├── holiday/                           # 节假日
│   ├── Holiday.java                   # 节假日模型
│   ├── HolidayUtil.java               # 节假日工具
│   ├── HolidayCalendar.java           # 节假日日历
│   └── HolidayProvider.java           # 节假日数据提供者接口
│
├── lunar/                             # 农历
│   ├── Lunar.java                     # 农历日期
│   ├── LunarUtil.java                 # 农历工具
│   └── SolarTerm.java                 # 二十四节气
│
├── timezone/                          # 时区
│   ├── TimezoneUtil.java              # 时区工具
│   └── TimezoneConverter.java         # 链式时区转换器
│
├── range/                             # 兼容范围类
│   ├── DateRange.java                 # 日期范围（兼容包装）
│   ├── DateTimeRange.java             # 日期时间范围（兼容包装）
│   └── TimeRange.java                 # 时间范围（兼容包装）
│
├── cron/                              # Cron 表达式
│   ├── CronExpression.java            # Cron 表达式解析与调度
│   └── CronUtil.java                  # Cron 工具类
│
└── exception/
    └── OpenDateException.java         # 日期模块异常
```

---

## 3. 核心 API

### 3.1 OpenDate - 核心日期工具

`OpenDate` 是日期模块的主入口，提供当前时间获取、创建、解析、格式化、截断、加减、边界获取、判断、计算、转换等全方位操作。

```java
public final class OpenDate {

    // ==================== 当前时间 ====================
    public static LocalDate today();
    public static LocalDate today(ZoneId zone);
    public static LocalDateTime now();
    public static LocalDateTime now(ZoneId zone);
    public static long currentTimeMillis();
    public static long currentTimeSeconds();
    public static Instant instant();

    // ==================== 创建 ====================
    public static LocalDate of(int year, int month, int dayOfMonth);
    public static LocalDateTime of(int year, int month, int dayOfMonth,
                                   int hour, int minute, int second);
    public static LocalDateTime ofEpochMilli(long epochMilli);
    public static LocalDateTime ofEpochSecond(long epochSecond);
    public static LocalDateTime from(Date date);
    public static LocalDateTime from(Calendar calendar);

    // ==================== 智能解析 ====================
    public static LocalDateTime parse(String text);
    public static LocalDate parseDate(String text);
    public static LocalTime parseTime(String text);
    public static LocalDateTime parse(String text, String pattern);

    // ==================== 格式化 ====================
    public static String format(Temporal temporal);              // yyyy-MM-dd HH:mm:ss
    public static String format(Temporal temporal, String pattern);
    public static String formatIso(Temporal temporal);
    public static String formatChinese(Temporal temporal);       // yyyy年MM月dd日 HH时mm分ss秒

    // ==================== 截断 ====================
    public static <T extends Temporal> T truncate(T temporal, TemporalUnit unit);
    public static LocalDateTime truncateToDay(LocalDateTime dateTime);
    public static LocalDateTime truncateToHour(LocalDateTime dateTime);
    public static LocalDateTime truncateToMinute(LocalDateTime dateTime);

    // ==================== 加减操作 ====================
    public static LocalDateTime plusYears(LocalDateTime dateTime, long years);
    public static LocalDateTime plusMonths(LocalDateTime dateTime, long months);
    public static LocalDateTime plusWeeks(LocalDateTime dateTime, long weeks);
    public static LocalDateTime plusDays(LocalDateTime dateTime, long days);
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours);
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes);
    public static LocalDateTime plusSeconds(LocalDateTime dateTime, long seconds);

    // ==================== 边界获取 ====================
    public static LocalDate startOfMonth(LocalDate date);
    public static LocalDate endOfMonth(LocalDate date);
    public static LocalDate startOfYear(LocalDate date);
    public static LocalDate endOfYear(LocalDate date);
    public static LocalDate startOfWeek(LocalDate date);
    public static LocalDate endOfWeek(LocalDate date);
    public static LocalDate startOfQuarter(LocalDate date);
    public static LocalDate endOfQuarter(LocalDate date);

    // ==================== 判断操作 ====================
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end);
    public static boolean isBetween(LocalDateTime dateTime,
                                    LocalDateTime start, LocalDateTime end);
    public static boolean isToday(LocalDate date);
    public static boolean isYesterday(LocalDate date);
    public static boolean isTomorrow(LocalDate date);
    public static boolean isWeekend(LocalDate date);
    public static boolean isLeapYear(int year);

    // ==================== 计算 ====================
    public static long daysBetween(LocalDate start, LocalDate end);
    public static long monthsBetween(LocalDate start, LocalDate end);
    public static long yearsBetween(LocalDate start, LocalDate end);
    public static Period periodBetween(LocalDate start, LocalDate end);
    public static Duration durationBetween(LocalDateTime start, LocalDateTime end);

    // ==================== 转换 ====================
    public static long toEpochMilli(LocalDateTime dateTime);
    public static long toEpochSecond(LocalDateTime dateTime);
    public static Date toDate(LocalDateTime dateTime);
    public static Date toDate(LocalDate date);

    // ==================== 扩展类型快捷方法 ====================
    public static LocalDateRange range(LocalDate start, LocalDate end);
    public static YearQuarter currentQuarter();
    public static YearWeek currentWeek();
}
```

**使用示例：**

```java
// 获取当前时间
LocalDate today = OpenDate.today();
LocalDateTime now = OpenDate.now();

// 创建日期
LocalDate date = OpenDate.of(2024, 1, 15);
LocalDateTime dateTime = OpenDate.of(2024, 1, 15, 14, 30, 0);

// 智能解析（自动识别格式）
LocalDateTime dt1 = OpenDate.parse("2024-01-15 14:30:45");
LocalDateTime dt2 = OpenDate.parse("20240115143045");

// 格式化
String formatted = OpenDate.format(now);                    // "2024-01-15 14:30:45"
String chinese = OpenDate.formatChinese(now);               // "2024年01月15日 14时30分45秒"

// 截断与加减
LocalDateTime truncated = OpenDate.truncateToDay(now);      // 当天 00:00:00
LocalDateTime future = OpenDate.plusDays(now, 7);            // 7天后

// 边界获取
LocalDate monthStart = OpenDate.startOfMonth(today);        // 月初
LocalDate monthEnd = OpenDate.endOfMonth(today);            // 月末

// 判断
boolean weekend = OpenDate.isWeekend(today);
boolean between = OpenDate.isBetween(today, OpenDate.of(2024, 1, 1),
                                     OpenDate.of(2024, 12, 31));

// 计算间隔
long days = OpenDate.daysBetween(OpenDate.of(2024, 1, 1),
                                 OpenDate.of(2024, 12, 31));
```

### 3.2 DateTimes - 日期时间静态工厂

`DateTimes` 提供语义化的日期时间创建方法，涵盖日期、时间、YearMonth、Year、Quarter、YearQuarter、YearWeek、Duration、Period、范围等类型。

```java
public final class DateTimes {

    // ==================== 日期创建 ====================
    public static LocalDate date(int year, int month, int dayOfMonth);
    public static LocalDate date(int year, Month month, int dayOfMonth);
    public static LocalDate today();
    public static LocalDate yesterday();
    public static LocalDate tomorrow();

    // ==================== 时间创建 ====================
    public static LocalTime time(int hour, int minute);
    public static LocalTime time(int hour, int minute, int second);
    public static LocalTime midnight();
    public static LocalTime noon();

    // ==================== 日期时间创建 ====================
    public static LocalDateTime dateTime(int year, int month, int dayOfMonth,
                                         int hour, int minute);
    public static LocalDateTime dateTime(int year, int month, int dayOfMonth,
                                         int hour, int minute, int second);
    public static LocalDateTime dateTime(LocalDate date, LocalTime time);
    public static LocalDateTime now();

    // ==================== 年月/年/季度/周 ====================
    public static YearMonth yearMonth(int year, int month);
    public static Year year(int year);
    public static YearMonth currentYearMonth();
    public static Year currentYear();
    public static Quarter quarter(int quarter);
    public static YearQuarter yearQuarter(int year, int quarter);
    public static YearQuarter yearQuarter(int year, Quarter quarter);
    public static YearQuarter currentQuarter();
    public static YearWeek yearWeek(int weekBasedYear, int week);
    public static YearWeek currentWeek();

    // ==================== Instant ====================
    public static Instant instant();
    public static Instant instant(long epochMilli);
    public static Instant instant(long epochSecond, long nanoAdjust);

    // ==================== Duration ====================
    public static Duration nanos(long nanos);
    public static Duration millis(long millis);
    public static Duration seconds(long seconds);
    public static Duration minutes(long minutes);
    public static Duration hours(long hours);
    public static Duration days(long days);

    // ==================== Period ====================
    public static Period periodDays(int days);
    public static Period weeks(int weeks);
    public static Period months(int months);
    public static Period years(int years);
    public static Period period(int years, int months, int days);

    // ==================== 范围/区间 ====================
    public static LocalDateRange dateRange(LocalDate start, LocalDate end);
    public static LocalDateTimeRange dateTimeRange(LocalDateTime start, LocalDateTime end);
    public static Interval interval(Instant start, Instant end);
    public static Interval interval(Instant start, Duration duration);

    // ==================== PeriodDuration ====================
    public static PeriodDuration periodDuration(Period period, Duration duration);
    public static PeriodDuration between(LocalDateTime start, LocalDateTime end);
}
```

**使用示例：**

```java
// 语义化创建
LocalDate date = DateTimes.date(2024, 1, 15);
LocalTime time = DateTimes.time(14, 30, 0);
LocalDateTime dateTime = DateTimes.dateTime(2024, 1, 15, 14, 30, 0);

// 扩展类型
YearQuarter yq = DateTimes.yearQuarter(2024, 1);   // 2024-Q1
YearWeek yw = DateTimes.yearWeek(2024, 1);          // 2024-W01

// Duration/Period 快捷创建
Duration d1 = DateTimes.hours(2);
Duration d2 = DateTimes.minutes(30);
Period p = DateTimes.months(3);

// 范围创建
LocalDateRange range = DateTimes.dateRange(
    DateTimes.date(2024, 1, 1),
    DateTimes.date(2024, 12, 31)
);
```

### 3.3 TemporalUtil - 时间工具类

提供 Instant/LocalDateTime 互转、边界获取、比较、时间差计算等通用操作。

```java
public final class TemporalUtil {

    // ==================== 类型转换 ====================
    public static LocalDateTime toLocalDateTime(Instant instant, ZoneId zone);
    public static LocalDateTime toLocalDateTime(Instant instant);         // 系统时区
    public static Instant toInstant(LocalDateTime dateTime, ZoneId zone);
    public static Instant toInstant(LocalDateTime dateTime);             // 系统时区
    public static LocalDateTime toLocalDateTime(LocalDate date);
    public static LocalDateTime fromEpochMilli(long epochMilli);
    public static LocalDateTime fromEpochSecond(long epochSecond);
    public static long toEpochMilli(LocalDateTime dateTime);
    public static long toEpochSecond(LocalDateTime dateTime);

    // ==================== 范围判断 ====================
    public static boolean isBetween(LocalDate date, LocalDate start, LocalDate end);
    public static boolean isBetween(LocalDateTime dateTime,
                                    LocalDateTime start, LocalDateTime end);

    // ==================== 最大/最小 ====================
    public static LocalDate max(LocalDate a, LocalDate b);
    public static LocalDate min(LocalDate a, LocalDate b);
    public static LocalDateTime max(LocalDateTime a, LocalDateTime b);
    public static LocalDateTime min(LocalDateTime a, LocalDateTime b);

    // ==================== 边界获取 ====================
    public static LocalDateTime startOfDay(LocalDateTime dateTime);
    public static LocalDateTime endOfDay(LocalDateTime dateTime);
    public static LocalDateTime startOfMonth(LocalDateTime dateTime);
    public static LocalDateTime endOfMonth(LocalDateTime dateTime);
    public static LocalDateTime startOfYear(LocalDateTime dateTime);
    public static LocalDateTime endOfYear(LocalDateTime dateTime);
    public static LocalDateTime startOfWeek(LocalDateTime dateTime);
    public static LocalDateTime endOfWeek(LocalDateTime dateTime);

    // ==================== 时间差 ====================
    public static long daysBetween(LocalDate start, LocalDate end);
    public static long monthsBetween(LocalDate start, LocalDate end);
    public static long yearsBetween(LocalDate start, LocalDate end);
    public static long hoursBetween(LocalDateTime start, LocalDateTime end);
    public static long minutesBetween(LocalDateTime start, LocalDateTime end);

    // ==================== 判断 ====================
    public static boolean isWeekend(LocalDate date);
    public static boolean isWeekday(LocalDate date);
    public static boolean isLeapYear(int year);
    public static boolean isLeapYear(LocalDate date);
    public static int daysInMonth(int year, int month);
    public static int daysInMonth(YearMonth yearMonth);
    public static int getQuarter(int month);
    public static int getQuarter(LocalDate date);
}
```

### 3.4 StopWatch - 高精度性能计时器

支持多任务分段计时、静态快捷计时、格式化输出。

```java
public class StopWatch {

    // ==================== 构造 ====================
    public StopWatch();
    public StopWatch(String name);
    public static StopWatch createStarted();
    public static StopWatch createStarted(String name);

    // ==================== 静态计时 ====================
    public static Duration time(Runnable runnable);
    public static Duration time(String taskName, Runnable runnable);

    // ==================== 控制 ====================
    public void start(String taskName);
    public Duration stop();
    public void reset();
    public Duration split(String newTaskName);

    // ==================== 查询 ====================
    public boolean isRunning();
    public String getName();
    public int getTaskCount();
    public List<TaskInfo> getTasks();
    public long getTotalTimeNanos();
    public long getTotalTimeMillis();
    public double getTotalTimeSeconds();
    public Duration getTotalDuration();
    public long getCurrentTimeNanos();

    // ==================== 输出 ====================
    public String formatTime();
    public String prettyPrint();
    public String shortSummary();

    // ==================== 任务信息 ====================
    public record TaskInfo(String name, long timeNanos) {
        public long timeMillis();
        public double timeSeconds();
        public Duration duration();
    }
}
```

**使用示例：**

```java
// 快捷计时
Duration elapsed = StopWatch.time(() -> {
    // 需要计时的代码
});

// 分段计时
StopWatch sw = new StopWatch("My Process");
sw.start("step1");
// ... step1 逻辑
sw.split("step2");
// ... step2 逻辑
Duration total = sw.stop();
System.out.println(sw.prettyPrint());
```

---

## 4. 时间调整器

### 4.1 TemporalAdjusters - 预定义调整器

扩展 JDK 内置 TemporalAdjusters，增加季度、半年、工作日等业务调整器。

```java
public final class TemporalAdjusters {

    // ==================== JDK 标准 ====================
    public static TemporalAdjuster firstDayOfMonth();
    public static TemporalAdjuster lastDayOfMonth();
    public static TemporalAdjuster firstDayOfNextMonth();
    public static TemporalAdjuster firstDayOfYear();
    public static TemporalAdjuster lastDayOfYear();
    public static TemporalAdjuster firstDayOfNextYear();
    public static TemporalAdjuster next(DayOfWeek dayOfWeek);
    public static TemporalAdjuster nextOrSame(DayOfWeek dayOfWeek);
    public static TemporalAdjuster previous(DayOfWeek dayOfWeek);
    public static TemporalAdjuster previousOrSame(DayOfWeek dayOfWeek);
    public static TemporalAdjuster dayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek);

    // ==================== 季度/半年 ====================
    public static TemporalAdjuster firstDayOfQuarter();
    public static TemporalAdjuster lastDayOfQuarter();
    public static TemporalAdjuster firstDayOfNextQuarter();
    public static TemporalAdjuster firstDayOfHalf();
    public static TemporalAdjuster lastDayOfHalf();

    // ==================== 周 ====================
    public static TemporalAdjuster firstDayOfWeek();
    public static TemporalAdjuster firstDayOfWeek(DayOfWeek firstDayOfWeek);
    public static TemporalAdjuster lastDayOfWeek();
    public static TemporalAdjuster lastDayOfWeek(DayOfWeek lastDayOfWeek);

    // ==================== 工作日 ====================
    public static TemporalAdjuster nextWorkday();
    public static TemporalAdjuster previousWorkday();
    public static TemporalAdjuster nextOrSameWorkday();
    public static TemporalAdjuster plusWorkdays(int days);
    public static TemporalAdjuster minusWorkdays(int days);

    // ==================== 时间调整 ====================
    public static TemporalAdjuster startOfDay();
    public static TemporalAdjuster endOfDay();
    public static TemporalAdjuster noon();
    public static TemporalAdjuster atHour(int hour);

    // ==================== 组合 ====================
    public static TemporalAdjuster compose(TemporalAdjuster... adjusters);
    public static TemporalAdjuster andThen(TemporalAdjuster first, TemporalAdjuster second);
    public static TemporalAdjuster of(TemporalAdjuster adjuster);
}
```

**使用示例：**

```java
LocalDate date = LocalDate.of(2024, 3, 15);

// 季度操作
LocalDate quarterStart = date.with(TemporalAdjusters.firstDayOfQuarter()); // 2024-01-01
LocalDate quarterEnd = date.with(TemporalAdjusters.lastDayOfQuarter());    // 2024-03-31

// 工作日操作
LocalDate nextWorkday = date.with(TemporalAdjusters.nextWorkday());
LocalDate plus5Workdays = date.with(TemporalAdjusters.plusWorkdays(5));

// 时间调整
LocalDateTime dayStart = dateTime.with(TemporalAdjusters.startOfDay());  // 00:00:00
LocalDateTime dayEnd = dateTime.with(TemporalAdjusters.endOfDay());      // 23:59:59.999999999

// 组合调整器
TemporalAdjuster combined = TemporalAdjusters.compose(
    TemporalAdjusters.firstDayOfMonth(),
    TemporalAdjusters.startOfDay()
);
```

### 4.2 DateAdjusters - 扩展日期调整器

```java
public final class DateAdjusters {

    // 季度操作
    public static TemporalAdjuster startOfQuarter();
    public static TemporalAdjuster endOfQuarter();
    public static TemporalAdjuster startOfQuarter(int quarter);

    // 半年操作
    public static TemporalAdjuster startOfFirstHalf();
    public static TemporalAdjuster endOfFirstHalf();
    public static TemporalAdjuster startOfSecondHalf();
    public static TemporalAdjuster endOfSecondHalf();

    // 月份跳转
    public static TemporalAdjuster nextMonthDay(Month month, int day);
    public static TemporalAdjuster dayOfMonth(int day);
    public static TemporalAdjuster nthDayOfWeekInMonth(int ordinal, DayOfWeek dayOfWeek);

    // 工作日
    public static TemporalAdjuster plusBusinessDays(int days);
    public static TemporalAdjuster nearestWeekday();

    // 通用加减
    public static TemporalAdjuster plus(long amount, ChronoUnit unit);
    public static TemporalAdjuster minus(long amount, ChronoUnit unit);
}
```

### 4.3 BusinessDayAdjuster - 业务日调整器

支持自定义节假日、调休日、中东周末等复杂业务场景。

```java
public final class BusinessDayAdjuster implements TemporalAdjuster {

    // ==================== 快捷创建 ====================
    public static BusinessDayAdjuster plusDays(int days);
    public static BusinessDayAdjuster minusDays(int days);
    public static BusinessDayAdjuster nextBusinessDay();
    public static BusinessDayAdjuster previousBusinessDay();
    public static Builder builder();

    // ==================== 核心方法 ====================
    public Temporal adjustInto(Temporal temporal);
    public boolean isBusinessDay(LocalDate date);
    public boolean isHoliday(LocalDate date);
    public boolean isSpecialWorkday(LocalDate date);
    public long countBusinessDays(LocalDate start, LocalDate end);
    public LocalDate nextFrom(LocalDate date);
    public LocalDate previousFrom(LocalDate date);
    public LocalDate nthBusinessDayOfMonth(int year, int month, int n);

    // ==================== Builder ====================
    public static class Builder {
        public Builder days(int days);
        public Builder weekendDays(Set<DayOfWeek> weekendDays);
        public Builder middleEastWeekend();       // 周五周六为周末
        public Builder holidays(Predicate<LocalDate> holidayPredicate);
        public Builder holidays(Set<LocalDate> holidays);
        public Builder specialWorkdays(Predicate<LocalDate> specialWorkdayPredicate);
        public Builder specialWorkdays(Set<LocalDate> specialWorkdays);
        public BusinessDayAdjuster build();
    }
}
```

**使用示例：**

```java
Set<LocalDate> holidays = Set.of(
    LocalDate.of(2024, 10, 1),  // 国庆节
    LocalDate.of(2024, 10, 2)
);
Set<LocalDate> makeupDays = Set.of(
    LocalDate.of(2024, 10, 12)  // 调休上班
);

BusinessDayAdjuster adjuster = BusinessDayAdjuster.builder()
    .days(5)
    .holidays(holidays)
    .specialWorkdays(makeupDays)
    .build();

LocalDate result = LocalDate.of(2024, 9, 30).with(adjuster);
long workdays = adjuster.countBusinessDays(
    LocalDate.of(2024, 10, 1),
    LocalDate.of(2024, 10, 31)
);
```

### 4.4 WorkdayAdjuster - 工作日调整器

```java
public final class WorkdayAdjuster implements TemporalAdjuster {

    public static WorkdayAdjuster plusDays(int days);
    public static WorkdayAdjuster plusDays(int days, Set<LocalDate> holidays);
    public static WorkdayAdjuster plusDays(int days, Predicate<LocalDate> holidayPredicate);
    public static WorkdayAdjuster minusDays(int days);
    public static WorkdayAdjuster nextWorkday();
    public static WorkdayAdjuster nextWorkday(Set<LocalDate> holidays);
    public static WorkdayAdjuster previousWorkday();
    public static TemporalAdjuster nearestWorkday();

    public Temporal adjustInto(Temporal temporal);
    public boolean isWorkday(LocalDate date);
    public long countWorkdays(LocalDate start, LocalDate end);

    public static Builder builder();

    public static class Builder {
        public Builder days(int days);
        public Builder weekendDays(Set<DayOfWeek> weekendDays);
        public Builder middleEastWeekend();
        public Builder holidays(Predicate<LocalDate> holidayPredicate);
        public Builder holidays(Set<LocalDate> holidays);
        public WorkdayAdjuster build();
    }
}
```

---

## 5. 扩展类型

### 5.1 YearQuarter - 年季度

```java
public final class YearQuarter implements Temporal, TemporalAdjuster,
        Comparable<YearQuarter>, Serializable {

    // 静态工厂
    public static YearQuarter of(int year, int quarter);
    public static YearQuarter of(int year, Quarter quarter);
    public static YearQuarter from(TemporalAccessor temporal);
    public static YearQuarter now();
    public static YearQuarter now(ZoneId zone);
    public static YearQuarter now(Clock clock);
    public static YearQuarter parse(CharSequence text);          // "2024-Q1"
    public static YearQuarter parse(CharSequence text, DateTimeFormatter formatter);

    // 获取
    public int getYear();
    public int getQuarterValue();
    public Quarter getQuarter();
    public boolean isLeapYear();
    public int lengthOfQuarter();
    public boolean isFirstQuarter();
    public boolean isLastQuarter();

    // 计算
    public YearQuarter plusYears(long years);
    public YearQuarter plusQuarters(long quarters);
    public YearQuarter minusYears(long years);
    public YearQuarter minusQuarters(long quarters);

    // 转换
    public LocalDate atStartOfQuarter();
    public LocalDate atEndOfQuarter();
    public LocalDate atDay(int dayOfQuarter);
    public YearMonth atMonth(int monthOfQuarter);

    // 比较
    public boolean isBefore(YearQuarter other);
    public boolean isAfter(YearQuarter other);

    // 格式化
    public String format();         // "2024-Q1"
    public String format(DateTimeFormatter formatter);
}
```

### 5.2 Quarter - 季度枚举

```java
public enum Quarter implements TemporalAccessor, TemporalQuery<Quarter> {
    Q1, Q2, Q3, Q4;

    public static Quarter of(int quarter);
    public static Quarter ofMonth(int month);
    public static Quarter from(Month month);
    public static Quarter from(TemporalAccessor temporal);

    public int getValue();
    public int firstMonth();
    public int lastMonth();
    public Month firstMonthOfQuarter();
    public Month lastMonthOfQuarter();
    public int length(boolean leapYear);
    public Quarter next();
    public Quarter previous();
    public Quarter plus(int quarters);
    public Quarter minus(int quarters);
    public boolean contains(int month);
    public boolean contains(Month month);
}
```

### 5.3 YearWeek - 年周

```java
public final class YearWeek implements Temporal, TemporalAdjuster,
        Comparable<YearWeek>, Serializable {

    public static YearWeek of(int weekBasedYear, int week);
    public static YearWeek from(TemporalAccessor temporal);
    public static YearWeek now();
    public static YearWeek parse(CharSequence text);   // "2024-W01"

    public int getYear();
    public int getWeek();
    public int lengthOfYear();
    public boolean isFirstWeek();
    public boolean isLastWeek();

    public YearWeek plusYears(long years);
    public YearWeek plusWeeks(long weeks);
    public YearWeek minusYears(long years);
    public YearWeek minusWeeks(long weeks);

    public LocalDate atDay(DayOfWeek dayOfWeek);
    public LocalDate atMonday();
    public LocalDate atTuesday();
    public LocalDate atWednesday();
    public LocalDate atThursday();
    public LocalDate atFriday();
    public LocalDate atSaturday();
    public LocalDate atSunday();

    public boolean isBefore(YearWeek other);
    public boolean isAfter(YearWeek other);
    public String format();         // "2024-W01"
}
```

### 5.4 YearHalf - 年半期

```java
public final class YearHalf implements Temporal, TemporalAdjuster,
        Comparable<YearHalf>, Serializable {

    public enum Half {
        H1, H2;
        public int getValue();
        public Month firstMonth();
        public Month lastMonth();
        public static Half of(int value);
        public static Half ofMonth(Month month);
        public static Half ofMonth(int month);
    }

    public static YearHalf of(int year, Half half);
    public static YearHalf of(int year, int half);
    public static YearHalf now();
    public static YearHalf from(TemporalAccessor temporal);
    public static YearHalf parse(CharSequence text);   // "2024-H1"

    public int getYear();
    public Half getHalf();
    public int getHalfValue();

    public LocalDate atStart();
    public LocalDate atEnd();
    public LocalDate atDay(int month, int dayOfMonth);

    public YearHalf plusHalves(long halves);
    public YearHalf minusHalves(long halves);
    public YearHalf plusYears(long years);
    public YearHalf minusYears(long years);

    public int lengthInDays();
    public int lengthInMonths();
}
```

**使用示例：**

```java
YearHalf yh = YearHalf.of(2024, YearHalf.Half.H1);
YearHalf next = yh.plusHalves(1);  // 2024-H2
LocalDate start = yh.atStart();    // 2024-01-01
LocalDate end = yh.atEnd();        // 2024-06-30
```

### 5.5 Interval - 时间区间

基于 `Instant` 的时间区间，支持包含、重叠、交集、并集、间隙等集合运算。

```java
public final class Interval implements Serializable {

    // 创建
    public static Interval of(Instant start, Instant end);
    public static Interval of(Instant start, Duration duration);
    public static Interval of(Duration duration, Instant end);
    public static Interval parse(CharSequence text);
    public static Interval empty(Instant instant);

    // 获取
    public Instant getStart();
    public Instant getEnd();
    public Duration toDuration();
    public boolean isEmpty();

    // 关系判断
    public boolean contains(Instant instant);
    public boolean encloses(Interval other);
    public boolean overlaps(Interval other);
    public boolean abuts(Interval other);
    public boolean isBefore(Interval other);
    public boolean isBefore(Instant instant);
    public boolean isAfter(Interval other);
    public boolean isAfter(Instant instant);

    // 集合运算
    public Optional<Interval> intersection(Interval other);
    public Interval union(Interval other);
    public Optional<Interval> gap(Interval other);

    // 修改
    public Interval withStart(Instant start);
    public Interval withEnd(Instant end);
    public Interval expand(Duration duration);

    // 转换
    public LocalDateRange toLocalDateRange(ZoneId zone);
    public LocalDateTimeRange toLocalDateTimeRange(ZoneId zone);
}
```

### 5.6 LocalDateRange - 日期范围

支持迭代、流式处理、按周/月分割。

```java
public final class LocalDateRange implements Iterable<LocalDate>, Serializable {

    public static LocalDateRange of(LocalDate start, LocalDate end);
    public static LocalDateRange ofExclusive(LocalDate start, LocalDate endExclusive);
    public static LocalDateRange empty();
    public static LocalDateRange parse(CharSequence text);

    public LocalDate getStart();
    public LocalDate getEnd();
    public long lengthInDays();
    public boolean isEmpty();

    public boolean contains(LocalDate date);
    public boolean encloses(LocalDateRange other);
    public boolean overlaps(LocalDateRange other);
    public boolean isConnected(LocalDateRange other);

    public Optional<LocalDateRange> intersection(LocalDateRange other);
    public LocalDateRange span(LocalDateRange other);

    public Iterator<LocalDate> iterator();
    public Stream<LocalDate> stream();
    public Stream<LocalDate> stream(Period step);    // 按步长迭代
    public List<LocalDate> toList();

    public List<LocalDateRange> splitByWeek();
    public List<LocalDateRange> splitByMonth();
}
```

**使用示例：**

```java
LocalDateRange range = LocalDateRange.of(
    LocalDate.of(2024, 1, 1),
    LocalDate.of(2024, 1, 31)
);

// 流式遍历
range.stream().forEach(date -> System.out.println(date));

// 每隔3天遍历
range.stream(Period.ofDays(3)).forEach(date -> System.out.println(date));

// 按周分割
List<LocalDateRange> weeks = range.splitByWeek();

// 集合运算
LocalDateRange other = LocalDateRange.of(
    LocalDate.of(2024, 1, 15),
    LocalDate.of(2024, 2, 15)
);
Optional<LocalDateRange> intersection = range.intersection(other);
```

### 5.7 LocalDateTimeRange - 日期时间范围

```java
public final class LocalDateTimeRange implements Serializable {

    public static LocalDateTimeRange of(LocalDateTime start, LocalDateTime end);
    public static LocalDateTimeRange of(LocalDateTime start, Duration duration);
    public static LocalDateTimeRange ofDay(LocalDate date);
    public static LocalDateTimeRange empty(LocalDateTime dateTime);
    public static LocalDateTimeRange parse(CharSequence text);

    public LocalDateTime getStart();
    public LocalDateTime getEnd();
    public Duration toDuration();
    public long toHours();
    public long toMinutes();
    public long toSeconds();
    public boolean isEmpty();

    public boolean contains(LocalDateTime dateTime);
    public boolean encloses(LocalDateTimeRange other);
    public boolean overlaps(LocalDateTimeRange other);
    public boolean abuts(LocalDateTimeRange other);
    public boolean isBefore(LocalDateTimeRange other);
    public boolean isAfter(LocalDateTimeRange other);

    public Optional<LocalDateTimeRange> intersection(LocalDateTimeRange other);
    public LocalDateTimeRange union(LocalDateTimeRange other);
    public Optional<LocalDateTimeRange> gap(LocalDateTimeRange other);

    public LocalDateTimeRange withStart(LocalDateTime start);
    public LocalDateTimeRange withEnd(LocalDateTime end);
    public LocalDateRange toLocalDateRange();
    public Interval toInterval(ZoneId zone);
}
```

### 5.8 LocalTimeRange - 时间范围

支持跨午夜的时间范围（如 22:00-06:00 夜班）。

```java
public final class LocalTimeRange implements Serializable {

    // 预定义常量
    public static final LocalTimeRange ALL_DAY;
    public static final LocalTimeRange BUSINESS_HOURS;  // 09:00-17:00
    public static final LocalTimeRange MORNING;          // 06:00-12:00
    public static final LocalTimeRange AFTERNOON;        // 12:00-18:00
    public static final LocalTimeRange EVENING;          // 18:00-22:00

    public static LocalTimeRange of(LocalTime start, LocalTime end);
    public static LocalTimeRange ofHours(int startHour, int endHour);
    public static LocalTimeRange ofDuration(LocalTime start, Duration duration);
    public static LocalTimeRange parse(String text);

    public LocalTime getStart();
    public LocalTime getEnd();
    public boolean crossesMidnight();
    public Duration getDuration();
    public long getHours();
    public long getMinutes();

    public boolean contains(LocalTime time);
    public boolean contains(LocalTimeRange other);
    public boolean overlaps(LocalTimeRange other);
    public LocalTimeRange intersection(LocalTimeRange other);

    public LocalTimeRange withStart(LocalTime newStart);
    public LocalTimeRange withEnd(LocalTime newEnd);
    public LocalTimeRange shift(Duration duration);
    public LocalTimeRange expand(Duration duration);

    public String format(DateTimeFormatter formatter);
    public String formatCompact();
}
```

### 5.9 时间单位类型

提供 `Seconds`、`Minutes`、`Hours`、`Days`、`Weeks`、`Months`、`Years` 七种时间单位类型，均实现 `TemporalAmount` 和 `Comparable`，支持算术运算和互相转换。

```java
// Days 示例（其他类型 API 结构相同）
public final class Days implements TemporalAmount, Comparable<Days>, Serializable {

    public static final Days ZERO;
    public static final Days ONE;

    public static Days of(int days);
    public static Days ofWeeks(int weeks);
    public static Days from(Period period);
    public static Days between(Temporal startInclusive, Temporal endExclusive);

    public int getAmount();
    public boolean isZero();
    public boolean isNegative();
    public boolean isPositive();

    public Days plus(Days other);
    public Days plus(int daysToAdd);
    public Days minus(Days other);
    public Days minus(int daysToSubtract);
    public Days multipliedBy(int multiplicand);
    public Days dividedBy(int divisor);
    public Days negated();
    public Days abs();

    public Period toPeriod();
    public Duration toDuration();
    public Hours toHours();
    public Weeks toWeeks();
}
```

**使用示例：**

```java
Days d = Days.of(7);
Days d2 = Days.ofWeeks(2);           // 14 days
LocalDate date = LocalDate.now().plus(d);

Hours h = Hours.of(48);
Days days = h.toDays();              // 2 days

Months m = Months.ofYears(2);        // 24 months
Years y = m.toYears();               // 2 years
```

### 5.10 PeriodDuration - 周期+时长组合

同时包含日期部分（Period）和时间部分（Duration）。

```java
public final class PeriodDuration implements TemporalAmount, Serializable {

    public static final PeriodDuration ZERO;

    public static PeriodDuration of(Period period, Duration duration);
    public static PeriodDuration ofPeriod(Period period);
    public static PeriodDuration ofDuration(Duration duration);
    public static PeriodDuration parse(CharSequence text);   // "P1Y2M3DT4H5M6S"
    public static PeriodDuration between(LocalDateTime start, LocalDateTime end);

    public Period getPeriod();
    public Duration getDuration();
    public boolean isZero();
    public boolean isNegative();

    public PeriodDuration plus(PeriodDuration other);
    public PeriodDuration minus(PeriodDuration other);
    public PeriodDuration negated();
    public PeriodDuration multipliedBy(int scalar);
    public PeriodDuration normalized();
}
```

### 5.11 AmPm / DayOfMonth / DayOfYear

```java
// 上午/下午
public enum AmPm implements TemporalAccessor, TemporalQuery<AmPm> {
    AM, PM;
    public static AmPm of(int value);
    public static AmPm ofHour(int hourOfDay);
    public static AmPm from(TemporalAccessor temporal);
    public static AmPm now();
    public boolean isAm();
    public boolean isPm();
    public int firstHour();
    public int lastHour();
    public AmPm opposite();
    public String getShortName();
    public String getChineseName();
}

// 月中天 (1-31)
public final class DayOfMonth implements TemporalAccessor, TemporalAdjuster,
        Comparable<DayOfMonth> {
    public static DayOfMonth of(int dayOfMonth);
    public static DayOfMonth first();
    public static DayOfMonth from(TemporalAccessor temporal);
    public static DayOfMonth now();
    public int getValue();
    public boolean isValidFor(YearMonth yearMonth);
    public boolean isValidFor(Month month, boolean leapYear);
    public boolean isFirst();
    public boolean isPossibleLastDay();
    public LocalDate atYearMonth(YearMonth yearMonth);
    public LocalDate atYearMonth(int year, int month);
}

// 年中天 (1-366)
public final class DayOfYear implements TemporalAccessor, TemporalAdjuster,
        Comparable<DayOfYear> {
    public static DayOfYear of(int dayOfYear);
    public static DayOfYear first();
    public static DayOfYear lastOf(int year);
    public static DayOfYear from(TemporalAccessor temporal);
    public static DayOfYear now();
    public int getValue();
    public boolean isValidFor(int year);
    public boolean isFirst();
    public boolean isLeapDay();
    public LocalDate atYear(int year);
}
```

---

## 6. 时间间隔计算

### 6.1 DateBetween - 日期间隔

```java
public final class DateBetween {

    public static DateBetween of(LocalDate start, LocalDate end);
    public static DateBetween of(LocalDateTime start, LocalDateTime end);
    public static DateBetween between(Temporal start, Temporal end);

    // 返回带符号的间隔
    public long days();
    public long weeks();
    public long months();
    public long years();
    public long hours();
    public long minutes();
    public long seconds();
    public long millis();

    // 返回绝对值
    public long absDays();
    public long absWeeks();
    public long absMonths();
    public long absYears();

    public Period toPeriod();
    public Duration toDuration();
    public DateDiff toDateDiff();

    public Temporal getStart();
    public Temporal getEnd();
}
```

### 6.2 DateDiff - 详细日期差异

将日期差异分解为年、月、日、时、分、秒各部分。

```java
public final class DateDiff {

    public static DateDiff of(LocalDate start, LocalDate end);
    public static DateDiff of(LocalDateTime start, LocalDateTime end);
    public static DateDiff of(Temporal start, Temporal end);

    public int getYears();
    public int getMonths();
    public int getDays();
    public int getHours();
    public int getMinutes();
    public int getSeconds();
    public boolean isNegative();

    public long toTotalDays();
    public Period toPeriod();

    public String format();          // "33 years, 10 months, 5 days"
    public String formatChinese();   // "33年10个月5天"
}
```

### 6.3 AgeBetween - 年龄计算

```java
public final class AgeBetween {

    public static AgeBetween fromBirth(LocalDate birthDate);
    public static AgeBetween fromBirth(LocalDateTime birthDateTime);
    public static AgeBetween at(LocalDate birthDate, LocalDate referenceDate);
    public static int ageInYears(LocalDate birthDate);

    public int getYears();
    public int getMonths();
    public int getDays();
    public long getTotalMonths();
    public long getTotalDays();
    public long getTotalWeeks();

    public boolean isBirthdayToday();
    public LocalDate getNextBirthday();
    public long getDaysUntilNextBirthday();
    public LocalDate getLastBirthday();

    public String getZodiacSign();          // "Aquarius"
    public String getZodiacSignChinese();   // "水瓶座"
    public String getChineseZodiac();       // "Dragon"
    public String getChineseZodiacChinese(); // "龙"

    public AgeDetail toDetail();
    public Period getPeriod();
    public String format();           // "34 years, 2 months, 5 days"
    public String formatChinese();    // "34岁2个月5天"
}
```

### 6.4 AgeDetail - 详细年龄统计

```java
public final class AgeDetail {

    public static AgeDetail of(AgeBetween ageBetween);
    public static AgeDetail of(LocalDate birthDate);
    public static AgeDetail of(LocalDate birthDate, LocalDate referenceDate);

    // 统计数据
    public long getTotalDays();
    public long getTotalWeeks();
    public long getTotalMonths();
    public long getTotalHours();
    public long getTotalMinutes();
    public long getTotalSeconds();
    public long getTotalWeekends();
    public long getEstimatedWeekends();
    public int getLeapYearsLived();
    public int getBirthdaysCelebrated();

    // 里程碑
    public int getNextMilestone();
    public LocalDate getNextMilestoneDate();
    public long getDaysUntilNextMilestone();
    public int getLastMilestone();

    // 出生信息
    public String getBirthSeason();
    public String getBirthSeasonChinese();

    // 生命统计
    public double getLifePercentage(int lifeExpectancy);
    public int getEstimatedRemainingYears(int lifeExpectancy);

    public String toSummary();
}
```

**使用示例：**

```java
LocalDate birth = LocalDate.of(1990, 5, 15);
AgeBetween age = AgeBetween.fromBirth(birth);
System.out.println(age.getYears());             // 34
System.out.println(age.getZodiacSignChinese()); // "金牛座"
System.out.println(age.getChineseZodiacChinese()); // "马"
System.out.println(age.formatChinese());        // "34岁8个月12天"

AgeDetail detail = age.toDetail();
System.out.println(detail.getTotalDays());       // 12000+
System.out.println(detail.getLeapYearsLived());  // 9
System.out.println(detail.getNextMilestone());   // 40
```

---

## 7. 格式化与解析

### 7.1 DateFormatter - 预定义格式化器

提供丰富的预定义格式化器常量和带缓存的自定义格式化器。

```java
public final class DateFormatter {

    // ====== 标准格式 ======
    public static final DateTimeFormatter NORM_DATE;         // yyyy-MM-dd
    public static final DateTimeFormatter NORM_TIME;         // HH:mm:ss
    public static final DateTimeFormatter NORM_DATETIME;     // yyyy-MM-dd HH:mm:ss
    public static final DateTimeFormatter NORM_DATETIME_MS;  // yyyy-MM-dd HH:mm:ss.SSS
    public static final DateTimeFormatter NORM_TIME_MS;      // HH:mm:ss.SSS

    // ====== 紧凑格式 ======
    public static final DateTimeFormatter PURE_DATE;         // yyyyMMdd
    public static final DateTimeFormatter PURE_TIME;         // HHmmss
    public static final DateTimeFormatter PURE_DATETIME;     // yyyyMMddHHmmss
    public static final DateTimeFormatter PURE_DATETIME_MS;  // yyyyMMddHHmmssSSS

    // ====== 中文格式 ======
    public static final DateTimeFormatter CHINESE_DATE;      // yyyy年MM月dd日
    public static final DateTimeFormatter CHINESE_TIME;      // HH时mm分ss秒
    public static final DateTimeFormatter CHINESE_DATETIME;  // yyyy年MM月dd日 HH时mm分ss秒
    public static final DateTimeFormatter CHINESE_DATE_SHORT; // yyyy年M月d日

    // ====== ISO 格式 ======
    public static final DateTimeFormatter ISO_DATE;
    public static final DateTimeFormatter ISO_TIME;
    public static final DateTimeFormatter ISO_DATETIME;
    public static final DateTimeFormatter ISO_OFFSET_DATETIME;
    public static final DateTimeFormatter ISO_ZONED_DATETIME;

    // ====== 其他 ======
    public static final DateTimeFormatter HTTP_DATE;         // RFC 1123
    public static final DateTimeFormatter NORM_MONTH;        // yyyy-MM
    public static final DateTimeFormatter NORM_YEAR;         // yyyy
    public static final DateTimeFormatter CHINESE_MONTH;     // yyyy年MM月
    public static final DateTimeFormatter FLEXIBLE_DATETIME; // 灵活格式

    // 缓存方法
    public static DateTimeFormatter ofPattern(String pattern);
    public static DateTimeFormatter ofPattern(String pattern, Locale locale);

    // 快捷格式化
    public static String format(TemporalAccessor temporal, String pattern);
    public static String formatDate(LocalDate date);
    public static String formatTime(LocalTime time);
    public static String formatDateTime(LocalDateTime dateTime);
    public static String formatIso(LocalDateTime dateTime);
    public static String formatChinese(LocalDate date);
    public static String formatChinese(LocalDateTime dateTime);

    // 缓存管理
    public static int cacheSize();
    public static void clearCache();
}
```

### 7.2 DateParser - 智能日期解析器

自动识别 20+ 种常见日期格式，无需指定格式字符串。

```java
public final class DateParser {

    // 智能解析（自动识别格式）
    public static LocalDateTime parseDateTime(String text);
    public static LocalDate parseDate(String text);
    public static LocalTime parseTime(String text);

    // 指定格式解析
    public static LocalDateTime parse(String text, String pattern);
    public static LocalDate parseDate(String text, String pattern);
    public static LocalTime parseTime(String text, String pattern);

    // 时间戳解析
    public static LocalDateTime fromEpochMilli(long epochMilli);
    public static LocalDateTime fromEpochMilli(long epochMilli, ZoneId zone);
    public static LocalDateTime fromEpochSecond(long epochSecond);
    public static LocalDateTime fromEpochSecond(long epochSecond, ZoneId zone);

    // 安全解析（返回 null 而非抛异常）
    public static LocalDateTime tryParseDateTime(String text);
    public static LocalDate tryParseDate(String text);
    public static LocalTime tryParseTime(String text);

    // 验证
    public static boolean isValidDateTime(String text);
    public static boolean isValidDate(String text);
    public static boolean isValidTime(String text);
}
```

**使用示例：**

```java
// 自动识别多种格式
LocalDateTime dt1 = DateParser.parseDateTime("2024-01-15 14:30:45");
LocalDateTime dt2 = DateParser.parseDateTime("2024/01/15 14:30:45");
LocalDateTime dt3 = DateParser.parseDateTime("20240115143045");
LocalDateTime dt4 = DateParser.parseDateTime("2024年01月15日 14时30分45秒");

// 安全解析
LocalDate date = DateParser.tryParseDate("invalid");  // 返回 null

// 验证
boolean valid = DateParser.isValidDate("2024-02-29"); // true (闰年)
```

### 7.3 RelativeTimeFormatter - 相对时间格式化

```java
public final class RelativeTimeFormatter {

    // 英文格式
    public static String format(Temporal temporal);
    public static String format(Temporal temporal, Temporal reference);
    public static String format(Instant instant);
    public static String formatDuration(Duration duration);

    // 中文格式
    public static String formatChinese(Temporal temporal);
    public static String formatChinese(Temporal temporal, Temporal reference);
    public static String formatChinese(Instant instant);
    public static String formatDurationChinese(Duration duration);

    // 智能格式（今天显示时间，昨天显示"昨天"，更早显示日期）
    public static String formatSmart(LocalDateTime dateTime);
    public static String formatSmartChinese(LocalDateTime dateTime);

    // 紧凑格式
    public static String formatCompact(Temporal temporal);
}
```

**使用示例：**

```java
LocalDateTime time = LocalDateTime.now().minusMinutes(5);
String relative = RelativeTimeFormatter.format(time);         // "5 minutes ago"
String relativeCn = RelativeTimeFormatter.formatChinese(time); // "5分钟前"

LocalDateTime future = LocalDateTime.now().plusDays(2);
String inFuture = RelativeTimeFormatter.format(future);       // "in 2 days"
```

### 7.4 PeriodFormatter - Period/Duration 格式化

```java
public final class PeriodFormatter {

    // Period 格式化
    public static String format(Period period);         // "1 year 2 months 15 days"
    public static String formatChinese(Period period);  // "1年2个月15天"
    public static String formatShort(Period period);    // "1y 2m 15d"
    public static String formatCompact(Period period);  // "P1Y2M15D"

    // Duration 格式化
    public static String format(Duration duration);        // "1 day 1 hour 30 minutes"
    public static String formatChinese(Duration duration); // "1天1小时30分钟"
    public static String formatShort(Duration duration);   // "1d 1h 30m"
    public static String formatTime(Duration duration);    // "25:30:00"
    public static String formatCompact(Duration duration); // "PT25H30M"

    // 解析
    public static Period parsePeriod(String text);         // "2 years 3 months"
    public static Duration parseDuration(String text);     // "5h 30m"

    // 安全解析
    public static Period tryParsePeriod(String text);
    public static Duration tryParseDuration(String text);

    // 通用
    public static String format(TemporalAmount amount);
}
```

---

## 8. 节假日与农历

### 8.1 HolidayCalendar - 节假日日历

```java
public final class HolidayCalendar {

    public static HolidayCalendar empty();
    public static HolidayCalendar of(Collection<Holiday> holidays);
    public static Builder builder();

    // 判断
    public boolean isHoliday(LocalDate date);
    public boolean isPublicHoliday(LocalDate date);
    public boolean isSpecialWorkday(LocalDate date);
    public boolean isWeekend(LocalDate date);
    public boolean isWorkday(LocalDate date);

    // 查询
    public Holiday getHoliday(LocalDate date);
    public Optional<Holiday> findHoliday(LocalDate date);
    public List<Holiday> getAllHolidays();
    public List<Holiday> getHolidays(int year);
    public List<Holiday> getHolidays(int year, Month month);
    public List<Holiday> getHolidaysByType(Holiday.HolidayType type);
    public List<Holiday> getHolidaysInRange(LocalDate start, LocalDate end);

    // 工作日计算
    public LocalDate nextWorkday(LocalDate date);
    public LocalDate previousWorkday(LocalDate date);
    public LocalDate addWorkdays(LocalDate date, int days);
    public long countWorkdays(LocalDate start, LocalDate end);

    // 转换
    public Predicate<LocalDate> asHolidayPredicate();
    public Predicate<LocalDate> asWorkdayPredicate();

    public static class Builder {
        public Builder name(String name);
        public Builder addHoliday(Holiday holiday);
        public Builder addHolidays(Collection<Holiday> holidays);
        public Builder addSpecialWorkday(LocalDate date);
        public Builder addSpecialWorkdays(Collection<LocalDate> dates);
        public Builder weekendDays(Set<DayOfWeek> weekendDays);
        public HolidayCalendar build();
    }
}
```

### 8.2 Holiday - 节假日模型

```java
public final class Holiday implements Comparable<Holiday>, Serializable {

    public enum HolidayType {
        PUBLIC, BANK, RELIGIOUS, COMMEMORATION, CUSTOM
    }

    public static Holiday of(LocalDate date, String name);
    public static Holiday of(LocalDate date, String name, HolidayType type);
    public static Holiday of(LocalDate date, String name, String chineseName, HolidayType type);
    public static Builder builder();

    public LocalDate getDate();
    public String getName();
    public String getChineseName();
    public String getLocalizedName(boolean preferChinese);
    public HolidayType getType();
    public LocalDate getObservedDate();
    public boolean isDayOff();
    public String getDescription();
    public boolean isOn(LocalDate date);
    public boolean isPublicHoliday();
    public int getYear();
}
```

### 8.3 HolidayUtil - 节假日工具

```java
public final class HolidayUtil {

    public static void setDefaultProvider(HolidayProvider provider);
    public static HolidayProvider getDefaultProvider();
    public static void registerProvider(HolidayProvider provider);
    public static Optional<HolidayProvider> getProvider(String countryCode);

    public static boolean isHoliday(LocalDate date);
    public static boolean isHoliday(LocalDate date, HolidayProvider provider);
    public static Optional<Holiday> getHoliday(LocalDate date);
    public static boolean isWorkday(LocalDate date);
    public static boolean isWorkday(LocalDate date, HolidayProvider provider);
    public static boolean isWeekend(LocalDate date);
    public static boolean isAdjustedWorkday(LocalDate date);

    public static LocalDate plusWorkdays(LocalDate date, int workdays);
    public static LocalDate minusWorkdays(LocalDate date, int workdays);
    public static long workdaysBetween(LocalDate start, LocalDate end);
    public static LocalDate nextWorkday(LocalDate date);
    public static LocalDate previousWorkday(LocalDate date);
    public static LocalDate nextOrSameWorkday(LocalDate date);

    public static List<Holiday> getHolidays(int year);
    public static List<Holiday> getHolidays(LocalDate start, LocalDate end);
}
```

### 8.4 Lunar - 农历

```java
public final class Lunar implements Comparable<Lunar>, Serializable {

    public static Lunar of(int year, int month, int day, boolean leapMonth);
    public static Lunar of(int year, int month, int day);

    public int getYear();
    public int getMonth();
    public int getDay();
    public boolean isLeapMonth();

    public String getZodiac();            // "龙"
    public String getHeavenlyStem();      // "甲"
    public String getEarthlyBranch();     // "辰"
    public String getStemBranchYear();    // "甲辰"
    public String getChineseYear();       // "二〇二四"
    public String getChineseMonth();      // "正月"
    public String getChineseDay();        // "初一"
    public String toChinese();            // "二〇二四年正月初一"
    public String toStemBranch();         // "甲辰年..."
}
```

### 8.5 LunarUtil - 农历工具

```java
public final class LunarUtil {

    public static Lunar toLunar(LocalDate solarDate);
    public static LocalDate toSolar(Lunar lunar);
    public static int getLeapMonth(int year);
    public static int getLunarYearDays(int year);
    public static int getLunarMonthDays(int year, int month);
    public static int getLeapMonthDays(int year);
    public static String getZodiac(int year);
    public static String getZodiac(LocalDate date);
    public static String getStemBranchYear(int year);
    public static boolean hasLeapMonth(int year);
    public static Lunar today();
}
```

### 8.6 SolarTerm - 二十四节气

```java
public enum SolarTerm {
    LICHUN, YUSHUI, JINGZHE, CHUNFEN, QINGMING, GUYU,
    LIXIA, XIAOMAN, MANGZHONG, XIAZHI, XIAOSHU, DASHU,
    LIQIU, CHUSHU, BAILU, QIUFEN, HANLU, SHUANGJIANG,
    LIDONG, XIAOXUE, DAXUE, DONGZHI, XIAOHAN, DAHAN;

    public static SolarTerm of(int index);
    public static SolarTerm fromChineseName(String chineseName);

    public int getIndex();
    public String getChineseName();       // "立春"
    public String getEnglishName();       // "Start of Spring"
    public double getSunLongitude();

    public SolarTerm next();
    public SolarTerm previous();
    public boolean isMajorTerm();         // 中气
    public boolean isMinorTerm();         // 节气
    public int getSeason();
    public String getSeasonName();
}
```

**使用示例：**

```java
// 公历转农历
Lunar lunar = LunarUtil.toLunar(LocalDate.of(2024, 2, 10));  // 春节
System.out.println(lunar.toChinese());    // "二〇二四年正月初一"
System.out.println(lunar.getZodiac());    // "龙"

// 农历转公历
LocalDate solar = LunarUtil.toSolar(Lunar.of(2024, 1, 1));

// 生肖
String zodiac = LunarUtil.getZodiac(2024);  // "龙"
```

---

## 9. 时区

### 9.1 TimezoneConverter - 链式时区转换器

```java
public final class TimezoneConverter {

    // 预定义时区常量
    public static final ZoneId UTC;
    public static final ZoneId CHINA;        // Asia/Shanghai
    public static final ZoneId BEIJING;
    public static final ZoneId HONG_KONG;
    public static final ZoneId TOKYO;
    public static final ZoneId SEOUL;
    public static final ZoneId SINGAPORE;
    public static final ZoneId NEW_YORK;
    public static final ZoneId LOS_ANGELES;
    public static final ZoneId CHICAGO;
    public static final ZoneId LONDON;
    public static final ZoneId PARIS;
    public static final ZoneId BERLIN;
    public static final ZoneId SYDNEY;

    // 链式 API
    public static TimezoneConverter from(ZoneId sourceZone);
    public static TimezoneConverter fromUTC();
    public static TimezoneConverter fromChina();
    public static TimezoneConverter fromSystem();

    public TimezoneConverter to(ZoneId targetZone);
    public TimezoneConverter toUTC();
    public TimezoneConverter toChina();
    public TimezoneConverter toSystem();

    // 转换
    public ZonedDateTime convert(LocalDateTime dateTime);
    public ZonedDateTime convert(Instant instant);
    public ZonedDateTime convert(ZonedDateTime zonedDateTime);
    public ZonedDateTime convert(long epochMilli);
    public LocalDateTime convertToLocal(LocalDateTime dateTime);
    public OffsetDateTime convertToOffset(LocalDateTime dateTime);

    // 静态便捷方法
    public static ZonedDateTime convert(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone);
    public static ZonedDateTime fromUTC(LocalDateTime utcDateTime, ZoneId toZone);
    public static ZonedDateTime toUTC(LocalDateTime dateTime, ZoneId fromZone);
    public static ZonedDateTime now(ZoneId zone);
    public static double getOffsetHours(ZoneId zone1, ZoneId zone2);
    public static String format(Instant instant, ZoneId zone, DateTimeFormatter formatter);
}
```

**使用示例：**

```java
// 链式转换
ZonedDateTime nyTime = TimezoneConverter.from(ZoneId.of("Asia/Shanghai"))
    .to(ZoneId.of("America/New_York"))
    .convert(LocalDateTime.now());

// 快捷转换
ZonedDateTime result = TimezoneConverter.fromUTC()
    .toChina()
    .convert(Instant.now());
```

### 9.2 TimezoneUtil - 时区工具

```java
public final class TimezoneUtil {

    // 预定义常量
    public static final ZoneId UTC;
    public static final ZoneId CHINA;
    public static final ZoneId JAPAN;
    // ... 更多

    // 当前时间
    public static ZonedDateTime nowUtc();
    public static ZonedDateTime now(ZoneId zone);
    public static ZonedDateTime nowLocal();

    // 转换
    public static ZonedDateTime convert(ZonedDateTime dateTime, ZoneId zone);
    public static ZonedDateTime toZoned(LocalDateTime dateTime, ZoneId zone);
    public static LocalDateTime toLocal(ZonedDateTime dateTime);
    public static ZonedDateTime toZoned(Instant instant, ZoneId zone);
    public static Instant toInstant(ZonedDateTime dateTime);
    public static LocalDateTime convert(LocalDateTime dateTime, ZoneId fromZone, ZoneId toZone);

    // 时区信息
    public static Duration getOffset(ZoneId zone);
    public static Duration getOffsetBetween(ZoneId from, ZoneId to);
    public static int getOffsetHours(ZoneId zone);
    public static String formatOffset(ZoneId zone);
    public static Set<String> getAllTimezoneIds();
    public static List<ZoneId> getAllTimezones();
    public static List<String> findTimezones(String pattern);
    public static String getDisplayName(ZoneId zone, Locale locale);
    public static boolean isValidTimezone(String zoneId);
    public static ZoneId getDefault();

    // 夏令时
    public static boolean isDaylightSavingTime(ZoneId zone);
    public static boolean usesDaylightSavingTime(ZoneId zone);
    public static ZonedDateTime getNextDstTransition(ZoneId zone);
}
```

---

## 10. Cron 表达式

### 10.1 CronExpression

```java
public final class CronExpression implements Serializable {

    // 预定义常量
    public static final String EVERY_MINUTE;   // "* * * * *"
    public static final String EVERY_HOUR;     // "0 * * * *"
    public static final String DAILY;          // "0 0 * * *"
    public static final String WEEKLY;         // "0 0 * * 1"
    public static final String MONTHLY;        // "0 0 1 * *"
    public static final String YEARLY;         // "0 0 1 1 *"

    // 创建
    public static CronExpression parse(String expression);
    public static CronExpression daily(int hour, int minute);
    public static CronExpression everyMinutes(int minutes);
    public static CronExpression everyHours(int hours);

    // 调度计算
    public LocalDateTime nextExecution();
    public LocalDateTime nextExecution(LocalDateTime after);
    public LocalDateTime previousExecution();
    public LocalDateTime previousExecution(LocalDateTime before);
    public List<LocalDateTime> nextExecutions(int count);
    public List<LocalDateTime> nextExecutions(LocalDateTime after, int count);
    public boolean matches(LocalDateTime dateTime);

    // 字段查询
    public String getExpression();
    public Set<Integer> getMinutes();
    public Set<Integer> getHours();
    public Set<Integer> getDaysOfMonth();
    public Set<Integer> getMonths();
    public Set<Integer> getDaysOfWeek();

    // 描述
    public String describe();
}
```

### 10.2 CronUtil

```java
public final class CronUtil {

    // 预定义常量
    public static final String EVERY_SECOND;       // "* * * * * ?"
    public static final String EVERY_MINUTE;        // "0 * * * * ?"
    public static final String EVERY_HOUR;          // "0 0 * * * ?"
    public static final String DAILY_MIDNIGHT;      // "0 0 0 * * ?"
    public static final String DAILY_NOON;          // "0 0 12 * * ?"
    public static final String WEEKLY_MONDAY;       // "0 0 0 ? * MON"
    public static final String MONTHLY_FIRST;       // "0 0 0 1 * ?"
    public static final String WEEKDAYS_9AM;        // "0 0 9 ? * MON-FRI"
    public static final String EVERY_5_MINUTES;     // "0 0/5 * * * ?"
    public static final String EVERY_15_MINUTES;    // "0 0/15 * * * ?"
    public static final String EVERY_30_MINUTES;    // "0 0/30 * * * ?"

    // 验证
    public static boolean isValid(String expression);
    public static void validate(String expression);

    // 调度计算
    public static Optional<LocalDateTime> getNextExecutionTime(String expression);
    public static Optional<LocalDateTime> getNextExecutionTime(String expression, LocalDateTime after);
    public static List<LocalDateTime> getNextExecutionTimes(String expression, int count);
    public static List<LocalDateTime> getNextExecutionTimes(String expression, LocalDateTime after, int count);
    public static Optional<ZonedDateTime> getNextExecutionTime(String expression, ZoneId zone);
    public static Optional<ZonedDateTime> getNextExecutionTime(String expression, ZonedDateTime after);

    // 描述
    public static String describe(String expression);
    public static String describeInChinese(String expression);

    // 快捷创建
    public static String dailyAt(int hour, int minute);
    public static String weekdaysAt(int hour, int minute);
    public static String everyMinutes(int minutes);
    public static String everyHours(int hours);
}
```

**使用示例：**

```java
// 解析与调度
CronExpression cron = CronExpression.parse("30 8 * * *");  // 每天 8:30
LocalDateTime next = cron.nextExecution();
List<LocalDateTime> nextFive = cron.nextExecutions(5);

// 匹配检查
boolean matches = cron.matches(LocalDateTime.of(2024, 1, 15, 8, 30));

// 描述
String desc = CronUtil.describeInChinese("0 0 9 ? * MON-FRI");
// "工作日上午9点"

// 快捷创建
String expr = CronUtil.dailyAt(8, 30);  // "0 30 8 * * ?"
```

---

## 11. 异常处理

```java
public class OpenDateException extends RuntimeException {

    public OpenDateException(String message);
    public OpenDateException(String message, Throwable cause);
    public OpenDateException(String message, String inputValue, String expectedFormat);
    public OpenDateException(String message, String inputValue, String expectedFormat, Throwable cause);

    // 工厂方法
    public static OpenDateException parseError(String input, String pattern);
    public static OpenDateException parseError(String input, String pattern, Throwable cause);
    public static OpenDateException parseError(String input);
    public static OpenDateException formatError(String message);
    public static OpenDateException formatError(String message, Throwable cause);
    public static OpenDateException invalidValue(String field, Object value, String range);
    public static OpenDateException timezoneError(String zoneId);
    public static OpenDateException rangeError(String message);
    public static OpenDateException cronError(String expression, String reason);
    public static OpenDateException cronError(String expression, String reason, Throwable cause);

    // 诊断信息
    public String getInputValue();
    public String getExpectedFormat();
    public boolean hasInputValue();
    public boolean hasExpectedFormat();
}
```

---

*文档更新日期：2026-02-27*

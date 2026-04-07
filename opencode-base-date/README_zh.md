# OpenCode Base Date

面向 JDK 25+ 的日期时间工具库，提供全面的一站式日期时间操作方案。

## 功能特性

- 当前时间访问（LocalDate、LocalDateTime、Instant、毫秒/秒时间戳）
- 从组件、时间戳、Date、Calendar 创建日期时间
- 智能格式检测解析（ISO、中文、紧凑格式等）
- 灵活的格式化（默认、ISO、中文、自定义模式）
- 按时间单位截断和取整
- 加减操作（年、月、周、日、时、分、秒）
- 边界计算（日、周、月、季度、年的开始/结束）
- 范围比较和日期检查（isToday、isWeekend、isBetween、isLeapYear）
- 日期之间的时长和周期计算
- 毫秒时间戳、秒时间戳、java.util.Date 互相转换
- 日期范围：LocalDateRange、LocalDateTimeRange、LocalTimeRange
- 扩展时间类型：Quarter、YearQuarter、YearWeek、YearHalf、PeriodDuration
- 类型安全单位：Days、Hours、Minutes、Seconds、Weeks、Months、Years
- 额外类型：AmPm、DayOfMonth、DayOfYear、Interval
- 日期谓词：isFuture、isPast、isSameDay/Month/Year/Week、isFirstDayOfMonth、isLastDayOfMonth、isMonday~isSunday
- 日期流：天、周、月、小时、周末、工作日的惰性流，支持自定义步长
- 最近日期查找：从集合中查找最接近的日期（之前/之后/总体最近）
- 日期舍入：舍入/向上取整/向下取整到最近的小时、分钟、N分钟间隔或任意 Duration
- 工作日计算：计算工作日数、加减工作日，支持假期配置
- 日期调整器：BusinessDayAdjuster、WorkdayAdjuster、自定义 TemporalAdjusters
- 日期差值：DateBetween、DateDiff、AgeBetween、AgeDetail
- 格式化器：DateFormatter、DateParser、PeriodFormatter、RelativeTimeFormatter
- 节假日支持：Holiday、HolidayCalendar、HolidayProvider、HolidayUtil
- Cron：CronExpression、CronUtil
- 时区：TimezoneConverter、TimezoneUtil
- StopWatch 计时工具
- TemporalUtil 通用时间操作
- 线程安全，空值安全，仅依赖 opencode-base-core

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-date</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenDate` | 门面类——日期时间操作的主入口 |
| `DateTimes` | 附加日期时间工具方法 |
| `DatePredicates` | 便捷布尔谓词（isFuture、isPast、isSameDay、isWeekend 等） |
| `DateStreams` | 日期时间惰性流（天、周、月、小时、周末、工作日） |
| `ClosestDate` | 从集合中查找最接近的日期（之前/之后/总体最近） |
| `DateRounding` | 日期时间舍入/向上取整/向下取整到小时、分钟或任意 Duration |
| `StopWatch` | 简单计时器，用于测量经过时间 |
| `TemporalUtil` | 通用时间工具操作 |
| **调整器** | |
| `BusinessDayAdjuster` | 将日期调整到下一个/上一个工作日 |
| `WorkdayAdjuster` | 考虑工作日安排的日期调整 |
| `DateAdjusters` | 常用日期调整器集合 |
| `TemporalAdjusters` | 自定义时间调整器 |
| **差值/间隔** | |
| `AgeBetween` | 计算两个日期之间的年龄 |
| `AgeDetail` | 详细年龄分解（年、月、日） |
| `BusinessDays` | 工作日差值和偏移计算，支持假期配置 |
| `DateBetween` | 计算两个日期之间的差值 |
| `DateDiff` | 多单位的日期差值结果 |
| **Cron** | |
| `CronExpression` | Cron 表达式解析和下次执行计算 |
| `CronUtil` | Cron 工具方法 |
| **扩展时间类型** | |
| `AmPm` | 上午/下午表示 |
| `DayOfMonth` | 月中日值类型 |
| `DayOfYear` | 年中日值类型 |
| `Days` | 类型安全的天数 |
| `Hours` | 类型安全的小时数 |
| `Minutes` | 类型安全的分钟数 |
| `Seconds` | 类型安全的秒数 |
| `Weeks` | 类型安全的周数 |
| `Months` | 类型安全的月数 |
| `Years` | 类型安全的年数 |
| `Interval` | 时间间隔表示 |
| `LocalDateRange` | 可迭代的日期范围（起止） |
| `LocalDateTimeRange` | 日期时间范围 |
| `LocalTimeRange` | 时间范围 |
| `PeriodDuration` | 组合的 Period + Duration |
| `Quarter` | 季度（Q1-Q4） |
| `YearQuarter` | 年份 + 季度组合 |
| `YearWeek` | ISO 年-周 |
| `YearHalf` | 年度半期（H1/H2） |
| **格式化器** | |
| `DateFormatter` | 日期格式化，支持预定义和自定义模式 |
| `DateParser` | 智能日期解析，自动检测格式 |
| `PeriodFormatter` | 人类可读的周期格式化 |
| `RelativeTimeFormatter` | 相对时间格式化（"3天前"、"2小时后"） |
| **节假日** | |
| `Holiday` | 节假日定义 |
| `HolidayCalendar` | 带查询的节假日日历 |
| `HolidayProvider` | 节假日数据提供者 SPI |
| `HolidayUtil` | 节假日工具方法 |
| **范围** | |
| `DateRange` | 日期范围操作 |
| `DateTimeRange` | 日期时间范围操作 |
| `TimeRange` | 时间范围操作 |
| **时区** | |
| `TimezoneConverter` | 时区转换工具 |
| `TimezoneUtil` | 时区信息工具 |
| **异常** | |
| `OpenDateException` | 日期模块异常（继承自 OpenException） |

## 快速开始

```java
import cloud.opencode.base.date.*;
import cloud.opencode.base.date.between.*;
import java.time.*;
import java.util.*;

// 当前时间
LocalDate today = OpenDate.today();
LocalDateTime now = OpenDate.now();

// 创建日期
LocalDate date = OpenDate.of(2024, 1, 15);
LocalDateTime dateTime = OpenDate.of(2024, 1, 15, 14, 30, 0);

// 智能解析
LocalDateTime dt = OpenDate.parse("2024-01-15 14:30:45");

// 格式化
String formatted = OpenDate.format(now);               // "2024-01-15 14:30:45"
String chinese = OpenDate.formatChinese(now);           // "2024年01月15日 14时30分45秒"

// 边界
LocalDate monthStart = OpenDate.startOfMonth(today);
LocalDate monthEnd = OpenDate.endOfMonth(today);

// 时长
long days = OpenDate.daysBetween(date1, date2);

// 谓词判断
boolean future = DatePredicates.isFuture(LocalDate.of(2030, 1, 1));
boolean weekend = DatePredicates.isWeekend(today);
boolean sameMonth = DatePredicates.isSameMonth(date1, date2);

// 日期流
DateStreams.days(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1))
    .filter(DatePredicates::isWeekday)
    .forEach(System.out::println);

// 最近日期
Optional<LocalDate> nearest = ClosestDate.closestTo(today, deadlines);

// 日期舍入
LocalDateTime rounded = DateRounding.roundToNearestHour(now);
LocalDateTime quarter = DateRounding.roundToNearestQuarterHour(now);

// 工作日
long bizDays = BusinessDays.between(start, end);
LocalDate result = BusinessDays.addBusinessDays(today, 5, holidays);
```

## V1.0.3 新增

### DatePredicates（日期谓词）

| 方法 | 说明 |
|------|------|
| `isFuture(LocalDate)` / `isFuture(LocalDateTime)` | 判断日期/时间是否在未来 |
| `isPast(LocalDate)` / `isPast(LocalDateTime)` | ��断日期/时间是否在过去 |
| `isSameDay(a, b)` | 判断两个日期/时间是否是同一天 |
| `isSameMonth(a, b)` | 判断两个日期是否在同一年月 |
| `isSameYear(a, b)` | 判断两个日期是否在同一年 |
| `isSameWeek(a, b)` | 判断两个日期是否在同一 ISO 周 |
| `isFirstDayOfMonth(date)` | 判断是否为月首 |
| `isLastDayOfMonth(date)` | 判断是否为月末 |
| `isMonday(date)` ... `isSunday(date)` | 判断具体星期几 |
| `isWeekend(date)` / `isWeekday(date)` | 判断是否为周末/工作日 |
| `isLeapYear(date)` | 判断是否为闰年 |
| `isBetween(date, start, end)` | 判断日期是否在范围内（两端包含） |

所有方法空值安全，null 输入返回 `false`。

### DateStreams（日期流）

| 方法 | 说明 |
|------|------|
| `days(start, end)` | 逐日流（起始包含，结束不包含） |
| `days(start, end, step)` | 自定义 Period 步长的日期流 |
| `weeks(start, end)` | 周一对齐的每周起始日流 |
| `months(start, end)` | YearMonth 逐月流（支持 YearMonth 或 LocalDate 参数） |
| `hours(start, end)` | 逐小时流 |
| `iterate(start, end, step)` | 自定义 Duration 步长的 LocalDateTime 流 |
| `weekends(start, end)` | 范围内的周六周日流 |
| `weekdays(start, end)` | 范围内的周一至周五流 |

所有流均为惰性流，按需生成。

### ClosestDate（最近日期查找）

| 方法 | 说明 |
|------|------|
| `closestTo(target, dates)` | 从集合中查找最接近目标的日期 |
| `closestBefore(target, dates)` | 查找严格在目标之前最接近的日期 |
| `closestAfter(target, dates)` | ��找严格在目标之后最接近��日期 |

每个方法均有 `LocalDate` 和 `LocalDateTime` 重载。集合为空时返回 `Optional.empty()`。

### DateRounding（日期舍入）

| 方法 | 说明 |
|------|------|
| `roundToNearestHour(dt)` | 舍入到最近整点（>= 30 分钟向上） |
| `ceilToHour(dt)` / `floorToHour(dt)` | 向上/向下取整到小时 |
| `roundToNearestMinute(dt)` | 舍入到最近整分钟（>= 30 秒向上） |
| `ceilToMinute(dt)` / `floorToMinute(dt)` | 向上/向下取整到分钟 |
| `roundToNearest(dt, minutes)` | 舍入到最近 N 分钟间隔 |
| `roundToNearestHalfHour(dt)` | 舍入到最近半小时 |
| `roundToNearestQuarterHour(dt)` | 舍入到最近刻钟（15分钟） |
| `roundToNearest(dt, duration)` | 舍入到任意 Duration 的最近倍数 |
| `ceilTo(dt, duration)` / `floorTo(dt, duration)` | 向上/向下取整到 Duration 边界 |

### BusinessDays（工作日计算）

| 方法 | 说明 |
|------|------|
| `between(start, end)` | 计算两个日期间的工作日数（周一至周五） |
| `between(start, end, holidays)` | 计算工作日数，排除假期 |
| `between(start, end, holidays, weekendDays)` | 自定义周末定义计算��作日数 |
| `addBusinessDays(date, days)` | 加 N 个工作日（负数为减） |
| `addBusinessDays(date, days, holidays)` | 加工作日，跳过假期 |
| `addBusinessDays(date, days, holidays, weekendDays)` | 自定义周末定义加工作日 |

## 环境要求

- Java 25+

## 开源���可

Apache License 2.0

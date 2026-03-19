# OpenCode Base Date

面向 JDK 25+ 的零依赖日期时间工具库，提供全面的一站式日期时间操作方案。

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
- 日期调整器：BusinessDayAdjuster、WorkdayAdjuster、自定义 TemporalAdjusters
- 日期差值：DateBetween、DateDiff、AgeBetween、AgeDetail
- 格式化器：DateFormatter、DateParser、PeriodFormatter、RelativeTimeFormatter
- 节假日支持：Holiday、HolidayCalendar、HolidayProvider、HolidayUtil
- 农历：Lunar、LunarUtil、SolarTerm
- Cron：CronExpression、CronUtil
- 时区：TimezoneConverter、TimezoneUtil
- StopWatch 计时工具
- TemporalUtil 通用时间操作
- 线程安全，空值安全，零外部依赖

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-date</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenDate` | 门面类——日期时间操作的主入口 |
| `DateTimes` | 附加日期时间工具方法 |
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
| **农历** | |
| `Lunar` | 农历日期表示 |
| `LunarUtil` | 农历/公历转换 |
| `SolarTerm` | 二十四节气 |
| **范围** | |
| `DateRange` | 日期范围操作 |
| `DateTimeRange` | 日期时间范围操作 |
| `TimeRange` | 时间范围操作 |
| **时区** | |
| `TimezoneConverter` | 时区转换工具 |
| `TimezoneUtil` | 时区信息工具 |
| **异常** | |
| `OpenDateException` | 日期模块运行时异常 |

## 快速开始

```java
import cloud.opencode.base.date.OpenDate;
import java.time.*;

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

// 比较
boolean weekend = OpenDate.isWeekend(today);
boolean between = OpenDate.isBetween(today, start, end);
```

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0

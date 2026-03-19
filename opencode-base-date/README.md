# OpenCode Base Date

Zero-dependency date and time utility library for JDK 25+ -- a comprehensive, one-stop solution for date/time operations.

## Features

- Current time access (LocalDate, LocalDateTime, Instant, epoch millis/seconds)
- Date/time creation from components, epoch values, Date, and Calendar
- Smart format-detection parsing (ISO, Chinese, compact, etc.)
- Flexible formatting (default, ISO, Chinese, custom patterns)
- Truncation and rounding by temporal unit
- Add/subtract operations (years, months, weeks, days, hours, minutes, seconds)
- Boundary calculations (start/end of day, week, month, quarter, year)
- Range comparisons and date checks (isToday, isWeekend, isBetween, isLeapYear)
- Duration and period calculation between dates
- Conversion to/from epoch millis, epoch seconds, java.util.Date
- Date ranges: LocalDateRange, LocalDateTimeRange, LocalTimeRange
- Extended temporals: Quarter, YearQuarter, YearWeek, YearHalf, PeriodDuration
- Type-safe units: Days, Hours, Minutes, Seconds, Weeks, Months, Years
- Extra types: AmPm, DayOfMonth, DayOfYear, Interval
- Date adjusters: BusinessDayAdjuster, WorkdayAdjuster, custom TemporalAdjusters
- Date difference: DateBetween, DateDiff, AgeBetween, AgeDetail
- Formatters: DateFormatter, DateParser, PeriodFormatter, RelativeTimeFormatter
- Holiday support: Holiday, HolidayCalendar, HolidayProvider, HolidayUtil
- Lunar calendar: Lunar, LunarUtil, SolarTerm
- Cron: CronExpression, CronUtil
- Timezone: TimezoneConverter, TimezoneUtil
- StopWatch for timing operations
- TemporalUtil for generic temporal operations
- Thread-safe, null-safe, zero external dependencies

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-date</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenDate` | Facade class -- main entry point for date/time operations |
| `DateTimes` | Additional date/time utility methods |
| `StopWatch` | Simple stopwatch for measuring elapsed time |
| `TemporalUtil` | Generic temporal utility operations |
| **Adjusters** | |
| `BusinessDayAdjuster` | Adjusts dates to the next/previous business day |
| `WorkdayAdjuster` | Adjusts dates considering workday schedules |
| `DateAdjusters` | Collection of common date adjusters |
| `TemporalAdjusters` | Custom temporal adjusters |
| **Between / Diff** | |
| `AgeBetween` | Calculates age between two dates |
| `AgeDetail` | Detailed age breakdown (years, months, days) |
| `DateBetween` | Calculates difference between two dates |
| `DateDiff` | Date difference result with multiple units |
| **Cron** | |
| `CronExpression` | Cron expression parsing and next-execution calculation |
| `CronUtil` | Cron utility methods |
| **Extra Temporals** | |
| `AmPm` | AM/PM representation |
| `DayOfMonth` | Day-of-month value type |
| `DayOfYear` | Day-of-year value type |
| `Days` | Type-safe day count |
| `Hours` | Type-safe hour count |
| `Minutes` | Type-safe minute count |
| `Seconds` | Type-safe second count |
| `Weeks` | Type-safe week count |
| `Months` | Type-safe month count |
| `Years` | Type-safe year count |
| `Interval` | Time interval representation |
| `LocalDateRange` | Iterable date range (start to end) |
| `LocalDateTimeRange` | DateTime range |
| `LocalTimeRange` | Time range |
| `PeriodDuration` | Combined Period + Duration |
| `Quarter` | Quarter-of-year (Q1-Q4) |
| `YearQuarter` | Year + Quarter combination |
| `YearWeek` | ISO year-week |
| `YearHalf` | Year half (H1/H2) |
| **Formatters** | |
| `DateFormatter` | Date formatting with predefined and custom patterns |
| `DateParser` | Smart date parsing with format auto-detection |
| `PeriodFormatter` | Human-readable period formatting |
| `RelativeTimeFormatter` | Relative time formatting ("3 days ago", "in 2 hours") |
| **Holiday** | |
| `Holiday` | Holiday definition |
| `HolidayCalendar` | Holiday calendar with lookup |
| `HolidayProvider` | SPI for holiday data providers |
| `HolidayUtil` | Holiday utility methods |
| **Lunar** | |
| `Lunar` | Lunar date representation |
| `LunarUtil` | Lunar/solar date conversion |
| `SolarTerm` | 24 solar terms |
| **Range** | |
| `DateRange` | Date range operations |
| `DateTimeRange` | DateTime range operations |
| `TimeRange` | Time range operations |
| **Timezone** | |
| `TimezoneConverter` | Timezone conversion utility |
| `TimezoneUtil` | Timezone information utility |
| **Exception** | |
| `OpenDateException` | Date module runtime exception |

## Quick Start

```java
import cloud.opencode.base.date.OpenDate;
import java.time.*;

// Current time
LocalDate today = OpenDate.today();
LocalDateTime now = OpenDate.now();

// Create dates
LocalDate date = OpenDate.of(2024, 1, 15);
LocalDateTime dateTime = OpenDate.of(2024, 1, 15, 14, 30, 0);

// Smart parsing
LocalDateTime dt = OpenDate.parse("2024-01-15 14:30:45");

// Formatting
String formatted = OpenDate.format(now);               // "2024-01-15 14:30:45"
String chinese = OpenDate.formatChinese(now);           // "2024年01月15日 14时30分45秒"

// Boundaries
LocalDate monthStart = OpenDate.startOfMonth(today);
LocalDate monthEnd = OpenDate.endOfMonth(today);

// Duration
long days = OpenDate.daysBetween(date1, date2);

// Comparisons
boolean weekend = OpenDate.isWeekend(today);
boolean between = OpenDate.isBetween(today, start, end);
```

## Requirements

- Java 25+

## License

Apache License 2.0

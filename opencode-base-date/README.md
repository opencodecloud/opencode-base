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
- Date predicates: isFuture, isPast, isSameDay/Month/Year/Week, isFirstDayOfMonth, isLastDayOfMonth, isMonday-isSunday
- Date streams: lazy streams of days, weeks, months, hours, weekends, weekdays with custom steps
- Closest date finder: find nearest date from a collection (before/after/overall)
- Date rounding: round/ceil/floor to nearest hour, minute, N-minute interval, or arbitrary Duration
- Business day calculations: count business days, add/subtract business days with holiday support
- Date adjusters: BusinessDayAdjuster, WorkdayAdjuster, custom TemporalAdjusters
- Date difference: DateBetween, DateDiff, AgeBetween, AgeDetail
- Formatters: DateFormatter, DateParser, PeriodFormatter, RelativeTimeFormatter
- Holiday support: Holiday, HolidayCalendar, HolidayProvider, HolidayUtil
- Cron: CronExpression, CronUtil
- Timezone: TimezoneConverter, TimezoneUtil
- StopWatch for timing operations
- TemporalUtil for generic temporal operations
- Thread-safe, null-safe, zero external dependencies (except opencode-base-core)

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-date</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenDate` | Facade class -- main entry point for date/time operations |
| `DateTimes` | Additional date/time utility methods |
| `DatePredicates` | Convenience boolean predicates (isFuture, isPast, isSameDay, isWeekend, etc.) |
| `DateStreams` | Lazy streams of dates/times (days, weeks, months, hours, weekends, weekdays) |
| `ClosestDate` | Find the closest date in a collection (before/after/overall) |
| `DateRounding` | Round/ceil/floor date-times to hour, minute, or arbitrary Duration |
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
| `BusinessDays` | Business day difference and offset calculations with holiday support |
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
| **Range** | |
| `DateRange` | Date range operations |
| `DateTimeRange` | DateTime range operations |
| `TimeRange` | Time range operations |
| **Timezone** | |
| `TimezoneConverter` | Timezone conversion utility |
| `TimezoneUtil` | Timezone information utility |
| **Exception** | |
| `OpenDateException` | Date module exception (extends OpenException) |

## Quick Start

```java
import cloud.opencode.base.date.*;
import cloud.opencode.base.date.between.*;
import java.time.*;
import java.util.*;

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

// Predicates
boolean future = DatePredicates.isFuture(LocalDate.of(2030, 1, 1));
boolean weekend = DatePredicates.isWeekend(today);
boolean sameMonth = DatePredicates.isSameMonth(date1, date2);

// Date Streams
DateStreams.days(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 2, 1))
    .filter(DatePredicates::isWeekday)
    .forEach(System.out::println);

// Closest date
Optional<LocalDate> nearest = ClosestDate.closestTo(today, deadlines);

// Date rounding
LocalDateTime rounded = DateRounding.roundToNearestHour(now);
LocalDateTime quarter = DateRounding.roundToNearestQuarterHour(now);

// Business days
long bizDays = BusinessDays.between(start, end);
LocalDate result = BusinessDays.addBusinessDays(today, 5, holidays);
```

## New in V1.0.3

### DatePredicates

| Method | Description |
|--------|-------------|
| `isFuture(LocalDate)` / `isFuture(LocalDateTime)` | Check if date/time is in the future |
| `isPast(LocalDate)` / `isPast(LocalDateTime)` | Check if date/time is in the past |
| `isSameDay(a, b)` | Check if two dates/date-times fall on the same day |
| `isSameMonth(a, b)` | Check if two dates are in the same year-month |
| `isSameYear(a, b)` | Check if two dates are in the same year |
| `isSameWeek(a, b)` | Check if two dates are in the same ISO week |
| `isFirstDayOfMonth(date)` | Check if date is the 1st of its month |
| `isLastDayOfMonth(date)` | Check if date is the last day of its month |
| `isMonday(date)` ... `isSunday(date)` | Check specific day of week |
| `isWeekend(date)` / `isWeekday(date)` | Check if Saturday/Sunday or Monday-Friday |
| `isLeapYear(date)` | Check if the year is a leap year |
| `isBetween(date, start, end)` | Check if date is within range (inclusive) |

All methods are null-safe and return `false` for null inputs.

### DateStreams

| Method | Description |
|--------|-------------|
| `days(start, end)` | Stream of consecutive days (start inclusive, end exclusive) |
| `days(start, end, step)` | Stream of days with custom Period step |
| `weeks(start, end)` | Stream of Monday-aligned week start dates |
| `months(start, end)` | Stream of YearMonth (from YearMonth or LocalDate) |
| `hours(start, end)` | Stream of consecutive hours |
| `iterate(start, end, step)` | Stream of LocalDateTime with custom Duration step |
| `weekends(start, end)` | Stream of Saturdays and Sundays in range |
| `weekdays(start, end)` | Stream of Monday-Friday dates in range |

All streams are lazy -- elements are generated on demand.

### ClosestDate

| Method | Description |
|--------|-------------|
| `closestTo(target, dates)` | Find the closest date to target from a collection |
| `closestBefore(target, dates)` | Find the closest date strictly before target |
| `closestAfter(target, dates)` | Find the closest date strictly after target |

Each method has both `LocalDate` and `LocalDateTime` overloads. Returns `Optional.empty()` if collection is null or empty.

### DateRounding

| Method | Description |
|--------|-------------|
| `roundToNearestHour(dt)` | Round to nearest hour (>= 30min rounds up) |
| `ceilToHour(dt)` / `floorToHour(dt)` | Ceil/floor to hour boundary |
| `roundToNearestMinute(dt)` | Round to nearest minute (>= 30s rounds up) |
| `ceilToMinute(dt)` / `floorToMinute(dt)` | Ceil/floor to minute boundary |
| `roundToNearest(dt, minutes)` | Round to nearest N-minute interval |
| `roundToNearestHalfHour(dt)` | Round to nearest 30-minute boundary |
| `roundToNearestQuarterHour(dt)` | Round to nearest 15-minute boundary |
| `roundToNearest(dt, duration)` | Round to nearest multiple of any Duration |
| `ceilTo(dt, duration)` / `floorTo(dt, duration)` | Ceil/floor to Duration boundary |

### BusinessDays

| Method | Description |
|--------|-------------|
| `between(start, end)` | Count business days (Mon-Fri) between dates |
| `between(start, end, holidays)` | Count business days excluding holidays |
| `between(start, end, holidays, weekendDays)` | Count business days with custom weekend |
| `addBusinessDays(date, days)` | Add N business days (negative to subtract) |
| `addBusinessDays(date, days, holidays)` | Add business days skipping holidays |
| `addBusinessDays(date, days, holidays, weekendDays)` | Add business days with custom weekend |

## Requirements

- Java 25+

## License

Apache License 2.0

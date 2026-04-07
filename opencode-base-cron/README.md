# OpenCode Base Cron

Full-featured cron expression library for JDK 25+. Zero dependencies, thread-safe, immutable.

## Features

- Parse 5-field and 6-field (with seconds) cron expressions
- Macro support: `@yearly`, `@monthly`, `@weekly`, `@daily`, `@hourly`
- Name aliases: `MON-FRI`, `JAN-DEC` (case-insensitive)
- Special characters: `L` (last), `W` (weekday), `#` (nth occurrence), range wrap-around
- Forward and reverse scheduling (next/previous execution times)
- Lazy `Stream<ZonedDateTime>` for forward and reverse iteration
- Filtered scheduling with `Predicate` (holiday exclusion, weekend skip, etc.)
- Schedule overlap detection between two cron expressions
- `TemporalAdjuster` integration (`zonedDateTime.with(cronExpr)`)
- Configurable search window (default 4 years, max 100)
- Execution count and listing between two time points
- Duration convenience methods (time to next / time from last)
- Expression equivalence detection (structural comparison)
- One-stop debugging with `explain()` (description + next 5 executions + interval)
- Human-readable descriptions in English and Chinese
- Fluent builder API with input validation
- Minimum interval validation
- Thread-safe, null-safe, zero dependencies

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cron</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Cron Expression Syntax

### Field Format

**5-field** (standard Unix):
```
┌───────────── minute (0-59)
│ ┌───────────── hour (0-23)
│ │ ┌───────────── day of month (1-31)
│ │ │ ┌───────────── month (1-12 or JAN-DEC)
│ │ │ │ ┌───────────── day of week (0-6, SUN=0, or SUN-SAT)
│ │ │ │ │
* * * * *
```

**6-field** (with seconds):
```
┌───────────── second (0-59)
│ ┌───────────── minute (0-59)
│ │ ┌───────────── hour (0-23)
│ │ │ ┌───────────── day of month (1-31)
│ │ │ │ ┌───────────── month (1-12 or JAN-DEC)
│ │ │ │ │ ┌───────────── day of week (0-6 or SUN-SAT)
│ │ │ │ │ │
* * * * * *
```

### Special Characters

| Character | Meaning | Example | Description |
|-----------|---------|---------|-------------|
| `*` | Any value | `* * * * *` | Every minute |
| `?` | No specific value | `0 0 * * ?` | Same as `*` for day fields |
| `,` | List | `1,15 * * * *` | At minute 1 and 15 |
| `-` | Range | `9-17 * * * *` | Minutes 9 through 17 |
| `/` | Step | `*/5 * * * *` | Every 5 minutes |
| `L` | Last | `0 0 L * *` | Last day of month |
| `L-N` | Last minus N | `0 0 L-3 * *` | 3 days before last day |
| `LW` | Last weekday | `0 0 LW * *` | Last weekday of month |
| `nW` | Nearest weekday | `0 0 15W * *` | Nearest weekday to 15th |
| `n#m` | Nth occurrence | `0 0 * * 5#3` | 3rd Friday |
| `nL` | Last occurrence | `0 0 * * 5L` | Last Friday |

### Predefined Macros

| Macro | Equivalent | Description |
|-------|-----------|-------------|
| `@yearly` / `@annually` | `0 0 1 1 *` | January 1st at midnight |
| `@monthly` | `0 0 1 * *` | 1st of each month at midnight |
| `@weekly` | `0 0 * * 0` | Sunday at midnight |
| `@daily` / `@midnight` | `0 0 * * *` | Every day at midnight |
| `@hourly` | `0 * * * *` | Every hour |

### Day-of-Month / Day-of-Week OR Semantics

When **both** day-of-month and day-of-week are explicitly set (not `*` or `?`), the expression fires when **either** matches (OR logic), following standard Unix cron behavior.

```java
// Fires on the 15th of any month OR any Monday at noon
CronExpression.parse("0 12 15 * MON")
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenCron` | Facade — unified entry point for all cron operations |
| `CronExpression` | Immutable parsed expression with scheduling, matching, description |
| `CronExplanation` | Immutable record for one-stop debugging output |
| `CronBuilder` | Fluent builder API for constructing expressions |
| `CronValidator` | Syntax and interval validation utility |
| `CronField` | Enum defining ranges, aliases for each cron field |
| `CronMacro` | Macro resolution (`@daily` → `0 0 * * *`) |
| `OpenCronException` | Runtime exception with diagnostic context |

## Quick Start

```java
import cloud.opencode.base.cron.*;
import java.time.*;
import java.util.*;

ZonedDateTime now = ZonedDateTime.now();

// Parse and query next execution
ZonedDateTime next = OpenCron.nextExecution("0 9 * * MON-FRI", now);

// Get next 5 executions
List<ZonedDateTime> times = OpenCron.nextExecutions("30 10 * * *", now, 5);

// Validate
boolean valid = OpenCron.isValid("0 0 L * *");

// Human-readable description
OpenCron.describe("0 9 * * MON-FRI");                  // "At 09:00, Monday through Friday"
OpenCron.describe("0 9 * * MON-FRI", Locale.CHINESE);  // "在09:00，周一到周五"

// Builder API
CronExpression expr = OpenCron.builder().weekdays().at(9, 0).build();

// Macro support
CronExpression daily = OpenCron.parse("@daily");
```

## Stream-Based Scheduling

```java
CronExpression expr = CronExpression.parse("*/5 * * * *");
ZonedDateTime now = ZonedDateTime.now();
ZonedDateTime deadline = now.plusDays(1);

// Lazy stream — computed on demand, supports all Stream operations
List<ZonedDateTime> next10 = expr.stream(now).limit(10).toList();

// All executions before a deadline
List<ZonedDateTime> beforeDeadline = expr.stream(now)
        .takeWhile(t -> t.isBefore(deadline))
        .toList();

// Reverse stream (past executions, newest first)
List<ZonedDateTime> last5 = expr.reverseStream(now).limit(5).toList();
```

## Filtered Scheduling

```java
CronExpression expr = CronExpression.parse("0 9 * * *"); // daily at 9:00
ZonedDateTime now = ZonedDateTime.now();

// Skip weekends
ZonedDateTime nextWeekday = expr.nextExecution(now,
        t -> t.getDayOfWeek().getValue() <= 5);

// Skip holidays
Set<LocalDate> holidays = Set.of(
        LocalDate.of(2026, 1, 1),
        LocalDate.of(2026, 12, 25));
ZonedDateTime nextWorkday = expr.nextExecution(now,
        t -> !holidays.contains(t.toLocalDate()));

// Previous execution with filter
ZonedDateTime prevWeekday = expr.previousExecution(now,
        t -> t.getDayOfWeek().getValue() <= 5);
```

## Overlap Detection

```java
CronExpression daily = CronExpression.parse("0 0 * * *");
CronExpression monthly = CronExpression.parse("0 0 1 * *");
ZonedDateTime now = ZonedDateTime.now();

// Find next time both fire simultaneously
ZonedDateTime overlap = daily.nextOverlap(monthly, now); // 1st of next month at 00:00

// Check if overlap exists in a range
ZonedDateTime from = ZonedDateTime.parse("2026-01-01T00:00:00Z");
ZonedDateTime to = ZonedDateTime.parse("2026-12-31T23:59:59Z");
boolean hasOverlap = daily.hasOverlapBetween(monthly, from, to); // true
```

## TemporalAdjuster Integration

```java
CronExpression expr = CronExpression.parse("0 9 * * MON-FRI");

// Use with java.time API natively
ZonedDateTime nextFire = ZonedDateTime.now().with(expr);
```

## Duration & Interval

```java
ZonedDateTime now = ZonedDateTime.now();

// Time until next execution
Duration toNext = OpenCron.timeToNextExecution("0 9 * * *", now);

// Time since last execution
Duration fromLast = OpenCron.timeFromLastExecution("0 9 * * *", now);

// Estimated interval between executions
Duration interval = OpenCron.getEstimatedInterval("*/5 * * * *"); // PT5M
```

## Executions Between Dates

```java
ZonedDateTime from = ZonedDateTime.parse("2026-01-01T00:00:00Z");
ZonedDateTime to = ZonedDateTime.parse("2026-01-08T00:00:00Z");

// Count only (no list allocation, capped at 1M)
long count = OpenCron.countExecutionsBetween("0 0 * * *", from, to); // 7

// List all executions (capped at 100K)
List<ZonedDateTime> execs = OpenCron.executionsBetween("0 0 * * *", from, to);
```

## Expression Equivalence

```java
// Structural comparison (BitSets + special fields)
boolean eq = OpenCron.isEquivalent("0 0 * * *", "@daily"); // true
```

## One-Stop Debugging

```java
CronExplanation info = OpenCron.explain("*/5 * * * *", ZonedDateTime.now());
System.out.println(info);
// Expression : */5 * * * *
// Description: Every 5 minutes
// Interval   : PT5M
// Next executions:
//   1. 2026-04-03T10:05:00+08:00[Asia/Shanghai]
//   2. 2026-04-03T10:10:00+08:00[Asia/Shanghai]
//   ...
```

## Builder API

```java
// Daily at 10:30
CronBuilder.every().day().at(10, 30).buildExpression();           // "30 10 * * *"

// Weekdays at 9:00
CronBuilder.every().weekdays().at(9, 0).buildExpression();        // "0 9 * * 1-5"

// Every 5 seconds (6-field)
CronBuilder.everySeconds(5).buildExpression();                     // "0/5 * * * * *"

// Last day of month at 18:00
CronBuilder.create().lastDayOfMonth().at(18, 0).buildExpression(); // "0 18 L * *"

// 3rd Friday at 10:00
CronBuilder.create().nthDayOfWeek(DayOfWeek.FRIDAY, 3).at(10, 0).buildExpression(); // "0 10 * * 5#3"

// Day/month ranges
CronBuilder.create().dayOfMonthRange(10, 20).at(9, 0).buildExpression(); // "0 9 10-20 * *"
CronBuilder.create().monthRange(3, 9).at(9, 0).buildExpression();        // "0 9 * 3-9 *"

// Configurable search window (Feb 29 only, search up to 10 years)
CronExpression rare = CronExpression.parse("0 0 29 2 *");
ZonedDateTime nextLeap = rare.nextExecution(ZonedDateTime.now(), 10);
```

## API Method Reference

### OpenCron (Facade)

| Method | Description |
|--------|-------------|
| `parse(expr)` | Parse a cron expression |
| `isValid(expr)` | Check if expression is valid |
| `validate(expr)` | Validate, throw on error |
| `validate(expr, minInterval)` | Validate with minimum interval |
| `nextExecution(expr, from)` | Next execution time |
| `nextExecution(expr, from, filter)` | Next execution matching filter |
| `nextExecutions(expr, from, count)` | Next N execution times |
| `previousExecution(expr, from)` | Previous execution time |
| `previousExecution(expr, from, filter)` | Previous execution matching filter |
| `previousExecutions(expr, from, count)` | Previous N execution times |
| `stream(expr, from)` | Lazy forward stream |
| `reverseStream(expr, from)` | Lazy reverse stream |
| `timeToNextExecution(expr, from)` | Duration to next execution |
| `timeFromLastExecution(expr, from)` | Duration from last execution |
| `getEstimatedInterval(expr)` | Estimated interval between executions |
| `countExecutionsBetween(expr, from, to)` | Count executions in range |
| `executionsBetween(expr, from, to)` | List executions in range |
| `isEquivalent(expr1, expr2)` | Structural equivalence check |
| `nextOverlap(expr1, expr2, from)` | Next time both fire |
| `hasOverlap(expr1, expr2, from, to)` | Check overlap in range |
| `explain(expr, from)` | One-stop debugging info |
| `describe(expr)` | English description |
| `describe(expr, locale)` | Localized description |
| `builder()` | Create CronBuilder |

### CronExpression

| Method | Description |
|--------|-------------|
| `parse(expr)` | Parse expression (static factory) |
| `matches(time)` | Check if time matches |
| `nextExecution(from)` | Next execution (4-year window) |
| `nextExecution(from, maxYears)` | Next execution (custom window) |
| `nextExecution(from, filter)` | Next execution matching filter |
| `nextExecutions(from, count)` | Next N executions |
| `previousExecution(from)` | Previous execution (4-year window) |
| `previousExecution(from, maxYears)` | Previous execution (custom window) |
| `previousExecution(from, filter)` | Previous execution matching filter |
| `previousExecutions(from, count)` | Previous N executions |
| `stream(from)` | Lazy forward execution stream |
| `reverseStream(from)` | Lazy reverse execution stream |
| `timeToNextExecution(from)` | Duration to next |
| `timeFromLastExecution(from)` | Duration from last |
| `countExecutionsBetween(from, to)` | Count in range |
| `executionsBetween(from, to)` | List in range (limit 100K) |
| `executionsBetween(from, to, limit)` | List in range (custom limit) |
| `isEquivalentTo(other)` | Structural equivalence |
| `nextOverlap(other, from)` | Next overlap time |
| `hasOverlapBetween(other, from, to)` | Overlap in range |
| `explain(from)` | Debugging info |
| `describe()` | English description |
| `describe(locale)` | Localized description |
| `adjustInto(temporal)` | TemporalAdjuster (use via `.with()`) |
| `getExpression()` | Original expression string |
| `hasSeconds()` | 6-field format check |

## Requirements

- Java 25+

## License

Apache License 2.0

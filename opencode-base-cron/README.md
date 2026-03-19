# OpenCode Base Cron

Cron expression parsing, building, validation, scheduling, and human-readable description for JDK 25+.

## Features

- Parse 5-field and 6-field (with seconds) cron expressions
- Macro support: `@yearly`, `@monthly`, `@weekly`, `@daily`, `@hourly`
- Name aliases: `MON-FRI`, `JAN-DEC` (case-insensitive)
- Special characters: `L` (last), `W` (weekday), `#` (nth occurrence), range wrap-around
- Forward and reverse scheduling (next/previous execution times)
- Human-readable English descriptions
- Fluent builder API for constructing expressions
- Minimum interval validation
- Thread-safe and null-safe

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cron</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenCron` | Facade class providing unified entry point for all cron operations |
| `CronExpression` | Immutable parsed cron expression with matching, scheduling, and description |
| `CronBuilder` | Fluent builder API for constructing cron expressions with input validation |
| `CronValidator` | Validation utility with syntax check and minimum interval enforcement |
| `CronDescriber` | Generates human-readable English descriptions from parsed expressions |
| `CronField` | Enum defining valid ranges, display names, and aliases for each cron field |
| `CronMacro` | Resolves predefined macros (`@daily`, `@yearly`, etc.) to cron expressions |
| `OpenCronException` | Runtime exception with diagnostic context (expression, field name) |

## Quick Start

```java
import cloud.opencode.base.cron.OpenCron;
import cloud.opencode.base.cron.CronExpression;
import java.time.ZonedDateTime;
import java.util.List;

// Parse and query next execution
ZonedDateTime next = OpenCron.nextExecution("0 9 * * MON-FRI", ZonedDateTime.now());

// Get next 5 executions
List<ZonedDateTime> times = OpenCron.nextExecutions("30 10 * * *", ZonedDateTime.now(), 5);

// Validate expression
boolean valid = OpenCron.isValid("0 0 L * *");

// Human-readable description
String desc = OpenCron.describe("0 9 * * MON-FRI"); // "At 09:00, Monday through Friday"

// Builder API
CronExpression expr = OpenCron.builder().weekdays().at(9, 0).build();

// Macro support
CronExpression daily = OpenCron.parse("@daily");

// Every 5 seconds (6-field)
String cron = CronBuilder.everySeconds(5).buildExpression(); // "0/5 * * * * *"
```

## Requirements

- Java 25+

## License

Apache License 2.0

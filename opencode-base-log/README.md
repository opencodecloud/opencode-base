# OpenCode Base Log

**Lightweight logging facade with SPI mechanism for pluggable log engines**

`opencode-base-log` is a modern logging framework that provides a unified logging facade with pluggable backends, structured logging, audit logging, performance logging, and sensitive data masking.

## Features

### Core Features
- **Unified Logging Facade**: Static methods via `OpenLog` with automatic caller detection
- **SPI Pluggable Backends**: Support for SLF4J, Log4j2, JUL via `LogProvider` SPI
- **Parameterized Logging**: `{}` placeholder support for efficient message formatting
- **Lambda Lazy Evaluation**: Deferred message construction for performance
- **Marker Support**: Log categorization and filtering via markers

### Enhanced Features
- **Structured Logging**: JSON-style key-value structured log entries for ELK/Loki
- **Log Masking**: Sensitive data masking for passwords, phone numbers, ID cards
- **Sampled Logging**: Probability, time-based, and count-based log sampling
- **Audit Logging**: Structured audit event recording with pluggable persistence
- **Performance Logging**: StopWatch, timed execution, slow operation detection
- **Virtual Thread Context**: Context propagation for virtual threads
- **MDC/NDC**: Mapped and Nested Diagnostic Context support
- **Conditional Logging**: Conditional log output based on dynamic rules
- **Scoped Log Context**: Auto-closeable scoped context management

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-log</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.log.OpenLog;
import cloud.opencode.base.log.Logger;

// Simple logging (auto-detects caller class)
OpenLog.info("Application started");

// Parameterized logging
OpenLog.info("User {} logged in from {}", userId, ipAddress);

// Lambda lazy evaluation
OpenLog.debug(() -> "Expensive: " + computeValue());

// Exception logging
OpenLog.error("Operation failed", exception);

// Get Logger instance
Logger log = OpenLog.get(MyClass.class);
log.info("Hello from {}", "MyClass");
```

### Structured Logging

```java
import cloud.opencode.base.log.enhance.StructuredLog;

StructuredLog.info()
    .message("User login successful")
    .field("userId", "user123")
    .field("ip", "192.168.1.1")
    .field("duration", 150)
    .log();
// Output: {"message":"User login successful","userId":"user123","ip":"192.168.1.1","duration":150}
```

### Performance Logging

```java
import cloud.opencode.base.log.perf.PerfLog;
import cloud.opencode.base.log.perf.StopWatch;

// StopWatch
StopWatch watch = PerfLog.start("queryUsers");
List<User> users = userDao.findAll();
watch.stopAndLog();

// Timed execution
PerfLog.timed("processOrder", () -> orderService.process(order));
```

### Log Masking

```java
import cloud.opencode.base.log.enhance.LogMasking;

String masked = LogMasking.mask("13812345678", MaskingStrategy.PHONE);
// Output: 138****5678
```

## Class Reference

| Class | Description |
|-------|-------------|
| `OpenLog` | Main entry point - unified static logging facade with auto caller detection |
| `Logger` | Core logging interface defining standard log operations |
| `LoggerFactory` | Factory for creating Logger instances by class or name |
| `LogLevel` | Log level enumeration: TRACE, DEBUG, INFO, WARN, ERROR, OFF |
| `AuditEvent` | Immutable audit event record with user, action, and resource |
| `AuditLog` | Static facade for audit event recording |
| `AuditLogger` | SPI interface for custom audit log persistence |
| `LogContext` | Utility for managing log context across threads |
| `MDC` | Mapped Diagnostic Context for key-value thread context |
| `NDC` | Nested Diagnostic Context for stack-based thread context |
| `ConditionalLog` | Conditional log output based on dynamic rules |
| `ExceptionLog` | Enhanced exception logging with stack trace formatting |
| `LogMasking` | Sensitive data masking utility (passwords, phones, IDs) |
| `LogMetrics` | Log metrics collection and reporting |
| `SampledLog` | Rate-limited and sampled logging (probability, time, count) |
| `ScopedLogContext` | Auto-closeable scoped log context management |
| `StructuredLog` | JSON-style structured logging with fluent API |
| `VirtualThreadContext` | Context propagation support for virtual threads |
| `OpenLogException` | Exception type for logging framework errors |
| `Marker` | Log marker for categorization and filtering |
| `Markers` | Predefined marker constants and factory methods |
| `PerfLog` | Performance logging utility with StopWatch integration |
| `SlowOperationConfig` | Configuration for slow operation detection thresholds |
| `StopWatch` | High-precision operation timing with log integration |
| `DefaultLogProvider` | Default SPI log provider implementation |
| `LogAdapter` | Adapter for bridging external logging frameworks |
| `LogProvider` | SPI interface for pluggable log engine backends |
| `LogProviderFactory` | Factory for discovering and managing log providers |
| `MDCAdapter` | SPI interface for MDC implementation |
| `NDCAdapter` | SPI interface for NDC implementation |

## Requirements

- Java 25+ (uses virtual threads, StackWalker, records)
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

# OpenCode Base Observability

**Tracing, span, and slow-log utilities for JDK 25+**

`opencode-base-observability` provides a framework-agnostic observability layer with distributed tracing via OpenTelemetry (auto-detected, no hard dependency), span management with try-with-resources support, and a bounded slow-operation log collector inspired by Redis SLOWLOG.

## Features

- **Framework-Agnostic Tracing**: Sealed `Tracer` interface with pluggable implementations
- **OpenTelemetry Integration**: Auto-detects OTel on classpath via reflection, zero hard dependency
- **No-Op Fallback**: Zero-overhead no-op implementation when OTel is unavailable
- **Span Management**: AutoCloseable spans with hit/miss recording, error capture, and custom attributes
- **Slow Log Collector**: Bounded, thread-safe slow operation log with configurable threshold
- **Statistical Aggregation**: Slow operation statistics (count, max, average, slowest operation)
- **Thread-Safe**: All components are fully thread-safe (ConcurrentHashMap, AtomicBoolean, ConcurrentLinkedDeque)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-observability</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Tracing with OpenTelemetry

```java
import cloud.opencode.base.observability.Tracer;
import cloud.opencode.base.observability.OpenTelemetryTracer;
import cloud.opencode.base.observability.Span;

// Auto-detects OTel on classpath; falls back to noop if absent
Tracer tracer = OpenTelemetryTracer.create("my-service");

try (Span span = tracer.startSpan("GET", "user:123")) {
    Object value = cache.get("user:123");
    span.setHit(value != null);
    span.setAttribute("cache.tier", "L1");
} catch (Exception e) {
    // span.setError(e) can be called for error recording
    throw e;
}

tracer.close();
```

### No-Op Tracer

```java
// Zero-overhead no-op tracer for testing or when tracing is disabled
Tracer noopTracer = Tracer.noop();
try (Span span = noopTracer.startSpan("GET", "key")) {
    // All span operations are no-op
}
```

### Slow Log Collector

```java
import cloud.opencode.base.observability.SlowLogCollector;
import java.time.Duration;

// Default: 10ms threshold, 1024 max entries
SlowLogCollector collector = SlowLogCollector.create();

// Custom threshold
SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(50));

// Custom threshold and buffer size
SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(50), 2048);

// Record a slow operation
collector.record("GET", "user:123", Duration.ofMillis(75));

// Query recent entries
List<SlowLogCollector.Entry> recent = collector.getEntries(10);

// Get statistics
SlowLogCollector.Stats stats = collector.stats();
System.out.println("Total slow ops: " + stats.totalSlowOps());
System.out.println("Max duration: " + stats.maxDuration());
System.out.println("Avg duration: " + stats.avgDuration());
System.out.println("Slowest op: " + stats.slowestOperation());

// Clear entries (cumulative count preserved)
collector.clear();
```

## Class Reference

| Class | Description |
|-------|-------------|
| `Tracer` | Sealed interface for framework-agnostic operation tracing |
| `Tracer.NoopTracer` | Zero-overhead no-op tracer implementation |
| `OpenTelemetryTracer` | OpenTelemetry integration via reflection with graceful noop fallback |
| `Span` | AutoCloseable span interface for hit/miss, error, and attribute recording |
| `SlowLogCollector` | Bounded, thread-safe slow operation log collector (Redis SLOWLOG-inspired) |
| `SlowLogCollector.Entry` | Single slow log entry record (operation, key, elapsed, timestamp, thread) |
| `SlowLogCollector.Stats` | Aggregated statistics for buffered slow operations |

## Requirements

- Java 25+ (uses sealed interfaces, records, StackWalker)
- No external dependencies required
- Optional: OpenTelemetry API on classpath for real tracing

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

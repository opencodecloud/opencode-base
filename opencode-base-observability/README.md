# OpenCode Base Observability

**Lightweight, framework-agnostic observability primitives for JDK 25+**

`opencode-base-observability` provides a complete observability toolkit with zero external dependencies: metrics (Counter/Gauge/Timer/Histogram), health checks, context propagation, distributed tracing (OpenTelemetry auto-detected via reflection), and slow-operation logging.

## Features

- **Metrics**: Counter, Gauge, Timer, Histogram with tag-based dimensional model (inspired by Micrometer, but lightweight)
- **MetricRegistry**: Central registry with idempotent registration, capacity limits (default 10,000), and snapshot export
- **Health Checks**: Functional interface with aggregated status (UP/DOWN/DEGRADED), exception isolation, capacity limits
- **Context Propagation**: ThreadLocal-based `ObservabilityContext` with traceId, spanId, baggage, cross-thread `wrap(Runnable/Callable)`
- **Tracing**: Sealed `Tracer` interface with OpenTelemetry integration via reflection (optional, no hard dependency)
- **Slow Log**: Bounded, thread-safe slow operation log with statistical aggregation (Redis SLOWLOG-inspired)
- **Zero Dependencies**: No Spring, no Micrometer, no OTel SDK required
- **Thread-Safe**: LongAdder-based counters, `ConcurrentHashMap` registry, `VarHandle` max tracking
- **High Performance**: Counter ~57M ops/s, Timer ~47M ops/s, Histogram ~41M ops/s (single-thread benchmarks)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-observability</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Metrics

```java
import cloud.opencode.base.observability.metric.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

MetricRegistry registry = MetricRegistry.create();

// Counter — LongAdder-based, high-concurrency optimized
Counter requests = registry.counter("http.requests", Tag.of("method", "GET"));
requests.increment();
requests.increment(10);
System.out.println(requests.count()); // 11

// Gauge — backed by Supplier<Double>
AtomicInteger queueSize = new AtomicInteger(42);
Gauge queue = registry.gauge("queue.size", queueSize::doubleValue);
System.out.println(queue.value()); // 42.0

// Timer — duration recording with time(Runnable/Callable) support
Timer dbQuery = registry.timer("db.query", Tag.of("table", "users"));
dbQuery.time(() -> {
    // your operation here
});
System.out.println(dbQuery.count());     // 1
System.out.println(dbQuery.totalTime()); // PT0.000012S
System.out.println(dbQuery.max());       // PT0.000012S
System.out.println(dbQuery.mean());      // PT0.000012S

// Histogram — ring-buffer percentile (8192 samples)
Histogram responseSize = registry.histogram("http.response.size");
responseSize.record(1024.0);
responseSize.record(2048.0);
responseSize.record(512.0);
System.out.println(responseSize.percentile(0.50)); // p50
System.out.println(responseSize.percentile(0.99)); // p99

// Snapshot all metrics for export
List<MetricSnapshot> snapshots = registry.snapshot();
```

### Health Checks

```java
import cloud.opencode.base.observability.health.*;
import java.time.Duration;
import java.util.Map;

HealthRegistry health = HealthRegistry.create(); // default limit: 1000 checks

health.register("database", () -> {
    long start = System.nanoTime();
    boolean ok = db.isConnected();
    Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
    return ok
        ? HealthResult.up("database", elapsed)
        : HealthResult.down("database", "connection refused", elapsed);
});

health.register("cache", () ->
    HealthResult.up("cache", Duration.ofMillis(1))
);

// Execute all checks (exceptions are caught and produce DOWN results)
Map<String, HealthResult> results = health.check();
HealthStatus overall = health.status(); // UP, DOWN, or DEGRADED
```

### Context Propagation

```java
import cloud.opencode.base.observability.context.*;

// Create and attach context (MUST use try-with-resources)
ObservabilityContext ctx = ObservabilityContext.create("trace-abc-123");
try (ObservabilityContext.Scope scope = ctx.attach()) {
    System.out.println(ObservabilityContext.current().traceId()); // trace-abc-123

    // Propagate to virtual threads
    Runnable wrapped = ctx.wrap(() -> {
        String traceId = ObservabilityContext.current().traceId(); // trace-abc-123
    });
    Thread.startVirtualThread(wrapped);
}
// Context automatically restored after try-with-resources

// Add baggage (returns new immutable context)
ObservabilityContext withBaggage = ctx.withBaggage("user.id", "u-456");
```

### Tracing

```java
import cloud.opencode.base.observability.*;

// Auto-detects OTel on classpath; falls back to noop if absent
Tracer tracer = OpenTelemetryTracer.create("my-service");

try (Span span = tracer.startSpan("GET", "user:123")) {
    Object value = cache.get("user:123");
    span.setHit(value != null);
    span.setAttribute("cache.tier", "L1");
}

tracer.close();

// Zero-overhead noop tracer for testing
Tracer noop = Tracer.noop();
```

### Slow Log Collector

```java
import cloud.opencode.base.observability.SlowLogCollector;
import java.time.Duration;

// threshold must be positive (> 0)
SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(50));
collector.record("GET", "user:123", Duration.ofMillis(75));

SlowLogCollector.Stats stats = collector.stats();
System.out.println("Total slow ops: " + stats.totalSlowOps());
System.out.println("Max duration:   " + stats.maxDuration());
System.out.println("Avg duration:   " + stats.avgDuration());
System.out.println("Slowest op:     " + stats.slowestOperation());

// Clear buffer (cumulative count preserved)
collector.clear();
```

## API Reference

### Metrics (`cloud.opencode.base.observability.metric`)

| Class | Type | Description |
|-------|------|-------------|
| `Counter` | interface | Monotonic increment counter (LongAdder-based) |
| `Gauge` | interface | Instantaneous value backed by `Supplier<Double>` |
| `Timer` | interface | Duration recording + count, `time(Runnable)` / `time(Callable<T>)` |
| `Histogram` | interface | Distribution statistics with ring-buffer percentile (8192 samples) |
| `MetricRegistry` | final class | Central registry: `create()`, `counter()`, `gauge()`, `timer()`, `histogram()`, `snapshot()`, `find()`, `remove()`, `clear()`, `size()` |
| `MetricId` | record | Metric identity: name + sorted tags, `of(name, tags...)` |
| `Tag` | record | Key-value label: `of(key, value)` |
| `MetricSnapshot` | record | Point-in-time snapshot: id, type, values map |

### Health (`cloud.opencode.base.observability.health`)

| Class | Type | Description |
|-------|------|-------------|
| `HealthCheck` | @FunctionalInterface | `HealthResult check()` |
| `HealthStatus` | enum | `UP`, `DOWN`, `DEGRADED` + `aggregate(Collection)` |
| `HealthResult` | record | name, status, detail, duration + `up()`, `down()`, `degraded()` factories |
| `HealthRegistry` | final class | `create()`, `create(maxChecks)`, `register()`, `unregister()`, `check()`, `status()`, `names()`, `size()`, `clear()` |

### Context (`cloud.opencode.base.observability.context`)

| Class | Type | Description |
|-------|------|-------------|
| `ObservabilityContext` | final class | `create()`, `current()`, `clear()`, `attach()`, `wrap()`, `withSpanId()`, `withBaggage()`, `traceId()`, `spanId()`, `baggage()`, `allBaggage()` |
| `ObservabilityContext.Scope` | static final class | `AutoCloseable` scope for context lifecycle (thread-safe close guard) |

### Tracing (`cloud.opencode.base.observability`)

| Class | Type | Description |
|-------|------|-------------|
| `Tracer` | sealed interface | `startSpan()`, `close()`, `noop()` |
| `OpenTelemetryTracer` | final class | `create(serviceName)`, `isOtelAvailable()`, `serviceName()` |
| `Span` | interface | `setHit()`, `setError()`, `setAttribute()`, `end()`, `close()`, `NOOP` singleton |
| `SlowLogCollector` | final class | `create()`, `record()`, `getEntries()`, `clear()`, `count()`, `stats()`, `threshold()`, `maxEntries()` |
| `SlowLogCollector.Entry` | record | operation, key, elapsed, timestamp, threadName |
| `SlowLogCollector.Stats` | record | totalSlowOps, maxDuration, avgDuration, slowestOperation + `EMPTY` |

### Exception (`cloud.opencode.base.observability.exception`)

| Class | Type | Description |
|-------|------|-------------|
| `ObservabilityException` | class | Extends `OpenException`, component = "Observability" |

## Performance

Benchmark results (Apple Silicon, JDK 25, single-thread / 16 virtual threads):

| Operation | 1 Thread (ops/s) | 16 Threads (ops/s) |
|-----------|------------------:|-------------------:|
| Counter.increment() | ~57M | ~37M |
| Timer.record(Duration) | ~47M | ~33M |
| Timer.time(Runnable) | ~25M | — |
| Histogram.record(double) | ~41M | ~20M |
| Registry.counter() lookup | ~25M | ~17M |
| Context.create() | ~32M | — |
| Context attach/detach | ~18M | — |

## Module Info

```java
module cloud.opencode.base.observability {
    requires transitive cloud.opencode.base.core;

    exports cloud.opencode.base.observability;
    exports cloud.opencode.base.observability.context;
    exports cloud.opencode.base.observability.exception;
    exports cloud.opencode.base.observability.health;
    exports cloud.opencode.base.observability.metric;
}
```

## Requirements

- Java 25+ (sealed interfaces, records, VarHandle, LongAdder)
- No external dependencies required
- Optional: OpenTelemetry API on classpath for real tracing

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

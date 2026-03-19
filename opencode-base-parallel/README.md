# OpenCode Base Parallel

**Modern parallel computing utilities for JDK 25+**

`opencode-base-parallel` is a comprehensive parallel computing library leveraging JDK 25 virtual threads and structured concurrency (JEP 499). It provides parallel execution, batch processing, async pipelines, rate limiting, deadline propagation, and structured concurrency scopes.

## Features

### Core Features
- **Virtual Thread Execution**: All parallel tasks run on virtual threads by default
- **Parallel Map**: Concurrent mapping with configurable parallelism and timeout
- **Batch Processing**: Partition-based parallel batch processing with progress tracking
- **Async Pipeline**: Fluent async pipeline composition with error handling
- **Future Combination**: Combine 2 or 3 futures with combiner functions

### Structured Concurrency (JEP 499)
- **invokeAll / invokeAny**: Structured task execution with fail-fast and first-success policies
- **Parallel Combine**: Type-safe parallel combination of 2 or 3 tasks
- **Scoped Values**: ScopedValue-based context propagation across structured tasks
- **Race**: Race multiple tasks, returning the first to complete

### Advanced Features
- **Rate Limiting**: Token bucket rate limiter with burst capacity
- **Deadline Propagation**: ScopedValue-based deadline context for virtual threads
- **Scheduled Scope**: Delayed, periodic, and deadline-based task scheduling
- **CPU-Bound Executor**: Platform thread executor optimized for CPU-intensive work
- **Hybrid Executor**: Auto-routing executor for mixed IO/CPU workloads

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-parallel</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Parallel Execution

```java
import cloud.opencode.base.parallel.OpenParallel;

// Run tasks in parallel
OpenParallel.runAll(
    () -> sendEmail(),
    () -> sendSMS(),
    () -> pushNotification()
);

// Parallel with results
List<String> results = OpenParallel.invokeAll(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
);

// First to complete wins
String fastest = OpenParallel.invokeAny(
    () -> fetchFromPrimary(),
    () -> fetchFromBackup()
);
```

### Parallel Map with Concurrency Control

```java
// Map items in parallel with concurrency limit
List<Result> processed = OpenParallel.parallelMap(items, item -> process(item), 10);

// With timeout
List<Result> results = OpenParallel.parallelMap(items, this::process, 10, Duration.ofSeconds(30));
```

### Structured Concurrency (JEP 499)

```java
import cloud.opencode.base.parallel.OpenStructured;

// All must succeed (fail-fast)
List<String> results = OpenStructured.invokeAll(List.of(
    () -> fetchA(),
    () -> fetchB(),
    () -> fetchC()
));

// First success wins (cancel others)
String result = OpenStructured.invokeAny(List.of(
    () -> fetchFromPrimary(),
    () -> fetchFromBackup()
));

// Parallel combine two tasks
Result result = OpenStructured.parallel(
    () -> fetchUser(),
    () -> fetchOrders(),
    (user, orders) -> new Result(user, orders)
);

// Race
String winner = OpenStructured.race(
    () -> queryDatabaseA(),
    () -> queryDatabaseB()
);
```

### Async Pipeline

```java
String result = OpenParallel.pipeline(() -> fetchData())
    .then(this::transform)
    .then(this::validate)
    .onError(e -> "fallback")
    .get();
```

### Batch Processing

```java
import cloud.opencode.base.parallel.batch.BatchProcessor;

// Simple batch processing
OpenParallel.processBatch(items, 100, batch -> repository.saveAll(batch));

// Configurable batch processor
BatchProcessor.builder()
    .batchSize(100)
    .parallelism(10)
    .build()
    .process(items, batch -> repository.saveAll(batch));
```

### Rate Limiting

```java
import cloud.opencode.base.parallel.executor.RateLimitedExecutor;

// 100 requests per second, burst of 20
RateLimitedExecutor executor = OpenParallel.rateLimited(100, 20);
executor.submit(() -> callApi());
```

### Deadline Propagation

```java
import cloud.opencode.base.parallel.deadline.DeadlineContext;

DeadlineContext.withTimeout(Duration.ofSeconds(5), () -> {
    // All operations in this scope can check the deadline
    Optional<Duration> remaining = DeadlineContext.remaining();
    if (remaining.isPresent() && remaining.get().isNegative()) {
        throw new TimeoutException("Deadline exceeded");
    }
});
```

### Scheduled Scope

```java
try (var scope = OpenParallel.<String>scheduledScope()) {
    scope.fork(() -> fetchA());
    scope.forkDelayed(Duration.ofSeconds(1), () -> fetchB());
    List<String> results = scope.joinAll();
}
```

## Class Reference

| Class | Description |
|-------|-------------|
| `OpenParallel` | Main facade - parallel execution, batch, pipeline, rate limiting, scheduling |
| `OpenStructured` | Structured concurrency facade (JEP 499) - invokeAll, invokeAny, parallel, race |
| `BatchProcessor` | Configurable parallel batch processor with progress tracking |
| `PartitionUtil` | List partitioning utility for batch processing |
| `OpenParallelException` | Exception type for parallel operation failures |
| `ExecutorConfig` | Configuration record for executor settings |
| `VirtualExecutor` | Virtual thread executor with concurrency limiting and statistics |
| `CpuBound` | Platform thread executor optimized for CPU-intensive tasks |
| `HybridExecutor` | Auto-routing executor for mixed IO/CPU workloads |
| `TokenBucketRateLimiter` | Token bucket rate limiter implementation |
| `RateLimitedExecutor` | Rate-limited task executor with token bucket algorithm |
| `AsyncPipeline` | Fluent async pipeline with chaining and error handling |
| `TriFunction` | Three-argument function interface for combining three results |
| `ScheduledScope` | Structured scope with delayed and periodic task scheduling |
| `ScopedContext` | ScopedValue-based context propagation for structured tasks |
| `StructuredScope` | Wrapper for JDK StructuredTaskScope with shutdown policies |
| `TaskResult` | Result record for structured task execution |
| `DeadlineContext` | ScopedValue-based deadline propagation for virtual threads |

## Requirements

- Java 25+ (uses virtual threads, structured concurrency JEP 499, ScopedValue, records)
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

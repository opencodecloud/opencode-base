# OpenCode Base Parallel

**Modern parallel computing utilities for JDK 25+**

`opencode-base-parallel` is a comprehensive parallel computing library leveraging JDK 25 virtual threads and ScopedValue. It provides parallel execution, batch processing, async pipelines, rate limiting, deadline propagation, and future aggregation utilities.

## Features

### Core Parallel Execution
- **runAll / invokeAll / invokeAny**: Parallel task execution with timeout support
- **parallelMap**: Concurrent mapping with configurable parallelism and timeout
- **parallelForEach**: Bounded-concurrency forEach for side-effect operations
- **parallelMapSettled**: Partial-success parallel map returning `ParallelResult`
- **forEachAsCompleted**: Process results in completion order (fastest first)

### Future Aggregation (Futures)
- **allAsList**: Collect all future results into a list (fail-fast)
- **successfulAsList**: Collect only successful results, ignore failures
- **settleAll**: Collect both successes and failures into `ParallelResult`
- **firstSuccessful**: Race futures, return first success, cancel losers
- **withTimeout**: Add timeout to any CompletableFuture

### Batch Processing
- **BatchProcessor**: Configurable parallel batch processor with progress tracking
- **PartitionUtil**: List partitioning utility for batch processing

### Executors
- **VirtualExecutor**: Virtual thread executor with concurrency limiting and statistics
- **HybridExecutor**: Auto-routing executor for mixed IO/CPU workloads
- **RateLimitedExecutor**: Token bucket rate-limited task executor
- **TokenBucketRateLimiter**: Standalone non-blocking rate limiter primitive

### Advanced Features
- **AsyncPipeline**: Fluent async pipeline composition with error handling
- **DeadlineContext**: ScopedValue-based deadline propagation for virtual threads
- **ScopedContext**: ScopedValue-based context propagation (trace ID, user ID, tenant ID)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-parallel</artifactId>
    <version>1.0.3</version>
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

// First to complete wins (cancels remaining)
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

### Partial Success with ParallelResult (V1.0.3)

```java
import cloud.opencode.base.parallel.ParallelResult;

// Collect both successes and failures instead of throwing
ParallelResult<Result> result = OpenParallel.parallelMapSettled(
    items, item -> riskyProcess(item), 10);

System.out.println("Succeeded: " + result.successCount());
System.out.println("Failed: " + result.failureCount());

// Get successes, or throw if any failed
List<Result> values = result.getOrThrow();

// Or handle partial failures gracefully
if (result.hasFailures()) {
    log.warn("{}/{} tasks failed", result.failureCount(), result.totalCount());
    result.failures().forEach(e -> log.warn("Failure: ", e));
}
```

### Future Aggregation (V1.0.3)

```java
import cloud.opencode.base.parallel.Futures;

// Collect all results (like Guava Futures.allAsList)
CompletableFuture<List<String>> all = Futures.allAsList(future1, future2, future3);

// Collect only successful results
CompletableFuture<List<String>> successes = Futures.successfulAsList(futures);

// Settle all - get both successes and failures
CompletableFuture<ParallelResult<String>> settled = Futures.settleAll(futures);

// Race - first successful wins, cancel losers
CompletableFuture<String> first = Futures.firstSuccessful(futures);

// Add timeout to any future
CompletableFuture<String> timed = Futures.withTimeout(future, Duration.ofSeconds(5));
```

### Parallel ForEach (V1.0.3)

```java
// Apply action to each item with bounded concurrency
OpenParallel.parallelForEach(urls, 20, url -> download(url));

// With timeout
OpenParallel.parallelForEach(urls, 20, url -> download(url), Duration.ofSeconds(60));
```

### Completion-Order Processing (V1.0.3)

```java
// Process results as they complete (fastest first)
OpenParallel.forEachAsCompleted(
    List.of(() -> slowQuery(), () -> fastQuery(), () -> mediumQuery()),
    10,  // max concurrency
    result -> display(result)
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
try (var processor = BatchProcessor.builder()
        .batchSize(100)
        .parallelism(10)
        .build()) {
    processor.process(items, batch -> repository.saveAll(batch));
}
```

### Rate Limiting

```java
import cloud.opencode.base.parallel.executor.RateLimitedExecutor;

// 100 requests per second, burst of 20
try (var executor = OpenParallel.rateLimited(100, 20)) {
    executor.submit(() -> callApi());
}
```

### Deadline Propagation

```java
import cloud.opencode.base.parallel.deadline.DeadlineContext;

DeadlineContext.withTimeout(Duration.ofSeconds(5), () -> {
    // All operations in this scope can check the deadline
    Optional<Duration> remaining = DeadlineContext.remaining();
    if (DeadlineContext.isExpired()) {
        throw new TimeoutException("Deadline exceeded");
    }
});
```

### Hybrid Executor (IO + CPU)

```java
import cloud.opencode.base.parallel.executor.HybridExecutor;
import cloud.opencode.base.parallel.executor.CpuBound;

try (var executor = HybridExecutor.create()) {
    executor.execute(() -> fetchFromNetwork());            // IO pool (virtual threads)
    executor.execute((CpuBound) () -> computeHash(data));  // CPU pool (platform threads)
}
```

## API Reference

### OpenParallel — Main Facade

| Method | Description |
|--------|-------------|
| `runAll(Runnable...)` | Run all tasks in parallel, wait for completion |
| `runAll(Collection<Runnable>)` | Run all tasks in parallel |
| `runAll(Collection<Runnable>, Duration)` | Run all tasks with timeout |
| `invokeAll(Supplier<T>...)` | Invoke all suppliers, collect results |
| `invokeAll(Collection<Supplier<T>>)` | Invoke all suppliers, collect results |
| `invokeAll(Collection<Supplier<T>>, Duration)` | Invoke all with timeout |
| `invokeAny(Supplier<T>...)` | Return first completed result, cancel remaining |
| `invokeAny(Collection<Supplier<T>>)` | Return first completed result, cancel remaining |
| `parallelForEach(Collection, int, Consumer)` | Bounded-concurrency forEach |
| `parallelForEach(Collection, int, Consumer, Duration)` | Bounded forEach with timeout |
| `parallelMap(List, Function)` | Parallel map using virtual threads |
| `parallelMap(List, Function, int)` | Parallel map with concurrency limit |
| `parallelMap(List, Function, int, Duration)` | Parallel map with concurrency limit and timeout |
| `parallelMapSettled(List, Function, int)` | Parallel map collecting successes + failures |
| `forEachAsCompleted(List<Supplier>, Consumer)` | Process results in completion order |
| `forEachAsCompleted(List<Supplier>, int, Consumer)` | Completion-order processing with bounded concurrency |
| `processBatch(List, int, Consumer)` | Parallel batch processing |
| `processBatchAndCollect(List, int, Function)` | Parallel batch processing with result collection |
| `pipeline(Supplier)` | Create async pipeline from supplier |
| `pipeline(CompletableFuture)` | Create async pipeline from future |
| `combine(CF, CF, BiFunction)` | Combine two futures |
| `combine(CF, CF, CF, TriFunction)` | Combine three futures |
| `async(Supplier)` / `async(Runnable)` | Submit async task |
| `delay(Duration, Supplier)` | Delayed execution |
| `rateLimited(double)` / `rateLimited(double, long)` | Create rate-limited executor |
| `invokeAllRateLimited(double, Supplier...)` | Execute with rate limiting |
| `getExecutor()` | Get the shared virtual thread executor |

### ParallelResult&lt;T&gt; — Partial Success Container

| Method | Description |
|--------|-------------|
| `of(List<T>, List<Throwable>)` | Create from successes and failures |
| `allSucceeded(List<T>)` | Create all-success result |
| `allFailed(List<Throwable>)` | Create all-failure result |
| `successes()` | Get success results (unmodifiable) |
| `failures()` | Get failure exceptions (unmodifiable) |
| `hasFailures()` | Check if any failures exist |
| `isAllSuccessful()` | Check if all tasks succeeded |
| `isAllFailed()` | Check if all tasks failed |
| `successCount()` / `failureCount()` / `totalCount()` | Get counts |
| `throwIfAnyFailed()` | Throw if any failure exists |
| `throwIfAllFailed()` | Throw only if all failed |
| `getOrThrow()` | Return successes or throw |

### Futures — CompletableFuture Aggregation

| Method | Description |
|--------|-------------|
| `allAsList(CompletableFuture...)` | Collect all results into list (fail-fast) |
| `allAsList(List<CompletableFuture>)` | Collect all results into list (fail-fast) |
| `successfulAsList(List<CompletableFuture>)` | Collect only successful results |
| `settleAll(List<CompletableFuture>)` | Collect successes + failures into ParallelResult |
| `firstSuccessful(List<CompletableFuture>)` | First success wins, cancel losers |
| `withTimeout(CompletableFuture, Duration)` | Add timeout to future |

### Class Overview

| Class | Description |
|-------|-------------|
| `OpenParallel` | Main facade - parallel execution, batch, pipeline, rate limiting |
| `ParallelResult` | Immutable container for partial success results (successes + failures) |
| `Futures` | CompletableFuture aggregation utilities (allAsList, settleAll, firstSuccessful) |
| `BatchProcessor` | Configurable parallel batch processor with progress tracking |
| `PartitionUtil` | List partitioning utility for batch processing |
| `OpenParallelException` | Exception type for parallel operation failures |
| `ExecutorConfig` | Configuration for executor settings |
| `VirtualExecutor` | Virtual thread executor with concurrency limiting and statistics |
| `CpuBound` | Marker interface for CPU-intensive tasks |
| `HybridExecutor` | Auto-routing executor for mixed IO/CPU workloads |
| `TokenBucketRateLimiter` | Standalone non-blocking token bucket rate limiter |
| `RateLimitedExecutor` | Rate-limited task executor with token bucket algorithm |
| `AsyncPipeline` | Fluent async pipeline with chaining and error handling |
| `TriFunction` | Three-argument function interface for combining three results |
| `ScopedContext` | ScopedValue-based context propagation for structured tasks |
| `DeadlineContext` | ScopedValue-based deadline propagation for virtual threads |

## Requirements

- Java 25+ (uses virtual threads, ScopedValue, records)
- No external dependencies (only depends on opencode-base-core)

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

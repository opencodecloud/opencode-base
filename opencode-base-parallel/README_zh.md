# OpenCode Base Parallel

**现代并行计算工具，适用于 JDK 25+**

`opencode-base-parallel` 是一个全面的并行计算库，利用 JDK 25 虚拟线程和结构化并发（JEP 499）。提供并行执行、批处理、异步流水线、限流、截止时间传播和结构化并发作用域。

## 功能特性

### 核心功能
- **虚拟线程执行**：所有并行任务默认在虚拟线程上运行
- **并行映射**：支持可配置并发数和超时的并发映射
- **批处理**：基于分区的并行批处理，支持进度跟踪
- **异步流水线**：流式异步流水线组合，支持错误处理
- **Future 组合**：使用组合函数组合 2 或 3 个 Future

### 结构化并发（JEP 499）
- **invokeAll / invokeAny**：结构化任务执行，支持快速失败和首个成功策略
- **并行组合**：类型安全的 2 或 3 个任务并行组合
- **作用域值**：基于 ScopedValue 的结构化任务间上下文传播
- **竞争**：竞争多个任务，返回首个完成的结果

### 高级功能
- **限流**：令牌桶限流器，支持突发容量
- **截止时间传播**：基于 ScopedValue 的虚拟线程截止时间上下文
- **定时作用域**：延迟、周期和基于截止时间的任务调度
- **CPU 密集执行器**：为 CPU 密集型工作优化的平台线程执行器
- **混合执行器**：IO/CPU 混合工作负载的自动路由执行器

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-parallel</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 并行执行

```java
import cloud.opencode.base.parallel.OpenParallel;

// 并行运行任务
OpenParallel.runAll(
    () -> sendEmail(),
    () -> sendSMS(),
    () -> pushNotification()
);

// 带结果的并行执行
List<String> results = OpenParallel.invokeAll(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
);

// 首个完成的获胜
String fastest = OpenParallel.invokeAny(
    () -> fetchFromPrimary(),
    () -> fetchFromBackup()
);
```

### 带并发控制的并行映射

```java
// 带并发限制的并行映射
List<Result> processed = OpenParallel.parallelMap(items, item -> process(item), 10);

// 带超时
List<Result> results = OpenParallel.parallelMap(items, this::process, 10, Duration.ofSeconds(30));
```

### 结构化并发（JEP 499）

```java
import cloud.opencode.base.parallel.OpenStructured;

// 全部必须成功（快速失败）
List<String> results = OpenStructured.invokeAll(List.of(
    () -> fetchA(),
    () -> fetchB(),
    () -> fetchC()
));

// 首个成功获胜（取消其他）
String result = OpenStructured.invokeAny(List.of(
    () -> fetchFromPrimary(),
    () -> fetchFromBackup()
));

// 并行组合两个任务
Result result = OpenStructured.parallel(
    () -> fetchUser(),
    () -> fetchOrders(),
    (user, orders) -> new Result(user, orders)
);

// 竞争
String winner = OpenStructured.race(
    () -> queryDatabaseA(),
    () -> queryDatabaseB()
);
```

### 异步流水线

```java
String result = OpenParallel.pipeline(() -> fetchData())
    .then(this::transform)
    .then(this::validate)
    .onError(e -> "fallback")
    .get();
```

### 批处理

```java
import cloud.opencode.base.parallel.batch.BatchProcessor;

// 简单批处理
OpenParallel.processBatch(items, 100, batch -> repository.saveAll(batch));

// 可配置批处理器
BatchProcessor.builder()
    .batchSize(100)
    .parallelism(10)
    .build()
    .process(items, batch -> repository.saveAll(batch));
```

### 限流

```java
import cloud.opencode.base.parallel.executor.RateLimitedExecutor;

// 每秒 100 请求，突发 20
RateLimitedExecutor executor = OpenParallel.rateLimited(100, 20);
executor.submit(() -> callApi());
```

### 截止时间传播

```java
import cloud.opencode.base.parallel.deadline.DeadlineContext;

DeadlineContext.withTimeout(Duration.ofSeconds(5), () -> {
    // 此作用域内的所有操作可以检查截止时间
    Optional<Duration> remaining = DeadlineContext.remaining();
    if (remaining.isPresent() && remaining.get().isNegative()) {
        throw new TimeoutException("超过截止时间");
    }
});
```

### 定时作用域

```java
try (var scope = OpenParallel.<String>scheduledScope()) {
    scope.fork(() -> fetchA());
    scope.forkDelayed(Duration.ofSeconds(1), () -> fetchB());
    List<String> results = scope.joinAll();
}
```

## 类参考

| 类名 | 说明 |
|------|------|
| `OpenParallel` | 主门面 - 并行执行、批处理、流水线、限流、调度 |
| `OpenStructured` | 结构化并发门面（JEP 499）- invokeAll、invokeAny、parallel、race |
| `BatchProcessor` | 可配置的并行批处理器，支持进度跟踪 |
| `PartitionUtil` | 批处理用的列表分区工具 |
| `OpenParallelException` | 并行操作失败的异常类型 |
| `ExecutorConfig` | 执行器设置的配置记录 |
| `VirtualExecutor` | 带并发限制和统计的虚拟线程执行器 |
| `CpuBound` | 为 CPU 密集型任务优化的平台线程执行器 |
| `HybridExecutor` | IO/CPU 混合工作负载的自动路由执行器 |
| `TokenBucketRateLimiter` | 令牌桶限流器实现 |
| `RateLimitedExecutor` | 基于令牌桶算法的限流任务执行器 |
| `AsyncPipeline` | 流式异步流水线，支持链式调用和错误处理 |
| `TriFunction` | 三参数函数接口，用于组合三个结果 |
| `ScheduledScope` | 支持延迟和周期任务调度的结构化作用域 |
| `ScopedContext` | 基于 ScopedValue 的结构化任务上下文传播 |
| `StructuredScope` | JDK StructuredTaskScope 的包装器，支持关闭策略 |
| `TaskResult` | 结构化任务执行的结果记录 |
| `DeadlineContext` | 基于 ScopedValue 的虚拟线程截止时间传播 |

## 环境要求

- Java 25+（使用虚拟线程、结构化并发 JEP 499、ScopedValue、记录类）
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

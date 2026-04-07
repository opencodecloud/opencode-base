# OpenCode Base Parallel

**现代并行计算工具，适用于 JDK 25+**

`opencode-base-parallel` 是一个全面的并行计算库，利用 JDK 25 虚拟线程和 ScopedValue。提供并行执行、批处理、异步流水线、限流、截止时间传播和 Future 聚合工具。

## 功能特性

### 核心并行执行
- **runAll / invokeAll / invokeAny**：带超时支持的并行任务执行
- **parallelMap**：支持可配置并发数和超时的并发映射
- **parallelForEach**：有界并发的 forEach，用于副作用操作
- **parallelMapSettled**：部分成功的并行映射，返回 `ParallelResult`
- **forEachAsCompleted**：按完成顺序处理结果（最快的先处理）

### Future 聚合工具（Futures）
- **allAsList**：将所有 Future 结果收集到列表（快速失败）
- **successfulAsList**：仅收集成功结果，忽略失败
- **settleAll**：将成功和失败收集到 `ParallelResult`
- **firstSuccessful**：竞争 Future，返回首个成功，取消其余
- **withTimeout**：为任意 CompletableFuture 添加超时

### 批处理
- **BatchProcessor**：可配置的并行批处理器，支持进度跟踪
- **PartitionUtil**：批处理用的列表分区工具

### 执行器
- **VirtualExecutor**：带并发限制和统计的虚拟线程执行器
- **HybridExecutor**：IO/CPU 混合工作负载的自动路由执行器
- **RateLimitedExecutor**：令牌桶限流任务执行器
- **TokenBucketRateLimiter**：独立的非阻塞限流原语

### 高级功能
- **AsyncPipeline**：流式异步流水线组合，支持错误处理
- **DeadlineContext**：基于 ScopedValue 的虚拟线程截止时间传播
- **ScopedContext**：基于 ScopedValue 的上下文传播（追踪 ID、用户 ID、租户 ID）

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-parallel</artifactId>
    <version>1.0.3</version>
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

// 首个完成的获胜（取消剩余）
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

### 部分成功处理 ParallelResult（V1.0.3 新增）

```java
import cloud.opencode.base.parallel.ParallelResult;

// 收集成功和失败结果，而非抛出异常
ParallelResult<Result> result = OpenParallel.parallelMapSettled(
    items, item -> riskyProcess(item), 10);

System.out.println("成功: " + result.successCount());
System.out.println("失败: " + result.failureCount());

// 获取成功结果，如有失败则抛出
List<Result> values = result.getOrThrow();

// 或优雅处理部分失败
if (result.hasFailures()) {
    log.warn("{}/{} 个任务失败", result.failureCount(), result.totalCount());
    result.failures().forEach(e -> log.warn("失败原因: ", e));
}
```

### Future 聚合工具（V1.0.3 新增）

```java
import cloud.opencode.base.parallel.Futures;

// 收集所有结果（类似 Guava Futures.allAsList）
CompletableFuture<List<String>> all = Futures.allAsList(future1, future2, future3);

// 仅收集成功结果
CompletableFuture<List<String>> successes = Futures.successfulAsList(futures);

// 结算全部 - 获取成功和失败
CompletableFuture<ParallelResult<String>> settled = Futures.settleAll(futures);

// 竞争 - 首个成功获胜，取消其余
CompletableFuture<String> first = Futures.firstSuccessful(futures);

// 为任意 Future 添加超时
CompletableFuture<String> timed = Futures.withTimeout(future, Duration.ofSeconds(5));
```

### 并发 ForEach（V1.0.3 新增）

```java
// 使用有界并发对每个元素应用操作
OpenParallel.parallelForEach(urls, 20, url -> download(url));

// 带超时
OpenParallel.parallelForEach(urls, 20, url -> download(url), Duration.ofSeconds(60));
```

### 按完成顺序处理（V1.0.3 新增）

```java
// 按完成顺序处理结果（最快的先处理）
OpenParallel.forEachAsCompleted(
    List.of(() -> slowQuery(), () -> fastQuery(), () -> mediumQuery()),
    10,  // 最大并发数
    result -> display(result)
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
try (var processor = BatchProcessor.builder()
        .batchSize(100)
        .parallelism(10)
        .build()) {
    processor.process(items, batch -> repository.saveAll(batch));
}
```

### 限流

```java
import cloud.opencode.base.parallel.executor.RateLimitedExecutor;

// 每秒 100 请求，突发 20
try (var executor = OpenParallel.rateLimited(100, 20)) {
    executor.submit(() -> callApi());
}
```

### 截止时间传播

```java
import cloud.opencode.base.parallel.deadline.DeadlineContext;

DeadlineContext.withTimeout(Duration.ofSeconds(5), () -> {
    // 此作用域内的所有操作可以检查截止时间
    Optional<Duration> remaining = DeadlineContext.remaining();
    if (DeadlineContext.isExpired()) {
        throw new TimeoutException("超过截止时间");
    }
});
```

### 混合执行器（IO + CPU）

```java
import cloud.opencode.base.parallel.executor.HybridExecutor;
import cloud.opencode.base.parallel.executor.CpuBound;

try (var executor = HybridExecutor.create()) {
    executor.execute(() -> fetchFromNetwork());            // IO 池（虚拟线程）
    executor.execute((CpuBound) () -> computeHash(data));  // CPU 池（平台线程）
}
```

## API 参考

### OpenParallel — 主门面

| 方法 | 说明 |
|------|------|
| `runAll(Runnable...)` | 并行运行所有任务，等待完成 |
| `runAll(Collection<Runnable>)` | 并行运行所有任务 |
| `runAll(Collection<Runnable>, Duration)` | 带超时并行运行所有任务 |
| `invokeAll(Supplier<T>...)` | 并行调用所有 Supplier，收集结果 |
| `invokeAll(Collection<Supplier<T>>)` | 并行调用所有 Supplier，收集结果 |
| `invokeAll(Collection<Supplier<T>>, Duration)` | 带超时并行调用 |
| `invokeAny(Supplier<T>...)` | 返回首个完成的结果，取消剩余 |
| `invokeAny(Collection<Supplier<T>>)` | 返回首个完成的结果，取消剩余 |
| `parallelForEach(Collection, int, Consumer)` | 有界并发的 forEach |
| `parallelForEach(Collection, int, Consumer, Duration)` | 带超时的有界并发 forEach |
| `parallelMap(List, Function)` | 使用虚拟线程并行映射 |
| `parallelMap(List, Function, int)` | 带并发限制的并行映射 |
| `parallelMap(List, Function, int, Duration)` | 带并发限制和超时的并行映射 |
| `parallelMapSettled(List, Function, int)` | 收集成功和失败的并行映射 |
| `forEachAsCompleted(List<Supplier>, Consumer)` | 按完成顺序处理结果 |
| `forEachAsCompleted(List<Supplier>, int, Consumer)` | 带并发控制的按完成顺序处理 |
| `processBatch(List, int, Consumer)` | 并行批处理 |
| `processBatchAndCollect(List, int, Function)` | 带结果收集的并行批处理 |
| `pipeline(Supplier)` / `pipeline(CompletableFuture)` | 创建异步流水线 |
| `combine(CF, CF, BiFunction)` | 组合两个 Future |
| `combine(CF, CF, CF, TriFunction)` | 组合三个 Future |
| `async(Supplier)` / `async(Runnable)` | 提交异步任务 |
| `delay(Duration, Supplier)` | 延迟执行 |
| `rateLimited(double)` / `rateLimited(double, long)` | 创建限流执行器 |
| `invokeAllRateLimited(double, Supplier...)` | 使用限流执行任务 |
| `getExecutor()` | 获取共享虚拟线程执行器 |

### ParallelResult&lt;T&gt; — 部分成功结果容器

| 方法 | 说明 |
|------|------|
| `of(List<T>, List<Throwable>)` | 从成功和失败列表创建 |
| `allSucceeded(List<T>)` | 创建全部成功的结果 |
| `allFailed(List<Throwable>)` | 创建全部失败的结果 |
| `successes()` | 获取成功结果（不可修改） |
| `failures()` | 获取失败异常（不可修改） |
| `hasFailures()` | 检查是否有失败 |
| `isAllSuccessful()` | 检查是否全部成功 |
| `isAllFailed()` | 检查是否全部失败 |
| `successCount()` / `failureCount()` / `totalCount()` | 获取计数 |
| `throwIfAnyFailed()` | 有失败则抛异常 |
| `throwIfAllFailed()` | 仅全部失败时抛异常 |
| `getOrThrow()` | 返回成功结果或抛异常 |

### Futures — CompletableFuture 聚合工具

| 方法 | 说明 |
|------|------|
| `allAsList(CompletableFuture...)` | 收集所有结果到列表（快速失败） |
| `allAsList(List<CompletableFuture>)` | 收集所有结果到列表（快速失败） |
| `successfulAsList(List<CompletableFuture>)` | 仅收集成功结果 |
| `settleAll(List<CompletableFuture>)` | 收集成功和失败到 ParallelResult |
| `firstSuccessful(List<CompletableFuture>)` | 首个成功获胜，取消其余 |
| `withTimeout(CompletableFuture, Duration)` | 为 Future 添加超时 |

### 类概览

| 类名 | 说明 |
|------|------|
| `OpenParallel` | 主门面 - 并行执行、批处理、流水线、限流 |
| `ParallelResult` | 不可变的部分成功结果容器（成功 + 失败） |
| `Futures` | CompletableFuture 聚合工具（allAsList、settleAll、firstSuccessful） |
| `BatchProcessor` | 可配置的并行批处理器，支持进度跟踪 |
| `PartitionUtil` | 批处理用的列表分区工具 |
| `OpenParallelException` | 并行操作失败的异常类型 |
| `ExecutorConfig` | 执行器设置的配置 |
| `VirtualExecutor` | 带并发限制和统计的虚拟线程执行器 |
| `CpuBound` | CPU 密集型任务的标记接口 |
| `HybridExecutor` | IO/CPU 混合工作负载的自动路由执行器 |
| `TokenBucketRateLimiter` | 独立的非阻塞令牌桶限流器 |
| `RateLimitedExecutor` | 基于令牌桶算法的限流任务执行器 |
| `AsyncPipeline` | 流式异步流水线，支持链式调用和错误处理 |
| `TriFunction` | 三参数函数接口，用于组合三个结果 |
| `ScopedContext` | 基于 ScopedValue 的结构化任务上下文传播 |
| `DeadlineContext` | 基于 ScopedValue 的虚拟线程截止时间传播 |

## 环境要求

- Java 25+（使用虚拟线程、ScopedValue、记录类）
- 无外部依赖（仅依赖 opencode-base-core）

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

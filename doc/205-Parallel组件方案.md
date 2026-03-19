# Parallel 组件方案

## 1. 组件概述

`opencode-base-parallel` 是基于 JDK 25 的现代化并行计算组件，充分利用 Virtual Threads (JEP 480)、Structured Concurrency (JEP 499) 和 Scoped Values (JEP 501) 三大预览特性。提供并行执行、批量处理、异步管线、结构化并发、调度作用域、限速执行等能力，零第三方依赖。

## 2. 包结构

```
cloud.opencode.base.parallel
├── OpenParallel.java                # 并行计算门面
├── OpenStructured.java              # 结构化并发门面 (JEP 499)
│
├── executor/                        # 执行器
│   ├── VirtualExecutor.java         # 虚拟线程执行器
│   ├── RateLimitedExecutor.java     # 限速执行器(令牌桶)
│   └── ExecutorConfig.java          # 执行器配置
│
├── structured/                      # 结构化并发 (JEP 499/501)
│   ├── StructuredScope.java         # 结构化作用域封装
│   ├── ScheduledScope.java          # 调度作用域(延迟/周期任务)
│   ├── TaskResult.java              # 任务结果封装
│   └── ScopedContext.java           # ScopedValue 上下文管理 (JEP 501)
│
├── pipeline/                        # 异步管线
│   ├── AsyncPipeline.java           # 异步处理管线
│   └── TriFunction.java             # 三参数函数接口
│
├── batch/                           # 批量处理
│   ├── BatchProcessor.java          # 批量处理器
│   └── PartitionUtil.java           # 列表分区工具
│
└── exception/                       # 异常
    └── OpenParallelException.java   # 并行执行异常
```

## 3. 核心 API

### 3.1 OpenParallel

> 并行计算统一门面，提供并行执行、批量处理、异步编排等静态方法。基于 Virtual Threads 实现。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static void runAll(Runnable... tasks)` | 并行执行所有任务(无返回值) |
| `static void runAll(Collection<Runnable> tasks)` | 并行执行任务集合 |
| `static void runAll(Collection<Runnable> tasks, Duration timeout)` | 带超时的并行执行 |
| `static <T> List<T> invokeAll(Supplier<T>... suppliers)` | 并行执行并收集所有结果 |
| `static <T> List<T> invokeAll(Collection<Supplier<T>> suppliers)` | 并行执行供应者集合 |
| `static <T> List<T> invokeAll(Collection<Supplier<T>> suppliers, Duration timeout)` | 带超时的并行调用 |
| `static <T> T invokeAny(Supplier<T>... suppliers)` | 返回最先完成的结果 |
| `static <T> T invokeAny(Collection<Supplier<T>> suppliers)` | 返回最先完成的结果 |
| `static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper)` | 并行映射 |
| `static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper, int concurrency)` | 限制并发数的并行映射 |
| `static <T, R> List<R> parallelMap(List<T> items, Function<T, R> mapper, int concurrency, Duration timeout)` | 带超时和并发限制的并行映射 |
| `static <T> void processBatch(List<T> items, int batchSize, Consumer<List<T>> processor)` | 批量处理 |
| `static <T, R> List<R> processBatchAndCollect(List<T> items, int batchSize, Function<List<T>, List<R>> processor)` | 批量处理并收集结果 |
| `static <T> AsyncPipeline<T> pipeline(Supplier<T> initial)` | 创建异步管线 |
| `static <T> AsyncPipeline<T> pipeline(CompletableFuture<T> future)` | 从 Future 创建管线 |
| `static <T1, T2, R> CompletableFuture<R> combine(Supplier<T1> s1, Supplier<T2> s2, BiFunction<T1, T2, R> combiner)` | 组合两个异步结果 |
| `static <T1, T2, T3, R> CompletableFuture<R> combine(Supplier<T1> s1, Supplier<T2> s2, Supplier<T3> s3, TriFunction<T1, T2, T3, R> combiner)` | 组合三个异步结果 |
| `static <T> CompletableFuture<T> async(Supplier<T> supplier)` | 异步执行(Virtual Thread) |
| `static CompletableFuture<Void> async(Runnable runnable)` | 异步执行无返回值 |
| `static <T> CompletableFuture<T> delay(Duration delay, Supplier<T> supplier)` | 延迟执行 |
| `static ExecutorService getExecutor()` | 获取共享虚拟线程执行器 |
| `static RateLimitedExecutor rateLimited(double permitsPerSecond)` | 创建限速执行器 |
| `static RateLimitedExecutor rateLimited(double permitsPerSecond, long burstCapacity)` | 创建带突发容量的限速执行器 |
| `static <T> List<T> invokeAllRateLimited(double permitsPerSecond, Collection<Supplier<T>> suppliers)` | 限速并行调用 |
| `static <T> ScheduledScope<T> scheduledScope()` | 创建调度作用域 |
| `static <T> ScheduledScope<T> scheduledScope(Duration timeout)` | 带超时的调度作用域 |
| `static <T> T invokeDelayed(Duration delay, Supplier<T> task)` | 延迟调用 |
| `static <T> List<T> invokePeriodic(Duration interval, int count, Supplier<T> task)` | 周期性调用 |

**示例:**

```java
// 并行执行多个任务
OpenParallel.runAll(
    () -> sendEmail(user),
    () -> updateCache(user),
    () -> logActivity(user)
);

// 并行获取多个结果
List<String> results = OpenParallel.invokeAll(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB(),
    () -> fetchFromServiceC()
);

// 限制并发数的并行映射
List<Result> processed = OpenParallel.parallelMap(
    items, item -> process(item), 10);

// 异步管线
String result = OpenParallel.pipeline(() -> fetchData())
    .then(data -> transform(data))
    .then(data -> enrich(data))
    .get();

// 组合多个异步结果
CompletableFuture<Report> report = OpenParallel.combine(
    () -> fetchUsers(),
    () -> fetchOrders(),
    (users, orders) -> buildReport(users, orders)
);
```

### 3.2 OpenStructured

> 结构化并发门面 (JDK 25 JEP 499)，确保子任务生命周期绑定到父任务。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <T> List<T> invokeAll(List<Callable<T>> tasks)` | 结构化并行执行所有任务 |
| `static <T> List<T> invokeAll(List<Callable<T>> tasks, Duration timeout)` | 带超时的结构化执行 |
| `static <T, U, R> R parallel(Callable<T> task1, Callable<U> task2, BiFunction<T, U, R> combiner)` | 并行两个任务并组合 |
| `static <T, U, V, R> R parallel(Callable<T> task1, Callable<U> task2, Callable<V> task3, TriFunction<T, U, V, R> combiner)` | 并行三个任务并组合 |
| `static <T> T invokeAny(List<Callable<T>> tasks)` | 返回最先完成的结果 |
| `static <T> T invokeAny(List<Callable<T>> tasks, Duration timeout)` | 带超时的竞争执行 |
| `static <T> T race(Callable<T>... tasks)` | 竞争执行(语法糖) |
| `static <T, R> R runWithContext(T contextValue, Function<T, R> action)` | 带上下文执行 |
| `static <T, R> List<R> runAllWithContext(T contextValue, List<Function<T, R>> actions)` | 带上下文并行执行 |
| `static <T> StructuredScope<T> scope()` | 创建 shutdownOnFailure 作用域 |
| `static <T> StructuredScope<T> scopeAny()` | 创建 shutdownOnSuccess 作用域 |

**示例:**

```java
// 结构化并行执行
List<String> results = OpenStructured.invokeAll(List.of(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB(),
    () -> fetchFromServiceC()
));

// 并行两个任务并组合结果
Result result = OpenStructured.parallel(
    () -> fetchUser(userId),
    () -> fetchOrders(userId),
    (user, orders) -> new Result(user, orders)
);

// 竞争执行，返回最快结果
String fastest = OpenStructured.race(
    () -> fetchFromPrimary(),
    () -> fetchFromSecondary()
);
```

### 3.3 StructuredScope

> 结构化并发作用域封装 (JDK 25 JEP 499)，支持 shutdownOnFailure 和 shutdownOnSuccess 两种策略。

**策略枚举:**

| 值 | 描述 |
|------|------|
| `SHUTDOWN_ON_FAILURE` | 任一子任务失败则关闭所有(默认) |
| `SHUTDOWN_ON_SUCCESS` | 任一子任务成功则关闭所有 |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <T> StructuredScope<T> shutdownOnFailure()` | 创建失败即关闭作用域 |
| `static <T> StructuredScope<T> shutdownOnSuccess()` | 创建成功即关闭作用域 |
| `StructuredScope<T> fork(Callable<T> task)` | fork 一个子任务 |
| `StructuredScope<T> forkAll(Callable<T>... tasks)` | fork 多个子任务 |
| `StructuredScope<T> forkAll(Iterable<? extends Callable<T>> tasks)` | fork 可迭代任务 |
| `List<T> joinAll()` | 等待所有任务完成并收集结果 |
| `List<T> joinAll(Duration timeout)` | 带超时等待 |
| `T joinAny()` | 等待任一任务完成 |
| `T joinAndReduce(T identity, BiFunction<T, T, T> reducer)` | 等待并归约 |
| `List<TaskResult<T>> joinAsResults()` | 等待并收集带状态的结果 |
| `int getTaskCount()` | 获取任务数量 |
| `Policy getPolicy()` | 获取策略 |

**示例:**

```java
List<String> results = StructuredScope.<String>shutdownOnFailure()
    .fork(() -> fetchFromDb())
    .fork(() -> fetchFromCache())
    .fork(() -> fetchFromApi())
    .joinAll(Duration.ofSeconds(5));
```

### 3.4 ScheduledScope

> 调度结构化并发作用域 (JDK 25 JEP 499)，支持延迟执行、周期任务、截止时间。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <T> ScheduledScope<T> create()` | 创建无截止时间的作用域 |
| `static <T> ScheduledScope<T> withDeadline(Instant deadline)` | 带截止时间 |
| `static <T> ScheduledScope<T> withTimeout(Duration timeout)` | 带超时 |
| `static <T> Builder<T> builder()` | 创建构建器 |
| `ScheduledScope<T> fork(Callable<T> task)` | fork 立即执行的任务 |
| `ScheduledScope<T> forkAll(Callable<T>... tasks)` | fork 多个任务 |
| `ScheduledScope<T> forkDelayed(Duration delay, Callable<T> task)` | 延迟 fork |
| `ScheduledScope<T> forkAt(Instant instant, Callable<T> task)` | 定时 fork |
| `ScheduledScope<T> forkPeriodic(Duration period, int count, Callable<T> task)` | 周期 fork |
| `ScheduledScope<T> forkPeriodicUntil(Duration period, Instant deadline, Callable<T> task)` | 周期 fork 直到截止 |
| `ScheduledScope<T> forkWithFixedDelay(Duration delay, int count, Callable<T> task)` | 固定延迟 fork |
| `List<T> joinAll()` | 等待所有结果 |
| `List<T> joinAll(Duration timeout)` | 带超时等待 |
| `T joinAndReduce(T identity, BiFunction<T, T, T> reducer)` | 等待并归约 |
| `List<TaskResult<T>> joinAsResults()` | 等待并收集带状态的结果 |
| `int getTaskCount()` | 任务总数 |
| `int getPendingScheduledCount()` | 待调度任务数 |
| `Instant getDeadline()` | 截止时间 |
| `Duration getRemainingTime()` | 剩余时间 |
| `boolean isDeadlinePassed()` | 是否已超时 |

**示例:**

```java
// 延迟和周期任务
ScheduledScope<String> scope = ScheduledScope.withTimeout(Duration.ofMinutes(5));
scope.fork(() -> fetchImmediate())
     .forkDelayed(Duration.ofSeconds(2), () -> fetchDelayed())
     .forkPeriodic(Duration.ofSeconds(10), 3, () -> pollStatus());
List<String> results = scope.joinAll();
```

### 3.5 TaskResult

> 结构化任务结果封装，包含成功值、异常或取消状态。

**状态枚举:**

| 值 | 描述 |
|------|------|
| `SUCCESS` | 成功完成 |
| `FAILURE` | 执行异常 |
| `CANCELLED` | 被取消 |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <T> TaskResult<T> success(T value)` | 创建成功结果 |
| `static <T> TaskResult<T> failure(Throwable exception)` | 创建失败结果 |
| `static <T> TaskResult<T> cancelled()` | 创建取消结果 |
| `static <T> TaskResult<T> of(Callable<T> callable)` | 执行并封装结果 |
| `boolean isSuccess()` | 是否成功 |
| `boolean isFailure()` | 是否失败 |
| `boolean isCancelled()` | 是否取消 |
| `State getState()` | 获取状态 |
| `T get()` | 获取值(失败抛异常) |
| `T getOrNull()` | 获取值或 null |
| `T getOrDefault(T defaultValue)` | 获取值或默认值 |
| `T getOrElse(Function<Throwable, T> mapper)` | 获取值或映射异常 |
| `Optional<T> toOptional()` | 转为 Optional |
| `<R> TaskResult<R> map(Function<T, R> mapper)` | 映射值 |
| `<R> TaskResult<R> flatMap(Function<T, TaskResult<R>> mapper)` | 扁平映射 |
| `TaskResult<T> recover(Function<Throwable, T> recovery)` | 异常恢复 |
| `TaskResult<T> ifSuccess(Consumer<T> action)` | 成功回调 |
| `TaskResult<T> ifFailure(Consumer<Throwable> action)` | 失败回调 |

**示例:**

```java
List<TaskResult<String>> results = scope.joinAsResults();
for (TaskResult<String> result : results) {
    String value = result.getOrDefault("fallback");
    result.ifFailure(e -> log.error("任务失败", e));
}
```

### 3.6 ScopedContext

> ScopedValue 上下文管理 (JDK 25 JEP 501)，替代 ThreadLocal，提供预定义的 traceId、userId、tenantId 等上下文传递。

**预定义 ScopedValue:**

| 字段 | 描述 |
|------|------|
| `ScopedValue<String> TRACE_ID` | 追踪 ID |
| `ScopedValue<String> USER_ID` | 用户 ID |
| `ScopedValue<String> TENANT_ID` | 租户 ID |
| `ScopedValue<String> REQUEST_ID` | 请求 ID |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static void runWithTraceId(String traceId, Runnable task)` | 携带 traceId 执行 |
| `static <T> T callWithTraceId(String traceId, Callable<T> task)` | 携带 traceId 调用 |
| `static String getTraceId()` | 获取当前 traceId |
| `static String getTraceIdOrDefault(String defaultValue)` | 获取 traceId 或默认值 |
| `static void runWithUserId(String userId, Runnable task)` | 携带 userId 执行 |
| `static String getUserId()` | 获取当前 userId |
| `static void runWithTenantId(String tenantId, Runnable task)` | 携带 tenantId 执行 |
| `static String getTenantId()` | 获取当前 tenantId |
| `static void run(String traceId, String userId, Runnable task)` | 多上下文执行 |
| `static <T> T call(String traceId, String userId, Callable<T> task)` | 多上下文调用 |
| `static <T> ScopedValue<T> newScopedValue()` | 创建自定义 ScopedValue |
| `static <T> void runWith(ScopedValue<T> scopedValue, T value, Runnable task)` | 通用上下文执行 |
| `static <T> boolean isBound(ScopedValue<T> scopedValue)` | 是否已绑定 |

**示例:**

```java
// 携带上下文执行
ScopedContext.runWithTraceId("trace-123", () -> {
    String traceId = ScopedContext.getTraceId(); // "trace-123"
    // 在 Virtual Thread 和结构化并发中自动传递
});

// 多上下文
ScopedContext.run("trace-123", "user-456", () -> {
    String traceId = ScopedContext.getTraceId();
    String userId = ScopedContext.getUserId();
});
```

### 3.7 VirtualExecutor

> 虚拟线程执行器，支持并发数限制、任务超时、提交统计。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static VirtualExecutor shared()` | 获取共享执行器 |
| `static VirtualExecutor create()` | 创建新执行器 |
| `static VirtualExecutor withConcurrency(int maxConcurrency)` | 创建限制并发数的执行器 |
| `static VirtualExecutor withConfig(ExecutorConfig config)` | 使用配置创建 |
| `CompletableFuture<Void> submit(Runnable task)` | 提交任务 |
| `<T> CompletableFuture<T> submit(Callable<T> task)` | 提交有返回值任务 |
| `<T> List<T> invokeAll(Collection<? extends Callable<T>> tasks)` | 执行所有任务 |
| `<T> List<T> invokeAll(Collection<? extends Callable<T>> tasks, Duration timeout)` | 带超时执行所有 |
| `Future<?> execute(Runnable task)` | 执行任务返回 Future |
| `long getSubmittedCount()` | 已提交任务数 |
| `long getCompletedCount()` | 已完成任务数 |
| `long getFailedCount()` | 失败任务数 |
| `long getPendingCount()` | 待执行任务数 |
| `int getAvailablePermits()` | 可用并发许可 |
| `ExecutorConfig getConfig()` | 获取配置 |
| `void shutdown()` | 关闭执行器 |
| `boolean shutdownAndAwait(Duration timeout)` | 关闭并等待 |

**示例:**

```java
// 限制并发数
VirtualExecutor executor = VirtualExecutor.withConcurrency(10);

List<String> results = executor.invokeAll(List.of(
    () -> fetchA(),
    () -> fetchB(),
    () -> fetchC()
));

executor.close();
```

### 3.8 RateLimitedExecutor

> 令牌桶限速执行器，控制任务提交吞吐量。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static RateLimitedExecutor create(double permitsPerSecond)` | 创建限速执行器 |
| `static RateLimitedExecutor create(double permitsPerSecond, long burstCapacity)` | 带突发容量 |
| `static Builder builder()` | 创建构建器 |
| `CompletableFuture<Void> submit(Runnable task)` | 限速提交任务 |
| `<T> CompletableFuture<T> submit(Callable<T> task)` | 限速提交有返回值任务 |
| `<T> CompletableFuture<T> submit(Callable<T> task, Duration timeout)` | 带超时限速提交 |
| `<T> Optional<CompletableFuture<T>> trySubmit(Callable<T> task)` | 尝试提交(不阻塞) |
| `<T> List<T> invokeAll(Collection<? extends Callable<T>> tasks)` | 限速批量执行 |
| `<T> List<T> invokeAll(Collection<? extends Callable<T>> tasks, Duration timeout)` | 带超时限速批量执行 |
| `void acquire()` | 手动获取令牌(阻塞) |
| `boolean tryAcquire()` | 尝试获取令牌 |
| `boolean tryAcquire(Duration timeout)` | 带超时获取令牌 |
| `long getSubmittedCount()` | 提交数 |
| `long getCompletedCount()` | 完成数 |
| `long getRejectedCount()` | 拒绝数 |
| `double getPermitsPerSecond()` | 每秒许可数 |
| `double getAvailablePermits()` | 可用许可 |
| `void shutdown()` | 关闭 |

**示例:**

```java
// 每秒 100 个请求
RateLimitedExecutor executor = RateLimitedExecutor.create(100);

for (String url : urls) {
    executor.submit(() -> httpClient.get(url));
}

executor.close();
```

### 3.9 ExecutorConfig

> 执行器配置，控制命名前缀、最大并发、任务超时等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static ExecutorConfig defaults()` | 默认配置 |
| `static Builder builder()` | 创建构建器 |
| `String getNamePrefix()` | 线程名前缀 |
| `int getMaxConcurrency()` | 最大并发数 |
| `Duration getTaskTimeout()` | 任务超时 |
| `boolean isInheritThreadLocals()` | 是否继承 ThreadLocal |
| `Thread.UncaughtExceptionHandler getUncaughtExceptionHandler()` | 未捕获异常处理器 |

**示例:**

```java
ExecutorConfig config = ExecutorConfig.builder()
    .namePrefix("worker-")
    .maxConcurrency(20)
    .taskTimeout(Duration.ofSeconds(30))
    .build();
VirtualExecutor executor = VirtualExecutor.withConfig(config);
```

### 3.10 AsyncPipeline

> 异步处理管线，支持链式异步操作、错误处理、组合操作。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <T> AsyncPipeline<T> of(CompletableFuture<T> future)` | 从 Future 创建 |
| `static <T> AsyncPipeline<T> completed(T value)` | 从已完成值创建 |
| `static <T> AsyncPipeline<T> failed(Throwable error)` | 从异常创建 |
| `<R> AsyncPipeline<R> then(Function<T, R> fn)` | 同步转换 |
| `<R> AsyncPipeline<R> thenAsync(Function<T, CompletableFuture<R>> fn)` | 异步转换 |
| `AsyncPipeline<T> peek(Consumer<T> action)` | 窥视(不改变值) |
| `AsyncPipeline<T> filter(Predicate<T> predicate)` | 过滤(不满足返回 null) |
| `AsyncPipeline<T> onError(Function<Throwable, T> handler)` | 错误恢复 |
| `AsyncPipeline<T> onErrorAsync(Function<Throwable, CompletableFuture<T>> handler)` | 异步错误恢复 |
| `<R> AsyncPipeline<R> handle(BiFunction<T, Throwable, R> handler)` | 处理值和异常 |
| `<U, R> AsyncPipeline<R> combine(AsyncPipeline<U> other, BiFunction<T, U, R> combiner)` | 组合另一个管线 |
| `AsyncPipeline<T> runAfter(AsyncPipeline<?> other)` | 在另一个管线之后执行 |
| `T get()` | 阻塞获取结果 |
| `T get(Duration timeout)` | 带超时获取 |
| `T getOrDefault(T defaultValue)` | 获取或返回默认值 |
| `Optional<T> getOptional()` | 获取 Optional 结果 |
| `CompletableFuture<T> toFuture()` | 获取底层 Future |
| `boolean isDone()` | 是否完成 |
| `boolean cancel(boolean mayInterruptIfRunning)` | 取消执行 |

**示例:**

```java
String result = AsyncPipeline.of(CompletableFuture.supplyAsync(() -> fetchRawData()))
    .then(data -> parse(data))
    .then(parsed -> enrich(parsed))
    .onError(e -> getDefaultData())
    .peek(data -> log.info("处理完成: {}", data))
    .get(Duration.ofSeconds(10));
```

### 3.11 BatchProcessor

> 并行批量处理器，支持分批执行、进度回调、错误控制。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static Builder builder()` | 创建构建器 |
| `static BatchProcessor defaultProcessor()` | 创建默认处理器 |
| `<T> void process(List<T> items, Consumer<List<T>> processor)` | 批量处理 |
| `<T, R> List<R> processAndCollect(List<T> items, Function<List<T>, List<R>> processor)` | 批量处理并收集结果 |
| `<T> void processWithProgress(List<T> items, Consumer<List<T>> processor, Consumer<BatchProgress> progressCallback)` | 带进度回调处理 |
| `int getBatchSize()` | 获取批次大小 |
| `int getParallelism()` | 获取并行度 |
| `void close()` | 关闭 |

**BatchProgress 记录:**

| 方法 | 描述 |
|------|------|
| `int completedBatches()` | 已完成批次数 |
| `int totalBatches()` | 总批次数 |
| `int percentage()` | 完成百分比 |
| `boolean isComplete()` | 是否全部完成 |

**Builder 方法:**

| 方法 | 描述 |
|------|------|
| `Builder batchSize(int batchSize)` | 设置批次大小 |
| `Builder parallelism(int parallelism)` | 设置并行度 |
| `Builder timeout(Duration timeout)` | 设置超时 |
| `Builder stopOnError(boolean stopOnError)` | 出错即停止 |

**示例:**

```java
BatchProcessor processor = BatchProcessor.builder()
    .batchSize(100)
    .parallelism(4)
    .timeout(Duration.ofMinutes(5))
    .stopOnError(true)
    .build();

processor.processWithProgress(users, batch -> {
    userService.batchUpdate(batch);
}, progress -> {
    System.out.printf("进度: %d%%\n", progress.percentage());
});
```

### 3.12 PartitionUtil

> 列表分区工具，将列表按指定大小分割为子列表。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <T> List<List<T>> partition(List<T> list, int size)` | 按大小分区 |
| `static <T> List<List<T>> partition(Collection<T> collection, int size)` | 集合分区 |
| `static <T> Stream<List<T>> partitionStream(List<T> list, int size)` | 分区流 |
| `static <T> List<List<T>> partitionInto(List<T> list, int count)` | 分为指定数量的组 |
| `static int partitionCount(int totalSize, int partitionSize)` | 计算分区数量 |
| `static <T> List<T> getPartition(List<T> list, int partitionSize, int partitionIndex)` | 获取指定分区 |

**示例:**

```java
List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7);
List<List<Integer>> partitions = PartitionUtil.partition(list, 3);
// [[1, 2, 3], [4, 5, 6], [7]]

// 分为 3 组
List<List<Integer>> groups = PartitionUtil.partitionInto(list, 3);
```

### 3.13 TriFunction

> 三参数函数接口，用于 `combine` 等组合操作。

```java
TriFunction<String, Integer, Boolean, String> fn =
    (name, age, active) -> name + "(" + age + ")" + (active ? "活跃" : "");
String result = fn.apply("张三", 25, true);
```

### 3.14 OpenParallelException

> 并行执行异常，包含抑制异常列表、失败计数等信息。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `OpenParallelException(String message)` | 构造异常 |
| `OpenParallelException(String message, Throwable cause)` | 带原因构造 |
| `OpenParallelException(String message, List<Throwable> suppressed, int totalCount)` | 带抑制异常列表 |
| `List<Throwable> getSuppressedExceptions()` | 获取抑制异常列表 |
| `int getFailedCount()` | 获取失败数量 |
| `int getTotalCount()` | 获取总数量 |
| `int getSuccessCount()` | 获取成功数量 |
| `static OpenParallelException timeout(Duration timeout)` | 创建超时异常 |
| `static OpenParallelException interrupted(InterruptedException cause)` | 创建中断异常 |
| `static OpenParallelException partialFailure(List<Throwable> failures, int totalCount)` | 部分失败异常 |
| `static OpenParallelException allFailed(List<Throwable> failures)` | 全部失败异常 |

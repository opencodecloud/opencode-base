# Resilience 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-resilience` 模块提供应用弹性能力，整合重试、限流、熔断、超时、舱壁隔离、对冲请求、请求合并、降级策略等弹性模式，帮助构建高可用的分布式系统。基于 JDK 25 原生特性，零第三方依赖。

### 1.2 核心特性

- **重试机制**：4种退避策略（固定、线性、指数、随机）、可配置重试/中止条件、异步重试、监听器
- **限流控制**：4种算法（令牌桶、漏桶、滑动窗口、固定窗口）、自适应限流
- **熔断保护**：滑动窗口故障检测、自动恢复、半开状态探测、分布式熔断器
- **超时控制**：统一超时管理、带降级的超时
- **舱壁隔离**：信号量隔离、并发控制
- **对冲请求**：延迟对冲、百分位对冲、自适应对冲、延迟跟踪
- **请求合并**：批量合并、窗口聚合、去重
- **降级策略**：Sealed 类型安全降级（静态值、缓存、降级服务、链式、熔断器感知）
- **组合装饰器**：流式API组合多种弹性策略
- **JDK 25 原生**：Virtual Thread 执行、结构化并发、ScopedValue 上下文传播
- **健康检查**：弹性组件健康状态监控
- **指标收集**：成功率、延迟、重试次数、限流统计
- **SPI 扩展**：可插拔的熔断器、退避策略、限流算法、指标收集

### 1.3 架构概览

```
+--------------------------------------------------------------------------+
|                       OpenResilience (统一门面)                             |
|     retry() / rateLimit() / circuitBreak() / timeout() / executor()       |
+--------------------------------------------------------------------------+
|                            弹性策略层                                       |
|  +-----------+ +-----------+ +-----------+ +-----------+ +-------------+  |
|  |  Retry    | | RateLimt  | | CircuitBk | | Bulkhead  | |  Timeout    |  |
|  |  重试机制  | | 限流控制   | | 熔断保护   | | 舱壁隔离   | |  超时控制   |  |
|  +-----------+ +-----------+ +-----------+ +-----------+ +-------------+  |
+--------------------------------------------------------------------------+
|                            算法层                                          |
|  +--------------------------------------------------------------------+  |
|  | Retry            | RateLimiter        | CircuitBreaker              |  |
|  | +- Exponential   | +- TokenBucket     | +- SlidingWindow            |  |
|  | +- Linear        | +- LeakyBucket     | +- CountBased               |  |
|  | +- Fixed         | +- SlidingWindow   | +- Distributed              |  |
|  | +- Random        | +- FixedWindow     |                             |  |
|  |                  | +- Adaptive        |                             |  |
|  +--------------------------------------------------------------------+  |
+--------------------------------------------------------------------------+
|                          高级特性                                          |
|  +------------------+ +------------------+ +----------------------------+ |
|  | Decorators       | | HedgedRequest    | | FallbackStrategy (Sealed)  | |
|  | 组合装饰器        | | 对冲请求          | | 降级策略                    | |
|  +------------------+ +------------------+ +----------------------------+ |
|  +------------------+ +------------------+ +----------------------------+ |
|  | RequestCollapser | | ReactiveResil.   | | ResilienceHealthCheck      | |
|  | 请求合并          | | 响应式弹性        | | 健康检查                    | |
|  +------------------+ +------------------+ +----------------------------+ |
+--------------------------------------------------------------------------+
|                       监控 & 上下文 & SPI                                  |
|  +--------------------------------------------------------------------+  |
|  | ResilienceMetrics (指标) | ResilienceContext (ScopedValue) | SPI    |  |
|  +--------------------------------------------------------------------+  |
+--------------------------------------------------------------------------+
|                JDK 25 Virtual Threads / Structured Concurrency             |
+--------------------------------------------------------------------------+
```

### 1.4 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-resilience</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.resilience
|-- OpenResilience.java                    // 统一门面
|-- ResilienceConfig.java                  // 统一配置（record）
|-- ResilienceStrategy.java                // 弹性策略枚举
|-- VirtualThreadResilience.java           // Virtual Thread弹性执行
|-- OpenResilienceExceptionHandler.java    // 异常处理（模式匹配）
|
|-- retry/                                 // 重试模块
|   |-- RetryConfig.java                  // 重试配置（record）
|   |-- Retryer.java                      // 重试器
|   |-- OpenRetry.java                    // 重试入口
|   |-- BackoffStrategy.java              // 退避策略枚举
|   |-- RetryListener.java                // 重试监听器接口
|   |-- LoggingRetryListener.java         // 日志监听器
|   |-- MetricsRetryListener.java         // 指标监听器
|   +-- exception/
|       |-- OpenRetryException.java       // 重试异常
|       +-- OpenRetryExhaustedException.java // 重试耗尽异常
|
|-- ratelimit/                             // 限流模块
|   |-- RateLimiter.java                  // 限流器接口
|   |-- RateLimitConfig.java              // 限流配置（record）
|   |-- OpenRateLimiter.java              // 限流入口
|   |-- algorithm/
|   |   |-- TokenBucketLimiter.java       // 令牌桶
|   |   |-- LeakyBucketLimiter.java       // 漏桶
|   |   |-- SlidingWindowLimiter.java     // 滑动窗口
|   |   |-- FixedWindowLimiter.java       // 固定窗口
|   |   |-- AdaptiveRateLimiter.java      // 自适应限流器
|   |   +-- SystemLoadMonitor.java        // 系统负载监控
|   +-- exception/
|       +-- OpenRateLimitException.java   // 限流异常
|
|-- circuitbreaker/                        // 熔断模块
|   |-- CircuitBreaker.java               // 熔断器接口
|   |-- CircuitBreakerConfig.java         // 熔断配置（record）
|   |-- CircuitState.java                 // 熔断状态枚举
|   |-- CircuitBreakerMetrics.java        // 熔断指标（record）
|   |-- OpenCircuitBreaker.java           // 熔断入口
|   |-- DefaultCircuitBreaker.java        // 默认实现
|   |-- DistributedCircuitBreaker.java    // 分布式熔断器接口
|   |-- DistributedCircuitBreakerFactory.java // 分布式熔断器工厂
|   |-- DistributedCircuitBreakerProvider.java // SPI提供者
|   |-- LocalCircuitBreakerAdapter.java   // 本地适配器
|   |-- ClusterMetrics.java              // 集群指标（record）
|   |-- StateChangeListener.java          // 状态变更监听器
|   +-- exception/
|       +-- OpenCircuitBreakerException.java // 熔断异常
|
|-- timeout/                               // 超时模块
|   |-- TimeoutConfig.java                // 超时配置（record）
|   +-- TimeoutUtil.java                  // 超时工具
|
|-- bulkhead/                              // 舱壁模块
|   |-- Bulkhead.java                     // 舱壁接口
|   |-- BulkheadConfig.java              // 舱壁配置（record）
|   +-- SemaphoreBulkhead.java            // 信号量舱壁
|
|-- collapser/                             // 请求合并
|   +-- RequestCollapser.java             // 请求合并器
|
|-- hedging/                               // 对冲请求
|   |-- HedgedRequest.java                // 对冲请求执行器
|   +-- LatencyTracker.java               // 延迟跟踪器
|
|-- fallback/                              // 降级策略
|   |-- FallbackStrategy.java             // 降级策略（sealed接口）
|   |-- FallbackStrategies.java           // 降级策略工厂
|   |-- StaticFallback.java              // 静态值降级
|   |-- CacheFallback.java              // 缓存降级
|   |-- DegradedFallback.java            // 降级服务
|   |-- ChainedFallback.java            // 链式降级
|   +-- CircuitBreakerFallback.java      // 熔断器感知降级
|
|-- failure/                               // 失败类型
|   |-- ResilienceFailure.java            // 弹性失败（sealed接口）
|   |-- RetryFailure.java                // 重试失败
|   |-- RateLimitFailure.java            // 限流失败
|   |-- CircuitBreakerFailure.java       // 熔断失败
|   |-- TimeoutFailure.java             // 超时失败
|   +-- BulkheadFailure.java            // 舱壁失败
|
|-- decorator/                             // 装饰器
|   |-- Decorators.java                   // 流式组合API
|   |-- ResilientExecutor.java            // 弹性执行器
|   |-- ResilientSupplier.java            // 弹性Supplier
|   +-- ResilientRunnable.java            // 弹性Runnable
|
|-- reactive/                              // 响应式弹性
|   +-- ReactiveResilience.java           // 非阻塞弹性工具
|
|-- context/                               // 上下文
|   |-- ResilienceContext.java            // ScopedValue上下文
|   +-- ContextAwareResilientExecutor.java // 上下文感知执行器
|
|-- metrics/                               // 指标
|   |-- ResilienceMetrics.java            // 指标接口
|   |-- DefaultResilienceMetrics.java     // 默认实现
|   |-- MetricsSnapshot.java             // 指标快照（record）
|   +-- ObservabilityMetricsAdapter.java  // 可观测性适配器
|
|-- health/                                // 健康检查
|   +-- ResilienceHealthCheck.java        // 弹性健康检查
|
|-- spi/                                   // SPI扩展
|   |-- ResilienceSPI.java                // SPI加载器
|   |-- CircuitBreakerProvider.java       // 熔断器提供者
|   |-- BackoffStrategyProvider.java      // 退避策略提供者
|   |-- RateLimitAlgorithmProvider.java   // 限流算法提供者
|   |-- ResilienceMetricsProvider.java    // 指标提供者
|   +-- CircuitBreakerEventListener.java  // 熔断器事件监听器
|
+-- exception/
    +-- OpenResilienceException.java      // 弹性异常基类
```

---

## 3. 统一门面 - OpenResilience

提供所有弹性操作的统一入口。

```java
// 重试
String result = OpenResilience.retry(() -> fetchFromApi());
String result = OpenResilience.retry(() -> fetchFromApi(), retryConfig);
CompletableFuture<String> future = OpenResilience.retryAsync(() -> fetchFromApi(), retryConfig);

// 限流
RateLimiter limiter = OpenResilience.rateLimiter("api", config);
String result = OpenResilience.rateLimit("api", config, () -> callApi());
String result = OpenResilience.rateLimitWithFallback("api", config, () -> callApi(), () -> "fallback");

// 熔断
CircuitBreaker cb = OpenResilience.circuitBreaker("service", config);
String result = OpenResilience.circuitBreak("service", config, () -> callService());

// 超时
String result = OpenResilience.timeout(() -> slowOp(), Duration.ofSeconds(5));
String result = OpenResilience.timeoutWithFallback(() -> slowOp(), Duration.ofSeconds(5), "default");

// 组合执行器
String result = OpenResilience.executor()
    .rateLimit("api", rateLimitConfig)
    .circuitBreaker("service", cbConfig)
    .timeout(Duration.ofSeconds(5))
    .retry(retryConfig)
    .execute(() -> callService());

// 装饰器
Supplier<String> decorated = OpenResilience.decorateSupplier(() -> callService())
    .withRetry(retryConfig)
    .withCircuitBreaker("service", cbConfig)
    .withFallback("default")
    .decorate();

OpenResilience.decorateRunnable(() -> sendEmail())
    .withRetry(retryConfig)
    .run();
```

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `retry(Supplier<T> action)` | `action` - 操作 | `T` | 默认配置重试执行 |
| `retry(Supplier<T> action, RetryConfig config)` | `action` - 操作；`config` - 配置 | `T` | 自定义配置重试执行 |
| `retryAsync(Supplier<T> action, RetryConfig config)` | `action` - 操作；`config` - 配置 | `CompletableFuture<T>` | 异步重试 |
| `rateLimiter(String key, RateLimitConfig config)` | `key` - 限流键；`config` - 配置 | `RateLimiter` | 获取限流器 |
| `rateLimit(String key, RateLimitConfig config, Supplier<T> action)` | key、配置、操作 | `T` | 限流执行 |
| `rateLimitWithFallback(String key, RateLimitConfig config, Supplier<T> action, Supplier<T> fallback)` | key、配置、操作、降级 | `T` | 带降级的限流执行 |
| `circuitBreaker(String key, CircuitBreakerConfig config)` | `key` - 熔断键；`config` - 配置 | `CircuitBreaker` | 获取熔断器 |
| `circuitBreak(String key, CircuitBreakerConfig config, Supplier<T> action)` | key、配置、操作 | `T` | 熔断执行 |
| `timeout(Supplier<T> action, Duration timeout)` | `action` - 操作；`timeout` - 超时 | `T` | 带超时执行 |
| `timeoutWithFallback(Supplier<T> action, Duration timeout, T fallback)` | 操作、超时、降级值 | `T` | 带降级的超时执行 |
| `executor()` | - | `ResilientExecutor` | 创建组合执行器 |
| `decorateSupplier(Supplier<T> supplier)` | `supplier` - 操作 | `DecoratorBuilder<T>` | 创建Supplier装饰器 |
| `decorateRunnable(Runnable runnable)` | `runnable` - 操作 | `RunnableDecoratorBuilder` | 创建Runnable装饰器 |

---

## 4. 重试模块

### 4.1 RetryConfig - 重试配置

```java
public record RetryConfig(
    int maxAttempts,                     // 最大尝试次数（默认3）
    Duration initialDelay,               // 初始延迟（默认100ms）
    Duration maxDelay,                   // 最大延迟（默认10s）
    double multiplier,                   // 退避乘数（默认2.0）
    BackoffStrategy backoffStrategy,     // 退避策略（默认EXPONENTIAL）
    Predicate<Throwable> retryOn,        // 重试条件（默认所有异常）
    Predicate<Throwable> abortOn         // 中止条件（默认无）
)
```

```java
// 默认配置
RetryConfig config = RetryConfig.defaults();

// 自定义配置
RetryConfig config = RetryConfig.builder()
    .maxAttempts(5)
    .initialDelay(Duration.ofMillis(200))
    .maxDelay(Duration.ofSeconds(5))
    .multiplier(2.0)
    .backoff(BackoffStrategy.EXPONENTIAL)
    .retryOn(IOException.class, TimeoutException.class)
    .abortOn(IllegalArgumentException.class)
    .build();

// 自定义重试条件
RetryConfig config = RetryConfig.builder()
    .retryOn(e -> e instanceof IOException)
    .abortOn(e -> e instanceof SecurityException)
    .build();
```

### 4.2 BackoffStrategy - 退避策略

```java
public enum BackoffStrategy {
    FIXED,          // 固定间隔
    LINEAR,         // 线性增长
    EXPONENTIAL,    // 指数增长（默认）
    RANDOM          // 随机间隔（含Jitter）
}
```

每个策略实现 `calculateDelay(int attempt, Duration initialDelay, Duration maxDelay, double multiplier)` 方法。

### 4.3 OpenRetry - 重试入口

```java
// 简单重试
String result = OpenRetry.execute(() -> fetchData());

// 自定义配置
String result = OpenRetry.execute(() -> fetchData(), config);

// 无返回值
OpenRetry.run(() -> sendEmail());
OpenRetry.run(() -> sendEmail(), config);

// 异步重试
CompletableFuture<String> future = OpenRetry.executeAsync(() -> fetchData());
CompletableFuture<String> future = OpenRetry.executeAsync(() -> fetchData(), config);

// 获取Retryer（可添加监听器）
Retryer<String> retryer = OpenRetry.of(() -> fetchData(), config);
retryer.addListener(new LoggingRetryListener("fetchData"))
       .addListener(new MetricsRetryListener("fetchData"));
String result = retryer.execute();
```

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `execute(Supplier<T> action)` | `action` - 操作 | `T` | 默认配置重试 |
| `execute(Supplier<T> action, RetryConfig config)` | `action`；`config` | `T` | 自定义配置重试 |
| `run(Runnable action)` | `action` - 操作 | `void` | 无返回值重试 |
| `run(Runnable action, RetryConfig config)` | `action`；`config` | `void` | 自定义配置无返回值重试 |
| `executeAsync(Supplier<T> action)` | `action` | `CompletableFuture<T>` | 异步重试 |
| `executeAsync(Supplier<T> action, RetryConfig config)` | `action`；`config` | `CompletableFuture<T>` | 自定义异步重试 |
| `of(Supplier<T> action)` | `action` | `Retryer<T>` | 创建Retryer |
| `of(Supplier<T> action, RetryConfig config)` | `action`；`config` | `Retryer<T>` | 创建带配置的Retryer |

### 4.4 RetryListener - 重试监听器

```java
public interface RetryListener {
    default void onSuccess(int attempt) {}
    default void onRetry(int attempt, Throwable e, Duration delay) {}
    default void onAbort(int attempt, Throwable e) {}
    default void onExhausted(int attempt, Throwable e) {}
}
```

**内置监听器：**

- **LoggingRetryListener**：日志记录重试事件。构造方法 `LoggingRetryListener()`, `LoggingRetryListener(String operationName)`, `LoggingRetryListener(String operationName, Logger logger)`。
- **MetricsRetryListener**：收集重试指标。提供 `getTotalAttempts()`, `getSuccessCount()`, `getRetryCount()`, `getAbortCount()`, `getExhaustedCount()`, `getSuccessRate()`, `getAverageAttempts()`, `getAverageDelayMillis()`, `getFirstAttemptSuccessRate()`, `snapshot()` 等方法。

---

## 5. 限流模块

### 5.1 RateLimiter - 限流器接口

```java
public interface RateLimiter {
    boolean tryAcquire();                  // 尝试获取1个许可
    boolean tryAcquire(int permits);       // 尝试获取多个许可
    void acquire();                        // 阻塞获取许可
    boolean acquire(Duration timeout);     // 带超时阻塞获取
    long availablePermits();               // 可用许可数
    RateLimitConfig getConfig();           // 获取配置
}
```

### 5.2 RateLimitConfig - 限流配置

```java
public record RateLimitConfig(
    long permits,           // 许可数
    Duration period,        // 时间周期
    Algorithm algorithm     // 算法
)
```

**Algorithm 枚举：**

| 算法 | 说明 |
|------|------|
| `TOKEN_BUCKET` | 令牌桶（默认） |
| `LEAKY_BUCKET` | 漏桶 |
| `SLIDING_WINDOW` | 滑动窗口 |
| `FIXED_WINDOW` | 固定窗口 |

```java
// 快捷创建
RateLimitConfig config = RateLimitConfig.perSecond(100);
RateLimitConfig config = RateLimitConfig.perMinute(1000);
RateLimitConfig config = RateLimitConfig.perHour(10000);

// 指定算法
RateLimitConfig config = RateLimitConfig.of(1000, Duration.ofMinutes(1), Algorithm.SLIDING_WINDOW);

// 默认算法（TOKEN_BUCKET）
RateLimitConfig config = RateLimitConfig.of(100, Duration.ofSeconds(1));
```

### 5.3 OpenRateLimiter - 限流入口

```java
// 获取限流器（按key缓存）
RateLimiter limiter = OpenRateLimiter.get("api:/users", config);

// 创建限流器（不缓存）
RateLimiter limiter = OpenRateLimiter.create(config);

// 尝试获取许可
boolean ok = OpenRateLimiter.tryAcquire("api:/users");

// 限流执行（获取失败抛 OpenRateLimitException）
String result = OpenRateLimiter.execute("api:/users", config, () -> callApi());

// 带降级的限流执行
String result = OpenRateLimiter.executeWithFallback("api:/users", config,
    () -> callApi(), () -> "fallback");

// 管理
OpenRateLimiter.remove("api:/users");
OpenRateLimiter.clear();
```

### 5.4 限流算法实现

| 类 | 算法 | 特点 |
|-----|------|------|
| `TokenBucketLimiter` | 令牌桶 | 允许突发流量，均匀补充令牌 |
| `LeakyBucketLimiter` | 漏桶 | 固定速率处理，溢出拒绝 |
| `SlidingWindowLimiter` | 滑动窗口 | 精确的时间窗口计数 |
| `FixedWindowLimiter` | 固定窗口 | 简单高效，有边界突发问题 |
| `AdaptiveRateLimiter` | 自适应 | 根据系统负载动态调整容量 |

### 5.5 AdaptiveRateLimiter - 自适应限流器

根据系统负载（CPU、内存）动态调整限流容量。实现 `RateLimiter` 和 `AutoCloseable`。

```java
// 默认配置
AdaptiveRateLimiter limiter = new AdaptiveRateLimiter(RateLimitConfig.perSecond(1000));

// 自定义自适应配置
AdaptiveRateLimiter limiter = new AdaptiveRateLimiter(
    RateLimitConfig.perSecond(1000),
    AdaptiveRateLimiter.AdaptiveConfig.builder()
        .monitoringInterval(Duration.ofSeconds(5))
        .cpuMediumThreshold(0.6)
        .cpuHighThreshold(0.8)
        .cpuCriticalThreshold(0.95)
        .memoryHighThreshold(0.8)
        .memoryCriticalThreshold(0.95)
        .reductionStep(0.1)
        .build()
);

// 获取有效许可数（经过负载调整后的）
long effective = limiter.getEffectivePermits();
double multiplier = limiter.getCurrentMultiplier();

// 手动触发调整
limiter.forceAdjustment();

// 关闭
limiter.close();
```

### 5.6 SystemLoadMonitor - 系统负载监控

独立的系统负载监控工具，可单独使用或与自适应限流器集成。

```java
// 简单创建
SystemLoadMonitor monitor = SystemLoadMonitor.create();

// 带回调的监控
SystemLoadMonitor monitor = SystemLoadMonitor.builder()
    .monitoringInterval(Duration.ofSeconds(10))
    .onHighCpuLoad(0.8, snapshot -> System.out.println("CPU高: " + snapshot))
    .onHighMemory(0.9, snapshot -> System.out.println("内存高: " + snapshot))
    .build();

monitor.startMonitoring();

// 获取指标
double cpuLoad = monitor.getCpuLoad();
double memUsage = monitor.getHeapMemoryUsageRatio();
SystemLoadMonitor.LoadSnapshot snapshot = monitor.getLoadSnapshot();

// LoadSnapshot
boolean highCpu = snapshot.isHighCpuLoad(0.8);
boolean highMem = snapshot.isHighMemoryUsage(0.9);
boolean overloaded = snapshot.isOverloaded();
String formatted = snapshot.toFormattedString();

monitor.close();
```

---

## 6. 熔断模块

### 6.1 CircuitBreaker - 熔断器接口

```java
public interface CircuitBreaker {
    <T> T execute(Supplier<T> action);                          // 执行
    <T> T execute(Supplier<T> action, Supplier<T> fallback);    // 带降级执行
    CircuitState getState();                                    // 获取状态
    void open();                                                // 手动打开
    void close();                                               // 手动关闭
    void reset();                                               // 重置
    CircuitBreakerMetrics getMetrics();                          // 获取指标
    void recordSuccess();                                       // 记录成功
    void recordFailure();                                       // 记录失败
    void recordFailure(Throwable throwable);                    // 记录失败（带异常）
}
```

### 6.2 CircuitState - 熔断状态

```java
public enum CircuitState {
    CLOSED,      // 关闭（正常通行）
    OPEN,        // 打开（熔断拒绝）
    HALF_OPEN    // 半开（探测恢复）
}
```

### 6.3 CircuitBreakerConfig - 熔断配置

```java
public record CircuitBreakerConfig(
    int failureThreshold,              // 失败阈值（默认5）
    int successThreshold,              // 成功阈值-半开状态（默认3）
    Duration waitDuration,             // 等待时长（默认30s）
    int slidingWindowSize,             // 滑动窗口大小（默认10）
    Predicate<Throwable> recordFailure // 失败记录条件（默认所有异常）
)
```

```java
CircuitBreakerConfig config = CircuitBreakerConfig.defaults();

CircuitBreakerConfig config = CircuitBreakerConfig.builder()
    .failureThreshold(5)
    .successThreshold(3)
    .waitDuration(Duration.ofSeconds(30))
    .slidingWindowSize(10)
    .recordFailure(e -> !(e instanceof BusinessException))
    .build();
```

### 6.4 OpenCircuitBreaker - 熔断入口

```java
// 获取熔断器（按key缓存）
CircuitBreaker cb = OpenCircuitBreaker.get("service", config);

// 执行
String result = OpenCircuitBreaker.execute("service", config, () -> callService());

// 带降级
String result = OpenCircuitBreaker.executeWithFallback("service", config,
    () -> callService(), () -> "fallback");

// 检查状态
CircuitState state = OpenCircuitBreaker.getState("service");

// 管理
OpenCircuitBreaker.remove("service");
OpenCircuitBreaker.clear();
```

### 6.5 CircuitBreakerMetrics - 熔断指标

```java
public record CircuitBreakerMetrics(
    long totalCalls,
    long successCount,
    long failureCount,
    double failureRate,
    double successRate
)
```

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `empty()` | `CircuitBreakerMetrics` | 空指标 |
| `of(long totalCalls, long successCount, long failureCount)` | `CircuitBreakerMetrics` | 创建指标 |
| `isFailureRateExceeded(double threshold)` | `boolean` | 判断失败率是否超阈值 |
| `successRate()` | `double` | 成功率 |

### 6.6 分布式熔断器

**DistributedCircuitBreaker** 接口扩展 `CircuitBreaker`，增加分布式功能：

```java
public interface DistributedCircuitBreaker extends CircuitBreaker {
    void syncState();                                          // 同步状态
    ClusterMetrics getClusterMetrics();                         // 获取集群指标
    void addStateChangeListener(StateChangeListener listener);  // 添加状态监听器
    boolean removeStateChangeListener(StateChangeListener listener);
    String getNodeId();                                        // 获取节点ID
    String getName();                                          // 获取名称
    boolean isDistributed();                                   // 是否分布式
    void forceClusterState(CircuitState newState, String reason); // 强制集群状态
}
```

**DistributedCircuitBreakerFactory** 工厂方法：

```java
// 创建（通过SPI发现分布式提供者）
DistributedCircuitBreaker breaker = DistributedCircuitBreakerFactory.create("my-service");
DistributedCircuitBreaker breaker = DistributedCircuitBreakerFactory.create("my-service", config);

// 获取或创建（缓存）
DistributedCircuitBreaker breaker = DistributedCircuitBreakerFactory.getOrCreate("my-service");
DistributedCircuitBreaker breaker = DistributedCircuitBreakerFactory.getOrCreate("my-service", config);

// 创建本地适配器（无分布式提供者时）
DistributedCircuitBreaker local = DistributedCircuitBreakerFactory.createLocal("my-service", config);

// 管理
boolean available = DistributedCircuitBreakerFactory.isDistributedProviderAvailable();
String providerName = DistributedCircuitBreakerFactory.getProviderName();
DistributedCircuitBreakerFactory.clearCache();
DistributedCircuitBreakerFactory.shutdown();
```

**ClusterMetrics**（集群指标 record）：

```java
public record ClusterMetrics(
    int totalNodes, Map<String, NodeMetrics> nodeMetrics,
    long totalCalls, long totalSuccesses, long totalFailures,
    double clusterFailureRate, Instant timestamp
)
```

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `hasOpenCircuits()` | `boolean` | 是否有打开的熔断器 |
| `openCircuitCount()` | `long` | 打开的熔断器数量 |
| `isMajorityOpen()` | `boolean` | 是否多数打开 |
| `isFailureRateExceeded(double threshold)` | `boolean` | 集群失败率是否超阈值 |
| `getNodeMetrics(String nodeId)` | `NodeMetrics` | 获取节点指标 |

**StateChangeListener**：

```java
@FunctionalInterface
public interface StateChangeListener {
    void onStateChange(CircuitState from, CircuitState to, String reason);
}
```

---

## 7. 超时模块

### 7.1 TimeoutConfig - 超时配置

```java
public record TimeoutConfig(Duration timeout, boolean cancelOnTimeout)
```

```java
TimeoutConfig config = TimeoutConfig.of(Duration.ofSeconds(5));
TimeoutConfig config = TimeoutConfig.of(Duration.ofSeconds(5), false);
TimeoutConfig config = TimeoutConfig.ofMillis(5000);
TimeoutConfig config = TimeoutConfig.ofSeconds(5);
```

### 7.2 TimeoutUtil - 超时工具

```java
// 带超时执行
String result = TimeoutUtil.execute(() -> slowOp(), Duration.ofSeconds(5));
String result = TimeoutUtil.execute(() -> slowOp(), timeoutConfig);

// 带降级
String result = TimeoutUtil.executeWithFallback(() -> slowOp(), Duration.ofSeconds(5), "default");
String result = TimeoutUtil.executeWithFallback(() -> slowOp(), Duration.ofSeconds(5), () -> getFallback());

// 无返回值
TimeoutUtil.run(() -> slowTask(), Duration.ofSeconds(5));

// 异步
CompletableFuture<String> future = TimeoutUtil.executeAsync(() -> slowOp(), Duration.ofSeconds(5));
```

---

## 8. 舱壁模块

### 8.1 Bulkhead - 舱壁接口

```java
public interface Bulkhead {
    <T> T execute(Supplier<T> action);
    <T> T execute(Supplier<T> action, Supplier<T> fallback);
    void run(Runnable action);
    boolean tryAcquire();
    void release();
    int availablePermits();
    BulkheadConfig getConfig();
}
```

### 8.2 BulkheadConfig - 舱壁配置

```java
public record BulkheadConfig(int maxConcurrent, int maxWaiting, Duration maxWaitDuration)
```

```java
BulkheadConfig config = BulkheadConfig.of(10);                        // 最大并发10
BulkheadConfig config = BulkheadConfig.of(10, 5, Duration.ofSeconds(5)); // 10并发，5等待，5s超时
BulkheadConfig config = BulkheadConfig.defaults();
```

### 8.3 SemaphoreBulkhead - 信号量舱壁

```java
Bulkhead bulkhead = new SemaphoreBulkhead(BulkheadConfig.of(10));

String result = bulkhead.execute(() -> callService());
String result = bulkhead.execute(() -> callService(), () -> "fallback");

int available = bulkhead.availablePermits();
```

---

## 9. 对冲请求

### 9.1 HedgedRequest - 对冲请求执行器

使用结构化并发发送冗余请求，返回最快的成功结果。

```java
// 并行执行多个操作，返回最快完成的
String result = HedgedRequest.execute(List.of(
    () -> fetchFromServer1(),
    () -> fetchFromServer2(),
    () -> fetchFromServer3()
));

// 带超时
String result = HedgedRequest.execute(List.of(
    () -> fetchFromServer1(),
    () -> fetchFromServer2()
), Duration.ofSeconds(5));

// 延迟对冲（primary先执行，延迟后启动fallback）
String result = HedgedRequest.executeWithDelay(
    () -> fetchFromPrimary(),
    List.of(() -> fetchFromSecondary()),
    Duration.ofMillis(500)    // 500ms后启动对冲
);

// 两个操作的延迟对冲
String result = HedgedRequest.executeWithDelay(
    () -> fetchFromPrimary(),
    () -> fetchFromSecondary(),
    Duration.ofMillis(500)
);

// 交错延迟（逐步启动）
String result = HedgedRequest.executeWithStaggeredDelay(
    () -> fetchFromPrimary(),
    List.of(() -> fallback1(), () -> fallback2()),
    Duration.ofMillis(200)    // 每200ms启动一个
);

// 基于百分位的对冲（超过P95延迟时启动）
LatencyTracker tracker = new LatencyTracker();
String result = HedgedRequest.executeWithPercentile(
    () -> fetchFromPrimary(),
    List.of(() -> fetchFromSecondary()),
    tracker, 95.0
);

// 快捷方法
String result = HedgedRequest.executeWithP95(primary, fallbacks, tracker);
String result = HedgedRequest.executeWithP99(primary, fallbacks, tracker);

// 自适应对冲
String result = HedgedRequest.executeAdaptive(
    () -> primary(),
    List.of(() -> fallback()),
    tracker
);

// 使用DelayedAction
String result = HedgedRequest.executeWithDelays(List.of(
    HedgedRequest.DelayedAction.immediate(() -> primary()),
    HedgedRequest.DelayedAction.delayed(() -> fallback(), Duration.ofMillis(500))
));
```

### 9.2 LatencyTracker - 延迟跟踪器

跟踪请求延迟，用于百分位对冲。

```java
LatencyTracker tracker = new LatencyTracker();           // 默认1000个样本
LatencyTracker tracker = new LatencyTracker(10000);      // 自定义样本数

// 记录延迟
tracker.record(Duration.ofMillis(150));
tracker.record(150_000_000L);  // 纳秒

// 获取百分位
Duration p50 = tracker.getP50();
Duration p90 = tracker.getP90();
Duration p95 = tracker.getP95();
Duration p99 = tracker.getP99();
Duration custom = tracker.getLatency(99.9);

// 统计
Duration min = tracker.getMin();
Duration max = tracker.getMax();
Duration avg = tracker.getAverage();
long count = tracker.getSampleCount();
boolean has = tracker.hasSamples();

// 快照
LatencyTracker.Statistics stats = tracker.snapshot();

// 重置
tracker.clear();
```

---

## 10. 请求合并

### 10.1 RequestCollapser - 请求合并器

将多个请求合并为批处理操作，减少远程调用次数。

```java
// 创建合并器
RequestCollapser<Long, User> userCollapser = RequestCollapser.<Long, User>builder()
    .batchExecutor(ids -> userService.batchGet(ids))  // 批量执行函数
    .maxBatchSize(50)                                  // 最大批量大小
    .maxWaitTime(Duration.ofMillis(10))                // 最大等待时间
    .build();

// 异步提交
CompletableFuture<User> user1 = userCollapser.submit(userId1);
CompletableFuture<User> user2 = userCollapser.submit(userId2);
CompletableFuture<User> user3 = userCollapser.submit(userId3);

// 获取结果
User result1 = user1.join();
User result2 = user2.join();

// 同步执行
User user = userCollapser.execute(userId);

// 手动刷新
userCollapser.flush();

// 使用作用域
try (RequestCollapser.Scope scope = userCollapser.newScope()) {
    CompletableFuture<User> user = scope.submit(userId);
    scope.flush();
}

// 统计
int totalRequests = userCollapser.getTotalRequests();
int totalBatches = userCollapser.getTotalBatches();
int pending = userCollapser.getPendingCount();
double avgBatchSize = userCollapser.getAverageBatchSize();

// 关闭
userCollapser.close();
```

**Builder 方法：**

| 方法 | 参数 | 返回值 | 说明 |
|------|------|--------|------|
| `batchExecutor(Function<List<K>, Map<K, V>>)` | 批量执行函数 | `Builder` | 设置批量执行器 |
| `maxBatchSize(int)` | 最大批量大小 | `Builder` | 设置最大批量 |
| `maxWaitTime(Duration)` | 最大等待时间 | `Builder` | 设置等待窗口 |
| `executor(ExecutorService)` | 执行器 | `Builder` | 设置线程池 |
| `defaultValue(V)` | 默认值 | `Builder` | 缺失key的默认值 |
| `failOnMissing(boolean)` | 是否抛异常 | `Builder` | 缺失key时是否抛异常 |

---

## 11. 降级策略

### 11.1 FallbackStrategy - 降级策略接口（Sealed）

```java
public sealed interface FallbackStrategy<T>
    permits StaticFallback, CacheFallback, DegradedFallback,
            ChainedFallback, CircuitBreakerFallback {
    T execute(Throwable cause);
    default FallbackStrategy<T> andThen(FallbackStrategy<T> next) { ... }
}
```

### 11.2 FallbackStrategies - 降级策略工厂

```java
// 静态值降级
FallbackStrategy<String> fb = FallbackStrategies.staticValue("default");
FallbackStrategy<String> fb = FallbackStrategies.nullValue();

// 缓存降级
FallbackStrategy<User> fb = FallbackStrategies.fromCache("user:123", cache::get);

// 降级服务
FallbackStrategy<List<Product>> fb = FallbackStrategies.degraded(
    () -> getCachedProductList()
);

// 链式降级（按顺序尝试）
FallbackStrategy<String> fb = FallbackStrategies.chain(cacheFallback, staticFallback);
FallbackStrategy<String> fb = FallbackStrategies.chain(first, second, third);

// 熔断器感知降级
FallbackStrategy<Response> fb = FallbackStrategies.circuitBreaker(
    "payment-service",
    () -> circuitBreaker.getState(),
    state -> switch (state) {
        case OPEN -> getCachedResponse();
        case HALF_OPEN -> getDegradedResponse();
        case CLOSED -> null;
    }
);

// 使用
String result = fb.execute(new RuntimeException("Service down"));
```

### 11.3 降级策略实现

| 类 | 说明 | 用法 |
|-----|------|------|
| `StaticFallback<T>` | 返回预定义静态值 | `new StaticFallback<>("default")` |
| `CacheFallback<T>` | 从缓存加载 | `new CacheFallback<>("key", cache::get)` |
| `DegradedFallback<T>` | 调用降级服务 | `new DegradedFallback<>(() -> degradedResult())` |
| `ChainedFallback<T>` | 链式降级，依次尝试 | `new ChainedFallback<>(primary, secondary)` |
| `CircuitBreakerFallback<T>` | 根据熔断器状态决策 | 基于 CircuitState 的降级 |

---

## 12. 失败类型（Sealed）

### 12.1 ResilienceFailure - 弹性失败接口

```java
public sealed interface ResilienceFailure
    permits RetryFailure, RateLimitFailure, CircuitBreakerFailure,
            TimeoutFailure, BulkheadFailure {
    String message();
    Throwable cause();
    Instant timestamp();
}
```

支持 JDK 25 模式匹配：

```java
String action = switch (failure) {
    case RetryFailure r -> "重试 " + r.attempt() + "/" + r.maxAttempts();
    case RateLimitFailure r -> "限流，重试间隔: " + r.retryAfterOrDefault(Duration.ofSeconds(1));
    case CircuitBreakerFailure c -> "熔断器 " + c.circuitBreakerKey() + " 状态: " + c.state();
    case TimeoutFailure t -> "超时 " + t.elapsedMillis() + "ms / " + t.timeoutMillis() + "ms";
    case BulkheadFailure b -> "舱壁满 " + b.utilizationPercent() + "%";
};
```

### 12.2 OpenResilienceExceptionHandler - 异常处理

基于模式匹配的异常处理工具。

```java
// 处理失败，返回降级值
String result = OpenResilienceExceptionHandler.handle(failure, () -> "fallback");

// 根据失败类型确定策略
ResilienceStrategy strategy = OpenResilienceExceptionHandler.determineStrategy(failure);

// 自定义处理
OpenResilienceExceptionHandler.handleWith(failure,
    retry -> retryHandler(retry),
    rateLimit -> rateLimitHandler(rateLimit),
    circuitBreaker -> cbHandler(circuitBreaker),
    timeout -> timeoutHandler(timeout),
    bulkhead -> bulkheadHandler(bulkhead)
);

// 判断
boolean shouldFallback = OpenResilienceExceptionHandler.shouldFallback(failure);
boolean isTransient = OpenResilienceExceptionHandler.isTransient(failure);
String desc = OpenResilienceExceptionHandler.describe(failure);
```

---

## 13. 组合装饰器

### 13.1 Decorators - 流式组合 API

应用顺序：RateLimiter -> Bulkhead -> CircuitBreaker -> Timeout -> Retry -> Fallback

```java
// Supplier 装饰器
Supplier<String> decorated = Decorators.ofSupplier(() -> callService())
    .withRateLimiter("api", rateLimitConfig)
    .withBulkhead(BulkheadConfig.of(10))
    .withCircuitBreaker("service", cbConfig)
    .withTimeout(Duration.ofSeconds(5))
    .withRetry(retryConfig)
    .withFallback("default")
    .decorate();

String result = decorated.get();

// 直接执行
String result = Decorators.ofSupplier(() -> callService())
    .withRetry(retryConfig)
    .withCircuitBreaker("service", cbConfig)
    .withFallback(() -> getCachedValue())
    .get();

// 异步执行
CompletableFuture<String> future = Decorators.ofSupplier(() -> callService())
    .withRetry(retryConfig)
    .getAsync();

// Callable 装饰器
String result = Decorators.ofCallable(() -> callService())
    .withRetry(retryConfig)
    .get();

// Runnable 装饰器
Decorators.ofRunnable(() -> sendEmail())
    .withRetry(retryConfig)
    .withCircuitBreaker("email", cbConfig)
    .withFallback(() -> logFailure())
    .run();

// 异步运行
CompletableFuture<Void> future = Decorators.ofRunnable(() -> sendEmail())
    .withRetry(retryConfig)
    .runAsync();
```

### 13.2 ResilientExecutor - 弹性执行器

```java
ResilientExecutor executor = new ResilientExecutor()
    .rateLimit("api", rateLimitConfig)
    .circuitBreaker("service", cbConfig)
    .timeout(Duration.ofSeconds(5))
    .retry(retryConfig);

// 执行
String result = executor.execute(() -> callService());

// 带降级
String result = executor.execute(() -> callService(), () -> "fallback");

// 无返回值
executor.run(() -> sendNotification());
executor.run(() -> sendNotification(), () -> logFailure());
```

### 13.3 ResilientSupplier / ResilientRunnable

```java
// 包装Supplier
ResilientSupplier<String> supplier = ResilientSupplier.of(() -> fetchData())
    .withFallback(() -> getCachedData());
String result = supplier.get();

// 包装Runnable
ResilientRunnable runnable = ResilientRunnable.of(() -> sendEmail())
    .withFallback(() -> logAndRetry());
runnable.run();
```

---

## 14. 响应式弹性

### 14.1 ReactiveResilience - 非阻塞弹性工具

所有操作返回 `CompletableFuture`，使用 Virtual Thread 执行。

```java
// 异步重试
CompletableFuture<String> result = ReactiveResilience.retryAsync(() -> fetchData());
CompletableFuture<String> result = ReactiveResilience.retryAsync(() -> fetchData(), retryConfig);
CompletableFuture<String> result = ReactiveResilience.retryAsyncWithFallback(
    () -> fetchData(), retryConfig, "fallback");

// 异步超时
CompletableFuture<String> result = ReactiveResilience.timeoutAsync(() -> slowOp(), Duration.ofSeconds(5));
CompletableFuture<String> result = ReactiveResilience.timeoutAsyncWithFallback(
    () -> slowOp(), Duration.ofSeconds(5), "default");

// 异步熔断
CompletableFuture<String> result = ReactiveResilience.circuitBreakAsync(
    "service", cbConfig, () -> callService());
CompletableFuture<String> result = ReactiveResilience.circuitBreakAsyncWithFallback(
    "service", cbConfig, () -> callService(), "fallback");

// 组合：重试+超时
CompletableFuture<String> result = ReactiveResilience.retryWithTimeoutAsync(
    () -> fetchData(), retryConfig, Duration.ofSeconds(5));

// 组合：熔断+超时
CompletableFuture<String> result = ReactiveResilience.circuitBreakWithTimeoutAsync(
    "service", cbConfig, () -> callService(), Duration.ofSeconds(5));

// 延迟执行
CompletableFuture<String> result = ReactiveResilience.delayedAsync(
    Duration.ofSeconds(1), () -> compute());
CompletableFuture<Void> delay = ReactiveResilience.delay(Duration.ofSeconds(1));
```

---

## 15. JDK 25 增强

### 15.1 VirtualThreadResilience - Virtual Thread 弹性执行

```java
// 异步弹性执行（Virtual Thread）
CompletableFuture<String> future = VirtualThreadResilience.executeAsync(
    () -> callService(), resilienceConfig);

// 带自定义执行器
CompletableFuture<String> future = VirtualThreadResilience.executeAsync(
    () -> callService(), resilienceConfig, customExecutor);

// 批量并行执行
List<String> results = VirtualThreadResilience.executeAll(
    List.of(() -> callA(), () -> callB(), () -> callC()),
    resilienceConfig
);

// 带超时的批量执行
List<String> results = VirtualThreadResilience.executeAllWithTimeout(
    List.of(() -> callA(), () -> callB()),
    Duration.ofSeconds(10),
    resilienceConfig
);

// 结构化并发（最快成功的返回）
String result = VirtualThreadResilience.executeWithStructuredConcurrency(
    () -> fetchFromPrimary(),
    () -> fetchFromSecondary(),
    resilienceConfig
);

// 快速失败批量执行
List<String> results = VirtualThreadResilience.executeAllWithFailFast(
    List.of(() -> callA(), () -> callB()),
    resilienceConfig
);

// 竞争执行（最快结果返回）
String result = VirtualThreadResilience.race(
    List.of(() -> callA(), () -> callB(), () -> callC()),
    resilienceConfig
);
```

### 15.2 ResilienceContext - ScopedValue 上下文

弹性操作的 ScopedValue 上下文传播。

**预定义 ScopedValue：**

| 常量 | 类型 | 说明 |
|------|------|------|
| `RETRY_ATTEMPT` | `ScopedValue<Integer>` | 当前重试次数 |
| `CIRCUIT_STATE` | `ScopedValue<CircuitState>` | 熔断器状态 |
| `RATE_LIMIT_REMAINING` | `ScopedValue<Long>` | 限流剩余配额 |
| `TRACE_ID` | `ScopedValue<String>` | 调用链ID |
| `DEADLINE` | `ScopedValue<Instant>` | 超时截止时间 |
| `OPERATION_NAME` | `ScopedValue<String>` | 操作名称 |

```java
// 获取上下文信息
int attempt = ResilienceContext.currentAttempt();
boolean isRetrying = ResilienceContext.isRetrying();
CircuitState state = ResilienceContext.currentCircuitState();
boolean isOpen = ResilienceContext.isCircuitOpen();
long remaining = ResilienceContext.remainingQuota();
boolean hasQuota = ResilienceContext.hasQuota();
Optional<String> traceId = ResilienceContext.traceId();
Optional<String> opName = ResilienceContext.operationName();
Optional<Instant> deadline = ResilienceContext.deadline();
Optional<Duration> remaining = ResilienceContext.remainingTime();
boolean expired = ResilienceContext.isExpired();
boolean approaching = ResilienceContext.isDeadlineApproaching(Duration.ofSeconds(1));

// 构建并运行
ResilienceContext.builder()
    .retryAttempt(3)
    .circuitState(CircuitState.CLOSED)
    .rateLimitRemaining(100)
    .traceId("trace-123")
    .deadline(Instant.now().plusSeconds(30))
    .operationName("fetchUser")
    .run(() -> {
        // 在此上下文中执行
        int attempt = ResilienceContext.currentAttempt();
    });
```

### 15.3 ContextAwareResilientExecutor - 上下文感知执行器

自动传播 ScopedValue 上下文的弹性执行器。

```java
ContextAwareResilientExecutor executor = new ContextAwareResilientExecutor()
    .withTraceId("trace-123")
    .withOperationName("fetchUser")
    .withTimeout(Duration.ofSeconds(30))
    .retry(retryConfig)
    .rateLimit("api", rateLimitConfig)
    .circuitBreaker("service", cbConfig)
    .timeout(Duration.ofSeconds(5));

// 同步执行
String result = executor.execute(() -> callService());
String result = executor.execute(() -> callService(), () -> "fallback");

// 异步执行
CompletableFuture<String> future = executor.executeAsync(() -> callService());

// 无返回值
executor.run(() -> sendNotification());
executor.run(() -> sendNotification(), () -> logFailure());

// 自动生成traceId
executor = new ContextAwareResilientExecutor().withNewTraceId();
```

---

## 16. 统一配置

### 16.1 ResilienceConfig - 统一配置

```java
public record ResilienceConfig(
    RetryConfig retryConfig,
    String rateLimitKey,
    RateLimitConfig rateLimitConfig,
    String circuitBreakerKey,
    CircuitBreakerConfig circuitBreakerConfig,
    TimeoutConfig timeoutConfig,
    BulkheadConfig bulkheadConfig,
    boolean enableMetrics
)
```

```java
ResilienceConfig config = ResilienceConfig.builder()
    .retryConfig(RetryConfig.builder().maxAttempts(3).build())
    .rateLimit("api", RateLimitConfig.perSecond(100))
    .circuitBreaker("service", CircuitBreakerConfig.defaults())
    .timeoutConfig(TimeoutConfig.ofSeconds(5))
    .bulkheadConfig(BulkheadConfig.of(10))
    .enableMetrics(true)
    .build();

// 默认配置
ResilienceConfig config = ResilienceConfig.ofDefaults();

// 获取超时
Duration timeout = config.timeout();
```

---

## 17. 指标收集

### 17.1 ResilienceMetrics - 指标接口

```java
public interface ResilienceMetrics {
    void recordSuccess(String componentName, Duration duration);
    void recordFailure(String componentName, Throwable cause);
    void recordRetry(String componentName, int attemptNumber, Duration delay);
    void recordRateLimited(String componentName, Duration waitDuration);
    void recordCircuitBreakerStateChange(String componentName, CircuitState from, CircuitState to);
    MetricsSnapshot snapshot(String componentName);
    void reset();
    void reset(String componentName);
}
```

### 17.2 MetricsSnapshot - 指标快照

```java
public record MetricsSnapshot(
    long totalCalls, long successCount, long failureCount,
    double successRate, Duration avgDuration, Duration p99Duration,
    long retryCount, long rateLimitedCount
)
```

```java
MetricsSnapshot snapshot = metrics.snapshot("service-a");
System.out.println("总调用: " + snapshot.totalCalls());
System.out.println("成功率: " + snapshot.successRate() + "%");
System.out.println("平均延迟: " + snapshot.avgDuration().toMillis() + "ms");
System.out.println("P99延迟: " + snapshot.p99Duration().toMillis() + "ms");

boolean exceeded = snapshot.isFailureRateExceeded(0.1);
boolean met = snapshot.isSuccessRateMet(0.99);
boolean p99High = snapshot.isP99Exceeded(Duration.ofSeconds(1));
double retryRate = snapshot.retryRate();
double rateLimitRate = snapshot.rateLimitRate();
```

### 17.3 DefaultResilienceMetrics - 默认实现

```java
DefaultResilienceMetrics metrics = new DefaultResilienceMetrics();
DefaultResilienceMetrics metrics = new DefaultResilienceMetrics(10000); // 自定义样本数
```

### 17.4 ObservabilityMetricsAdapter - 可观测性适配器

将弹性指标导出到 observability 模块。

```java
ResilienceMetrics metrics = ObservabilityMetricsAdapter.createMetrics();
boolean available = ObservabilityMetricsAdapter.isObservabilityModuleAvailable();
```

---

## 18. 健康检查

### 18.1 ResilienceHealthCheck

```java
ResilienceHealthCheck healthCheck = new ResilienceHealthCheck();
// 或自定义阈值
ResilienceHealthCheck healthCheck = new ResilienceHealthCheck(0.5, 0.3);

// 注册组件
healthCheck.registerCircuitBreaker("payment-service", circuitBreaker);
healthCheck.registerRateLimiter("api", rateLimiter);
healthCheck.registerMetrics(metrics);

// 执行检查
ResilienceHealthCheck.HealthStatus status = healthCheck.check();
System.out.println("状态: " + status.status());
System.out.println("健康: " + status.isHealthy());
System.out.println("降级: " + status.isDegraded());
System.out.println("宕机: " + status.isDown());

// 检查单个组件
ResilienceHealthCheck.ComponentHealth health = healthCheck.checkComponent("payment-service");

// 查询
int count = healthCheck.getRegisteredComponentCount();
boolean hasDown = healthCheck.hasDownComponents();
boolean allHealthy = healthCheck.isAllHealthy();

// 注销
healthCheck.unregisterCircuitBreaker("payment-service");
healthCheck.unregisterRateLimiter("api");
```

**HealthStatus record：**

| 方法 | 返回值 | 说明 |
|------|--------|------|
| `status()` | `Status` | 整体状态 |
| `components()` | `List<ComponentHealth>` | 组件健康列表 |
| `isHealthy()` | `boolean` | 是否健康 |
| `isDegraded()` | `boolean` | 是否降级 |
| `isDown()` | `boolean` | 是否宕机 |
| `countByStatus(Status)` | `long` | 按状态统计组件数 |

**Status 枚举：** `UP`, `DOWN`, `DEGRADED`, `UNKNOWN`

**ComponentType 枚举：** `CIRCUIT_BREAKER`, `RATE_LIMITER`, `RETRY`, `BULKHEAD`, `TIMEOUT`

---

## 19. SPI 扩展

### 19.1 ResilienceSPI - SPI 加载器

```java
// 初始化
ResilienceSPI.initialize();

// 获取自定义熔断器
CircuitBreaker cb = ResilienceSPI.getCircuitBreaker("redis", "my-service", config);

// 计算自定义退避延迟
Duration delay = ResilienceSPI.calculateBackoff("FIBONACCI", 3, retryConfig);

// 查询可用提供者
List<String> cbProviders = ResilienceSPI.getAvailableCircuitBreakerProviders();
List<String> backoffStrategies = ResilienceSPI.getAvailableBackoffStrategies();
List<String> rateLimitAlgos = ResilienceSPI.getAvailableRateLimitAlgorithms();

// 注册自定义提供者
ResilienceSPI.registerCircuitBreakerProvider(customProvider);
ResilienceSPI.registerBackoffProvider(customBackoff);
ResilienceSPI.registerRateLimitProvider(customAlgo);

// 重置
ResilienceSPI.reset();
```

### 19.2 SPI 接口

| 接口 | 说明 |
|------|------|
| `CircuitBreakerProvider` | 自定义熔断器实现 |
| `BackoffStrategyProvider` | 自定义退避策略 |
| `RateLimitAlgorithmProvider` | 自定义限流算法 |
| `ResilienceMetricsProvider` | 自定义指标收集 |
| `CircuitBreakerEventListener` | 熔断器事件监听 |
| `DistributedCircuitBreakerProvider` | 分布式熔断器提供者 |

### 19.3 CircuitBreakerEventListener

```java
public interface CircuitBreakerEventListener {
    void onStateChange(String name, CircuitState from, CircuitState to, String reason);
    default void onSuccess(String name, Duration duration) {}
    default void onFailure(String name, Throwable cause, Duration duration) {}
    default void onReset(String name, Instant timestamp) {}
    default void onCallNotPermitted(String name) {}
    default void onSlowCall(String name, Duration duration) {}
    default void onHalfOpenTrialStarted(String name, int permittedCalls) {}
}
```

内置实现：`NoopCircuitBreakerEventListener`、`LoggingCircuitBreakerEventListener`、`CompositeCircuitBreakerEventListener`。

---

## 20. 异常体系

```
OpenException (opencode-base-core)
+-- OpenResilienceException               // 弹性异常基类
    +-- OpenRetryException                 // 重试异常
    |   +-- OpenRetryExhaustedException    // 重试耗尽
    +-- OpenRateLimitException             // 限流异常
    +-- OpenCircuitBreakerException        // 熔断异常
```

**OpenResilienceException** 构造方法：

```java
public OpenResilienceException(String message);
public OpenResilienceException(String message, Throwable cause);
public OpenResilienceException(Throwable cause);
public OpenResilienceException(String errorCode, String message);
public OpenResilienceException(String errorCode, String message, Throwable cause);
```

**OpenRetryException** 额外方法：`getAttempt()` - 获取失败时的尝试次数。

**OpenRetryExhaustedException** 额外方法：`getMaxAttempts()` - 获取最大尝试次数。

**OpenRateLimitException** 额外方法：`getRateLimitKey()`, `getRetryAfter()` -> `Optional<Duration>`。

**OpenCircuitBreakerException** 额外方法：`getCircuitBreakerKey()`, `getState()` -> `CircuitState`。

---

## 21. 使用示例

### 21.1 基本重试

```java
// 简单重试
String result = OpenResilience.retry(() -> fetchFromApi());

// 自定义配置
RetryConfig config = RetryConfig.builder()
    .maxAttempts(5)
    .initialDelay(Duration.ofMillis(200))
    .backoff(BackoffStrategy.EXPONENTIAL)
    .retryOn(IOException.class, TimeoutException.class)
    .build();

String result = OpenResilience.retry(() -> fetchFromApi(), config);
```

### 21.2 限流保护

```java
// 创建限流器
RateLimiter limiter = OpenResilience.rateLimiter("api:/users", RateLimitConfig.perSecond(100));

if (limiter.tryAcquire()) {
    processRequest();
} else {
    returnTooManyRequests();
}

// 带降级
String result = OpenResilience.rateLimitWithFallback(
    "api:/search", RateLimitConfig.perSecond(1000),
    () -> callSearchApi(),
    () -> "Fallback response"
);
```

### 21.3 熔断保护

```java
CircuitBreakerConfig cbConfig = CircuitBreakerConfig.builder()
    .failureThreshold(5)
    .successThreshold(3)
    .waitDuration(Duration.ofSeconds(30))
    .build();

String result = OpenResilience.circuitBreak("external-service", cbConfig,
    () -> callExternalService());

// 状态检查
CircuitState state = OpenCircuitBreaker.getState("external-service");
```

### 21.4 组合策略

```java
String result = OpenResilience.executor()
    .rateLimit("api:/orders", RateLimitConfig.perSecond(100))
    .circuitBreaker("order-service", CircuitBreakerConfig.defaults())
    .timeout(Duration.ofSeconds(5))
    .retry(RetryConfig.builder()
        .maxAttempts(3)
        .retryOn(IOException.class)
        .build())
    .execute(
        () -> orderService.createOrder(order),
        () -> getCachedOrder(order)
    );
```

### 21.5 对冲请求

```java
// 延迟对冲：primary超过500ms后启动secondary
String result = HedgedRequest.executeWithDelay(
    () -> fetchFromPrimary(),
    () -> fetchFromSecondary(),
    Duration.ofMillis(500)
);

// 基于P95延迟的自适应对冲
LatencyTracker tracker = new LatencyTracker();
String result = HedgedRequest.executeWithP95(
    () -> fetchFromPrimary(),
    List.of(() -> fetchFromSecondary()),
    tracker
);
```

### 21.6 流式装饰器

```java
Supplier<String> decorated = Decorators.ofSupplier(() -> callService())
    .withRateLimiter("api", RateLimitConfig.perSecond(100))
    .withCircuitBreaker("service", CircuitBreakerConfig.defaults())
    .withTimeout(Duration.ofSeconds(5))
    .withRetry(RetryConfig.defaults())
    .withFallback("default")
    .decorate();

String result = decorated.get();
```

### 21.7 Virtual Thread 批量执行

```java
ResilienceConfig config = ResilienceConfig.builder()
    .retryConfig(RetryConfig.defaults())
    .circuitBreaker("service", CircuitBreakerConfig.defaults())
    .build();

List<String> results = VirtualThreadResilience.executeAll(
    List.of(
        () -> fetchFromService1(),
        () -> fetchFromService2(),
        () -> fetchFromService3()
    ),
    config
);
```

---

## 22. 线程安全

| 组件 | 线程安全级别 | 说明 |
|------|-------------|------|
| OpenResilience | 完全安全 | 无状态静态方法 |
| OpenRetry | 完全安全 | 无状态静态方法 |
| OpenRateLimiter | 完全安全 | ConcurrentHashMap缓存 |
| OpenCircuitBreaker | 完全安全 | ConcurrentHashMap缓存 |
| Retryer | 完全安全 | 每次创建新实例 |
| TokenBucketLimiter | 完全安全 | AtomicLong |
| SlidingWindowLimiter | 完全安全 | AtomicLong |
| DefaultCircuitBreaker | 完全安全 | AtomicReference状态机 |
| ResilientExecutor | 非线程安全 | 可变Builder |
| Decorators.Builder | 非线程安全 | 可变Builder |
| RequestCollapser | 完全安全 | 内部同步 |
| LatencyTracker | 完全安全 | ReadWriteLock |
| ResilienceContext | 完全安全 | ScopedValues |
| MetricsSnapshot | 完全安全 | 不可变record |

# OpenCode Base Observability

**轻量级、框架无关的可观测性基础组件，适用于 JDK 25+**

`opencode-base-observability` 提供完整的可观测性工具包，零外部依赖：指标（Counter/Gauge/Timer/Histogram）、健康检查、上下文传播、分布式追踪（通过反射自动检测 OpenTelemetry）和慢操作日志。

## 功能特性

- **指标体系**：Counter、Gauge、Timer、Histogram，基于 Tag 维度模型（借鉴 Micrometer 设计，但更轻量）
- **指标注册表**：中央注册表，幂等注册、容量限制（默认 10,000）、快照导出
- **健康检查**：函数式接口，支持聚合状态（UP/DOWN/DEGRADED），异常隔离，容量限制
- **上下文传播**：基于 ThreadLocal 的 `ObservabilityContext`，支持 traceId、spanId、baggage，跨线程 `wrap(Runnable/Callable)`
- **追踪**：密封 `Tracer` 接口，通过反射集成 OpenTelemetry（可选，无硬依赖）
- **慢日志**：有界、线程安全的慢操作日志，带统计聚合（Redis SLOWLOG 风格）
- **零依赖**：无需 Spring、Micrometer、OTel SDK
- **线程安全**：LongAdder 计数器、`ConcurrentHashMap` 注册表、`VarHandle` 最大值跟踪
- **高性能**：Counter ~57M ops/s、Timer ~47M ops/s、Histogram ~41M ops/s（单线程基准测试）

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-observability</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 指标

```java
import cloud.opencode.base.observability.metric.*;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

MetricRegistry registry = MetricRegistry.create();

// 计数器 — 基于 LongAdder，高并发优化
Counter requests = registry.counter("http.requests", Tag.of("method", "GET"));
requests.increment();
requests.increment(10);
System.out.println(requests.count()); // 11

// 仪表盘 — 由 Supplier<Double> 驱动
AtomicInteger queueSize = new AtomicInteger(42);
Gauge queue = registry.gauge("queue.size", queueSize::doubleValue);
System.out.println(queue.value()); // 42.0

// 计时器 — 支持 time(Runnable/Callable) 便捷计时
Timer dbQuery = registry.timer("db.query", Tag.of("table", "users"));
dbQuery.time(() -> {
    // 你的操作
});
System.out.println(dbQuery.count());     // 1
System.out.println(dbQuery.totalTime()); // PT0.000012S
System.out.println(dbQuery.max());       // PT0.000012S
System.out.println(dbQuery.mean());      // PT0.000012S

// 直方图 — 环形缓冲区百分位数（8192 样本）
Histogram responseSize = registry.histogram("http.response.size");
responseSize.record(1024.0);
responseSize.record(2048.0);
responseSize.record(512.0);
System.out.println(responseSize.percentile(0.50)); // p50
System.out.println(responseSize.percentile(0.99)); // p99

// 快照所有指标（用于导出）
List<MetricSnapshot> snapshots = registry.snapshot();
```

### 健康检查

```java
import cloud.opencode.base.observability.health.*;
import java.time.Duration;
import java.util.Map;

HealthRegistry health = HealthRegistry.create(); // 默认上限：1000 个检查

health.register("database", () -> {
    long start = System.nanoTime();
    boolean ok = db.isConnected();
    Duration elapsed = Duration.ofNanos(System.nanoTime() - start);
    return ok
        ? HealthResult.up("database", elapsed)
        : HealthResult.down("database", "连接被拒绝", elapsed);
});

health.register("cache", () ->
    HealthResult.up("cache", Duration.ofMillis(1))
);

// 执行所有检查（异常被捕获并产生 DOWN 结果）
Map<String, HealthResult> results = health.check();
HealthStatus overall = health.status(); // UP、DOWN 或 DEGRADED
```

### 上下文传播

```java
import cloud.opencode.base.observability.context.*;

// 创建并附加上下文（必须使用 try-with-resources）
ObservabilityContext ctx = ObservabilityContext.create("trace-abc-123");
try (ObservabilityContext.Scope scope = ctx.attach()) {
    System.out.println(ObservabilityContext.current().traceId()); // trace-abc-123

    // 传播到虚拟线程
    Runnable wrapped = ctx.wrap(() -> {
        String traceId = ObservabilityContext.current().traceId(); // trace-abc-123
    });
    Thread.startVirtualThread(wrapped);
}
// try-with-resources 结束后上下文自动恢复

// 添加 baggage（返回新的不可变上下文）
ObservabilityContext withBaggage = ctx.withBaggage("user.id", "u-456");
```

### 追踪

```java
import cloud.opencode.base.observability.*;

// 自动检测类路径上的 OTel；不可用时回退到 noop
Tracer tracer = OpenTelemetryTracer.create("my-service");

try (Span span = tracer.startSpan("GET", "user:123")) {
    Object value = cache.get("user:123");
    span.setHit(value != null);
    span.setAttribute("cache.tier", "L1");
}

tracer.close();

// 用于测试的零开销 noop 追踪器
Tracer noop = Tracer.noop();
```

### 慢日志收集器

```java
import cloud.opencode.base.observability.SlowLogCollector;
import java.time.Duration;

// 阈值必须为正数（> 0）
SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(50));
collector.record("GET", "user:123", Duration.ofMillis(75));

SlowLogCollector.Stats stats = collector.stats();
System.out.println("慢操作总数: " + stats.totalSlowOps());
System.out.println("最大耗时:   " + stats.maxDuration());
System.out.println("平均耗时:   " + stats.avgDuration());
System.out.println("最慢操作:   " + stats.slowestOperation());

// 清除缓冲区（累积计数保留）
collector.clear();
```

## API 参考

### 指标 (`cloud.opencode.base.observability.metric`)

| 类名 | 类型 | 说明 |
|------|------|------|
| `Counter` | interface | 单调递增计数器（基于 LongAdder） |
| `Gauge` | interface | 由 `Supplier<Double>` 支持的瞬时值 |
| `Timer` | interface | 耗时记录 + 计数，`time(Runnable)` / `time(Callable<T>)` |
| `Histogram` | interface | 分布统计，环形缓冲区百分位数（8192 样本） |
| `MetricRegistry` | final class | 中央注册表：`create()`、`counter()`、`gauge()`、`timer()`、`histogram()`、`snapshot()`、`find()`、`remove()`、`clear()`、`size()` |
| `MetricId` | record | 指标标识：名称 + 排序标签，`of(name, tags...)` |
| `Tag` | record | 键值标签：`of(key, value)` |
| `MetricSnapshot` | record | 时间点快照：id、type、values 映射 |

### 健康检查 (`cloud.opencode.base.observability.health`)

| 类名 | 类型 | 说明 |
|------|------|------|
| `HealthCheck` | @FunctionalInterface | `HealthResult check()` |
| `HealthStatus` | enum | `UP`、`DOWN`、`DEGRADED` + `aggregate(Collection)` |
| `HealthResult` | record | name、status、detail、duration + `up()`、`down()`、`degraded()` 工厂方法 |
| `HealthRegistry` | final class | `create()`、`create(maxChecks)`、`register()`、`unregister()`、`check()`、`status()`、`names()`、`size()`、`clear()` |

### 上下文 (`cloud.opencode.base.observability.context`)

| 类名 | 类型 | 说明 |
|------|------|------|
| `ObservabilityContext` | final class | `create()`、`current()`、`clear()`、`attach()`、`wrap()`、`withSpanId()`、`withBaggage()`、`traceId()`、`spanId()`、`baggage()`、`allBaggage()` |
| `ObservabilityContext.Scope` | static final class | `AutoCloseable` 作用域，管理上下文生命周期（含线程安全关闭守卫） |

### 追踪 (`cloud.opencode.base.observability`)

| 类名 | 类型 | 说明 |
|------|------|------|
| `Tracer` | sealed interface | `startSpan()`、`close()`、`noop()` |
| `OpenTelemetryTracer` | final class | `create(serviceName)`、`isOtelAvailable()`、`serviceName()` |
| `Span` | interface | `setHit()`、`setError()`、`setAttribute()`、`end()`、`close()`、`NOOP` 单例 |
| `SlowLogCollector` | final class | `create()`、`record()`、`getEntries()`、`clear()`、`count()`、`stats()`、`threshold()`、`maxEntries()` |
| `SlowLogCollector.Entry` | record | operation、key、elapsed、timestamp、threadName |
| `SlowLogCollector.Stats` | record | totalSlowOps、maxDuration、avgDuration、slowestOperation + `EMPTY` |

### 异常 (`cloud.opencode.base.observability.exception`)

| 类名 | 类型 | 说明 |
|------|------|------|
| `ObservabilityException` | class | 继承自 `OpenException`，component = "Observability" |

## 性能

基准测试结果（Apple Silicon，JDK 25，单线程/16 虚拟线程）：

| 操作 | 1 线程 (ops/s) | 16 线程 (ops/s) |
|------|---------------:|----------------:|
| Counter.increment() | ~57M | ~37M |
| Timer.record(Duration) | ~47M | ~33M |
| Timer.time(Runnable) | ~25M | — |
| Histogram.record(double) | ~41M | ~20M |
| Registry.counter() 查找 | ~25M | ~17M |
| Context.create() | ~32M | — |
| Context attach/detach | ~18M | — |

## 模块信息

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

## 环境要求

- Java 25+（密封接口、记录类、VarHandle、LongAdder）
- 无需外部依赖
- 可选：类路径上的 OpenTelemetry API 以启用真实追踪

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

# Observability 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-observability` 模块提供轻量级可观测性支持，包含指标采集（Metrics）、链路追踪（Tracing）和健康检查（Health Check）三大核心能力。基于 JDK 25 特性（ScopedValue、Virtual Thread、Sealed Classes、Record）实现，零外部依赖。

**核心特性：**
- 指标采集（Metrics）：Counter、Gauge、Histogram、Timer、Summary 五种指标类型
- 链路追踪（Tracing）：V1（ThreadLocal）和 V2（ScopedValue）两套实现
- 健康检查（Health Check）：V1 和 V2（Virtual Thread 并行检查）两套实现
- 导出器（Exporter）：Prometheus、JSON、OTLP 三种导出格式
- 上下文传播（Context Propagation）：W3C Trace Context 和 Baggage 支持
- 验证与安全：指标名称验证、数量限制、追踪数据脱敏

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application Layer                         │
│                    (业务代码埋点监控)                              │
└─────────────────────────────────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────┐
│                          Facade Layer                            │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐      │
│  │ OpenMetrics  │  │ OpenTracing  │  │ OpenTracingV2    │      │
│  │ (指标注册)    │  │ (追踪 V1)    │  │ (追踪 V2/VT)    │      │
│  └──────────────┘  └──────────────┘  └──────────────────┘      │
└─────────────────────────────────────────────────────────────────┘
                                  │
        ┌─────────────────────────┼─────────────────────────┐
        ▼                         ▼                         ▼
┌───────────────┐        ┌───────────────┐        ┌───────────────┐
│    Metrics    │        │    Tracing    │        │    Health     │
│    指标模块    │        │    追踪模块    │        │   健康检查     │
├───────────────┤        ├───────────────┤        ├───────────────┤
│CounterMetric  │        │TraceContext   │        │HealthCheck    │
│GaugeMetric    │        │TraceContextV2 │        │HealthCheckV2  │
│HistogramMetric│        │Span / SpanV2  │        │HealthStatus   │
│TimerMetric    │        │SpanData/V2    │        │HealthStatusV2 │
│SummaryMetric  │        │SpanEvent      │        │HealthChecks   │
│MetricsRegistry│        │SpanLink       │        │               │
│               │        │SpanStatus/V2  │        │               │
│               │        │Baggage        │        │               │
│               │        │ContextPropag. │        │               │
│               │        │BaggagePropag. │        │               │
└───────────────┘        └───────────────┘        └───────────────┘
        │                         │                         │
        ▼                         ▼                         ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Registry / Export Layer                    │
│  ┌──────────────────┐  ┌──────────────────┐                     │
│  │ MetricsRegistry  │  │HealthCheckRegistry│                    │
│  │                  │  │HealthCheckRegV2  │                    │
│  └──────────────────┘  └──────────────────┘                     │
│  ┌───────────────┐  ┌───────────────┐  ┌───────────────┐       │
│  │Prometheus     │  │ JSON          │  │ OTLP          │       │
│  │Exporter       │  │ Exporter      │  │ Metrics/Span  │       │
│  └───────────────┘  └───────────────┘  └───────────────┘       │
│  ┌───────────────┐                                              │
│  │SpanReporterV2 │                                              │
│  └───────────────┘                                              │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-observability</artifactId>
    <version>${version}</version>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.observability
├── Metric.java                      # 指标接口
├── HealthCheck.java                 # 健康检查接口
├── Span.java                        # 跨度 V1（ThreadLocal）
├── OpenMetrics.java                 # 指标注册表门面
├── OpenTracing.java                 # 链路追踪门面 V1
├── OpenTracingV2.java               # 链路追踪门面 V2（ScopedValue + VT）
│
├── metrics/                         # 指标实现
│   ├── CounterMetric.java           # 计数器
│   ├── GaugeMetric.java             # 计量器
│   ├── HistogramMetric.java         # 直方图
│   ├── TimerMetric.java             # 计时器
│   ├── SummaryMetric.java           # 摘要
│   ├── MetricsRegistry.java         # 指标注册表
│   ├── TimerSnapshot.java           # 计时器快照（Record）
│   ├── HistogramSnapshot.java       # 直方图快照（Record）
│   └── SummarySnapshot.java         # 摘要快照（Record）
│
├── tracing/                         # 链路追踪
│   ├── TraceContext.java            # 追踪上下文 V1（ThreadLocal）
│   ├── TraceContextV2.java          # 追踪上下文 V2（ScopedValue）
│   ├── SpanV2.java                  # 跨度 V2
│   ├── SpanData.java                # 跨度数据 V1（Record）
│   ├── SpanDataV2.java              # 跨度数据 V2（Record）
│   ├── SpanEvent.java               # 跨度事件（Record）
│   ├── SpanLink.java                # 跨度链接（Record）
│   ├── SpanStatus.java              # 跨度状态枚举 V1
│   ├── SpanStatusV2.java            # 跨度状态 V2（Sealed Interface）
│   ├── SpanReporterV2.java          # 跨度上报器 V2
│   ├── Baggage.java                 # W3C Baggage 实现
│   ├── BaggagePropagation.java      # Baggage 传播工具
│   └── ContextPropagation.java      # 上下文传播工具（ScopedValue）
│
├── health/                          # 健康检查
│   ├── HealthStatus.java            # 健康状态 V1
│   ├── HealthStatusV2.java          # 健康状态 V2（Sealed Interface）
│   ├── HealthCheckRegistry.java     # 健康检查注册表 V1
│   ├── HealthCheckRegistryV2.java   # V2 注册表（Virtual Thread）
│   ├── HealthCheckResults.java      # 检查结果 V1（Record）
│   ├── HealthCheckResultsV2.java    # 检查结果 V2（Record）
│   ├── HealthCheckV2.java           # 健康检查接口 V2
│   └── HealthChecks.java            # 常用检查实现（内存/磁盘/TCP/线程/数据库）
│
├── export/                          # 导出器
│   ├── MetricsExporter.java         # 导出器接口
│   ├── PrometheusExporter.java      # Prometheus 格式导出
│   ├── JSONExporter.java            # JSON 格式导出
│   ├── OtlpConfig.java             # OTLP 协议配置
│   ├── OtlpMetricsExporter.java    # OTLP 指标导出器
│   └── OtlpSpanExporter.java       # OTLP 跨度导出器
│
├── validation/                      # 验证与安全
│   ├── MetricNameValidator.java     # 指标名称验证
│   ├── MetricsLimiter.java          # 指标数量限制
│   └── TraceSanitizer.java          # 追踪数据脱敏
│
├── exception/                       # 异常
│   ├── ObservabilityException.java  # 基础异常
│   ├── ObservabilityErrorCode.java  # 错误码枚举
│   ├── MetricException.java         # 指标异常
│   ├── TracingException.java        # 追踪异常
│   └── HealthCheckException.java    # 健康检查异常
│
└── spi/                             # SPI 扩展
    └── SpanReporterProvider.java    # Span 上报器提供者接口
```

---

## 3. 指标采集（Metrics）

### 3.1 OpenMetrics - 指标门面

全局指标注册与管理入口，所有指标通过此类创建和查询。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `counter(String name)` | 指标名 | `CounterMetric` | 创建/获取计数器 |
| `counter(String name, Map<String, String> tags)` | 指标名、标签 | `CounterMetric` | 带标签的计数器 |
| `gauge(String name, Supplier<Number> supplier)` | 指标名、值提供者 | `GaugeMetric` | 创建/获取计量器 |
| `gauge(String name, Supplier<Number> supplier, Map<String, String> tags)` | 指标名、值提供者、标签 | `GaugeMetric` | 带标签的计量器 |
| `histogram(String name)` | 指标名 | `HistogramMetric` | 创建/获取直方图 |
| `histogram(String name, Map<String, String> tags)` | 指标名、标签 | `HistogramMetric` | 带标签的直方图 |
| `timer(String name)` | 指标名 | `TimerMetric` | 创建/获取计时器 |
| `timer(String name, Map<String, String> tags)` | 指标名、标签 | `TimerMetric` | 带标签的计时器 |
| `summary(String name)` | 指标名 | `SummaryMetric` | 创建/获取摘要 |
| `getAll()` | 无 | `Map<String, Metric>` | 获取所有指标 |
| `get(String name)` | 指标名 | `Metric` | 获取指定指标 |
| `remove(String name)` | 指标名 | `boolean` | 移除指标 |
| `clear()` | 无 | `void` | 清除所有指标 |
| `size()` | 无 | `int` | 指标数量 |
| `getRegistry()` | 无 | `MetricsRegistry` | 获取底层注册表 |

```java
// 创建计数器
CounterMetric counter = OpenMetrics.counter("requests_total");
counter.increment();
counter.increment(5);

// 创建计时器
TimerMetric timer = OpenMetrics.timer("request_duration");
timer.record(() -> processRequest());

// 创建计量器
GaugeMetric gauge = OpenMetrics.gauge("queue_size", queue::size);

// 创建直方图
HistogramMetric histogram = OpenMetrics.histogram("response_size");
histogram.record(1024);
```

### 3.2 CounterMetric - 计数器

单调递增的计数器，适用于请求数、错误数等场景。内部使用 `LongAdder` 实现高并发无锁计数。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `increment()` | 无 | `void` | 计数加一 |
| `increment(long n)` | 增量 | `void` | 计数加 n |
| `getCount()` | 无 | `long` | 获取当前计数值 |
| `getTags()` | 无 | `Map<String, String>` | 获取标签 |
| `getName()` | 无 | `String` | 获取指标名 |
| `getType()` | 无 | `MetricType` | 返回 COUNTER |
| `getValue()` | 无 | `Object` | 获取值（同 getCount） |
| `reset()` | 无 | `void` | 重置为零 |

```java
CounterMetric counter = OpenMetrics.counter("http_requests_total");
counter.increment();
counter.increment(10);
long total = counter.getCount();  // 11
```

### 3.3 GaugeMetric - 计量器

可上下浮动的计量器，适用于队列大小、内存使用量等场景。通过 `Supplier<Number>` 延迟获取当前值。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `getValue()` | 无 | `Number` | 获取当前值 |
| `getDoubleValue()` | 无 | `double` | 获取 double 值 |
| `getLongValue()` | 无 | `long` | 获取 long 值 |
| `getTags()` | 无 | `Map<String, String>` | 获取标签 |
| `getName()` | 无 | `String` | 获取指标名 |
| `getType()` | 无 | `MetricType` | 返回 GAUGE |
| `reset()` | 无 | `void` | 重置 |

```java
Queue<String> queue = new ConcurrentLinkedQueue<>();
GaugeMetric gauge = OpenMetrics.gauge("queue_size", queue::size);
double currentSize = gauge.getDoubleValue();
```

### 3.4 HistogramMetric - 直方图

分桶统计，适用于请求大小、响应时间分布等场景。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `record(double value)` | 观测值 | `void` | 记录一个观测值 |
| `getCount()` | 无 | `long` | 获取总记录数 |
| `getSum()` | 无 | `double` | 获取总和 |
| `getBuckets()` | 无 | `double[]` | 获取桶边界 |
| `getBucketCounts()` | 无 | `long[]` | 获取各桶计数 |
| `getCumulativeBucketCounts()` | 无 | `long[]` | 获取累计桶计数 |
| `getTags()` | 无 | `Map<String, String>` | 获取标签 |
| `getName()` | 无 | `String` | 获取指标名 |
| `getType()` | 无 | `MetricType` | 返回 HISTOGRAM |
| `reset()` | 无 | `void` | 重置 |

**HistogramSnapshot（直方图快照）：**

```java
public record HistogramSnapshot(long count, double sum, double mean, double max, double min,
                                 double[] buckets, long[] bucketCounts)
```

| 方法 | 说明 |
|------|------|
| `empty()` | 空快照 |
| `of(long count, double sum, double mean, double max, double min)` | 创建快照 |
| `stdDev()` | 标准差 |
| `range()` | 范围（max - min） |

```java
HistogramMetric histogram = OpenMetrics.histogram("response_bytes");
histogram.record(256);
histogram.record(512);
histogram.record(1024);
long count = histogram.getCount();  // 3
double sum = histogram.getSum();    // 1792.0
```

### 3.5 TimerMetric - 计时器

记录操作耗时，支持多种使用方式。内部使用 `LongAdder`（计数/总时间）和 `AtomicLong`（最大时间）。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `record(Duration duration)` | 时长 | `void` | 记录时长 |
| `recordNanos(long nanos)` | 纳秒 | `void` | 记录纳秒 |
| `recordMillis(long millis)` | 毫秒 | `void` | 记录毫秒 |
| `record(Runnable runnable)` | 可执行任务 | `void` | 自动计时执行 |
| `record(Supplier<T> supplier)` | 有返回值任务 | `T` | 自动计时执行并返回结果 |
| `recordCallable(Callable<T> callable)` | Callable 任务 | `T` | 自动计时执行（可抛受检异常） |
| `start()` | 无 | `Sample` | 开始计时，返回采样对象 |
| `getCount()` | 无 | `long` | 获取记录次数 |
| `getTotalTime()` | 无 | `Duration` | 获取总时长 |
| `getMaxTime()` | 无 | `Duration` | 获取最大时长 |
| `getAverageTime()` | 无 | `Duration` | 获取平均时长 |
| `getTotalTimeSeconds()` | 无 | `double` | 获取总时长（秒） |
| `reset()` | 无 | `void` | 重置 |

**TimerMetric.Sample（采样对象）：**

| 方法 | 说明 |
|------|------|
| `stop()` | 停止计时，返回 Duration，自动记录到 Timer |

**TimerSnapshot（计时器快照）：**

```java
public record TimerSnapshot(long count, Duration totalTime, Duration mean,
                             Duration max, Duration min, Duration p50,
                             Duration p90, Duration p95, Duration p99)
```

```java
TimerMetric timer = OpenMetrics.timer("request_duration");

// 方式1：自动计时
String result = timer.record(() -> processRequest());

// 方式2：手动计时
TimerMetric.Sample sample = timer.start();
// ... 执行操作 ...
Duration elapsed = sample.stop();

// 方式3：记录 Duration
timer.record(Duration.ofMillis(150));

// 获取统计
long count = timer.getCount();
Duration avg = timer.getAverageTime();
Duration max = timer.getMaxTime();
```

### 3.6 SummaryMetric - 摘要

带分位数的统计摘要，适用于延迟百分位等场景。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `record(double value)` | 观测值 | `void` | 记录一个观测值 |
| `getCount()` | 无 | `long` | 获取记录数 |
| `getSum()` | 无 | `double` | 获取总和 |
| `getQuantiles()` | 无 | `double[]` | 获取分位点列表 |
| `getQuantileValues()` | 无 | `Map<Double, Double>` | 获取分位数值映射 |
| `getQuantile(double quantile)` | 分位点 | `double` | 获取指定分位数的值 |
| `reset()` | 无 | `void` | 重置 |

**SummarySnapshot（摘要快照）：**

```java
public record SummarySnapshot(long count, double sum,
                               Map<Double, Double> quantiles)
```

| 方法 | 说明 |
|------|------|
| `empty()` | 空快照 |
| `of(long count, double sum, double p50, double p90, double p99)` | 创建快照 |
| `getQuantile(double quantile)` | 获取分位数值 |
| `median()` | P50 |
| `p95()` | P95 |
| `p99()` | P99 |

```java
SummaryMetric summary = OpenMetrics.summary("request_latency");
summary.record(10.5);
summary.record(25.3);
summary.record(100.8);

double p99 = summary.getQuantile(0.99);
Map<Double, Double> quantiles = summary.getQuantileValues();
```

### 3.7 MetricsRegistry - 指标注册表

底层指标注册表，`OpenMetrics` 的委托对象。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `counter(String name)` | 指标名 | `CounterMetric` | 创建/获取计数器 |
| `counter(String name, Map<String, String> tags)` | 指标名、标签 | `CounterMetric` | 带标签 |
| `gauge(String name, Supplier<Number> supplier)` | 指标名、提供者 | `GaugeMetric` | 创建/获取计量器 |
| `gauge(String name, Supplier<Number> supplier, Map<String, String> tags)` | 指标名、提供者、标签 | `GaugeMetric` | 带标签 |
| `histogram(String name)` | 指标名 | `HistogramMetric` | 创建/获取直方图 |
| `histogram(String name, Map<String, String> tags)` | 指标名、标签 | `HistogramMetric` | 带标签 |
| `timer(String name)` | 指标名 | `TimerMetric` | 创建/获取计时器 |
| `timer(String name, Map<String, String> tags)` | 指标名、标签 | `TimerMetric` | 带标签 |
| `summary(String name)` | 指标名 | `SummaryMetric` | 创建/获取摘要 |
| `summary(String name, Map<String, String> tags)` | 指标名、标签 | `SummaryMetric` | 带标签 |
| `getAll()` | 无 | `Map<String, Metric>` | 获取所有指标 |
| `get(String name)` | 指标名 | `Metric` | 获取指定指标 |
| `remove(String name)` | 指标名 | `boolean` | 移除指标 |
| `clear()` | 无 | `void` | 清除所有 |
| `size()` | 无 | `int` | 数量 |
| `contains(String name)` | 指标名 | `boolean` | 是否包含 |

---

## 4. 链路追踪（Tracing）

模块提供 V1（基于 ThreadLocal）和 V2（基于 ScopedValue + Virtual Thread）两套追踪实现。V2 推荐在 JDK 25 环境使用。

### 4.1 OpenTracing - 追踪门面 V1

基于 ThreadLocal 的追踪门面，适用于传统线程模型。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `startSpan(String name)` | 跨度名 | `Span` | 开始新跨度（自动关联父跨度） |
| `startRootSpan(String name)` | 跨度名 | `Span` | 开始根跨度（新追踪链） |
| `startSpan(String name, String traceId)` | 跨度名、Trace ID | `Span` | 指定 Trace ID |
| `trace(String name, Runnable runnable)` | 名称、任务 | `void` | 在跨度中执行无返回值任务 |
| `trace(String name, Supplier<T> supplier)` | 名称、任务 | `T` | 在跨度中执行有返回值任务 |
| `currentContext()` | 无 | `TraceContext` | 获取当前追踪上下文 |
| `currentTraceId()` | 无 | `String` | 获取当前 Trace ID |
| `currentSpanId()` | 无 | `String` | 获取当前 Span ID |
| `setBaggage(String key, String value)` | 键、值 | `void` | 设置 Baggage |
| `getBaggage(String key)` | 键 | `String` | 获取 Baggage |
| `clearContext()` | 无 | `void` | 清除上下文 |
| `extractTraceParent()` | 无 | `String` | 导出 W3C traceparent |
| `injectTraceParent(String)` | traceparent 头 | `void` | 注入 W3C traceparent |

```java
// 方式1：try-with-resources
try (Span span = OpenTracing.startSpan("processOrder")) {
    span.tag("orderId", "12345");
    // 处理逻辑
    span.log("Order processed successfully");
}

// 方式2：函数式
String result = OpenTracing.trace("compute", () -> {
    return expensiveComputation();
});

// Baggage 传递
OpenTracing.setBaggage("userId", "12345");
String userId = OpenTracing.getBaggage("userId");
```

### 4.2 Span - 跨度 V1

表示一个追踪操作单元，实现 `AutoCloseable`，支持 try-with-resources。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `tag(String key, String value)` | 键、值 | `Span` | 添加字符串标签 |
| `tag(String key, Object value)` | 键、对象 | `Span` | 添加对象标签 |
| `log(String message)` | 消息 | `Span` | 添加日志条目 |
| `error(Throwable t)` | 异常 | `Span` | 标记错误 |
| `setStatus(SpanStatus status)` | 状态 | `Span` | 设置状态 |
| `close()` | 无 | `void` | 结束跨度并上报 |
| `getName()` | 无 | `String` | 跨度名称 |
| `getContext()` | 无 | `TraceContext` | 追踪上下文 |
| `getStartTime()` | 无 | `Instant` | 开始时间 |
| `getEndTime()` | 无 | `Instant` | 结束时间 |
| `getTags()` | 无 | `Map<String, String>` | 所有标签（不可变） |
| `getLogs()` | 无 | `List<LogEntry>` | 所有日志（不可变） |
| `getStatus()` | 无 | `SpanStatus` | 状态 |
| `getTraceId()` | 无 | `String` | Trace ID |
| `getSpanId()` | 无 | `String` | Span ID |
| `getParentSpanId()` | 无 | `String` | 父 Span ID |
| `getDuration()` | 无 | `Duration` | 持续时长 |
| `isFinished()` | 无 | `boolean` | 是否已结束 |

**Span.LogEntry（日志条目）：**

```java
public record LogEntry(Instant timestamp, String message) {}
```

### 4.3 TraceContext - 追踪上下文 V1

基于 ThreadLocal 的追踪上下文，管理 Trace ID、Span ID、父 Span ID 和 Baggage。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `current()` | 无 | `TraceContext` | 获取当前线程上下文 |
| `set(TraceContext context)` | 上下文 | `void` | 设置当前线程上下文 |
| `clear()` | 无 | `void` | 清除当前线程上下文 |
| `runWhere(TraceContext ctx, Runnable task)` | 上下文、任务 | `void` | 在指定上下文中执行 |
| `callWhere(TraceContext ctx, Callable<R> task)` | 上下文、任务 | `R` | 在指定上下文中执行并返回结果 |
| `create()` | 无 | `TraceContext` | 创建新根上下文 |
| `createWithTraceId(String traceId)` | Trace ID | `TraceContext` | 指定 Trace ID 创建 |
| `createChild()` | 无 | `TraceContext` | 创建子上下文 |
| `setBaggage(String key, String value)` | 键、值 | `TraceContext` | 设置 Baggage |
| `getBaggage(String key)` | 键 | `String` | 获取 Baggage |
| `getAllBaggage()` | 无 | `Map<String, String>` | 获取全部 Baggage |
| `getTraceId()` | 无 | `String` | Trace ID |
| `getSpanId()` | 无 | `String` | Span ID |
| `getParentSpanId()` | 无 | `String` | 父 Span ID |
| `isRoot()` | 无 | `boolean` | 是否根跨度 |
| `toTraceParent()` | 无 | `String` | 导出 W3C traceparent |
| `fromTraceParent(String)` | traceparent | `TraceContext` | 从 W3C traceparent 解析 |

### 4.4 OpenTracingV2 - 追踪门面 V2（ScopedValue + Virtual Thread）

基于 ScopedValue 的追踪门面，Virtual Thread 友好，自动上下文传播，无内存泄漏风险。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `startSpan(String name)` | 跨度名 | `SpanV2` | 开始新跨度 |
| `startSpan(String name, String traceId)` | 跨度名、Trace ID | `SpanV2` | 指定 Trace ID |
| `trace(String name, CallableOp<T, X> action)` | 名称、任务 | `T` | 在跨度中执行（可抛受检异常） |
| `trace(String name, Runnable action)` | 名称、任务 | `void` | 在跨度中执行 |
| `traceUnchecked(String name, CallableOp<T, RuntimeException> action)` | 名称、任务 | `T` | 无受检异常版本 |
| `traceParallel(String name, List<Callable<T>> tasks)` | 名称、任务列表 | `List<T>` | 并行追踪多个任务（VT + StructuredTaskScope） |
| `currentTraceId()` | 无 | `String` | 当前 Trace ID |
| `currentSpanId()` | 无 | `String` | 当前 Span ID |
| `isTracing()` | 无 | `boolean` | 是否处于追踪中 |

```java
// 基本追踪
String result = OpenTracingV2.trace("fetchData", () -> {
    return fetchFromDatabase();
});

// 并行追踪
List<Order> orders = OpenTracingV2.traceParallel("batchProcess",
    List.of(
        () -> processOrder("order-1"),
        () -> processOrder("order-2"),
        () -> processOrder("order-3")
    ));
```

### 4.5 SpanV2 - 跨度 V2

增强版跨度，支持属性（多类型）、事件、链接，线程安全（ConcurrentHashMap + CopyOnWriteArrayList）。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `setAttribute(String key, String value)` | 键、字符串值 | `SpanV2` | 设置字符串属性 |
| `setAttribute(String key, long value)` | 键、长整型值 | `SpanV2` | 设置长整型属性 |
| `setAttribute(String key, double value)` | 键、双精度值 | `SpanV2` | 设置双精度属性 |
| `setAttribute(String key, boolean value)` | 键、布尔值 | `SpanV2` | 设置布尔属性 |
| `getAttribute(String key)` | 键 | `String` | 获取属性值 |
| `addEvent(String name)` | 事件名 | `SpanV2` | 添加事件 |
| `addEvent(String name, Map<String, String> attributes)` | 事件名、属性 | `SpanV2` | 添加带属性的事件 |
| `addLink(TraceContextV2 linkedContext)` | 关联上下文 | `SpanV2` | 添加链接 |
| `addLink(TraceContextV2, Map<String, String>)` | 关联上下文、属性 | `SpanV2` | 添加带属性的链接 |
| `success()` | 无 | `void` | 标记成功 |
| `success(String description)` | 描述 | `void` | 标记成功（带描述） |
| `error(Throwable t)` | 异常 | `void` | 标记错误 |
| `timeout(Duration elapsed)` | 耗时 | `void` | 标记超时 |
| `cancelled()` | 无 | `void` | 标记取消 |
| `name()` | 无 | `String` | 跨度名称 |
| `context()` | 无 | `TraceContextV2` | 追踪上下文 |
| `status()` | 无 | `SpanStatusV2` | 状态 |
| `duration()` | 无 | `Duration` | 持续时长 |
| `isEnded()` | 无 | `boolean` | 是否已结束 |
| `toSpanData()` | 无 | `SpanDataV2` | 转换为不可变数据对象 |
| `close()` | 无 | `void` | 结束跨度并上报 |

### 4.6 TraceContextV2 - 追踪上下文 V2（ScopedValue）

基于 ScopedValue 的不可变追踪上下文，Virtual Thread 友好。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `create()` | 无 | `TraceContextV2` | 创建新根上下文 |
| `create(String traceId)` | Trace ID | `TraceContextV2` | 指定 Trace ID 创建 |
| `createChild()` | 无 | `TraceContextV2` | 创建子上下文 |
| `withBaggage(String key, String value)` | 键、值 | `TraceContextV2` | 添加 Baggage（返回新实例） |
| `getBaggage(String key)` | 键 | `Optional<String>` | 获取 Baggage |
| `run(TraceContextV2 context, CallableOp<T, X> task)` | 上下文、任务 | `T` | 在上下文中执行 |
| `run(TraceContextV2 context, Runnable task)` | 上下文、任务 | `void` | 在上下文中执行 |
| `current()` | 无 | `Optional<TraceContextV2>` | 获取当前上下文 |
| `currentOrCreate()` | 无 | `TraceContextV2` | 获取或创建上下文 |
| `traceId()` | 无 | `String` | Trace ID |
| `spanId()` | 无 | `String` | Span ID |
| `parentSpanId()` | 无 | `Optional<String>` | 父 Span ID |
| `baggage()` | 无 | `Map<String, String>` | 全部 Baggage |
| `startTime()` | 无 | `Instant` | 创建时间 |

```java
TraceContextV2 ctx = TraceContextV2.create();
TraceContextV2.run(ctx, () -> {
    String traceId = TraceContextV2.current().get().traceId();
    // 子任务自动继承上下文
    TraceContextV2 child = TraceContextV2.currentOrCreate().createChild();
});
```

### 4.7 SpanStatus / SpanStatusV2 - 跨度状态

**SpanStatus V1（枚举）：**

```java
public enum SpanStatus {
    OK, ERROR, TIMEOUT, CANCELLED, UNKNOWN;

    public String getCode();
    public String getDescription();
    public boolean isOk();
    public boolean isError();
}
```

**SpanStatusV2（Sealed Interface）：**

```java
public sealed interface SpanStatusV2
    permits SpanStatusV2.Ok, SpanStatusV2.Error, SpanStatusV2.Timeout, SpanStatusV2.Cancelled {

    boolean isSuccess();

    record Ok(String description) implements SpanStatusV2 { ... }
    record Error(String description, Throwable cause) implements SpanStatusV2 { ... }
    record Timeout(String description, Duration elapsed) implements SpanStatusV2 { ... }
    record Cancelled(String description) implements SpanStatusV2 { ... }
}
```

```java
// 模式匹配
switch (span.status()) {
    case SpanStatusV2.Ok ok -> log.info("Success: {}", ok.description());
    case SpanStatusV2.Error err -> log.error("Error: {}", err.cause());
    case SpanStatusV2.Timeout to -> log.warn("Timeout after {}", to.elapsed());
    case SpanStatusV2.Cancelled c -> log.info("Cancelled: {}", c.description());
}
```

### 4.8 SpanData / SpanDataV2 - 跨度数据

不可变的跨度快照数据。

**SpanData V1：**

```java
public record SpanData(String name, String traceId, String spanId, String parentSpanId,
                        Instant startTime, Instant endTime, SpanStatus status,
                        Map<String, String> attributes, List<SpanEvent> events,
                        List<SpanLink> links) {
    public Duration getDuration();
    public long getDurationMillis();
    public boolean isRoot();
    public boolean isError();
}
```

**SpanDataV2：**

```java
public record SpanDataV2(String name, String traceId, String spanId, String parentSpanId,
                          Instant startTime, Instant endTime, SpanStatusV2 status,
                          Map<String, String> attributes, List<SpanEvent> events,
                          List<SpanLink> links) {
    public Duration duration();
    public long durationMillis();
    public boolean isRoot();
    public boolean isSuccess();
    public boolean isError();
}
```

### 4.9 SpanEvent / SpanLink - 跨度事件与链接

**SpanEvent（跨度事件）：**

```java
public record SpanEvent(String name, Instant timestamp, Map<String, String> attributes) {
    public static SpanEvent of(String name);
    public static SpanEvent of(String name, Map<String, String> attributes);
    public static SpanEvent exception(Throwable t);
}
```

**SpanLink（跨度链接）：**

```java
public record SpanLink(String traceId, String spanId, Map<String, String> attributes) {
    public static SpanLink of(String traceId, String spanId);
    public static SpanLink of(String traceId, String spanId, Map<String, String> attributes);
    public static SpanLink fromContext(TraceContext context);
}
```

### 4.10 SpanReporterV2 - 跨度上报器

静态上报器，支持注册多个上报回调。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `report(SpanDataV2 span)` | 跨度数据 | `void` | 上报单个跨度 |
| `reportBatch(List<SpanDataV2> spans)` | 跨度列表 | `void` | 批量上报 |
| `addReporter(Consumer<SpanDataV2>)` | 上报回调 | `void` | 注册上报器 |
| `removeReporter(Consumer<SpanDataV2>)` | 上报回调 | `void` | 移除上报器 |
| `clearReporters()` | 无 | `void` | 清除所有上报器 |
| `reporterCount()` | 无 | `int` | 上报器数量 |
| `console()` | 无 | `Consumer<SpanDataV2>` | 控制台上报器 |
| `noop()` | 无 | `Consumer<SpanDataV2>` | 空操作上报器 |

```java
// 注册控制台上报器
SpanReporterV2.addReporter(SpanReporterV2.console());

// 注册自定义上报器
SpanReporterV2.addReporter(span -> {
    log.info("Span: {} duration={}ms", span.name(), span.durationMillis());
});
```

### 4.11 Baggage - W3C Baggage

W3C Baggage 规范实现，用于跨服务传递任意上下文数据。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `empty()` | 无 | `Baggage` | 空 Baggage |
| `of(String key, String value)` | 键、值 | `Baggage` | 单条 Baggage |
| `builder()` | 无 | `Builder` | 创建 Builder |
| `fromHeaderValue(String)` | 头部值 | `Baggage` | 从 HTTP 头解析 |
| `get(String key)` | 键 | `Optional<String>` | 获取值 |
| `getEntry(String key)` | 键 | `Optional<BaggageEntry>` | 获取条目 |
| `keys()` | 无 | `Set<String>` | 所有键 |
| `entries()` | 无 | `Collection<BaggageEntry>` | 所有条目 |
| `size()` | 无 | `int` | 条目数 |
| `isEmpty()` | 无 | `boolean` | 是否为空 |
| `containsKey(String key)` | 键 | `boolean` | 是否包含键 |
| `with(String key, String value)` | 键、值 | `Baggage` | 添加条目（返回新实例） |
| `without(String key)` | 键 | `Baggage` | 移除条目（返回新实例） |
| `merge(Baggage other)` | 另一 Baggage | `Baggage` | 合并 |
| `toHeaderValue()` | 无 | `String` | 转为 HTTP 头部值 |
| `toMap()` | 无 | `Map<String, String>` | 转为 Map |

**BaggageEntry（条目）：**

```java
public record BaggageEntry(String key, String value, Map<String, String> properties) {
    public Optional<String> getProperty(String propertyKey);
}
```

```java
Baggage baggage = Baggage.builder()
    .put("userId", "12345")
    .put("requestId", "abc-def")
    .build();

String userId = baggage.get("userId").orElse("unknown");
String header = baggage.toHeaderValue();
Baggage parsed = Baggage.fromHeaderValue(header);
```

### 4.12 ContextPropagation - 上下文传播工具

基于 ScopedValue 的上下文传播工具，提供上下文绑定、包装和跨线程传递。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `runWithContext(TraceContextV2, CallableOp)` | 上下文、任务 | `T` | 在上下文中执行 |
| `runWithContext(TraceContextV2, Runnable)` | 上下文、任务 | `void` | 在上下文中执行 |
| `current()` | 无 | `Optional<TraceContextV2>` | 获取当前上下文 |
| `currentOrCreate()` | 无 | `TraceContextV2` | 获取或创建 |
| `currentTraceId()` | 无 | `Optional<String>` | 当前 Trace ID |
| `currentSpanId()` | 无 | `Optional<String>` | 当前 Span ID |
| `wrap(TraceContextV2, Callable<T>)` | 上下文、Callable | `Callable<T>` | 包装 Callable |
| `wrap(TraceContextV2, Runnable)` | 上下文、Runnable | `Runnable` | 包装 Runnable |
| `wrapWithCurrent(Callable<T>)` | Callable | `Callable<T>` | 用当前上下文包装 |
| `wrapWithCurrent(Runnable)` | Runnable | `Runnable` | 用当前上下文包装 |
| `wrapExecutor(Executor)` | Executor | `Executor` | 包装 Executor |
| `wrapExecutorService(ExecutorService)` | ExecutorService | `ContextAwareExecutor` | 包装 ExecutorService |
| `extractTraceParent()` | 无 | `Optional<String>` | 导出 W3C traceparent |
| `parseTraceParent(String)` | traceparent | `Optional<TraceContextV2>` | 解析 traceparent |

### 4.13 BaggagePropagation - Baggage 传播工具

基于 ScopedValue 的 Baggage 传播工具。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `current()` | 无 | `Optional<Baggage>` | 当前 Baggage |
| `currentOrEmpty()` | 无 | `Baggage` | 当前或空 Baggage |
| `get(String key)` | 键 | `Optional<String>` | 获取当前 Baggage 值 |
| `getOrDefault(String key, String default)` | 键、默认值 | `String` | 获取值或默认值 |
| `contains(String key)` | 键 | `boolean` | 当前 Baggage 是否包含 |
| `runWithBaggage(Baggage, CallableOp)` | Baggage、任务 | `T` | 在 Baggage 上下文中执行 |
| `runWithBaggage(Baggage, Runnable)` | Baggage、任务 | `void` | 在 Baggage 上下文中执行 |
| `runWith(String key, String value, CallableOp)` | 键、值、任务 | `T` | 添加单条 Baggage 并执行 |
| `runWithContext(TraceContextV2, Baggage, CallableOp)` | 追踪上下文、Baggage、任务 | `T` | 同时绑定追踪和 Baggage |
| `wrap(Baggage, Callable<T>)` | Baggage、Callable | `Callable<T>` | 包装 Callable |
| `wrap(Baggage, Runnable)` | Baggage、Runnable | `Runnable` | 包装 Runnable |
| `wrapWithCurrent(Callable<T>)` | Callable | `Callable<T>` | 用当前 Baggage 包装 |
| `wrapExecutor(Executor)` | Executor | `Executor` | 包装 Executor |
| `wrapExecutorService(ExecutorService)` | ExecutorService | `BaggageAwareExecutor` | 包装 ExecutorService |
| `inject(BiConsumer<String, String>)` | 注入器 | `void` | 注入当前 Baggage 到 HTTP 头 |
| `inject(Baggage, BiConsumer<String, String>)` | Baggage、注入器 | `void` | 注入指定 Baggage |
| `extract(Function<String, String>)` | 提取器 | `Baggage` | 从 HTTP 头提取 |
| `extractFromHeaders(Map<String, String>)` | 头部 Map | `Baggage` | 从头部 Map 提取 |
| `injectW3C(BiConsumer<String, String>)` | 注入器 | `void` | 注入 W3C 格式（traceparent + baggage） |
| `extractW3C(Function<String, String>)` | 提取器 | `PropagationContext` | 提取 W3C 格式 |

```java
Baggage baggage = Baggage.of("userId", "12345");
BaggagePropagation.runWithBaggage(baggage, () -> {
    String userId = BaggagePropagation.get("userId").orElse("unknown");
    // HTTP 请求注入
    HttpRequest.Builder builder = HttpRequest.newBuilder();
    BaggagePropagation.inject((name, value) -> builder.header(name, value));
});

// 从 HTTP 响应提取
Baggage extracted = BaggagePropagation.extract(headers::get);
```

---

## 5. 健康检查（Health Check）

### 5.1 HealthCheck - 健康检查接口

```java
public interface HealthCheck {
    HealthStatus check();
}
```

### 5.2 HealthStatus - 健康状态 V1

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `healthy()` | 无 | `HealthStatus` | 健康状态 |
| `healthy(String message)` | 消息 | `HealthStatus` | 带消息的健康状态 |
| `healthy(Map<String, Object> details)` | 详情 | `HealthStatus` | 带详情的健康状态 |
| `unhealthy(String message)` | 消息 | `HealthStatus` | 不健康状态 |
| `unhealthy(String, Map<String, Object>)` | 消息、详情 | `HealthStatus` | 带详情的不健康状态 |
| `unhealthy(Throwable t)` | 异常 | `HealthStatus` | 异常导致的不健康 |
| `unknown()` | 无 | `HealthStatus` | 未知状态 |
| `outOfService(String message)` | 消息 | `HealthStatus` | 停用状态 |
| `getStatus()` | 无 | `Status` | 获取状态枚举 |
| `getMessage()` | 无 | `String` | 获取消息 |
| `getDetails()` | 无 | `Map<String, Object>` | 获取详情 |
| `isHealthy()` | 无 | `boolean` | 是否健康 |
| `isUnhealthy()` | 无 | `boolean` | 是否不健康 |

**HealthStatus.Status 枚举：** `UP`、`DOWN`、`UNKNOWN`、`OUT_OF_SERVICE`

### 5.3 HealthStatusV2 - 健康状态 V2（Sealed Interface）

```java
public sealed interface HealthStatusV2
    permits HealthStatusV2.Up, HealthStatusV2.Down, HealthStatusV2.Unknown {

    boolean isHealthy();

    record Up(String message, Map<String, Object> details) implements HealthStatusV2 { ... }
    record Down(String message, Map<String, Object> details) implements HealthStatusV2 { ... }
    record Unknown(String message, Map<String, Object> details) implements HealthStatusV2 { ... }
}
```

```java
// 模式匹配
switch (status) {
    case HealthStatusV2.Up up -> log.info("Healthy: {}", up.message());
    case HealthStatusV2.Down down -> log.error("Unhealthy: {}", down.message());
    case HealthStatusV2.Unknown u -> log.warn("Unknown: {}", u.message());
}
```

### 5.4 HealthCheckRegistry - 注册表 V1

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `register(String name, HealthCheck check)` | 名称、检查器 | `HealthCheckRegistry` | 注册检查 |
| `unregister(String name)` | 名称 | `HealthCheckRegistry` | 注销检查 |
| `runAll()` | 无 | `HealthCheckResults` | 运行所有检查 |
| `run(String name)` | 名称 | `HealthStatus` | 运行指定检查 |
| `getAll()` | 无 | `Map<String, HealthCheck>` | 获取所有检查 |
| `size()` | 无 | `int` | 检查数量 |
| `clear()` | 无 | `void` | 清除所有 |

### 5.5 HealthCheckRegistryV2 - 注册表 V2（Virtual Thread）

支持 Virtual Thread 并行执行健康检查。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `global()` | 无 | `HealthCheckRegistryV2` | 获取全局实例 |
| `register(String name, HealthCheckV2 check)` | 名称、检查器 | `void` | 注册检查 |
| `unregister(String name)` | 名称 | `void` | 注销检查 |
| `getCheckNames()` | 无 | `Set<String>` | 获取所有检查名 |
| `isRegistered(String name)` | 名称 | `boolean` | 是否已注册 |
| `checkAll()` | 无 | `HealthCheckResultsV2` | 并行运行所有检查 |
| `checkAll(Duration timeout)` | 超时时长 | `HealthCheckResultsV2` | 带超时的并行检查 |
| `check(String name)` | 名称 | `Optional<HealthStatusV2>` | 运行指定检查 |
| `clear()` | 无 | `void` | 清除所有 |

```java
HealthCheckRegistryV2 registry = HealthCheckRegistryV2.global();
registry.register("database", () -> {
    // 检查数据库连接
    return HealthStatusV2.up();
});
registry.register("redis", () -> {
    // 检查 Redis 连接
    return HealthStatusV2.up();
});

// 并行检查（5秒超时）
HealthCheckResultsV2 results = registry.checkAll(Duration.ofSeconds(5));
boolean healthy = results.isHealthy();
long unhealthyCount = results.unhealthyCount();
```

### 5.6 HealthCheckResults / HealthCheckResultsV2 - 检查结果

**HealthCheckResults V1：**

```java
public record HealthCheckResults(Map<String, HealthStatus> results) {
    public boolean isHealthy();
    public boolean hasUnhealthy();
    public HealthStatus getOverallStatus();
    public Map<String, HealthStatus> getHealthy();
    public Map<String, HealthStatus> getUnhealthy();
    public HealthStatus get(String name);
    public int size();
}
```

**HealthCheckResultsV2：**

```java
public record HealthCheckResultsV2(Map<String, HealthStatusV2> results, Duration totalDuration) {
    public boolean isHealthy();
    public HealthStatusV2 overall();
    public Optional<HealthStatusV2> get(String name);
    public long healthyCount();
    public long unhealthyCount();
    public int size();
    public boolean isEmpty();
}
```

### 5.7 HealthChecks - 常用检查实现

提供开箱即用的健康检查工厂方法。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `of(BooleanSupplier supplier)` | 布尔提供者 | `HealthCheck` | 从布尔条件创建 |
| `of(Supplier<HealthStatus> supplier)` | 状态提供者 | `HealthCheck` | 从状态提供者创建 |
| `memory(double maxUsagePercent)` | 最大内存使用率 | `HealthCheck` | 内存使用检查 |
| `diskSpace(String path, long minFreeBytes)` | 路径、最小空闲字节 | `HealthCheck` | 磁盘空间检查 |
| `tcp(String host, int port, Duration timeout)` | 主机、端口、超时 | `HealthCheck` | TCP 连通性检查 |
| `threadCount(int maxThreads)` | 最大线程数 | `HealthCheck` | 线程数检查 |
| `alwaysHealthy()` | 无 | `HealthCheck` | 始终健康 |
| `alwaysUnhealthy(String reason)` | 原因 | `HealthCheck` | 始终不健康 |
| `database(DataSource dataSource)` | 数据源 | `HealthCheck` | 数据库连接检查 |
| `database(DataSource dataSource, Duration timeout)` | 数据源、超时 | `HealthCheck` | 带超时的数据库检查 |

```java
HealthCheckRegistry registry = new HealthCheckRegistry();
registry.register("memory", HealthChecks.memory(0.9));          // 内存使用率 < 90%
registry.register("disk", HealthChecks.diskSpace("/", 1_000_000_000L));  // 磁盘 > 1GB
registry.register("db", HealthChecks.database(dataSource, Duration.ofSeconds(3)));
registry.register("redis", HealthChecks.tcp("localhost", 6379, Duration.ofSeconds(2)));

HealthCheckResults results = registry.runAll();
boolean allHealthy = results.isHealthy();
```

---

## 6. 导出器（Export）

### 6.1 MetricsExporter - 导出器接口

```java
public interface MetricsExporter {
    String export(Collection<? extends Metric> metrics);
    String getContentType();
    String getFormatName();
}
```

### 6.2 PrometheusExporter - Prometheus 格式导出

将指标导出为 Prometheus 文本格式（text/plain; version=0.0.4）。

```java
PrometheusExporter exporter = new PrometheusExporter();
String output = exporter.export(OpenMetrics.getAll().values());
// 输出 Prometheus 格式的指标文本
```

| 方法 | 说明 |
|------|------|
| `export(Collection<? extends Metric>)` | 导出为 Prometheus 文本格式 |
| `getContentType()` | 返回 `text/plain; version=0.0.4; charset=utf-8` |
| `getFormatName()` | 返回 `prometheus` |

### 6.3 JSONExporter - JSON 格式导出

将指标导出为 JSON 格式。

```java
JSONExporter exporter = new JSONExporter(true);  // prettyPrint
String json = exporter.export(OpenMetrics.getAll().values());
```

| 方法 | 说明 |
|------|------|
| `JSONExporter()` | 默认构造（不美化） |
| `JSONExporter(boolean prettyPrint)` | 指定是否美化输出 |
| `export(Collection<? extends Metric>)` | 导出为 JSON |
| `getContentType()` | 返回 `application/json` |
| `getFormatName()` | 返回 `json` |

### 6.4 OtlpConfig - OTLP 协议配置

OpenTelemetry Protocol 配置，支持 Builder 模式。

| 常量/方法 | 说明 |
|----------|------|
| `DEFAULT_ENDPOINT` | `http://localhost:4318` |
| `TRACES_PATH` | `/v1/traces` |
| `METRICS_PATH` | `/v1/metrics` |
| `DEFAULT_TIMEOUT` | 10 秒 |
| `DEFAULT_BATCH_SIZE` | 512 |
| `DEFAULT_EXPORT_INTERVAL` | 5 秒 |
| `defaults()` | 默认配置 |
| `of(String endpoint)` | 指定端点 |
| `builder()` | Builder 模式 |

**Builder 方法：**

| 方法 | 说明 |
|------|------|
| `endpoint(String)` | 设置端点 |
| `tracesEndpoint(String)` | 设置追踪端点 |
| `metricsEndpoint(String)` | 设置指标端点 |
| `timeout(Duration)` | 设置超时 |
| `header(String name, String value)` | 添加 HTTP 头 |
| `headers(Map<String, String>)` | 设置 HTTP 头 |
| `compression(Compression)` | 设置压缩（NONE / GZIP） |
| `serviceName(String)` | 服务名 |
| `serviceVersion(String)` | 服务版本 |
| `serviceNamespace(String)` | 服务命名空间 |
| `resourceAttribute(String key, String value)` | 资源属性 |
| `resourceAttributes(Map<String, String>)` | 资源属性集合 |
| `batchSize(int)` | 批量大小 |
| `exportInterval(Duration)` | 导出间隔 |
| `enabled(boolean)` | 是否启用 |
| `build()` | 构建配置 |

```java
OtlpConfig config = OtlpConfig.builder()
    .endpoint("http://otel-collector:4318")
    .serviceName("my-service")
    .serviceVersion("1.0.0")
    .batchSize(256)
    .exportInterval(Duration.ofSeconds(10))
    .build();
```

### 6.5 OtlpMetricsExporter - OTLP 指标导出器

向 OTLP 端点导出指标数据，支持定期导出。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `create()` | 无 | `OtlpMetricsExporter` | 默认配置创建 |
| `create(OtlpConfig config)` | 配置 | `OtlpMetricsExporter` | 指定配置创建 |
| `create(String endpoint)` | 端点 | `OtlpMetricsExporter` | 指定端点创建 |
| `export(Collection<? extends Metric>)` | 指标集合 | `String` | 导出为 JSON |
| `exportToEndpoint(Collection<? extends Metric>)` | 指标集合 | `ExportResult` | 导出到端点 |
| `exportRegistry(MetricsRegistry)` | 注册表 | `ExportResult` | 导出整个注册表 |
| `startPeriodicExport(MetricsRegistry)` | 注册表 | `void` | 开始定期导出 |
| `stopPeriodicExport()` | 无 | `void` | 停止定期导出 |
| `getExportedCount()` | 无 | `long` | 已导出数量 |
| `getFailedCount()` | 无 | `long` | 失败数量 |
| `getConfig()` | 无 | `OtlpConfig` | 获取配置 |
| `isClosed()` | 无 | `boolean` | 是否已关闭 |
| `close()` | 无 | `void` | 关闭导出器 |

**ExportResult（密封接口）：**

```java
public sealed interface ExportResult {
    boolean isSuccess();
    record Success(int count) implements ExportResult { ... }
    record Failure(String error, Throwable cause) implements ExportResult { ... }
}
```

### 6.6 OtlpSpanExporter - OTLP 跨度导出器

向 OTLP 端点导出跨度数据，支持批量和异步导出。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `create()` | 无 | `OtlpSpanExporter` | 默认配置创建 |
| `create(OtlpConfig config)` | 配置 | `OtlpSpanExporter` | 指定配置创建 |
| `create(String endpoint)` | 端点 | `OtlpSpanExporter` | 指定端点创建 |
| `export(SpanDataV2)` | 跨度数据 | `void` | 导出单个跨度 |
| `exportBatch(List<SpanDataV2>)` | 跨度列表 | `ExportResult` | 批量导出 |
| `flush()` | 无 | `ExportResult` | 刷新缓冲区 |
| `asReporter()` | 无 | `Consumer<SpanDataV2>` | 转为上报器回调 |
| `getExportedCount()` | 无 | `long` | 已导出数量 |
| `getFailedCount()` | 无 | `long` | 失败数量 |
| `getQueueSize()` | 无 | `int` | 队列大小 |
| `close()` | 无 | `void` | 关闭导出器 |

```java
OtlpSpanExporter exporter = OtlpSpanExporter.create(config);

// 注册为上报器
SpanReporterV2.addReporter(exporter.asReporter());

// 手动导出
ExportResult result = exporter.flush();
```

---

## 7. 验证与安全

### 7.1 MetricNameValidator - 指标名称验证

验证指标名称和标签是否符合 Prometheus 规范。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `isValidPrometheusName(String name)` | 名称 | `boolean` | 是否合法指标名 |
| `isValidPrometheusLabel(String label)` | 标签 | `boolean` | 是否合法标签名 |
| `sanitizePrometheusName(String name)` | 名称 | `String` | 清理不合法字符 |
| `sanitizePrometheusLabel(String label)` | 标签 | `String` | 清理不合法标签字符 |
| `isReservedName(String name)` | 名称 | `boolean` | 是否保留名称 |

### 7.2 MetricsLimiter - 指标数量限制

防止指标爆炸（Cardinality Explosion）。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `MetricsLimiter()` | 无 | - | 默认限制构造 |
| `MetricsLimiter(int, int, int, int)` | 最大指标数、最大标签数、最大标签名长度、最大标签值长度 | - | 自定义限制 |
| `tryAcquire()` | 无 | `boolean` | 尝试获取配额 |
| `release()` | 无 | `void` | 释放配额 |
| `isValidLabelCount(int)` | 标签数 | `boolean` | 标签数是否合法 |
| `isValidLabelName(String)` | 标签名 | `boolean` | 标签名长度是否合法 |
| `isValidLabelValue(String)` | 标签值 | `boolean` | 标签值长度是否合法 |
| `getMetricCount()` | 无 | `int` | 当前指标数 |
| `getDroppedCount()` | 无 | `long` | 丢弃数 |
| `reset()` | 无 | `void` | 重置 |

### 7.3 TraceSanitizer - 追踪数据脱敏

验证和清理追踪相关数据。

| 方法 | 参数 | 返回类型 | 说明 |
|------|------|---------|------|
| `isValidTraceId(String)` | Trace ID | `boolean` | 是否合法 Trace ID |
| `isValidSpanId(String)` | Span ID | `boolean` | 是否合法 Span ID |
| `sanitizeSpanName(String)` | 跨度名 | `String` | 清理跨度名 |
| `sanitizeAttributeKey(String)` | 属性键 | `String` | 清理属性键 |
| `sanitizeAttributeValue(String)` | 属性值 | `String` | 清理属性值 |
| `sanitizeAttributes(Map<String, String>)` | 属性 Map | `Map<String, String>` | 清理属性集合 |
| `normalizeTraceId(String)` | Trace ID | `String` | 规范化 Trace ID |
| `normalizeSpanId(String)` | Span ID | `String` | 规范化 Span ID |

---

## 8. 异常体系

### 8.1 异常层次结构

```
RuntimeException
└── ObservabilityException                  # 可观测性异常基类
    ├── MetricException                     # 指标异常
    ├── TracingException                    # 追踪异常
    └── HealthCheckException                # 健康检查异常
```

### 8.2 ObservabilityErrorCode - 错误码枚举

| 分类 | 说明 |
|------|------|
| 1xxx | 指标相关（名称无效、类型不匹配、数量超限等） |
| 2xxx | 追踪相关（ID 无效、上下文未找到等） |
| 3xxx | 健康检查相关（检查超时、执行失败等） |
| 4xxx | 导出/上报相关（导出失败、上报失败等） |

| 方法 | 返回类型 | 说明 |
|------|---------|------|
| `getCode()` | `int` | 错误码 |
| `getMessage()` | `String` | 英文消息 |
| `getMessageZh()` | `String` | 中文消息 |

### 8.3 异常类

**ObservabilityException - 基类：**

```java
public class ObservabilityException extends RuntimeException {
    public ObservabilityException(String message);
    public ObservabilityException(String message, ObservabilityErrorCode errorCode);
    public ObservabilityException(String message, Throwable cause);
    public ObservabilityException(String message, Throwable cause, ObservabilityErrorCode errorCode);
    public ObservabilityErrorCode getErrorCode();
}
```

**MetricException - 指标异常：**

```java
public class MetricException extends ObservabilityException {
    public MetricException(String message);
    public MetricException(String message, String metricName);
    public MetricException(String message, ObservabilityErrorCode errorCode);
    public static MetricException invalidName(String name);
    public static MetricException notFound(String name);
    public static MetricException typeMismatch(String name, String expected, String actual);
    public String getMetricName();
}
```

**TracingException - 追踪异常：**

```java
public class TracingException extends ObservabilityException {
    public TracingException(String message);
    public TracingException(String message, ObservabilityErrorCode errorCode);
    public TracingException(String message, String traceId, String spanId);
    public static TracingException invalidTraceId(String traceId);
    public static TracingException invalidSpanId(String spanId);
    public static TracingException contextNotFound();
    public String getTraceId();
    public String getSpanId();
}
```

**HealthCheckException - 健康检查异常：**

```java
public class HealthCheckException extends ObservabilityException {
    public HealthCheckException(String message);
    public HealthCheckException(String message, String checkName);
    public HealthCheckException(String message, Throwable cause);
    public HealthCheckException(String message, ObservabilityErrorCode errorCode);
    public static HealthCheckException failed(String name, String reason);
    public static HealthCheckException timeout(String name);
    public String getCheckName();
}
```

---

## 9. 线程安全与性能

### 9.1 线程安全保证

| 组件 | 线程安全机制 | 说明 |
|------|-------------|------|
| `MetricsRegistry` | `ConcurrentHashMap` | 指标注册并发安全 |
| `CounterMetric` | `LongAdder` | 无锁高性能计数 |
| `TimerMetric` | `LongAdder` + `AtomicLong` | 累计时间与最大值 |
| `HistogramMetric` | `LongAdder[]` | 桶计数并发安全 |
| `SummaryMetric` | `SlidingWindowReservoir` | 滑动窗口采样 |
| `GaugeMetric` | 无状态 | 委托给 Supplier |
| `TraceContext` | `ThreadLocal` | V1 上下文传播 |
| `TraceContextV2` | `ScopedValue` | V2 上下文传播（VT 友好） |
| `SpanV2` | `ConcurrentHashMap` + `CopyOnWriteArrayList` | 属性/事件并发安全 |
| `HealthCheckRegistry` | `ConcurrentHashMap` | 检查注册并发安全 |
| `Baggage` | 不可变 | 所有修改返回新实例 |

### 9.2 V1 vs V2 选择建议

| 场景 | 推荐 | 说明 |
|------|------|------|
| 传统线程池 | V1（ThreadLocal） | 兼容性好 |
| Virtual Thread | V2（ScopedValue） | 自动传播、无泄漏 |
| StructuredTaskScope | V2 | 与结构化并发协同 |
| 跨服务传播 | V2 + Baggage | W3C 标准 |

---

## 10. 使用示例

### 10.1 完整指标采集

```java
// 注册指标
CounterMetric requests = OpenMetrics.counter("http_requests_total");
TimerMetric latency = OpenMetrics.timer("http_request_duration_seconds");
GaugeMetric activeConns = OpenMetrics.gauge("http_active_connections", pool::activeCount);
HistogramMetric responseSize = OpenMetrics.histogram("http_response_size_bytes");

// 在请求处理中记录
String result = latency.record(() -> {
    requests.increment();
    String body = handleRequest();
    responseSize.record(body.length());
    return body;
});

// Prometheus 格式导出
PrometheusExporter exporter = new PrometheusExporter();
String metrics = exporter.export(OpenMetrics.getAll().values());
```

### 10.2 完整链路追踪（V2）

```java
// 注册上报器
SpanReporterV2.addReporter(SpanReporterV2.console());

// 追踪业务操作
Order order = OpenTracingV2.trace("createOrder", () -> {
    try (SpanV2 span = OpenTracingV2.startSpan("validateOrder")) {
        span.setAttribute("orderId", orderId);
        validateOrder(request);
        span.success();
    }

    try (SpanV2 span = OpenTracingV2.startSpan("saveOrder")) {
        Order saved = orderRepository.save(request);
        span.setAttribute("orderId", saved.getId());
        span.success();
        return saved;
    }
});
```

### 10.3 健康检查端点

```java
HealthCheckRegistryV2 registry = HealthCheckRegistryV2.global();
registry.register("database", () -> checkDatabase());
registry.register("redis", () -> checkRedis());
registry.register("memory", () -> {
    double usage = Runtime.getRuntime().totalMemory() * 1.0 / Runtime.getRuntime().maxMemory();
    return usage < 0.9 ? HealthStatusV2.up() : HealthStatusV2.down("Memory > 90%");
});

// HTTP 端点处理
HealthCheckResultsV2 results = registry.checkAll(Duration.ofSeconds(5));
int statusCode = results.isHealthy() ? 200 : 503;
```

### 10.4 OTLP 导出

```java
OtlpConfig config = OtlpConfig.builder()
    .endpoint("http://otel-collector:4318")
    .serviceName("order-service")
    .serviceVersion("2.0.0")
    .batchSize(256)
    .build();

// 指标导出
OtlpMetricsExporter metricsExporter = OtlpMetricsExporter.create(config);
metricsExporter.startPeriodicExport(OpenMetrics.getRegistry());

// 跨度导出
OtlpSpanExporter spanExporter = OtlpSpanExporter.create(config);
SpanReporterV2.addReporter(spanExporter.asReporter());
```

---

## 11. SPI 扩展

### 11.1 SpanReporterProvider

通过 SPI 机制自动发现并加载 Span 上报器。

```java
public interface SpanReporterProvider {
    Consumer<SpanDataV2> createReporter();
    String getName();
    int getPriority();
}
```

在 `META-INF/services/cloud.opencode.base.observability.spi.SpanReporterProvider` 中注册实现类。

---

## 12. 版本信息

| 属性 | 值 |
|------|-----|
| 模块名 | opencode-base-observability |
| 最低 JDK | 25 |
| 第三方依赖 | 无 |
| 指标类型 | Counter、Gauge、Histogram、Timer、Summary |
| 导出格式 | Prometheus、JSON、OTLP |
| 追踪实现 | V1（ThreadLocal）、V2（ScopedValue） |
| 健康检查 | V1、V2（Virtual Thread 并行） |

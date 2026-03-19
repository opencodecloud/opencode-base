# OpenCode Base Observability

**追踪、Span 和慢日志工具，适用于 JDK 25+**

`opencode-base-observability` 提供框架无关的可观测性层，包括通过 OpenTelemetry 实现的分布式追踪（自动检测，无硬依赖）、支持 try-with-resources 的 Span 管理，以及受 Redis SLOWLOG 启发的有界慢操作日志收集器。

## 功能特性

- **框架无关追踪**：密封的 `Tracer` 接口，支持可插拔实现
- **OpenTelemetry 集成**：通过反射自动检测类路径上的 OTel，零硬依赖
- **空操作回退**：OTel 不可用时的零开销空操作实现
- **Span 管理**：AutoCloseable Span，支持命中/未命中记录、错误捕获和自定义属性
- **慢日志收集器**：有界、线程安全的慢操作日志，可配置阈值
- **统计聚合**：慢操作统计（计数、最大值、平均值、最慢操作）
- **线程安全**：所有组件完全线程安全（ConcurrentHashMap、AtomicBoolean、ConcurrentLinkedDeque）

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-observability</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 使用 OpenTelemetry 追踪

```java
import cloud.opencode.base.observability.Tracer;
import cloud.opencode.base.observability.OpenTelemetryTracer;
import cloud.opencode.base.observability.Span;

// 自动检测类路径上的 OTel；不可用时回退到 noop
Tracer tracer = OpenTelemetryTracer.create("my-service");

try (Span span = tracer.startSpan("GET", "user:123")) {
    Object value = cache.get("user:123");
    span.setHit(value != null);
    span.setAttribute("cache.tier", "L1");
} catch (Exception e) {
    // 可调用 span.setError(e) 记录错误
    throw e;
}

tracer.close();
```

### 空操作追踪器

```java
// 用于测试或禁用追踪时的零开销空操作追踪器
Tracer noopTracer = Tracer.noop();
try (Span span = noopTracer.startSpan("GET", "key")) {
    // 所有 span 操作都是空操作
}
```

### 慢日志收集器

```java
import cloud.opencode.base.observability.SlowLogCollector;
import java.time.Duration;

// 默认：10ms 阈值，1024 最大条目
SlowLogCollector collector = SlowLogCollector.create();

// 自定义阈值
SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(50));

// 自定义阈值和缓冲区大小
SlowLogCollector collector = SlowLogCollector.create(Duration.ofMillis(50), 2048);

// 记录慢操作
collector.record("GET", "user:123", Duration.ofMillis(75));

// 查询最近条目
List<SlowLogCollector.Entry> recent = collector.getEntries(10);

// 获取统计信息
SlowLogCollector.Stats stats = collector.stats();
System.out.println("慢操作总数: " + stats.totalSlowOps());
System.out.println("最大耗时: " + stats.maxDuration());
System.out.println("平均耗时: " + stats.avgDuration());
System.out.println("最慢操作: " + stats.slowestOperation());

// 清除条目（累积计数保留）
collector.clear();
```

## 类参考

| 类名 | 说明 |
|------|------|
| `Tracer` | 框架无关的操作追踪密封接口 |
| `Tracer.NoopTracer` | 零开销的空操作追踪器实现 |
| `OpenTelemetryTracer` | 通过反射集成 OpenTelemetry，支持优雅的 noop 回退 |
| `Span` | AutoCloseable Span 接口，支持命中/未命中、错误和属性记录 |
| `SlowLogCollector` | 有界、线程安全的慢操作日志收集器（Redis SLOWLOG 风格） |
| `SlowLogCollector.Entry` | 单条慢日志记录（操作、键、耗时、时间戳、线程） |
| `SlowLogCollector.Stats` | 缓冲慢操作的聚合统计信息 |

## 环境要求

- Java 25+（使用密封接口、记录类、StackWalker）
- 无需外部依赖
- 可选：类路径上的 OpenTelemetry API 以启用真实追踪

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

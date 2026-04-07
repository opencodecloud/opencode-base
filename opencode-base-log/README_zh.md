# OpenCode Base Log

**轻量级日志门面，支持 SPI 可插拔日志引擎**

`opencode-base-log` 是一个现代日志框架，提供统一的日志门面，支持可插拔后端、结构化日志、审计日志、性能日志和敏感数据脱敏。

## 功能特性

### 核心功能
- **统一日志门面**：通过 `OpenLog` 提供静态方法，自动检测调用类
- **SPI 可插拔后端**：通过 `LogProvider` SPI 支持 SLF4J、Log4j2、JUL
- **参数化日志**：`{}` 占位符支持，高效消息格式化
- **Lambda 延迟求值**：延迟消息构造，提升性能
- **标记支持**：通过标记实现日志分类和过滤

### V1.0.3 新增功能
- **日志事件模型**：不可变 `LogEvent` 记录，携带完整事件上下文（级别、消息、异常、标记、MDC、时间戳、线程、调用者）
- **调用者信息**：`CallerInfo` 记录，通过 StackWalker 捕获类名、方法名、文件名和行号
- **日志过滤管道**：`LogFilter` 接口 + `LogFilterChain`，内置 `LevelFilter`、`MarkerFilter`、`ThrottleFilter`
- **异步日志**：`AsyncLogger` 基于虚拟线程的异步分发包装器，支持背压回退和优雅关闭
- **动态日志级别**：`DynamicLevelManager` 单例，支持运行时按 Logger 名称调整日志级别，无需重启
- **彩色控制台**：`ConsoleFormatter` ANSI 彩色输出 + `AnsiColor` 枚举，自动检测终端能力

### 增强功能
- **结构化日志**：JSON 风格的键值对结构化日志条目，适用于 ELK/Loki
- **日志脱敏**：密码、手机号、身份证等敏感数据脱敏
- **采样日志**：概率、时间和计数三种采样模式
- **审计日志**：结构化审计事件记录，支持可插拔持久化
- **性能日志**：StopWatch 计时、定时执行、慢操作检测
- **虚拟线程上下文**：虚拟线程上下文传播
- **MDC/NDC**：映射诊断上下文和嵌套诊断上下文支持
- **条件日志**：基于动态规则的条件日志输出
- **作用域日志上下文**：自动关闭的作用域上下文管理

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-log</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.log.OpenLog;
import cloud.opencode.base.log.Logger;

// 简单日志（自动检测调用类）
OpenLog.info("应用启动");

// 参数化日志
OpenLog.info("用户 {} 从 {} 登录", userId, ipAddress);

// Lambda 延迟求值
OpenLog.debug(() -> "耗时计算: " + computeValue());

// 异常日志
OpenLog.error("操作失败", exception);

// 获取 Logger 实例
Logger log = OpenLog.get(MyClass.class);
log.info("来自 {} 的消息", "MyClass");
```

### 结构化日志

```java
import cloud.opencode.base.log.enhance.StructuredLog;

StructuredLog.info()
    .message("用户登录成功")
    .field("userId", "user123")
    .field("ip", "192.168.1.1")
    .field("duration", 150)
    .log();
// 输出: {"message":"用户登录成功","userId":"user123","ip":"192.168.1.1","duration":150}
```

### 性能日志

```java
import cloud.opencode.base.log.perf.PerfLog;
import cloud.opencode.base.log.perf.StopWatch;

// 计时器
StopWatch watch = PerfLog.start("queryUsers");
List<User> users = userDao.findAll();
watch.stopAndLog();

// 定时执行
PerfLog.timed("processOrder", () -> orderService.process(order));
```

### 日志脱敏

```java
import cloud.opencode.base.log.enhance.LogMasking;

String masked = LogMasking.mask("13812345678", MaskingStrategy.PHONE);
// 输出: 138****5678
```

### 异步日志

```java
import cloud.opencode.base.log.async.AsyncLogger;

Logger delegate = LoggerFactory.getLogger(MyService.class);
try (AsyncLogger async = AsyncLogger.wrap(delegate)) {
    async.info("通过虚拟线程异步记录");
    async.flush(); // 等待挂起的消息处理完成
}
```

### 日志过滤

```java
import cloud.opencode.base.log.filter.*;

LogFilterChain chain = new LogFilterChain();
chain.addFilter(new LevelFilter(LogLevel.WARN));           // 仅 WARN 及以上
chain.addFilter(new ThrottleFilter(Duration.ofSeconds(5))); // 去重

LogEvent event = LogEvent.builder(LogLevel.INFO, "test").build();
FilterAction result = chain.apply(event); // DENY（低于 WARN）
```

### 动态日志级别

```java
import cloud.opencode.base.log.level.DynamicLevelManager;

DynamicLevelManager manager = DynamicLevelManager.getInstance();
manager.setLevel("com.example.MyService", LogLevel.DEBUG); // 运行时启用 DEBUG
manager.resetLevel("com.example.MyService");               // 恢复默认
```

### 调用者信息

```java
import cloud.opencode.base.log.CallerInfo;

CallerInfo info = CallerInfo.capture();
System.out.println(info.toShortString());  // "MyClass.myMethod:42"
System.out.println(info.toCompactString()); // "MyClass:42"
```

## 类参考

| 类名 | 说明 |
|------|------|
| `OpenLog` | 主入口 - 统一的静态日志门面，自动检测调用类 |
| `Logger` | 核心日志接口，定义标准日志操作 |
| `LoggerFactory` | 日志记录器工厂，按类或名称创建 Logger 实例 |
| `LogLevel` | 日志级别枚举：TRACE、DEBUG、INFO、WARN、ERROR、OFF |
| `AuditEvent` | 不可变审计事件记录，包含用户、操作和资源 |
| `AuditLog` | 审计事件记录的静态门面 |
| `AuditLogger` | 自定义审计日志持久化的 SPI 接口 |
| `LogContext` | 跨线程日志上下文管理工具 |
| `MDC` | 映射诊断上下文，键值对线程上下文 |
| `NDC` | 嵌套诊断上下文，栈式线程上下文 |
| `ConditionalLog` | 基于动态规则的条件日志输出 |
| `ExceptionLog` | 增强异常日志，支持堆栈跟踪格式化 |
| `LogMasking` | 敏感数据脱敏工具（密码、手机号、身份证） |
| `LogMetrics` | 日志指标收集和报告 |
| `SampledLog` | 限流和采样日志（概率、时间、计数） |
| `ScopedLogContext` | 自动关闭的作用域日志上下文管理 |
| `StructuredLog` | JSON 风格的结构化日志，流式 API |
| `VirtualThreadContext` | 虚拟线程上下文传播支持 |
| `OpenLogException` | 日志框架异常类型 |
| `Marker` | 日志标记，用于分类和过滤 |
| `Markers` | 预定义标记常量和工厂方法 |
| `PerfLog` | 性能日志工具，集成 StopWatch |
| `SlowOperationConfig` | 慢操作检测阈值配置 |
| `StopWatch` | 高精度操作计时器，集成日志输出 |
| `DefaultLogProvider` | 默认 SPI 日志提供者实现 |
| `LogAdapter` | 外部日志框架桥接适配器 |
| `LogProvider` | 可插拔日志引擎后端的 SPI 接口 |
| `LogProviderFactory` | 日志提供者发现和管理工厂 |
| `MDCAdapter` | MDC 实现的 SPI 接口 |
| `NDCAdapter` | NDC 实现的 SPI 接口 |
| `LogEvent` | 不可变日志事件记录，携带完整上下文 |
| `CallerInfo` | 调用者位置记录（类名、方法、文件、行号）|
| `LogFilter` | 日志事件过滤的函数式接口 |
| `LogFilterChain` | 线程安全的过滤器链，支持短路求值 |
| `LevelFilter` | 内置按级别阈值过滤器 |
| `MarkerFilter` | 内置按标记名称过滤器 |
| `ThrottleFilter` | 内置重复消息限流过滤器 |
| `AsyncLogger` | 基于虚拟线程的异步日志包装器 |
| `DynamicLevelManager` | 运行时按 Logger 名称管理日志级别 |
| `ConsoleFormatter` | 支持 ANSI 颜色的日志行格式化器 |
| `AnsiColor` | ANSI 颜色代码枚举 |

## 环境要求

- Java 25+（使用虚拟线程、StackWalker、记录类）
- 核心功能无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

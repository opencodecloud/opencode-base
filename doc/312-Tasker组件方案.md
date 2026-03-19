# Tasker 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-tasker` 模块提供任务调度能力。

**核心特性：**
- 定时任务调度
- Cron 表达式支持（5字段和6字段格式）
- 延迟任务 / 定时任务 / 固定频率任务
- 任务管理与生命周期
- Virtual Thread / 混合线程执行
- 超时控制
- SPI 扩展（TaskListener、TaskStore、TaskExecutor）
- Observability 指标导出

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────┐
│                      Application Layer                       │
│                      (业务任务定义)                           │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                        Facade Layer                          │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                     OpenTasker                         │  │
│  │  create / schedule / cancel / executeNow / close       │  │
│  └───────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
         │              │              │              │
         ▼              ▼              ▼              ▼
┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────────┐
│ Schedule │  │ Trigger  │  │Scheduler │  │  Executor    │
│  调度计划 │  │  触发器   │  │  调度器   │  │  执行器      │
└──────────┘  └──────────┘  └──────────┘  └──────────────┘
```

## 2. 包结构

```
cloud.opencode.base.tasker
├── Task.java                          # 任务接口
├── SimpleTask.java                    # 简单任务实现
├── OpenTasker.java                    # 门面类（主入口）
├── TaskResult.java                    # 任务结果 (record)
├── TaskContext.java                   # 任务上下文
├── TaskConfig.java                    # 任务配置 (record + Builder)
├── TaskStatus.java                    # 任务状态枚举
├── trigger/
│   ├── Trigger.java                   # 触发器接口
│   ├── SimpleTrigger.java             # 间隔触发器
│   ├── IntervalTrigger.java           # 定时触发器
│   └── OneTimeTrigger.java            # 一次性触发器
├── cron/
│   ├── CronTrigger.java               # Cron 触发器
│   ├── CronValidator.java             # Cron 验证器
│   └── CronScheduledFuture.java       # Cron 调度 Future
├── schedule/
│   ├── Schedule.java                  # 调度计划接口
│   ├── CronSchedule.java             # Cron 调度
│   ├── FixedRateSchedule.java         # 固定频率调度
│   ├── OneTimeSchedule.java           # 一次性调度
│   └── CronExpression.java            # Cron 表达式解析器
├── scheduler/
│   ├── TaskScheduler.java             # 任务调度器接口
│   ├── ScheduledTask.java             # 已调度任务接口
│   └── DefaultTaskScheduler.java      # 默认调度器实现
├── spi/
│   ├── TaskListener.java              # 任务监听器 SPI
│   ├── TaskStore.java                 # 任务存储 SPI
│   └── TaskExecutor.java              # 任务执行器 SPI
├── executor/
│   ├── VirtualThreadExecutor.java     # 虚拟线程执行器
│   ├── HybridThreadExecutor.java      # 混合线程执行器
│   └── TimeoutTaskWrapper.java        # 超时任务包装器
├── store/
│   └── MemoryTaskStore.java           # 内存任务存储
├── metrics/
│   └── ObservabilityMetricsExporter.java  # 指标导出
├── internal/
│   ├── TaskStats.java                 # 任务统计（内部）
│   ├── TaskResourceLimiter.java       # 资源限制（内部）
│   └── ConcurrencyController.java     # 并发控制（内部）
└── exception/
    ├── TaskerException.java           # 基础异常
    ├── TaskerErrorCode.java           # 错误码枚举
    ├── TaskExecutionException.java    # 执行异常
    ├── TaskTimeoutException.java      # 超时异常
    └── TaskScheduleException.java     # 调度异常
```

## 3. 核心 API

### 3.1 Task（任务接口）

任务的核心抽象，继承 `Runnable`。

| 方法签名 | 说明 |
|---------|------|
| `String getId()` | 获取任务ID |
| `String getName()` | 获取任务名称 |
| `String getDescription()` | 获取任务描述 |
| `void execute()` | 执行任务 |
| `Duration getEstimatedDuration()` | 获取预估时长 |
| `boolean isReady()` | 是否就绪 |
| `Instant getCreatedAt()` | 获取创建时间 |
| `static Task of(String id, Runnable action)` | 工厂方法：创建任务 |
| `static Task of(String id, String name, Runnable action)` | 工厂方法：带名称创建任务 |

**代码示例：**

```java
// 使用工厂方法创建任务
Task task = Task.of("cleanup", () -> {
    System.out.println("执行清理...");
});

// 带名称的任务
Task namedTask = Task.of("backup", "数据库备份", () -> {
    backupDatabase();
});
```

### 3.2 SimpleTask（简单任务）

包装 `Runnable` 的基本任务实现。

| 方法签名 | 说明 |
|---------|------|
| `SimpleTask(String id, Runnable action)` | 构造函数 |
| `SimpleTask(String id, String name, Runnable action)` | 带名称的构造函数 |

### 3.3 OpenTasker（门面类）

任务调度的主要入口，实现 `AutoCloseable`。

| 方法签名 | 说明 |
|---------|------|
| `static OpenTasker create()` | 创建默认调度器 |
| `static OpenTasker create(int threadPoolSize)` | 创建指定线程池大小的调度器 |
| `static OpenTasker of(TaskScheduler scheduler)` | 使用自定义调度器创建 |
| `ScheduledTask scheduleAt(Task task, Instant time)` | 在指定时间执行 |
| `ScheduledTask scheduleAfter(Task task, Duration delay)` | 延迟执行 |
| `ScheduledTask scheduleFixedRate(Task task, Duration period)` | 固定频率执行 |
| `ScheduledTask scheduleFixedRate(Task task, Duration initialDelay, Duration period)` | 带初始延迟的固定频率执行 |
| `ScheduledTask scheduleCron(Task task, String cronExpression)` | Cron 调度 |
| `ScheduledTask schedule(Task task, Schedule schedule)` | 通用调度 |
| `boolean cancel(String taskId)` | 取消任务 |
| `Optional<ScheduledTask> getTask(String taskId)` | 获取任务 |
| `List<ScheduledTask> getAllTasks()` | 获取所有任务 |
| `Future<TaskResult> executeNow(Task task)` | 立即执行 |
| `Future<TaskResult> executeNow(String taskId, Runnable action)` | 立即执行 Runnable |
| `boolean isRunning()` | 是否运行中 |
| `TaskScheduler getScheduler()` | 获取底层调度器 |
| `void close()` | 关闭调度器 |

**代码示例：**

```java
try (OpenTasker tasker = OpenTasker.create()) {
    Task task = Task.of("job1", () -> System.out.println("Hello"));

    // 延迟 5 秒执行
    tasker.scheduleAfter(task, Duration.ofSeconds(5));

    // 每 10 秒执行一次
    Task periodic = Task.of("heartbeat", () -> ping());
    tasker.scheduleFixedRate(periodic, Duration.ofSeconds(10));

    // Cron 表达式：每天凌晨 2 点
    Task nightly = Task.of("nightly", () -> cleanup());
    tasker.scheduleCron(nightly, "0 2 * * *");

    // 立即执行
    Future<TaskResult> future = tasker.executeNow(task);
}
```

### 3.4 TaskResult（任务结果）

不可变记录，包含任务执行结果信息。

| 字段/方法 | 说明 |
|---------|------|
| `String taskId` | 任务ID |
| `TaskStatus status` | 状态 |
| `Instant startTime` | 开始时间 |
| `Instant endTime` | 结束时间 |
| `Throwable error` | 错误（可选） |
| `Duration getDuration()` | 获取执行时长 |
| `boolean isSuccess()` | 是否成功 |
| `boolean isFailed()` | 是否失败 |
| `boolean isTimeout()` | 是否超时 |
| `static TaskResult success(String taskId, Instant start, Instant end)` | 成功结果 |
| `static TaskResult success(String taskId, Duration duration)` | 成功结果（带时长） |
| `static TaskResult failure(String taskId, Instant start, Instant end, Throwable error)` | 失败结果 |
| `static TaskResult failure(String taskId, Duration duration, String message)` | 失败结果（带消息） |
| `static TaskResult cancelled(String taskId)` | 取消结果 |
| `static TaskResult skipped(String taskId)` | 跳过结果 |
| `static TaskResult timeout(String taskId, Duration timeout, Duration elapsed)` | 超时结果 |

### 3.5 TaskStatus（任务状态枚举）

| 枚举值 | 说明 |
|---------|------|
| `PENDING` | 等待中 |
| `RUNNING` | 运行中 |
| `SUCCESS` | 成功 |
| `FAILED` | 失败 |
| `CANCELLED` | 已取消 |
| `SKIPPED` | 已跳过 |

| 方法签名 | 说明 |
|---------|------|
| `boolean isTerminal()` | 是否终态 |
| `boolean isRunning()` | 是否运行中 |

### 3.6 TaskContext（任务上下文）

| 方法签名 | 说明 |
|---------|------|
| `String getTaskId()` | 获取任务ID |
| `Instant getScheduledTime()` | 获取计划时间 |
| `Instant getStartTime()` | 获取开始时间 |
| `void put(String key, Object value)` | 设置数据 |
| `<T> T get(String key)` | 获取数据 |
| `<T> T getOrDefault(String key, T defaultValue)` | 获取数据（带默认值） |
| `boolean isCancelled()` | 是否已取消 |
| `void cancel()` | 取消任务 |
| `Map<String, Object> getData()` | 获取所有数据 |

### 3.7 TaskConfig（任务配置）

record + Builder 模式。

| 字段 | 说明 |
|---------|------|
| `String name` | 名称 |
| `String cron` | Cron 表达式 |
| `Duration initialDelay` | 初始延迟 |
| `Duration fixedRate` | 固定频率 |
| `Duration fixedDelay` | 固定延迟 |
| `Duration timeout` | 超时时间 |
| `int maxConcurrent` | 最大并发 |
| `int maxRetries` | 最大重试 |
| `Duration retryDelay` | 重试延迟 |
| `boolean enabled` | 是否启用 |

### 3.8 Schedule（调度计划接口）

| 方法签名 | 说明 |
|---------|------|
| `Optional<Instant> nextExecutionTime(Instant from)` | 下次执行时间 |
| `Optional<Instant> nextExecutionTime()` | 下次执行时间（从当前时间） |
| `boolean isRecurring()` | 是否循环 |
| `static Schedule at(Instant time)` | 定时调度 |
| `static Schedule after(Duration delay)` | 延迟调度 |
| `static Schedule fixedRate(Duration period)` | 固定频率调度 |
| `static Schedule fixedRate(Duration initialDelay, Duration period)` | 带初始延迟的固定频率 |
| `static Schedule cron(String expression)` | Cron 调度 |

**代码示例：**

```java
Schedule once = Schedule.at(Instant.now().plusSeconds(60));
Schedule delayed = Schedule.after(Duration.ofMinutes(5));
Schedule periodic = Schedule.fixedRate(Duration.ofSeconds(30));
Schedule cronSchedule = Schedule.cron("0 2 * * *");
```

### 3.9 CronSchedule / FixedRateSchedule / OneTimeSchedule

| 类 | 方法 | 说明 |
|---|---------|------|
| `CronSchedule` | `String getExpression()` | 获取 Cron 表达式 |
| `CronSchedule` | `ZoneId getZoneId()` | 获取时区 |
| `FixedRateSchedule` | `Duration getPeriod()` | 获取周期 |
| `FixedRateSchedule` | `Instant getFirstExecution()` | 获取首次执行时间 |
| `OneTimeSchedule` | `Instant getScheduledTime()` | 获取计划时间 |
| `OneTimeSchedule` | `boolean isExecuted()` | 是否已执行 |

### 3.10 CronExpression（Cron 表达式解析器）

支持 5 字段（分 时 日 月 周）和 6 字段（秒 分 时 日 月 周）格式。

| 方法签名 | 说明 |
|---------|------|
| `static CronExpression parse(String expression)` | 解析表达式 |
| `ZonedDateTime nextExecution(ZonedDateTime from)` | 下次执行时间 |
| `boolean matches(ZonedDateTime time)` | 是否匹配 |
| `boolean hasSeconds()` | 是否含秒字段 |
| `String getExpression()` | 获取表达式字符串 |

**代码示例：**

```java
// 5字段：每天 10:30
CronExpression cron5 = CronExpression.parse("30 10 * * *");

// 6字段：每5秒
CronExpression cron6 = CronExpression.parse("0/5 * * * * *");

ZonedDateTime next = cron5.nextExecution(ZonedDateTime.now());
```

### 3.11 Trigger（触发器接口）

| 方法签名 | 说明 |
|---------|------|
| `Optional<Instant> nextFireTime(Instant from)` | 下次触发时间 |
| `String getDescription()` | 获取描述 |
| `boolean isValid()` | 是否有效 |
| `boolean isRepeating()` | 是否重复 |

### 3.12 SimpleTrigger / IntervalTrigger / OneTimeTrigger / CronTrigger

| 类 | 工厂方法 | 说明 |
|---|---------|------|
| `SimpleTrigger` | `every(Duration interval)` | 每隔指定间隔 |
| `SimpleTrigger` | `times(Duration interval, int repeatCount)` | 指定重复次数 |
| `IntervalTrigger` | `seconds(long s)` | 每 N 秒 |
| `IntervalTrigger` | `minutes(long m)` | 每 N 分钟 |
| `IntervalTrigger` | `hours(long h)` | 每 N 小时 |
| `OneTimeTrigger` | `at(Instant time)` | 指定时间触发 |
| `OneTimeTrigger` | `after(Duration delay)` | 延迟触发 |
| `OneTimeTrigger` | `afterSeconds(long seconds)` | 延迟 N 秒 |
| `OneTimeTrigger` | `afterMinutes(long minutes)` | 延迟 N 分钟 |
| `CronTrigger` | `of(String expression)` | Cron 触发 |
| `CronTrigger` | `daily(int hour, int minute)` | 每日定时 |
| `CronTrigger` | `hourly()` | 每小时 |
| `CronTrigger` | `everyMinutes(int minutes)` | 每 N 分钟 |
| `CronTrigger` | `weekly(DayOfWeek day, int hour)` | 每周指定日 |
| `CronTrigger` | `monthly(int dayOfMonth, int hour)` | 每月指定日 |

**代码示例：**

```java
Trigger every5s = SimpleTrigger.every(Duration.ofSeconds(5));
Trigger once = OneTimeTrigger.after(Duration.ofMinutes(10));
Trigger cron = CronTrigger.daily(2, 0); // 每天凌晨 2:00
```

### 3.13 CronValidator（Cron 验证器）

| 方法签名 | 说明 |
|---------|------|
| `static void validate(String expression)` | 验证表达式 |
| `static void validate(String expression, Duration minInterval)` | 验证并检查最小间隔 |
| `static boolean isValid(String expression)` | 检查是否有效 |
| `static Duration getEstimatedInterval(String expression)` | 获取预估间隔 |

### 3.14 TaskScheduler（调度器接口）

| 方法签名 | 说明 |
|---------|------|
| `ScheduledTask schedule(Task task, Schedule schedule)` | 调度任务 |
| `ScheduledTask schedule(String taskId, Runnable action, Schedule schedule)` | 调度 Runnable |
| `boolean cancel(String taskId)` | 取消任务 |
| `Optional<ScheduledTask> getTask(String taskId)` | 获取任务 |
| `List<ScheduledTask> getAllTasks()` | 获取所有任务 |
| `Future<TaskResult> executeNow(Task task)` | 立即执行 |
| `void start()` | 启动调度器 |
| `void shutdown(boolean waitForCompletion)` | 关闭调度器 |
| `boolean isRunning()` | 是否运行中 |
| `void close()` | 关闭（默认等待完成） |

### 3.15 ScheduledTask（已调度任务接口）

| 方法签名 | 说明 |
|---------|------|
| `Task getTask()` | 获取任务 |
| `Schedule getSchedule()` | 获取调度计划 |
| `TaskStatus getStatus()` | 获取状态 |
| `Optional<Instant> getNextExecutionTime()` | 下次执行时间 |
| `Optional<Instant> getLastExecutionTime()` | 上次执行时间 |
| `int getExecutionCount()` | 执行次数 |
| `int getFailureCount()` | 失败次数 |
| `boolean cancel()` | 取消 |
| `boolean isCancelled()` | 是否已取消 |
| `boolean isDone()` | 是否已完成 |

### 3.16 SPI 扩展

#### TaskListener（任务监听器）

| 方法签名 | 说明 |
|---------|------|
| `void beforeExecute(Task task, TaskContext context)` | 执行前回调 |
| `void afterExecute(Task task, TaskContext context, TaskResult result)` | 执行后回调 |
| `void onSchedule(Task task, Schedule schedule)` | 调度时回调 |
| `void onCancel(String taskId)` | 取消时回调 |
| `void onError(Task task, Throwable error)` | 出错时回调 |
| `void onSkip(Task task, String reason)` | 跳过时回调 |
| `int getOrder()` | 优先级排序 |

#### TaskStore（任务存储）

| 方法签名 | 说明 |
|---------|------|
| `void save(StoredTask task)` | 保存任务 |
| `Optional<StoredTask> load(String taskId)` | 加载任务 |
| `List<StoredTask> loadAll()` | 加载所有 |
| `void remove(String taskId)` | 删除任务 |
| `boolean exists(String taskId)` | 是否存在 |
| `void clear()` | 清空 |
| `String getName()` | 存储名称 |

#### TaskExecutor（任务执行器）

| 方法签名 | 说明 |
|---------|------|
| `TaskResult execute(Task task, TaskContext context)` | 执行任务 |
| `void execute(Runnable runnable)` | 执行 Runnable |
| `void shutdown()` | 关闭 |
| `List<Runnable> shutdownNow()` | 立即关闭 |
| `boolean isShutdown()` | 是否已关闭 |
| `String getName()` | 执行器名称 |

### 3.17 VirtualThreadExecutor / HybridThreadExecutor

| 类 | 说明 |
|---|------|
| `VirtualThreadExecutor` | 基于虚拟线程的执行器，适合 I/O 密集型任务 |
| `HybridThreadExecutor` | 混合 CPU/IO 线程池执行器 |

`HybridThreadExecutor` 额外方法：

| 方法签名 | 说明 |
|---------|------|
| `void executeOnCpuPool(Runnable runnable)` | 在 CPU 池执行 |
| `void executeOnIoPool(Runnable runnable)` | 在 IO 池执行 |

内部标记接口 `CpuBoundTask` 用于区分任务类型。

### 3.18 TimeoutTaskWrapper（超时包装器）

| 方法签名 | 说明 |
|---------|------|
| `static TaskResult executeWithTimeout(Task task, TaskContext context, Duration timeout)` | 带超时执行 |
| `static void executeWithTimeoutOrThrow(Task task, TaskContext context, Duration timeout)` | 超时则抛异常 |
| `static Task wrap(Task task, Duration timeout)` | 包装为超时任务 |

**代码示例：**

```java
Task wrapped = TimeoutTaskWrapper.wrap(longRunningTask, Duration.ofSeconds(30));
wrapped.execute(); // 超过 30 秒将抛出 TaskTimeoutException
```

### 3.19 MemoryTaskStore（内存存储）

`TaskStore` 的内存实现，额外提供 `int size()` 方法。

### 3.20 ObservabilityMetricsExporter（指标导出）

当 Observability 模块可用时，自动导出任务指标。

| 方法签名 | 说明 |
|---------|------|
| `static boolean isObservabilityModuleAvailable()` | 模块是否可用 |
| `static void export(TaskStats stats)` | 导出统计 |
| `static void export(String prefix, TaskStats stats)` | 带前缀导出 |
| `static void exportAll(Map<String, TaskStats> statsMap)` | 批量导出 |
| `static void exportAll(String prefix, Map<String, TaskStats> statsMap)` | 带前缀批量导出 |
| `static Map<String, Object> toMetricsMap(TaskStats stats)` | 转换为指标 Map |

### 3.21 异常体系

| 异常类 | 说明 |
|---------|------|
| `TaskerException` | 基础异常 |
| `TaskExecutionException` | 执行异常 |
| `TaskTimeoutException` | 超时异常 |
| `TaskScheduleException` | 调度异常 |
| `TaskerErrorCode` | 错误码枚举 |

### 3.22 CronScheduledFuture

Cron 调度的 `ScheduledFuture` 实现。

| 方法签名 | 说明 |
|---------|------|
| `static CronScheduledFuture of(String cronExpression, Runnable task)` | 创建 |
| `long getDelay(TimeUnit unit)` | 获取延迟 |
| `boolean cancel(boolean mayInterruptIfRunning)` | 取消 |
| `boolean isCancelled()` | 是否已取消 |
| `boolean isDone()` | 是否已完成 |
| `Void get()` | 获取结果 |
| `void executeIfDue()` | 到期则执行 |
| `Optional<Instant> getNextExecutionTime()` | 下次执行时间 |
| `int getExecutionCount()` | 执行次数 |
| `void setMaxExecutions(int max)` | 设置最大执行次数 |
| `int getMaxExecutions()` | 获取最大执行次数 |
| `String getCronExpression()` | 获取 Cron 表达式 |
| `Optional<Throwable> getLastError()` | 获取最近错误 |

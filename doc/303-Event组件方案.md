# Event 组件方案

## 1. 组件概述

`opencode-base-event` 是 OpenCode Base 框架的事件总线组件，提供发布/订阅模式的事件驱动编程能力。支持同步/异步事件分发、注解驱动的订阅、事件存储与重放、Saga 分布式事务编排、事件签名验证、速率限制等企业级功能。

**核心特性：**
- 发布/订阅事件总线（同步 + 异步）
- 注解驱动的事件订阅（`@Subscribe`、`@Async`、`@Priority`）
- 事件存储与重放（Event Sourcing）
- Saga 分布式事务编排模式
- 事件签名与验证（安全事件总线）
- 事件速率限制
- 异常处理（日志记录、自动重试、死信队列）
- 事件序列化（可选模块委托）

**Maven 坐标：**
```xml
<dependency>
    <groupId>cloud.opencode</groupId>
    <artifactId>opencode-base-event</artifactId>
</dependency>
```

---

## 2. 包结构

```
cloud.opencode.base.event
├── Event                          // 事件基类
├── DataEvent<T>                   // 通用数据事件
├── WaitableEvent                  // 可等待事件包装器
├── EventListener<E>               // 事件监听器接口
├── OpenEvent                      // 事件总线（核心入口）
├── annotation/
│   ├── Subscribe                  // 订阅注解
│   ├── Async                      // 异步处理注解
│   └── Priority                   // 优先级注解
├── dispatcher/
│   ├── EventDispatcher            // 事件分发器接口
│   ├── SyncDispatcher             // 同步分发器
│   └── AsyncDispatcher            // 异步分发器
├── handler/
│   ├── EventExceptionHandler      // 异常处理器接口
│   ├── LoggingExceptionHandler    // 日志异常处理器
│   └── RetryExceptionHandler      // 重试异常处理器
├── store/
│   ├── EventStore                 // 事件存储接口
│   ├── InMemoryEventStore         // 内存事件存储
│   └── EventRecord                // 事件记录
├── saga/
│   ├── Saga<T>                    // Saga 编排器
│   ├── SagaResult<T>              // Saga 执行结果
│   ├── SagaStatus                 // Saga 状态枚举
│   └── SagaStep<T>               // Saga 步骤
├── security/
│   ├── SecureEventBus             // 安全事件总线
│   ├── SignedEvent                // 签名事件基类
│   ├── VerifiableEvent            // 可验证事件接口
│   └── EventRateLimiter           // 事件速率限制器
├── serialization/
│   └── EventSerializer            // 事件序列化器
└── exception/
    ├── EventException             // 事件异常基类
    ├── EventErrorCode             // 事件错误码枚举
    ├── EventListenerException     // 监听器异常
    ├── EventPublishException      // 发布异常
    ├── EventSecurityException     // 安全异常
    └── EventStoreException        // 存储异常
```

---

## 3. 核心 API

### 3.1 Event

> 事件基类，所有自定义事件都应继承此类。提供事件 ID、时间戳、来源和取消支持。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getId()` | 获取事件唯一 ID |
| `Instant getTimestamp()` | 获取事件时间戳 |
| `String getSource()` | 获取事件来源 |
| `boolean isCancelled()` | 检查事件是否已取消 |
| `void cancel()` | 取消事件 |

**示例：**
```java
// 自定义事件
public class UserRegisteredEvent extends Event {
    private final long userId;
    private final String email;

    public UserRegisteredEvent(long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    public long getUserId() { return userId; }
    public String getEmail() { return email; }
}

// 发布事件
OpenEvent.getDefault().publish(new UserRegisteredEvent(1L, "user@example.com"));
```

---

### 3.2 DataEvent\<T\>

> 通用数据事件，可携带任意类型的数据载荷，无需创建自定义事件子类。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `DataEvent(T data)` | 使用数据构造事件 |
| `DataEvent(T data, String source)` | 使用数据和来源构造事件 |
| `T getData()` | 获取数据载荷 |
| `Class<?> getDataType()` | 获取数据类型 |

**示例：**
```java
Order order = new Order(1L, "Product");
DataEvent<Order> event = new DataEvent<>(order, "OrderService");

OpenEvent.getDefault().publish(event);

OpenEvent.getDefault().on(DataEvent.class, e -> {
    Order data = (Order) e.getData();
    System.out.println("Order received: " + data);
});
```

---

### 3.3 WaitableEvent

> 可等待事件包装器，用于同步等待事件处理完成。内部使用 CountDownLatch 实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `WaitableEvent(Event event, CountDownLatch latch)` | 使用事件和 Latch 构造 |
| `WaitableEvent(Event event)` | 使用事件构造（自动创建 Latch） |
| `Event getWrappedEvent()` | 获取被包装的原始事件 |
| `CountDownLatch getLatch()` | 获取内部的 CountDownLatch |
| `void complete()` | 标记事件处理完成 |
| `boolean await(long timeout)` | 等待事件完成（超时，毫秒） |
| `void await()` | 无限等待事件完成 |

**示例：**
```java
Event event = new UserRegisteredEvent(1L, "user@example.com");
boolean completed = OpenEvent.getDefault().publishAndWait(event, Duration.ofSeconds(5));
```

---

### 3.4 EventListener\<E\>

> 事件监听器函数式接口，接收特定类型的事件进行处理。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void onEvent(E event)` | 处理事件的回调方法 |

**示例：**
```java
// Lambda 方式
OpenEvent.getDefault().on(UserRegisteredEvent.class, event -> {
    System.out.println("User registered: " + event.getUserId());
});

// 方法引用
OpenEvent.getDefault().on(UserRegisteredEvent.class, this::handleUserRegistered);
```

---

### 3.5 OpenEvent

> 事件总线核心入口，提供事件发布、订阅、存储管理等功能。支持 Builder 模式自定义配置。实现 `AutoCloseable` 接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static OpenEvent getDefault()` | 获取默认事件总线单例 |
| `static OpenEvent create()` | 创建新的事件总线实例 |
| `static Builder builder()` | 创建 Builder |
| `void register(Object subscriber)` | 注册注解驱动的订阅者 |
| `<E> void on(Class<E> eventType, EventListener<E> listener)` | 注册事件监听器 |
| `<E> void on(Class<E> eventType, EventListener<E> listener, boolean async)` | 注册监听器（指定同步/异步） |
| `<E> void on(Class<E> eventType, EventListener<E> listener, boolean async, int priority)` | 注册监听器（指定异步和优先级） |
| `void unregister(Object subscriber)` | 注销订阅者 |
| `void publish(Event event)` | 同步发布事件 |
| `CompletableFuture<Void> publishAsync(Event event)` | 异步发布事件 |
| `<T> void publish(T data)` | 发布数据事件（自动包装为 DataEvent） |
| `<T> void publish(T data, String source)` | 发布带来源的数据事件 |
| `boolean publishAndWait(Event event, Duration timeout)` | 发布事件并等待处理完成 |
| `void setEventStore(EventStore store)` | 设置事件存储 |
| `EventStore getEventStore()` | 获取事件存储 |
| `void setExceptionHandler(EventExceptionHandler handler)` | 设置异常处理器 |
| `void close()` | 关闭事件总线 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder asyncExecutor(ExecutorService executor)` | 设置异步执行器 |
| `Builder syncDispatcher(EventDispatcher dispatcher)` | 设置同步分发器 |
| `Builder asyncDispatcher(EventDispatcher dispatcher)` | 设置异步分发器 |
| `Builder eventStore(EventStore store)` | 设置事件存储 |
| `Builder exceptionHandler(EventExceptionHandler handler)` | 设置异常处理器 |
| `OpenEvent build()` | 构建事件总线 |

**示例：**
```java
// 使用默认事件总线
OpenEvent eventBus = OpenEvent.getDefault();

// 注册监听器
eventBus.on(UserRegisteredEvent.class, event -> {
    System.out.println("User registered: " + event.getUserId());
});

// 发布事件
eventBus.publish(new UserRegisteredEvent(1L, "user@example.com"));

// 异步发布
eventBus.publishAsync(new UserRegisteredEvent(2L, "user2@example.com"))
    .thenRun(() -> System.out.println("Event processed"));

// 自定义事件总线
OpenEvent custom = OpenEvent.builder()
    .asyncExecutor(Executors.newVirtualThreadPerTaskExecutor())
    .eventStore(new InMemoryEventStore(10000))
    .exceptionHandler(new RetryExceptionHandler(3, Duration.ofSeconds(1)))
    .build();
```

---

### 3.6 @Subscribe

> 订阅注解，标注方法为事件处理器。方法参数类型决定订阅的事件类型。

**示例：**
```java
public class UserEventHandler {

    @Subscribe
    public void onUserRegistered(UserRegisteredEvent event) {
        System.out.println("User registered: " + event.getUserId());
    }
}

OpenEvent.getDefault().register(new UserEventHandler());
```

---

### 3.7 @Async

> 异步处理注解，标注事件处理方法以异步方式执行。

**示例：**
```java
public class AsyncEventHandler {

    @Subscribe
    @Async
    public void onOrderCreated(OrderCreatedEvent event) {
        // 在虚拟线程中异步执行
        sendNotification(event);
    }
}
```

---

### 3.8 @Priority

> 优先级注解，用于控制同一事件的多个监听器的执行顺序。值越小优先级越高。

**示例：**
```java
public class PriorityHandler {

    @Subscribe
    @Priority(1)  // 最先执行
    public void validateFirst(OrderEvent event) { ... }

    @Subscribe
    @Priority(10) // 后执行
    public void processLater(OrderEvent event) { ... }
}
```

---

### 3.9 EventDispatcher

> 事件分发器接口，定义事件到监听器的分发策略。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void dispatch(Event event, List<Consumer<Event>> listeners)` | 将事件分发到监听器列表 |
| `boolean isAsync()` | 是否为异步分发器（默认 false） |
| `void shutdown()` | 关闭分发器并释放资源（默认空操作） |

---

### 3.10 SyncDispatcher

> 同步事件分发器，在当前线程中按顺序执行所有监听器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `SyncDispatcher()` | 默认构造（遇错不停止） |
| `SyncDispatcher(boolean stopOnError)` | 指定遇错是否停止 |
| `void dispatch(Event event, List<Consumer<Event>> listeners)` | 同步分发事件 |
| `boolean isAsync()` | 返回 false |

**示例：**
```java
SyncDispatcher dispatcher = new SyncDispatcher();
dispatcher.dispatch(event, listeners);

// 遇到异常时停止后续监听器
SyncDispatcher strictDispatcher = new SyncDispatcher(true);
```

---

### 3.11 AsyncDispatcher

> 异步事件分发器，使用线程池（默认虚拟线程）并行执行监听器。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `AsyncDispatcher()` | 默认构造（使用虚拟线程） |
| `AsyncDispatcher(ExecutorService executor)` | 指定线程池 |
| `void dispatch(Event event, List<Consumer<Event>> listeners)` | 异步分发事件 |
| `CompletableFuture<Void> dispatchAsync(Event event, List<Consumer<Event>> listeners)` | 异步分发并返回 Future |
| `boolean isAsync()` | 返回 true |
| `void shutdown()` | 关闭线程池 |
| `ExecutorService getExecutor()` | 获取线程池 |

**示例：**
```java
AsyncDispatcher dispatcher = new AsyncDispatcher();
dispatcher.dispatch(event, listeners);

// 自定义线程池
AsyncDispatcher customDispatcher = new AsyncDispatcher(myExecutor);
CompletableFuture<Void> future = customDispatcher.dispatchAsync(event, listeners);
```

---

### 3.12 EventExceptionHandler

> 事件异常处理器函数式接口，在事件处理过程中发生异常时调用。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void handleException(Event event, Throwable exception, String listenerName)` | 处理异常 |

**示例：**
```java
EventExceptionHandler handler = (event, exception, listenerName) -> {
    log.error("Listener {} failed for event {}: {}",
        listenerName, event.getId(), exception.getMessage());
};

OpenEvent eventBus = OpenEvent.builder()
    .exceptionHandler(handler)
    .build();
```

---

### 3.13 LoggingExceptionHandler

> 日志异常处理器，将事件处理异常记录到日志。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `LoggingExceptionHandler()` | 默认构造（ERROR 级别） |
| `LoggingExceptionHandler(System.Logger.Level logLevel)` | 指定日志级别 |
| `void handleException(Event event, Throwable exception, String listenerName)` | 记录异常日志 |

---

### 3.14 RetryExceptionHandler

> 重试异常处理器，支持自动重试和死信队列。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RetryExceptionHandler()` | 默认构造（3 次重试，1 秒延迟） |
| `RetryExceptionHandler(int maxRetries, Duration delay)` | 指定重试次数和延迟 |
| `void setRetryAction(Consumer<Event> retryAction)` | 设置重试回调 |
| `void handleException(Event event, Throwable exception, String listenerName)` | 处理异常（自动重试） |
| `void processDeadLetters(Consumer<FailedEvent> handler)` | 处理死信队列 |
| `int getDeadLetterQueueSize()` | 获取死信队列大小 |
| `void clearDeadLetterQueue()` | 清空死信队列 |
| `int getMaxRetries()` | 获取最大重试次数 |
| `Duration getDelay()` | 获取重试延迟 |

**内部记录：**

| 类型 | 描述 |
|------|------|
| `record FailedEvent(Event event, Throwable exception, String listenerName, int retryCount)` | 失败事件记录 |

**示例：**
```java
RetryExceptionHandler handler = new RetryExceptionHandler(3, Duration.ofSeconds(1));
handler.setRetryAction(event -> OpenEvent.getDefault().publish(event));

// 处理死信队列
handler.processDeadLetters(failed -> {
    log.error("Permanently failed: event={}, retries={}",
        failed.event().getId(), failed.retryCount());
});
```

---

### 3.15 EventStore

> 事件存储接口，用于事件持久化、检索和重放（Event Sourcing）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EventRecord save(Event event)` | 保存事件 |
| `Optional<EventRecord> findById(String eventId)` | 按 ID 查找事件记录 |
| `List<EventRecord> findByType(Class<? extends Event> eventType)` | 按类型查找事件记录 |
| `List<EventRecord> findByTimeRange(Instant from, Instant to)` | 按时间范围查找事件记录 |
| `List<EventRecord> findBySource(String source)` | 按来源查找事件记录 |
| `void replay(Class<? extends Event> eventType, Consumer<Event> handler)` | 按类型重放事件 |
| `void replayByTimeRange(Instant from, Instant to, Consumer<Event> handler)` | 按时间范围重放事件 |
| `long count()` | 获取存储事件总数 |
| `void clear()` | 清除所有存储事件 |

---

### 3.16 InMemoryEventStore

> 内存事件存储实现，支持容量限制和自动淘汰旧记录。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `InMemoryEventStore()` | 默认构造（默认容量） |
| `InMemoryEventStore(int maxCapacity)` | 指定最大容量 |
| `EventRecord save(Event event)` | 保存事件（超过容量自动淘汰旧记录） |
| `Optional<EventRecord> findById(String eventId)` | 按 ID 查找 |
| `List<EventRecord> findByType(Class<? extends Event> eventType)` | 按类型查找 |
| `List<EventRecord> findByTimeRange(Instant from, Instant to)` | 按时间范围查找 |
| `List<EventRecord> findBySource(String source)` | 按来源查找 |
| `void replay(Class<? extends Event> eventType, Consumer<Event> handler)` | 按类型重放 |
| `void replayByTimeRange(Instant from, Instant to, Consumer<Event> handler)` | 按时间范围重放 |
| `long count()` | 获取事件总数 |
| `void clear()` | 清除所有事件 |
| `int getMaxCapacity()` | 获取最大容量 |
| `List<EventRecord> findAll()` | 获取所有事件记录 |
| `long getCurrentSequence()` | 获取当前序列号 |

**示例：**
```java
InMemoryEventStore store = new InMemoryEventStore(10000);

OpenEvent eventBus = OpenEvent.builder()
    .eventStore(store)
    .build();

// 查询事件
List<EventRecord> records = store.findByType(OrderCreatedEvent.class);

// 重放事件
store.replay(OrderEvent.class, event -> processEvent(event));
```

---

### 3.17 EventRecord

> 事件记录（record 类型），封装存储的事件及其元数据。

**字段：**

| 字段 | 类型 | 描述 |
|------|------|------|
| `event` | `Event` | 原始事件 |
| `eventType` | `String` | 事件类型名称 |
| `eventId` | `String` | 事件 ID |
| `timestamp` | `Instant` | 事件时间戳 |
| `source` | `String` | 事件来源 |
| `sequenceNumber` | `long` | 序列号 |

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static EventRecord of(Event event, long sequenceNumber)` | 从事件创建记录 |
| `boolean isType(Class<? extends Event> type)` | 检查是否为指定类型 |
| `boolean isWithinTimeRange(Instant from, Instant to)` | 检查是否在时间范围内 |

---

### 3.18 Saga\<T\>

> Saga 分布式事务编排器，支持多步骤事务执行和自动补偿。采用 Builder 模式构建，支持超时、重试和异步执行。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `SagaResult<T> execute(T context)` | 同步执行 Saga |
| `CompletableFuture<SagaResult<T>> executeAsync(T context)` | 异步执行 Saga |
| `String getName()` | 获取 Saga 名称 |
| `int getStepCount()` | 获取步骤数量 |
| `List<String> getStepNames()` | 获取所有步骤名称 |
| `static <T> Builder<T> builder()` | 创建 Builder |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder<T> name(String name)` | 设置 Saga 名称 |
| `StepBuilder<T> step(String stepName)` | 添加步骤（进入步骤构建） |
| `Builder<T> addStep(SagaStep<T> step)` | 直接添加步骤对象 |
| `Builder<T> onSuccess(Consumer<T> handler)` | 设置成功回调 |
| `Builder<T> onFailure(BiConsumer<T, Throwable> handler)` | 设置失败回调 |
| `Builder<T> globalTimeout(Duration timeout)` | 设置全局超时 |
| `Saga<T> build()` | 构建 Saga |

**StepBuilder 方法：**

| 方法 | 描述 |
|------|------|
| `StepBuilder<T> action(Consumer<T> action)` | 设置步骤正向操作 |
| `StepBuilder<T> compensation(Consumer<T> compensation)` | 设置步骤补偿操作 |
| `StepBuilder<T> timeout(Duration timeout)` | 设置步骤超时 |
| `StepBuilder<T> retries(int maxRetries)` | 设置步骤重试次数 |
| `Builder<T> build()` | 构建步骤并返回 Builder |

**示例：**
```java
Saga<OrderContext> orderSaga = Saga.<OrderContext>builder()
    .name("create-order")
    .step("create-order")
        .action(ctx -> orderService.createOrder(ctx))
        .compensation(ctx -> orderService.cancelOrder(ctx))
        .timeout(Duration.ofSeconds(5))
        .retries(3)
        .build()
    .step("deduct-inventory")
        .action(ctx -> inventoryService.deduct(ctx))
        .compensation(ctx -> inventoryService.restore(ctx))
        .build()
    .step("charge-payment")
        .action(ctx -> paymentService.charge(ctx))
        .compensation(ctx -> paymentService.refund(ctx))
        .build()
    .onSuccess(ctx -> log.info("Order saga completed: {}", ctx))
    .onFailure((ctx, ex) -> log.error("Order saga failed: {}", ctx, ex))
    .globalTimeout(Duration.ofSeconds(30))
    .build();

// 同步执行
SagaResult<OrderContext> result = orderSaga.execute(new OrderContext(orderId, items, amount));

// 异步执行
CompletableFuture<SagaResult<OrderContext>> future = orderSaga.executeAsync(context);
```

---

### 3.19 SagaResult\<T\>

> Saga 执行结果（record 类型），包含执行状态、上下文、耗时和错误信息。

**字段：**

| 字段 | 类型 | 描述 |
|------|------|------|
| `status` | `SagaStatus` | 执行状态 |
| `context` | `T` | 执行上下文 |
| `error` | `Throwable` | 错误信息（可能为 null） |
| `startTime` | `Instant` | 开始时间 |
| `endTime` | `Instant` | 结束时间 |

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean isSuccess()` | 是否成功完成 |
| `boolean isCompensated()` | 是否已补偿 |
| `boolean isFailed()` | 是否失败 |
| `Duration getDuration()` | 获取执行耗时 |
| `String getErrorMessage()` | 获取错误信息 |

---

### 3.20 SagaStatus

> Saga 状态枚举，表示 Saga 执行的各个阶段。

| 枚举值 | 描述 |
|--------|------|
| `RUNNING` | 正在执行 |
| `COMPLETED` | 执行成功完成 |
| `COMPENSATED` | 失败并已执行补偿 |
| `FAILED` | 失败（补偿可能也失败） |
| `CANCELLED` | 已取消 |
| `PENDING` | 等待执行 |
| `PAUSED` | 已暂停 |

---

### 3.21 SagaStep\<T\>

> Saga 步骤（record 类型），定义事务中的单个步骤。

**字段：**

| 字段 | 类型 | 描述 |
|------|------|------|
| `name` | `String` | 步骤名称 |
| `action` | `Consumer<T>` | 正向操作 |
| `compensation` | `Consumer<T>` | 补偿操作 |
| `timeout` | `Duration` | 超时时间 |
| `maxRetries` | `int` | 最大重试次数 |

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> SagaStep<T> of(String name, Consumer<T> action)` | 创建步骤（仅正向操作） |
| `static <T> SagaStep<T> of(String name, Consumer<T> action, Consumer<T> compensation)` | 创建步骤（正向 + 补偿） |

---

### 3.22 SecureEventBus

> 安全事件总线，在 OpenEvent 基础上增加白名单验证、包名过滤、速率限制和事件签名验证。实现 `AutoCloseable` 接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `SecureEventBus()` | 默认构造 |
| `SecureEventBus(EventRateLimiter rateLimiter)` | 指定速率限制器 |
| `SecureEventBus(EventRateLimiter rateLimiter, String verificationSecret)` | 指定速率限制器和验证密钥 |
| `void addToWhitelist(Class<?> listenerClass)` | 添加监听器类到白名单 |
| `void addAllowedPackage(String packageName)` | 添加允许的包名 |
| `void register(Object subscriber)` | 注册订阅者（白名单验证） |
| `<E> void on(Class<E> eventType, EventListener<E> listener)` | 注册监听器 |
| `void unregister(Object subscriber)` | 注销订阅者 |
| `void publish(Event event)` | 发布事件（速率限制 + 签名验证） |
| `void close()` | 关闭安全事件总线 |
| `OpenEvent getEventBus()` | 获取底层事件总线 |

**示例：**
```java
SecureEventBus bus = new SecureEventBus(
    new EventRateLimiter(100),
    "my-secret-key"
);

bus.addToWhitelist(MyHandler.class);
bus.addAllowedPackage("com.myapp.handlers");

bus.register(new MyHandler());
bus.publish(new SecureOrderEvent(order, "my-secret-key"));
```

---

### 3.23 SignedEvent

> 签名事件抽象基类，提供基于 HMAC 的事件签名和验证。继承 Event 并实现 VerifiableEvent 接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getSignature()` | 获取事件签名 |
| `boolean verify(String secret)` | 验证事件签名 |

---

### 3.24 VerifiableEvent

> 可验证事件接口，定义事件签名和验证能力。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String getSignature()` | 获取签名字符串 |
| `boolean verify(String secret)` | 使用密钥验证签名 |

---

### 3.25 EventRateLimiter

> 事件速率限制器，限制每秒发布的事件数量，支持按事件类型设置独立限制。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EventRateLimiter(int defaultMaxPerSecond)` | 构造，指定默认最大 QPS |
| `void setLimit(Class<? extends Event> eventType, int maxPerSecond)` | 按事件类型设置限制 |
| `boolean allowPublish(Event event)` | 检查是否允许发布 |
| `int getCurrentCount(Class<? extends Event> eventType)` | 获取当前计数 |
| `int getDefaultMaxPerSecond()` | 获取默认最大 QPS |
| `void reset()` | 重置所有计数 |
| `void reset(Class<? extends Event> eventType)` | 重置指定类型的计数 |

**示例：**
```java
EventRateLimiter limiter = new EventRateLimiter(100);  // 默认每秒100
limiter.setLimit(HighFreqEvent.class, 1000);  // 高频事件每秒1000

SecureEventBus bus = new SecureEventBus(limiter);
```

---

### 3.26 EventSerializer

> 事件序列化器，支持事件的二进制和 Base64 序列化/反序列化。如果存在序列化模块（opencode-base-serialization），自动委托给 OpenSerializer。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static boolean isSerializationModuleAvailable()` | 检查序列化模块是否可用 |
| `static <T extends Event> byte[] serialize(T event)` | 序列化事件为字节数组 |
| `static <T extends Event> T deserialize(byte[] data, Class<T> eventType)` | 从字节数组反序列化事件 |
| `static <T extends Event> String serializeToString(T event)` | 序列化事件为 Base64 字符串 |
| `static <T extends Event> T deserializeFromString(String base64, Class<T> eventType)` | 从 Base64 字符串反序列化事件 |

**内部异常：**

| 类型 | 描述 |
|------|------|
| `EventSerializationException` | 序列化/反序列化异常 |

---

### 3.27 EventException

> 事件异常基类，继承 RuntimeException，携带错误码和关联事件。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EventException(String message)` | 使用消息构造 |
| `EventException(String message, Throwable cause)` | 使用消息和原因构造 |
| `EventException(String message, EventErrorCode errorCode)` | 使用消息和错误码构造 |
| `EventException(String message, Throwable cause, Event event, EventErrorCode errorCode)` | 完整构造 |
| `EventErrorCode getErrorCode()` | 获取错误码 |
| `Event getEvent()` | 获取关联事件 |

---

### 3.28 EventErrorCode

> 事件错误码枚举，定义事件处理中的各种错误类型。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int getCode()` | 获取错误码数值 |
| `String getDescription()` | 获取英文描述 |
| `String getDescriptionCn()` | 获取中文描述 |
| `static EventErrorCode fromException(Throwable e)` | 根据异常推断错误码 |

---

### 3.29 EventListenerException

> 事件监听器异常，继承 EventException。在监听器处理事件失败时抛出。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EventListenerException(String message)` | 使用消息构造 |
| `EventListenerException(String message, Throwable cause)` | 使用消息和原因构造 |
| `EventListenerException(String message, Throwable cause, Event event, EventErrorCode errorCode)` | 完整构造 |

---

### 3.30 EventPublishException

> 事件发布异常，继承 EventException。在事件发布失败时抛出。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EventPublishException(String message)` | 使用消息构造 |
| `EventPublishException(String message, Throwable cause)` | 使用消息和原因构造 |
| `EventPublishException(String message, Throwable cause, Event event, EventErrorCode errorCode)` | 完整构造 |

---

### 3.31 EventSecurityException

> 事件安全异常，继承 EventException。在安全验证失败时抛出。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EventSecurityException(String message)` | 使用消息构造 |
| `EventSecurityException(String message, EventErrorCode errorCode)` | 使用消息和错误码构造 |
| `EventSecurityException(String message, Throwable cause)` | 使用消息和原因构造 |
| `EventSecurityException(String message, Throwable cause, Event event, EventErrorCode errorCode)` | 完整构造 |

---

### 3.32 EventStoreException

> 事件存储异常，继承 EventException。在事件存储操作失败时抛出。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `EventStoreException(String message)` | 使用消息构造 |
| `EventStoreException(String message, Throwable cause)` | 使用消息和原因构造 |
| `EventStoreException(String message, Throwable cause, Event event, EventErrorCode errorCode)` | 完整构造 |

---

### 3.33 Saga.SagaTimeoutException

> Saga 超时异常，在步骤执行超时时抛出。继承 Exception。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `SagaTimeoutException(String message, Throwable cause)` | 使用消息和原因构造 |

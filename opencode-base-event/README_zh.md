# OpenCode Base Event

面向 JDK 25+ 的事件驱动架构支持，提供发布/订阅模式和虚拟线程异步处理。

## 功能特性

- 发布/订阅事件总线
- 同步和异步事件处理
- 基于虚拟线程的异步执行
- 事件优先级排序
- 事件取消支持
- 注解驱动的监听器注册（`@Subscribe`、`@Async`、`@Priority`）
- Lambda 监听器注册
- **订阅句柄**（AutoCloseable）精确生命周期管理
- **死事件检测** -- 无监听器的事件包装为 `DeadEvent` 重新分发
- **事件过滤** -- 使用 `Predicate` 按条件接收事件
- **拦截器链** -- 发布前/后钩子，支持日志、安全检查等横切关注点
- **粘性事件** -- 延迟注册的订阅者自动收到最后一次事件
- **事件总线指标** -- 运行统计（发布数、投递数、错误数、死事件数）
- **测试工具** -- `EventCaptor` 用于在测试中捕获和断言事件
- 数据事件包装器，承载任意负载
- 可等待事件，支持超时
- 事件溯源，可插拔事件存储
- 内存事件存储实现
- Saga 模式支持（多步事务与补偿）
- 事件序列化支持
- 事件安全：频率限制、签名事件、可验证事件
- 可配置异常处理器（日志、重试）
- 心跳监控
- 构建器 API 用于自定义事件总线配置
- 单例和实例模式
- AutoCloseable 生命周期管理
- 线程安全
- 异常体系继承 `OpenException`（OpenCode 统一异常基类）

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-event</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenEvent` | 事件总线门面——发布/订阅操作的主入口 |
| `Event` | 基础事件类，包含 ID、时间戳、来源和取消功能 |
| `DataEvent` | 通用数据承载事件包装器 |
| `WaitableEvent` | 支持阻塞等待处理完成的事件 |
| `DeadEvent` | 包装无订阅者的事件（V1.0.3 新增） |
| `EventListener` | 函数式事件监听器接口 |
| `Subscription` | AutoCloseable 订阅句柄（V1.0.3 新增） |
| **注解** | |
| `@Subscribe` | 将方法标记为事件监听器 |
| `@Async` | 将监听器标记为异步执行 |
| `@Priority` | 设置监听器执行优先级 |
| **分发器** | |
| `EventDispatcher` | 事件分发器接口 |
| `SyncDispatcher` | 同步事件分发器 |
| `AsyncDispatcher` | 使用虚拟线程的异步事件分发器 |
| **拦截器** | |
| `EventInterceptor` | 发布前/后拦截器接口（V1.0.3 新增） |
| **异常处理器** | |
| `EventExceptionHandler` | 异常处理器接口 |
| `LoggingExceptionHandler` | 记录事件处理过程中的异常 |
| `RetryExceptionHandler` | 重试失败的事件处理 |
| **Saga** | |
| `Saga` | Saga 编排器，用于多步事务 |
| `SagaStep` | Saga 中的单个步骤，包含操作和补偿 |
| `SagaResult` | Saga 执行结果 |
| `SagaStatus` | Saga 执行状态枚举 |
| **安全** | |
| `SecureEventBus` | 带安全功能的事件总线 |
| `EventRateLimiter` | 事件发布频率限制器 |
| `SignedEvent` | 带密码学签名的事件 |
| `VerifiableEvent` | 可验证真实性的事件 |
| **序列化** | |
| `EventSerializer` | 事件序列化接口 |
| **存储** | |
| `EventStore` | 事件存储接口，用于事件溯源 |
| `InMemoryEventStore` | 内存事件存储实现 |
| `EventRecord` | 存储的事件记录 |
| **监控** | |
| `HeartbeatMonitor` | 事件总线健康心跳监控 |
| `EventBusMetrics` | 事件总线统计快照（V1.0.3 新增） |
| **测试** | |
| `EventCaptor` | 事件捕获测试工具（V1.0.3 新增） |
| **异常** | |
| `EventException` | 通用事件异常（继承 `OpenException`） |
| `EventListenerException` | 监听器相关异常 |
| `EventPublishException` | 发布相关异常 |
| `EventSecurityException` | 安全相关异常 |
| `EventStoreException` | 事件存储异常 |
| `EventErrorCode` | 错误码枚举 |

## 快速开始

```java
import cloud.opencode.base.event.*;

// 获取默认事件总线
OpenEvent eventBus = OpenEvent.getDefault();

// 注册 Lambda 监听器
eventBus.on(UserRegisteredEvent.class, event -> {
    System.out.println("用户已注册: " + event.getUserId());
});

// 使用订阅句柄（精确取消订阅）
Subscription sub = eventBus.subscribe(OrderEvent.class, event -> {
    processOrder(event);
});
sub.unsubscribe(); // 或使用 try-with-resources

// 带过滤器的订阅
eventBus.subscribe(OrderEvent.class,
    event -> processLargeOrder(event),
    event -> event.getAmount() > 1000);

// 死事件检测
eventBus.subscribe(DeadEvent.class, dead -> {
    log.warn("未处理事件: {}", dead.getOriginalEvent());
});

// 粘性事件
eventBus.publishSticky(new ConfigEvent(config));
// 延迟订阅者立即收到：
eventBus.subscribe(ConfigEvent.class, e -> applyConfig(e));

// 发布事件
eventBus.publish(new UserRegisteredEvent(userId, email));

// 异步发布
eventBus.publishAsync(event)
    .thenRun(() -> System.out.println("事件已处理"));

// 指标
EventBusMetrics metrics = eventBus.getMetrics();
System.out.println("已发布: " + metrics.totalPublished());

// 自定义事件总线（带拦截器）
OpenEvent custom = OpenEvent.builder()
    .eventStore(new InMemoryEventStore(10000))
    .exceptionHandler(new LoggingExceptionHandler())
    .interceptor(event -> { log.info("发布中: {}", event); return true; })
    .build();

// 测试工具
EventCaptor<MyEvent> captor = new EventCaptor<>();
eventBus.subscribe(MyEvent.class, captor);
eventBus.publish(new MyEvent());
assertThat(captor.count()).isEqualTo(1);
```

## OpenEvent 方法参考

### 工厂方法

| 方法 | 说明 |
|------|------|
| `OpenEvent.getDefault()` | 获取共享单例事件总线实例 |
| `OpenEvent.create()` | 创建新的独立事件总线实例 |
| `OpenEvent.builder()` | 创建自定义配置的事件总线构建器 |

### 监听器注册

| 方法 | 说明 |
|------|------|
| `subscribe(Class<E>, EventListener<E>)` | 订阅并返回 `Subscription` 句柄 |
| `subscribe(Class<E>, EventListener<E>, Predicate<E>)` | 使用事件过滤谓词订阅 |
| `subscribe(Class<E>, EventListener<E>, Predicate<E>, boolean, int)` | 使用过滤器、异步标志和优先级订阅 |
| `on(Class<E>, EventListener<E>)` | 注册 Lambda 监听器（兼容旧接口，无返回值） |
| `on(Class<E>, EventListener<E>, boolean)` | 注册 Lambda 监听器，支持异步选项 |
| `on(Class<E>, EventListener<E>, boolean, int)` | 注册 Lambda 监听器，支持异步和优先级 |
| `register(Object)` | 注册基于注解的订阅者（`@Subscribe`） |
| `unregister(Object)` | 从订阅者注销所有监听器 |

### 事件发布

| 方法 | 说明 |
|------|------|
| `publish(Event)` | 同步发布事件 |
| `publishAsync(Event)` | 异步发布事件，返回 `CompletableFuture` |
| `publish(T)` | 发布任意数据，自动包装为 `DataEvent` |
| `publish(T, String)` | 发布数据并附带来源标识 |
| `publishAndWait(Event, Duration)` | 发布并阻塞等待处理完成或超时 |
| `publishSticky(Event)` | 发布粘性事件（存储并重放给未来订阅者） |

### 粘性事件

| 方法 | 说明 |
|------|------|
| `publishSticky(Event)` | 发布并存储粘性事件（按类型保留最后一个） |
| `getStickyEvent(Class<E>)` | 获取指定类型的最后一个粘性事件，无则返回 null |
| `removeStickyEvent(Class<E>)` | 移除并返回指定类型的粘性事件 |

### 拦截器

| 方法 | 说明 |
|------|------|
| `addInterceptor(EventInterceptor)` | 添加发布前/后拦截器 |
| `removeInterceptor(EventInterceptor)` | 移除拦截器 |

### 指标

| 方法 | 说明 |
|------|------|
| `getMetrics()` | 获取当前指标快照（`EventBusMetrics` 记录） |
| `resetMetrics()` | 重置所有指标计数器为零 |

### 配置

| 方法 | 说明 |
|------|------|
| `setEventStore(EventStore)` | 设置事件存储用于事件溯源 |
| `getEventStore()` | 获取当前事件存储 |
| `setExceptionHandler(EventExceptionHandler)` | 设置异常处理器 |
| `close()` | 关闭分发器和执行器，释放资源 |

### 构建器选项

| 方法 | 说明 |
|------|------|
| `asyncExecutor(ExecutorService)` | 自定义异步执行器 |
| `syncDispatcher(EventDispatcher)` | 自定义同步分发器 |
| `asyncDispatcher(EventDispatcher)` | 自定义异步分发器 |
| `eventStore(EventStore)` | 事件存储用于事件溯源 |
| `exceptionHandler(EventExceptionHandler)` | 自定义异常处理器 |
| `interceptor(EventInterceptor)` | 构建时添加拦截器 |

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0

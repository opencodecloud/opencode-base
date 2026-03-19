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

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-event</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenEvent` | 事件总线门面——发布/订阅操作的主入口 |
| `Event` | 基础事件类，包含 ID、时间戳、来源和取消功能 |
| `DataEvent` | 通用数据承载事件包装器 |
| `WaitableEvent` | 支持阻塞等待处理完成的事件 |
| `EventListener` | 函数式事件监听器接口 |
| **注解** | |
| `@Subscribe` | 将方法标记为事件监听器 |
| `@Async` | 将监听器标记为异步执行 |
| `@Priority` | 设置监听器执行优先级 |
| **分发器** | |
| `EventDispatcher` | 事件分发器接口 |
| `SyncDispatcher` | 同步事件分发器 |
| `AsyncDispatcher` | 使用虚拟线程的异步事件分发器 |
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
| **异常** | |
| `EventException` | 通用事件异常 |
| `EventListenerException` | 监听器相关异常 |
| `EventPublishException` | 发布相关异常 |
| `EventSecurityException` | 安全相关异常 |
| `EventStoreException` | 事件存储异常 |
| `EventErrorCode` | 错误码枚举 |

## 快速开始

```java
import cloud.opencode.base.event.OpenEvent;
import cloud.opencode.base.event.Event;

// 获取默认事件总线
OpenEvent eventBus = OpenEvent.getDefault();

// 注册 Lambda 监听器
eventBus.on(UserRegisteredEvent.class, event -> {
    System.out.println("用户已注册: " + event.getUserId());
});

// 注册基于注解的监听器
eventBus.register(new MyEventHandler());

// 同步发布事件
eventBus.publish(new UserRegisteredEvent(userId, email));

// 异步发布事件
eventBus.publishAsync(event)
    .thenRun(() -> System.out.println("事件已处理"));

// 发布并等待（带超时）
boolean completed = eventBus.publishAndWait(event, Duration.ofSeconds(5));

// 发布任意数据
eventBus.publish("你好，世界！");

// 自定义事件总线（带事件存储）
OpenEvent custom = OpenEvent.builder()
    .eventStore(new InMemoryEventStore(10000))
    .exceptionHandler(new LoggingExceptionHandler())
    .build();

// 基于注解的处理器类
public class MyEventHandler {
    @Subscribe
    @Async
    @Priority(10)
    public void onUserRegistered(UserRegisteredEvent event) {
        // 异步处理，高优先级
    }
}
```

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0

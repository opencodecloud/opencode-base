# OpenCode Base Pool

**高性能 Java 25+ 对象池库**

`opencode-base-pool` 是一个现代对象池库，支持通用池、键控池、线程本地池、软引用池和虚拟线程优化池。使用 JDK 25 Record 实现不可变配置，并提供全面的指标和驱逐策略。

## 功能特性

### 核心功能
- **通用对象池**：可配置的池，具有借用/归还语义和自动归还执行模式
- **键控对象池**：按键分隔的子池，适用于多租户或多资源场景
- **线程本地池**：每线程一个对象，零竞争访问
- **软引用池**：GC 友好的池，在内存压力下自动释放��象
- **虚拟线程池**：针对虚拟线程优化，支持 ScopedValue 上下文传播

### V1.0.3 新功能
- **PoolLease**（AutoCloseable）：try-with-resources 模式借用对象 -- 杜绝忘记 `returnObject()` 导致的泄漏
- **SimplePooledObjectFactory**：从 `Supplier`/`Consumer` 创建池 -- 一行代码创建池
- **PoolEventListener**：生命周期事件钩子（onBorrow、onReturn、onCreate、onDestroy、onEvict、onExhausted���onTimeout）
- **MaxAge 驱逐**：驱逐超过最大存活时间的对象（如每 30 分钟回收数据库连接）
- **最大对象生命周期**：配置级生命周期强制 -- 过期对象在借用时被拒绝
- **池预热**：`preparePool(count)` 方法按需预创建对象

### 配置
- **构建器模式**：不可变的 `PoolConfig` Record，带流式构建器
- **大小控制**：maxTotal、maxIdle、minIdle，可配置等待策略
- **超时**：可配置的借用超时，支持 BLOCK、FAIL 或 GROW ���略
- **LIFO/FIFO**：可配置的对象顺序

### 驱逐与验证
- **驱逐策略**：空闲时间、LRU、LFU、MaxAge 和组合（全部/任一）驱逐
- **定期驱逐**：可��置的驱逐运行间隔和批次大小
- **对象验证**：借用时、归还时、创建时和空闲时验证

### 可观测性
- **池指���**：借用计数、归还计数、活跃/空闲计数、等待时间统计
- **指标快照**：池指���的不可变时间点快照
- **可观测性导出器**：将指标导出到外部���控系统
- **对象追踪**：追踪池化对象生命周期并检测泄漏
- **事件监听器**：实时生命周期事件回调，用于监控和调试

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pool</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 最简用法（V1.0.3）
```java
import cloud.opencode.base.pool.*;

// 一行代码从 Supplier 创建池
ObjectPool<StringBuilder> pool = OpenPool.createPool(StringBuilder::new);

// try-with-resources（推荐 -- 防止泄漏）
try (PoolLease<StringBuilder> lease = pool.borrowLease()) {
    lease.get().append("Hello");
}

// 或者使用 Supplier + Consumer（销毁时自动关闭）
ObjectPool<Connection> pool = OpenPool.createPool(
    () -> DriverManager.getConnection(url),
    Connection::close
);
```

### 传统用法
```java
// 使用工厂创建池
ObjectPool<Connection> pool = OpenPool.createPool(myFactory);

// 执行模式（自动归还）
String result = pool.execute(conn -> conn.executeQuery("SELECT 1"));

// 手动借用/归还
Connection conn = pool.borrowObject();
try {
    conn.executeQuery("SELECT 1");
} finally {
    pool.returnObject(conn);
}
```

### 自定义配置
```java
PoolConfig config = PoolConfig.builder()
    .maxTotal(20)
    .maxIdle(10)
    .minIdle(5)
    .maxWait(Duration.ofSeconds(10))
    .testOnBorrow(true)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .maxObjectLifetime(Duration.ofHours(1))  // V1.0.3: 1小时后强制回收
    .build();

ObjectPool<Connection> pool = OpenPool.createPool(factory, config);
```

### 事件监听器（V1.0.3）
```java
PoolConfig config = PoolConfig.builder()
    .maxTotal(10)
    .eventListener(new PoolEventListener<Connection>() {
        @Override
        public void onExhausted() {
            log.warn("连接池已���尽！");
        }
        @Override
        public void onTimeout(Duration waitDuration) {
            log.error("等待 {}ms 后超时", waitDuration.toMillis());
        }
    })
    .build();
```

### 池租约与失效标记（V1.0.3）
```java
try (PoolLease<Connection> lease = pool.borrowLease()) {
    try {
        lease.get().executeUpdate("INSERT ...");
    } catch (SQLException e) {
        lease.invalidate();  // 标记为坏对象 -- 将被销毁而非归还
        throw e;
    }
}
```

### 池预热（V1.0.3）
```java
ObjectPool<Connection> pool = OpenPool.createPool(factory, config);
pool.preparePool(5);  // 预创建 5 个对象
```

### 键控池
```java
KeyedObjectPool<String, Connection> pool =
    OpenPool.createKeyedPool(keyedFactory, config);

Connection conn = pool.borrowObject("database-1");
try {
    // 使用 database-1 的连接
} finally {
    pool.returnObject("database-1", conn);
}
```

### 虚拟线程池
```java
ObjectPool<Connection> pool = OpenPool.createVirtualThreadPool(factory, config);
```

### 驱逐策略
```java
// MaxAge 驱逐：回收超过 30 分钟的对象（V1.0.3）
EvictionPolicy<Connection> maxAge = OpenPool.maxAgeEviction(Duration.ofMinutes(30));

// 组合驱逐：空闲 > 30分钟 且 借用���数 < 5
EvictionPolicy<Connection> policy = OpenPool.allEviction(
    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
    OpenPool.lfuEviction(5)
);

PoolConfig config = PoolConfig.builder()
    .evictionPolicy(policy)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .build();
```

### 简化工厂（V1.0.3）
```java
// 使用构建器完全控制
PooledObjectFactory<Connection> factory = SimplePooledObjectFactory
    .<Connection>builder(() -> DriverManager.getConnection(url))
    .destroyer(Connection::close)
    .validator(conn -> !conn.isClosed())
    .passivator(Connection::clearWarnings)
    .build();
```

## 类参考

### 根�� (`cloud.opencode.base.pool`)
| 类 | 说明 |
|----|------|
| `OpenPool` | 主门面类，提供创建所有池类型的工厂方�� |
| `ObjectPool<T>` | 核心对象池接口，具有借用/归还/执行/borrowLease 语义 |
| `KeyedObjectPool<K,V>` | 键控对象池接口，按键管理子池 |
| `PoolConfig` | 不可变的池配置 Record，支持构建器模式 |
| `PoolContext` | 池操作期间传递的上下文���息 |
| `PooledObject<T>` | 池化对象的包装器，带状态和时间戳追踪 |
| `PooledObjectFactory<T>` | 创建、验证和销毁池化对象��工厂接口 |
| `PoolLease<T>` | AutoCloseable 租约，用于 try-with-resources 借用模式 **（V1.0.3）** |
| `PoolEventListener<T>` | 生命周期事件监听器，用于池��控 **（V1.0.3）** |

### 工厂 (`cloud.opencode.base.pool.factory`)
| 类 | ���明 |
|----|------|
| `BasePooledObjectFactory<T>` | 简单池化��象工厂的抽象基类 |
| `BaseKeyedPooledObjectFactory<K,V>` | 键控池化对象工厂的抽象基类 |
| `DefaultPooledObject<T>` | PooledObject 的默认实现，带状态管理 |
| `KeyedPooledObjectFactory<K,V>` | 键控池化对象的工厂接�� |
| `PooledObjectState` | 池化对象生命周期状态枚举（IDLE、ALLOCATED 等） |
| `SimplePooledObjectFactory<T>` | 基于 Supplier/Consumer 的简化工厂 **（V1.0.3）** |

### 实现 (`cloud.opencode.base.pool.impl`)
| 类 | 说明 |
|----|------|
| `GenericObjectPool<T>` | 功能完整的通用对象池实现 |
| `GenericKeyedObjectPool<K,V>` | 功能完整的键控对象池实现 |
| `ThreadLocalPool<T>` | 线程本地池，每线程一个对象 |
| `SoftReferencePool<T>` | 使用���引用的 GC ��好池 |
| `VirtualThreadPool<T>` | 针对虚拟线��优化的池，支持 ScopedValue |
| `IdentityWrapper<T>` | 基于身份的包装器，用于池���象追踪 |

### 策略 (`cloud.opencode.base.pool.policy`)
| 类 | 说明 |
|----|------|
| `EvictionPolicy<T>` | 密封接口：IdleTime、LRU、LFU、MaxAge、Composite |
| `EvictionPolicy.MaxAge<T>` | 基于对象年龄的驱逐策略 **（V1.0.3）** |
| `ValidationPolicy<T>` | 池化对象的验证策略 |
| `WaitPolicy` | 池耗尽时的行为枚举（BLOCK、FAIL、GROW��� |
| `EvictionContext<T>` | 驱逐评估期间提供给策略的上下文 |

### 指标 (`cloud.opencode.base.pool.metrics`)
| 类 | 说明 |
|----|------|
| `PoolMetrics` | 池指标���问接口 |
| `DefaultPoolMetrics` | 使用原子计数器的默认指标实现 |
| `MetricsSnapshot` | 池指标的不可变时间点快照 |
| `ObservabilityMetricsExporter` | 将池指标导出到可观测性系统 |

### 追踪器 (`cloud.opencode.base.pool.tracker`)
| 类 | 说明 |
|----|------|
| `PooledObjectTracker<T>` | 追踪池化对象生命周期以检测泄漏 |

### 异常 (`cloud.opencode.base.pool.exception`)
| 类 | 说明 |
|----|------|
| `OpenPoolException` | 池操��错误的运行时异常 |

## 环境要求

- Java 25+
- 无外部���赖

## 性能 (V1.0.3)

单线程 borrow+return 延迟：**~80 ns/op**（比 V1.0.0 快 9.3 倍）

| 场景 | 吞吐量 (ops/ms) | 延迟 (ns/op) |
|------|----------------:|-------------:|
| borrowReturn（手动） | 12,435 | 80 |
| borrowReturn（PoolLease） | 11,938 | 84 |
| borrowReturn（execute） | 11,942 | 84 |
| getNumIdle+getNumActive | 189,027 | 5.3 |
| 并发（4 线程） | 1,846 | 542 |
| 并发（100 虚拟线程） | 1,283 | 779 |

关键优化：热路径零分配（nanoTime 时间戳，无 `Instant.now()`）、O(1) 空闲计数跟踪、合并借用时间戳。

## 配置校验 (V1.0.3)

`PoolConfig.builder().build()` 强制执行：
- `maxTotal >= 1`
- `maxIdle >= 0`，`minIdle >= 0`
- 自动钳位：`maxIdle = min(maxIdle, maxTotal)`，`minIdle = min(minIdle, maxIdle)`

## 许可证

Apache License 2.0

## 作���

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

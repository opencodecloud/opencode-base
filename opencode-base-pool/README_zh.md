# OpenCode Base Pool

**高性能 Java 25+ 对象池库**

`opencode-base-pool` 是一个现代对象池库，支持通用池、键控池、线程本地池、软引用池和虚拟线程优化池。使用 JDK 25 Record 实现不可变配置，并提供全面的指标和驱逐策略。

## 功能特性

### 核心功能
- **通用对象池**：可配置的池，具有借用/归还语义和自动归还执行模式
- **键控对象池**：按键分隔的子池，适用于多租户或多资源场景
- **线程本地池**：每线程一个对象，零竞争访问
- **软引用池**：GC 友好的池，在内存压力下自动释放对象
- **虚拟线程池**：针对虚拟线程优化，支持 ScopedValue 上下文传播

### 配置
- **构建器模式**：不可变的 `PoolConfig` Record，带流式构建器
- **大小控制**：maxTotal、maxIdle、minIdle，可配置等待策略
- **超时**：可配置的借用超时，支持 BLOCK 或 FAIL 策略
- **LIFO/FIFO**：可配置的对象顺序

### 驱逐与验证
- **驱逐策略**：空闲时间、LRU、LFU 和组合（全部/任一）驱逐
- **定期驱逐**：可配置的驱逐运行间隔和批次大小
- **对象验证**：借用时、归还时、创建时和空闲时验证

### 可观测性
- **池指标**：借用计数、归还计数、活跃/空闲计数、等待时间统计
- **指标快照**：池指标的不可变时间点快照
- **可观测性导出器**：将指标导出到外部监控系统
- **对象追踪**：追踪池化对象生命周期并检测泄漏

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pool</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法
```java
import cloud.opencode.base.pool.*;

// 使用默认配置创建池
ObjectPool<Connection> pool = OpenPool.createPool(myFactory);

// 手动借用和归还
Connection conn = pool.borrowObject();
try {
    conn.executeQuery("SELECT 1");
} finally {
    pool.returnObject(conn);
}

// 执行模式（推荐 -- 自动归还）
String result = pool.execute(conn -> conn.executeQuery("SELECT 1"));
```

### 自定义配置
```java
PoolConfig config = OpenPool.configBuilder()
    .maxTotal(20)
    .maxIdle(10)
    .minIdle(5)
    .maxWait(Duration.ofSeconds(10))
    .testOnBorrow(true)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .build();

ObjectPool<Connection> pool = OpenPool.createPool(factory, config);
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
// 组合驱逐：空闲 > 30分钟 且 借用次数 < 5
EvictionPolicy<Connection> policy = OpenPool.allEviction(
    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
    OpenPool.lfuEviction(5)
);

PoolConfig config = OpenPool.configBuilder()
    .evictionPolicy(policy)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .build();
```

## 类参考

### 根包 (`cloud.opencode.base.pool`)
| 类 | 说明 |
|----|------|
| `OpenPool` | 主门面类，提供创建所有池类型的工厂方法 |
| `ObjectPool<T>` | 核心对象池接口，具有借用/归还/执行语义 |
| `KeyedObjectPool<K,V>` | 键控对象池接口，按键管理子池 |
| `PoolConfig` | 不可变的池配置 Record，支持构建器模式 |
| `PoolContext` | 池操作期间传递的上下文信息 |
| `PooledObject<T>` | 池化对象的包装器，带状态和时间戳追踪 |
| `PooledObjectFactory<T>` | 创建、验证和销毁池化对象的工厂接口 |

### 工厂 (`cloud.opencode.base.pool.factory`)
| 类 | 说明 |
|----|------|
| `BasePooledObjectFactory<T>` | 简单池化对象工厂的抽象基类 |
| `BaseKeyedPooledObjectFactory<K,V>` | 键控池化对象工厂的抽象基类 |
| `DefaultPooledObject<T>` | PooledObject 的默认实现，带状态管理 |
| `KeyedPooledObjectFactory<K,V>` | 键控池化对象的工厂接口 |
| `PooledObjectState` | 池化对象生命周期状态枚举（IDLE、ALLOCATED 等） |

### 实现 (`cloud.opencode.base.pool.impl`)
| 类 | 说明 |
|----|------|
| `GenericObjectPool<T>` | 功能完整的通用对象池实现 |
| `GenericKeyedObjectPool<K,V>` | 功能完整的键控对象池实现 |
| `ThreadLocalPool<T>` | 线程本地池，每线程一个对象 |
| `SoftReferencePool<T>` | 使用软引用的 GC 友好池 |
| `VirtualThreadPool<T>` | 针对虚拟线程优化的池，支持 ScopedValue |
| `IdentityWrapper<T>` | 基于身份的包装器，用于池对象追踪 |

### 策略 (`cloud.opencode.base.pool.policy`)
| 类 | 说明 |
|----|------|
| `EvictionPolicy<T>` | 驱逐策略接口及内置实现 |
| `ValidationPolicy<T>` | 池化对象的验证策略 |
| `WaitPolicy` | 池耗尽时的行为枚举（BLOCK、FAIL、GROW） |
| `EvictionContext<T>` | 驱逐评估期间提供给策略的上下文 |

### 指标 (`cloud.opencode.base.pool.metrics`)
| 类 | 说明 |
|----|------|
| `PoolMetrics` | 池指标访问接口 |
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
| `OpenPoolException` | 池操作错误的运行时异常 |

## 环境要求

- Java 25+
- 无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

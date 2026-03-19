# Pool 组件方案

## 1. 组件概述

`opencode-base-pool` 是基于 JDK 25 的高性能通用对象池组件，支持通用对象池、键控对象池、Virtual Thread 优化池、线程本地池、软引用池等多种池实现。利用 JDK 25 的 Sealed Types、Record、ScopedValue 等特性，提供类型安全的驱逐策略和现代化的池上下文管理。零第三方依赖。

## 2. 包结构

```
cloud.opencode.base.pool
├── ObjectPool.java                  # 对象池接口
├── KeyedObjectPool.java             # 键控对象池接口
├── PooledObject.java                # 池化对象包装接口
├── PooledObjectFactory.java         # 池化对象工厂接口
├── PoolConfig.java                  # 池配置 (Record)
├── PoolContext.java                 # 池上下文 (ScopedValue)
├── OpenPool.java                    # 池门面入口类
│
├── factory/                         # 工厂
│   ├── BasePooledObjectFactory.java         # 基础工厂(子类只需实现 create)
│   ├── KeyedPooledObjectFactory.java        # 键控工厂接口
│   ├── BaseKeyedPooledObjectFactory.java    # 基础键控工厂
│   ├── DefaultPooledObject.java             # 默认池化对象实现
│   └── PooledObjectState.java               # 池化对象状态枚举
│
├── policy/                          # 策略
│   ├── EvictionPolicy.java          # 驱逐策略 (Sealed Interface)
│   ├── EvictionContext.java         # 驱逐上下文 (Record)
│   ├── ValidationPolicy.java        # 验证策略 (Record)
│   └── WaitPolicy.java             # 等待策略枚举
│
├── metrics/                         # 指标
│   ├── PoolMetrics.java             # 池指标接口
│   ├── DefaultPoolMetrics.java      # 默认指标实现
│   ├── MetricsSnapshot.java         # 指标快照 (Record)
│   └── ObservabilityMetricsExporter.java  # 可观测性指标导出
│
├── tracker/                         # 追踪
│   └── PooledObjectTracker.java     # 池化对象追踪器(泄漏检测)
│
└── exception/                       # 异常
    └── OpenPoolException.java       # 对象池异常
```

## 3. 核心 API

### 3.1 OpenPool

> 池组件门面入口类，提供简化的对象池创建和驱逐策略工厂方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <T> ObjectPool<T> createPool(PooledObjectFactory<T> factory)` | 创建默认配置对象池 |
| `static <T> ObjectPool<T> createPool(PooledObjectFactory<T> factory, PoolConfig config)` | 创建自定义配置对象池 |
| `static <K, V> KeyedObjectPool<K, V> createKeyedPool(KeyedPooledObjectFactory<K, V> factory, PoolConfig config)` | 创建键控对象池 |
| `static <T> ObjectPool<T> createThreadLocalPool(PooledObjectFactory<T> factory)` | 创建线程本地池 |
| `static <T> ObjectPool<T> createSoftReferencePool(PooledObjectFactory<T> factory)` | 创建软引用池(默认配置) |
| `static <T> ObjectPool<T> createSoftReferencePool(PooledObjectFactory<T> factory, PoolConfig config)` | 创建软引用池(自定义配置) |
| `static <T> ObjectPool<T> createVirtualThreadPool(PooledObjectFactory<T> factory)` | 创建 Virtual Thread 优化池(默认配置) |
| `static <T> ObjectPool<T> createVirtualThreadPool(PooledObjectFactory<T> factory, PoolConfig config)` | 创建 Virtual Thread 优化池(自定义配置) |
| `static PoolConfig.Builder configBuilder()` | 创建配置构建器 |
| `static PoolConfig defaultConfig()` | 获取默认配置 |
| `static <T> EvictionPolicy<T> idleTimeEviction(Duration maxIdleTime)` | 空闲时间驱逐策略 |
| `static <T> EvictionPolicy<T> lruEviction(int maxObjects)` | LRU 驱逐策略 |
| `static <T> EvictionPolicy<T> lfuEviction(long minBorrowCount)` | LFU 驱逐策略 |
| `static <T> EvictionPolicy<T> allEviction(EvictionPolicy<T>... policies)` | 组合策略(全部满足) |
| `static <T> EvictionPolicy<T> anyEviction(EvictionPolicy<T>... policies)` | 组合策略(任一满足) |

**示例:**

```java
// 创建对象池
PooledObjectFactory<Connection> factory = new BasePooledObjectFactory<>() {
    @Override
    protected Connection create() throws OpenPoolException {
        return DriverManager.getConnection(url, user, password);
    }
};

ObjectPool<Connection> pool = OpenPool.createPool(factory,
    OpenPool.configBuilder()
        .maxTotal(20)
        .maxIdle(10)
        .minIdle(5)
        .testOnBorrow(true)
        .build());

// 使用 execute 自动借还
pool.execute(conn -> {
    conn.createStatement().execute("SELECT 1");
});

// 自定义驱逐策略
EvictionPolicy<Connection> policy = OpenPool.anyEviction(
    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
    OpenPool.lfuEviction(5)
);
```

### 3.2 ObjectPool

> 对象池接口，定义借用、归还、失效、执行等核心操作。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `T borrowObject()` | 借用对象 |
| `T borrowObject(Duration timeout)` | 带超时借用 |
| `void returnObject(T obj)` | 归还对象 |
| `void invalidateObject(T obj)` | 使对象失效 |
| `void addObject()` | 向池中添加对象 |
| `int getNumIdle()` | 空闲对象数 |
| `int getNumActive()` | 活跃对象数 |
| `void clear()` | 清空池 |
| `PoolMetrics getMetrics()` | 获取池指标 |
| `default <R> R execute(Function<T, R> action)` | 执行池化操作(自动借还) |
| `default void execute(Consumer<T> action)` | 执行无返回值操作(自动借还) |
| `void close()` | 关闭池 |

**示例:**

```java
// 手动借还
Connection conn = pool.borrowObject();
try {
    Statement stmt = conn.createStatement();
    ResultSet rs = stmt.executeQuery("SELECT * FROM users");
} finally {
    pool.returnObject(conn);
}

// 自动借还(推荐)
String result = pool.execute(conn -> {
    return conn.createStatement().executeQuery("SELECT 1").getString(1);
});
```

### 3.3 KeyedObjectPool

> 键控对象池接口，按 Key 管理多个独立的对象池。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `V borrowObject(K key)` | 借用指定 Key 的对象 |
| `V borrowObject(K key, Duration timeout)` | 带超时借用 |
| `void returnObject(K key, V obj)` | 归还指定 Key 的对象 |
| `void invalidateObject(K key, V obj)` | 使指定 Key 的对象失效 |
| `int getNumIdle(K key)` | 指定 Key 的空闲数 |
| `int getNumActive(K key)` | 指定 Key 的活跃数 |
| `void clear(K key)` | 清空指定 Key 的池 |
| `void clear()` | 清空所有池 |
| `default <R> R execute(K key, BiFunction<K, V, R> action)` | 执行键控操作 |
| `void close()` | 关闭所有池 |

**示例:**

```java
// 多数据源连接池
KeyedObjectPool<String, Connection> pool = OpenPool.createKeyedPool(
    new BaseKeyedPooledObjectFactory<>() {
        @Override
        protected Connection create(String dsName) throws OpenPoolException {
            DataSourceConfig config = getConfig(dsName);
            return DriverManager.getConnection(config.url());
        }
    }, OpenPool.defaultConfig());

Connection master = pool.borrowObject("master");
Connection slave = pool.borrowObject("slave");
try {
    // 使用连接
} finally {
    pool.returnObject("master", master);
    pool.returnObject("slave", slave);
}
```

### 3.4 PooledObjectFactory / BasePooledObjectFactory

> 池化对象工厂接口及基础实现。子类只需实现 `create()` 方法。

**PooledObjectFactory 接口方法:**

| 方法 | 描述 |
|------|------|
| `PooledObject<T> makeObject()` | 创建池化对象 |
| `void destroyObject(PooledObject<T> obj)` | 销毁对象 |
| `boolean validateObject(PooledObject<T> obj)` | 验证对象是否有效 |
| `void activateObject(PooledObject<T> obj)` | 激活对象(借出前调用) |
| `void passivateObject(PooledObject<T> obj)` | 钝化对象(归还后调用) |

**BasePooledObjectFactory 方法:**

| 方法 | 描述 |
|------|------|
| `protected abstract T create()` | 创建实际对象(子类实现) |
| `protected PooledObject<T> wrap(T obj)` | 包装为池化对象 |

**示例:**

```java
PooledObjectFactory<StringBuilder> factory = new BasePooledObjectFactory<>() {
    @Override
    protected StringBuilder create() {
        return new StringBuilder(256);
    }

    @Override
    public void passivateObject(PooledObject<StringBuilder> obj) {
        obj.getObject().setLength(0); // 归还时重置
    }

    @Override
    public boolean validateObject(PooledObject<StringBuilder> obj) {
        return obj.getObject().capacity() > 0;
    }
};
```

### 3.5 KeyedPooledObjectFactory / BaseKeyedPooledObjectFactory

> 键控池化对象工厂接口及基础实现。每个 Key 可产生不同配置的对象。

**KeyedPooledObjectFactory 接口方法:**

| 方法 | 描述 |
|------|------|
| `PooledObject<V> makeObject(K key)` | 按 Key 创建池化对象 |
| `void destroyObject(K key, PooledObject<V> obj)` | 按 Key 销毁对象 |
| `boolean validateObject(K key, PooledObject<V> obj)` | 按 Key 验证对象 |
| `void activateObject(K key, PooledObject<V> obj)` | 按 Key 激活对象 |
| `void passivateObject(K key, PooledObject<V> obj)` | 按 Key 钝化对象 |

**BaseKeyedPooledObjectFactory 方法:**

| 方法 | 描述 |
|------|------|
| `protected abstract V create(K key)` | 按 Key 创建实际对象(子类实现) |
| `protected PooledObject<V> wrap(V obj)` | 包装为池化对象 |

### 3.6 PooledObject (接口) / DefaultPooledObject

> 池化对象包装，封装对象的状态、借用/归还时间、借用次数等元数据。

**PooledObject 接口方法:**

| 方法 | 描述 |
|------|------|
| `T getObject()` | 获取实际对象 |
| `Instant getCreateInstant()` | 创建时间 |
| `Instant getLastBorrowInstant()` | 最后借用时间 |
| `Instant getLastReturnInstant()` | 最后归还时间 |
| `Instant getLastUseInstant()` | 最后使用时间 |
| `PooledObjectState getState()` | 当前状态 |
| `long getBorrowCount()` | 借用次数 |
| `Duration getActiveDuration()` | 活跃时长 |
| `Duration getIdleDuration()` | 空闲时长 |
| `boolean compareAndSetState(PooledObjectState expect, PooledObjectState update)` | CAS 状态转换 |

**DefaultPooledObject 额外方法:**

| 方法 | 描述 |
|------|------|
| `DefaultPooledObject(T object)` | 构造函数 |
| `void markBorrowed()` | 标记为已借出 |
| `void markReturned()` | 标记为已归还 |
| `void setState(PooledObjectState newState)` | 设置状态 |

### 3.7 PooledObjectState

> 池化对象状态枚举，定义对象在池中的生命周期状态。

**状态值:**

| 值 | 描述 |
|------|------|
| `IDLE` | 空闲(在池中等待借用) |
| `ALLOCATED` | 已分配(已被借出) |
| `EVICTION` | 驱逐中 |
| `VALIDATION` | 验证中 |
| `INVALID` | 已失效 |
| `RETURNING` | 归还中 |
| `ABANDONED` | 已废弃(超时未归还) |

**状态转换:**
```
IDLE -> ALLOCATED (borrow)
ALLOCATED -> RETURNING -> IDLE (return)
IDLE -> EVICTION -> INVALID (evict)
ALLOCATED -> INVALID (invalidate)
Any -> ABANDONED (timeout detection)
```

### 3.8 PoolConfig

> 对象池配置 (JDK 25 Record)，通过 Builder 模式构建。

**Record 字段:**

| 字段 | 类型 | 默认值 | 描述 |
|------|------|--------|------|
| `maxTotal` | `int` | 8 | 最大对象总数 |
| `maxIdle` | `int` | 8 | 最大空闲数 |
| `minIdle` | `int` | 0 | 最小空闲数 |
| `maxWait` | `Duration` | 30s | 借用最大等待时间 |
| `minEvictableIdleTime` | `Duration` | 30min | 最小可驱逐空闲时间 |
| `timeBetweenEvictionRuns` | `Duration` | 0(禁用) | 驱逐任务间隔 |
| `numTestsPerEvictionRun` | `int` | 3 | 每次驱逐检查数量 |
| `testOnBorrow` | `boolean` | false | 借出时验证 |
| `testOnReturn` | `boolean` | false | 归还时验证 |
| `testOnCreate` | `boolean` | false | 创建时验证 |
| `testWhileIdle` | `boolean` | false | 空闲时验证 |
| `lifo` | `boolean` | true | 后进先出 |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static Builder builder()` | 创建构建器 |
| `static PoolConfig defaults()` | 获取默认配置 |
| `boolean isEvictionEnabled()` | 是否启用驱逐 |
| `boolean blockWhenExhausted()` | 耗尽时是否阻塞 |

**示例:**

```java
PoolConfig config = PoolConfig.builder()
    .maxTotal(20)
    .maxIdle(10)
    .minIdle(5)
    .maxWait(Duration.ofSeconds(10))
    .testOnBorrow(true)
    .testWhileIdle(true)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .minEvictableIdleTime(Duration.ofMinutes(30))
    .build();
```

### 3.9 EvictionPolicy (Sealed Interface)

> 密封驱逐策略接口 (JDK 25 Sealed Types)，编译器强制穷尽匹配。

**实现类:**

| 实现 | 描述 |
|------|------|
| `EvictionPolicy.IdleTime<T>` | 空闲时间驱逐(超过指定时间则驱逐) |
| `EvictionPolicy.LRU<T>` | LRU 驱逐(超过最大空闲数则驱逐) |
| `EvictionPolicy.LFU<T>` | LFU 驱逐(借用次数低于阈值则驱逐) |
| `EvictionPolicy.Composite<T>` | 组合驱逐(支持全部/任一满足) |

**接口方法:**

| 方法 | 描述 |
|------|------|
| `boolean evict(PooledObject<T> obj, EvictionContext context)` | 判断是否应该驱逐 |

**示例:**

```java
// 类型安全的穷尽匹配
String info = switch (policy) {
    case EvictionPolicy.IdleTime<T>(var duration) ->
        "空闲超过 " + duration + " 驱逐";
    case EvictionPolicy.LRU<T>(var max) ->
        "超过 " + max + " 个空闲对象驱逐";
    case EvictionPolicy.LFU<T>(var minCount) ->
        "借用次数低于 " + minCount + " 驱逐";
    case EvictionPolicy.Composite<T>(var policies, var all) ->
        "组合: " + (all ? "全部" : "任一") + " 满足, " + policies.size() + " 条规则";
};

// 组合策略
EvictionPolicy<Connection> combined = OpenPool.allEviction(
    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
    OpenPool.lfuEviction(3)
);
```

### 3.10 EvictionContext

> 驱逐上下文记录 (JDK 25 Record)，提供驱逐决策所需的池状态信息。

**Record 字段:**

| 字段 | 描述 |
|------|------|
| `int currentIdleCount` | 当前空闲数 |
| `int currentActiveCount` | 当前活跃数 |
| `int maxTotal` | 最大总数 |
| `Instant evictionTime` | 驱逐时间 |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `int totalCount()` | 总对象数 |
| `boolean isAtCapacity()` | 是否已满 |
| `double idleRatio()` | 空闲比率 |

### 3.11 ValidationPolicy

> 验证策略记录 (JDK 25 Record)，控制在哪些阶段执行对象验证。

**Record 字段:**

| 字段 | 描述 |
|------|------|
| `boolean testOnBorrow` | 借出时验证 |
| `boolean testOnReturn` | 归还时验证 |
| `boolean testOnCreate` | 创建时验证 |
| `boolean testWhileIdle` | 空闲时验证 |

**工厂方法:**

| 方法 | 描述 |
|------|------|
| `static ValidationPolicy none()` | 不验证 |
| `static ValidationPolicy onBorrow()` | 仅借出时验证 |
| `static ValidationPolicy recommended()` | 推荐配置(借出+空闲) |
| `static ValidationPolicy strict()` | 严格配置(全部验证) |
| `boolean hasAnyValidation()` | 是否有任何验证 |

### 3.12 WaitPolicy

> 等待策略枚举，定义池耗尽时的等待行为。

**枚举值:**

| 值 | 描述 |
|------|------|
| `BLOCK` | 阻塞等待(默认) |
| `FAIL` | 立即失败 |
| `GROW` | 自动扩容 |

### 3.13 PoolContext

> ScopedValue 池上下文 (JDK 25 JEP 501)，支持在池操作中传递上下文信息。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static PoolContext create(String poolName)` | 创建命名上下文 |
| `static PoolContext create()` | 创建匿名上下文 |
| `static <T, X extends Throwable> T run(PoolContext context, CallableOp<T, X> task)` | 在上下文中执行 |
| `static void run(PoolContext context, Runnable task)` | 在上下文中运行 |
| `static Optional<PoolContext> current()` | 获取当前上下文 |
| `static PoolContext currentOrCreate()` | 获取或创建上下文 |
| `void recordBorrow(Object object)` | 记录借用 |
| `void recordReturn()` | 记录归还 |
| `boolean hasBorrowedObject()` | 是否有借出对象 |
| `PoolContext setAttribute(String key, Object value)` | 设置属性 |
| `<T> Optional<T> getAttribute(String key)` | 获取属性 |
| `String poolName()` | 池名称 |
| `Instant createdAt()` | 创建时间 |
| `Optional<Instant> borrowTime()` | 借用时间 |
| `boolean isVirtualThread()` | 是否虚拟线程 |

**示例:**

```java
PoolContext context = PoolContext.create("connectionPool");
PoolContext.run(context, () -> {
    // 在上下文中执行池操作
    String poolName = PoolContext.current().get().poolName();
});
```

### 3.14 PoolMetrics (接口) / DefaultPoolMetrics

> 池指标接口及默认实现，提供借用/归还/创建/销毁统计。

**PoolMetrics 接口方法:**

| 方法 | 描述 |
|------|------|
| `long getBorrowCount()` | 借用次数 |
| `long getReturnCount()` | 归还次数 |
| `long getCreatedCount()` | 创建次数 |
| `long getDestroyedCount()` | 销毁次数 |
| `Duration getAverageBorrowDuration()` | 平均借用时长 |
| `Duration getMaxBorrowDuration()` | 最大借用时长 |
| `Duration getAverageWaitDuration()` | 平均等待时长 |
| `MetricsSnapshot snapshot()` | 创建指标快照 |

**DefaultPoolMetrics 额外方法:**

| 方法 | 描述 |
|------|------|
| `void setActiveSupplier(IntSupplier supplier)` | 设置活跃数供应者 |
| `void setIdleSupplier(IntSupplier supplier)` | 设置空闲数供应者 |
| `void recordBorrow()` | 记录借用 |
| `void recordReturn()` | 记录归还 |
| `void recordCreate()` | 记录创建 |
| `void recordDestroy()` | 记录销毁 |
| `void recordBorrowDuration(Duration duration)` | 记录借用时长 |
| `void recordWaitDuration(Duration duration)` | 记录等待时长 |
| `void reset()` | 重置指标 |

### 3.15 MetricsSnapshot

> 指标快照记录 (JDK 25 Record)，提供池运行时统计的不可变快照。

**Record 字段:**

| 字段 | 描述 |
|------|------|
| `long borrowCount` | 借用次数 |
| `long returnCount` | 归还次数 |
| `long createdCount` | 创建次数 |
| `long destroyedCount` | 销毁次数 |
| `int currentActive` | 当前活跃数 |
| `int currentIdle` | 当前空闲数 |
| `Duration avgBorrowDuration` | 平均借用时长 |
| `Duration maxBorrowDuration` | 最大借用时长 |
| `Duration avgWaitDuration` | 平均等待时长 |
| `Instant timestamp` | 快照时间 |

**主要方法:**

| 方法 | 描述 |
|------|------|
| `int totalCount()` | 总对象数 |
| `double utilizationRate()` | 利用率 |
| `double hitRate()` | 命中率 |
| `double creationRate()` | 创建率 |

**示例:**

```java
MetricsSnapshot snapshot = pool.getMetrics().snapshot();
System.out.println("借用次数: " + snapshot.borrowCount());
System.out.println("当前活跃: " + snapshot.currentActive());
System.out.println("利用率: " + snapshot.utilizationRate());
System.out.println("平均等待: " + snapshot.avgWaitDuration().toMillis() + "ms");
```

### 3.16 ObservabilityMetricsExporter

> 可观测性指标导出，将池指标注册到 OpenMetrics 可观测性系统。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static boolean isObservabilityModuleAvailable()` | 可观测性模块是否可用 |
| `static void export(String poolName, PoolMetrics metrics)` | 导出指标 |
| `static void export(String prefix, String poolName, PoolMetrics metrics)` | 带前缀导出指标 |

### 3.17 PooledObjectTracker

> 池化对象追踪器，用于检测对象泄漏(忘记归还)和监控池使用。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `PooledObjectTracker()` | 创建默认追踪器 |
| `PooledObjectTracker(Duration abandonedTimeout, Consumer<TrackedObject<T>> abandonedHandler)` | 指定超时和处理器 |
| `void trackBorrow(PooledObject<T> pooledObject)` | 追踪借出 |
| `void trackReturn(PooledObject<T> pooledObject)` | 追踪归还 |
| `List<TrackedObject<T>> getTrackedObjects()` | 获取所有追踪对象 |
| `List<TrackedObject<T>> getAbandonedObjects()` | 获取废弃对象 |
| `int checkAndHandleAbandoned()` | 检查并处理废弃对象 |
| `int getTrackedCount()` | 追踪中对象数 |
| `void clear()` | 清空追踪 |

**TrackedObject Record:**

| 方法 | 描述 |
|------|------|
| `Duration borrowDuration()` | 借用时长 |
| `String stackTraceString()` | 借用时的堆栈 |

### 3.18 OpenPoolException

> 对象池异常，包含池名称和错误类型分类。

**PoolErrorType 枚举:**

| 值 | 描述 |
|------|------|
| `GENERAL` | 一般错误 |
| `EXHAUSTED` | 池耗尽 |
| `TIMEOUT` | 等待超时 |
| `VALIDATION` | 验证失败 |
| `CLOSED` | 池已关闭 |
| `CREATE` | 创建失败 |
| `DESTROY` | 销毁失败 |
| `ACTIVATE` | 激活失败 |
| `PASSIVATE` | 钝化失败 |

**工厂方法:**

| 方法 | 描述 |
|------|------|
| `static OpenPoolException exhausted(String poolName)` | 池耗尽异常 |
| `static OpenPoolException timeout(String poolName, Duration timeout)` | 超时异常 |
| `static OpenPoolException validationFailed(String poolName)` | 验证失败异常 |
| `static OpenPoolException closed(String poolName)` | 池已关闭异常 |
| `static OpenPoolException createFailed(String poolName, Throwable cause)` | 创建失败异常 |
| `static OpenPoolException destroyFailed(String poolName, Throwable cause)` | 销毁失败异常 |
| `static OpenPoolException invalidState(String message)` | 非法状态异常 |

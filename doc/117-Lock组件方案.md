# Lock 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-lock` 模块提供统一的锁抽象与实现，支持本地锁和分布式锁，帮助构建高并发、线程安全的应用程序。基于 JDK 25 构建，充分利用 Virtual Thread、Record、Sealed Interface 等现代语言特性。

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            OpenLock (统一门面)                               │
│  lock() / fairLock() / readWriteLock() / spinLock() / segmentLock()        │
│  namedLockFactory() / lockGroup() / manager()                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                              锁抽象层                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                     Lock<T> 统一锁接口                               │    │
│  │  ├─ lock() / tryLock() / unlock()                                   │    │
│  │  ├─ lockInterruptibly()                                             │    │
│  │  └─ execute(action) / executeWithResult(supplier)                   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                ReadWriteLock<T> 读写锁接口                           │    │
│  │  ├─ readLock() / writeLock()                                        │    │
│  │  └─ executeRead() / executeWrite()                                  │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────────────────┤
│                              本地锁层                                        │
│  ┌─────────────┐ ┌─────────────────┐ ┌─────────────┐ ┌─────────────┐      │
│  │  LocalLock   │ │LocalReadWrite   │ │  SpinLock   │ │ SegmentLock │      │
│  │ (可重入锁)   │ │    Lock         │ │ (自旋锁)    │ │ (分段锁)    │      │
│  └─────────────┘ └─────────────────┘ └─────────────┘ └─────────────┘      │
├─────────────────────────────────────────────────────────────────────────────┤
│                           分布式锁层 (SPI)                                   │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  DistributedLock (接口，扩展 Lock<String>)                          │    │
│  │  └─ 通过 DistributedLockProvider SPI 提供具体实现                    │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────────────────┤
│                              管理层                                          │
│  ┌───────────────────┐ ┌───────────────────┐ ┌─────────────────────────┐    │
│  │   LockManager     │ │  NamedLockFactory │ │   LockGroup             │    │
│  │   锁管理器        │ │   命名锁工厂       │ │   锁组(批量锁)           │    │
│  └───────────────────┘ └───────────────────┘ └─────────────────────────┘    │
├─────────────────────────────────────────────────────────────────────────────┤
│                           监控 & 令牌                                        │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │  LockMetrics / DefaultLockMetrics / LockStats (指标统计)            │    │
│  │  FencingTokenGenerator (防脑裂令牌生成)                              │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────────────────────────────┤
│                      JDK 25 Virtual Threads / ReentrantLock                 │
└─────────────────────────────────────────────────────────────────────────────┘
```

### 1.3 核心特性

- **本地锁**：LocalLock（可重入锁）、LocalReadWriteLock（读写锁）、SpinLock（自旋锁）、SegmentLock（分段锁）
- **分布式锁**：DistributedLock 接口，通过 SPI 扩展支持 Redis、Zookeeper 等实现
- **锁组**：LockGroup 支持原子性批量获取多个锁，内置死锁预防
- **命名锁**：NamedLockFactory 支持按名称获取锁，支持 Striped Lock 模式
- **锁管理器**：LockManager 集中管理锁的创建与生命周期
- **锁指标**：DefaultLockMetrics 提供获取/释放/超时/竞争统计
- **Fencing Token**：FencingTokenGenerator 生成防脑裂令牌
- **LockGuard**：try-with-resources 自动释放锁

### 1.4 设计原则

| 原则 | 说明 |
|------|------|
| 统一抽象 | 本地锁和分布式锁使用统一的 `Lock<T>` 接口 |
| Virtual Thread 友好 | 使用 `ReentrantLock`，避免 `synchronized` pinning |
| 安全释放 | `LockGuard` + try-with-resources 自动释放锁 |
| 可观测 | 内置 `LockMetrics`，支持获取/等待/超时/竞争统计 |
| 可扩展 | SPI 支持自定义分布式锁实现 |
| 不可变配置 | `LockConfig` / `DistributedLockConfig` 使用 Record，不可变 |

### 1.5 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-lock</artifactId>
    <version>${version}</version>
</dependency>
```

**依赖关系：**
```
lock 模块依赖:
├── opencode-base-core (必需，基础工具)
└── opencode-base-id (可选，Fencing Token 生成)
```

---

## 2. 包结构

```
cloud.opencode.base.lock
├── Lock.java                           # 统一锁接口（泛型 Token）
├── ReadWriteLock.java                  # 读写锁接口
├── LockConfig.java                     # 锁配置 Record
├── LockGuard.java                      # 锁守卫 Record（自动释放）
├── LockType.java                       # 锁类型枚举
├── OpenLock.java                       # 统一门面入口
│
├── exception/                          # 异常
│   ├── OpenLockException.java          # 锁基础异常（含 LockErrorType 枚举）
│   ├── OpenLockTimeoutException.java   # 锁超时异常
│   └── OpenLockAcquireException.java   # 锁获取失败异常
│
├── local/                              # 本地锁实现
│   ├── LocalLock.java                  # 基于 ReentrantLock 的本地锁
│   ├── LocalReadWriteLock.java         # 基于 ReentrantReadWriteLock 的读写锁
│   ├── SpinLock.java                   # 自旋锁（短临界区优化）
│   └── SegmentLock.java               # 分段锁（细粒度锁）
│
├── distributed/                        # 分布式锁
│   ├── DistributedLock.java            # 分布式锁接口（扩展 Lock<String>）
│   └── DistributedLockConfig.java      # 分布式锁配置 Record
│
├── spi/                                # SPI 扩展
│   └── DistributedLockProvider.java    # 分布式锁提供者 SPI
│
├── manager/                            # 锁管理
│   ├── LockManager.java               # 锁管理器（集中创建管理）
│   ├── NamedLockFactory.java           # 命名锁工厂（Striped Lock）
│   └── LockGroup.java                 # 锁组（原子批量获取）
│
├── metrics/                            # 指标统计
│   ├── LockMetrics.java               # 锁指标接口
│   ├── DefaultLockMetrics.java        # 默认指标实现（Lock-Free）
│   └── LockStats.java                 # 锁统计快照 Record
│
└── token/                              # Fencing Token
    └── FencingTokenGenerator.java      # Fencing Token 生成器
```

---

## 3. 核心 API

### 3.1 OpenLock 门面

`OpenLock` 是锁组件的统一入口，提供所有类型锁的工厂方法。

```java
public final class OpenLock {

    // ==================== 本地锁 ====================

    /** 创建默认本地锁（非公平、可重入） */
    public static Lock<Long> lock();

    /** 使用自定义配置创建本地锁 */
    public static Lock<Long> lock(LockConfig config);

    /** 创建公平锁 */
    public static Lock<Long> fairLock();

    // ==================== 读写锁 ====================

    /** 创建默认读写锁 */
    public static ReadWriteLock<Long> readWriteLock();

    /** 使用自定义配置创建读写锁 */
    public static ReadWriteLock<Long> readWriteLock(LockConfig config);

    // ==================== 自旋锁 ====================

    /** 创建默认自旋锁 */
    public static Lock<Long> spinLock();

    /** 创建自定义自旋次数的自旋锁 */
    public static Lock<Long> spinLock(int maxSpinCount);

    // ==================== 分段锁 ====================

    /** 创建默认分段锁（16段） */
    public static <K> SegmentLock<K> segmentLock();

    /** 创建指定段数的分段锁 */
    public static <K> SegmentLock<K> segmentLock(int segments);

    // ==================== 命名锁 ====================

    /** 创建默认命名锁工厂 */
    public static NamedLockFactory namedLockFactory();

    /** 创建指定条纹数的命名锁工厂 */
    public static NamedLockFactory namedLockFactory(int stripes);

    // ==================== 锁组 ====================

    /** 获取锁组构建器 */
    public static LockGroup.Builder lockGroup();

    // ==================== 锁管理器 ====================

    /** 创建默认锁管理器 */
    public static LockManager manager();

    /** 使用自定义配置创建锁管理器 */
    public static LockManager manager(LockConfig config);

    // ==================== 便捷方法 ====================

    /** 使用临时锁执行操作 */
    public static void execute(Runnable action);

    /** 使用临时锁执行并返回结果 */
    public static <R> R executeWithResult(Supplier<R> supplier);

    // ==================== 配置 ====================

    /** 获取锁配置构建器 */
    public static LockConfig.Builder configBuilder();

    /** 获取默认锁配置 */
    public static LockConfig defaultConfig();

    /** 获取分布式锁配置构建器 */
    public static DistributedLockConfig.Builder distributedConfigBuilder();

    /** 获取默认分布式锁配置 */
    public static DistributedLockConfig defaultDistributedConfig();
}
```

**使用示例：**

```java
// 1. 快速使用默认锁
Lock<Long> lock = OpenLock.lock();
try (var guard = lock.lock()) {
    // 临界区
}

// 2. 使用 execute 便捷方法
OpenLock.execute(() -> {
    // 自动加锁、自动释放
    updateSharedResource();
});

// 3. 获取结果
String result = OpenLock.executeWithResult(() -> {
    return computeValue();
});

// 4. 创建读写锁
ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
String data = rwLock.executeRead(() -> loadData());
rwLock.executeWrite(() -> saveData(newData));

// 5. 创建自旋锁
Lock<Long> spinLock = OpenLock.spinLock();

// 6. 创建分段锁
SegmentLock<String> segmentLock = OpenLock.segmentLock(32);
segmentLock.execute("user:123", () -> processUser("123"));

// 7. 创建命名锁工厂
NamedLockFactory factory = OpenLock.namedLockFactory();
factory.execute("order:456", () -> processOrder("456"));
```

---

### 3.2 Lock<T> 接口

统一锁接口，本地锁和分布式锁的公共抽象。实现 `AutoCloseable`，支持 try-with-resources。

```java
public interface Lock<T> extends AutoCloseable {

    /** 获取锁，返回 LockGuard 用于自动释放 */
    LockGuard<T> lock();

    /** 带超时获取锁 */
    LockGuard<T> lock(Duration timeout);

    /** 尝试立即获取锁 */
    boolean tryLock();

    /** 带超时尝试获取锁 */
    boolean tryLock(Duration timeout);

    /** 可中断地获取锁 */
    LockGuard<T> lockInterruptibly() throws InterruptedException;

    /** 释放锁 */
    void unlock();

    /** 检查当前线程是否持有锁 */
    boolean isHeldByCurrentThread();

    /** 获取当前锁令牌 */
    Optional<T> getToken();

    /** 在锁保护下执行操作（default 方法） */
    default void execute(Runnable action);

    /** 带超时，在锁保护下执行操作（default 方法） */
    default void execute(Runnable action, Duration timeout);

    /** 在锁保护下执行并返回结果（default 方法） */
    default <R> R executeWithResult(Supplier<R> supplier);

    /** 带超时，在锁保护下执行并返回结果（default 方法） */
    default <R> R executeWithResult(Supplier<R> supplier, Duration timeout);

    /** AutoCloseable：若当前线程持有锁则释放（default 方法） */
    default void close();
}
```

**使用示例：**

```java
Lock<Long> lock = OpenLock.lock();

// 方式一：try-with-resources（推荐）
try (var guard = lock.lock()) {
    // 临界区，退出时自动释放
}

// 方式二：带超时
try (var guard = lock.lock(Duration.ofSeconds(5))) {
    // 临界区
}

// 方式三：tryLock 手动管理
if (lock.tryLock(Duration.ofSeconds(3))) {
    try {
        // 临界区
    } finally {
        lock.unlock();
    }
}

// 方式四：execute 便捷方法（最简洁）
lock.execute(() -> updateData());
String result = lock.executeWithResult(() -> computeData());
```

---

### 3.3 LockGuard<T>

锁守卫 Record，持有锁引用和令牌，实现 `AutoCloseable`，用于 try-with-resources 自动释放。

```java
public record LockGuard<T>(Lock<T> lock, T token) implements AutoCloseable {

    /** 关闭时自动释放锁 */
    @Override
    public void close();
}
```

---

### 3.4 ReadWriteLock<T> 接口

读写锁接口，支持读共享、写独占。

```java
public interface ReadWriteLock<T> {

    /** 获取读锁 */
    Lock<T> readLock();

    /** 获取写锁 */
    Lock<T> writeLock();

    /** 在读锁保护下执行操作（default 方法） */
    default void executeRead(Runnable action);

    /** 在读锁保护下执行并返回结果（default 方法） */
    default <R> R executeRead(Supplier<R> supplier);

    /** 在写锁保护下执行操作（default 方法） */
    default void executeWrite(Runnable action);

    /** 在写锁保护下执行并返回结果（default 方法） */
    default <R> R executeWrite(Supplier<R> supplier);
}
```

**使用示例：**

```java
ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();

// 读操作（多线程可并发读）
String data = rwLock.executeRead(() -> loadData());

// 写操作（独占）
rwLock.executeWrite(() -> saveData(newData));

// 也可以直接获取底层锁
Lock<Long> readLock = rwLock.readLock();
Lock<Long> writeLock = rwLock.writeLock();
```

---

### 3.5 LockConfig

锁配置 Record，不可变，通过 Builder 构建。

```java
public record LockConfig(
    Duration defaultTimeout,    // 默认超时时间
    boolean fair,               // 是否公平锁
    boolean reentrant,          // 是否可重入
    int spinCount,              // 自旋次数（SpinLock 专用）
    boolean enableMetrics,      // 是否启用指标
    LockType lockType           // 锁类型
) {
    /** 获取 Builder */
    public static Builder builder();

    /** 获取默认配置 */
    public static LockConfig defaults();

    public static class Builder {
        public Builder timeout(Duration timeout);
        public Builder fair(boolean fair);
        public Builder reentrant(boolean reentrant);
        public Builder spinCount(int spinCount);
        public Builder enableMetrics(boolean enable);
        public Builder lockType(LockType type);
        public LockConfig build();
    }
}
```

**使用示例：**

```java
// 默认配置
LockConfig config = LockConfig.defaults();

// 自定义配置
LockConfig customConfig = LockConfig.builder()
    .timeout(Duration.ofSeconds(10))
    .fair(true)
    .reentrant(true)
    .enableMetrics(true)
    .build();

Lock<Long> lock = OpenLock.lock(customConfig);
```

---

### 3.6 LockType 枚举

```java
public enum LockType {
    REENTRANT,      // 可重入锁
    READ_WRITE,     // 读写锁
    STAMPED,        // 乐观读锁
    SPIN,           // 自旋锁
    SEGMENT         // 分段锁
}
```

---

## 4. 本地锁实现

### 4.1 LocalLock

基于 JDK `ReentrantLock` 的本地锁实现，Virtual Thread 友好。支持公平/非公平模式，内置指标统计。

```java
public class LocalLock implements Lock<Long> {

    /** 创建默认 LocalLock（非公平、可重入） */
    public LocalLock();

    /** 使用自定义配置创建 LocalLock */
    public LocalLock(LockConfig config);

    // --- Lock<Long> 接口实现 ---
    public LockGuard<Long> lock();
    public LockGuard<Long> lock(Duration timeout);
    public boolean tryLock();
    public boolean tryLock(Duration timeout);
    public LockGuard<Long> lockInterruptibly() throws InterruptedException;
    public void unlock();
    public boolean isHeldByCurrentThread();
    public Optional<Long> getToken();

    // --- 扩展方法 ---
    /** 获取当前线程的锁持有计数 */
    public int getHoldCount();

    /** 是否为公平锁 */
    public boolean isFair();

    /** 是否有线程在排队等待锁 */
    public boolean hasQueuedThreads();

    /** 获取等待锁的线程数 */
    public int getQueueLength();

    /** 获取锁指标（若启用 enableMetrics） */
    public Optional<LockMetrics> getMetrics();
}
```

**使用示例：**

```java
// 默认锁
LocalLock lock = new LocalLock();
try (var guard = lock.lock()) {
    // 临界区
}

// 公平锁 + 指标
LocalLock fairLock = new LocalLock(LockConfig.builder()
    .fair(true)
    .enableMetrics(true)
    .build());

fairLock.execute(() -> doWork());

// 查看指标
fairLock.getMetrics().ifPresent(metrics -> {
    LockStats stats = metrics.snapshot();
    System.out.println("竞争率: " + stats.getContentionRate());
});
```

---

### 4.2 LocalReadWriteLock

基于 JDK `ReentrantReadWriteLock` 的读写锁实现，支持读共享、写独占。

```java
public class LocalReadWriteLock implements ReadWriteLock<Long> {

    /** 创建默认读写锁 */
    public LocalReadWriteLock();

    /** 使用自定义配置创建读写锁 */
    public LocalReadWriteLock(LockConfig config);

    // --- ReadWriteLock<Long> 接口实现 ---
    public Lock<Long> readLock();
    public Lock<Long> writeLock();

    // --- 扩展方法 ---
    /** 获取读锁持有计数 */
    public int getReadLockCount();

    /** 获取当前线程的读锁持有计数 */
    public int getReadHoldCount();

    /** 写锁是否被持有 */
    public boolean isWriteLocked();

    /** 当前线程是否持有写锁 */
    public boolean isWriteLockedByCurrentThread();

    /** 获取写锁持有计数 */
    public int getWriteHoldCount();
}
```

**使用示例：**

```java
LocalReadWriteLock rwLock = new LocalReadWriteLock();

// 读操作
String data = rwLock.executeRead(() -> loadData());

// 写操作
rwLock.executeWrite(() -> saveData(newData));

// 手动使用读/写锁
try (var guard = rwLock.readLock().lock()) {
    // 读临界区
}

// 公平读写锁
LocalReadWriteLock fairRwLock = new LocalReadWriteLock(
    LockConfig.builder().fair(true).build());
```

---

### 4.3 SpinLock

自旋锁实现，适用于锁持有时间非常短的高并发场景。超过自旋次数后 yield 让出 CPU。

```java
public class SpinLock implements Lock<Long> {

    /** 创建默认自旋锁 */
    public SpinLock();

    /** 使用自定义配置创建自旋锁 */
    public SpinLock(LockConfig config);

    // --- Lock<Long> 接口实现 ---
    public LockGuard<Long> lock();
    public LockGuard<Long> lock(Duration timeout);
    public boolean tryLock();
    public boolean tryLock(Duration timeout);
    public LockGuard<Long> lockInterruptibly() throws InterruptedException;
    public void unlock();
    public boolean isHeldByCurrentThread();
    public Optional<Long> getToken();

    // --- 扩展方法 ---
    /** 获取当前线程的锁持有计数 */
    public int getHoldCount();
}
```

**使用示例：**

```java
// 默认自旋锁
SpinLock lock = new SpinLock();
lock.execute(() -> {
    // 极短临界区操作
    counter.incrementAndGet();
});

// 自定义自旋次数
SpinLock customLock = new SpinLock(
    LockConfig.builder()
        .spinCount(2000)
        .reentrant(true)
        .build());
```

---

### 4.4 SegmentLock<K>

分段锁实现，将 Key 通过一致性哈希映射到不同的锁段，允许对不同 Key 的操作并发执行。

```java
public class SegmentLock<K> {

    /** 创建默认分段锁（16段） */
    public SegmentLock();

    /** 创建指定段数的分段锁（自动向上取 2 的幂次） */
    public SegmentLock(int segments);

    /** 创建指定段数和配置的分段锁 */
    public SegmentLock(int segments, LockConfig config);

    /** 获取指定 Key 对应的锁 */
    public Lock<Long> getLock(K key);

    /** 在指定 Key 的锁保护下执行操作 */
    public void execute(K key, Runnable action);

    /** 在指定 Key 的锁保护下执行并返回结果 */
    public <R> R executeWithResult(K key, Supplier<R> supplier);

    /** 获取总段数 */
    public int getSegments();

    /** 获取指定 Key 的段索引 */
    public int getSegmentIndex(K key);
}
```

**使用示例：**

```java
SegmentLock<String> lock = new SegmentLock<>(32);

// 不同 Key 可以并发执行
lock.execute("user:123", () -> processUser("123"));
lock.execute("user:456", () -> processUser("456")); // 不阻塞

// 获取结果
User user = lock.executeWithResult("user:123", () -> loadUser("123"));

// 获取底层锁
Lock<Long> userLock = lock.getLock("user:123");
```

---

## 5. 分布式锁

### 5.1 DistributedLock 接口

分布式锁接口，扩展 `Lock<String>`，增加 TTL 和自动续期支持。

```java
public interface DistributedLock extends Lock<String> {

    // 继承 Lock<String> 的所有方法，Token 类型为 String

    // 分布式锁特有功能通过 DistributedLockConfig 配置：
    // - TTL（锁过期时间）
    // - 自动续期
    // - Fencing Token
    // - 重试策略
}
```

### 5.2 DistributedLockConfig

分布式锁配置 Record。

```java
public record DistributedLockConfig(
    Duration lockTimeout,       // 获取锁超时时间
    Duration leaseTime,         // 锁租约时间（TTL）
    Duration renewInterval,     // 续期间隔
    boolean autoRenew,          // 是否自动续期
    int retryCount,             // 重试次数
    Duration retryInterval,     // 重试间隔
    boolean enableFencing       // 是否启用 Fencing Token
) {
    public static Builder builder();
    public static DistributedLockConfig defaults();

    public static class Builder {
        public Builder lockTimeout(Duration timeout);
        public Builder leaseTime(Duration duration);
        public Builder renewInterval(Duration interval);
        public Builder autoRenew(boolean autoRenew);
        public Builder retryCount(int count);
        public Builder retryInterval(Duration interval);
        public Builder enableFencing(boolean enable);
        public DistributedLockConfig build();
    }
}
```

**使用示例：**

```java
DistributedLockConfig config = DistributedLockConfig.builder()
    .lockTimeout(Duration.ofSeconds(10))
    .leaseTime(Duration.ofSeconds(30))
    .autoRenew(true)
    .renewInterval(Duration.ofSeconds(10))
    .enableFencing(true)
    .build();
```

### 5.3 DistributedLockProvider SPI

```java
public interface DistributedLockProvider {
    // 通过 Java SPI 机制提供分布式锁实现
    // 用户可实现此接口来对接 Redis、Zookeeper、etcd 等后端
}
```

---

## 6. 锁管理

### 6.1 LockManager

集中管理锁的创建和生命周期，实现 `AutoCloseable`。

```java
public class LockManager implements AutoCloseable {

    /** 创建默认锁管理器 */
    public LockManager();

    /** 使用自定义配置创建锁管理器 */
    public LockManager(LockConfig localConfig);

    /** 获取或创建命名本地锁 */
    public Lock<Long> getLocalLock(String name);

    /** 获取或创建命名读写锁 */
    public ReadWriteLock<Long> getLocalReadWriteLock(String name);

    /** 使用命名锁执行操作 */
    public void executeWithLocalLock(String name, Runnable action);

    /** 检查是否已创建指定名称的锁 */
    public boolean hasLock(String name);

    /** 移除指定名称的锁 */
    public boolean removeLock(String name);

    /** 获取所有已管理的锁名称 */
    public Set<String> getManagedLockNames();

    /** 获取已管理的锁数量 */
    public int getManagedLockCount();

    /** 关闭管理器 */
    public void close();
}
```

**使用示例：**

```java
try (LockManager manager = OpenLock.manager()) {
    // 按名称获取锁（相同名称返回同一锁实例）
    Lock<Long> lock = manager.getLocalLock("user:123");
    lock.execute(() -> processUser("123"));

    // 读写锁
    ReadWriteLock<Long> rwLock = manager.getLocalReadWriteLock("config");
    String config = rwLock.executeRead(() -> loadConfig());

    // 便捷方法
    manager.executeWithLocalLock("order:789", () -> processOrder("789"));

    // 查询状态
    System.out.println("管理锁数量: " + manager.getManagedLockCount());
}
```

---

### 6.2 NamedLockFactory

命名锁工厂，支持 Striped Lock 模式。Striped 模式将名称映射到有限数量的锁上，减少内存占用。

```java
public class NamedLockFactory {

    /** 创建默认命名锁工厂（Striped，32条纹） */
    public NamedLockFactory();

    /** 创建指定条纹数的命名锁工厂 */
    public NamedLockFactory(int stripes);

    /** 创建自定义配置的命名锁工厂 */
    public NamedLockFactory(int stripes, boolean useStriping, LockConfig config);

    /** 获取指定名称的锁 */
    public Lock<Long> getLock(String name);

    /** 在指定名称的锁保护下执行操作 */
    public void execute(String name, Runnable action);

    /** 在指定名称的锁保护下执行并返回结果 */
    public <R> R executeWithResult(String name, Supplier<R> supplier);

    /** 获取条纹数量 */
    public int getStripes();

    /** 获取已创建的命名锁数量 */
    public int getNamedLockCount();

    /** 是否启用 Striped 模式 */
    public boolean isStripingEnabled();
}
```

**使用示例：**

```java
// Striped 模式（推荐，有限锁数量）
NamedLockFactory factory = new NamedLockFactory(64);
factory.execute("order:12345", () -> processOrder("12345"));

Order order = factory.executeWithResult("order:12345", () -> loadOrder("12345"));

// 非 Striped 模式（每个名称一把锁）
NamedLockFactory unlimited = new NamedLockFactory(0, false, LockConfig.defaults());
```

---

### 6.3 LockGroup

锁组，支持原子性批量获取多个锁，内置死锁预防（按固定顺序获取）。

```java
public class LockGroup implements AutoCloseable {

    /** 获取构建器 */
    public static Builder builder();

    /** 原子获取所有锁，返回 LockGroupGuard */
    public LockGroupGuard lockAll();

    /** 尝试获取所有锁（不等待），返回是否全部成功 */
    public boolean tryLockAll();

    /** 带超时尝试获取所有锁 */
    public boolean tryLockAll(Duration timeout);

    /** 释放所有已获取的锁 */
    public void releaseAll();

    /** 获取锁组中的锁数量 */
    public int size();

    /** 获取已成功获取的锁数量 */
    public int acquiredCount();

    /** 关闭锁组（释放所有锁） */
    public void close();

    public static class Builder {
        /** 添加锁 */
        public Builder add(Lock<?> lock);

        /** 批量添加锁 */
        public Builder addAll(Collection<? extends Lock<?>> locks);

        /** 设置超时 */
        public Builder timeout(Duration timeout);

        /** 构建锁组 */
        public LockGroup build();
    }

    /** 锁组守卫，AutoCloseable */
    public record LockGroupGuard(LockGroup group) implements AutoCloseable {
        public void close();
    }
}
```

**使用示例：**

```java
Lock<Long> lockA = OpenLock.lock();
Lock<Long> lockB = OpenLock.lock();
Lock<Long> lockC = OpenLock.lock();

// 原子获取所有锁（死锁预防）
LockGroup group = LockGroup.builder()
    .add(lockA)
    .add(lockB)
    .add(lockC)
    .timeout(Duration.ofSeconds(5))
    .build();

try (var guard = group.lockAll()) {
    // 所有锁都已获取
    transferFunds(accountA, accountB, amount);
}
// 自动释放所有锁

// tryLockAll
if (group.tryLockAll(Duration.ofSeconds(3))) {
    try {
        // 所有锁都已获取
    } finally {
        group.releaseAll();
    }
}
```

---

## 7. 锁指标

### 7.1 LockMetrics 接口

```java
public interface LockMetrics {
    void recordAcquire(Duration waitTime);
    void recordRelease();
    void recordTimeout();
    long getAcquireCount();
    long getReleaseCount();
    long getTimeoutCount();
    long getContentionCount();
    Duration getAverageWaitTime();
    Duration getMaxWaitTime();
    int getCurrentHoldCount();
    LockStats snapshot();
    void reset();
}
```

### 7.2 DefaultLockMetrics

默认锁指标实现，使用 Lock-Free 原子操作记录统计数据。

```java
public class DefaultLockMetrics implements LockMetrics {

    /** 记录一次成功获取锁 */
    public void recordAcquire(Duration waitTime);

    /** 记录一次释放锁 */
    public void recordRelease();

    /** 记录一次超时 */
    public void recordTimeout();

    /** 获取成功获取锁次数 */
    public long getAcquireCount();

    /** 获取释放锁次数 */
    public long getReleaseCount();

    /** 获取超时次数 */
    public long getTimeoutCount();

    /** 获取竞争次数 */
    public long getContentionCount();

    /** 获取平均等待时间 */
    public Duration getAverageWaitTime();

    /** 获取最大等待时间 */
    public Duration getMaxWaitTime();

    /** 获取当前持有计数 */
    public int getCurrentHoldCount();

    /** 获取统计快照 */
    public LockStats snapshot();

    /** 重置所有指标 */
    public void reset();
}
```

### 7.3 LockStats

锁统计快照 Record。

```java
public record LockStats(
    long acquireCount,          // 获取锁次数
    long releaseCount,          // 释放锁次数
    long timeoutCount,          // 超时次数
    long contentionCount,       // 竞争次数
    Duration averageWaitTime,   // 平均等待时间
    Duration maxWaitTime,       // 最大等待时间
    int currentHoldCount,       // 当前持有计数
    java.time.Instant timestamp // 快照时间戳
) {
    /** 获取成功率 */
    public double getSuccessRate();

    /** 获取竞争率 */
    public double getContentionRate();

    /** 获取超时率 */
    public double getTimeoutRate();
}
```

**使用示例：**

```java
LocalLock lock = new LocalLock(LockConfig.builder()
    .enableMetrics(true)
    .build());

// 执行一些操作...
lock.execute(() -> doWork());

// 获取指标
LockMetrics metrics = lock.getMetrics().orElseThrow();
LockStats stats = metrics.snapshot();

System.out.println("获取锁次数: " + stats.acquireCount());
System.out.println("竞争率: " + stats.getContentionRate());
System.out.println("平均等待时间: " + stats.averageWaitTime());
System.out.println("最大等待时间: " + stats.maxWaitTime());
System.out.println("超时率: " + stats.getTimeoutRate());
```

---

## 8. Fencing Token

### 8.1 FencingTokenGenerator

Fencing Token 生成器，防止分布式锁过期后的脑裂问题。如果 `opencode-base-id` 模块可用，则委托 OpenId 生成更高质量的令牌。

```java
public final class FencingTokenGenerator {

    /** 检查 ID 模块是否可用 */
    public static boolean isIdModuleAvailable();

    /** 生成字符串形式的 Fencing Token */
    public static String generateStringToken();

    /** 生成 long 形式的 Fencing Token */
    public static long generateLongToken();

    /** 生成带前缀的 Fencing Token */
    public static String generatePrefixedToken(String prefix);
}
```

**使用示例：**

```java
// 生成 Fencing Token
String token = FencingTokenGenerator.generateStringToken();
long numericToken = FencingTokenGenerator.generateLongToken();
String prefixed = FencingTokenGenerator.generatePrefixedToken("order");

// 检查 ID 模块
if (FencingTokenGenerator.isIdModuleAvailable()) {
    // 使用 ID 模块生成高质量令牌
}
```

---

## 9. 异常体系

### 9.1 异常层次

```
OpenException (core)
└── OpenLockException               # 锁基础异常
    ├── OpenLockTimeoutException     # 锁超时异常
    └── OpenLockAcquireException     # 锁获取失败异常
```

### 9.2 OpenLockException

```java
public class OpenLockException extends OpenException {

    public OpenLockException(String message);
    public OpenLockException(String message, Throwable cause);
    public OpenLockException(String message, String lockName, LockErrorType errorType);
    public OpenLockException(String message, String lockName, LockErrorType errorType, Throwable cause);

    /** 获取锁名称 */
    public String lockName();

    /** 获取错误类型 */
    public LockErrorType errorType();

    /** 锁错误类型枚举 */
    public enum LockErrorType {
        ACQUIRE_FAILED,     // 获取失败
        TIMEOUT,            // 超时
        INTERRUPTED,        // 中断
        DEADLOCK,           // 死锁
        INVALID_STATE,      // 无效状态
        RELEASE_FAILED,     // 释放失败
        UNKNOWN             // 未知
    }
}
```

### 9.3 OpenLockTimeoutException

```java
public class OpenLockTimeoutException extends OpenLockException {

    public OpenLockTimeoutException(String message);
    public OpenLockTimeoutException(String message, Duration waitTime);
    public OpenLockTimeoutException(String message, String lockName, Duration waitTime);

    /** 获取等待时间 */
    public Duration waitTime();
}
```

### 9.4 OpenLockAcquireException

```java
public class OpenLockAcquireException extends OpenLockException {

    public OpenLockAcquireException(String message);
    public OpenLockAcquireException(String message, Throwable cause);
    public OpenLockAcquireException(String message, String lockName, Throwable cause);
}
```

---

## 10. 使用场景

### 10.1 并发计数器

```java
Lock<Long> lock = OpenLock.lock();
AtomicInteger counter = new AtomicInteger(0);

// 多线程安全递增
lock.execute(() -> {
    int current = counter.get();
    counter.set(current + 1);
});
```

### 10.2 缓存更新

```java
ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
Map<String, Object> cache = new HashMap<>();

// 读缓存（多线程并发读）
Object value = rwLock.executeRead(() -> cache.get("key"));

// 写缓存（独占）
rwLock.executeWrite(() -> cache.put("key", computeValue()));
```

### 10.3 分段锁处理用户请求

```java
SegmentLock<String> userLock = OpenLock.segmentLock(64);

// 不同用户的请求可以并发处理
void handleRequest(String userId) {
    userLock.execute(userId, () -> {
        User user = loadUser(userId);
        processUser(user);
        saveUser(user);
    });
}
```

### 10.4 多资源转账（锁组）

```java
void transfer(String fromAccount, String toAccount, BigDecimal amount) {
    NamedLockFactory factory = OpenLock.namedLockFactory();
    Lock<Long> fromLock = factory.getLock(fromAccount);
    Lock<Long> toLock = factory.getLock(toAccount);

    try (var guard = LockGroup.builder()
            .add(fromLock)
            .add(toLock)
            .timeout(Duration.ofSeconds(5))
            .build()
            .lockAll()) {
        // 两把锁都已获取，安全执行转账
        debit(fromAccount, amount);
        credit(toAccount, amount);
    }
}
```

### 10.5 锁指标监控

```java
LockManager manager = OpenLock.manager(LockConfig.builder()
    .enableMetrics(true)
    .build());

Lock<Long> lock = manager.getLocalLock("critical-section");

// 周期性采集指标
((LocalLock) lock).getMetrics().ifPresent(metrics -> {
    LockStats stats = metrics.snapshot();
    reportToMonitoring("lock.acquire_count", stats.acquireCount());
    reportToMonitoring("lock.contention_rate", stats.getContentionRate());
    reportToMonitoring("lock.timeout_rate", stats.getTimeoutRate());
    reportToMonitoring("lock.avg_wait_ms", stats.averageWaitTime().toMillis());
});
```

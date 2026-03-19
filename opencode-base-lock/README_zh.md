# OpenCode Base Lock

面向 JDK 25+ 的统一锁抽象，支持本地锁和分布式锁。提供可重入锁、读写锁、自旋锁、分段锁、命名锁、带死锁预防的锁组以及分布式锁 SPI。

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-lock</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 功能特性

- 统一的 `Lock<T>` 接口，同时适用于本地锁和分布式锁
- 通过 `LockGuard` 支持 try-with-resources 自动释放
- 本地可重入锁，支持公平/非公平模式
- 读写锁，支持并发读取和独占写入
- 自旋锁，适用于极短临界区
- 分段锁，基于键的细粒度锁定
- 命名锁工厂，带条纹的字符串键锁定
- 锁组，通过一致排序预防死锁
- 锁管理器，集中管理锁生命周期
- 分布式锁 SPI，支持可插拔后端（Redis、ZooKeeper 等）
- 护栏令牌生成，保障分布式锁安全
- 锁指标和统计数据收集
- 可配置超时、公平性和自旋次数
- 线程安全，虚拟线程友好
- 便捷的 `execute()` 和 `executeWithResult()` 方法

## 类参考

### 核心类

| 类 | 说明 |
|---|------|
| `OpenLock` | 创建和管理各类锁的主门面类 |
| `Lock<T>` | 统一锁接口，支持 try-with-resources、超时、可中断和执行模式 |
| `ReadWriteLock<T>` | 读写锁接口，允许并发读取和独占写入 |
| `LockGuard<T>` | 可自动关闭的锁守卫，用于 try-with-resources 模式 |
| `LockConfig` | 锁配置：超时、公平性、自旋次数及其他选项 |
| `LockType` | 锁类型枚举：REENTRANT、READ_WRITE、SPIN、SEGMENT |

### 本地锁

| 类 | 说明 |
|---|------|
| `LocalLock` | 基于 `ReentrantLock` 的本地可重入锁实现 |
| `LocalReadWriteLock` | 基于 `ReentrantReadWriteLock` 的本地读写锁实现 |
| `SegmentLock<K>` | 将键映射到独立锁段以减少争用的细粒度锁 |
| `SpinLock` | 适用于极短临界区（纳秒级）的自旋锁 |

### 分布式锁

| 类 | 说明 |
|---|------|
| `DistributedLock` | 分布式锁实现的抽象基类 |
| `DistributedLockConfig` | 分布式锁配置：租约时间、重试、心跳 |

### 管理器

| 类 | 说明 |
|---|------|
| `LockManager` | 集中创建和跟踪锁实例的锁管理器 |
| `LockGroup` | 原子多锁获取，通过一致排序预防死锁 |
| `NamedLockFactory` | 按名称创建/复用锁的工厂，支持可配置的条纹数 |

### 指标

| 类 | 说明 |
|---|------|
| `LockMetrics` | 锁指标收集接口 |
| `DefaultLockMetrics` | 锁指标的默认实现，含计数器和计时 |
| `LockStats` | 锁统计记录：获取次数、争用次数、等待时间 |

### 令牌

| 类 | 说明 |
|---|------|
| `FencingTokenGenerator` | 生成单调递增的护栏令牌，保障分布式锁安全 |

### SPI

| 类 | 说明 |
|---|------|
| `DistributedLockProvider` | 可插拔分布式锁后端的 SPI 接口 |

### 异常

| 类 | 说明 |
|---|------|
| `OpenLockException` | 锁操作的基础异常 |
| `OpenLockAcquireException` | 锁获取失败时抛出 |
| `OpenLockTimeoutException` | 锁获取超时时抛出 |

## 快速开始

```java
// 创建和使用本地锁
Lock<Long> lock = OpenLock.lock();
lock.execute(() -> {
    // 临界区
    updateSharedState();
});

// 带返回值的锁
String result = lock.executeWithResult(() -> readSharedState());

// try-with-resources
try (var guard = lock.lock()) {
    // 离开代码块时自动释放
    doWork();
}

// 带超时的锁
try (var guard = lock.lock(Duration.ofSeconds(5))) {
    doWork();
}

// 公平锁（FIFO 顺序）
Lock<Long> fairLock = OpenLock.fairLock();

// 读写锁
ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
String data = rwLock.executeRead(() -> loadData());
rwLock.executeWrite(() -> saveData(newData));

// 短临界区自旋锁
Lock<Long> spinLock = OpenLock.spinLock();

// 基于键的分段锁
SegmentLock<String> segmentLock = OpenLock.segmentLock(32);
segmentLock.execute("user:123", () -> updateUser("123"));

// 命名锁工厂
NamedLockFactory factory = OpenLock.namedLockFactory();
factory.execute("order:12345", () -> processOrder("12345"));

// 锁组（死锁预防）
try (var guard = OpenLock.lockGroup()
        .add(lockA).add(lockB).add(lockC)
        .timeout(Duration.ofSeconds(10))
        .build().lockAll()) {
    transferFunds(accountA, accountB, accountC);
}

// 锁管理器
LockManager manager = OpenLock.manager();

// 自定义配置
Lock<Long> customLock = OpenLock.lock(
    LockConfig.builder()
        .fair(true)
        .timeout(Duration.ofSeconds(30))
        .build()
);
```

## 环境要求

- Java 25+

## 开源协议

Apache License 2.0

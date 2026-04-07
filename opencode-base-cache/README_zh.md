# OpenCode Base Cache

高性能本地缓存库，支持 LRU/LFU/W-TinyLFU 淘汰策略，适用于 JDK 25+。

## 功能特性

### 核心功能
- **多种淘汰策略**：LRU、LFU、FIFO、W-TinyLFU 及组合策略
- **灵活的过期机制**：TTL、TTI 及自定义过期策略
- **异步 API**：基于 CompletableFuture 的完整异步支持
- **统计指标**：全面的缓存统计和度量
- **虚拟线程**：原生 JDK 25 虚拟线程支持

### 高级功能
- **缓存保护**：BloomFilter 和 SingleFlight 集成（ProtectedCache）
- **预刷新**：过期前主动后台刷新（RefreshAheadCache）
- **读时复制**：线程安全的深拷贝装饰器（CopyOnReadCache）
- **缓存预热**：支持优先级的并行预热（CacheWarmerManager）
- **响应式 API**：JDK Flow API 和 Project Reactor 支持（ReactiveCache）
- **弹性加载**：重试、熔断器、隔离舱和超时（ResilientCacheLoader）
- **采样统计**：高吞吐量概率性统计（SamplingStatsCounter）
- **标签批量失效**：按标签批量失效缓存条目（TaggedCache）
- **条件原子操作**：Cache 接口 replaceIf / computeIfMatch CAS 方法
- **缓存快照持久化**：将缓存内容保存到磁盘并恢复（CacheSnapshot）
- **无锁读路径**：tryLock 降级，高并发读不再被淘汰追踪阻塞
- **多级缓存**：多层缓存组合（MultiLevelCache）
- **分布式缓存**：分布式缓存接口及配置
- **压缩缓存**：值压缩装饰器（CompressedCache）
- **写穿/写后**：WriteThroughCache 和 WriteBehindCache
- **租户缓存**：多租户隔离的缓存（TenantCache）
- **缓存事件**：事件监听和分发机制
- **JMX 支持**：JMX MBean 注册和监控
- **Spring 集成**：Spring Boot 自动配置和健康检查

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cache</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API 概览

### 核心接口与门面

| 类名 | 说明 |
|------|------|
| `Cache` | 核心缓存接口 |
| `OpenCache` | 缓存门面和工厂 |
| `AsyncCache` | 异步缓存接口 |
| `LoadingCache` | 自动加载缓存接口 |
| `CacheManager` | 缓存生命周期管理 |
| `CacheStats` | 缓存统计接口 |
| `CacheMetrics` | 缓存度量指标 |

### 缓存装饰器

| 类名 | 说明 |
|------|------|
| `CacheDecorators` | 缓存装饰器工厂 |
| `CopyOnReadCache` | 读时深拷贝装饰器 |
| `ProtectedCache` | BloomFilter + SingleFlight 保护缓存 |
| `RefreshAheadCache` | 预刷新缓存 |
| `ReadThroughCache` | 读穿缓存 |
| `WriteThroughCache` | 写穿缓存 |
| `WriteBehindCache` | 异步写后缓存 |
| `NullSafeCache` | 空值安全缓存 |
| `TimeoutCache` | 操作超时包装缓存 |
| `ReferenceCache` | 引用类型缓存（软引用/弱引用） |
| `LayeredCache` | 分层缓存 |
| `TenantCache` | 多租户缓存 |
| `VariableTtlCache` | 可变 TTL 缓存 |
| `CompressedCache` | 压缩值缓存 |
| `MultiLevelCache` | 多级缓存 |
| `TaggedCache` | 标签批量失效装饰器 |

### 配置

| 类名 | 说明 |
|------|------|
| `CacheConfig` | 缓存配置构建器 |
| `CacheSpec` | 缓存规格定义 |

### 淘汰策略

| 类名 | 说明 |
|------|------|
| `EvictionPolicy` | 淘汰策略接口 |
| `LruEvictionPolicy` | LRU（最近最少使用）淘汰策略 |
| `LfuEvictionPolicy` | LFU（最不经常使用）淘汰策略 |
| `FifoEvictionPolicy` | FIFO（先进先出）淘汰策略 |
| `WTinyLfuEvictionPolicy` | W-TinyLFU 淘汰策略 |

### 过期策略

| 类名 | 说明 |
|------|------|
| `ExpiryPolicy` | 过期策略接口 |
| `TtlExpiryPolicy` | 写入后过期（TTL）策略 |
| `TtiExpiryPolicy` | 访问后过期（TTI）策略 |
| `CombinedExpiryPolicy` | 组合过期策略 |
| `TtlPolicy` | TTL 策略接口 |
| `TtlDecayPolicy` | TTL 衰减策略 |

### 保护

| 类名 | 说明 |
|------|------|
| `BloomFilter` | 布隆过滤器，用于缓存穿透防护 |
| `SingleFlight` | 请求合并，防止缓存击穿 |
| `CircuitBreaker` | 熔断器 |
| `Bulkhead` | 隔离舱（信号量/线程池） |
| `BackoffStrategy` | 退避策略枚举 |
| `RetryBudget` | 重试预算 |
| `TtlJitter` | TTL 抖动，防止缓存雪崩 |

### 弹性

| 类名 | 说明 |
|------|------|
| `ResilientCacheLoader` | 弹性缓存加载器（重试、熔断、隔离、超时） |
| `RetryExecutor` | 重试执行器 |
| `GracefulDegradation` | 优雅降级 |

### 事件

| 类名 | 说明 |
|------|------|
| `CacheEvent` | 缓存事件记录 |
| `CacheEventListener` | 缓存事件监听器接口 |
| `CacheEventDispatcher` | 缓存事件分发器 |

### SPI 接口

| 类名 | 说明 |
|------|------|
| `CacheLoader` | 同步缓存加载器 |
| `AsyncCacheLoader` | 异步缓存加载器 |
| `CacheSerializer` | 缓存序列化器 |
| `CacheWarmer`（SPI） | 缓存预热器接口 |
| `RefreshAheadPolicy` | 预刷新策略接口 |
| `RemovalListener` | 移除事件监听器 |
| `RetryPolicy` | 重试策略密封接口 |
| `StatsCounter` | 统计计数器接口 |
| `ValueWeigher` | 值权重计算器 |

### 统计

| 类名 | 说明 |
|------|------|
| `LongAdderStatsCounter` | 基于 LongAdder 的统计计数器 |
| `SamplingStatsCounter` | 高吞吐量采样统计计数器 |

### 分布式

| 类名 | 说明 |
|------|------|
| `DistributedCache` | 分布式缓存接口 |
| `DistributedCacheConfig` | 分布式缓存配置记录 |
| `DistributedCacheStats` | 分布式缓存统计记录 |

### 其他

| 类名 | 说明 |
|------|------|
| `CacheEntry` | 缓存条目记录 |
| `RemovalCause` | 移除原因枚举 |
| `CacheQuery` | 缓存查询工具 |
| `ReactiveCache` | 响应式缓存 API |
| `BulkOperations` | 批量操作工具 |
| `AccessPatternAnalyzer` | 访问模式分析器 |
| `WriteCoalescer` | 写合并器 |
| `DeadLetterQueue` | 死信队列 |
| `CacheUtil` | 缓存工具类 |
| `CacheWarmer`（warming） | 缓存预热器 |
| `CacheWarmerManager` | 缓存预热管理器 |
| `CacheTestSupport` | 缓存测试支持 |
| `OpenCacheException` | 缓存异常 |
| `CacheSnapshot` | 缓存快照持久化工具 |

### Cache 接口新增 CAS 方法

| 方法 | 说明 |
|------|------|
| `replaceIf(key, predicate, newValue)` | 条件满足时替换值 |
| `computeIfMatch(key, predicate, remapper)` | 条件满足时计算新值 |

### TaggedCache 方法

| 方法 | 说明 |
|------|------|
| `TaggedCache.wrap(cache)` | 包装缓存为标签缓存 |
| `put(key, value, tags...)` | 写入值并关联标签 |
| `putWithTtl(key, value, ttl, tags...)` | 写入值并关联标签，指定 TTL |
| `addTags(key, tags...)` | 为已有 key 添加标签 |
| `getKeysByTag(tag)` | 获取标签关联的所有 key |
| `getTags(key)` | 获取 key 关联的所有标签 |
| `invalidateByTag(tag)` | 按标签批量失效 |
| `invalidateByTags(tags...)` | 按多个标签批量失效 |
| `getAllTags()` | 获取所有标签 |
| `getTagSize(tag)` | 获取标签关联的 key 数量 |

### CacheSnapshot 方法

| 方法 | 说明 |
|------|------|
| `save(cache, path, keySerializer, valueSerializer)` | 保存缓存到磁盘（自定义序列化） |
| `restore(path, cache, keyDeserializer, valueDeserializer)` | 从磁盘恢复缓存（自定义反序列化） |
| `saveStringCache(cache, path)` | 保存 String 缓存到磁盘 |
| `restoreStringCache(path, cache)` | 从磁盘恢复 String 缓存 |

### Spring 集成

| 类名 | 说明 |
|------|------|
| `CacheAutoConfiguration` | Spring Boot 自动配置 |
| `CacheProperties` | Spring 缓存配置属性 |
| `OpenCodeCacheManager` | Spring CacheManager 实现 |
| `CacheHealthIndicator` | Spring Boot 健康检查指示器 |

### JMX

| 类名 | 说明 |
|------|------|
| `CacheMXBean` | JMX MBean 接口 |
| `CacheJmxRegistration` | JMX 注册工具 |

### Metrics

| 类名 | 说明 |
|------|------|
| `MicrometerMetricsExporter` | Micrometer 指标导出器 |
| `PrometheusMetricsExporter` | Prometheus 指标导出器 |

## 快速开始

```java
import cloud.opencode.base.cache.*;
import java.time.Duration;

// 创建简单缓存
Cache<String, User> cache = OpenCache.getOrCreate("users", config -> config
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats());

// 基本操作
cache.put("user:1", user);
User user = cache.get("user:1");
User user = cache.get("user:2", key -> userService.findById(key));

// BloomFilter + SingleFlight 保护缓存
ProtectedCache<String, User> protectedCache = ProtectedCache.wrap(cache)
    .bloomFilter(1_000_000, 0.01)
    .singleFlight(true)
    .negativeCache(Duration.ofMinutes(5))
    .build();

// 预刷新缓存
RefreshAheadCache<String, User> refreshCache = RefreshAheadCache.wrap(cache)
    .refreshPolicy(RefreshAheadPolicy.percentageOfTtl(0.8))
    .loader(key -> userService.findById(key))
    .ttl(Duration.ofMinutes(30))
    .build();

// 弹性缓存加载器
Function<String, User> resilientLoader = ResilientCacheLoader.<String, User>builder()
    .loader(key -> userService.findById(key))
    .retry(RetryPolicy.exponentialBackoffWithJitter(3, Duration.ofMillis(100), Duration.ofSeconds(5)))
    .circuitBreaker(5, Duration.ofSeconds(30))
    .bulkhead(10)
    .timeout(Duration.ofSeconds(5))
    .fallback((key, ex) -> User.DEFAULT)
    .build();
```

### 标签批量失效

```java
import cloud.opencode.base.cache.TaggedCache;
import cloud.opencode.base.cache.OpenCache;
import cloud.opencode.base.cache.Cache;

Cache<String, String> base = OpenCache.lruCache(1000);
TaggedCache<String, String> cache = TaggedCache.wrap(base);

cache.put("user:1", "Alice", "tenant:acme", "role:admin");
cache.put("user:2", "Bob", "tenant:acme", "role:user");
cache.put("user:3", "Charlie", "tenant:beta", "role:admin");

// 按标签失效 tenant:acme 下所有条目
cache.invalidateByTag("tenant:acme"); // 移除 user:1 和 user:2

// 按标签失效所有 admin 条目
cache.invalidateByTag("role:admin"); // 移除 user:3（user:1 已被移除）
```

### 条件原子操作（CAS）

```java
import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;

Cache<String, Integer> counter = OpenCache.lruCache(100);
counter.put("hits", 10);
counter.replaceIf("hits", v -> v < 100, 20); // 替换：10 < 100
counter.computeIfMatch("hits", v -> v == 20, v -> v + 1); // Optional.of(21)
```

### 缓存快照持久化

```java
import cloud.opencode.base.cache.util.CacheSnapshot;
import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import java.nio.file.Path;

Cache<String, String> cache = OpenCache.lruCache(1000);
cache.put("key1", "value1");
cache.put("key2", "value2");

// 保存缓存到磁盘
CacheSnapshot.saveStringCache(cache, Path.of("/tmp/cache-snapshot.dat"));

// 重启后恢复
Cache<String, String> newCache = OpenCache.lruCache(1000);
CacheSnapshot.restoreStringCache(Path.of("/tmp/cache-snapshot.dat"), newCache);
```

## 安全修复（开发者须知）

- `DefaultCache.get(K, loader)` 现在是原子操作（使用 `store.compute`），防止缓存击穿
- `evictionLock` 改为 `ReentrantLock` + `tryLock`，读路径不再阻塞
- `CopyOnReadCache` 默认 copier 添加了 `ObjectInputFilter`
- `WriteBehindCache` shutdown 循环 flush 全部待处理写入（之前可能丢数据）
- `TenantCache` tenantId 有长度（256）和数量（10000）限制

## 性能优化（开发者须知）

- `get()` 路径：tryLock 降级，高并发读不再被 eviction tracking 阻塞
- `CacheSnapshot.save`：改为流式遍历，消除 O(n) 内存峰值

## 环境要求

- Java 25+（使用虚拟线程、密封接口、Record）
- 核心功能无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

@author Leon Soo, @since JDK 25

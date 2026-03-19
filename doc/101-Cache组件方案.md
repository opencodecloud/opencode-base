# Cache 组件方案

## 1. 组件概述

`opencode-base-cache` 是一个高性能、可扩展的本地缓存组件库，提供统一的缓存 API。仅依赖 JDK 25 和 core 组件，零外部依赖。

**设计原则：**

1. **零外部依赖**：仅依赖 JDK 25 和 core 组件
2. **高性能**：基于 ConcurrentHashMap 与虚拟线程实现
3. **策略丰富**：支持 LRU/LFU/FIFO/W-TinyLFU 等淘汰策略
4. **异步支持**：完整的 AsyncCache API，基于 CompletableFuture
5. **可监控**：完善的统计指标与延迟百分位跟踪

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cache</artifactId>
    <version>${version}</version>
</dependency>
```

## 2. 包结构

```text
cloud.opencode.base.cache
├── Cache.java                     # 缓存核心接口
├── AsyncCache.java                # 异步缓存接口
├── CacheStats.java                # 缓存统计快照接口
├── CacheMetrics.java              # 详细延迟指标（P50/P95/P99）
├── OpenCache.java                 # 门面入口类
├── CacheManager.java              # 缓存管理器
├── CacheDecorators.java           # 缓存装饰器链式 API
├── LayeredCache.java              # 多级缓存（L1/L2）
├── LoadingCache.java              # 自动加载缓存
├── ReadThroughCache.java          # 读穿透缓存
├── WriteThroughCache.java         # 写穿透缓存
├── WriteBehindCache.java          # 异步写后缓存
├── CopyOnReadCache.java           # 读时复制缓存
├── ProtectedCache.java            # 防穿透/击穿保护缓存
├── NullSafeCache.java             # 空值安全缓存
├── RefreshAheadCache.java         # 提前刷新缓存
├── ReferenceCache.java            # 弱/软引用缓存
├── TimeoutCache.java              # 操作超时缓存
├── TenantCache.java               # 多租户缓存
├── config/                        # 配置
│   ├── CacheConfig.java          # 缓存配置类
│   └── CacheSpec.java            # 规格字符串解析器
├── event/                         # 事件系统
│   ├── CacheEvent.java           # 缓存事件
│   ├── CacheEventListener.java   # 事件监听器
│   └── CacheEventDispatcher.java # 事件分发器
├── analysis/                      # 访问模式分析
│   └── AccessPatternAnalyzer.java # 访问模式分析器
├── bulk/                          # 批量操作
│   └── BulkOperations.java       # 增强批量操作
├── compression/                   # 压缩
│   ├── CompressedCache.java      # 压缩缓存
│   ├── CompressionAlgorithm.java # 压缩算法枚举
│   └── ValueCompressor.java      # 值压缩器
├── distributed/                   # 分布式缓存接口
│   ├── DistributedCache.java     # 分布式缓存接口
│   ├── DistributedCacheConfig.java # 分布式缓存配置
│   └── DistributedCacheStats.java  # 分布式缓存统计
├── dlq/                           # 死信队列
│   └── DeadLetterQueue.java      # 缓存死信队列
├── exception/                     # 异常
│   └── OpenCacheException.java   # 组件统一异常
├── internal/                      # 内部实现
│   ├── DefaultCache.java         # 默认缓存实现
│   ├── ExpirationScheduler.java  # 后台过期清理调度器
│   ├── eviction/                 # 淘汰策略实现
│   │   ├── LruEvictionPolicy.java
│   │   ├── LfuEvictionPolicy.java
│   │   ├── FifoEvictionPolicy.java
│   │   └── WTinyLfuEvictionPolicy.java
│   └── expiry/                   # 过期策略实现
│       ├── TtlExpiryPolicy.java
│       ├── TtiExpiryPolicy.java
│       └── CombinedExpiryPolicy.java
```

---

## 3. 核心 API

### 3.1 OpenCache
> 缓存组件门面入口类，提供便捷的缓存创建与管理 API。

**主要方法 - 缓存创建：**

| 方法 | 描述 |
|------|------|
| `Cache<K,V> getOrCreate(String)` | 创建或获取缓存 |
| `Cache<K,V> getOrCreate(String, Consumer<CacheConfig.Builder>)` | 创建或获取缓存（带配置） |
| `Cache<K,V> fromSpec(String, String)` | 从规格字符串创建缓存 |
| `Cache<K,V> fromSpec(String)` | 从规格字符串创建匿名缓存 |
| `CacheConfig<K,V> parseSpec(String)` | 解析规格字符串为配置 |
| `boolean isValidSpec(String)` | 验证规格字符串 |
| `CacheBuilder<K,V> builder()` | 创建缓存构建器 |

**主要方法 - 快捷缓存创建：**

| 方法 | 描述 |
|------|------|
| `Cache<K,V> lruCache(long)` | 创建 LRU 缓存 |
| `Cache<K,V> lfuCache(long)` | 创建 LFU 缓存 |
| `Cache<K,V> fifoCache(long)` | 创建 FIFO 缓存 |
| `Cache<K,V> wTinyLfuCache(long)` | 创建 W-TinyLFU 缓存 |
| `Cache<K,V> ttlCache(long, Duration)` | 创建 TTL 缓存 |
| `Cache<K,V> ttiCache(long, Duration)` | 创建 TTI 缓存 |
| `Cache<K,V> lruTtlCache(long, Duration)` | 创建 LRU+TTL 缓存 |
| `LoadingCache<K,V> loadingCache(String, Function, long)` | 创建加载缓存 |
| `LoadingCache<K,V> loadingCache(String, Function, long, Duration)` | 创建加载缓存（TTL） |

**主要方法 - 缓存管理：**

| 方法 | 描述 |
|------|------|
| `Optional<Cache<K,V>> get(String)` | 获取已有缓存 |
| `Set<String> names()` | 获取所有缓存名称 |
| `void remove(String)` | 移除缓存 |
| `void cleanUpAll()` | 清理所有缓存 |
| `Map<String,CacheStats> stats()` | 获取全局统计 |
| `void shutdown()` | 关闭缓存系统 |
| `void invalidate(String, K)` | 按名称和键失效 |
| `void invalidateAll(String)` | 按名称全部失效 |
| `boolean isEmpty(String)` | 是否为空 |
| `long size(String)` | 获取缓存大小 |
| `void put(String, K, V)` | 按名称放入 |
| `Optional<V> getValue(String, K)` | 按名称获取值 |
| `CacheDecorators.ChainBuilder<K,V> decorate(Cache)` | 创建装饰器链 |

**主要方法 - 策略工厂：**

| 方法 | 描述 |
|------|------|
| `EvictionPolicy<K,V> lru()` | LRU 淘汰策略 |
| `EvictionPolicy<K,V> lfu()` | LFU 淘汰策略 |
| `EvictionPolicy<K,V> fifo()` | FIFO 淘汰策略 |
| `EvictionPolicy<K,V> wTinyLfu()` | W-TinyLFU 淘汰策略 |
| `ExpiryPolicy<K,V> ttl(Duration)` | TTL 过期策略 |
| `ExpiryPolicy<K,V> tti(Duration)` | TTI 过期策略 |
| `ExpiryPolicy<K,V> combined(Duration, Duration)` | 组合过期策略 |

**CacheBuilder 方法：**

| 方法 | 描述 |
|------|------|
| `CacheBuilder maximumSize(long)` | 最大容量 |
| `CacheBuilder maximumWeight(long)` | 最大权重 |
| `CacheBuilder expireAfterWrite(Duration)` | 写后过期 |
| `CacheBuilder expireAfterAccess(Duration)` | 访问后过期 |
| `CacheBuilder evictionPolicy(EvictionPolicy)` | 淘汰策略 |
| `CacheBuilder expiryPolicy(ExpiryPolicy)` | 过期策略 |
| `CacheBuilder recordStats()` | 开启统计 |
| `CacheBuilder useVirtualThreads()` | 使用虚拟线程 |
| `Cache<K,V> build(String)` | 构建（指定名称） |
| `Cache<K,V> build()` | 构建（匿名） |

**示例：**

```java
// 快捷创建
Cache<String, User> cache = OpenCache.getOrCreate("users", config -> config
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofHours(1)));

// 构建器方式
Cache<String, User> cache = OpenCache.<String, User>builder()
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofHours(1))
    .recordStats()
    .build("users");

// 从规格字符串创建
Cache<String, User> cache = OpenCache.fromSpec("users", "maximumSize=10000,expireAfterWrite=1h");

// 预置策略缓存
Cache<String, User> lru = OpenCache.lruCache(1000);
Cache<String, User> ttl = OpenCache.ttlCache(1000, Duration.ofMinutes(30));
```

---

### 3.2 Cache
> 缓存核心接口，高性能本地缓存同步 API。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `V get(K key)` | 获取值，不存在返回 null |
| `V get(K key, Function loader)` | 获取值，不存在时通过 loader 加载 |
| `Map<K,V> getAll(Iterable keys)` | 批量获取 |
| `Map<K,V> getAll(Iterable keys, Function loader)` | 批量获取，缺失的通过 loader 加载 |
| `void put(K key, V value)` | 放入缓存 |
| `void putAll(Map map)` | 批量放入 |
| `boolean putIfAbsent(K key, V value)` | 不存在时放入 |
| `void putWithTtl(K key, V value, Duration ttl)` | 放入（指定 TTL） |
| `void putAllWithTtl(Map map, Duration ttl)` | 批量放入（指定 TTL） |
| `boolean putIfAbsentWithTtl(K key, V value, Duration ttl)` | 不存在时放入（指定 TTL） |
| `void invalidate(K key)` | 删除 |
| `void invalidateAll(Iterable keys)` | 批量删除 |
| `void invalidateAll()` | 清空全部 |
| `boolean containsKey(K key)` | 是否包含键 |
| `long size()` | 缓存大小 |
| `long estimatedSize()` | 估计大小 |
| `Set<K> keys()` | 所有键 |
| `Collection<V> values()` | 所有值 |
| `Set<Map.Entry<K,V>> entries()` | 所有条目 |
| `ConcurrentMap<K,V> asMap()` | 转为 ConcurrentMap 视图 |
| `CacheStats stats()` | 获取统计 |
| `CacheMetrics metrics()` | 获取详细指标 |
| `void cleanUp()` | 触发清理 |
| `AsyncCache<K,V> async()` | 获取异步视图 |
| `String name()` | 缓存名称 |

**示例：**

```java
Cache<String, User> cache = OpenCache.getOrCreate("users");
cache.put("user:1001", user);
User user = cache.get("user:1001");
User loaded = cache.get("user:1001", key -> userService.findById(key));
cache.invalidate("user:1001");
```

---

### 3.3 CacheStats
> 缓存统计快照接口，不可变的缓存性能指标。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `long requestCount()` | 总请求数 |
| `long hitCount()` | 命中数 |
| `long missCount()` | 未命中数 |
| `double hitRate()` | 命中率 |
| `double missRate()` | 未命中率 |
| `long evictionCount()` | 淘汰数 |
| `long loadCount()` | 加载数 |
| `double averageLoadPenalty()` | 平均加载耗时 |
| `CacheStats minus(CacheStats)` | 两次统计的差值 |
| `CacheStats plus(CacheStats)` | 两次统计的和 |

**示例：**

```java
CacheStats stats = cache.stats();
double hitRate = stats.hitRate();
CacheStats before = cache.stats();
// ... 操作 ...
CacheStats after = cache.stats();
CacheStats delta = after.minus(before);
```

---

### 3.4 CacheMetrics
> 详细延迟指标跟踪，支持百分位计算（P50/P95/P99）。

**主要方法 - 记录：**

| 方法 | 描述 |
|------|------|
| `CacheMetrics create()` | 创建指标实例 |
| `void recordGetLatency(long)` | 记录 get 延迟（纳秒） |
| `void recordPutLatency(long)` | 记录 put 延迟（纳秒） |
| `void recordLoadLatency(long)` | 记录 load 延迟（纳秒） |
| `void recordEviction()` | 记录淘汰事件 |

**主要方法 - 查询：**

| 方法 | 描述 |
|------|------|
| `long getGetCount()` | get 总数 |
| `long getPutCount()` | put 总数 |
| `long getLoadCount()` | load 总数 |
| `long getEvictionCount()` | 淘汰总数 |
| `long getGetLatencyP50()` | get 延迟 P50 |
| `long getGetLatencyP95()` | get 延迟 P95 |
| `long getGetLatencyP99()` | get 延迟 P99 |
| `long getPutLatencyP50()` | put 延迟 P50 |
| `long getPutLatencyP95()` | put 延迟 P95 |
| `long getPutLatencyP99()` | put 延迟 P99 |
| `long getLoadLatencyP50()` | load 延迟 P50 |
| `long getLoadLatencyP95()` | load 延迟 P95 |
| `long getLoadLatencyP99()` | load 延迟 P99 |
| `double getAverageGetLatency()` | 平均 get 延迟 |
| `double getAveragePutLatency()` | 平均 put 延迟 |
| `double getAverageLoadLatency()` | 平均 load 延迟 |
| `long getMinGetLatency()` | 最小 get 延迟 |
| `long getMaxGetLatency()` | 最大 get 延迟 |
| `double getGetThroughput()` | get 吞吐量 |
| `double getPutThroughput()` | put 吞吐量 |
| `Duration getUptime()` | 运行时间 |
| `MetricsSnapshot snapshot()` | 获取快照 |
| `void reset()` | 重置指标 |

**示例：**

```java
CacheMetrics metrics = CacheMetrics.create();
metrics.recordGetLatency(1000);
MetricsSnapshot snapshot = metrics.snapshot();
System.out.println(snapshot.format());
```

---

### 3.5 CacheManager
> 全局缓存实例管理。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CacheManager getInstance()` | 获取单例实例 |
| `Cache<K,V> getOrCreateCache(String, Consumer)` | 创建或获取缓存 |
| `Cache<K,V> getOrCreateCache(String)` | 创建或获取缓存（默认配置） |
| `Cache<K,V> createCache(String, CacheConfig)` | 创建缓存 |
| `Optional<Cache<K,V>> getCache(String)` | 获取缓存 |
| `Set<String> getCacheNames()` | 获取所有名称 |
| `void removeCache(String)` | 移除缓存 |
| `Map<String,CacheStats> getAllStats()` | 获取所有统计 |
| `CacheStats getCombinedStats()` | 获取合并统计 |
| `CacheStats getCacheStats(String)` | 获取指定缓存统计 |
| `Map<String,Cache> getCachesByPattern(String)` | 按模式获取缓存 |
| `int invalidateByPattern(String)` | 按模式失效 |
| `void cleanUpAll()` | 清理所有 |
| `void invalidateAll()` | 全部失效 |
| `void shutdown()` | 关闭 |
| `boolean isShutdown()` | 是否已关闭 |
| `void reset()` | 重置 |

**示例：**

```java
CacheManager manager = CacheManager.getInstance();
Cache<String, User> cache = manager.getOrCreateCache("users", config -> config
    .maximumSize(10_000)
    .expireAfterWrite(Duration.ofHours(1)));
Set<String> names = manager.getCacheNames();
manager.shutdown();
```

---

### 3.6 CacheDecorators
> 缓存装饰器，流式 API 链式组合多种缓存增强。

**ChainBuilder 方法：**

| 方法 | 描述 |
|------|------|
| `ChainBuilder withProtection()` | 添加防穿透/击穿保护 |
| `ChainBuilder withProtection(long, double)` | 保护（指定 BloomFilter 参数） |
| `ChainBuilder withRefreshAhead(Function, Duration)` | 添加提前刷新 |
| `ChainBuilder withRefreshAhead(Function, Duration, Executor)` | 提前刷新（自定义执行器） |
| `ChainBuilder withTimeout(Duration)` | 添加操作超时 |
| `ChainBuilder withTimeout(Duration, ExecutorService)` | 超时（自定义执行器） |
| `ChainBuilder withCopyOnRead()` | 添加读时复制 |
| `ChainBuilder withCopyOnRead(UnaryOperator)` | 读时复制（自定义复制器） |
| `ChainBuilder with(UnaryOperator)` | 自定义装饰器 |
| `Cache<K,V> build()` | 构建 |

**示例：**

```java
Cache<String, User> decorated = CacheDecorators.chain(cache)
    .withProtection()
    .withRefreshAhead(key -> loadUser(key), Duration.ofMinutes(5))
    .withTimeout(Duration.ofSeconds(3))
    .build();
```

---

### 3.7 LayeredCache
> 多级缓存实现（L1/L2），L1 未命中时自动查询 L2 并回填。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `LayeredCache<K,V> of(Cache, Cache)` | 创建二级缓存 |
| `Builder<K,V> builder(Cache, Cache)` | 创建构建器 |
| `Cache<K,V> l1()` | 获取 L1 缓存 |
| `Cache<K,V> l2()` | 获取 L2 缓存 |
| `WriteStrategy writeStrategy()` | 获取写策略 |
| `boolean isPromoteOnL2Hit()` | L2 命中时是否提升到 L1 |
| `void flush()` | 刷新 L1 到 L2 |
| `void warmUp(Iterable keys)` | 预热 |
| `LayeredCacheStats layeredStats()` | 获取分级统计 |

**WriteStrategy 枚举：**

| 值 | 描述 |
|------|------|
| `WRITE_ALL` | 写入所有层 |
| `WRITE_L1_ONLY` | 仅写入 L1 |
| `WRITE_L2_ONLY` | 仅写入 L2 |

**示例：**

```java
Cache<String, User> l1 = OpenCache.<String, User>builder()
    .maximumSize(100).build("l1");
Cache<String, User> l2 = OpenCache.<String, User>builder()
    .maximumSize(10_000).build("l2");
LayeredCache<String, User> cache = LayeredCache.of(l1, l2);
User user = cache.get("user:1001");  // L1 未命中则查 L2
```

---

### 3.8 LoadingCache
> 自动加载缓存接口，get 时自动通过 loader 加载缺失值。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `V get(K key)` | 获取（自动加载） |
| `V get(K key, Function loader)` | 获取（自定义 loader） |
| `Map<K,V> getAll(Iterable keys)` | 批量获取（自动加载） |
| `CompletableFuture<V> refresh(K key)` | 异步刷新 |
| `Function loader()` | 获取加载器 |
| `AsyncLoadingCache<K,V> asyncLoading()` | 获取异步加载视图 |

**示例：**

```java
LoadingCache<String, User> cache = LoadingCache.create(
    key -> userService.findById(key), 10_000);
User user = cache.get("user:1001");  // 自动加载
cache.refresh("user:1001");           // 异步刷新
```

---

### 3.9 ReadThroughCache
> 读穿透缓存，配置 loader 后自动透明加载。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> wrap(Cache)` | 包装已有缓存 |
| `ReadThroughCache create(String, Function, long)` | 快速创建 |
| `Function loader()` | 获取加载器 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder loader(Function)` | 设置加载器 |
| `Builder loader(CacheLoader)` | 设置 CacheLoader |
| `Builder batchLoader(Function)` | 设置批量加载器 |
| `Builder fallback(Function)` | 设置回退策略 |
| `Builder fallback(V)` | 设置回退值 |
| `Builder cacheNullValues()` | 缓存 null 值 |
| `ReadThroughCache build()` | 构建 |

**示例：**

```java
ReadThroughCache<String, User> cache = ReadThroughCache.wrap(baseCache)
    .loader(key -> userService.findById(key))
    .fallback(User.EMPTY)
    .build();
User user = cache.get("user:1001");  // 透明加载
```

---

### 3.10 WriteThroughCache
> 写穿透缓存，同步写入缓存和后端存储。

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> wrap(Cache)` | 包装已有缓存 |
| `Builder writer(BiConsumer<K,V>)` | 设置写入器 |
| `Builder writer(CacheWriter<K,V>)` | 设置 CacheWriter |
| `Builder deleter(Consumer<K>)` | 设置删除器 |
| `Builder onError(BiConsumer)` | 设置错误处理器 |
| `WriteThroughCache build()` | 构建 |

**示例：**

```java
WriteThroughCache<String, User> cache = WriteThroughCache.wrap(baseCache)
    .writer((key, value) -> userDao.save(key, value))
    .deleter(key -> userDao.delete(key))
    .build();
cache.put("user:1", user);  // 同时写缓存和数据库
```

---

### 3.11 WriteBehindCache
> 异步写后缓存，批量异步持久化，支持写合并、重试和失败处理。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> builder(Cache)` | 创建构建器 |
| `long pendingWriteCount()` | 待写入数量 |
| `void flush()` | 立即刷新 |
| `WriteBehindStats writeBehindStats()` | 获取写入统计 |
| `void shutdown()` | 关闭 |
| `void shutdown(Duration)` | 超时关闭 |
| `void close()` | AutoCloseable 关闭 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder writer(BatchWriter<K,V>)` | 设置批量写入器 |
| `Builder writer(Consumer<Map<K,V>>)` | 设置简单写入器 |
| `Builder batchSize(int)` | 批次大小 |
| `Builder maxQueueSize(int)` | 最大队列 |
| `Builder flushInterval(Duration)` | 刷新间隔 |
| `Builder maxRetries(int)` | 最大重试次数 |
| `Builder retryDelay(Duration)` | 重试延迟 |
| `Builder onFailure(Consumer<WriteFailure>)` | 失败处理器 |
| `WriteBehindCache build()` | 构建 |

**WriteBehindStats 记录：**

| 方法 | 描述 |
|------|------|
| `long totalWrites()` | 总写入数 |
| `long successfulWrites()` | 成功写入数 |
| `long failedWrites()` | 失败写入数 |
| `long coalescedWrites()` | 合并写入数 |
| `long totalBatches()` | 总批次数 |
| `double coalescingRatio()` | 合并率 |
| `double successRatio()` | 成功率 |

**示例：**

```java
WriteBehindCache<String, User> writeBehind = WriteBehindCache.builder(cache)
    .writer(batch -> userDao.batchSave(batch))
    .batchSize(100)
    .flushInterval(Duration.ofSeconds(5))
    .maxRetries(3)
    .build();
writeBehind.put("user:1", user);  // 异步批量写入
```

---

### 3.12 ProtectedCache
> 防穿透/击穿保护缓存，集成 BloomFilter 和 SingleFlight。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> wrap(Cache)` | 包装已有缓存 |
| `ProtectionStats getProtectionStats()` | 获取保护统计 |
| `void clearNegativeCache()` | 清除负缓存 |
| `void shutdown()` | 关闭 |
| `boolean isKeyNegativelyCached(K)` | 键是否在负缓存中 |
| `void resetProtectionStats()` | 重置统计 |
| `boolean mightContainInBloomFilter(K)` | BloomFilter 判断 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder bloomFilter(boolean)` | 启用 BloomFilter |
| `Builder bloomFilter(long, double)` | BloomFilter（指定参数） |
| `Builder singleFlight(boolean)` | 启用 SingleFlight |
| `Builder negativeCache(Duration)` | 设置负缓存时间 |
| `ProtectedCache build()` | 构建 |

**示例：**

```java
ProtectedCache<String, User> protectedCache = ProtectedCache.wrap(cache)
    .bloomFilter(100_000, 0.01)
    .singleFlight(true)
    .negativeCache(Duration.ofMinutes(5))
    .build();
User user = protectedCache.get("user:1", key -> userService.findById(key));
```

---

### 3.13 NullSafeCache
> 空值安全缓存，可存储和区分 null 值。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `NullSafeCache<K,V> wrap(Cache)` | 包装已有缓存 |
| `Optional<V> getIfPresent(K)` | 获取值（区分不存在和 null） |
| `boolean containsNullValue(K)` | 键是否存储了 null 值 |

**示例：**

```java
NullSafeCache<String, User> cache = NullSafeCache.wrap(baseCache);
cache.put("user:deleted", null);               // 存储 null
Optional<User> result = cache.getIfPresent("user:deleted");  // Optional.empty 表示 null
boolean isNull = cache.containsNullValue("user:deleted");    // true
```

---

### 3.14 CopyOnReadCache
> 读时复制缓存，返回缓存值的深拷贝，保证线程安全。

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> wrap(Cache)` | 包装已有缓存 |
| `Builder copier(UnaryOperator<V>)` | 自定义复制器 |
| `Builder copyOnWrite(boolean)` | 是否写入时也复制 |
| `CopyOnReadCache build()` | 构建 |

**示例：**

```java
CopyOnReadCache<String, User> copyCache = CopyOnReadCache.wrap(cache)
    .copier(user -> user.clone())
    .build();
User user = copyCache.get("user:1");  // 返回的是副本
```

---

### 3.15 RefreshAheadCache
> 提前刷新缓存，后台异步刷新即将过期的条目。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> wrap(Cache)` | 包装已有缓存 |
| `RefreshStats getRefreshStats()` | 获取刷新统计 |
| `CompletableFuture<V> forceRefresh(K)` | 强制刷新 |
| `boolean cancelPendingRefresh(K)` | 取消待刷新 |
| `int cancelAllPendingRefreshes()` | 取消所有待刷新 |
| `void shutdown()` | 关闭 |
| `boolean isRefreshInProgress(K)` | 是否刷新中 |
| `int getInFlightRefreshCount()` | 进行中的刷新数 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder refreshPolicy(RefreshAheadPolicy)` | 设置刷新策略 |
| `Builder loader(Function)` | 设置加载器 |
| `Builder executor(Executor)` | 设置执行器 |
| `Builder ttl(Duration)` | 设置 TTL |
| `RefreshAheadCache build()` | 构建 |

**示例：**

```java
RefreshAheadCache<String, User> refreshCache = RefreshAheadCache.wrap(cache)
    .loader(key -> userService.findById(key))
    .ttl(Duration.ofMinutes(10))
    .build();
```

---

### 3.16 ReferenceCache
> 内存敏感的弱/软引用缓存，GC 时自动回收。

**ReferenceType 枚举：**

| 值 | 描述 |
|------|------|
| `STRONG` | 强引用 |
| `SOFT` | 软引用（内存不足时回收） |
| `WEAK` | 弱引用（GC 时回收） |
| `SOFT_KEYS` | 软引用键 |
| `WEAK_KEYS` | 弱引用键 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> builder(String)` | 创建构建器 |
| `Builder referenceType(ReferenceType)` | 设置引用类型 |
| `Builder removalListener(RemovalListener)` | 设置移除监听器 |
| `Builder recordStats()` | 开启统计 |
| `ReferenceCache build()` | 构建 |

**示例：**

```java
Cache<String, byte[]> blobCache = ReferenceCache.<String, byte[]>builder("blobs")
    .referenceType(ReferenceType.SOFT)
    .build();
```

---

### 3.17 TimeoutCache
> 操作超时缓存，防止加载器无限阻塞。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> wrap(Cache)` | 包装已有缓存 |
| `V getWithTimeout(K, Function, Duration)` | 获取（指定超时） |
| `Map<K,V> getAllWithTimeout(Iterable, Function, Duration)` | 批量获取（超时） |
| `Cache<K,V> getDelegate()` | 获取被包装的缓存 |
| `Duration getDefaultTimeout()` | 获取默认超时 |
| `void shutdown()` | 关闭 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder defaultTimeout(Duration)` | 设置默认超时 |
| `Builder executor(ExecutorService)` | 设置执行器 |
| `TimeoutCache build()` | 构建 |

**示例：**

```java
TimeoutCache<String, User> timeoutCache = TimeoutCache.wrap(cache)
    .defaultTimeout(Duration.ofSeconds(3))
    .build();
User user = timeoutCache.get("user:1", key -> slowDatabaseCall(key));
```

---

### 3.18 TenantCache
> 多租户缓存，租户间数据隔离，支持独立配额。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> builder(String)` | 创建构建器 |
| `V get(String tenantId, K key)` | 获取（指定租户） |
| `V get(String tenantId, K key, Function)` | 获取（带加载器） |
| `void put(String tenantId, K key, V value)` | 放入 |
| `void put(String tenantId, K key, V value, Duration ttl)` | 放入（指定 TTL） |
| `void invalidate(String tenantId, K key)` | 删除 |
| `void invalidateAll(String tenantId)` | 清空租户数据 |
| `void invalidateAll()` | 清空所有租户 |
| `Cache<K,V> forTenant(String tenantId)` | 获取租户视图 |
| `Set<String> tenants()` | 获取所有租户 ID |
| `long tenantSize(String tenantId)` | 租户缓存大小 |
| `CacheStats tenantStats(String tenantId)` | 租户统计 |
| `CacheStats aggregatedStats()` | 聚合统计 |
| `void removeTenant(String tenantId)` | 移除租户 |
| `void setTenantQuota(String, long)` | 设置租户配额 |
| `void setTenantQuota(String, long, Duration)` | 设置租户配额（含 TTL） |
| `long totalSize()` | 总大小 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder defaultMaxSize(long)` | 默认最大容量 |
| `Builder defaultTtl(Duration)` | 默认 TTL |
| `Builder tenantQuota(String, long)` | 租户配额 |
| `Builder tenantQuota(String, long, Duration)` | 租户配额（含 TTL） |
| `Builder cacheFactory(Supplier)` | 缓存工厂 |
| `TenantCache build()` | 构建 |

**示例：**

```java
TenantCache<String, User> cache = TenantCache.builder("users")
    .defaultMaxSize(1000)
    .tenantQuota("tenant-1", 5000)
    .build();
cache.put("tenant-1", "user:1", user);
Cache<String, User> tenantView = cache.forTenant("tenant-1");
```

---

### 3.19 事件系统（event 包）

#### CacheEvent
> 缓存事件，记录缓存操作的详情。

**CacheEvent.EventType 枚举值：** `CREATED`, `UPDATED`, `REMOVED`, `EXPIRED`, `EVICTED`, `LOADED`, `CLEARED`

#### CacheEventListener
> 事件监听器接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `void onEvent(CacheEvent)` | 处理事件 |
| `CacheEventListener filtering(Predicate)` | 过滤 |
| `CacheEventListener andThen(CacheEventListener)` | 链式组合 |

#### CacheEventDispatcher
> 事件分发器，支持同步/异步分发。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CacheEventDispatcher create()` | 创建分发器 |
| `void addListener(CacheEventListener)` | 添加监听器 |
| `void removeListener(CacheEventListener)` | 移除监听器 |
| `void dispatch(CacheEvent)` | 分发事件 |
| `boolean hasListenersFor(CacheEvent.EventType)` | 是否有监听器 |
| `void shutdown()` | 关闭 |

---

### 3.20 AccessPatternAnalyzer
> 访问模式分析器，追踪缓存键的访问频率和模式。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `AccessPatternAnalyzer<K> create()` | 创建分析器 |
| `void recordAccess(K key)` | 记录访问 |
| `void recordAccess(K key, long count)` | 记录访问（批量） |
| `void recordMiss(K key)` | 记录未命中 |
| `Set<K> getHotKeys()` | 获取热点键 |
| `Set<K> getColdKeys()` | 获取冷数据键 |
| `List<KeyAccessCount<K>> getTopK()` | 获取 Top K |
| `List<KeyAccessCount<K>> getTopK(int)` | 获取 Top K（指定 K） |
| `long getAccessCount(K key)` | 获取键访问次数 |
| `AccessPatternReport<K> analyze()` | 生成分析报告 |
| `int pruneCold(Duration)` | 清理冷数据 |

**示例：**

```java
AccessPatternAnalyzer<String> analyzer = AccessPatternAnalyzer.<String>builder()
    .hotKeyThreshold(100)
    .topKSize(20)
    .build();
analyzer.recordAccess("user:1");
Set<String> hotKeys = analyzer.getHotKeys();
AccessPatternReport<String> report = analyzer.analyze();
```

---

### 3.21 BulkOperations
> 增强批量操作工具。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `BulkOperations<K,V> on(Cache)` | 在缓存上创建 |
| `boolean bulkPutAll(Map)` | 原子批量放入 |
| `Map<K,V> atomicInvalidateAll(Iterable)` | 原子批量删除（返回旧值） |
| `boolean putAllIfAllPresent(Map)` | 全部存在时才放入 |
| `boolean putAllIfAllAbsent(Map)` | 全部不存在时才放入 |
| `BulkPutResult<K> putAllIfAbsent(Map)` | 不存在时放入（返回详情） |
| `int batchProcess(Iterable, int, BatchProcessor)` | 分批处理 |
| `CompletableFuture<BatchResult> batchProcessParallel(Iterable, int, BatchProcessor)` | 并行分批处理 |
| `Map<K,V> computeAll(Iterable, Function)` | 批量计算 |
| `void putAllWithTtl(Map<K, TtlValue<V>>)` | 批量放入（独立 TTL） |

**示例：**

```java
BulkOperations<String, User> bulk = BulkOperations.on(cache);
bulk.bulkPutAll(Map.of("k1", v1, "k2", v2));
bulk.batchProcess(keys, 100, (batch, batchCache) -> { /* 处理 */ });
```

---

### 3.22 CompressedCache
> 压缩缓存装饰器，透明压缩/解压缓存值以节省内存。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Builder<K,V> wrap(Cache)` | 包装已有缓存 |
| `CompressionStats compressionStats()` | 获取压缩统计 |
| `void resetCompressionStats()` | 重置压缩统计 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder compressor(ValueCompressor)` | 设置压缩器 |
| `Builder serializer(ValueSerializer<V>)` | 设置序列化器 |
| `CompressedCache build()` | 构建 |

**CompressionAlgorithm 枚举值：** `NONE`, `GZIP`, `DEFLATE`, `LZ4`, `SNAPPY`, `ZSTD`

**示例：**

```java
CompressedCache<String, byte[]> cache = CompressedCache.<String, byte[]>wrap(baseCache)
    .compressor(ValueCompressor.gzip())
    .build();
CompressionStats stats = cache.compressionStats();
System.out.println("Compression ratio: " + stats.compressionRatio());
```

---

### 3.23 CacheSpec（config 包）
> 缓存规格字符串解析器，支持从字符串配置创建缓存。

格式示例：`maximumSize=10000,expireAfterWrite=1h,expireAfterAccess=30m`

**主要方法（通过 OpenCache 暴露）：**

| 方法 | 描述 |
|------|------|
| `OpenCache.fromSpec(String, String)` | 从规格字符串创建缓存 |
| `OpenCache.parseSpec(String)` | 解析为配置 |
| `OpenCache.isValidSpec(String)` | 验证规格字符串 |

# OpenCode Base Cache

**High-performance local caching library for Java 25+**

`opencode-base-cache` is a modern, feature-rich caching library designed for high-throughput applications. It provides a comprehensive set of caching capabilities comparable to Caffeine, Guava Cache, and EhCache.

## Features

### Core Features
- **Multiple Eviction Policies**: LRU, LFU, FIFO, W-TinyLFU, and composite policies
- **Flexible Expiration**: TTL, TTI, and custom expiry policies
- **Async API**: Full async support with CompletableFuture
- **Statistics**: Comprehensive cache statistics and metrics
- **Virtual Threads**: Native JDK 25 virtual thread support

### Advanced Features
- **Cache Protection**: BloomFilter and SingleFlight integration (ProtectedCache)
- **Refresh-Ahead**: Proactive background refresh before expiration (RefreshAheadCache)
- **Copy-on-Read**: Thread-safe deep copy decorator (CopyOnReadCache)
- **Cache Warming**: Priority-based warming with parallel execution (CacheWarmerManager)
- **Reactive API**: JDK Flow API and Project Reactor support (ReactiveCache)
- **Resilient Loading**: Retry, circuit breaker, bulkhead, and timeout (ResilientCacheLoader)
- **Sampling Statistics**: High-throughput probabilistic statistics (SamplingStatsCounter)
- **Tag-based Batch Invalidation**: Invalidate groups of entries by tag (TaggedCache)
- **CAS Conditional Operations**: Atomic replaceIf / computeIfMatch on Cache interface
- **Cache Snapshot Persistence**: Save and restore cache contents to disk (CacheSnapshot)
- **Lock-free Read Path**: tryLock degradation ensures high-concurrency reads are never blocked by eviction tracking

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cache</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.cache.*;
import java.time.Duration;

// Create a simple cache
Cache<String, User> cache = OpenCache.getOrCreate("users", config -> config
    .maximumSize(10000)
    .expireAfterWrite(Duration.ofMinutes(30))
    .recordStats());

// Basic operations
cache.put("user:1", user);
User user = cache.get("user:1");
User user = cache.get("user:2", key -> userService.findById(key));
```

## Advanced Usage

### Protected Cache (BloomFilter + SingleFlight)

```java
Cache<String, User> cache = OpenCache.getOrCreate("users");
ProtectedCache<String, User> protectedCache = ProtectedCache.wrap(cache)
    .bloomFilter(1_000_000, 0.01)  // 1M expected, 1% false positive
    .singleFlight(true)            // Deduplicate concurrent loads
    .negativeCache(Duration.ofMinutes(5))
    .build();
```

### Refresh-Ahead Cache

```java
RefreshAheadCache<String, User> refreshCache = RefreshAheadCache.wrap(cache)
    .refreshPolicy(RefreshAheadPolicy.percentageOfTtl(0.8))  // Refresh at 80% of TTL
    .loader(key -> userService.findById(key))
    .ttl(Duration.ofMinutes(30))
    .build();
```

### Copy-on-Read Cache

```java
CopyOnReadCache<String, Config> copyCache = CopyOnReadCache.wrap(cache)
    .copier(config -> config.toBuilder().build())  // Custom copier
    .copyOnWrite(true)                              // Also copy on write
    .build();
```

### Cache Warming

```java
CacheWarmerManager manager = CacheWarmerManager.getInstance();

// Register warmers with priority
manager.register("users", userCache, () -> userDao.findHotUsers(1000), 0);
manager.register("products", productCache,
    CacheWarmer.paged(offset -> productDao.findProducts(offset, 100), 100, 10), 1);

// Warm all caches
WarmingResult result = manager.warmAllParallel(4);  // 4 concurrent warmings
```

### Reactive Cache

```java
ReactiveCache<String, User> reactiveCache = ReactiveCache.wrap(cache);

// Using JDK Flow API
reactiveCache.getMono("user:1")
    .subscribe(new Flow.Subscriber<>() { ... });

// Using CompletableFuture
reactiveCache.getOrLoad("user:1", key -> loadUser(key))
    .thenAccept(user -> process(user));
```

### Resilient Cache Loader

```java
Function<String, User> resilientLoader = ResilientCacheLoader.<String, User>builder()
    .loader(key -> userService.findById(key))
    .retry(RetryPolicy.exponentialBackoffWithJitter(3, Duration.ofMillis(100), Duration.ofSeconds(5)))
    .circuitBreaker(5, Duration.ofSeconds(30))
    .bulkhead(10)
    .timeout(Duration.ofSeconds(5))
    .fallback((key, ex) -> User.DEFAULT)
    .build();

User user = cache.get("user:1", resilientLoader);
```

### Policy Composition

```java
// Combine eviction policies
EvictionPolicy<String, User> policy = EvictionPolicy.<String, User>lru()
    .or(EvictionPolicy.lfu());  // LRU or LFU

// Weighted combination
EvictionPolicy<String, User> weighted = EvictionPolicy.weighted(
    EvictionPolicy.WeightedPolicy.of(EvictionPolicy.lru(), 0.7),
    EvictionPolicy.WeightedPolicy.of(EvictionPolicy.lfu(), 0.3)
);
```

### Sampling Statistics (High Throughput)

```java
// 10% sampling for high-throughput scenarios
StatsCounter counter = StatsCounter.sampling(0.1);

// Pre-configured options
StatsCounter highThroughput = StatsCounter.samplingHighThroughput();  // 1%
StatsCounter balanced = StatsCounter.samplingBalanced();              // 10%
```

### Tag-based Batch Invalidation

```java
import cloud.opencode.base.cache.TaggedCache;
import cloud.opencode.base.cache.OpenCache;
import cloud.opencode.base.cache.Cache;

Cache<String, String> base = OpenCache.lruCache(1000);
TaggedCache<String, String> cache = TaggedCache.wrap(base);

cache.put("user:1", "Alice", "tenant:acme", "role:admin");
cache.put("user:2", "Bob", "tenant:acme", "role:user");
cache.put("user:3", "Charlie", "tenant:beta", "role:admin");

// Invalidate all entries for tenant:acme
cache.invalidateByTag("tenant:acme"); // removes user:1 and user:2

// Invalidate all admin entries
cache.invalidateByTag("role:admin"); // removes user:3 (user:1 already gone)
```

### CAS Conditional Operations

```java
import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;

Cache<String, Integer> counter = OpenCache.lruCache(100);
counter.put("hits", 10);
counter.replaceIf("hits", v -> v < 100, 20); // replaces: 10 < 100
counter.computeIfMatch("hits", v -> v == 20, v -> v + 1); // Optional.of(21)
```

### Cache Snapshot Persistence

```java
import cloud.opencode.base.cache.util.CacheSnapshot;
import cloud.opencode.base.cache.Cache;
import cloud.opencode.base.cache.OpenCache;
import java.nio.file.Path;

Cache<String, String> cache = OpenCache.lruCache(1000);
cache.put("key1", "value1");
cache.put("key2", "value2");

// Save cache to disk
CacheSnapshot.saveStringCache(cache, Path.of("/tmp/cache-snapshot.dat"));

// Restore on restart
Cache<String, String> newCache = OpenCache.lruCache(1000);
CacheSnapshot.restoreStringCache(Path.of("/tmp/cache-snapshot.dat"), newCache);
```

## Package Structure

```
cloud.opencode.base.cache
├── Cache.java                   # Core cache interface
├── OpenCache.java               # Facade and factory
├── CacheManager.java            # Cache lifecycle management
├── CopyOnReadCache.java         # Deep copy decorator
├── ProtectedCache.java          # BloomFilter + SingleFlight
├── RefreshAheadCache.java       # Proactive refresh
├── TaggedCache.java             # Tag-based batch invalidation decorator
├── TimeoutCache.java            # Operation timeout wrapper
├── config/
│   └── CacheConfig.java         # Configuration builder
├── internal/
│   ├── DefaultCache.java        # Default implementation
│   ├── eviction/                # Eviction policy implementations
│   └── stats/
│       ├── LongAdderStatsCounter.java
│       └── SamplingStatsCounter.java
├── util/
│   └── CacheSnapshot.java      # Cache snapshot persistence
├── protection/
│   ├── BloomFilter.java
│   └── SingleFlight.java
├── reactive/
│   └── ReactiveCache.java       # Reactive API support
├── resilience/
│   └── ResilientCacheLoader.java
├── spi/
│   ├── CacheLoader.java
│   ├── CacheSerializer.java
│   ├── CacheWarmer.java
│   ├── EvictionPolicy.java
│   ├── ExpiryPolicy.java
│   ├── RefreshAheadPolicy.java
│   ├── RemovalListener.java
│   ├── RetryPolicy.java
│   └── StatsCounter.java
└── warming/
    └── CacheWarmerManager.java
```

## Comparison with Other Libraries

| Feature | OpenCode Cache | Caffeine | Guava Cache | EhCache |
|---------|---------------|----------|-------------|---------|
| W-TinyLFU | Yes | Yes | No | No |
| Async API | Yes | Yes | Yes | Yes |
| Virtual Threads | Native | No | No | No |
| Refresh-Ahead | Yes | Yes | No | Yes |
| BloomFilter Protection | Yes | No | Yes | No |
| SingleFlight | Yes | No | No | No |
| Copy-on-Read | Yes | No | No | Yes |
| Reactive API | Yes | No | No | No |
| Sampling Stats | Yes | No | No | Yes |
| Tag-based Invalidation | Yes | No | No | Yes |
| CAS Operations | Yes | No | No | No |
| Snapshot Persistence | Yes | No | No | Yes |
| Spring Boot Integration | Yes | Yes | Yes | Yes |
| Zero Dependencies | Yes | Yes | Yes | No |

## Security Fixes (Developer Notes)

- `DefaultCache.get(K, loader)` is now atomic (uses `store.compute`), preventing cache stampede
- `evictionLock` changed to `ReentrantLock` + `tryLock`, read path no longer blocks
- `CopyOnReadCache` default copier now applies `ObjectInputFilter`
- `WriteBehindCache` shutdown flushes all pending writes in a loop (previously could lose data)
- `TenantCache` tenantId has length (256) and count (10000) limits

## Performance Improvements (Developer Notes)

- `get()` path: tryLock degradation ensures high-concurrency reads are never blocked by eviction tracking
- `CacheSnapshot.save`: streaming traversal eliminates O(n) memory peak

## Requirements

- Java 25+ (uses virtual threads, sealed interfaces, records)
- No external dependencies for core functionality

## Optional Dependencies

- `com.fasterxml.jackson.core:jackson-databind` - JSON serialization
- `com.google.code.gson:gson` - Alternative JSON serialization
- `com.esotericsoftware:kryo` - High-performance serialization
- `io.micrometer:micrometer-core` - Metrics export
- `io.projectreactor:reactor-core` - Reactor Mono/Flux support
- `org.springframework.boot:spring-boot-starter` - Spring Boot integration

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

@author Leon Soo, @since JDK 25

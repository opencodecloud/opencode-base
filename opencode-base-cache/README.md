# OpenCode Base Cache

**High-performance local caching library for Java 25+**

`opencode-base-cache` is a modern, feature-rich caching library designed for high-throughput applications. It provides a comprehensive set of caching capabilities comparable to Caffeine, Guava Cache, and EhCache.

## Features

### Core Features
- **Multiple Eviction Policies**: LRU, LFU, FIFO, W-TinyLFU, and composite policies
- **Flexible Expiration**: TTL, TTI, and custom expiry policies
- **Async API**: Full async support with CompletableFuture
- **Statistics**: Comprehensive cache statistics and metrics
- **Virtual Threads**: Native JDK 21+ virtual thread support

### Advanced Features
- **Cache Protection**: BloomFilter and SingleFlight integration (ProtectedCache)
- **Refresh-Ahead**: Proactive background refresh before expiration (RefreshAheadCache)
- **Copy-on-Read**: Thread-safe deep copy decorator (CopyOnReadCache)
- **Cache Warming**: Priority-based warming with parallel execution (CacheWarmerManager)
- **Reactive API**: JDK Flow API and Project Reactor support (ReactiveCache)
- **Resilient Loading**: Retry, circuit breaker, bulkhead, and timeout (ResilientCacheLoader)
- **Sampling Statistics**: High-throughput probabilistic statistics (SamplingStatsCounter)

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-cache</artifactId>
    <version>1.0.0</version>
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

## Package Structure

```
cloud.opencode.base.cache
├── Cache.java                   # Core cache interface
├── OpenCache.java               # Facade and factory
├── CacheManager.java            # Cache lifecycle management
├── CopyOnReadCache.java         # Deep copy decorator
├── ProtectedCache.java          # BloomFilter + SingleFlight
├── RefreshAheadCache.java       # Proactive refresh
├── TimeoutCache.java            # Operation timeout wrapper
├── config/
│   └── CacheConfig.java         # Configuration builder
├── internal/
│   ├── DefaultCache.java        # Default implementation
│   ├── eviction/                # Eviction policy implementations
│   └── stats/
│       ├── LongAdderStatsCounter.java
│       └── SamplingStatsCounter.java
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
| Spring Boot Integration | Yes | Yes | Yes | Yes |
| Zero Dependencies | Yes | Yes | Yes | No |

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

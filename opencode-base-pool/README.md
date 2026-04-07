# OpenCode Base Pool

**High-performance object pool for Java 25+**

`opencode-base-pool` is a modern object pooling library with support for generic pools, keyed pools, thread-local pools, soft-reference pools, and virtual-thread-optimized pools. It uses JDK 25 records for immutable configuration and provides comprehensive metrics and eviction policies.

## Features

### Core Features
- **Generic Object Pool**: Configurable pool with borrow/return semantics and auto-return execute pattern
- **Keyed Object Pool**: Separate sub-pools per key for multi-tenant or multi-resource scenarios
- **Thread-Local Pool**: One object per thread for zero-contention access
- **Soft Reference Pool**: GC-friendly pool that releases objects under memory pressure
- **Virtual Thread Pool**: Optimized for virtual threads with ScopedValue context propagation

### V1.0.3 New Features
- **PoolLease** (AutoCloseable): Try-with-resources pattern for borrowed objects -- no more forgotten `returnObject()` calls
- **SimplePooledObjectFactory**: Create pools from `Supplier`/`Consumer` -- one-line pool creation
- **PoolEventListener**: Lifecycle event hooks (onBorrow, onReturn, onCreate, onDestroy, onEvict, onExhausted, onTimeout)
- **MaxAge Eviction**: Evict objects that exceed a maximum lifetime (e.g., recycle DB connections every 30 minutes)
- **Max Object Lifetime**: Config-level lifetime enforcement -- expired objects are rejected on borrow
- **Pool Warm-up**: `preparePool(count)` method to pre-create objects on demand

### Configuration
- **Builder Pattern**: Immutable `PoolConfig` record with fluent builder
- **Size Control**: maxTotal, maxIdle, minIdle with configurable wait policy
- **Timeout**: Configurable borrow timeout with BLOCK, FAIL, or GROW policies
- **LIFO/FIFO**: Configurable object ordering

### Eviction & Validation
- **Eviction Policies**: Idle-time, LRU, LFU, MaxAge, and composite (all/any) eviction
- **Periodic Eviction**: Configurable eviction run interval and batch size
- **Object Validation**: Test on borrow, return, create, and while idle

### Observability
- **Pool Metrics**: Borrow count, return count, active/idle counts, wait time statistics
- **Metrics Snapshot**: Point-in-time immutable snapshot of pool metrics
- **Observability Exporter**: Export metrics to external monitoring systems
- **Object Tracking**: Track pooled object lifecycle and detect leaks
- **Event Listener**: Real-time lifecycle event callbacks for monitoring and debugging

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pool</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Simplest Usage (V1.0.3)
```java
import cloud.opencode.base.pool.*;

// One-line pool creation from Supplier
ObjectPool<StringBuilder> pool = OpenPool.createPool(StringBuilder::new);

// Try-with-resources (recommended -- prevents leaks)
try (PoolLease<StringBuilder> lease = pool.borrowLease()) {
    lease.get().append("Hello");
}

// Or with Supplier + Consumer (auto-close on destroy)
ObjectPool<Connection> pool = OpenPool.createPool(
    () -> DriverManager.getConnection(url),
    Connection::close
);
```

### Traditional Usage
```java
// Create a pool with factory
ObjectPool<Connection> pool = OpenPool.createPool(myFactory);

// Execute pattern (auto-return)
String result = pool.execute(conn -> conn.executeQuery("SELECT 1"));

// Manual borrow/return
Connection conn = pool.borrowObject();
try {
    conn.executeQuery("SELECT 1");
} finally {
    pool.returnObject(conn);
}
```

### Custom Configuration
```java
PoolConfig config = PoolConfig.builder()
    .maxTotal(20)
    .maxIdle(10)
    .minIdle(5)
    .maxWait(Duration.ofSeconds(10))
    .testOnBorrow(true)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .maxObjectLifetime(Duration.ofHours(1))  // V1.0.3: force recycle after 1h
    .build();

ObjectPool<Connection> pool = OpenPool.createPool(factory, config);
```

### Event Listener (V1.0.3)
```java
PoolConfig config = PoolConfig.builder()
    .maxTotal(10)
    .eventListener(new PoolEventListener<Connection>() {
        @Override
        public void onExhausted() {
            log.warn("Connection pool exhausted!");
        }
        @Override
        public void onTimeout(Duration waitDuration) {
            log.error("Timed out after {}ms", waitDuration.toMillis());
        }
    })
    .build();
```

### Pool Lease with Invalidation (V1.0.3)
```java
try (PoolLease<Connection> lease = pool.borrowLease()) {
    try {
        lease.get().executeUpdate("INSERT ...");
    } catch (SQLException e) {
        lease.invalidate();  // Mark as bad -- will be destroyed, not returned
        throw e;
    }
}
```

### Pool Warm-up (V1.0.3)
```java
ObjectPool<Connection> pool = OpenPool.createPool(factory, config);
pool.preparePool(5);  // Pre-create 5 objects
```

### Keyed Pool
```java
KeyedObjectPool<String, Connection> pool =
    OpenPool.createKeyedPool(keyedFactory, config);

Connection conn = pool.borrowObject("database-1");
try {
    // use connection for database-1
} finally {
    pool.returnObject("database-1", conn);
}
```

### Virtual Thread Pool
```java
ObjectPool<Connection> pool = OpenPool.createVirtualThreadPool(factory, config);
```

### Eviction Policies
```java
// MaxAge eviction: recycle objects older than 30 minutes (V1.0.3)
EvictionPolicy<Connection> maxAge = OpenPool.maxAgeEviction(Duration.ofMinutes(30));

// Composite eviction: idle > 30min AND borrow count < 5
EvictionPolicy<Connection> policy = OpenPool.allEviction(
    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
    OpenPool.lfuEviction(5)
);

PoolConfig config = PoolConfig.builder()
    .evictionPolicy(policy)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .build();
```

### Simplified Factory (V1.0.3)
```java
// Full control with builder
PooledObjectFactory<Connection> factory = SimplePooledObjectFactory
    .<Connection>builder(() -> DriverManager.getConnection(url))
    .destroyer(Connection::close)
    .validator(conn -> !conn.isClosed())
    .passivator(Connection::clearWarnings)
    .build();
```

## Class Reference

### Root Package (`cloud.opencode.base.pool`)
| Class | Description |
|-------|-------------|
| `OpenPool` | Main facade with factory methods for creating all pool types |
| `ObjectPool<T>` | Core object pool interface with borrow/return/execute/borrowLease semantics |
| `KeyedObjectPool<K,V>` | Keyed object pool interface for per-key sub-pools |
| `PoolConfig` | Immutable pool configuration record with builder pattern |
| `PoolContext` | Contextual information passed during pool operations |
| `PooledObject<T>` | Wrapper around a pooled object with state and timestamp tracking |
| `PooledObjectFactory<T>` | Factory interface for creating, validating, and destroying pooled objects |
| `PoolLease<T>` | AutoCloseable lease for try-with-resources borrow pattern **(V1.0.3)** |
| `PoolEventListener<T>` | Lifecycle event listener for pool monitoring **(V1.0.3)** |

### Factory (`cloud.opencode.base.pool.factory`)
| Class | Description |
|-------|-------------|
| `BasePooledObjectFactory<T>` | Abstract base class for simple pooled object factories |
| `BaseKeyedPooledObjectFactory<K,V>` | Abstract base class for keyed pooled object factories |
| `DefaultPooledObject<T>` | Default implementation of PooledObject with state management |
| `KeyedPooledObjectFactory<K,V>` | Factory interface for keyed pooled objects |
| `PooledObjectState` | Enum of pooled object lifecycle states (IDLE, ALLOCATED, etc.) |
| `SimplePooledObjectFactory<T>` | Supplier/Consumer-based factory for simple use cases **(V1.0.3)** |

### Implementations (`cloud.opencode.base.pool.impl`)
| Class | Description |
|-------|-------------|
| `GenericObjectPool<T>` | Full-featured generic object pool implementation |
| `GenericKeyedObjectPool<K,V>` | Full-featured keyed object pool implementation |
| `ThreadLocalPool<T>` | Thread-local pool with one object per thread |
| `SoftReferencePool<T>` | GC-friendly pool using soft references |
| `VirtualThreadPool<T>` | Pool optimized for virtual threads with ScopedValue support |
| `IdentityWrapper<T>` | Identity-based wrapper for pool object tracking |

### Policy (`cloud.opencode.base.pool.policy`)
| Class | Description |
|-------|-------------|
| `EvictionPolicy<T>` | Sealed interface: IdleTime, LRU, LFU, MaxAge, Composite |
| `EvictionPolicy.MaxAge<T>` | Age-based eviction policy **(V1.0.3)** |
| `ValidationPolicy<T>` | Validation strategy for pooled objects |
| `WaitPolicy` | Enum for pool exhaustion behavior (BLOCK, FAIL, GROW) |
| `EvictionContext<T>` | Context provided to eviction policies during evaluation |

### Metrics (`cloud.opencode.base.pool.metrics`)
| Class | Description |
|-------|-------------|
| `PoolMetrics` | Interface for accessing pool metrics |
| `DefaultPoolMetrics` | Default metrics implementation with atomic counters |
| `MetricsSnapshot` | Immutable point-in-time snapshot of pool metrics |
| `ObservabilityMetricsExporter` | Exports pool metrics to observability systems |

### Tracker (`cloud.opencode.base.pool.tracker`)
| Class | Description |
|-------|-------------|
| `PooledObjectTracker<T>` | Tracks pooled object lifecycle for leak detection |

### Exception (`cloud.opencode.base.pool.exception`)
| Class | Description |
|-------|-------------|
| `OpenPoolException` | Runtime exception for pool operation errors |

## Performance (V1.0.3)

Single-thread borrow+return latency: **~80 ns/op** (9.3× faster than V1.0.0)

| Scenario | Throughput (ops/ms) | Latency (ns/op) |
|----------|--------------------:|----------------:|
| borrowReturn (manual) | 12,435 | 80 |
| borrowReturn (PoolLease) | 11,938 | 84 |
| borrowReturn (execute) | 11,942 | 84 |
| getNumIdle+getNumActive | 189,027 | 5.3 |
| concurrent (4 threads) | 1,846 | 542 |
| concurrent (100 vthreads) | 1,283 | 779 |

Key optimizations: zero-allocation hot path (nanoTime timestamps, no `Instant.now()`), O(1) idle count tracking, merged borrow timestamps.

## Config Validation (V1.0.3)

`PoolConfig.builder().build()` enforces:
- `maxTotal >= 1`
- `maxIdle >= 0`, `minIdle >= 0`
- Auto-clamps: `maxIdle = min(maxIdle, maxTotal)`, `minIdle = min(minIdle, maxIdle)`

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

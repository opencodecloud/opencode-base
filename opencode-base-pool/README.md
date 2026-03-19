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

### Configuration
- **Builder Pattern**: Immutable `PoolConfig` record with fluent builder
- **Size Control**: maxTotal, maxIdle, minIdle with configurable wait policy
- **Timeout**: Configurable borrow timeout with BLOCK or FAIL policies
- **LIFO/FIFO**: Configurable object ordering

### Eviction & Validation
- **Eviction Policies**: Idle-time, LRU, LFU, and composite (all/any) eviction
- **Periodic Eviction**: Configurable eviction run interval and batch size
- **Object Validation**: Test on borrow, return, create, and while idle

### Observability
- **Pool Metrics**: Borrow count, return count, active/idle counts, wait time statistics
- **Metrics Snapshot**: Point-in-time immutable snapshot of pool metrics
- **Observability Exporter**: Export metrics to external monitoring systems
- **Object Tracking**: Track pooled object lifecycle and detect leaks

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-pool</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage
```java
import cloud.opencode.base.pool.*;

// Create a pool with default config
ObjectPool<Connection> pool = OpenPool.createPool(myFactory);

// Borrow and return manually
Connection conn = pool.borrowObject();
try {
    conn.executeQuery("SELECT 1");
} finally {
    pool.returnObject(conn);
}

// Execute pattern (recommended -- auto-return)
String result = pool.execute(conn -> conn.executeQuery("SELECT 1"));
```

### Custom Configuration
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
// Composite eviction: idle > 30min AND borrow count < 5
EvictionPolicy<Connection> policy = OpenPool.allEviction(
    OpenPool.idleTimeEviction(Duration.ofMinutes(30)),
    OpenPool.lfuEviction(5)
);

PoolConfig config = OpenPool.configBuilder()
    .evictionPolicy(policy)
    .timeBetweenEvictionRuns(Duration.ofMinutes(5))
    .build();
```

## Class Reference

### Root Package (`cloud.opencode.base.pool`)
| Class | Description |
|-------|-------------|
| `OpenPool` | Main facade with factory methods for creating all pool types |
| `ObjectPool<T>` | Core object pool interface with borrow/return/execute semantics |
| `KeyedObjectPool<K,V>` | Keyed object pool interface for per-key sub-pools |
| `PoolConfig` | Immutable pool configuration record with builder pattern |
| `PoolContext` | Contextual information passed during pool operations |
| `PooledObject<T>` | Wrapper around a pooled object with state and timestamp tracking |
| `PooledObjectFactory<T>` | Factory interface for creating, validating, and destroying pooled objects |

### Factory (`cloud.opencode.base.pool.factory`)
| Class | Description |
|-------|-------------|
| `BasePooledObjectFactory<T>` | Abstract base class for simple pooled object factories |
| `BaseKeyedPooledObjectFactory<K,V>` | Abstract base class for keyed pooled object factories |
| `DefaultPooledObject<T>` | Default implementation of PooledObject with state management |
| `KeyedPooledObjectFactory<K,V>` | Factory interface for keyed pooled objects |
| `PooledObjectState` | Enum of pooled object lifecycle states (IDLE, ALLOCATED, etc.) |

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
| `EvictionPolicy<T>` | Interface and built-in implementations for eviction strategies |
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

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

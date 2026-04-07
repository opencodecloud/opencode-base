# OpenCode Base Lock

Unified lock abstraction for local and distributed locks for JDK 25+. Provides reentrant locks, read-write locks, stamped locks, spin locks, segment locks, retry locks, TTL locks, observable locks, named locks, lock groups with deadlock prevention, and distributed lock SPI.

## Maven Dependency

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-lock</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Features

- Unified `Lock<T>` interface for both local and distributed locks
- Auto-release with try-with-resources via `LockGuard`
- Local reentrant locks with fair/unfair modes
- Read-write locks for concurrent read access with exclusive writes
- Stamped locks with optimistic reads for read-heavy workloads
- Spin locks for extremely short critical sections
- Segment locks for fine-grained key-based locking
- Retry locks with configurable exponential backoff
- TTL locks with expiry detection for long-held lock monitoring
- Observable locks with event listeners for monitoring
- Named lock factory with striping for string-keyed locking
- Lock groups with deadlock prevention (consistent ordering)
- Lock manager for centralized lock lifecycle management
- Distributed lock SPI for pluggable backends (Redis, ZooKeeper, etc.)
- Fencing token generation for distributed lock safety
- Lock metrics and statistics collection
- Configurable timeout, fairness, and spin count
- Thread-safe and virtual thread friendly
- Convenient `execute()` and `executeWithResult()` methods

## Class Reference

### Core

| Class | Description |
|-------|-------------|
| `OpenLock` | Main facade class for creating and managing all types of locks |
| `Lock<T>` | Unified lock interface supporting try-with-resources, timeout, interruptible, and execute patterns |
| `ReadWriteLock<T>` | Read-write lock interface allowing concurrent readers with exclusive writers |
| `LockGuard<T>` | Auto-closeable lock guard for try-with-resources pattern |
| `LockConfig` | Lock configuration: timeout, fairness, spin count, and other options |
| `LockType` | Enum of lock types: REENTRANT, READ_WRITE, STAMPED, SPIN, SEGMENT |

### Local Locks

| Class | Description |
|-------|-------------|
| `LocalLock` | Local reentrant lock implementation based on `ReentrantLock` |
| `LocalReadWriteLock` | Local read-write lock implementation based on `ReentrantReadWriteLock` |
| `StampedLockAdapter` | StampedLock wrapper with safe optimistic read support |
| `SpinLock` | Spin lock for very short critical sections (nanosecond-level) |
| `SegmentLock<K>` | Fine-grained lock that maps keys to separate lock segments to reduce contention |
| `RetryLock<T>` | Decorator adding configurable retry with exponential backoff to any lock |
| `TtlLock` | Lock with TTL auto-expiry to prevent permanent deadlocks from hung threads |

### Event

| Class | Description |
|-------|-------------|
| `LockEvent` | Lock lifecycle event record (ACQUIRED, RELEASED, TIMEOUT, ERROR) |
| `LockListener` | Functional interface for lock event callbacks |
| `ObservableLock<T>` | Decorator adding event notification to any lock |

### Distributed

| Class | Description |
|-------|-------------|
| `DistributedLock` | Abstract base for distributed lock implementations |
| `DistributedLockConfig` | Configuration for distributed locks: lease time, retry, heartbeat |

### Manager

| Class | Description |
|-------|-------------|
| `LockManager` | Centralized lock manager for creating and tracking lock instances |
| `LockGroup` | Atomic multi-lock acquisition with deadlock prevention via consistent ordering |
| `NamedLockFactory` | Factory that creates/reuses locks by name with configurable striping |

### Metrics

| Class | Description |
|-------|-------------|
| `LockMetrics` | Interface for lock metrics collection |
| `DefaultLockMetrics` | Default implementation of lock metrics with counters and timings |
| `LockStats` | Record containing lock statistics: acquisitions, contentions, wait times |

### Token

| Class | Description |
|-------|-------------|
| `FencingTokenGenerator` | Generates monotonically increasing fencing tokens for distributed lock safety |

### SPI

| Class | Description |
|-------|-------------|
| `DistributedLockProvider` | SPI interface for pluggable distributed lock backends |

### Exceptions

| Class | Description |
|-------|-------------|
| `OpenLockException` | Base exception for lock operations |
| `OpenLockAcquireException` | Thrown when lock acquisition fails |
| `OpenLockTimeoutException` | Thrown when lock acquisition times out |

## Quick Start

```java
// Create and use a local lock
Lock<Long> lock = OpenLock.lock();
lock.execute(() -> {
    // Critical section
    updateSharedState();
});

// Lock with return value
String result = lock.executeWithResult(() -> readSharedState());

// Try-with-resources
try (var guard = lock.lock()) {
    // Automatically released when block exits
    doWork();
}

// Lock with timeout
try (var guard = lock.lock(Duration.ofSeconds(5))) {
    doWork();
}

// Fair lock (FIFO order)
Lock<Long> fairLock = OpenLock.fairLock();

// Read-write lock
ReadWriteLock<Long> rwLock = OpenLock.readWriteLock();
String data = rwLock.executeRead(() -> loadData());
rwLock.executeWrite(() -> saveData(newData));

// Stamped lock with optimistic reads (best for read-heavy workloads)
StampedLockAdapter stampedLock = OpenLock.stampedLock();
String cached = stampedLock.optimisticRead(() -> readFromCache());
stampedLock.executeWrite(() -> updateCache(newData));

// Spin lock for short critical sections
Lock<Long> spinLock = OpenLock.spinLock();

// Segment lock for key-based locking
SegmentLock<String> segmentLock = OpenLock.segmentLock(32);
segmentLock.execute("user:123", () -> updateUser("123"));

// Retry lock with exponential backoff
Lock<Long> retryLock = OpenLock.retryLock(existingLock)
    .maxRetries(5)
    .retryDelay(Duration.ofMillis(200))
    .backoffMultiplier(1.5)
    .build();

// TTL lock (auto-expires to prevent permanent deadlocks)
Lock<Long> ttlLock = OpenLock.ttlLock(Duration.ofSeconds(30));
ttlLock.execute(() -> processTask());

// Observable lock with event listener
Lock<Long> observable = OpenLock.observableLock("myLock",
    event -> log.info("Lock event: {}", event));
observable.execute(() -> doWork());

// Named lock factory
NamedLockFactory factory = OpenLock.namedLockFactory();
factory.execute("order:12345", () -> processOrder("12345"));

// Lock group (deadlock prevention)
try (var guard = OpenLock.lockGroup()
        .add(lockA).add(lockB).add(lockC)
        .timeout(Duration.ofSeconds(10))
        .build().lockAll()) {
    transferFunds(accountA, accountB, accountC);
}

// Lock manager
LockManager manager = OpenLock.manager();

// Custom configuration
Lock<Long> customLock = OpenLock.lock(
    LockConfig.builder()
        .fair(true)
        .timeout(Duration.ofSeconds(30))
        .build()
);
```

## Requirements

- Java 25+

## License

Apache License 2.0

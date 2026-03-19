# OpenCode Base DeepClone

High-performance deep clone library with multiple cloning strategies (reflection, serialization, Unsafe) for JDK 25+.

## Features

- Single-object deep cloning
- Batch cloning for lists
- Parallel cloning with virtual threads
- Async cloning with CompletableFuture
- Multiple strategies: reflective (default), serialization, Unsafe
- Annotation-driven control: `@CloneDeep`, `@CloneIgnore`, `@CloneReference`
- Custom type handlers for arrays, collections, maps, and records
- Pluggable clone strategies via SPI
- Configurable max depth
- Immutable type detection and registration
- Circular reference detection via CloneContext
- Builder API for custom cloner configuration
- Thread-safe

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-deepclone</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

| Class | Description |
|-------|-------------|
| `OpenClone` | Facade class -- main entry point for all cloning operations |
| `Cloner` | Cloner interface for deep clone implementations |
| `ClonerBuilder` | Builder for creating custom Cloner instances with strategy selection |
| `CloneContext` | Cloning context with circular reference tracking and depth control |
| **Cloner Implementations** | |
| `ReflectiveCloner` | Default cloner using reflection (handles most types) |
| `SerializingCloner` | Cloner using Java serialization (requires Serializable) |
| `UnsafeCloner` | High-performance cloner using sun.misc.Unsafe |
| `AbstractCloner` | Base class for cloner implementations |
| **Annotations** | |
| `@CloneDeep` | Marks a field for deep cloning |
| `@CloneIgnore` | Marks a field to be skipped during cloning |
| `@CloneReference` | Marks a field to be copied by reference (shallow) |
| **Type Handlers** | |
| `ArrayHandler` | Handles deep cloning of arrays |
| `CollectionHandler` | Handles deep cloning of Collection types |
| `MapHandler` | Handles deep cloning of Map types |
| `RecordHandler` | Handles deep cloning of Java records |
| `TypeHandler` | Type handler interface |
| **Strategy** | |
| `CloneStrategy` | Clone strategy interface |
| `FieldCloneStrategy` | Per-field clone strategy |
| `TypeCloneStrategy` | Per-type clone strategy |
| **SPI** | |
| `CloneStrategyProvider` | SPI for pluggable clone strategies |
| **Contract** | |
| `DeepCloneable` | Interface for objects that provide custom deep clone logic |
| **Exception** | |
| `OpenDeepCloneException` | Deep clone runtime exception |

## Quick Start

```java
import cloud.opencode.base.deepclone.OpenClone;

// Simple deep clone
User cloned = OpenClone.clone(originalUser);

// Clone via serialization
User serialClone = OpenClone.cloneBySerialization(originalUser);

// Clone via Unsafe (high performance)
User unsafeClone = OpenClone.cloneByUnsafe(originalUser);

// Batch clone
List<User> clonedList = OpenClone.cloneBatch(userList);

// Parallel clone with virtual threads
List<User> parallel = OpenClone.cloneBatchParallel(userList, 4);

// Async clone
CompletableFuture<User> future = OpenClone.cloneAsync(user);

// Custom cloner via builder
Cloner custom = OpenClone.builder()
    .reflective()
    .maxDepth(50)
    .build();
User result = custom.clone(user);
```

## Requirements

- Java 25+

## License

Apache License 2.0

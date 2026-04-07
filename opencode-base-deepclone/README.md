# OpenCode Base DeepClone

High-performance deep clone library for JDK 25+, supporting reflection, serialization, and Unsafe strategies.

## Features

- **Multiple strategies**: Reflective (default), Serialization, Unsafe
- **Null-safe**: All clone methods return `null` for `null` input
- **Shallow clone**: `shallowClone()` copies field references without deep cloning
- **Copy-to**: `copyTo()` merges non-null fields into an existing object
- **Clone policy**: `STANDARD` / `STRICT` (no Unsafe fallback) / `LENIENT` (skip errors)
- **Field filter**: Programmatic field exclusion by name, type, or annotation
- **Clone listener**: Lifecycle hooks for audit/logging (before/after/error)
- **Enum identity**: Enum values preserve `==` identity after clone
- **Optional\<T\>**: Deep clones Optional contents correctly
- **JDK immutable collections**: Detects `List.of()`, `Set.of()`, `Map.of()`, `Collections.unmodifiable*()` (skips copy)
- **Annotation-driven**: `@CloneDeep`, `@CloneIgnore`, `@CloneReference`
- **Type handlers**: Arrays, Collections, Maps, Records, Enums, Optionals
- **Batch & parallel**: `cloneBatch()`, `cloneBatchParallel()` (virtual threads), `cloneAsync()`
- **Circular reference detection** via `CloneContext`
- **Pluggable strategies** via SPI (`CloneStrategyProvider`)
- **Thread-safe**

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-deepclone</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Quick Start

```java
import cloud.opencode.base.deepclone.*;

// Deep clone (reflective, default)
User cloned = OpenClone.clone(user);

// Shallow clone (copies references only)
User shallow = OpenClone.shallowClone(user);

// Copy non-null fields into existing object
OpenClone.copyTo(source, target);

// Clone with policy
User lenient = OpenClone.cloneWith(user, ClonePolicy.LENIENT);
```

## API Reference — `OpenClone`

Main facade class. All methods are `static` and thread-safe.

### Deep Clone

| Method | Description |
|--------|-------------|
| `<T> T clone(T original)` | Deep clone using default reflective strategy |
| `<T> T clone(T original, Cloner cloner)` | Deep clone using a specific cloner |
| `<T> T cloneBySerialization(T original)` | Deep clone via Java serialization (requires `Serializable`) |
| `<T> T cloneByUnsafe(T original)` | Deep clone via Unsafe (highest performance, no constructor call) |
| `<T> T cloneWith(T original, ClonePolicy policy)` | Deep clone with a specific policy |

### Shallow Clone & Copy

| Method | Description |
|--------|-------------|
| `<T> T shallowClone(T original)` | Shallow copy — field references are shared, not deep cloned |
| `<T> T copyTo(T source, T target)` | Copy all non-null fields from source to target (deep clones values) |

### Batch & Async

| Method | Description |
|--------|-------------|
| `<T> List<T> cloneBatch(List<T> originals)` | Clone a list of objects sequentially |
| `<T> List<T> cloneBatchParallel(List<T> originals, int parallelism)` | Clone in parallel using virtual threads |
| `<T> CompletableFuture<T> cloneAsync(T original)` | Async deep clone |
| `<T> CompletableFuture<List<T>> cloneBatchAsync(List<T> originals)` | Async batch clone |

### Utility

| Method | Description |
|--------|-------------|
| `boolean isImmutable(Class<?> type)` | Check if a type is registered as immutable |
| `void registerImmutable(Class<?>... types)` | Register custom immutable types (will not be cloned) |
| `Cloner getDefaultCloner()` | Get the default ReflectiveCloner instance |
| `ClonerBuilder builder()` | Create a builder for custom Cloner configuration |

## API Reference — `ClonerBuilder`

Fluent builder for creating configured `Cloner` instances.

```java
Cloner cloner = OpenClone.builder()
    .reflective()                                          // or .serializing() or .unsafe()
    .maxDepth(50)                                          // default: 100
    .cloneTransient(true)                                  // default: false
    .policy(ClonePolicy.LENIENT)                           // default: STANDARD
    .filter(FieldFilter.excludeNames("password", "token")) // field exclusion
    .listener(myListener)                                  // lifecycle hooks
    .registerImmutable(Money.class)                        // custom immutable types
    .registerHandler(MyType.class, myHandler)              // custom type handler
    .build();
```

## API Reference — `ClonePolicy`

| Value | Behavior |
|-------|----------|
| `STANDARD` | Default. Throws exception on uncloneable types |
| `STRICT` | Forbids Unsafe fallback. All fields must be accessible via reflection |
| `LENIENT` | Best effort. Skips errors, uses shallow reference for uncloneable types, records warnings |

## API Reference — `FieldFilter`

Functional interface for programmatic field exclusion. Composable via `and()`, `or()`, `negate()`.

| Factory Method | Description |
|----------------|-------------|
| `FieldFilter.acceptAll()` | Accepts all fields (no filtering) |
| `FieldFilter.excludeNames(String... names)` | Excludes fields by name |
| `FieldFilter.includeNames(String... names)` | Includes only matching field names |
| `FieldFilter.excludeTypes(Class<?>... types)` | Excludes fields by declared type |
| `FieldFilter.excludeAnnotated(Class<? extends Annotation>)` | Excludes annotated fields |

```java
// Compose filters
FieldFilter filter = FieldFilter.excludeNames("password")
    .and(FieldFilter.excludeTypes(InputStream.class));
```

## API Reference — `CloneListener`

Interface for clone lifecycle hooks. All methods are `default` (no-op). Listener exceptions are isolated and do not affect the clone flow.

| Method | Description |
|--------|-------------|
| `void beforeClone(Object original, CloneContext context)` | Called before each object is cloned |
| `void afterClone(Object original, Object cloned, CloneContext context)` | Called after successful clone |
| `void onError(Object original, Throwable error, CloneContext context)` | Called when clone fails |

## Annotations

| Annotation | Target | Description |
|------------|--------|-------------|
| `@CloneDeep` | Field | Force deep clone for this field (default behavior) |
| `@CloneIgnore` | Field | Skip this field during cloning (set to `null`) |
| `@CloneReference` | Field | Copy by reference only (shallow, for shared resources) |

```java
public class User {
    private String name;                    // deep cloned (default)
    @CloneIgnore private String password;   // set to null in clone
    @CloneReference private Logger logger;  // same reference shared
}
```

## Type Handlers

| Handler | Supported Types | Priority |
|---------|----------------|----------|
| `EnumHandler` | All `enum` types | 5 |
| `ArrayHandler` | Primitive and object arrays | 10 |
| `RecordHandler` | Java `record` types | 15 |
| `OptionalHandler` | `Optional<T>` | 15 |
| `CollectionHandler` | ArrayList, LinkedList, HashSet, TreeSet, ArrayDeque, etc. | 20 |
| `MapHandler` | HashMap, LinkedHashMap, TreeMap, ConcurrentHashMap, etc. | 20 |

## Custom Type Handler

```java
import cloud.opencode.base.deepclone.handler.TypeHandler;

public class MoneyHandler implements TypeHandler<Money> {
    @Override
    public Money clone(Money original, Cloner cloner, CloneContext context) {
        return new Money(original.getAmount(), original.getCurrency());
    }

    @Override
    public boolean supports(Class<?> type) {
        return Money.class.isAssignableFrom(type);
    }
}

// Register
Cloner cloner = OpenClone.builder()
    .registerHandler(Money.class, new MoneyHandler())
    .build();
```

## Custom Clone Logic via `DeepCloneable`

```java
import cloud.opencode.base.deepclone.contract.DeepCloneable;

public class Config implements DeepCloneable<Config> {
    private Map<String, String> settings;

    @Override
    public Config deepClone() {
        Config copy = new Config();
        copy.settings = new HashMap<>(this.settings);
        return copy;
    }
}
```

## API Overview

| Category | Classes |
|----------|---------|
| **Facade** | `OpenClone` |
| **Core** | `Cloner`, `ClonerBuilder`, `CloneContext`, `ClonePolicy`, `FieldFilter`, `CloneListener` |
| **Cloner Impl** | `AbstractCloner`, `ReflectiveCloner`, `SerializingCloner`, `UnsafeCloner` |
| **Annotations** | `@CloneDeep`, `@CloneIgnore`, `@CloneReference` |
| **Handlers** | `TypeHandler`, `ArrayHandler`, `CollectionHandler`, `MapHandler`, `RecordHandler`, `EnumHandler`, `OptionalHandler` |
| **Strategy** | `CloneStrategy`, `FieldCloneStrategy`, `TypeCloneStrategy` |
| **SPI** | `CloneStrategyProvider` |
| **Contract** | `DeepCloneable` |
| **Internal** | `ImmutableDetector` |
| **Exception** | `OpenDeepCloneException` |

## Requirements

- Java 25+

## License

Apache License 2.0

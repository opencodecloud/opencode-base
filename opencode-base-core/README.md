# OpenCode Base Core

Core utilities -- zero-dependency base library providing fundamental types, conversions, reflection, threading, and primitive array operations for JDK 25+.

## Features

- Primitive type utilities (array, boolean, char, number, math, bit, hex, base64, radix)
- Type conversion framework with extensible converter registry
- Bean utilities (copy, path access, property descriptor)
- Builder pattern support (Bean, Record, Map builders)
- Reflection utilities (field, method, constructor, modifier, record, unsafe)
- Thread utilities (virtual threads, scoped values, structured concurrency, named thread factory)
- Tuple types (Pair, Triple, Quadruple)
- Stream utilities (Optional extensions, parallel stream helpers)
- Preconditions and assertion helpers
- String joining/splitting (Joiner, Splitter)
- Range, Ordering, Stopwatch, Singleton, SPI loader
- Checked functional interfaces (Function, Consumer, Supplier, Predicate, Runnable, Callable)
- Pagination support (Page, PageRequest, Sort)
- Custom exception hierarchy (OpenException and subtypes)
- Primitive array utilities (Ints, Longs, Doubles, Floats, Shorts, Bytes, Chars, Booleans)

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

### Root Utilities

| Class | Description |
|-------|-------------|
| `OpenArray` | Comprehensive array operations for primitive and object arrays |
| `OpenBase64` | Base64 encoding and decoding utilities |
| `OpenBit` | Bitwise operation utilities |
| `OpenBoolean` | Boolean conversion and evaluation utilities |
| `OpenChar` | Character type checking and conversion utilities |
| `OpenCharset` | Charset detection and conversion utilities |
| `OpenClass` | Class metadata and type inspection utilities |
| `OpenEnum` | Enum lookup and conversion utilities |
| `OpenHex` | Hexadecimal encoding and decoding utilities |
| `OpenMath` | Mathematical operations with overflow protection |
| `OpenNumber` | Number parsing, comparison, and conversion utilities |
| `OpenObject` | Object equality, hashing, and null-safe operations |
| `OpenRadix` | Radix (base) conversion utilities |
| `OpenStream` | Stream creation and transformation utilities |
| `OpenStringBase` | Basic string operations (null-safe, blank checks, trim) |
| `Joiner` | Fluent string joiner with separator, prefix, suffix support |
| `Splitter` | Fluent string splitter with regex and limit support |
| `MoreObjects` | ToStringHelper and firstNonNull utilities |
| `Ordering` | Comparator builder with chaining and null-handling |
| `Preconditions` | Argument and state validation with descriptive messages |
| `Range` | Immutable range with open/closed/unbounded endpoints |
| `Stopwatch` | High-precision elapsed time measurement |
| `Suppliers` | Memoizing and expiring supplier wrappers |

### Annotation

| Class | Description |
|-------|-------------|
| `Experimental` | Marks an API as experimental (may change without notice) |

### Assertion

| Class | Description |
|-------|-------------|
| `OpenAssert` | Fluent assertion utility for argument validation |

### Bean

| Class | Description |
|-------|-------------|
| `OpenBean` | Bean copy, property access, and introspection facade |
| `BeanPath` | Nested property path access (e.g. `user.address.city`) |
| `PropertyConverter` | Interface for bean property type conversion |
| `PropertyDescriptor` | Bean property metadata descriptor |

### Builder

| Class | Description |
|-------|-------------|
| `Builder` | Generic builder interface |
| `OpenBuilder` | Builder factory facade |
| `BeanBuilder` | Fluent builder for JavaBean instances |
| `RecordBuilder` | Fluent builder for record instances |
| `MapBuilder` | Fluent builder for Map instances |

### Compare

| Class | Description |
|-------|-------------|
| `CompareUtil` | Generic comparison operator dispatch (EQ/NE/LT/LE/GT/GE) |

### Container

| Class | Description |
|-------|-------------|
| `ContainerUtil` | Generic size/empty operations for Collection, Map, Array, CharSequence, Optional |

### Convert

| Class | Description |
|-------|-------------|
| `Convert` | Type conversion facade |
| `Converter` | Converter interface |
| `ConverterRegistry` | Extensible converter registry |
| `TypeReference` | Generic type token for preserving type information |
| `TypeUtil` | Type resolution and inspection utilities |
| `AttributeConverter` | Bidirectional attribute conversion interface |
| `StringConverter` | String-to-target type converter |
| `NumberConverter` | Number-to-target type converter |
| `DateConverter` | Date/time type converter |
| `ArrayConverter` | Array type converter |

### Exception

| Class | Description |
|-------|-------------|
| `OpenException` | Base runtime exception for all OpenCode modules |
| `OpenIOException` | I/O related exception |
| `OpenIllegalArgumentException` | Illegal argument exception |
| `OpenIllegalStateException` | Illegal state exception |
| `OpenTimeoutException` | Timeout exception |
| `OpenUnsupportedOperationException` | Unsupported operation exception |
| `ExceptionUtil` | Exception wrapping, unwrapping, and stack trace utilities |

### Functional Interfaces

| Class | Description |
|-------|-------------|
| `CheckedFunction` | Function that may throw checked exceptions |
| `CheckedConsumer` | Consumer that may throw checked exceptions |
| `CheckedSupplier` | Supplier that may throw checked exceptions |
| `CheckedPredicate` | Predicate that may throw checked exceptions |
| `CheckedRunnable` | Runnable that may throw checked exceptions |
| `CheckedCallable` | Callable that may throw checked exceptions |

### Page

| Class | Description |
|-------|-------------|
| `Page` | Paginated result container with total count and content |
| `PageRequest` | Pagination request record (page, size, sort) |
| `Sort` | Sort specification with property name and direction |

### Primitives

| Class | Description |
|-------|-------------|
| `Ints` | int array utilities (contains, indexOf, min, max, sort, reverse) |
| `Longs` | long array utilities |
| `Doubles` | double array utilities |
| `Floats` | float array utilities |
| `Shorts` | short array utilities |
| `Bytes` | byte array utilities |
| `Chars` | char array utilities |
| `Booleans` | boolean array utilities |

### Random

| Class | Description |
|-------|-------------|
| `OpenRandom` | Secure random number generation utilities |
| `IdGenerator` | Unique ID generator interface |
| `VerifyCodeUtil` | Verification code generation utility |

### Reflect

| Class | Description |
|-------|-------------|
| `ReflectUtil` | General reflection utilities |
| `FieldUtil` | Field access and manipulation utilities |
| `MethodUtil` | Method lookup and invocation utilities |
| `ConstructorUtil` | Constructor lookup and instantiation utilities |
| `ModifierUtil` | Modifier inspection utilities |
| `RecordUtil` | Record component access utilities |
| `UnsafeUtil` | sun.misc.Unsafe wrapper for low-level operations |

### Singleton

| Class | Description |
|-------|-------------|
| `Singleton` | Thread-safe lazy singleton registry |

### SPI

| Class | Description |
|-------|-------------|
| `SpiLoader` | ServiceLoader wrapper with caching and ordering |

### Stream

| Class | Description |
|-------|-------------|
| `OptionalUtil` | Optional extension utilities |
| `ParallelStreamUtil` | Parallel stream execution utilities |

### Thread

| Class | Description |
|-------|-------------|
| `OpenThread` | Thread utilities (sleep, virtual thread creation) |
| `NamedThreadFactory` | ThreadFactory with custom naming pattern |
| `ScopedValueUtil` | JDK 25 ScopedValue utilities |
| `StructuredTaskUtil` | JDK 25 structured concurrency utilities |
| `ThreadLocalUtil` | ThreadLocal management utilities |

### Tuple

| Class | Description |
|-------|-------------|
| `Pair` | Immutable two-element tuple record |
| `Triple` | Immutable three-element tuple record |
| `Quadruple` | Immutable four-element tuple record |
| `TupleUtil` | Tuple creation and transformation utilities |

### Internal

| Class | Description |
|-------|-------------|
| `InternalCache` | Internal cache interface |
| `InternalLRUCache` | LRU cache implementation for internal use |

## Quick Start

```java
import cloud.opencode.base.core.*;
import cloud.opencode.base.core.tuple.*;
import cloud.opencode.base.core.primitives.*;

// Preconditions
Preconditions.checkArgument(age > 0, "age must be positive");
Preconditions.checkNotNull(name, "name");

// Type conversion
int value = Convert.toInt("42");
String str = Convert.toStr(123);

// Array operations
int[] arr = {3, 1, 4, 1, 5};
boolean has = Ints.contains(arr, 4);      // true
int idx = Ints.indexOf(arr, 5);           // 4

// Tuples
Pair<String, Integer> pair = Pair.of("Alice", 30);
Triple<String, Integer, Boolean> triple = Triple.of("Bob", 25, true);

// Builder
Map<String, Object> map = MapBuilder.<String, Object>create()
    .put("name", "Alice")
    .put("age", 30)
    .build();

// Range
Range<Integer> range = Range.closed(1, 10);
boolean contains = range.contains(5);  // true

// Stopwatch
Stopwatch sw = Stopwatch.createStarted();
// ... do work ...
System.out.println("Elapsed: " + sw.elapsed());

// String join/split
String joined = Joiner.on(", ").skipNulls().join("a", null, "b"); // "a, b"
List<String> parts = Splitter.on(",").trimResults().splitToList("a, b, c");

// Bean copy
OpenBean.copyProperties(source, target);

// Structured concurrency (JDK 25)
var result = StructuredTaskUtil.allOf(
    () -> fetchUser(id),
    () -> fetchOrders(id)
);
```

## Requirements

- Java 25+

## License

Apache License 2.0

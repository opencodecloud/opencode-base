# OpenCode Base Core

> Core utilities and foundational types for the OpenCode Base library.

Zero-dependency base library providing fundamental types, type conversions, reflection, threading,
functional error handling, virtual-thread concurrency, and primitive array operations for JDK 25+.

## Requirements

- JDK 25+
- Zero external dependencies (JSpecify is compile-only)

## Installation

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-core</artifactId>
    <version>1.0.3</version>
</dependency>
```

## Module System (JPMS)

```java
requires cloud.opencode.base.core;
```

## What's New in v1.0.3

| Feature | Description |
|---------|-------------|
| `Result<T>` | Sealed interface for functional error handling -- Success or Failure with monadic operations |
| `Either<L,R>` | Sealed interface for two-valued computations -- Left or Right with right-biased operations |
| `Lazy<T>` | Virtual-thread-safe lazy evaluation using VarHandle CAS (no `synchronized`, no pinning) |
| `VirtualTasks` | High-level concurrency utilities built on JDK 25 virtual threads |
| `OpenCollections` | Immutable collection builders (ListBuilder, MapBuilder) and set operations (union, intersection, difference) |
| `ObjectDiff` | Bean difference comparison engine with deep comparison, cycle detection, and field filtering |
| `Environment` | Runtime environment detection (JDK, OS, GraalVM, container, virtual thread) |
| `Page<T>` | **BREAKING:** Refactored to immutable record with factory methods and `map()` |
| `TriFunction` / `QuadFunction` | Three- and four-argument functional interfaces |
| JSpecify `@NullMarked` | All exported packages annotated for null-safety |
| `Retry` | General purpose retry utility with configurable backoff strategies (fixed, exponential, fibonacci, jitter), multi-exception retry, abort conditions |
| `OpenCollections` enhancements | `partition`, `groupBy`, `chunk`, `sliding`, `zip`, `zipWith`, `distinctBy`, `frequencies`, `flatten` |
| `Preconditions` enhancements | `checkPositive`, `checkNonNegative`, `checkBetween`, `checkNotBlank`, `checkNotEmpty` |
| `MoreObjects` | Note: `allNull`/`anyNull`/`defaultIfNull`/`firstNonNull(varargs)` available in `OpenObject` |
| `ExceptionUtil` enhancements | `findCause` (Optional-based), `isOrCausedBy` |
| `VirtualTasks` enhancements | `supplyAsync`, `runAsync` (CompletableFuture bridge), `parallelMap` with concurrency limit |
| `Stopwatch` enhancements | `suspend`/`resume`, `split`/`getLaps` (lap timing), `time(Callable)`/`time(Runnable)` (one-liner timing) |
| `SpiLoader` enhancements | `loadSafe` (error-isolated loading), `loadOrdered` (priority-sorted loading) |
| `Range` implements `Predicate` | Enables `stream().filter(range)` and Predicate composition (`and`/`or`/`negate`) |
| `Environment` enhancements | `pid()`, `uptime()`, `javaHome()`, `userDir()`, `tempDir()` |
| `SystemInfo` | Comprehensive system monitoring — CPU load, physical/heap/swap memory, disk usage, OS info, JVM runtime details |
| `ProcessManager` | OS process management — discovery, execution with output capture, kill/wait, process tree traversal |

---

## API Reference

### Package: `cloud.opencode.base.core`

#### OpenArray

Comprehensive array operations for primitive and object arrays.

| Method | Description |
|--------|-------------|
| `newArray(Class<T>, int)` | Create a typed array via reflection |
| `of(T...)` | Create an array from varargs |
| `nullToEmpty(T[], Class<T[]>)` | Convert null array to empty typed array |
| `nullToEmpty(int[])` / `nullToEmpty(long[])` | Convert null primitive array to empty |
| `add(T[], T)` | Append element to end of array |
| `add(T[], int, T)` | Insert element at index |
| `addAll(T[], T...)` | Concatenate two arrays |
| `insert(int, T[], T...)` | Insert elements at index |
| `insert(int, int[], int...)` | Insert int elements at index |
| `remove(T[], int)` | Remove element at index |
| `removeElement(T[], T)` | Remove first occurrence of element |
| `removeAll(T[], int...)` | Remove elements at multiple indices |
| `subarray(T[], int, int)` | Extract subarray (also int[], long[], byte[] overloads) |
| `isEmpty(T[])` / `isNotEmpty(T[])` | Check array emptiness |
| `contains(T[], T)` | Check if array contains element |
| `indexOf(T[], T)` | Find first index of element |
| `lastIndexOf(T[], T)` | Find last index of element |
| `reverse(T[])` | Reverse array in-place |
| `swap(T[], int, int)` | Swap two elements |
| `shift(T[], int)` | Circular shift elements |
| `isSorted(T[])` | Check if array is sorted |
| `distinct(T[])` | Remove duplicate elements |
| `toList(T[])` / `toSet(T[])` | Convert to Collection |
| `wrap(int[])` / `unwrap(Integer[])` | Box/unbox primitive arrays |

#### OpenBase64

Base64 encoding and decoding utilities.

| Method | Description |
|--------|-------------|
| `encode(byte[])` | Encode to Base64 string |
| `encodeUrlSafe(byte[])` | Encode using URL-safe Base64 |
| `decode(String)` | Decode Base64 string to bytes |
| `decodeUrlSafe(String)` | Decode URL-safe Base64 string |
| `encodeToString(byte[])` | Encode with standard alphabet |
| `isBase64(String)` | Check if string is valid Base64 |

#### OpenBit

Bitwise operation utilities.

| Method | Description |
|--------|-------------|
| `setBit(int, int)` / `clearBit(int, int)` | Set/clear a specific bit |
| `testBit(int, int)` | Test if a bit is set |
| `toggleBit(int, int)` | Toggle a specific bit |
| `highestOneBit(int)` / `lowestOneBit(int)` | Find highest/lowest set bit |
| `bitCount(int)` / `bitCount(long)` | Count set bits |
| `isPowerOfTwo(int)` / `isPowerOfTwo(long)` | Check if value is power of 2 |
| `nextPowerOfTwo(int)` | Find next power of 2 |
| `toBinaryString(int)` / `toBinaryString(long)` | Convert to binary string |

#### OpenBoolean

Boolean conversion and evaluation utilities.

| Method | Description |
|--------|-------------|
| `toBoolean(String)` | Parse string to boolean (supports "yes", "1", "true", etc.) |
| `toBoolean(int)` | Convert int to boolean (0 = false) |
| `toInt(boolean)` | Convert boolean to int |
| `negate(Boolean)` | Null-safe negate |
| `isTrue(Boolean)` / `isFalse(Boolean)` | Null-safe boolean check |
| `and(boolean...)` / `or(boolean...)` | Logical operations on arrays |

#### OpenChar

Character type checking and conversion utilities.

| Method | Description |
|--------|-------------|
| `isAscii(char)` / `isAsciiPrintable(char)` | ASCII checks |
| `isLetter(char)` / `isDigit(char)` / `isLetterOrDigit(char)` | Type checks |
| `isUpperCase(char)` / `isLowerCase(char)` | Case checks |
| `toUpperCase(char)` / `toLowerCase(char)` | Case conversion |
| `isBlank(char)` | Check if whitespace |
| `isChinese(char)` | Check if Chinese character |
| `equals(char, char, boolean)` | Compare with optional case-insensitivity |

#### OpenCharset

Charset detection and conversion utilities.

| Method | Description |
|--------|-------------|
| `defaultCharset()` | Get system default charset |
| `forName(String)` | Get charset by name (null-safe) |
| `convert(String, String, String)` | Convert string between charsets |
| `isSupported(String)` | Check if charset name is supported |

#### OpenClass

Class metadata and type inspection utilities.

| Method | Description |
|--------|-------------|
| `getClass(String)` / `loadClass(String)` | Load class by name |
| `getClassName(Object)` | Get class name of object |
| `isAssignable(Class, Class)` | Check assignability |
| `isPrimitive(Class)` / `isPrimitiveWrapper(Class)` | Type checks |
| `getDefaultValue(Class)` | Get default value for type |
| `getInterfaces(Class)` | Get all implemented interfaces |
| `getSuperClasses(Class)` | Get superclass chain |
| `isRecord(Class)` / `isSealed(Class)` | JDK 25 type checks |
| `isInnerClass(Class)` | Check if inner/nested class |

#### OpenEnum

Enum lookup and conversion utilities.

| Method | Description |
|--------|-------------|
| `valueOf(Class, String)` | Case-sensitive enum lookup |
| `valueOfIgnoreCase(Class, String)` | Case-insensitive enum lookup |
| `getEnumMap(Class)` | Get name-to-enum map |
| `getEnumList(Class)` | Get all enum constants as list |
| `contains(Class, String)` | Check if name exists in enum |

#### OpenHex

Hexadecimal encoding and decoding utilities.

| Method | Description |
|--------|-------------|
| `encodeHex(byte[])` | Encode bytes to hex char array |
| `encodeHexStr(byte[])` | Encode bytes to hex string |
| `decodeHex(String)` / `decodeHex(char[])` | Decode hex to bytes |
| `isHex(char)` | Check if character is hex digit |
| `toDigit(char)` | Convert hex char to int value |

#### OpenMath

Mathematical operations with overflow protection.

| Method | Description |
|--------|-------------|
| `add(int, int)` / `add(long, long)` | Addition with overflow check (Math.addExact) |
| `subtract(int, int)` / `subtract(long, long)` | Subtraction with overflow check |
| `multiply(int, int)` / `multiply(long, long)` | Multiplication with overflow check |
| `divide(int, int)` / `divide(double, double)` | Safe division |
| `pow(double, double)` | Power function |
| `sqrt(double)` | Square root |
| `abs(int)` / `abs(long)` / `abs(double)` | Absolute value |
| `max(int...)` / `min(int...)` | Varargs min/max |
| `sum(int...)` / `sum(long...)` | Sum with overflow protection |
| `average(int...)` / `average(double...)` | Arithmetic average |
| `clamp(int, int, int)` | Clamp value to range |
| `gcd(int, int)` / `lcm(int, int)` | Greatest common divisor / least common multiple |
| `isPrime(long)` | Primality test |
| `factorial(int)` | Factorial computation |
| `fibonacci(int)` | Fibonacci number |
| `round(double, int)` | Round to decimal places |
| `ceil(double)` / `floor(double)` | Ceiling/floor |

#### OpenNumber

Number parsing, comparison, and conversion utilities.

| Method | Description |
|--------|-------------|
| `isNumber(String)` / `isInteger(String)` | Number format checks |
| `parseInt(String, int)` | Parse int with default |
| `parseLong(String, long)` | Parse long with default |
| `parseDouble(String, double)` | Parse double with default |
| `compare(Number, Number)` | Generic number comparison |
| `toBigDecimal(Number)` | Convert to BigDecimal |
| `toInt(Number)` / `toLong(Number)` | Convert number types |
| `isPositive(Number)` / `isNegative(Number)` / `isZero(Number)` | Sign checks |

#### OpenObject

Object equality, hashing, and null-safe operations.

| Method | Description |
|--------|-------------|
| `equal(Object, Object)` | Null-safe equals |
| `hashCode(Object...)` | Multi-value hash code |
| `toString(Object)` / `toString(Object, String)` | Null-safe toString |
| `defaultIfNull(T, T)` | Return default if null |
| `requireNonNull(T, String)` | Require non-null with message |
| `clone(T)` | Deep clone via serialization |
| `compare(T, T, Comparator)` | Null-safe compare |

#### OpenRadix

Radix (base) conversion utilities.

| Method | Description |
|--------|-------------|
| `toBase(long, int)` | Convert to arbitrary base string |
| `fromBase(String, int)` | Parse arbitrary base string |
| `toBinary(long)` / `toOctal(long)` / `toHex(long)` | Common base conversions |
| `fromBinary(String)` / `fromOctal(String)` / `fromHex(String)` | Parse common bases |
| `encode62(long)` / `decode62(String)` | Base62 encoding/decoding |

#### OpenStream

Stream creation and transformation utilities.

| Method | Description |
|--------|-------------|
| `of(T...)` | Create stream from varargs |
| `ofNullable(T)` | Create stream from nullable value |
| `concat(Stream...)` | Concatenate multiple streams |
| `zip(Stream, Stream, BiFunction)` | Zip two streams |
| `distinct(Stream, Function)` | Distinct by key |
| `batched(Stream, int)` | Batch stream into lists |

#### OpenStringBase

Basic string operations (null-safe, blank checks, trim).

| Method | Description |
|--------|-------------|
| `isEmpty(CharSequence)` / `isNotEmpty(CharSequence)` | Null-safe empty check |
| `isBlank(CharSequence)` / `isNotBlank(CharSequence)` | Null-safe blank check |
| `trim(String)` / `strip(String)` | Null-safe trim/strip |
| `defaultIfEmpty(String, String)` | Default if empty |
| `defaultIfBlank(String, String)` | Default if blank |
| `nullToEmpty(String)` / `emptyToNull(String)` | Null/empty conversion |
| `truncate(String, int)` / `truncateMiddle(String, int)` | Truncate with ellipsis |
| `repeat(String, int)` | Repeat string N times |
| `reverse(String)` | Reverse string |
| `capitalize(String)` / `uncapitalize(String)` | Case first letter |
| `contains(String, String)` / `containsIgnoreCase(String, String)` | Substring search |
| `startsWith(String, String)` / `endsWith(String, String)` | Prefix/suffix check |
| `removePrefix(String, String)` / `removeSuffix(String, String)` | Remove prefix/suffix |
| `padLeft(String, int, char)` / `padRight(String, int, char)` | Pad string |
| `countOccurrences(String, String)` | Count substring occurrences |
| `substringBefore(String, String)` / `substringAfter(String, String)` | Substring extraction |

#### Joiner

Fluent string joiner with separator, prefix, suffix support.

| Method | Description |
|--------|-------------|
| `on(String)` | Create Joiner with separator |
| `on(char)` | Create Joiner with char separator |
| `skipNulls()` | Skip null elements |
| `useForNull(String)` | Replace null with default string |
| `withPrefix(String)` / `withSuffix(String)` | Add prefix/suffix |
| `join(Object...)` | Join varargs |
| `join(Iterable)` | Join iterable |
| `join(Iterator)` | Join iterator |
| `appendTo(StringBuilder, Object...)` | Append to existing builder |

#### Splitter

Fluent string splitter with regex and limit support.

| Method | Description |
|--------|-------------|
| `on(String)` | Create Splitter with separator |
| `on(char)` | Create Splitter with char separator |
| `onPattern(String)` | Create Splitter with regex pattern |
| `trimResults()` | Trim each result |
| `omitEmptyStrings()` | Remove empty strings |
| `limit(int)` | Limit number of parts |
| `split(String)` | Split to iterable |
| `splitToList(String)` | Split to list |
| `splitToStream(String)` | Split to stream |

#### MoreObjects

ToStringHelper and firstNonNull utilities.

| Method | Description |
|--------|-------------|
| `toStringHelper(Object)` / `toStringHelper(Class)` | Create a ToStringHelper |
| `firstNonNull(T, T)` | Return first non-null argument |
| `ToStringHelper.add(String, Object)` | Add named value |
| `ToStringHelper.addValue(Object)` | Add unnamed value |
| `ToStringHelper.omitNullValues()` | Skip null values |
| `ToStringHelper.toString()` | Build the string |

#### Ordering

Comparator builder with chaining and null-handling.

| Method | Description |
|--------|-------------|
| `natural()` | Natural ordering comparator |
| `from(Comparator)` | Create from existing comparator |
| `reverse()` | Reverse the ordering |
| `nullsFirst()` / `nullsLast()` | Null handling |
| `onResultOf(Function)` | Transform before comparing |
| `compound(Comparator)` | Secondary comparator |
| `min(T, T)` / `max(T, T)` | Min/max of two values |
| `min(Iterable)` / `max(Iterable)` | Min/max of collection |
| `sortedCopy(Iterable)` | Return sorted copy |
| `isOrdered(Iterable)` | Check if already sorted |

#### Preconditions

Argument and state validation with descriptive messages.

| Method | Description |
|--------|-------------|
| `checkArgument(boolean, String, Object...)` | Validate argument condition |
| `checkState(boolean, String, Object...)` | Validate state condition |
| `checkNotNull(T, String, Object...)` | Require non-null |
| `checkElementIndex(int, int)` | Validate element index |
| `checkPositionIndex(int, int)` | Validate position index |
| `checkPositive(int/long, String)` | Validate value > 0, returns value |
| `checkNonNegative(int/long, String)` | Validate value >= 0, returns value |
| `checkBetween(int/long, min, max, String)` | Validate value in [min, max], returns value |
| `checkNotBlank(String, String)` | Validate non-null, non-blank string, returns value |
| `checkNotEmpty(Collection, String)` | Validate non-null, non-empty collection, returns it |
| `checkNotEmpty(Map, String)` | Validate non-null, non-empty map, returns it |

#### Range

Immutable range with open/closed/unbounded endpoints.

| Method | Description |
|--------|-------------|
| `closed(C, C)` | Closed range [a, b] |
| `open(C, C)` | Open range (a, b) |
| `closedOpen(C, C)` | Half-open [a, b) |
| `openClosed(C, C)` | Half-open (a, b] |
| `atLeast(C)` / `atMost(C)` | Unbounded ranges |
| `greaterThan(C)` / `lessThan(C)` | Exclusive unbounded |
| `all()` | Universal range |
| `contains(C)` | Test membership |
| `test(C)` | Predicate support — delegates to `contains()` |
| `encloses(Range)` | Test if range encloses another |
| `isConnected(Range)` | Test if ranges are connected |
| `intersection(Range)` | Compute intersection |
| `span(Range)` | Compute minimal enclosing range |
| `gap(Range)` | Compute gap between disconnected ranges |

Range implements `Predicate<C>`, enabling:
```java
Range<Integer> range = Range.closed(1, 100);
List<Integer> filtered = numbers.stream().filter(range).toList();
Predicate<Integer> evenInRange = range.and(n -> n % 2 == 0);
```

#### Stopwatch

High-precision elapsed time measurement.

| Method | Description |
|--------|-------------|
| `createStarted()` | Create and start a stopwatch |
| `createUnstarted()` | Create without starting |
| `start()` / `stop()` / `reset()` | Control methods |
| `suspend()` / `resume()` | Pause/resume without resetting |
| `split()` | Record lap time, return Duration since last split |
| `getLaps()` | Get all recorded lap durations |
| `elapsed()` | Get elapsed Duration |
| `elapsed(TimeUnit)` | Get elapsed in specified unit |
| `isRunning()` | Check if running |
| `time(Callable<T>)` | One-liner: returns Pair&lt;T, Duration&gt; |
| `time(Runnable)` | One-liner: returns Duration |

#### Suppliers

Memoizing and expiring supplier wrappers.

| Method | Description |
|--------|-------------|
| `memoize(Supplier)` | Cache supplier result forever |
| `memoizeWithExpiration(Supplier, long, TimeUnit)` | Cache with TTL |
| `ofInstance(T)` | Supplier returning fixed value |
| `compose(Supplier, Function)` | Transform supplier output |

#### Lazy **[NEW in v1.0.3]**

Virtual-thread-safe lazy evaluation container using VarHandle CAS.

| Method | Description |
|--------|-------------|
| `of(Supplier<T>)` | Create Lazy from Supplier |
| `of(CheckedSupplier<T>)` | Create Lazy from CheckedSupplier (wraps checked exceptions) |
| `value(T)` | Create already-evaluated Lazy |
| `get()` | Get value (compute if needed, thread-safe via CAS) |
| `isEvaluated()` | Check if value has been computed |
| `map(Function)` | Lazy transform value |
| `flatMap(Function)` | Lazy flatMap to another Lazy |
| `filter(Predicate)` | Lazy filter (throws NoSuchElementException if not matched) |
| `getOrElse(T)` | Get value or default on error |
| `getOrElse(Supplier)` | Get value or compute default on error |
| `toOptional()` | Convert to Optional |
| `reset()` | **[Experimental]** Reset to unevaluated state |

#### Environment **[NEW in v1.0.3]**

Runtime environment detection (JDK, OS, GraalVM, container, virtual thread).

| Method | Description |
|--------|-------------|
| `javaVersion()` | Get Java feature version number (e.g. 25) |
| `javaVendor()` | Get Java vendor string |
| `isJavaVersionAtLeast(int)` | Check minimum Java version |
| `osName()` | Get OS name |
| `isWindows()` / `isLinux()` / `isMacOS()` | OS type checks |
| `isGraalVmNativeImage()` | Check if running as GraalVM native image |
| `isContainer()` | Best-effort container detection (Docker, K8s, cgroup) |
| `isVirtualThread()` | Check if current thread is virtual |
| `availableProcessors()` | Available CPUs (live, not cached) |
| `maxMemory()` / `totalMemory()` / `freeMemory()` | Memory metrics (live) |
| `pid()` | Current JVM process ID |
| `uptime()` | JVM uptime as Duration |
| `javaHome()` | Java installation directory |
| `userDir()` | Current working directory |
| `tempDir()` | System temporary directory |

---

### Package: `cloud.opencode.base.core.result` **[NEW in v1.0.3]**

#### Result\<T\>

Sealed interface for functional error handling -- Success or Failure.

| Method | Description |
|--------|-------------|
| `of(CheckedSupplier<T>)` | Create Result by executing supplier, catching exceptions as Failure |
| `success(T)` | Create Success |
| `failure(Throwable)` | Create Failure |
| `successVoid()` | Create Success\<Void\> for side-effect-only operations |
| `isSuccess()` / `isFailure()` | State queries |
| `map(Function)` | Transform success value (auto-catches exceptions) |
| `flatMap(Function)` | Transform to another Result |
| `recover(Function)` | Recover from Failure with a value |
| `recoverWith(Function)` | Recover from Failure with another Result |
| `peek(Consumer)` | Side-effect on success |
| `peekFailure(Consumer)` | Side-effect on failure |
| `getOrElse(T)` | Get value or default |
| `getOrElseGet(Supplier)` | Get value or compute default |
| `getOrElseThrow(Function)` | Get value or throw mapped exception |
| `toOptional()` | Convert to Optional |
| `stream()` | Convert to Stream |

**Inner types:**
- `Result.Success<T>(T value)` -- record for successful result
- `Result.Failure<T>(Throwable cause)` -- record for failed result

```java
// Pattern matching with switch (JDK 25)
Result<String> result = Result.of(() -> Files.readString(path));
switch (result) {
    case Result.Success(var v) -> process(v);
    case Result.Failure(var e) -> handleError(e);
}

// Chaining operations
Result<Integer> length = result.map(String::trim).map(String::length);

// Recovery
String value = result.recover(ex -> "default").getOrElse("fallback");
```

#### Either\<L, R\>

Sealed interface for two-valued computations -- Left or Right (right-biased).

| Method | Description |
|--------|-------------|
| `left(L)` | Create Left Either |
| `right(R)` | Create Right Either |
| `isLeft()` / `isRight()` | State queries |
| `getLeft()` / `getRight()` | Get value as Optional |
| `map(Function)` | Transform Right value |
| `flatMap(Function)` | Transform Right to another Either |
| `mapLeft(Function)` | Transform Left value |
| `bimap(Function, Function)` | Transform both sides |
| `getOrElse(R)` | Get Right or default |
| `orElse(Either)` | This or alternative if Left |
| `fold(Function, Function)` | Fold both cases to a single result |
| `swap()` | Swap Left and Right |
| `peek(Consumer)` | Side-effect on Right |
| `peekLeft(Consumer)` | Side-effect on Left |
| `toOptional()` | Convert Right to Optional |
| `stream()` | Convert Right to Stream |
| `toResult()` | Convert to Result (Right=Success, Left=Failure) |
| `toResult(Function)` | Convert to Result with custom Left-to-Throwable mapping |

**Inner types:**
- `Either.Left<L, R>(L value)` -- record for left case
- `Either.Right<L, R>(R value)` -- record for right case

```java
// Pattern matching with switch (JDK 25)
Either<String, User> either = findUser(id);
switch (either) {
    case Either.Left(var err)  -> handleError(err);
    case Either.Right(var val) -> handleSuccess(val);
}

// Right-biased chaining
findUser(1L)
    .map(User::getName)
    .fold(err -> log.error(err), name -> log.info("Found: " + name));
```

---

### Package: `cloud.opencode.base.core.concurrent` **[NEW in v1.0.3]**

#### VirtualTasks

High-level concurrency primitives built on JDK 25 virtual threads.

| Method | Description |
|--------|-------------|
| `invokeAll(List<Callable<T>>)` | All-or-nothing: all succeed or throw on first failure |
| `invokeAll(List<Callable<T>>, Duration)` | invokeAll with timeout |
| `invokeAny(List<Callable<T>>)` | First success wins, cancel rest |
| `invokeAny(List<Callable<T>>, Duration)` | invokeAny with timeout |
| `invokeAllSettled(List<Callable<T>>)` | Collect all as Result (never throws from task failures) |
| `invokeAllSettled(List<Callable<T>>, Duration)` | invokeAllSettled with timeout |
| `parallelMap(List<T>, Function<T,R>)` | Parallel mapping over a list |
| `parallelMap(List<T>, Function<T,R>, Duration)` | parallelMap with timeout |
| `runAll(List<Runnable>)` | Run all tasks to completion |
| `runAll(List<Runnable>, Duration)` | runAll with timeout |
| `supplyAsync(Callable<T>)` | Execute on virtual thread, return CompletableFuture |
| `runAsync(Runnable)` | Run on virtual thread, return CompletableFuture&lt;Void&gt; |
| `parallelMap(List<T>, Function, int)` | Parallel map with concurrency limit |
| `parallelMap(List<T>, Function, int, Duration)` | Concurrency-limited parallelMap with timeout |

```java
// All-or-nothing
List<String> results = VirtualTasks.invokeAll(List.of(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
));

// First success wins
String fastest = VirtualTasks.invokeAny(List.of(
    () -> queryMirror1(),
    () -> queryMirror2()
));

// Collect all (success or failure)
List<Result<String>> settled = VirtualTasks.invokeAllSettled(List.of(
    () -> riskyOperation1(),
    () -> riskyOperation2()
));

// Parallel map with timeout
List<Integer> lengths = VirtualTasks.parallelMap(
    urls, url -> download(url).length(), Duration.ofSeconds(30)
);
```

---

### Package: `cloud.opencode.base.core.collect` **[NEW in v1.0.3]**

#### OpenCollections

Unmodifiable collection factory and utility methods.

| Method | Description |
|--------|-------------|
| `listBuilder()` / `listBuilder(int)` | Create a ListBuilder for incremental list building |
| `mapBuilder()` | Create a MapBuilder for incremental map building |
| `append(List<T>, T)` | New list with element appended |
| `prepend(T, List<T>)` | New list with element prepended |
| `concat(List<T>, List<T>)` | Concatenate two lists |
| `without(List<T>, T)` | New list with first occurrence removed |
| `withReplaced(List<T>, int, T)` | New list with element replaced at index |
| `union(Set<T>, Set<T>)` | Set union |
| `intersection(Set<T>, Set<T>)` | Set intersection |
| `difference(Set<T>, Set<T>)` | Set difference (a \\ b) |
| `partition(List<T>, Predicate)` | Split list into true/false groups |
| `groupBy(List<T>, Function)` | Group elements by classifier |
| `chunk(List<T>, int)` | Split list into fixed-size chunks |
| `sliding(List<T>, int)` | Sliding windows of given size |
| `zip(List<A>, List<B>)` | Zip two lists into Pair list |
| `zipWith(List<A>, List<B>, BiFunction)` | Zip with combiner function |
| `distinctBy(List<T>, Function)` | Deduplicate by key extractor |
| `frequencies(Collection<T>)` | Element frequency map |
| `flatten(List<List<T>>)` | Flatten nested lists |
| `toUnmodifiableList()` | Collector producing unmodifiable List |
| `toUnmodifiableSet()` | Collector producing unmodifiable Set |

**Inner types:**
- `OpenCollections.ListBuilder<T>` -- `add(T)`, `addAll(Iterable)`, `build()`
- `OpenCollections.MapBuilder<K,V>` -- `put(K,V)`, `putAll(Map)`, `build()`

---

### Package: `cloud.opencode.base.core.retry` **[NEW in v1.0.3]**

#### Retry

General purpose retry utility with configurable backoff strategies.

| Class / Method | Description |
|--------|-------------|
| `Retry.of(Callable<T>)` | Create a fluent retry builder |
| `.maxAttempts(int)` | Set max retry attempts (default: 3) |
| `.backoff(BackoffStrategy)` | Set backoff strategy |
| `.delay(Duration)` | Shorthand for fixed backoff |
| `.exponentialBackoff(Duration, double)` | Shorthand for exponential backoff |
| `.maxDelay(Duration)` | Cap the maximum delay |
| `.retryOn(Predicate<Throwable>)` | Predicate-based retry filtering |
| `.retryOn(Class)` | Retry only on specific exception type |
| `.retryOnAny(Class...)` | Retry on any of multiple exception types |
| `.abortOn(Class)` | Abort retry immediately on specific exception type (takes precedence over `retryOn`) |
| `.abortIf(Predicate<Throwable>)` | Abort retry immediately when predicate matches (takes precedence over `retryOn`) |
| `.onRetry(BiConsumer)` | Callback on each retry |
| `.execute()` | Execute with retry logic |
| `Retry.execute(Callable)` | Static convenience (3 attempts, 100ms) |
| `BackoffStrategy.fixed(Duration)` | Constant delay between retries |
| `BackoffStrategy.exponential(Duration, double)` | Exponential growth delay |
| `BackoffStrategy.exponentialWithJitter(...)` | Exponential with random jitter |
| `BackoffStrategy.fibonacci(Duration)` | Fibonacci sequence delay |

```java
// Simple retry with defaults
String data = Retry.execute(() -> fetchData());

// Builder pattern with exponential backoff
String result = Retry.of(() -> httpClient.get(url))
    .maxAttempts(5)
    .exponentialBackoff(Duration.ofMillis(200), 2.0)
    .retryOn(IOException.class)
    .maxDelay(Duration.ofSeconds(30))
    .execute();

// Retry on multiple exception types
String result = Retry.of(() -> httpClient.get(url))
    .retryOnAny(IOException.class, TimeoutException.class)
    .maxAttempts(3)
    .execute();

// Abort immediately on non-retryable exceptions
String result = Retry.of(() -> httpClient.post(url, body))
    .maxAttempts(5)
    .retryOn(IOException.class)
    .abortOn(IllegalArgumentException.class)
    .execute();
```

---

### Package: `cloud.opencode.base.core.annotation`

| Class | Description |
|-------|-------------|
| `Experimental` | Marks an API as experimental (may change without notice) |

---

### Package: `cloud.opencode.base.core.assertion`

#### OpenAssert

Fluent assertion utility for argument validation.

| Method | Description |
|--------|-------------|
| `notNull(T, String)` | Assert not null |
| `isTrue(boolean, String)` / `isFalse(boolean, String)` | Assert boolean condition |
| `state(boolean, String)` | Assert state condition |
| `notEmpty(CharSequence, String)` | Assert CharSequence not empty |
| `notBlank(CharSequence, String)` | Assert CharSequence not blank |
| `matchesPattern(CharSequence, String, String)` | Assert regex match |
| `notEmpty(Collection, String)` | Assert collection not empty |
| `notEmpty(Map, String)` | Assert map not empty |
| `notEmpty(T[], String)` | Assert array not empty |
| `noNullElements(T[], String)` | Assert no null elements in array |
| `noNullElements(Iterable, String)` | Assert no null elements in iterable |
| `inclusiveBetween(T, T, T)` | Assert value in [start, end] |
| `exclusiveBetween(T, T, T)` | Assert value in (start, end) |
| `validIndex(int, int)` | Assert valid array/list index |

---

### Package: `cloud.opencode.base.core.bean`

| Class | Description |
|-------|-------------|
| `OpenBean` | Bean copy, property access, and introspection facade |
| `BeanPath` | Nested property path access (e.g. `user.address.city`) |
| `PropertyConverter` | Interface for bean property type conversion |
| `PropertyDescriptor` | Bean property metadata descriptor |
| `ObjectDiff` **[v1.0.3]** | Object difference comparison engine |
| `ObjectDiff.ObjectDiffBuilder` **[v1.0.3]** | Builder for advanced diff comparison |
| `DiffResult<T>` **[v1.0.3]** | Comparison result record |
| `Diff<T>` **[v1.0.3]** | Single property difference record |
| `ChangeType` **[v1.0.3]** | Change type enum: ADDED, REMOVED, MODIFIED, UNCHANGED, CIRCULAR_REFERENCE |

#### ObjectDiff **[NEW in v1.0.3]**

| Method | Description |
|--------|-------------|
| `compare(T, T)` | Simple shallow property comparison |
| `builder(T, T)` | Create advanced diff builder |

#### ObjectDiff.ObjectDiffBuilder **[NEW in v1.0.3]**

| Method | Description |
|--------|-------------|
| `deep(boolean)` | Enable/disable deep recursive comparison |
| `maxDepth(int)` | Set maximum recursion depth (default: 10) |
| `maxCollectionSize(int)` | Set max collection size for comparison |
| `include(String...)` | Whitelist fields to compare |
| `exclude(String...)` | Blacklist fields from comparison |
| `collectionDiff(boolean)` | Enable element-level collection diff |
| `compare()` | Execute comparison and return DiffResult |

#### DiffResult\<T\> **[NEW in v1.0.3]**

| Method | Description |
|--------|-------------|
| `type()` | The class of compared objects |
| `diffs()` | All property diffs |
| `hasDiffs()` | True if any non-UNCHANGED diffs |
| `getModified()` | Only MODIFIED diffs |
| `getAdded()` | Only ADDED diffs |
| `getRemoved()` | Only REMOVED diffs |

```java
// Simple compare
DiffResult<User> result = ObjectDiff.compare(oldUser, newUser);
if (result.hasDiffs()) {
    result.getModified().forEach(d ->
        System.out.println(d.fieldName() + ": " + d.oldValue() + " -> " + d.newValue()));
}

// Advanced compare with builder
DiffResult<User> result = ObjectDiff.builder(oldUser, newUser)
    .deep(true)
    .maxDepth(5)
    .exclude("password", "internalId")
    .collectionDiff(true)
    .compare();
```

---

### Package: `cloud.opencode.base.core.builder`

| Class | Description |
|-------|-------------|
| `Builder<T>` | Generic builder interface with `build()` method |
| `OpenBuilder` | Builder factory facade |
| `BeanBuilder<T>` | Fluent builder for JavaBean instances |
| `RecordBuilder<T>` | Fluent builder for record instances |
| `MapBuilder<K,V>` | Fluent builder for Map instances |

---

### Package: `cloud.opencode.base.core.compare`

| Class | Description |
|-------|-------------|
| `CompareUtil` | Generic comparison operator dispatch (EQ, NE, LT, LE, GT, GE) |

---

### Package: `cloud.opencode.base.core.container`

| Class | Description |
|-------|-------------|
| `ContainerUtil` | Generic size/empty operations for Collection, Map, Array, CharSequence, Optional |

---

### Package: `cloud.opencode.base.core.convert`

| Class | Description |
|-------|-------------|
| `Convert` | Type conversion facade |
| `Converter<T>` | Converter interface |
| `ConverterRegistry` | Extensible converter registry |
| `TypeReference<T>` | Generic type token for preserving type information |
| `TypeUtil` | Type resolution and inspection utilities |
| `AttributeConverter<S,T>` | Bidirectional attribute conversion SPI |

---

### Package: `cloud.opencode.base.core.exception`

| Class | Description |
|-------|-------------|
| `OpenException` | Base runtime exception for all OpenCode modules |
| `OpenIOException` | I/O related exception |
| `OpenIllegalArgumentException` | Illegal argument exception |
| `OpenIllegalStateException` | Illegal state exception |
| `OpenTimeoutException` | Timeout exception |
| `OpenUnsupportedOperationException` | Unsupported operation exception |
| `ExceptionUtil` | Exception wrapping, unwrapping, and stack trace utilities |

---

### Package: `cloud.opencode.base.core.func`

| Class | Description |
|-------|-------------|
| `CheckedFunction<T,R>` | Function that may throw checked exceptions |
| `CheckedConsumer<T>` | Consumer that may throw checked exceptions |
| `CheckedSupplier<T>` | Supplier that may throw checked exceptions |
| `CheckedPredicate<T>` | Predicate that may throw checked exceptions |
| `CheckedRunnable` | Runnable that may throw checked exceptions |
| `CheckedCallable<T>` | Callable that may throw checked exceptions |
| `TriFunction<A,B,C,R>` | Three-argument function |
| `QuadFunction<A,B,C,D,R>` | Four-argument function |

---

### Package: `cloud.opencode.base.core.page`

#### Page\<T\> **[BREAKING in v1.0.3 -- now an immutable record]**

| Method | Description |
|--------|-------------|
| `of(long, long, long, List<T>)` | Create Page (current, size, total, records) |
| `empty(long)` | Create empty Page with given size |
| `current()` / `size()` / `total()` / `records()` | Record accessors |
| `pages()` | Compute total number of pages |
| `hasNext()` / `hasPrevious()` | Navigation checks |
| `offset()` | Zero-based offset: (current - 1) * size |
| `map(Function<T,U>)` | Map records preserving pagination metadata |

#### PageRequest

| Method | Description |
|--------|-------------|
| `of(long, long)` / `of(long, long, Sort)` | Create PageRequest |
| `ofSize(long)` | Create page 1 request with given size |
| `getOffset()` | Compute offset |
| `isFirst()` | Check if first page |
| `next()` / `previous()` / `first()` | Navigation |
| `withSort(Sort)` / `withPage(long)` | Copy with modification |
| `toPage()` **[v1.0.3]** | Create empty Page matching this request |

#### Sort

| Method | Description |
|--------|-------------|
| `by(String)` / `by(Direction, String)` | Create Sort |
| `by(Direction, String...)` / `by(Order...)` / `by(List<Order>)` | Create multi-field Sort |
| `unsorted()` | No sorting |
| `getOrders()` | Get sort orders |
| `isUnsorted()` | Check if unsorted |
| `and(Sort)` | Combine with another Sort |
| `toSql()` | Render as SQL fragment |

---

### Package: `cloud.opencode.base.core.primitives`

Primitive array utilities. Each class provides:

| Method | Available on |
|--------|-------------|
| `contains(arr, val)` | All types |
| `indexOf(arr, val)` / `lastIndexOf(arr, val)` | All types |
| `min(arr)` / `max(arr)` | Numeric types |
| `sum(arr)` | int, long, double, float |
| `sort(arr)` / `reverse(arr)` | All types |
| `concat(arr, arr)` | All types |
| `toArray(Collection)` | All types |
| `asList(arr)` | All types |

| Class | Type |
|-------|------|
| `Ints` | `int[]` |
| `Longs` | `long[]` |
| `Doubles` | `double[]` |
| `Floats` | `float[]` |
| `Shorts` | `short[]` |
| `Bytes` | `byte[]` |
| `Chars` | `char[]` |
| `Booleans` | `boolean[]` |

---

### Package: `cloud.opencode.base.core.random`

| Class | Description |
|-------|-------------|
| `OpenRandom` | Secure random number generation utilities |
| `IdGenerator` | Unique ID generator interface |
| `VerifyCodeUtil` | Verification code generation utility |

---

### Package: `cloud.opencode.base.core.reflect`

| Class | Description |
|-------|-------------|
| `ReflectUtil` | General reflection utilities |
| `FieldUtil` | Field access and manipulation |
| `MethodUtil` | Method lookup and invocation |
| `ConstructorUtil` | Constructor lookup and instantiation |
| `ModifierUtil` | Modifier inspection |
| `RecordUtil` | Record component access |
| `UnsafeUtil` | `sun.misc.Unsafe` wrapper for low-level operations |

---

### Package: `cloud.opencode.base.core.singleton`

| Class | Description |
|-------|-------------|
| `Singleton` | Thread-safe lazy singleton registry |

---

### Package: `cloud.opencode.base.core.spi`

#### SpiLoader

Cached SPI service loading with error isolation and priority ordering.

| Method | Description |
|--------|-------------|
| `load(Class<T>)` | Load all implementations (cached) |
| `load(Class<T>, ClassLoader)` | Load with specified ClassLoader |
| `loadFirst(Class<T>)` | Load first implementation as Optional |
| `loadFirstOrDefault(Class<T>, T)` | Load first or use default |
| `loadSafe(Class<T>)` | Load with error isolation — skip broken providers |
| `loadOrdered(Class<T>)` | Load sorted by `getPriority()`/`getOrder()` (lower = higher priority) |
| `loadByType(Class<T>, Class<S>)` | Filter by subtype |
| `loadStream(Class<T>)` | Lazy loading as Stream |
| `reload(Class<T>)` | Force reload (invalidate cache) |
| `hasService(Class<T>)` / `count(Class<T>)` | Service availability checks |
| `clearCache()` / `clearCache(Class)` | Cache management |

---

### Package: `cloud.opencode.base.core.stream`

| Class | Description |
|-------|-------------|
| `OptionalUtil` | Optional extension utilities |
| `ParallelStreamUtil` | Parallel stream execution utilities |

---

### Package: `cloud.opencode.base.core.thread`

| Class | Description |
|-------|-------------|
| `OpenThread` | Thread utilities (sleep, virtual thread creation) |
| `NamedThreadFactory` | ThreadFactory with custom naming pattern |
| `ScopedValueUtil` | JDK 25 ScopedValue utilities |
| `ThreadLocalUtil` | ThreadLocal management utilities |

---

### Package: `cloud.opencode.base.core.tuple`

| Class | Description |
|-------|-------------|
| `Pair<A,B>` | Immutable two-element tuple record |
| `Triple<A,B,C>` | Immutable three-element tuple record |
| `Quadruple<A,B,C,D>` | Immutable four-element tuple record |
| `TupleUtil` | Tuple creation and transformation utilities |

### Package: `cloud.opencode.base.core.system` **[NEW in v1.0.3]**

Comprehensive system information facade — CPU, memory, disk, OS, and JVM runtime metrics.

#### SystemInfo

| Method | Description |
|--------|-------------|
| `cpu()` | CPU info snapshot (processors, arch, load) |
| `cpuLoad()` | System-wide CPU load [0.0, 1.0], or -1 |
| `processCpuLoad()` | JVM process CPU load [0.0, 1.0], or -1 |
| `loadAverage()` | 1/5/15 min load averages (Linux: /proc/loadavg) |
| `memory()` | Physical memory info snapshot |
| `heapMemory()` | JVM heap memory info |
| `nonHeapMemory()` | JVM non-heap memory info |
| `physicalMemoryTotal()` / `physicalMemoryFree()` | Physical memory bytes |
| `swapTotal()` / `swapFree()` | Swap space bytes |
| `disks()` | All file stores info |
| `disk(Path)` | Disk info for a specific path |
| `diskTotal(Path)` / `diskFree(Path)` / `diskUsable(Path)` | Disk space bytes |
| `os()` | OS info (name, version, arch, hostname) |
| `hostname()` | Machine hostname (env var first, DNS fallback) |
| `uptime()` | JVM uptime in milliseconds |
| `environmentVariables()` | System env vars (unmodifiable) |
| `runtime()` | JVM runtime info (version, PID, uptime, args) |

#### Records

| Record | Fields |
|--------|--------|
| `CpuInfo` | `availableProcessors`, `arch`, `systemCpuLoad`, `processCpuLoad`, `loadAverage` |
| `MemoryInfo` | `total`, `used`, `free`, `max` + `usagePercent()`, `totalDisplay()` |
| `DiskInfo` | `name`, `type`, `totalSpace`, `usableSpace`, `unallocatedSpace`, `readOnly` |
| `OsInfo` | `name`, `version`, `arch`, `hostname`, `availableProcessors`, `physicalMemoryTotal`, `swapTotal` |
| `RuntimeInfo` | `javaVersion`, `javaVendor`, `javaHome`, `vmName`, `vmVersion`, `uptime`, `startTime`, `pid`, `inputArguments` |

### Package: `cloud.opencode.base.core.process` **[NEW in v1.0.3]**

Process management utility — discovery, execution, and control using JDK ProcessHandle and ProcessBuilder.

#### ProcessManager

| Method | Description |
|--------|-------------|
| `current()` | Current JVM process info |
| `find(long pid)` | Find process by PID |
| `listAll()` | List all visible processes |
| `findByName(String)` | Find by command name (case-insensitive contains) |
| `findByCommand(String)` | Find by full command line |
| `currentPid()` | Current process PID |
| `parent()` | Parent process info |
| `children()` / `children(long pid)` | Direct child processes |
| `descendants()` / `descendants(long pid)` | All descendant processes |
| `execute(String...)` | Execute command, capture stdout/stderr |
| `execute(List<String>)` | Execute command list |
| `execute(ProcessConfig)` | Execute with full configuration |
| `start(String...)` / `start(ProcessConfig)` | Start without waiting |
| `kill(long pid)` / `killForcibly(long pid)` | Terminate process |
| `isAlive(long pid)` | Check if process is alive |
| `waitFor(long pid, long timeout, TimeUnit)` | Wait for process exit |

#### ProcessConfig (Builder)

| Method | Description |
|--------|-------------|
| `builder(String...)` / `builder(List<String>)` | Create builder with command |
| `workingDirectory(Path)` | Set working directory |
| `environment(String, String)` | Add environment variable |
| `timeout(Duration)` | Set execution timeout |
| `redirectErrorStream(boolean)` | Merge stderr into stdout |
| `stdoutFile(Path)` / `stderrFile(Path)` | Redirect to file |
| `inheritIO(boolean)` | Inherit parent IO streams |

#### Records

| Record | Fields |
|--------|--------|
| `ProcessInfo` | `pid`, `command`, `commandLine`, `user`, `startTime`, `cpuDuration`, `alive` |
| `ProcessResult` | `exitCode`, `stdout`, `stderr`, `duration`, `command` + `isSuccess()`, `orThrow()` |

---

## Quick Start

```java
import cloud.opencode.base.core.*;
import cloud.opencode.base.core.result.*;
import cloud.opencode.base.core.concurrent.*;
import cloud.opencode.base.core.collect.*;
import cloud.opencode.base.core.tuple.*;
import cloud.opencode.base.core.primitives.*;
import cloud.opencode.base.core.system.*;
import cloud.opencode.base.core.process.*;

// ---- v1.0.3 Result monad ----
Result<String> result = Result.of(() -> Files.readString(path));
String content = result
    .map(String::trim)
    .recover(ex -> "fallback")
    .getOrElse("");

// ---- v1.0.3 Either monad ----
Either<String, User> either = findUser(id);
String name = either.map(User::getName).getOrElse("unknown");

// ---- v1.0.3 Lazy (virtual-thread-safe) ----
Lazy<ExpensiveObject> lazy = Lazy.of(() -> createExpensiveObject());
ExpensiveObject obj = lazy.get();  // Computed once, cached

// ---- v1.0.3 VirtualTasks ----
List<String> results = VirtualTasks.invokeAll(List.of(
    () -> fetchFromServiceA(),
    () -> fetchFromServiceB()
));

// ---- v1.0.3 OpenCollections ----
List<String> items = OpenCollections.<String>listBuilder()
    .add("a").add("b").add("c").build();
List<String> appended = OpenCollections.append(items, "d");

// ---- v1.0.3 ObjectDiff ----
DiffResult<User> diff = ObjectDiff.builder(oldUser, newUser)
    .deep(true).exclude("password").compare();

// ---- v1.0.3 Environment ----
if (Environment.isContainer()) {
    int cpus = Environment.availableProcessors();
}

// ---- v1.0.3 Page (immutable record) ----
Page<User> page = Page.of(1, 10, 100, userList);
Page<UserDto> dtoPage = page.map(UserDto::from);

// ---- v1.0.3 Retry ----
String data = Retry.of(() -> httpClient.get(url))
    .maxAttempts(5)
    .exponentialBackoff(Duration.ofMillis(200), 2.0)
    .retryOnAny(IOException.class, TimeoutException.class)
    .abortOn(IllegalArgumentException.class)
    .execute();

// ---- v1.0.3 Stopwatch lap timing ----
Stopwatch sw = Stopwatch.createStarted();
// ... phase 1 ...
Duration lap1 = sw.split();
// ... phase 2 ...
Duration lap2 = sw.split();
List<Duration> laps = sw.getLaps();  // [lap1, lap2]

// ---- v1.0.3 One-liner timing ----
Duration elapsed = Stopwatch.time(() -> heavyComputation());
var timed = Stopwatch.time(() -> fetchData());  // Pair<Result, Duration>

// ---- v1.0.3 Range as Predicate ----
Range<Integer> range = Range.closed(1, 100);
List<Integer> inRange = numbers.stream().filter(range).toList();

// ---- v1.0.3 OpenCollections ----
List<List<Integer>> chunks = OpenCollections.chunk(List.of(1,2,3,4,5), 2);
// [[1,2], [3,4], [5]]
Map<String, List<User>> byCity = OpenCollections.groupBy(users, User::city);

// ---- v1.0.3 Preconditions ----
this.size = Preconditions.checkPositive(size, "size");
this.name = Preconditions.checkNotBlank(name, "name");

// ---- v1.0.3 SystemInfo ----
CpuInfo cpu = SystemInfo.cpu();
System.out.println("Processors: " + cpu.availableProcessors());
System.out.println("CPU load: " + SystemInfo.cpuLoad());

MemoryInfo heap = SystemInfo.heapMemory();
System.out.println("Heap: " + heap.usedDisplay() + " / " + heap.totalDisplay());

List<DiskInfo> disks = SystemInfo.disks();
disks.forEach(d -> System.out.println(d.name() + ": " + d.usagePercent() + "% used"));

RuntimeInfo rt = SystemInfo.runtime();
System.out.println("PID: " + rt.pid() + ", uptime: " + rt.uptime() + "ms");

// ---- v1.0.3 ProcessManager ----
ProcessResult result = ProcessManager.execute("echo", "hello");
System.out.println(result.stdout());  // "hello\n"
result.orThrow();                     // throws if exitCode != 0

ProcessConfig config = ProcessConfig.builder("git", "status")
    .workingDirectory(Path.of("/my/repo"))
    .timeout(Duration.ofSeconds(10))
    .build();
ProcessResult gitResult = ProcessManager.execute(config);

List<ProcessInfo> javaProcs = ProcessManager.findByName("java");
boolean alive = ProcessManager.isAlive(ProcessManager.currentPid());

// ---- Classic APIs ----
int value = Convert.toInt("42");
Pair<String, Integer> pair = Pair.of("Alice", 30);
String joined = Joiner.on(", ").skipNulls().join("a", null, "b");
```

## License

Apache License 2.0

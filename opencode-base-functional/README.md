# OpenCode Base Functional

**Functional programming utilities for Java 25+**

`opencode-base-functional` brings robust functional programming constructs to Java, including monads (Try, Either, Option, Validation), pattern matching, pipelines, optics (Lens), async utilities with virtual threads, and record manipulation tools.

## Features

### Monad Types
- **Try**: Exception-safe computation with `map`, `flatMap`, `recover`
- **Either**: Disjoint union for error handling (Left = error, Right = success)
- **Option**: Null-safe container (Some/None) as an alternative to Optional
- **Validation**: Accumulating error validation (collects all errors, not just first)
- **Lazy**: Deferred computation with memoization
- **Sequence**: Lazy sequence operations
- **Trampoline**: Stack-safe recursive computation

### Pattern Matching
- **Type Matching**: Match by class type with extraction
- **Predicate Matching**: Match by arbitrary predicates
- **Fluent API**: Chainable `caseOf` / `when` / `orElse`

### Function Utilities
- **Composition**: `compose`, `andThen` for function chaining
- **Currying**: Convert multi-arg functions to curried form
- **Memoization**: Cache function results with configurable LRU bounds
- **Checked Functions**: `CheckedFunction`, `CheckedBiFunction`, `CheckedBiConsumer`
- **TriFunction**: Three-argument functional interface

### Optics
- **Lens**: Immutable nested update for records and objects
- **OptionalLens**: Lens for optional/nullable fields

### Pipeline
- **Pipeline**: Typed data transformation chains with `then` steps
- **PipeUtil**: Lightweight pipe operator (`pipe(value).then(f).then(g).get()`)

### Async
- **Virtual Thread Execution**: `async()` runs on virtual threads
- **Timeout Support**: `asyncTimeout()` with Try-based result
- **LazyAsync**: Deferred async that starts on first access
- **Parallel Map**: `parallelMap()` for concurrent list transformations

### Record Utilities
- **Record Lens**: Create lenses for record components by name
- **Record Copy**: Copy records with selective modifications
- **Record to Map**: Convert records to Map representation

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-functional</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.functional.OpenFunctional;

// Try monad for exception handling
Try<Integer> result = OpenFunctional.tryOf(() -> Integer.parseInt(input));
int value = result.getOrElse(0);

// Either for error handling
Either<String, User> user = OpenFunctional.right(new User("Alice"));
user.map(u -> u.name()).forEach(System.out::println);

// Option for nullable values
Option<String> name = OpenFunctional.some("value");
Option<String> empty = OpenFunctional.none();

// Pattern matching
String type = OpenFunctional.match(value)
    .caseOf(String.class, s -> "String: " + s)
    .caseOf(Integer.class, n -> "Number: " + n)
    .orElse(o -> "Unknown");
```

### Advanced Usage

```java
// Function composition
Function<String, Integer> parseAndDouble = OpenFunctional.compose(
    Integer::parseInt,
    n -> n * 2
);

// Currying
Function<Integer, Function<Integer, Integer>> add = OpenFunctional.curry(Integer::sum);
Function<Integer, Integer> add5 = add.apply(5);

// Memoization with LRU cache
Function<String, Data> cachedFetch = OpenFunctional.memoize(this::fetchData, 1000);

// Lens for immutable updates
Lens<Person, String> nameLens = OpenFunctional.lens(
    Person::name,
    (p, n) -> new Person(n, p.age())
);
Person updated = nameLens.set(person, "New Name");

// Pipeline transformations
String result = OpenFunctional.pipe("  hello  ")
    .then(String::trim)
    .then(String::toUpperCase)
    .get();  // "HELLO"

// Async with Virtual Threads
CompletableFuture<Data> future = OpenFunctional.async(() -> fetchData());

// Parallel map
List<Result> results = OpenFunctional.parallelMap(items, this::process);

// Record utilities
Lens<Person, String> lens = OpenFunctional.recordLens(Person.class, "name");
Person copy = OpenFunctional.copyRecord(person, Map.of("name", "New Name"));
Map<String, Object> map = OpenFunctional.recordToMap(person);
```

### Validation

```java
// Accumulate all errors
Validation<String, Integer> v1 = OpenFunctional.valid(42);
Validation<String, Integer> v2 = OpenFunctional.invalid("must be positive");

// Combine validations
Validation<List<String>, User> result = Validation.combine(
    validateName(name),
    validateAge(age),
    validateEmail(email)
).apply(User::new);
```

## Class Reference

### Root Package (`cloud.opencode.base.functional`)
| Class | Description |
|-------|-------------|
| `OpenFunctional` | Unified entry point with static convenience methods for all functional utilities |

### Monad Package (`cloud.opencode.base.functional.monad`)
| Class | Description |
|-------|-------------|
| `Try` | Computation that may succeed or fail with an exception |
| `Either` | Disjoint union type (Left for error, Right for success) |
| `Option` | Null-safe container (Some for value, None for absent) |
| `Validation` | Accumulating error validation that collects all errors |
| `Lazy` | Deferred computation with thread-safe memoization |
| `Sequence` | Lazy sequence with functional transformations |
| `Trampoline` | Stack-safe recursive computation via trampolining |
| `For` | For-comprehension support for monadic composition |

### Pattern Package (`cloud.opencode.base.functional.pattern`)
| Class | Description |
|-------|-------------|
| `OpenMatch` | Fluent pattern matching entry point and Matcher builder |
| `Pattern` | Pattern definition (type pattern, predicate pattern) |
| `Case` | Match case with condition and action |

### Function Package (`cloud.opencode.base.functional.function`)
| Class | Description |
|-------|-------------|
| `FunctionUtil` | Utility methods for compose, curry, memoize, and partial application |
| `CheckedFunction` | Function that may throw checked exceptions |
| `CheckedBiFunction` | BiFunction that may throw checked exceptions |
| `CheckedBiConsumer` | BiConsumer that may throw checked exceptions |
| `TriFunction` | Three-argument functional interface |

### Optics Package (`cloud.opencode.base.functional.optics`)
| Class | Description |
|-------|-------------|
| `Lens` | Composable getter/setter pair for immutable nested updates |
| `OptionalLens` | Lens variant for optional/nullable fields |

### Pipeline Package (`cloud.opencode.base.functional.pipeline`)
| Class | Description |
|-------|-------------|
| `Pipeline` | Typed transformation pipeline with builder pattern |
| `PipeUtil` | Lightweight pipe operator for value transformation chains |

### Async Package (`cloud.opencode.base.functional.async`)
| Class | Description |
|-------|-------------|
| `AsyncFunctionUtil` | Async execution utilities using virtual threads |
| `Future` | Enhanced future with functional operations |
| `LazyAsync` | Deferred async computation that starts on first access |

### Record Package (`cloud.opencode.base.functional.record`)
| Class | Description |
|-------|-------------|
| `RecordUtil` | Utilities for record lens creation, copying, and map conversion |

### Exception Package (`cloud.opencode.base.functional.exception`)
| Class | Description |
|-------|-------------|
| `OpenFunctionalException` | Base exception for functional module errors |
| `OpenMatchException` | Exception thrown when no match case is satisfied |

## Requirements

- Java 25+ (uses records, sealed interfaces, virtual threads, pattern matching)
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

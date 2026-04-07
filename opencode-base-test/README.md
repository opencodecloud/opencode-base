# OpenCode Base Test

**Testing utilities and framework for Java 25+**

`opencode-base-test` is a lightweight testing toolkit providing fluent assertions, mock builders, benchmark runners, test data generators, concurrent testers, and HTTP test servers -- all without heavy external dependencies.

## Features

### Core Features
- **Fluent Assertions**: Type-safe assertion API for objects, strings, collections, maps, numbers, booleans, and exceptions
- **Record Assertions**: Assert on Java Record components by name — no getter boilerplate
- **Map Assertions**: Standalone fluent Map assertion class with rich API
- **Timing Assertions**: Assert code completes within a specified duration
- **Snapshot Assertions**: JSON snapshot testing — auto-create on first run, compare on subsequent runs
- **Soft Assertions**: Collect multiple assertion failures before reporting
- **Mock Builder**: Interface-based mock proxy creation with method stubbing
- **Spy**: Method invocation recording and verification

### Advanced Features
- **Auto Fill**: Auto-populate Record/POJO instances via reflection — one line of code
- **Edge Cases**: Boundary value generators for all primitive types, strings, collections, dates
- **Benchmark Runner**: Micro-benchmark with warmup, iterations, and comparison
- **Concurrent Tester**: Thread-safety verification with configurable concurrency
- **Test Data Generators**: Random strings, emails, phones, names, UUIDs, and more
- **Faker**: Realistic fake data generation (names, addresses, etc.)
- **HTTP Test Server**: Lightweight mock HTTP server with request verification
- **Test Fixtures**: Reusable test data setup with fixture registry
- **Test Reports**: Report generation and formatting (text, HTML, JSON, JUnit XML, Markdown)
- **Custom Annotations**: `@FastTest`, `@SlowTest`, `@IntegrationTest`, `@Repeat`

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-test</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.test.OpenTest;

// Fluent assertions
OpenTest.assertThat("hello").isNotEmpty().startsWith("he");
OpenTest.assertThat(list).hasSize(3).contains("a");
OpenTest.assertThatThrownBy(() -> divide(1, 0))
    .isInstanceOf(ArithmeticException.class);

// Record assertions
record User(String name, int age) {}
OpenTest.assertRecord(new User("Alice", 30))
    .hasComponent("name", "Alice")
    .hasComponent("age", 30);

// Timing assertions
OpenTest.assertCompletesWithin(Duration.ofMillis(100), () -> compute());

// Quick mocking
Runnable mock = OpenTest.quickMock(Runnable.class);

// Benchmarking
Duration elapsed = OpenTest.time(() -> sort(largeList));

// Test data generation
String email = OpenTest.randomEmail();
int n = OpenTest.randomInt(1, 100);
```

### Auto Fill — Zero-Boilerplate Test Data

```java
import cloud.opencode.base.test.data.AutoFill;

record User(String name, int age, String email) {}

// Random data
User user = AutoFill.of(User.class).build();

// Deterministic (seeded)
User user = AutoFill.of(User.class).seed(42L).build();

// Override specific fields
User user = AutoFill.of(User.class).with("name", "Alice").build();

// Generate a list
List<User> users = AutoFill.of(User.class).list(10);
```

### Edge Case Testing

```java
import cloud.opencode.base.test.data.EdgeCases;

// Boundary values for int: [MIN_VALUE, -1, 0, 1, MAX_VALUE]
for (int edge : EdgeCases.forInt()) {
    assertDoesNotThrow(() -> process(edge));
}

// Boundary values for String: [null, "", " ", "\t", "\n", "a", "aaa...128"]
for (String edge : EdgeCases.forString()) {
    validate(edge);
}
```

### HTTP Mock Server with Verification

```java
import cloud.opencode.base.test.http.*;

try (TestHttpServer server = TestHttpServer.start()) {
    server.when(RequestMatcher.get("/api/users"))
          .thenRespond(MockResponse.ok("{\"id\": 1}"));

    // ... make HTTP requests to server.url("/api/users") ...

    // Verify requests
    server.verify()
        .that(RequestMatcher.get("/api/users"))
        .wasCalled(1)
        .withHeader("accept", "application/json");
}
```

### Snapshot Testing

```java
import cloud.opencode.base.test.assertion.SnapshotAssert;

// First run: creates snapshot file automatically
// Subsequent runs: compares against stored snapshot
SnapshotAssert.assertMatchesSnapshot("user-response", actualJson);

// Update snapshots: -Dopencode.test.update-snapshots=true
```

## Class Reference

### Root Package (`cloud.opencode.base.test`)
| Class | Description |
|-------|-------------|
| `OpenAssert` | Basic assertion utilities |
| `OpenData` | Test data utilities |
| `OpenMock` | Mock utilities |
| `OpenTest` | Main facade for testing: assertions, mocking, benchmarking, data generation |
| `ResourceLoader` | Test resource file loading utilities |
| `TestContext` | Test execution context management |

### Annotations (`test.annotation`)
| Class | Description |
|-------|-------------|
| `@FastTest` | Marks a test as fast-running (for filtering) |
| `@IntegrationTest` | Marks a test as an integration test |
| `@Repeat` | Repeats a test a specified number of times |
| `@SlowTest` | Marks a test as slow-running |

### Assertions (`test.assertion`)
| Class | Description |
|-------|-------------|
| `AssertionResult` | Result of an assertion check |
| `CollectionAssert` | Fluent assertions for collections |
| `ExceptionAssert` | Fluent assertions for exceptions |
| `JsonAssert` | Fluent assertions for JSON strings |
| `MapAssert` | Fluent assertions for maps |
| `NumberAssert` | Fluent assertions for numbers |
| `OpenAssertions` | Main assertion entry point with type-specific assertion builders |
| `RecordAssert` | Fluent assertions for Java Record components |
| `SnapshotAssert` | JSON snapshot testing assertions |
| `SoftAssert` | Soft assertions that collect failures before reporting |
| `StringAssert` | Fluent assertions for strings |
| `TimingAssert` | Performance timing assertions |

### Benchmark (`test.benchmark`)
| Class | Description |
|-------|-------------|
| `Benchmark` | Micro-benchmark runner with warmup and iterations |
| `BenchmarkResult` | Benchmark execution result with statistics |
| `BenchmarkRunner` | Configurable benchmark execution engine |

### Concurrent (`test.concurrent`)
| Class | Description |
|-------|-------------|
| `ConcurrentTester` | Thread-safety testing with configurable thread count and iterations |
| `ThreadSafetyChecker` | Automated thread-safety verification |

### Data (`test.data`)
| Class | Description |
|-------|-------------|
| `AutoFill` | Auto-populate Record/POJO instances via reflection |
| `DataGenerator` | Base data generator |
| `EdgeCases` | Boundary value generators for all common types |
| `Faker` | Realistic fake data generation (names, addresses, companies) |
| `RandomData` | Random primitive and string data generation |
| `RepeatableRandom` | Seeded random for reproducible tests |
| `SensitiveDataGenerator` | Generate test data for sensitive fields (ID card, bank card, etc.) |
| `TestDataGenerator` | Comprehensive test data factory |

### Exception (`test.exception`)
| Class | Description |
|-------|-------------|
| `AssertionException` | Thrown when an assertion fails |
| `BenchmarkException` | Thrown when a benchmark fails |
| `DataGenerationException` | Thrown when test data generation fails |
| `EqualsAssertionException` | Thrown when an equality assertion fails |
| `MockException` | Thrown when mock setup or verification fails |
| `TestErrorCode` | Error codes for test exceptions |
| `TestException` | Base test exception (extends `OpenException`) |

### Fixture (`test.fixture`)
| Class | Description |
|-------|-------------|
| `FixtureRegistry` | Registry for reusable test fixtures |
| `TestFixture` | Test data fixture with lazy init and teardown |

### HTTP (`test.http`)
| Class | Description |
|-------|-------------|
| `MockResponse` | Configurable mock HTTP response |
| `RecordedRequest` | Captured HTTP request for verification |
| `RequestMatcher` | HTTP request matching predicates |
| `RequestVerification` | Fluent HTTP request verification builder |
| `TestHttpServer` | Lightweight mock HTTP server with request verification |

### Internal (`test.internal`)
| Class | Description |
|-------|-------------|
| `AssertionMessageMasker` | Masks sensitive data in assertion failure messages |

### Mock (`test.mock`)
| Class | Description |
|-------|-------------|
| `Invocation` | Recorded method invocation |
| `MockBuilder` | Fluent builder for creating mock proxies |
| `MockInvocationHandler` | Proxy invocation handler for mocks |
| `MockProxy` | Dynamic proxy-based mock object |
| `Spy` | Method invocation spy for recording and verification |

### Report (`test.report`)
| Class | Description |
|-------|-------------|
| `ReportGenerator` | Test report generation (text, HTML, JSON) |
| `TestReport` | Test execution report data |
| `TestReportFormatter` | Report formatting (text, JUnit XML, Markdown) |

### Wait (`test.wait`)
| Class | Description |
|-------|-------------|
| `Poller` | Polling-based condition waiting with timeout |
| `Wait` | Utility for waiting on conditions with configurable timeout and interval |

## What's New in V1.0.3

### New Classes
| Class | Description |
|-------|-------------|
| `RecordAssert` | Assert Java Record components by name — `hasComponent("name", "Alice")` |
| `MapAssert` | Standalone fluent Map assertions — `containsEntry(k, v)` |
| `TimingAssert` | Assert code completes within a duration — `assertCompletesWithin(100ms, task)` |
| `SnapshotAssert` | JSON snapshot testing — auto-create, auto-compare, update via system property |
| `AutoFill` | Auto-populate Record/POJO with one line — `AutoFill.of(User.class).build()` |
| `EdgeCases` | Boundary value generators for 14 types — `forInt()`, `forString()`, `forDuration()` |
| `RequestVerification` | Fluent HTTP request verification — `verify().that(get("/api")).wasCalled(1)` |

### Key Improvements
- `TestException` now extends `OpenException` (unified exception hierarchy)
- `OpenTest` facade expanded with `assertRecord()`, `assertMap()`, `assertCompletesWithin()`, `autoFill()`, `edgeCasesForInt/String()`
- `TestHttpServer.verify()` for request count, body, and header assertions
- 30 security fixes across 3 audit rounds (path traversal, XSS, CRLF injection, integer overflow, thread safety)
- 11 performance optimizations (reflection caching, COWAL elimination, lazy message building)

## Requirements

- Java 25+
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

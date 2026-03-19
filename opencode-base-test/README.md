# OpenCode Base Test

**Testing utilities and framework for Java 25+**

`opencode-base-test` is a lightweight testing toolkit providing fluent assertions, mock builders, benchmark runners, test data generators, concurrent testers, and HTTP test servers -- all without heavy external dependencies.

## Features

### Core Features
- **Fluent Assertions**: Type-safe assertion API for objects, strings, collections, maps, numbers, booleans, and exceptions
- **Soft Assertions**: Collect multiple assertion failures before reporting
- **Mock Builder**: Interface-based mock proxy creation with method stubbing
- **Spy**: Method invocation recording and verification

### Advanced Features
- **Benchmark Runner**: Micro-benchmark with warmup, iterations, and comparison
- **Concurrent Tester**: Thread-safety verification with configurable concurrency
- **Test Data Generators**: Random strings, emails, phones, names, UUIDs, and more
- **Faker**: Realistic fake data generation (names, addresses, etc.)
- **HTTP Test Server**: Lightweight mock HTTP server for integration tests
- **Test Fixtures**: Reusable test data setup with fixture registry
- **Test Reports**: Report generation and formatting
- **Custom Annotations**: `@FastTest`, `@SlowTest`, `@IntegrationTest`, `@Repeat`

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-test</artifactId>
    <version>1.0.0</version>
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

// Quick mocking
Runnable mock = OpenTest.quickMock(Runnable.class);

// Benchmarking
Duration elapsed = OpenTest.time(() -> sort(largeList));
OpenTest.compare("quickSort", () -> quickSort(data),
                  "mergeSort", () -> mergeSort(data));

// Test data generation
String email = OpenTest.randomEmail();    // "abc123@test.com"
String phone = OpenTest.randomPhone();    // random phone number
String name = OpenTest.randomName();      // random full name
int n = OpenTest.randomInt(1, 100);       // random int in range
```

### HTTP Mock Server

```java
import cloud.opencode.base.test.http.*;

TestHttpServer server = new TestHttpServer();
server.enqueue(MockResponse.ok("{\"id\": 1}"));
server.start();

// Make requests to server.getUrl()
// Verify with server.takeRequest()
RecordedRequest request = server.takeRequest();
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
| `NumberAssert` | Fluent assertions for numbers |
| `OpenAssertions` | Main assertion entry point with type-specific assertion builders |
| `SoftAssert` | Soft assertions that collect failures before reporting |
| `StringAssert` | Fluent assertions for strings |

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
| `DataGenerator` | Base interface for data generators |
| `Faker` | Realistic fake data generation (names, addresses, companies) |
| `RandomData` | Random primitive and string data generation |
| `RepeatableRandom` | Seeded random for reproducible tests |
| `SensitiveDataGenerator` | Generate test data for sensitive fields (SSN, credit card, etc.) |
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
| `TestException` | Base test exception |

### Fixture (`test.fixture`)
| Class | Description |
|-------|-------------|
| `FixtureRegistry` | Registry for reusable test fixtures |
| `TestFixture` | Interface for test data fixtures |

### HTTP (`test.http`)
| Class | Description |
|-------|-------------|
| `MockResponse` | Configurable mock HTTP response |
| `RecordedRequest` | Captured HTTP request for verification |
| `RequestMatcher` | HTTP request matching predicates |
| `TestHttpServer` | Lightweight mock HTTP server for integration testing |

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
| `ReportGenerator` | Test report generation |
| `TestReport` | Test execution report data |
| `TestReportFormatter` | Report formatting utilities |

### Wait (`test.wait`)
| Class | Description |
|-------|-------------|
| `Poller` | Polling-based condition waiting with timeout |
| `Wait` | Utility for waiting on conditions with configurable timeout and interval |

## Requirements

- Java 25+
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

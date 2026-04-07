# OpenCode Base Test

**适用于 Java 25+ 的测试工具框架**

`opencode-base-test` 是一个轻量级测试工具包，提供流式断言、Mock 构建器、基准测试运行器、测试数据生成器、并发测试器和 HTTP 测试服务器——无需沉重的外部依赖。

## 功能特性

### 核心功能
- **流式断言**：类型安全的断言 API，支持对象、字符串、集合、Map、数字、布尔值和异常
- **Record 断言**：按名称断言 Java Record 组件——无需 getter 样板代码
- **Map 断言**：独立的 Map 流式断言类，API 丰富
- **性能断言**：断言代码在指定时间内完成
- **快照断言**：JSON 快照测试——首次运行自动创建，后续运行自动对比
- **软断言**：收集多个断言失败后统一报告
- **Mock 构建器**：基于接口的 Mock 代理创建和方法桩设置
- **Spy**：方法调用记录和验证

### 高级功能
- **自动填充**：通过反射自动填充 Record/POJO 实例——一行代码搞定
- **边界值生成器**：为所有基础类型、字符串、集合、日期生成边界值
- **基准测试**：微基准测试，支持预热、迭代和对比
- **并发测试器**：可配置并发度的线程安全验证
- **测试数据生成器**：随机字符串、邮箱、手机号、姓名、UUID 等
- **Faker**：生成逼真的假数据（姓名、地址等）
- **HTTP 测试服务器**：轻量级 Mock HTTP 服务器，支持请求验证
- **测试夹具**：可复用的测试数据设置和夹具注册表
- **测试报告**：报告生成和格式化（文本、HTML、JSON、JUnit XML、Markdown）
- **自定义注解**：`@FastTest`、`@SlowTest`、`@IntegrationTest`、`@Repeat`

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-test</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.test.OpenTest;

// 流式断言
OpenTest.assertThat("hello").isNotEmpty().startsWith("he");
OpenTest.assertThat(list).hasSize(3).contains("a");
OpenTest.assertThatThrownBy(() -> divide(1, 0))
    .isInstanceOf(ArithmeticException.class);

// Record 断言
record User(String name, int age) {}
OpenTest.assertRecord(new User("Alice", 30))
    .hasComponent("name", "Alice")
    .hasComponent("age", 30);

// 性能断言
OpenTest.assertCompletesWithin(Duration.ofMillis(100), () -> compute());

// 快速 Mock
Runnable mock = OpenTest.quickMock(Runnable.class);

// 基准测试
Duration elapsed = OpenTest.time(() -> sort(largeList));

// 测试数据生成
String email = OpenTest.randomEmail();
int n = OpenTest.randomInt(1, 100);
```

### 自动填充——零样板代码测试数据

```java
import cloud.opencode.base.test.data.AutoFill;

record User(String name, int age, String email) {}

// 随机数据
User user = AutoFill.of(User.class).build();

// 确定性（带种子）
User user = AutoFill.of(User.class).seed(42L).build();

// 覆盖指定字段
User user = AutoFill.of(User.class).with("name", "Alice").build();

// 批量生成
List<User> users = AutoFill.of(User.class).list(10);
```

### 边界值测试

```java
import cloud.opencode.base.test.data.EdgeCases;

// int 边界值: [MIN_VALUE, -1, 0, 1, MAX_VALUE]
for (int edge : EdgeCases.forInt()) {
    assertDoesNotThrow(() -> process(edge));
}

// String 边界值: [null, "", " ", "\t", "\n", "a", "aaa...128"]
for (String edge : EdgeCases.forString()) {
    validate(edge);
}
```

### HTTP Mock 服务器与请求验证

```java
import cloud.opencode.base.test.http.*;

try (TestHttpServer server = TestHttpServer.start()) {
    server.when(RequestMatcher.get("/api/users"))
          .thenRespond(MockResponse.ok("{\"id\": 1}"));

    // ... 向 server.url("/api/users") 发送 HTTP 请求 ...

    // 验证请求
    server.verify()
        .that(RequestMatcher.get("/api/users"))
        .wasCalled(1)
        .withHeader("accept", "application/json");
}
```

### 快照测试

```java
import cloud.opencode.base.test.assertion.SnapshotAssert;

// 首次运行：自动创建快照文件
// 后续运行：与存储的快照对比
SnapshotAssert.assertMatchesSnapshot("user-response", actualJson);

// 更新快照：-Dopencode.test.update-snapshots=true
```

## 类参考

### 根包 (`cloud.opencode.base.test`)
| 类 | 说明 |
|----|------|
| `OpenAssert` | 基础断言工具 |
| `OpenData` | 测试数据工具 |
| `OpenMock` | Mock 工具 |
| `OpenTest` | 测试主门面：断言、Mock、基准测试、数据生成 |
| `ResourceLoader` | 测试资源文件加载工具 |
| `TestContext` | 测试执行上下文管理 |

### 注解 (`test.annotation`)
| 类 | 说明 |
|----|------|
| `@FastTest` | 标记为快速测试（用于过滤） |
| `@IntegrationTest` | 标记为集成测试 |
| `@Repeat` | 重复执行测试指定次数 |
| `@SlowTest` | 标记为慢速测试 |

### 断言 (`test.assertion`)
| 类 | 说明 |
|----|------|
| `AssertionResult` | 断言检查结果 |
| `CollectionAssert` | 集合流式断言 |
| `ExceptionAssert` | 异常流式断言 |
| `JsonAssert` | JSON 字符串流式断言 |
| `MapAssert` | Map 流式断言 |
| `NumberAssert` | 数字流式断言 |
| `OpenAssertions` | 断言主入口，提供类型特定的断言构建器 |
| `RecordAssert` | Java Record 组件流式断言 |
| `SnapshotAssert` | JSON 快照测试断言 |
| `SoftAssert` | 软断言，收集失败后统一报告 |
| `StringAssert` | 字符串流式断言 |
| `TimingAssert` | 性能计时断言 |

### 基准测试 (`test.benchmark`)
| 类 | 说明 |
|----|------|
| `Benchmark` | 微基准测试运行器，支持预热和迭代 |
| `BenchmarkResult` | 基准测试执行结果及统计数据 |
| `BenchmarkRunner` | 可配置的基准测试执行引擎 |

### 并发 (`test.concurrent`)
| 类 | 说明 |
|----|------|
| `ConcurrentTester` | 可配置线程数和迭代次数的线程安全测试 |
| `ThreadSafetyChecker` | 自动化线程安全验证 |

### 数据 (`test.data`)
| 类 | 说明 |
|----|------|
| `AutoFill` | 通过反射自动填充 Record/POJO 实例 |
| `DataGenerator` | 数据生成器 |
| `EdgeCases` | 所有常用类型的边界值生成器 |
| `Faker` | 生成逼真假数据（姓名、地址、公司） |
| `RandomData` | 随机基础类型和字符串数据生成 |
| `RepeatableRandom` | 带种子的随机数，用于可复现测试 |
| `SensitiveDataGenerator` | 生成敏感字段测试数据（身份证、银行卡等） |
| `TestDataGenerator` | 综合测试数据工厂 |

### 异常 (`test.exception`)
| 类 | 说明 |
|----|------|
| `AssertionException` | 断言失败时抛出 |
| `BenchmarkException` | 基准测试失败时抛出 |
| `DataGenerationException` | 测试数据生成失败时抛出 |
| `EqualsAssertionException` | 相等断言失败时抛出 |
| `MockException` | Mock 设置或验证失败时抛出 |
| `TestErrorCode` | 测试异常错误码 |
| `TestException` | 测试基础异常（继承 `OpenException`） |

### 夹具 (`test.fixture`)
| 类 | 说明 |
|----|------|
| `FixtureRegistry` | 可复用测试夹具注册表 |
| `TestFixture` | 测试数据夹具，支持懒初始化和清理 |

### HTTP (`test.http`)
| 类 | 说明 |
|----|------|
| `MockResponse` | 可配置的 Mock HTTP 响应 |
| `RecordedRequest` | 捕获的 HTTP 请求，用于验证 |
| `RequestMatcher` | HTTP 请求匹配谓词 |
| `RequestVerification` | HTTP 请求流式验证构建器 |
| `TestHttpServer` | 轻量级 Mock HTTP 服务器，支持请求验证 |

### 内部 (`test.internal`)
| 类 | 说明 |
|----|------|
| `AssertionMessageMasker` | 在断言失败消息中掩码敏感数据 |

### Mock (`test.mock`)
| 类 | 说明 |
|----|------|
| `Invocation` | 记录的方法调用 |
| `MockBuilder` | Mock 代理的流式构建器 |
| `MockInvocationHandler` | Mock 的代理调用处理器 |
| `MockProxy` | 基于动态代理的 Mock 对象 |
| `Spy` | 方法调用间谍，用于记录和验证 |

### 报告 (`test.report`)
| 类 | 说明 |
|----|------|
| `ReportGenerator` | 测试报告生成（文本、HTML、JSON） |
| `TestReport` | 测试执行报告数据 |
| `TestReportFormatter` | 报告格式化（文本、JUnit XML、Markdown） |

### 等待 (`test.wait`)
| 类 | 说明 |
|----|------|
| `Poller` | 基于轮询的条件等待，支持超时 |
| `Wait` | 条件等待工具，可配置超时和间隔 |

## V1.0.3 新增功能

### 新增类
| 类 | 说明 |
|----|------|
| `RecordAssert` | 按名称断言 Java Record 组件 — `hasComponent("name", "Alice")` |
| `MapAssert` | 独立 Map 流式断言 — `containsEntry(k, v)` |
| `TimingAssert` | 性能计时断言 — `assertCompletesWithin(100ms, task)` |
| `SnapshotAssert` | JSON 快照测试 — 自动创建、自动比对、系统属性更新 |
| `AutoFill` | 一行代码自动填充 Record/POJO — `AutoFill.of(User.class).build()` |
| `EdgeCases` | 14 种类型边界值生成器 — `forInt()`, `forString()`, `forDuration()` |
| `RequestVerification` | HTTP 请求流式验证 — `verify().that(get("/api")).wasCalled(1)` |

### 主要改进
- `TestException` 改为继承 `OpenException`（统一异常体系）
- `OpenTest` 门面扩展：`assertRecord()`, `assertMap()`, `assertCompletesWithin()`, `autoFill()`, `edgeCasesForInt/String()`
- `TestHttpServer.verify()` 支持请求次数、请求体、请求头验证
- 3 轮安全审计共修复 30 项问题（路径穿越、XSS、CRLF 注入、整数溢出、线程安全）
- 11 项性能优化（反射缓存、COWAL 消除、延迟消息构建）

## 环境要求

- Java 25+
- 核心功能无外部依赖

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

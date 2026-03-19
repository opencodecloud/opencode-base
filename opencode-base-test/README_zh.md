# OpenCode Base Test

**适用于 Java 25+ 的测试工具框架**

`opencode-base-test` 是一个轻量级测试工具包，提供流式断言、Mock 构建器、基准测试运行器、测试数据生成器、并发测试器和 HTTP 测试服务器——无需沉重的外部依赖。

## 功能特性

### 核心功能
- **流式断言**：类型安全的断言 API，支持对象、字符串、集合、Map、数字、布尔值和异常
- **软断言**：收集多个断言失败后统一报告
- **Mock 构建器**：基于接口的 Mock 代理创建和方法桩设置
- **Spy**：方法调用记录和验证

### 高级功能
- **基准测试**：微基准测试，支持预热、迭代和对比
- **并发测试器**：可配置并发度的线程安全验证
- **测试数据生成器**：随机字符串、邮箱、手机号、姓名、UUID 等
- **Faker**：生成逼真的假数据（姓名、地址等）
- **HTTP 测试服务器**：轻量级 Mock HTTP 服务器，用于集成测试
- **测试夹具**：可复用的测试数据设置和夹具注册表
- **测试报告**：报告生成和格式化
- **自定义注解**：`@FastTest`、`@SlowTest`、`@IntegrationTest`、`@Repeat`

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-test</artifactId>
    <version>1.0.0</version>
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

// 快速 Mock
Runnable mock = OpenTest.quickMock(Runnable.class);

// 基准测试
Duration elapsed = OpenTest.time(() -> sort(largeList));
OpenTest.compare("quickSort", () -> quickSort(data),
                  "mergeSort", () -> mergeSort(data));

// 测试数据生成
String email = OpenTest.randomEmail();    // "abc123@test.com"
String phone = OpenTest.randomPhone();    // 随机手机号
String name = OpenTest.randomName();      // 随机姓名
int n = OpenTest.randomInt(1, 100);       // 范围内随机整数
```

### HTTP Mock 服务器

```java
import cloud.opencode.base.test.http.*;

TestHttpServer server = new TestHttpServer();
server.enqueue(MockResponse.ok("{\"id\": 1}"));
server.start();

// 向 server.getUrl() 发送请求
// 使用 server.takeRequest() 验证
RecordedRequest request = server.takeRequest();
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
| `NumberAssert` | 数字流式断言 |
| `OpenAssertions` | 断言主入口，提供类型特定的断言构建器 |
| `SoftAssert` | 软断言，收集失败后统一报告 |
| `StringAssert` | 字符串流式断言 |

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
| `DataGenerator` | 数据生成器基础接口 |
| `Faker` | 生成逼真假数据（姓名、地址、公司） |
| `RandomData` | 随机基础类型和字符串数据生成 |
| `RepeatableRandom` | 带种子的随机数，用于可复现测试 |
| `SensitiveDataGenerator` | 生成敏感字段测试数据（身份证、信用卡等） |
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
| `TestException` | 测试基础异常 |

### 夹具 (`test.fixture`)
| 类 | 说明 |
|----|------|
| `FixtureRegistry` | 可复用测试夹具注册表 |
| `TestFixture` | 测试数据夹具接口 |

### HTTP (`test.http`)
| 类 | 说明 |
|----|------|
| `MockResponse` | 可配置的 Mock HTTP 响应 |
| `RecordedRequest` | 捕获的 HTTP 请求，用于验证 |
| `RequestMatcher` | HTTP 请求匹配谓词 |
| `TestHttpServer` | 轻量级 Mock HTTP 服务器，用于集成测试 |

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
| `ReportGenerator` | 测试报告生成 |
| `TestReport` | 测试执行报告数据 |
| `TestReportFormatter` | 报告格式化工具 |

### 等待 (`test.wait`)
| 类 | 说明 |
|----|------|
| `Poller` | 基于轮询的条件等待，支持超时 |
| `Wait` | 条件等待工具，可配置超时和间隔 |

## 环境要求

- Java 25+
- 核心功能无外部依赖

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

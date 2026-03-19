# Test 组件方案

## 1. 组件概述

### 1.1 设计目标

`opencode-base-test` 模块提供轻量级测试辅助能力，专注于补充 JUnit 等框架缺失的功能，而非替代。

**核心特性：**
- 断言增强（静态断言 + 流式断言 + 软断言 + JSON断言）
- Mock 支持（基于 JDK Proxy 的接口 Mock、Spy、验证）
- 测试数据生成（随机数据、Faker假数据、敏感数据、可重复随机）
- 性能基准测试（预热、统计、对比）
- 并发测试（线程安全检测、并发执行）
- 测试报告生成（文本、HTML、JSON、Markdown、JUnit XML）
- 测试上下文（基于 ScopedValue 的上下文传播）
- 测试资源加载
- 测试注解（FastTest、SlowTest、IntegrationTest、Repeat）
- 异步等待（Poller、Wait）

### 1.2 架构概览

```
┌─────────────────────────────────────────────────────────────────┐
│                        Application                               │
│                    (测试用例 Test Cases)                          │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                         Facade Layer                             │
│  ┌───────────┐ ┌───────────┐ ┌───────────┐ ┌───────────┐       │
│  │ OpenAssert│ │ OpenMock  │ │ OpenData  │ │ OpenTest  │       │
│  │  断言入口  │ │  Mock入口 │ │ 数据入口  │ │ 统一入口  │       │
│  └───────────┘ └───────────┘ └───────────┘ └───────────┘       │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                        Core Layer                                │
│  ┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐  │
│  │assertion/││  mock/   ││  data/   ││benchmark/││concurrent/│  │
│  │ 断言扩展 ││ Mock支持 ││ 数据生成 ││ 性能测试 ││ 并发测试  │  │
│  └──────────┘└──────────┘└──────────┘└──────────┘└──────────┘  │
└─────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                      Infrastructure Layer                        │
│  ┌──────────┐┌──────────┐┌──────────┐┌──────────┐┌──────────┐  │
│  │ report/  ││exception/││ fixture/ ││  wait/   ││annotation/│  │
│  │ 测试报告 ││  异常处理 ││ 夹具管理 ││ 异步等待 ││ 测试注解  │  │
│  └──────────┘└──────────┘└──────────┘└──────────┘└──────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 1.3 模块依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-test</artifactId>
    <version>${version}</version>
    <scope>test</scope>
</dependency>
```

---

## 2. 包结构设计

```
cloud.opencode.base.test
├── OpenAssert.java                     # 静态断言入口类
├── OpenMock.java                       # Mock 入口类
├── OpenData.java                       # 测试数据生成入口类
├── OpenTest.java                       # 统一入口类（整合断言+Mock+数据+基准）
├── ResourceLoader.java                 # 测试资源加载工具
├── TestContext.java                    # 测试上下文（基于ScopedValue）
│
├── annotation/                         # 测试注解
│   ├── FastTest.java                   # 快速测试标记
│   ├── SlowTest.java                   # 慢速测试标记
│   ├── IntegrationTest.java            # 集成测试标记
│   └── Repeat.java                     # 重复执行注解
│
├── assertion/                          # 断言扩展
│   ├── OpenAssertions.java             # 流式断言入口（assertThat链式调用）
│   ├── CollectionAssert.java           # 集合断言
│   ├── StringAssert.java              # 字符串断言
│   ├── NumberAssert.java              # 数值断言
│   ├── ExceptionAssert.java           # 异常断言
│   ├── JsonAssert.java                # JSON 断言
│   ├── SoftAssert.java                # 软断言收集器
│   └── AssertionResult.java           # 断言结果 (sealed interface)
│
├── mock/                               # Mock 支持
│   ├── MockBuilder.java               # Mock 构建器
│   ├── MockProxy.java                 # Mock 代理工厂
│   ├── MockInvocationHandler.java     # 调用处理器（Stubbing）
│   ├── Invocation.java                # 调用记录 (Record)
│   └── Spy.java                       # Spy 调用记录器
│
├── data/                               # 测试数据生成
│   ├── DataGenerator.java             # 通用数据生成器
│   ├── TestDataGenerator.java         # 测试数据生成器
│   ├── RandomData.java                # 随机数据工具（UUID/Hex/Base64/令牌）
│   ├── Faker.java                     # 假数据生成（姓名/地址/公司等）
│   ├── SensitiveDataGenerator.java    # 敏感数据生成（脱敏测试数据）
│   └── RepeatableRandom.java          # 可重复随机生成器（固定种子）
│
├── benchmark/                          # 性能基准测试
│   ├── Benchmark.java                 # 基准测试工具
│   ├── BenchmarkResult.java           # 测试结果 (Record)
│   └── BenchmarkRunner.java           # 可配置测试运行器
│
├── concurrent/                         # 并发测试
│   ├── ConcurrentTester.java          # 并发测试器
│   └── ThreadSafetyChecker.java       # 线程安全检测器
│
├── report/                             # 测试报告
│   ├── TestReport.java                # 测试报告数据
│   ├── ReportGenerator.java           # 报告生成器（文本/HTML/JSON）
│   └── TestReportFormatter.java       # 报告格式化（文本/JUnit XML/Markdown）
│
├── fixture/                            # 测试夹具
│   ├── TestFixture.java               # 测试夹具（setup/teardown/get）
│   └── FixtureRegistry.java           # 夹具全局注册表
│
├── wait/                               # 异步等待
│   ├── Wait.java                      # 等待工具（条件等待、时间等待）
│   └── Poller.java                    # 轮询器（超时、间隔、描述）
│
└── exception/                          # 异常体系
    ├── TestException.java             # 测试异常基类
    ├── TestErrorCode.java             # 错误码枚举
    ├── AssertionException.java        # 断言异常
    ├── EqualsAssertionException.java  # 相等断言异常（含expected/actual）
    ├── MockException.java             # Mock 异常
    ├── DataGenerationException.java   # 数据生成异常
    └── BenchmarkException.java        # 基准测试异常
```

---

## 3. 核心 API

### 3.1 OpenAssert - 静态断言入口

提供全面的静态断言方法，涵盖基础断言、数值比较、集合断言、字符串断言、异常断言、超时断言。

```java
public final class OpenAssert {

    // === 基础断言 ===
    public static void assertTrue(boolean condition)
    public static void assertTrue(boolean condition, String message)
    public static void assertTrue(boolean condition, Supplier<String> messageSupplier)
    public static void assertFalse(boolean condition)
    public static void assertFalse(boolean condition, String message)
    public static void assertNull(Object obj)
    public static void assertNull(Object obj, String message)
    public static void assertNotNull(Object obj)
    public static void assertNotNull(Object obj, String message)

    // === 相等断言 ===
    public static void assertEquals(Object expected, Object actual)
    public static void assertEquals(Object expected, Object actual, String message)
    public static void assertNotEquals(Object unexpected, Object actual)
    public static void assertSame(Object expected, Object actual)
    public static void assertNotSame(Object unexpected, Object actual)

    // === 数值断言 ===
    public static void assertEquals(double expected, double actual, double delta)
    public static <T extends Comparable<T>> void assertGreaterThan(T actual, T expected)
    public static <T extends Comparable<T>> void assertLessThan(T actual, T expected)
    public static <T extends Comparable<T>> void assertBetween(T actual, T min, T max)

    // === 集合断言 ===
    public static void assertEmpty(Collection<?> collection)
    public static void assertNotEmpty(Collection<?> collection)
    public static void assertSize(int expected, Collection<?> collection)
    public static void assertContains(Object element, Collection<?> collection)
    public static void assertEmpty(Map<?, ?> map)
    public static void assertContainsKey(Object key, Map<?, ?> map)

    // === 字符串断言 ===
    public static void assertBlank(String str)
    public static void assertNotBlank(String str)
    public static void assertContains(String expected, String actual)
    public static void assertStartsWith(String prefix, String actual)
    public static void assertEndsWith(String suffix, String actual)
    public static void assertMatches(String regex, String actual)

    // === 异常断言 ===
    public static <T extends Throwable> T assertThrows(Class<T> expectedType,
                                                        Executable executable)
    public static void assertDoesNotThrow(Executable executable)

    // === 超时断言 ===
    public static void assertTimeout(Duration timeout, Executable executable)

    // === 失败 ===
    public static void fail()
    public static void fail(String message)

    /** 可执行接口 */
    public interface Executable {
        void execute() throws Throwable;
    }
}
```

```java
// 基础断言
OpenAssert.assertTrue(result.isSuccess());
OpenAssert.assertNotNull(user);
OpenAssert.assertEquals("hello", greeting);

// 数值断言
OpenAssert.assertGreaterThan(count, 0);
OpenAssert.assertBetween(score, 0, 100);

// 集合断言
OpenAssert.assertNotEmpty(users);
OpenAssert.assertSize(3, results);
OpenAssert.assertContains("admin", roles);

// 字符串断言
OpenAssert.assertNotBlank(name);
OpenAssert.assertStartsWith("http", url);
OpenAssert.assertMatches("\\d{4}-\\d{2}-\\d{2}", dateStr);

// 异常断言
IllegalArgumentException ex = OpenAssert.assertThrows(
    IllegalArgumentException.class, () -> service.process(null));
OpenAssert.assertContains("must not be null", ex.getMessage());

// 超时断言
OpenAssert.assertTimeout(Duration.ofSeconds(1), () -> {
    service.performAction();
});
```

### 3.2 OpenTest - 统一入口

整合断言、Mock、数据生成、基准测试的一站式入口。

```java
public final class OpenTest {

    // === 流式断言 ===
    public static <T> OpenAssertions.ObjectAssertion<T> assertThat(T actual)
    public static OpenAssertions.StringAssertion assertThat(String actual)
    public static <T> OpenAssertions.CollectionAssertion<T> assertThat(Collection<T> actual)
    public static <K, V> OpenAssertions.MapAssertion<K, V> assertThat(Map<K, V> actual)
    public static OpenAssertions.NumberAssertion assertThat(Number actual)
    public static OpenAssertions.BooleanAssertion assertThat(Boolean actual)
    public static OpenAssertions.ThrowableAssertion assertThatThrownBy(Runnable runnable)
    public static void assertThatCode(Runnable runnable)

    // === Mock ===
    public static <T> MockBuilder<T> mock(Class<T> interfaceType)
    public static <T> T quickMock(Class<T> interfaceType)
    public static Spy spy()

    // === 基准测试 ===
    public static Duration time(Runnable runnable)
    public static <T> Benchmark.TimedResult<T> time(Supplier<T> supplier)
    public static Benchmark.BenchmarkResult benchmark(String name, Runnable runnable)
    public static Benchmark.ComparisonResult compare(String name1, Runnable r1,
                                                     String name2, Runnable r2)

    // === 随机数据 ===
    public static String randomString(int length)
    public static String randomEmail()
    public static String randomPhone()
    public static String randomName()
    public static String uuid()
    public static int randomInt(int max)
    public static int randomInt(int min, int max)
    public static double randomDouble()
    public static boolean randomBoolean()
    public static byte[] randomBytes(int length)
    public static <T> T oneOf(T... options)
}
```

```java
// 一站式使用
import static cloud.opencode.base.test.OpenTest.*;

// 流式断言
assertThat(user.getName()).isNotBlank().contains("张");
assertThat(scores).isNotEmpty().hasSize(3);
assertThat(count).isPositive().isLessThan(100);

// Mock
UserService mock = OpenTest.mock(UserService.class)
    .when("findById", new User(1L, "test"))
    .build();

// 基准测试
Duration elapsed = OpenTest.time(() -> service.process(data));

// 随机数据
String phone = OpenTest.randomPhone();
String email = OpenTest.randomEmail();
```

### 3.3 OpenAssertions - 流式断言

提供 AssertJ 风格的流式断言 API。

```java
public final class OpenAssertions {

    // === 入口方法 ===
    public static <T> ObjectAssertion<T> assertThat(T actual)
    public static StringAssertion assertThat(String actual)
    public static <T> CollectionAssertion<T> assertThat(Collection<T> actual)
    public static <K, V> MapAssertion<K, V> assertThat(Map<K, V> actual)
    public static NumberAssertion assertThat(Number actual)
    public static BooleanAssertion assertThat(Boolean actual)
    public static ThrowableAssertion assertThatThrownBy(Runnable runnable)
    public static void assertThatCode(Runnable runnable)

    // === ObjectAssertion<T> ===
    isNull() / isNotNull() / isEqualTo(T) / isNotEqualTo(T)
    isSameAs(T) / isInstanceOf(Class<?>) / matches(Predicate<T>) / satisfies(Consumer<T>)

    // === StringAssertion (extends ObjectAssertion<String>) ===
    isEmpty() / isNotEmpty() / isBlank() / isNotBlank()
    contains(String) / startsWith(String) / endsWith(String)
    hasLength(int) / matchesRegex(String)

    // === CollectionAssertion<T> (extends ObjectAssertion<Collection<T>>) ===
    isEmpty() / isNotEmpty() / hasSize(int) / contains(T) / doesNotContain(T)
    containsAll(T...)

    // === MapAssertion<K,V> (extends ObjectAssertion<Map<K,V>>) ===
    isEmpty() / isNotEmpty() / hasSize(int) / containsKey(K) / containsValue(V)
    containsEntry(K, V)

    // === NumberAssertion (extends ObjectAssertion<Number>) ===
    isZero() / isPositive() / isNegative() / isGreaterThan(Number) / isLessThan(Number)
    isBetween(Number, Number)

    // === BooleanAssertion (extends ObjectAssertion<Boolean>) ===
    isTrue() / isFalse()

    // === ThrowableAssertion (extends ObjectAssertion<Throwable>) ===
    isInstanceOf(Class<?>) / hasMessage(String) / hasMessageContaining(String)
    hasCauseInstanceOf(Class<?>)
}
```

### 3.4 专项断言类

#### CollectionAssert - 集合断言

```java
public final class CollectionAssert<E> {
    public static <E> CollectionAssert<E> assertThat(Collection<E> actual)

    isNull() / isNotNull() / isEmpty() / isNotEmpty()
    hasSize(int) / hasSizeGreaterThan(int) / hasSizeLessThan(int)
    contains(E) / doesNotContain(E) / containsAll(E...) / containsExactly(E...)
    containsExactlyInAnyOrder(E...)
    allMatch(Predicate<E>) / anyMatch(Predicate<E>) / noneMatch(Predicate<E>)
    hasNoDuplicates() / isSorted(Comparator<E>) / isEqualTo(Collection<E>)
}
```

#### StringAssert - 字符串断言

```java
public final class StringAssert {
    public static StringAssert assertThat(String actual)

    isNull() / isNotNull() / isEmpty() / isNotEmpty() / isBlank() / isNotBlank()
    hasLength(int) / hasLengthBetween(int, int)
    isEqualTo(String) / isEqualToIgnoringCase(String)
    contains(String) / doesNotContain(String) / startsWith(String) / endsWith(String)
    matches(String) / matches(Pattern)
    containsOnlyDigits() / containsOnlyLetters()
    isUpperCase() / isLowerCase() / isEqualToIgnoringWhitespace(String)
}
```

#### NumberAssert - 数值断言

```java
public final class NumberAssert<T extends Number & Comparable<T>> {
    public static <T extends Number & Comparable<T>> NumberAssert<T> assertThat(T actual)
    public static NumberAssert<Integer> assertThat(int actual)
    public static NumberAssert<Long> assertThat(long actual)
    public static NumberAssert<Double> assertThat(double actual)

    isNull() / isNotNull() / isEqualTo(T) / isZero() / isNotZero()
    isPositive() / isNegative() / isNotNegative() / isNotPositive()
    isGreaterThan(T) / isGreaterThanOrEqualTo(T)
    isLessThan(T) / isLessThanOrEqualTo(T)
    isBetween(T, T) / isStrictlyBetween(T, T) / isCloseTo(T, T)
    isEven() / isOdd()
}
```

#### ExceptionAssert - 异常断言

```java
public final class ExceptionAssert {
    public interface ThrowableRunner { void run() throws Throwable; }

    public static ExceptionAssert assertThatThrownBy(ThrowableRunner runner)
    public static ExceptionAssert assertThat(Throwable throwable)
    public static ExceptionAssert assertThatCode(ThrowableRunner runner)

    doesNotThrowAnyException()
    isInstanceOf(Class<?>) / isExactlyInstanceOf(Class<?>)
    hasMessage(String) / hasMessageContaining(String)
    hasMessageStartingWith(String) / hasMessageEndingWith(String)
    hasCause() / hasNoCause() / hasCauseInstanceOf(Class<?>)
    hasRootCauseInstanceOf(Class<?>)
    <T extends Throwable> T getThrowable()
}
```

```java
ExceptionAssert.assertThatThrownBy(() -> service.validate(null))
    .isInstanceOf(IllegalArgumentException.class)
    .hasMessageContaining("must not be null")
    .hasNoCause();

ExceptionAssert.assertThatCode(() -> service.process(validData))
    .doesNotThrowAnyException();
```

#### JsonAssert - JSON 断言

```java
public final class JsonAssert {
    public static JsonAssert assertThat(String actual)

    isNull() / isNotNull() / isValidJson()
    isJsonObject() / isJsonArray()
    containsKey(String) / doesNotContainKey(String) / containsValue(String)
    hasKeyValue(String, String) / hasKeyValue(String, Number) / hasKeyValue(String, boolean)
    hasNullValue(String)
    isEqualTo(String) / hasLength(int)
    isEmptyObject() / isEmptyArray()
}
```

```java
JsonAssert.assertThat(jsonStr)
    .isValidJson()
    .isJsonObject()
    .containsKey("name")
    .hasKeyValue("age", 25)
    .doesNotContainKey("password");
```

#### SoftAssert - 软断言

收集多个断言失败后统一抛出。

```java
public final class SoftAssert {
    public SoftAssert()

    // 空值断言
    isNull(Object) / isNull(Object, String) / isNull(Object, Supplier<String>)
    isNotNull(Object) / isNotNull(Object, String) / isNotNull(Object, Supplier<String>)

    // 相等断言
    isEqualTo(Object, Object) / isEqualTo(Object, Object, String)
    isNotEqualTo(Object, Object) / isNotEqualTo(Object, Object, String)

    // 布尔断言
    isTrue(boolean) / isTrue(boolean, String) / isFalse(boolean) / isFalse(boolean, String)

    // 字符串断言
    isEmpty(String) / isNotEmpty(String)
    contains(String, String) / startsWith(String, String) / endsWith(String, String)

    // 数值断言
    isGreaterThan(Number, Number) / isGreaterThanOrEqualTo(Number, Number)
    isLessThan(Number, Number) / isLessThanOrEqualTo(Number, Number)
    isBetween(Number, Number, Number)

    // 验证
    void assertAll()                   // 有失败则抛出
    void assertAll(String heading)     // 带标题的断言
    boolean hasFailures()
    int getFailureCount()
    List<AssertionError> getFailures()
    SoftAssert reset()
}
```

```java
SoftAssert soft = new SoftAssert();
soft.isNotNull(user, "user should not be null");
soft.isEqualTo("张三", user.getName(), "name mismatch");
soft.isTrue(user.isActive(), "user should be active");
soft.isGreaterThan(user.getAge(), 0, "age should be positive");
soft.assertAll("User validation");
// 如果有多个失败，抛出包含所有失败信息的异常
```

#### AssertionResult - 断言结果（sealed interface）

```java
public sealed interface AssertionResult
    permits AssertionResult.Success, AssertionResult.Failure {

    boolean passed();

    record Success() implements AssertionResult {
        public boolean passed()     // 返回 true
    }

    record Failure(String message, Object expected, Object actual)
        implements AssertionResult {
        public boolean passed()     // 返回 false
        public boolean hasValues()  // expected和actual是否有值
    }
}
```

### 3.5 OpenMock - Mock 入口

```java
public final class OpenMock {

    /** 创建Mock对象（返回默认值） */
    public static <T> T mock(Class<T> type)

    /** 创建带桩定义的Mock（Builder模式） */
    public static <T> MockBuilder<T> when(Class<T> type)

    /** 验证Mock调用 */
    public static <T> Verification<T> verify(T mock)

    /** 重置Mock */
    public static void reset(Object mock)

    /** 调用记录 */
    public record Invocation(String methodName, Class<?>[] parameterTypes, Object[] args)

    /** Mock构建器 */
    public static class MockBuilder<T> {
        public MockBuilder<T> thenReturn(String methodName, Object returnValue)
        public MockBuilder<T> thenReturn(String methodName, Object[] args, Object returnValue)
        public T build()
    }

    /** 验证器 */
    public static class Verification<T> {
        public Verification<T> wasInvoked(String methodName)
        public Verification<T> wasInvoked(String methodName, int times)
        public Verification<T> wasNeverInvoked(String methodName)
        public int invocationCount(String methodName)
        public List<Invocation> getAllInvocations()
    }
}
```

```java
// 简单Mock
UserService mock = OpenMock.mock(UserService.class);
mock.findById(1L);  // 返回null（默认值）

// 带桩Mock
UserService service = OpenMock.when(UserService.class)
    .thenReturn("findById", new User(1L, "张三"))
    .thenReturn("count", 42)
    .build();
User user = service.findById(1L);  // 返回User(1, "张三")

// 验证调用
OpenMock.verify(service)
    .wasInvoked("findById")
    .wasInvoked("findById", 1)
    .wasNeverInvoked("deleteById");
```

### 3.6 MockBuilder / MockProxy / MockInvocationHandler

#### MockBuilder - Mock构建器

```java
public class MockBuilder<T> {
    public static <T> MockBuilder<T> of(Class<T> interfaceType)

    /** 定义方法返回值 */
    public MockBuilder<T> when(String methodName, Object returnValue)

    /** 定义方法处理函数 */
    public MockBuilder<T> when(String methodName, Function<Object[], Object> handler)

    /** 设置默认返回值 */
    public MockBuilder<T> defaultReturn(Object defaultValue)

    /** 设置默认处理函数 */
    public MockBuilder<T> defaultHandler(Function<Method, Object> handler)

    /** 构建Mock代理 */
    public T build()

    /** 快速创建空Mock */
    public static <T> T mock(Class<T> interfaceType)
}
```

#### MockProxy - Mock代理工厂

```java
public final class MockProxy {
    /** 创建单接口Mock */
    public static <T> T create(Class<T> interfaceType)

    /** 创建多接口Mock */
    public static Object create(Class<?>... interfaces)

    /** 获取Mock的InvocationHandler */
    public static MockInvocationHandler getHandler(Object mock)

    /** 判断是否为Mock对象 */
    public static boolean isMock(Object object)

    /** 获取调用记录 */
    public static List<Invocation> getInvocations(Object mock)

    /** 清除调用记录 */
    public static void clearInvocations(Object mock)

    /** 重置Mock（清除桩和调用记录） */
    public static void reset(Object mock)

    /** 创建验证器 */
    public static MockVerification verify(Object mock)
    public static MockVerification verify(Object mock, int times)

    public static class MockVerification {
        public void called(String methodName, Object... args)
        public void neverCalled(String methodName)
    }
}
```

#### MockInvocationHandler - 调用处理器

```java
public final class MockInvocationHandler implements InvocationHandler {
    public MockInvocationHandler(Class<?> mockedType)

    /** 配置桩定义 */
    public Stubbing when(String methodName, Object... args)

    /** 查询调用记录 */
    public List<Invocation> getInvocations()
    public List<Invocation> getInvocations(String methodName)
    public int countInvocations(String methodName)
    public int countInvocations(String methodName, Object... args)
    public void clearInvocations()
    public void reset()
    public Class<?> getMockedType()

    /** 桩定义 */
    public static class Stubbing {
        public Stubbing thenReturn(Object value)
        public Stubbing thenThrow(Throwable throwable)
        public Stubbing thenAnswer(Function<Object[], Object> answer)
        public Stubbing thenCallRealMethod()
    }
}
```

```java
// 高级Mock用法
UserService mock = MockProxy.create(UserService.class);
MockInvocationHandler handler = MockProxy.getHandler(mock);

// 定义桩
handler.when("findById", 1L).thenReturn(new User(1L, "张三"));
handler.when("findById", 2L).thenThrow(new UserNotFoundException());
handler.when("search").thenAnswer(args -> searchByKeyword((String) args[0]));

// 使用
User user = mock.findById(1L);  // 返回User

// 验证
MockProxy.verify(mock).called("findById", 1L);
MockProxy.verify(mock, 1).called("findById", 1L);  // 恰好调用1次
```

#### Spy - 调用记录器

```java
public class Spy {
    public void record(String methodName, Object... args)
    public boolean wasCalled(String methodName)
    public boolean wasCalledTimes(String methodName, int times)
    public int getCallCount(String methodName)
    public List<Invocation> getInvocations()
    public List<Invocation> getInvocations(String methodName)
    public Invocation getLastInvocation()
    public void clear()
    public boolean noInteractions()

    public record Invocation(String methodName, Object[] args) {
        public Object getArg(int index)
        public int getArgCount()
    }
}
```

#### Invocation - 调用记录（Record）

```java
public record Invocation(Method method, Object[] args, Instant timestamp) {
    public static Invocation of(Method method, Object[] args)
    public String methodName()
    public Class<?> returnType()
    public Class<?>[] parameterTypes()
    public boolean argsMatch(Object... expectedArgs)
    public boolean isMethod(String name)
}
```

### 3.7 OpenData - 测试数据生成入口

```java
public final class OpenData {

    // === 随机数值 ===
    public static int randomInt()
    public static int randomInt(int bound)
    public static int randomInt(int min, int max)
    public static long randomLong()
    public static long randomLong(long min, long max)
    public static double randomDouble()
    public static double randomDouble(double min, double max)
    public static float randomFloat()
    public static float randomFloat(float min, float max)
    public static boolean randomBoolean()

    // === 随机字符串 ===
    public static String randomString(int length)
    public static String randomString(int length, String characters)
    public static String randomAlphabetic(int length)
    public static String randomNumeric(int length)
    public static String uuid()
    public static byte[] randomBytes(int length)
    public static String randomHex(int length)

    // === 人物数据 ===
    public static String chineseName()
    public static String englishName()
    public static String email()
    public static String phone()
    public static String city()
    public static int age(int min, int max)

    // === 日期数据 ===
    public static LocalDate pastDate(int daysBack)
    public static LocalDate futureDate(int daysForward)
    public static LocalDateTime pastDateTime(int hoursBack)
    public static LocalDate birthday(int minAge, int maxAge)
    public static LocalDate randomDate(LocalDate start, LocalDate end)
    public static LocalDateTime randomDateTime()
    public static LocalDateTime randomDateTime(LocalDateTime start, LocalDateTime end)

    // === 金额数据 ===
    public static BigDecimal randomMoney()
    public static BigDecimal randomMoney(double min, double max)
    public static BigDecimal randomPrice(int min, int max)

    // === 集合操作 ===
    public static <T> T pick(T[] array)
    public static <T> T pick(List<T> list)
    public static <T> T pick(Collection<T> collection)
    public static <T> List<T> pickMany(List<T> list, int count)
    public static <T> List<T> shuffle(List<T> list)
    public static <T> List<T> listOf(int count, Supplier<T> supplier)

    // === 可重复随机 ===
    public static void withSeed(long seed, Runnable action)
    public static <T> T withSeed(long seed, Supplier<T> supplier)
}
```

```java
// 随机数据
String name = OpenData.chineseName();       // "张伟"
String email = OpenData.email();            // "zhang@example.com"
String phone = OpenData.phone();            // "13812345678"
int age = OpenData.age(18, 60);
BigDecimal price = OpenData.randomPrice(10, 1000);

// 批量生成
List<String> names = OpenData.listOf(10, OpenData::chineseName);

// 可重复随机（调试用）
OpenData.withSeed(12345, () -> {
    String s1 = OpenData.randomString(10);  // 每次运行结果相同
    int n1 = OpenData.randomInt(1, 100);
});
```

### 3.8 DataGenerator - 通用数据生成器

```java
public final class DataGenerator {
    // 字符串
    public static String string(int length)
    public static String alpha(int length)
    public static String numeric(int length)
    public static String randomString(int length, String chars)

    // 数值
    public static int intBetween(int min, int max)
    public static long longBetween(long min, long max)
    public static double doubleBetween(double min, double max)
    public static BigDecimal decimal(double min, double max, int scale)
    public static boolean bool()
    public static boolean bool(double trueProbability)

    // 日期
    public static LocalDate dateBetween(LocalDate start, LocalDate end)
    public static LocalDateTime dateTimeBetween(LocalDateTime start, LocalDateTime end)
    public static Instant instantBetween(Instant start, Instant end)

    // 集合
    public static <T> List<T> list(int size, Supplier<T> supplier)
    public static <K, V> Map<K, V> map(int size, Supplier<K> keySupplier,
                                        Supplier<V> valueSupplier)
    public static byte[] bytes(int length)
    public static <T> T oneOf(T... elements)
    public static <T> T oneOf(List<T> elements)

    // Record实例化
    public static <T extends Record> T record(Class<T> recordClass)
}
```

```java
// 生成Record实例（自动填充字段）
record User(long id, String name, String email) {}
User user = DataGenerator.record(User.class);
```

### 3.9 Faker - 假数据生成

生成逼真的测试数据。

```java
public final class Faker {
    // 姓名
    public static String firstName()
    public static String lastName()
    public static String name()                // 英文姓名
    public static String chineseName()         // 中文姓名

    // 联系方式
    public static String email()
    public static String phone()               // 国际手机号
    public static String chinesePhone()        // 中国手机号

    // 地址
    public static String city()
    public static String chineseCity()
    public static String streetAddress()
    public static String chineseStreetAddress()
    public static String address()             // 完整英文地址
    public static String chineseAddress()      // 完整中文地址
    public static String state()
    public static String zipCode()
    public static String chinesePostalCode()

    // 公司
    public static String company()
    public static String chineseCompany()

    // 网络
    public static String username()
    public static String domainName()
    public static String url()
    public static String ipv4()

    // 日期
    public static LocalDate pastDate(int yearsBack)
    public static LocalDate futureDate(int yearsAhead)
    public static LocalDate birthday(int minAge, int maxAge)

    // 文本
    public static String word()
    public static String sentence(int wordCount)
    public static String paragraph(int sentenceCount)
}
```

### 3.10 RandomData - 随机数据工具

生成UUID、密钥、令牌等随机标识数据。

```java
public final class RandomData {
    // UUID
    public static String uuid()            // 标准UUID
    public static String shortUuid()       // 短UUID
    public static String compactUuid()     // 去横线UUID

    // 字节与编码
    public static byte[] bytes(int length)
    public static String hex(int byteLength)
    public static String base64(int byteLength)
    public static String base64Url(int byteLength)

    // 哈希
    public static String md5()
    public static String sha256()
    public static String sha512()

    // 令牌与密钥
    public static String apiKey()
    public static String secretKey()
    public static String accessToken()
    public static String refreshToken()

    // 编码
    public static String numericCode(int length)
    public static String alphanumericCode(int length)
    public static String sequenceId(String prefix)
    public static String orderNumber()
    public static String transactionId()
}
```

### 3.11 SensitiveDataGenerator - 敏感数据生成

生成格式正确但非真实的测试敏感数据。

```java
public final class SensitiveDataGenerator {
    /** 测试手机号（199号段，非真实） */
    public static String testPhone()
    public static String testPhone(String prefix)

    /** 测试身份证号（999999地区码，校验位有效） */
    public static String testIdCard()
    public static String testIdCard(int birthYear)

    /** 测试银行卡号（622848 BIN，Luhn校验有效） */
    public static String testBankCard()
    public static String testBankCard(String bin)

    /** 测试邮箱 */
    public static String testEmail()
    public static String testEmail(String domain)

    /** 测试统一社会信用代码 */
    public static String testSocialCreditCode()
}
```

### 3.12 RepeatableRandom - 可重复随机生成器

使用固定种子实现可重复的随机数据生成，方便调试。

```java
public final class RepeatableRandom {
    public RepeatableRandom(long seed)
    public static RepeatableRandom withRandomSeed()
    public static RepeatableRandom withSeed(long seed)

    public long getSeed()
    public int nextInt() / nextInt(int bound) / nextInt(int min, int max)
    public long nextLong() / nextLong(long bound)
    public double nextDouble() / nextDouble(double min, double max)
    public boolean nextBoolean() / nextBoolean(double trueProbability)
    public String nextString(int length) / nextDigits(int length)
    public <T> T nextElement(T[] array)
    public RepeatableRandom reset()    // 重置到初始种子
}
```

```java
// 两个相同种子的实例产生相同序列
RepeatableRandom r1 = new RepeatableRandom(12345L);
RepeatableRandom r2 = new RepeatableRandom(12345L);
assert r1.nextInt(100) == r2.nextInt(100);  // 相同值
assert r1.nextString(10).equals(r2.nextString(10));  // 相同字符串
```

---

## 4. 性能基准测试

### 4.1 Benchmark - 基准测试工具

```java
public final class Benchmark {
    /** 计时执行 */
    public static Duration time(Runnable runnable)
    public static <T> TimedResult<T> time(Supplier<T> supplier)

    /** 运行基准测试（含预热） */
    public static BenchmarkResult run(String name, Runnable runnable,
                                      int warmupIterations, int measureIterations)
    public static BenchmarkResult run(String name, Runnable runnable)

    /** 对比两个实现 */
    public static ComparisonResult compare(String name1, Runnable r1,
                                           String name2, Runnable r2)

    public record TimedResult<T>(T result, Duration duration) {
        public long millis()
        public long nanos()
    }

    public static class BenchmarkResult {
        public String getName()
        public long getMin() / getMax() / getCount()
        public double getAverage()
        public Duration getMinDuration() / getMaxDuration() / getAverageDuration()
        public double getOpsPerSecond()
    }

    public record ComparisonResult(BenchmarkResult first, BenchmarkResult second) {
        public double getSpeedup()
        public String getFaster()
    }
}
```

### 4.2 BenchmarkResult - 基准结果（Record）

```java
public record BenchmarkResult(String name, long[] timesNanos) {
    public int iterations()
    public double averageMs() / averageNanos()
    public double minMs() / maxMs()
    public double percentileMs(int percentile)   // 百分位
    public double medianMs()                     // 中位数
    public double p95Ms() / p99Ms()              // P95/P99
    public long totalMs()
    public double throughputPerSecond()
    public double stdDevMs()                     // 标准差
    public String summary()
}
```

### 4.3 BenchmarkRunner - 可配置基准运行器

```java
public final class BenchmarkRunner {
    public static BenchmarkRunner create()
    public static BenchmarkResult runSingle(String name, Runnable runnable)

    public BenchmarkRunner warmup(int iterations)        // 预热次数
    public BenchmarkRunner iterations(int iterations)    // 测量次数
    public BenchmarkRunner timeout(Duration timeout)     // 超时
    public BenchmarkRunner output(PrintStream output)    // 输出流
    public BenchmarkRunner verbose()                     // 详细输出

    public BenchmarkRunner add(String name, Runnable runnable)
    public <T> BenchmarkRunner add(String name, Callable<T> callable)

    public List<BenchmarkResult> run()                   // 执行所有
    public List<BenchmarkResult> getResults()
    public BenchmarkRunner printResults()                // 打印结果
    public BenchmarkRunner printComparison()             // 打印对比
    public BenchmarkRunner onComplete(Consumer<List<BenchmarkResult>> consumer)
}
```

```java
// 基准测试示例
BenchmarkRunner.create()
    .warmup(100)
    .iterations(10000)
    .add("StringBuilder", () -> {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 100; i++) sb.append("x");
        sb.toString();
    })
    .add("StringBuffer", () -> {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 100; i++) sb.append("x");
        sb.toString();
    })
    .run()
    .forEach(r -> System.out.printf("%s: avg=%.3fms, p99=%.3fms, ops=%.0f/s%n",
        r.name(), r.averageMs(), r.p99Ms(), r.throughputPerSecond()));
```

---

## 5. 并发测试

### 5.1 ConcurrentTester - 并发测试器

```java
public final class ConcurrentTester {
    /** 并发执行任务（指定线程数和每线程迭代次数） */
    public static ConcurrentResult runConcurrently(Runnable task, int threads, int iterations)

    /** 并发执行任务（每个线程传入线程编号） */
    public static ConcurrentResult runConcurrently(Consumer<Integer> task, int threads)

    /** 断言操作是线程安全的 */
    public static void assertThreadSafe(Runnable task, int threads, int iterations)

    public record ConcurrentResult(
        int threads, int iterations, long durationMs, List<Throwable> errors
    ) {
        public int totalIterations()
        public double throughput()       // 操作/秒
        public boolean allSucceeded()
    }
}
```

### 5.2 ThreadSafetyChecker - 线程安全检测

```java
public final class ThreadSafetyChecker {
    /** 检测计数器线程安全 */
    public static CheckResult checkCounter(Supplier<Object> counterFactory,
                                           Consumer<Object> increment,
                                           Function<Object, Integer> getValue,
                                           int threads, int iterations)

    /** 检测操作线程安全 */
    public static boolean isThreadSafe(Runnable operation, int threads, int iterations)

    public record CheckResult(boolean passed, int expected, int actual, int difference) {
        public double accuracy()
    }
}
```

```java
// 并发测试示例
ConcurrentTester.assertThreadSafe(() -> {
    cache.put("key", "value");
    cache.get("key");
}, 10, 1000);

// 检测AtomicInteger线程安全
var result = ThreadSafetyChecker.checkCounter(
    AtomicInteger::new,
    counter -> ((AtomicInteger) counter).incrementAndGet(),
    counter -> ((AtomicInteger) counter).get(),
    10, 10000);
assert result.passed();
```

---

## 6. 测试资源与上下文

### 6.1 ResourceLoader - 资源加载

```java
public final class ResourceLoader {
    // 从classpath加载
    public static String loadString(String resourcePath)
    public static String loadString(String resourcePath, Charset charset)
    public static Optional<String> loadStringOptional(String resourcePath)
    public static List<String> loadLines(String resourcePath)
    public static List<String> loadLines(String resourcePath, Charset charset)
    public static byte[] loadBytes(String resourcePath)
    public static Properties loadProperties(String resourcePath)
    public static InputStream getResourceStream(String resourcePath)
    public static Optional<URL> getResourceURL(String resourcePath)
    public static boolean exists(String resourcePath)

    // 从文件系统加载
    public static String loadFile(Path path)
    public static String loadFile(Path path, Charset charset)
    public static List<String> loadFileLines(Path path)
    public static byte[] loadFileBytes(Path path)
}
```

```java
// 加载测试JSON
String json = ResourceLoader.loadString("test-data/users.json");

// 加载测试配置
Properties props = ResourceLoader.loadProperties("test-config.properties");

// 检查资源是否存在
if (ResourceLoader.exists("expected-output.txt")) {
    String expected = ResourceLoader.loadString("expected-output.txt");
}
```

### 6.2 TestContext - 测试上下文

基于 JDK 25 `ScopedValue` 的测试执行上下文，提供线程安全的变量存储和生命周期钩子。

```java
public final class TestContext {
    public static final ScopedValue<TestContext> CURRENT = ScopedValue.newInstance();

    public interface FailureCallback {
        void onFailure(TestContext context, Throwable exception);
    }

    // === 创建 ===
    public static TestContext create(String testName)
    public static TestContext create()

    // === 运行 ===
    public static <T, X extends Throwable> T run(TestContext context, CallableOp<T, X> task)
    public static void run(TestContext context, Runnable task)

    // === 获取当前上下文 ===
    public static Optional<TestContext> current()
    public static TestContext currentOrCreate()
    public static TestContext currentOrCreate(String testName)

    // === 变量管理 ===
    public TestContext setVariable(String key, Object value)
    public <T> Optional<T> getVariable(String key)
    public <T> T getVariable(String key, T defaultValue)
    public boolean hasVariable(String key)
    public TestContext removeVariable(String key)
    public Map<String, Object> variables()

    // === 属性管理 ===
    public TestContext setAttribute(String key, Object value)
    public <T> Optional<T> getAttribute(String key)
    public <T> T getAttribute(String key, T defaultValue)
    public boolean hasAttribute(String key)
    public Map<String, Object> attributes()

    // === 生命周期钩子 ===
    public TestContext onSuccess(Consumer<TestContext> callback)
    public TestContext onFailure(FailureCallback callback)

    // === 状态查询 ===
    public String testName()
    public Instant startTime()
    public Optional<Instant> endTime()
    public Duration duration()
    public String status()
    public boolean isPassed()
    public boolean isFailed()
    public Optional<Throwable> exception()
}
```

```java
TestContext context = TestContext.create("userServiceTest");
context.onSuccess(ctx -> System.out.println("通过: " + ctx.testName()));
context.onFailure((ctx, ex) -> System.out.println("失败: " + ex.getMessage()));

TestContext.run(context, () -> {
    TestContext current = TestContext.current().orElseThrow();
    current.setVariable("userId", 1L);
    current.setAttribute("component", "UserService");
    // 执行测试...
});
```

---

## 7. 测试夹具

### 7.1 TestFixture - 测试夹具

```java
public class TestFixture<T> {
    public TestFixture(String name, Supplier<T> setup)
    public TestFixture(String name, Supplier<T> setup, Consumer<T> teardown)
    public static <T> Builder<T> builder(String name)

    public String getName()
    public T setUp()          // 初始化并返回数据
    public T get()            // 获取数据（未初始化则先setUp）
    public void tearDown()    // 清理
    public boolean isInitialized()
    public void reset()       // 重置状态

    public static class Builder<T> {
        public Builder<T> setup(Supplier<T> setup)
        public Builder<T> teardown(Consumer<T> teardown)
        public TestFixture<T> build()
    }
}
```

### 7.2 FixtureRegistry - 夹具注册表

```java
public final class FixtureRegistry {
    public static <T> void register(TestFixture<T> fixture)
    public static <T> void register(String name, Supplier<T> supplier)
    public static <T> TestFixture<T> get(String name)
    public static <T> T getData(String name)
    public static boolean exists(String name)
    public static void unregister(String name)
    public static void resetAll()
    public static void tearDownAll()
    public static void clear()
    public static int size()
}
```

```java
// 注册夹具
FixtureRegistry.register(TestFixture.<DataSource>builder("dataSource")
    .setup(() -> createTestDataSource())
    .teardown(ds -> ds.close())
    .build());

// 使用夹具
DataSource ds = FixtureRegistry.getData("dataSource");

// 测试结束后清理
FixtureRegistry.tearDownAll();
```

---

## 8. 异步等待

### 8.1 Wait - 等待工具

```java
public final class Wait {
    /** 等待条件满足 */
    public static void until(BooleanSupplier condition, Duration timeout)
    public static void until(BooleanSupplier condition, Duration timeout, Duration pollInterval)
    public static void until(BooleanSupplier condition)   // 默认10秒超时

    /** 等待非空结果 */
    public static <T> T untilNotNull(Supplier<T> supplier, Duration timeout)
    public static <T> T untilNotNull(Supplier<T> supplier, Duration timeout, Duration pollInterval)

    /** 固定等待 */
    public static void forDuration(Duration duration)
    public static void forMillis(long millis)
    public static void forSeconds(long seconds)
}
```

### 8.2 Poller - 轮询器

```java
public final class Poller {
    public static Poller await()

    public Poller timeout(Duration timeout)
    public Poller pollInterval(Duration interval)
    public Poller describedAs(String description)

    /** 等待条件为true */
    public void until(BooleanSupplier condition)

    /** 等待结果满足断言 */
    public <T> T until(Supplier<T> supplier, Predicate<T> predicate)

    /** 等待结果非空 */
    public <T> T untilNotNull(Supplier<T> supplier)

    /** 等待结果等于期望值 */
    public <T> T untilEquals(Supplier<T> supplier, T expected)
}
```

```java
// 等待异步操作完成
Wait.until(() -> service.isReady(), Duration.ofSeconds(30));

// 轮询等待
String result = Poller.await()
    .timeout(Duration.ofSeconds(10))
    .pollInterval(Duration.ofMillis(200))
    .describedAs("等待任务完成")
    .untilNotNull(() -> taskQueue.getResult(taskId));
```

---

## 9. 测试报告

### 9.1 TestReport - 测试报告

```java
public class TestReport {
    public TestReport(String name)

    public void add(TestResult result)
    public void addPassed(String testName, Duration duration)
    public void addFailed(String testName, Duration duration, Throwable error)
    public void addSkipped(String testName, String reason)
    public void complete()

    // 查询
    public String getName()
    public List<TestResult> getResults()
    public int getTotalCount() / getPassedCount() / getFailedCount() / getSkippedCount()
    public Duration getTotalDuration()
    public boolean allPassed()
    public double getSuccessRate()
    public String getSummary()

    // 兼容接口
    public String suiteName() / int totalTests() / int passedTests()
    public int failedTests() / int skippedTests() / double successRate()
    public long durationMs() / List<TestResult> testCases()

    public enum TestStatus { PASSED, FAILED, SKIPPED }

    public record TestResult(String testName, TestStatus status, Duration duration,
                             String message, Throwable error) {
        public static TestResult passed(String testName, Duration duration)
        public static TestResult failed(String testName, Duration duration, Throwable error)
        public static TestResult skipped(String testName, String reason)
        public boolean passed()
        public long durationMs()
        public String errorMessage()
    }
}
```

### 9.2 ReportGenerator - 报告生成器

```java
public final class ReportGenerator {
    /** 生成文本报告 */
    public static String toText(TestReport report)
    public static void writeText(TestReport report, Writer writer)

    /** 生成HTML报告 */
    public static String toHtml(TestReport report)
    public static void writeHtml(TestReport report, Writer writer)
    public static void writeHtml(TestReport report, Path path)

    /** 生成JSON报告 */
    public static String toJson(TestReport report)
    public static void writeJson(TestReport report, Path path)
}
```

### 9.3 TestReportFormatter - 报告格式化

```java
public final class TestReportFormatter {
    public static String toText(TestReport report)
    public static String toJUnitXml(TestReport report)
    public static String toMarkdown(TestReport report)
}
```

```java
// 构建测试报告
TestReport report = new TestReport("UserServiceTest");
report.addPassed("testFindById", Duration.ofMillis(50));
report.addFailed("testCreateUser", Duration.ofMillis(120),
    new AssertionException("name mismatch"));
report.addSkipped("testDeleteUser", "Not implemented");
report.complete();

// 输出报告
System.out.println(ReportGenerator.toText(report));
ReportGenerator.writeHtml(report, Path.of("build/test-report.html"));
String xml = TestReportFormatter.toJUnitXml(report);
```

---

## 10. 测试注解

```java
/** 快速测试标记 - 标记为快速执行的测试 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FastTest {}

/** 慢速测试标记 - 标记为耗时较长的测试 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface SlowTest {}

/** 集成测试标记 - 标记为需要外部依赖的集成测试 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface IntegrationTest {}

/** 重复执行注解 - 标记测试需要重复执行 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Repeat {
    int value() default 1;
}
```

---

## 11. 异常体系

### 11.1 异常层次结构

```
TestException (RuntimeException)
├── AssertionException                # 断言失败
│   └── EqualsAssertionException      # 相等断言失败（含expected/actual）
├── MockException                     # Mock相关异常
├── DataGenerationException           # 数据生成异常
└── BenchmarkException                # 基准测试异常
```

### 11.2 TestErrorCode - 错误码

```java
public enum TestErrorCode {
    // 1xxx - 断言错误
    ASSERTION_FAILED, ASSERTION_NULL, ASSERTION_EQUALS, ASSERTION_TIMEOUT,
    ASSERTION_TYPE, ASSERTION_CONTAINS, ASSERTION_RANGE, ASSERTION_EXCEPTION,

    // 2xxx - Mock错误
    MOCK_CREATION_FAILED, MOCK_NOT_INTERFACE, MOCK_VERIFICATION_FAILED,

    // 3xxx - 数据生成错误
    DATA_GENERATION_FAILED, DATA_RANGE_INVALID,

    // 4xxx - 基准测试错误
    BENCHMARK_FAILED, BENCHMARK_TIMEOUT;

    public String code()       // "TEST-1001" 格式
    public String message()    // 错误描述
}
```

### 11.3 关键异常类

```java
/** 断言异常 */
public class AssertionException extends TestException {
    public static AssertionException failed(String message)
    public static AssertionException nullAssertion()
    public static AssertionException notEqual(Object expected, Object actual)
    public static AssertionException timeout(long timeoutMs, long actualMs)
}

/** 相等断言异常（包含期望值和实际值） */
public class EqualsAssertionException extends AssertionException {
    public Object getExpected()
    public Object getActual()
    public String getExpectedString()
    public String getActualString()
    public String getComparisonInfo()

    public static void assertEqualsOrThrow(Object expected, Object actual)
    public static void assertEqualsOrThrow(Object expected, Object actual, String message)
    public static EqualsAssertionException expectedNull(Object actual)
    public static EqualsAssertionException actualNull(Object expected)
}

/** Mock异常 */
public class MockException extends TestException {
    public static MockException creationFailed(Class<?> type)
    public static MockException notInterface(Class<?> type)
    public static MockException verificationFailed(String expected, int actual)
}
```

---

## 12. 线程安全与性能

### 12.1 线程安全设计

| 组件 | 线程安全策略 | 说明 |
|------|-------------|------|
| `OpenAssert` | 无状态 | 所有方法静态，天然线程安全 |
| `OpenData` | ThreadLocalRandom | 使用ThreadLocalRandom避免竞争 |
| `OpenMock` | 每次创建新实例 | 无共享状态 |
| `MockInvocationHandler` | CopyOnWriteArrayList + ConcurrentHashMap | 并发读写安全 |
| `TestContext` | ScopedValue | JDK 25 ScopedValue隔离 |
| `SoftAssert` | 非线程安全 | 设计为单线程使用 |
| `BenchmarkResult` | Record不可变 | 构建后不可修改 |
| `RepeatableRandom` | 非线程安全 | 设计为单线程使用 |

### 12.2 性能基准

| 操作 | 单次耗时 | 吞吐量 |
|------|----------|--------|
| OpenAssert.assertEquals | ~50ns | 20M ops/s |
| OpenMock.mock(Interface) | ~500ns | 2M ops/s |
| OpenData.randomString(10) | ~200ns | 5M ops/s |
| OpenData.randomPhone() | ~150ns | 6M ops/s |
| Faker.chineseName() | ~100ns | 10M ops/s |
| Benchmark.run(1000次) | ~10ms | 100 runs/s |

---

## 13. FAQ

### Q1: OpenAssert 与 JUnit Assertions 的区别？

| 特性 | OpenAssert | JUnit Assertions |
|------|-----------|-----------------|
| 集合断言 | assertSize, assertContains | 需组合多个断言 |
| 字符串断言 | assertBlank, assertMatches | assertTrue + String方法 |
| 数值比较 | assertBetween, assertGreaterThan | assertTrue + 比较运算符 |
| Map断言 | assertContainsKey, assertEmpty | 需手动编写 |
| 延迟消息 | 支持Supplier<String> | 支持Supplier<String> |

### Q2: OpenMock 只支持接口，如何 Mock 类？

OpenMock 基于 JDK Proxy，仅支持接口 Mock。对于类 Mock：
- 方案1：提取接口，面向接口编程
- 方案2：使用 Mockito（支持类Mock，需byte-buddy依赖）

### Q3: 如何进行软断言？

```java
SoftAssert soft = new SoftAssert();
soft.isEqualTo("张三", user.getName());
soft.isTrue(user.isActive());
soft.isGreaterThan(user.getAge(), 0);
soft.assertAll("用户验证");  // 收集所有失败后一次性抛出
```

### Q4: 如何使用可重复的随机数据调试？

```java
// 方式1：OpenData.withSeed
OpenData.withSeed(12345L, () -> {
    String s = OpenData.randomString(10);  // 每次运行相同
});

// 方式2：RepeatableRandom
RepeatableRandom r = new RepeatableRandom(12345L);
int n = r.nextInt(100);    // 每次运行相同
r.reset();                  // 重置到初始种子
```

### Q5: 如何生成安全的测试敏感数据？

```java
// 使用非真实号段，避免影响真实用户
String phone = SensitiveDataGenerator.testPhone();       // 199xxxxxxxx
String idCard = SensitiveDataGenerator.testIdCard();     // 999999开头
String bankCard = SensitiveDataGenerator.testBankCard(); // 622848开头
// 以上数据格式校验有效，但不属于真实号段
```

### Q6: 如何等待异步操作完成？

```java
// 简单等待
Wait.until(() -> service.isReady(), Duration.ofSeconds(30));

// 高级轮询
String result = Poller.await()
    .timeout(Duration.ofSeconds(10))
    .pollInterval(Duration.ofMillis(200))
    .describedAs("等待任务完成")
    .until(() -> service.getStatus(id), status -> "DONE".equals(status));
```

---

## 14. 版本信息

| 属性 | 值 |
|------|-----|
| 模块名 | opencode-base-test |
| 最低 JDK | 25 |
| 核心依赖 | opencode-base-core |
| 第三方依赖 | 无 |

---

*文档版本：3.0 | 更新日期：2026-02-27*

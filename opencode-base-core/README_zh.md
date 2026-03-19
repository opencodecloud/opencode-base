# OpenCode Base Core

核心工具库 -- 零依赖的基础库，提供基本类型、类型转换、反射、线程、原始数组操作等功能，适用于 JDK 25+。

## 功能特性

- 原始类型工具（数组、布尔、字符、数字、数学、位运算、十六进制、Base64、进制转换）
- 类型转换框架，可扩展的转换器注册表
- Bean 工具（拷贝、路径访问、属性描述符）
- Builder 模式支持（Bean、Record、Map 构建器）
- 反射工具（字段、方法、构造器、修饰符、Record、Unsafe）
- 线程工具（虚拟线程、ScopedValue、结构化并发、命名线程工厂）
- 元组类型（Pair、Triple、Quadruple）
- Stream 工具（Optional 扩展、并行流辅助）
- 前置条件检查与断言辅助
- 字符串连接/分割（Joiner、Splitter）
- Range、Ordering、Stopwatch、Singleton、SPI 加载器
- 受检函数式接口（Function、Consumer、Supplier、Predicate、Runnable、Callable）
- 分页支持（Page、PageRequest、Sort）
- 自定义异常体系（OpenException 及其子类型）
- 原始数组工具（Ints、Longs、Doubles、Floats、Shorts、Bytes、Chars、Booleans）

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

### 根工具类

| 类名 | 说明 |
|------|------|
| `OpenArray` | 全面的数组操作工具，支持原始类型和对象数组 |
| `OpenBase64` | Base64 编解码工具 |
| `OpenBit` | 位运算工具 |
| `OpenBoolean` | 布尔值转换和评估工具 |
| `OpenChar` | 字符类型检查和转换工具 |
| `OpenCharset` | 字符集检测和转换工具 |
| `OpenClass` | 类元数据和类型检查工具 |
| `OpenEnum` | 枚举查找和转换工具 |
| `OpenHex` | 十六进制编解码工具 |
| `OpenMath` | 带溢出保护的数学运算 |
| `OpenNumber` | 数字解析、比较和转换工具 |
| `OpenObject` | 对象相等性、哈希和空值安全操作 |
| `OpenRadix` | 进制（基数）转换工具 |
| `OpenStream` | Stream 创建和转换工具 |
| `OpenStringBase` | 基础字符串操作（空值安全、空白检查、裁剪） |
| `Joiner` | 流式字符串连接器，支持分隔符、前缀、后缀 |
| `Splitter` | 流式字符串分割器，支持正则和限制 |
| `MoreObjects` | ToStringHelper 和 firstNonNull 工具 |
| `Ordering` | 比较器构建器，支持链式调用和空值处理 |
| `Preconditions` | 参数和状态校验，带描述性消息 |
| `Range` | 不可变区间，支持开/闭/无界端点 |
| `Stopwatch` | 高精度计时器 |
| `Suppliers` | 带缓存和过期功能的 Supplier 包装器 |

### 注解

| 类名 | 说明 |
|------|------|
| `Experimental` | 标记 API 为实验性的（可能不经通知即更改） |

### 断言

| 类名 | 说明 |
|------|------|
| `OpenAssert` | 流式断言工具，用于参数校验 |

### Bean

| 类名 | 说明 |
|------|------|
| `OpenBean` | Bean 拷贝、属性访问和内省门面 |
| `BeanPath` | 嵌套属性路径访问（如 `user.address.city`） |
| `PropertyConverter` | Bean 属性类型转换接口 |
| `PropertyDescriptor` | Bean 属性元数据描述符 |

### 构建器

| 类名 | 说明 |
|------|------|
| `Builder` | 通用构建器接口 |
| `OpenBuilder` | 构建器工厂门面 |
| `BeanBuilder` | JavaBean 实例的流式构建器 |
| `RecordBuilder` | Record 实例的流式构建器 |
| `MapBuilder` | Map 实例的流式构建器 |

### 比较

| 类名 | 说明 |
|------|------|
| `CompareUtil` | 通用比较运算符分派（EQ/NE/LT/LE/GT/GE） |

### 容器

| 类名 | 说明 |
|------|------|
| `ContainerUtil` | 对 Collection、Map、Array、CharSequence、Optional 的通用 size/empty 操作 |

### 类型转换

| 类名 | 说明 |
|------|------|
| `Convert` | 类型转换门面 |
| `Converter` | 转换器接口 |
| `ConverterRegistry` | 可扩展的转换器注册表 |
| `TypeReference` | 泛型类型令牌，用于保留类型信息 |
| `TypeUtil` | 类型解析和检查工具 |
| `AttributeConverter` | 双向属性转换接口 |
| `StringConverter` | 字符串到目标类型的转换器 |
| `NumberConverter` | 数字到目标类型的转换器 |
| `DateConverter` | 日期时间类型转换器 |
| `ArrayConverter` | 数组类型转换器 |

### 异常

| 类名 | 说明 |
|------|------|
| `OpenException` | 所有 OpenCode 模块的基础运行时异常 |
| `OpenIOException` | I/O 相关异常 |
| `OpenIllegalArgumentException` | 非法参数异常 |
| `OpenIllegalStateException` | 非法状态异常 |
| `OpenTimeoutException` | 超时异常 |
| `OpenUnsupportedOperationException` | 不支持的操作异常 |
| `ExceptionUtil` | 异常包装、解包和堆栈跟踪工具 |

### 函数式接口

| 类名 | 说明 |
|------|------|
| `CheckedFunction` | 可抛出受检异常的 Function |
| `CheckedConsumer` | 可抛出受检异常的 Consumer |
| `CheckedSupplier` | 可抛出受检异常的 Supplier |
| `CheckedPredicate` | 可抛出受检异常的 Predicate |
| `CheckedRunnable` | 可抛出受检异常的 Runnable |
| `CheckedCallable` | 可抛出受检异常的 Callable |

### 分页

| 类名 | 说明 |
|------|------|
| `Page` | 分页结果容器，包含总数和内容 |
| `PageRequest` | 分页请求记录（页码、大小、排序） |
| `Sort` | 排序规范，包含属性名和方向 |

### 原始类型数组

| 类名 | 说明 |
|------|------|
| `Ints` | int 数组工具（contains、indexOf、min、max、sort、reverse） |
| `Longs` | long 数组工具 |
| `Doubles` | double 数组工具 |
| `Floats` | float 数组工具 |
| `Shorts` | short 数组工具 |
| `Bytes` | byte 数组工具 |
| `Chars` | char 数组工具 |
| `Booleans` | boolean 数组工具 |

### 随机

| 类名 | 说明 |
|------|------|
| `OpenRandom` | 安全随机数生成工具 |
| `IdGenerator` | 唯一 ID 生成器接口 |
| `VerifyCodeUtil` | 验证码生成工具 |

### 反射

| 类名 | 说明 |
|------|------|
| `ReflectUtil` | 通用反射工具 |
| `FieldUtil` | 字段访问和操作工具 |
| `MethodUtil` | 方法查找和调用工具 |
| `ConstructorUtil` | 构造器查找和实例化工具 |
| `ModifierUtil` | 修饰符检查工具 |
| `RecordUtil` | Record 组件访问工具 |
| `UnsafeUtil` | sun.misc.Unsafe 包装器，用于底层操作 |

### 单例

| 类名 | 说明 |
|------|------|
| `Singleton` | 线程安全的懒加载单例注册表 |

### SPI

| 类名 | 说明 |
|------|------|
| `SpiLoader` | ServiceLoader 包装器，支持缓存和排序 |

### Stream

| 类名 | 说明 |
|------|------|
| `OptionalUtil` | Optional 扩展工具 |
| `ParallelStreamUtil` | 并行流执行工具 |

### 线程

| 类名 | 说明 |
|------|------|
| `OpenThread` | 线程工具（sleep、虚拟线程创建） |
| `NamedThreadFactory` | 支持自定义命名模式的 ThreadFactory |
| `ScopedValueUtil` | JDK 25 ScopedValue 工具 |
| `StructuredTaskUtil` | JDK 25 结构化并发工具 |
| `ThreadLocalUtil` | ThreadLocal 管理工具 |

### 元组

| 类名 | 说明 |
|------|------|
| `Pair` | 不可变的二元组 Record |
| `Triple` | 不可变的三元组 Record |
| `Quadruple` | 不可变的四元组 Record |
| `TupleUtil` | 元组创建和转换工具 |

### 内部

| 类名 | 说明 |
|------|------|
| `InternalCache` | 内部缓存接口 |
| `InternalLRUCache` | LRU 缓存实现，供内部使用 |

## 快速开始

```java
import cloud.opencode.base.core.*;
import cloud.opencode.base.core.tuple.*;
import cloud.opencode.base.core.primitives.*;

// 前置条件检查
Preconditions.checkArgument(age > 0, "age must be positive");
Preconditions.checkNotNull(name, "name");

// 类型转换
int value = Convert.toInt("42");
String str = Convert.toStr(123);

// 数组操作
int[] arr = {3, 1, 4, 1, 5};
boolean has = Ints.contains(arr, 4);      // true
int idx = Ints.indexOf(arr, 5);           // 4

// 元组
Pair<String, Integer> pair = Pair.of("Alice", 30);
Triple<String, Integer, Boolean> triple = Triple.of("Bob", 25, true);

// 构建器
Map<String, Object> map = MapBuilder.<String, Object>create()
    .put("name", "Alice")
    .put("age", 30)
    .build();

// 区间
Range<Integer> range = Range.closed(1, 10);
boolean contains = range.contains(5);  // true

// 计时器
Stopwatch sw = Stopwatch.createStarted();
// ... 执行操作 ...
System.out.println("耗时: " + sw.elapsed());

// 字符串连接/分割
String joined = Joiner.on(", ").skipNulls().join("a", null, "b"); // "a, b"
List<String> parts = Splitter.on(",").trimResults().splitToList("a, b, c");

// Bean 拷贝
OpenBean.copyProperties(source, target);

// 结构化并发 (JDK 25)
var result = StructuredTaskUtil.allOf(
    () -> fetchUser(id),
    () -> fetchOrders(id)
);
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0

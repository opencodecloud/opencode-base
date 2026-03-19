# OpenCode Base Functional

**函数式编程工具库，适用于 Java 25+**

`opencode-base-functional` 为 Java 带来了完善的函数式编程构造，包括 Monad（Try、Either、Option、Validation）、模式匹配、管道、光学组件（Lens）、基于虚拟线程的异步工具和 Record 操作工具。

## 功能特性

### Monad 类型
- **Try**：异常安全计算，支持 `map`、`flatMap`、`recover`
- **Either**：不相交联合类型（Left = 错误，Right = 成功）
- **Option**：空值安全容器（Some/None），替代 Optional
- **Validation**：累积错误验证（收集所有错误，而非仅第一个）
- **Lazy**：延迟计算与记忆化
- **Sequence**：惰性序列操作
- **Trampoline**：栈安全的递归计算

### 模式匹配
- **类型匹配**：按类类型匹配并提取
- **谓词匹配**：按任意谓词匹配
- **流式 API**：可链式调用的 `caseOf` / `when` / `orElse`

### 函数工具
- **组合**：`compose`、`andThen` 用于函数链接
- **柯里化**：将多参数函数转换为柯里化形式
- **记忆化**：缓存函数结果，支持可配置的 LRU 容量
- **受检函数**：`CheckedFunction`、`CheckedBiFunction`、`CheckedBiConsumer`
- **TriFunction**：三参数函数接口

### 光学组件
- **Lens**：用于 Record 和对象的不可变嵌套更新
- **OptionalLens**：用于可选/可空字段的 Lens

### 管道
- **Pipeline**：带 `then` 步骤的类型化数据转换链
- **PipeUtil**：轻量级管道操作符（`pipe(value).then(f).then(g).get()`）

### 异步
- **虚拟线程执行**：`async()` 在虚拟线程上运行
- **超时支持**：`asyncTimeout()` 基于 Try 的结果
- **LazyAsync**：首次访问时启动的延迟异步
- **并行映射**：`parallelMap()` 用于并发列表转换

### Record 工具
- **Record Lens**：按名称为 Record 组件创建 Lens
- **Record 复制**：选择性修改复制 Record
- **Record 转 Map**：将 Record 转换为 Map 表示

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-functional</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.functional.OpenFunctional;

// Try monad 异常处理
Try<Integer> result = OpenFunctional.tryOf(() -> Integer.parseInt(input));
int value = result.getOrElse(0);

// Either 错误处理
Either<String, User> user = OpenFunctional.right(new User("Alice"));
user.map(u -> u.name()).forEach(System.out::println);

// Option 空值安全
Option<String> name = OpenFunctional.some("value");
Option<String> empty = OpenFunctional.none();

// 模式匹配
String type = OpenFunctional.match(value)
    .caseOf(String.class, s -> "String: " + s)
    .caseOf(Integer.class, n -> "Number: " + n)
    .orElse(o -> "Unknown");
```

### 高级用法

```java
// 函数组合
Function<String, Integer> parseAndDouble = OpenFunctional.compose(
    Integer::parseInt,
    n -> n * 2
);

// 柯里化
Function<Integer, Function<Integer, Integer>> add = OpenFunctional.curry(Integer::sum);
Function<Integer, Integer> add5 = add.apply(5);

// 带 LRU 缓存的记忆化
Function<String, Data> cachedFetch = OpenFunctional.memoize(this::fetchData, 1000);

// Lens 不可变更新
Lens<Person, String> nameLens = OpenFunctional.lens(
    Person::name,
    (p, n) -> new Person(n, p.age())
);
Person updated = nameLens.set(person, "New Name");

// 管道转换
String result = OpenFunctional.pipe("  hello  ")
    .then(String::trim)
    .then(String::toUpperCase)
    .get();  // "HELLO"

// 虚拟线程异步
CompletableFuture<Data> future = OpenFunctional.async(() -> fetchData());

// 并行映射
List<Result> results = OpenFunctional.parallelMap(items, this::process);

// Record 工具
Lens<Person, String> lens = OpenFunctional.recordLens(Person.class, "name");
Person copy = OpenFunctional.copyRecord(person, Map.of("name", "New Name"));
Map<String, Object> map = OpenFunctional.recordToMap(person);
```

### 验证

```java
// 累积所有错误
Validation<String, Integer> v1 = OpenFunctional.valid(42);
Validation<String, Integer> v2 = OpenFunctional.invalid("must be positive");

// 组合验证
Validation<List<String>, User> result = Validation.combine(
    validateName(name),
    validateAge(age),
    validateEmail(email)
).apply(User::new);
```

## 类参考

### 根包 (`cloud.opencode.base.functional`)
| 类 | 说明 |
|---|------|
| `OpenFunctional` | 统一入口点，提供所有函数式工具的静态便捷方法 |

### Monad 包 (`cloud.opencode.base.functional.monad`)
| 类 | 说明 |
|---|------|
| `Try` | 可能成功或因异常失败的计算 |
| `Either` | 不相交联合类型（Left 表示错误，Right 表示成功） |
| `Option` | 空值安全容器（Some 表示有值，None 表示缺失） |
| `Validation` | 累积错误验证，收集所有错误 |
| `Lazy` | 延迟计算，带线程安全的记忆化 |
| `Sequence` | 带函数式转换的惰性序列 |
| `Trampoline` | 通过蹦床实现的栈安全递归计算 |
| `For` | 用于 Monad 组合的 for 推导式支持 |

### 模式包 (`cloud.opencode.base.functional.pattern`)
| 类 | 说明 |
|---|------|
| `OpenMatch` | 流式模式匹配入口点和 Matcher 构建器 |
| `Pattern` | 模式定义（类型模式、谓词模式） |
| `Case` | 带条件和动作的匹配分支 |

### 函数包 (`cloud.opencode.base.functional.function`)
| 类 | 说明 |
|---|------|
| `FunctionUtil` | 组合、柯里化、记忆化和偏应用的工具方法 |
| `CheckedFunction` | 可抛出受检异常的函数 |
| `CheckedBiFunction` | 可抛出受检异常的双参函数 |
| `CheckedBiConsumer` | 可抛出受检异常的双参消费者 |
| `TriFunction` | 三参数函数接口 |

### 光学包 (`cloud.opencode.base.functional.optics`)
| 类 | 说明 |
|---|------|
| `Lens` | 可组合的 getter/setter 对，用于不可变嵌套更新 |
| `OptionalLens` | 用于可选/可空字段的 Lens 变体 |

### 管道包 (`cloud.opencode.base.functional.pipeline`)
| 类 | 说明 |
|---|------|
| `Pipeline` | 带构建器模式的类型化转换管道 |
| `PipeUtil` | 用于值转换链的轻量级管道操作符 |

### 异步包 (`cloud.opencode.base.functional.async`)
| 类 | 说明 |
|---|------|
| `AsyncFunctionUtil` | 使用虚拟线程的异步执行工具 |
| `Future` | 带函数式操作的增强 Future |
| `LazyAsync` | 首次访问时启动的延迟异步计算 |

### Record 包 (`cloud.opencode.base.functional.record`)
| 类 | 说明 |
|---|------|
| `RecordUtil` | Record Lens 创建、复制和 Map 转换工具 |

### 异常包 (`cloud.opencode.base.functional.exception`)
| 类 | 说明 |
|---|------|
| `OpenFunctionalException` | 函数式模块错误的基础异常 |
| `OpenMatchException` | 无匹配分支满足时抛出的异常 |

## 环境要求

- Java 25+（使用 record、密封接口、虚拟线程、模式匹配）
- 核心功能无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

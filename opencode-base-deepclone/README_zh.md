# OpenCode Base DeepClone

面向 JDK 25+ 的高性能深度克隆库，支持反射、序列化、Unsafe 三种策略。

## 功能特性

- **多种策略**: 反射（默认）、序列化、Unsafe
- **Null 安全**: 所有克隆方法对 `null` 输入返回 `null`
- **浅拷贝**: `shallowClone()` 复制字段引用，不深度克隆
- **合并复制**: `copyTo()` 将非 null 字段合并到已有对象
- **克隆策略**: `STANDARD` / `STRICT`（禁止 Unsafe 回退）/ `LENIENT`（跳过错误）
- **字段过滤器**: 按名称、类型或注解编程式排除字段
- **克隆监听器**: 生命周期钩子，用于审计/日志（前/后/错误）
- **枚举身份保持**: 枚举值克隆后保持 `==` 一致
- **Optional\<T\>**: 正确深度克隆 Optional 内容
- **JDK 不可变集合识别**: 检测 `List.of()`、`Set.of()`、`Map.of()`、`Collections.unmodifiable*()`（跳过拷贝）
- **注解驱动**: `@CloneDeep`、`@CloneIgnore`、`@CloneReference`
- **类型处理器**: 数组、集合、Map、Record、枚举、Optional
- **批量与并行**: `cloneBatch()`、`cloneBatchParallel()`（虚拟线程）、`cloneAsync()`
- **循环引用检测**: 通过 `CloneContext` 自动处理
- **可插拔策略**: 通过 SPI（`CloneStrategyProvider`）扩展
- **线程安全**

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-deepclone</artifactId>
    <version>1.0.3</version>
</dependency>
```

## 快速开始

```java
import cloud.opencode.base.deepclone.*;

// 深度克隆（反射，默认）
User cloned = OpenClone.clone(user);

// 浅拷贝（仅复制引用）
User shallow = OpenClone.shallowClone(user);

// 合并非 null 字段到已有对象
OpenClone.copyTo(source, target);

// 指定策略克隆
User lenient = OpenClone.cloneWith(user, ClonePolicy.LENIENT);
```

## API 参考 — `OpenClone`

主门面类。所有方法均为 `static` 且线程安全。

### 深度克隆

| 方法 | 说明 |
|------|------|
| `<T> T clone(T original)` | 使用默认反射策略深度克隆 |
| `<T> T clone(T original, Cloner cloner)` | 使用指定克隆器深度克隆 |
| `<T> T cloneBySerialization(T original)` | 通过 Java 序列化深度克隆（需要 `Serializable`） |
| `<T> T cloneByUnsafe(T original)` | 通过 Unsafe 深度克隆（最高性能，不调用构造器） |
| `<T> T cloneWith(T original, ClonePolicy policy)` | 使用指定策略深度克隆 |

### 浅拷贝与合并复制

| 方法 | 说明 |
|------|------|
| `<T> T shallowClone(T original)` | 浅拷贝——字段引用共享，不深度克隆 |
| `<T> T copyTo(T source, T target)` | 将 source 的非 null 字段复制到 target（值会被深度克隆） |

### 批量与异步

| 方法 | 说明 |
|------|------|
| `<T> List<T> cloneBatch(List<T> originals)` | 顺序批量克隆 |
| `<T> List<T> cloneBatchParallel(List<T> originals, int parallelism)` | 虚拟线程并行克隆 |
| `<T> CompletableFuture<T> cloneAsync(T original)` | 异步深度克隆 |
| `<T> CompletableFuture<List<T>> cloneBatchAsync(List<T> originals)` | 异步批量克隆 |

### 工具方法

| 方法 | 说明 |
|------|------|
| `boolean isImmutable(Class<?> type)` | 检查类型是否已注册为不可变 |
| `void registerImmutable(Class<?>... types)` | 注册自定义不可变类型（不会被克隆） |
| `Cloner getDefaultCloner()` | 获取默认 ReflectiveCloner 实例 |
| `ClonerBuilder builder()` | 创建自定义 Cloner 配置构建器 |

## API 参考 — `ClonerBuilder`

流式构建器，用于创建配置化的 `Cloner` 实例。

```java
Cloner cloner = OpenClone.builder()
    .reflective()                                          // 或 .serializing() 或 .unsafe()
    .maxDepth(50)                                          // 默认: 100
    .cloneTransient(true)                                  // 默认: false
    .policy(ClonePolicy.LENIENT)                           // 默认: STANDARD
    .filter(FieldFilter.excludeNames("password", "token")) // 字段排除
    .listener(myListener)                                  // 生命周期钩子
    .registerImmutable(Money.class)                        // 自定义不可变类型
    .registerHandler(MyType.class, myHandler)              // 自定义类型处理器
    .build();
```

## API 参考 — `ClonePolicy`

| 值 | 行为 |
|------|------|
| `STANDARD` | 默认。遇到不可克隆类型抛出异常 |
| `STRICT` | 禁止 Unsafe 回退。所有字段必须通过反射可访问 |
| `LENIENT` | 尽力而为。跳过错误，对不可克隆类型使用浅引用，记录警告 |

## API 参考 — `FieldFilter`

函数式接口，用于编程式字段排除。支持通过 `and()`、`or()`、`negate()` 组合。

| 工厂方法 | 说明 |
|----------|------|
| `FieldFilter.acceptAll()` | 接受所有字段（不过滤） |
| `FieldFilter.excludeNames(String... names)` | 按名称排除字段 |
| `FieldFilter.includeNames(String... names)` | 仅包含匹配名称的字段 |
| `FieldFilter.excludeTypes(Class<?>... types)` | 按声明类型排除字段 |
| `FieldFilter.excludeAnnotated(Class<? extends Annotation>)` | 排除带有指定注解的字段 |

```java
// 组合过滤器
FieldFilter filter = FieldFilter.excludeNames("password")
    .and(FieldFilter.excludeTypes(InputStream.class));
```

## API 参考 — `CloneListener`

克隆生命周期钩子接口。所有方法都是 `default`（空操作）。监听器异常被隔离，不影响克隆流程。

| 方法 | 说明 |
|------|------|
| `void beforeClone(Object original, CloneContext context)` | 每个对象克隆前调用 |
| `void afterClone(Object original, Object cloned, CloneContext context)` | 克隆成功后调用 |
| `void onError(Object original, Throwable error, CloneContext context)` | 克隆失败时调用 |

## 注解

| 注解 | 目标 | 说明 |
|------|------|------|
| `@CloneDeep` | 字段 | 强制深度克隆（默认行为） |
| `@CloneIgnore` | 字段 | 克隆时跳过此字段（设为 `null`） |
| `@CloneReference` | 字段 | 仅复制引用（浅拷贝，适用于共享资源） |

```java
public class User {
    private String name;                    // 深度克隆（默认）
    @CloneIgnore private String password;   // 克隆后为 null
    @CloneReference private Logger logger;  // 共享同一引用
}
```

## 类型处理器

| 处理器 | 支持的类型 | 优先级 |
|--------|-----------|--------|
| `EnumHandler` | 所有 `enum` 类型 | 5 |
| `ArrayHandler` | 基本类型和对象数组 | 10 |
| `RecordHandler` | Java `record` 类型 | 15 |
| `OptionalHandler` | `Optional<T>` | 15 |
| `CollectionHandler` | ArrayList、LinkedList、HashSet、TreeSet、ArrayDeque 等 | 20 |
| `MapHandler` | HashMap、LinkedHashMap、TreeMap、ConcurrentHashMap 等 | 20 |

## 自定义类型处理器

```java
import cloud.opencode.base.deepclone.handler.TypeHandler;

public class MoneyHandler implements TypeHandler<Money> {
    @Override
    public Money clone(Money original, Cloner cloner, CloneContext context) {
        return new Money(original.getAmount(), original.getCurrency());
    }

    @Override
    public boolean supports(Class<?> type) {
        return Money.class.isAssignableFrom(type);
    }
}

// 注册
Cloner cloner = OpenClone.builder()
    .registerHandler(Money.class, new MoneyHandler())
    .build();
```

## 通过 `DeepCloneable` 自定义克隆逻辑

```java
import cloud.opencode.base.deepclone.contract.DeepCloneable;

public class Config implements DeepCloneable<Config> {
    private Map<String, String> settings;

    @Override
    public Config deepClone() {
        Config copy = new Config();
        copy.settings = new HashMap<>(this.settings);
        return copy;
    }
}
```

## API 总览

| 分类 | 类 |
|------|-----|
| **门面** | `OpenClone` |
| **核心** | `Cloner`、`ClonerBuilder`、`CloneContext`、`ClonePolicy`、`FieldFilter`、`CloneListener` |
| **克隆器实现** | `AbstractCloner`、`ReflectiveCloner`、`SerializingCloner`、`UnsafeCloner` |
| **注解** | `@CloneDeep`、`@CloneIgnore`、`@CloneReference` |
| **处理器** | `TypeHandler`、`ArrayHandler`、`CollectionHandler`、`MapHandler`、`RecordHandler`、`EnumHandler`、`OptionalHandler` |
| **策略** | `CloneStrategy`、`FieldCloneStrategy`、`TypeCloneStrategy` |
| **SPI** | `CloneStrategyProvider` |
| **契约** | `DeepCloneable` |
| **内部工具** | `ImmutableDetector` |
| **异常** | `OpenDeepCloneException` |

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0

# DeepClone 组件方案

## 1. 组件概述

`opencode-base-deepclone` 是一个高性能、可扩展的 Java 深度克隆组件，为复杂对象图（包含嵌套对象、集合、数组、循环引用等）提供完整复制解决方案。支持反射、序列化、Unsafe 三种克隆策略，通过注解精细控制字段行为，基于 JDK 25 实现，零外部依赖。

**适用场景：**
- DTO 复制，防止 Web 层与领域层对象共享状态
- 缓存隔离，避免缓存对象被业务代码修改
- 快照/回滚，对象状态备份用于审计或撤销
- 测试场景，快速构造复杂对象副本

**模块依赖：**
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-deepclone</artifactId>
    <version>${version}</version>
</dependency>
```

## 2. 包结构

```
cloud.opencode.base.deepclone
├── OpenClone.java                  // 静态门面类
├── Cloner.java                     // 克隆器接口
├── ClonerBuilder.java              // 克隆器构建器
├── CloneContext.java               // 克隆上下文（循环引用检测、深度追踪）
│
├── annotation/                     // 注解
│   ├── CloneIgnore.java            // 忽略字段（克隆后为默认值）
│   ├── CloneReference.java         // 浅拷贝字段（仅复制引用）
│   └── CloneDeep.java              // 强制深拷贝（覆盖不可变判断）
│
├── cloner/                         // 克隆器实现（密封类层级）
│   ├── AbstractCloner.java         // 抽象基类（sealed）
│   ├── ReflectiveCloner.java       // 反射克隆器（默认，支持任意对象）
│   ├── SerializingCloner.java      // 序列化克隆器（需 Serializable）
│   └── UnsafeCloner.java           // Unsafe 高性能克隆器
│
├── handler/                        // 类型处理器
│   ├── TypeHandler.java            // 类型处理器接口
│   ├── ArrayHandler.java           // 数组处理器
│   ├── CollectionHandler.java      // 集合处理器（List/Set/Queue）
│   ├── MapHandler.java             // Map 处理器
│   └── RecordHandler.java          // Record 类型处理器
│
├── strategy/                       // 克隆策略
│   ├── CloneStrategy.java          // 策略接口（SPI）
│   ├── FieldCloneStrategy.java     // 字段策略枚举（DEEP/SHALLOW/IGNORE/NULL）
│   └── TypeCloneStrategy.java      // 类型克隆策略（record）
│
├── contract/                       // 契约接口
│   └── DeepCloneable.java          // 可深度克隆接口
│
├── spi/                            // SPI 扩展
│   └── CloneStrategyProvider.java  // 策略提供者接口
│
└── exception/
    └── OpenDeepCloneException.java // 克隆异常
```

## 3. 核心 API

### 3.1 OpenClone

> 深度克隆门面入口类，提供单对象克隆、批量克隆、异步克隆和工具方法的统一入口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> T clone(T original)` | 使用反射策略深度克隆对象 |
| `static <T> T cloneBySerialization(T original)` | 使用序列化策略克隆（需 Serializable） |
| `static <T> T cloneByUnsafe(T original)` | 使用 Unsafe 策略高性能克隆 |
| `static <T> T clone(T original, Cloner cloner)` | 使用指定克隆器克隆 |
| `static <T> List<T> cloneBatch(List<T> originals)` | 批量深度克隆 |
| `static <T> List<T> cloneBatchParallel(List<T> originals, int parallelism)` | 并行批量克隆（虚拟线程） |
| `static <T> CompletableFuture<T> cloneAsync(T original)` | 异步深度克隆 |
| `static <T> CompletableFuture<List<T>> cloneBatchAsync(List<T> originals)` | 异步批量克隆 |
| `static boolean isImmutable(Class<?> type)` | 判断类型是否为不可变类型 |
| `static void registerImmutable(Class<?>... types)` | 注册自定义不可变类型 |
| `static Cloner getDefaultCloner()` | 获取默认克隆器 |
| `static ClonerBuilder builder()` | 创建克隆器构建器 |

**示例：**

```java
// 简单克隆
User original = new User("John", new Address("New York"));
User cloned = OpenClone.clone(original);

// 不同策略
User cloned1 = OpenClone.cloneBySerialization(original); // 需 Serializable
User cloned2 = OpenClone.cloneByUnsafe(original);        // 高性能

// 批量克隆
List<User> users = loadUsers();
List<User> clonedUsers = OpenClone.cloneBatch(users);

// 并行批量克隆（虚拟线程）
List<User> parallelCloned = OpenClone.cloneBatchParallel(users, 4);

// 异步克隆
CompletableFuture<User> future = OpenClone.cloneAsync(user);
future.thenAccept(cloned -> { /* 处理克隆结果 */ });

// 自定义克隆器
Cloner customCloner = OpenClone.builder()
    .reflective()
    .registerImmutable(BigDecimal.class)
    .maxDepth(10)
    .cloneTransient(true)
    .build();
User cloned3 = customCloner.clone(original);
```

### 3.2 Cloner

> 克隆器接口，定义克隆操作的核心契约。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `<T> T clone(T original)` | 深度克隆对象 |
| `<T> T clone(T original, CloneContext context)` | 使用指定上下文克隆 |
| `String getStrategyName()` | 获取克隆策略名称 |
| `boolean supports(Class<?> type)` | 是否支持该类型 |

**示例：**

```java
Cloner cloner = OpenClone.builder()
    .reflective()
    .registerImmutable(Money.class)
    .build();

User cloned = cloner.clone(originalUser);
System.out.println(cloner.getStrategyName()); // "reflective"
System.out.println(cloner.supports(User.class)); // true
```

### 3.3 ClonerBuilder

> 克隆器构建器，通过流畅 API 创建配置化的克隆器实例。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ClonerBuilder reflective()` | 使用反射策略 |
| `ClonerBuilder serializing()` | 使用序列化策略 |
| `ClonerBuilder unsafe()` | 使用 Unsafe 策略 |
| `ClonerBuilder registerImmutable(Class<?>... types)` | 注册不可变类型 |
| `<T> ClonerBuilder registerHandler(Class<T> type, TypeHandler<T> handler)` | 注册自定义类型处理器 |
| `<T> ClonerBuilder registerCloner(Class<T> type, UnaryOperator<T> cloner)` | 注册自定义克隆函数 |
| `ClonerBuilder cloneTransient(boolean cloneTransient)` | 设置是否克隆 transient 字段 |
| `ClonerBuilder useCache(boolean useCache)` | 设置是否使用缓存 |
| `ClonerBuilder maxDepth(int maxDepth)` | 设置最大克隆深度 |
| `Cloner build()` | 构建克隆器实例 |

**示例：**

```java
Cloner cloner = OpenClone.builder()
    .reflective()
    .registerImmutable(Money.class, Currency.class)
    .registerCloner(Money.class, money ->
        new Money(money.getAmount(), money.getCurrency()))
    .cloneTransient(false)
    .useCache(true)
    .maxDepth(20)
    .build();
```

### 3.4 CloneContext

> 克隆上下文，用于跟踪克隆状态、检测循环引用和收集统计信息。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static CloneContext create()` | 创建新的克隆上下文 |
| `static CloneContext create(int maxDepth)` | 创建带最大深度限制的上下文 |
| `Map<Object, Object> getClonedObjects()` | 获取已克隆对象的映射 |
| `boolean isCloned(Object original)` | 检查对象是否已克隆 |
| `<T> T getCloned(Object original)` | 获取已克隆的副本 |
| `void registerCloned(Object original, Object cloned)` | 注册克隆映射 |
| `int getDepth()` | 获取当前克隆深度 |
| `int getMaxDepth()` | 获取最大克隆深度 |
| `int incrementDepth()` | 增加深度 |
| `int decrementDepth()` | 减少深度 |
| `boolean isMaxDepthExceeded()` | 是否超过最大深度 |
| `List<String> getPath()` | 获取克隆路径（调试用） |
| `String getPathString()` | 获取路径字符串表示 |
| `void pushPath(String element)` | 压入路径元素 |
| `void popPath()` | 弹出路径元素 |
| `CloneStatistics getStatistics()` | 获取克隆统计信息 |

**示例：**

```java
CloneContext context = CloneContext.create(50);
User cloned = cloner.clone(original, context);

CloneContext.CloneStatistics stats = context.getStatistics();
System.out.println("已克隆对象: " + stats.objectsCloned());
System.out.println("跳过对象: " + stats.objectsSkipped());
System.out.println("最大深度: " + stats.maxDepthReached());
System.out.println("耗时: " + stats.elapsedMillis() + " ms");
```

### 3.5 CloneContext.CloneStatistics

> 克隆统计信息记录，包含克隆过程中的各项指标。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `int objectsCloned()` | 已克隆对象数量 |
| `int objectsSkipped()` | 跳过的对象数量 |
| `int maxDepthReached()` | 达到的最大深度 |
| `long elapsedNanos()` | 耗时（纳秒） |
| `double elapsedMillis()` | 耗时（毫秒） |

### 3.6 @CloneIgnore

> 标记字段在克隆时被忽略，克隆后该字段为默认值（null/0/false）。适用于缓存、临时数据等不需要克隆的字段。

**示例：**

```java
public class Order {
    private String orderId;

    @CloneIgnore(reason = "缓存数据，无需克隆")
    private transient Map<String, Object> cache;
}

Order cloned = OpenClone.clone(order);
// cloned.cache == null
```

### 3.7 @CloneReference

> 标记字段在克隆时仅复制引用（浅拷贝），适用于连接池、线程池等共享资源对象。

**示例：**

```java
public class Service {
    private String name;

    @CloneReference(reason = "共享数据库连接池")
    private DataSource dataSource;

    @CloneReference(reason = "共享线程池")
    private ExecutorService executor;
}

Service cloned = OpenClone.clone(service);
// cloned.dataSource == service.dataSource (同一引用)
```

### 3.8 @CloneDeep

> 标记字段强制进行深度克隆，覆盖默认的不可变类型判断。

**示例：**

```java
public class Config {
    @CloneDeep(reason = "需要独立副本")
    private LocalDateTime createTime;
}
```

### 3.9 AbstractCloner

> 抽象克隆器基类，使用密封类 (`sealed`) 限制继承，仅允许 `ReflectiveCloner`、`SerializingCloner`、`UnsafeCloner` 三种实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `<T> T clone(T original)` | 深度克隆对象（创建新上下文） |
| `<T> T clone(T original, CloneContext context)` | 使用指定上下文克隆 |
| `void registerImmutable(Class<?>... types)` | 注册不可变类型 |
| `<T> void registerHandler(Class<T> type, TypeHandler<T> handler)` | 注册类型处理器 |
| `void setMaxDepth(int maxDepth)` | 设置最大克隆深度 |
| `void setCloneTransient(boolean cloneTransient)` | 设置是否克隆 transient 字段 |

### 3.10 ReflectiveCloner

> 基于反射的深度克隆器，支持任意对象类型，无需 Serializable 接口。默认克隆策略。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static ReflectiveCloner create()` | 创建默认配置的反射克隆器 |
| `static ReflectiveCloner create(ReflectiveConfig config)` | 创建带配置的反射克隆器 |
| `static void clearCaches()` | 清除字段缓存 |
| `String getStrategyName()` | 返回 "reflective" |
| `boolean supports(Class<?> type)` | 支持所有类型，始终返回 true |

**示例：**

```java
ReflectiveCloner cloner = ReflectiveCloner.create();
User cloned = cloner.clone(originalUser);

// 自定义配置
ReflectiveCloner configured = ReflectiveCloner.create(
    new ReflectiveCloner.ReflectiveConfig(
        true,   // cloneTransient - 克隆 transient 字段
        true,   // useFieldCache - 使用字段缓存
        true    // respectAnnotations - 尊重注解
    )
);
```

### 3.11 ReflectiveCloner.ReflectiveConfig

> 反射克隆器配置记录。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean cloneTransient()` | 是否克隆 transient 字段 |
| `boolean useFieldCache()` | 是否使用字段缓存 |
| `boolean respectAnnotations()` | 是否尊重注解 |
| `static ReflectiveConfig defaults()` | 创建默认配置 (false, true, true) |

### 3.12 SerializingCloner

> 基于 Java 序列化的深度克隆器，要求对象实现 Serializable 接口。简单但性能较低。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static SerializingCloner create()` | 创建序列化克隆器 |
| `String getStrategyName()` | 返回 "serializing" |
| `boolean supports(Class<?> type)` | 仅支持 Serializable 类型 |

**示例：**

```java
SerializingCloner cloner = SerializingCloner.create();
User cloned = cloner.clone(originalUser); // User 必须实现 Serializable
```

### 3.13 UnsafeCloner

> 基于 Unsafe 的高性能深度克隆器，直接内存操作，无需构造函数。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static UnsafeCloner create()` | 创建 Unsafe 克隆器 |
| `static boolean isAvailable()` | 检查 Unsafe 是否可用 |
| `static <T> T allocateInstanceStatic(Class<T> type)` | 使用 Unsafe 分配对象实例（不调用构造器） |
| `String getStrategyName()` | 返回 "unsafe" |
| `boolean supports(Class<?> type)` | 支持所有类型 |

**示例：**

```java
if (UnsafeCloner.isAvailable()) {
    UnsafeCloner cloner = UnsafeCloner.create();
    User cloned = cloner.clone(originalUser);
}
```

### 3.14 TypeHandler\<T\>

> 类型处理器接口，用于自定义特定类型的克隆逻辑。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `T clone(T original, Cloner cloner, CloneContext context)` | 克隆对象 |
| `boolean supports(Class<?> type)` | 是否支持该类型 |
| `default int priority()` | 处理器优先级（数值越小越优先，默认100） |

**示例：**

```java
public class MoneyHandler implements TypeHandler<Money> {
    @Override
    public Money clone(Money original, Cloner cloner, CloneContext context) {
        return new Money(original.getAmount(), original.getCurrency());
    }

    @Override
    public boolean supports(Class<?> type) {
        return Money.class.equals(type);
    }
}
```

### 3.15 ArrayHandler

> 数组类型克隆处理器，支持基本类型数组和对象数组的深度克隆。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Object clone(Object array, Cloner cloner, CloneContext context)` | 克隆数组 |
| `Object clonePrimitiveArray(Object array, Class<?> componentType, int length)` | 克隆指定类型的基本类型数组 |
| `Object clonePrimitiveArray(Object array)` | 克隆基本类型数组（自动检测类型） |
| `<T> T[] cloneObjectArray(T[] array, Cloner cloner, CloneContext context)` | 克隆对象数组 |
| `boolean supports(Class<?> type)` | 仅支持数组类型 |

### 3.16 CollectionHandler

> 集合类型克隆处理器，递归深度克隆集合中的每个元素。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Collection<?> clone(Collection<?> original, Cloner cloner, CloneContext context)` | 克隆集合 |
| `<T> List<T> cloneList(List<T> list, Cloner cloner, CloneContext context)` | 克隆 List |
| `<T> Set<T> cloneSet(Set<T> set, Cloner cloner, CloneContext context)` | 克隆 Set |
| `<T> Queue<T> cloneQueue(Queue<T> queue, Cloner cloner, CloneContext context)` | 克隆 Queue |
| `<T> Collection<T> createInstance(Class<?> type, int size)` | 创建集合实例 |
| `boolean supports(Class<?> type)` | 仅支持 Collection 类型 |

### 3.17 MapHandler

> Map 类型克隆处理器，支持键值的深度克隆或仅值深拷贝。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Map<?, ?> clone(Map<?, ?> original, Cloner cloner, CloneContext context)` | 克隆 Map |
| `<K, V> Map<K, V> cloneDeep(Map<K, V> map, Cloner cloner, CloneContext context)` | 深度克隆键和值 |
| `<K, V> Map<K, V> cloneValues(Map<K, V> map, Cloner cloner, CloneContext context)` | 仅深度克隆值 |
| `<K, V> Map<K, V> createInstance(Class<?> type, int size)` | 创建 Map 实例 |
| `boolean supports(Class<?> type)` | 仅支持 Map 类型 |

### 3.18 RecordHandler

> Record 类型克隆处理器，通过提取组件值并重新构造 Record 实例实现深度克隆。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Record clone(Record original, Cloner cloner, CloneContext context)` | 克隆 Record |
| `<T extends Record> T cloneRecord(T record, Cloner cloner, CloneContext context)` | 泛型克隆 Record |
| `Object[] getComponents(Record record)` | 获取 Record 的组件值数组 |
| `<T extends Record> T createInstance(Class<T> type, Object[] values)` | 创建 Record 实例 |
| `boolean supports(Class<?> type)` | 仅支持 Record 类型 |

**示例：**

```java
record Point(int x, int y, String label) {}
Point original = new Point(10, 20, "origin");
Point cloned = OpenClone.clone(original);
// cloned.x() == 10, cloned.label().equals("origin")
```

### 3.19 CloneStrategy

> 克隆策略接口（SPI），用于通过 SPI 机制提供自定义克隆策略。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `String name()` | 策略名称 |
| `<T> T clone(T original, CloneContext context)` | 执行克隆 |
| `default int priority()` | 策略优先级（默认100） |
| `default boolean supports(Class<?> type)` | 是否支持该类型（默认 true） |

### 3.20 FieldCloneStrategy

> 字段克隆策略枚举，定义字段的克隆行为。可从注解自动解析。

**枚举值：**

| 值 | 描述 |
|------|------|
| `DEEP` | 深度克隆（默认） |
| `SHALLOW` | 浅拷贝（仅复制引用） |
| `IGNORE` | 忽略（保持默认值） |
| `NULL` | 显式设为 null |

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static FieldCloneStrategy fromAnnotations(Field field)` | 从字段注解解析策略 |

**示例：**

```java
Field field = MyClass.class.getDeclaredField("data");
FieldCloneStrategy strategy = FieldCloneStrategy.fromAnnotations(field);
// 根据 @CloneIgnore/@CloneReference/@CloneDeep 返回对应策略
```

### 3.21 TypeCloneStrategy\<T\>

> 类型级克隆策略配置（record），为特定类型定义克隆行为。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `Class<T> type()` | 目标类型 |
| `UnaryOperator<T> cloner()` | 克隆函数 |
| `boolean deepClone()` | 是否深拷贝 |
| `static <T> TypeCloneStrategy<T> deep(Class<T> type, UnaryOperator<T> cloner)` | 创建深拷贝策略 |
| `static <T> TypeCloneStrategy<T> shallow(Class<T> type)` | 创建浅拷贝策略 |
| `static <T> TypeCloneStrategy<T> immutable(Class<T> type)` | 创建不可变策略（直接返回原对象） |
| `T apply(T original)` | 应用策略克隆对象 |

**示例：**

```java
TypeCloneStrategy<Money> strategy = TypeCloneStrategy.deep(
    Money.class, m -> new Money(m.getAmount(), m.getCurrency()));
TypeCloneStrategy<LocalDate> immutable = TypeCloneStrategy.immutable(LocalDate.class);
TypeCloneStrategy<SharedResource> shallow = TypeCloneStrategy.shallow(SharedResource.class);
```

### 3.22 DeepCloneable\<T\>

> 可深度克隆的契约接口，实现此接口的类可以提供自定义克隆逻辑。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `T deepClone()` | 执行深度克隆 |
| `default T deepClone(Cloner cloner)` | 使用指定克隆器执行克隆 |

**示例：**

```java
public class Product implements DeepCloneable<Product> {
    private String id;
    private String name;
    private BigDecimal price;
    private List<String> tags;

    @Override
    public Product deepClone() {
        return OpenClone.clone(this);
    }
}
```

### 3.23 CloneStrategyProvider

> 克隆策略提供者 SPI 接口，通过 `META-INF/services` 注册自定义策略。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `List<CloneStrategy> getStrategies()` | 获取提供的策略列表 |
| `default int priority()` | 提供者优先级（默认100） |

### 3.24 OpenDeepCloneException

> 深度克隆操作异常类，继承自 `OpenException`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `OpenDeepCloneException(String message)` | 基本构造 |
| `OpenDeepCloneException(String message, Throwable cause)` | 带原因构造 |
| `OpenDeepCloneException(Class<?> targetType, String path, String message)` | 带类型和路径构造 |
| `OpenDeepCloneException(Class<?> targetType, String path, String message, Throwable cause)` | 完整构造 |
| `Class<?> getTargetType()` | 获取目标类型 |
| `String getPath()` | 获取克隆路径 |
| `static OpenDeepCloneException maxDepthExceeded(int depth, String path)` | 最大深度超出异常 |
| `static OpenDeepCloneException unsupportedType(Class<?> type)` | 不支持的类型异常 |
| `static OpenDeepCloneException instantiationFailed(Class<?> type, Throwable cause)` | 实例化失败异常 |
| `static OpenDeepCloneException fieldAccessFailed(String field, Class<?> type, Throwable cause)` | 字段访问失败异常 |
| `static OpenDeepCloneException serializationFailed(Class<?> type, Throwable cause)` | 序列化失败异常 |
| `static OpenDeepCloneException circularReference(Class<?> type, String path)` | 循环引用异常 |

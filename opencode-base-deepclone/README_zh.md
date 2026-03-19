# OpenCode Base DeepClone

面向 JDK 25+ 的高性能深度克隆库，支持多种克隆策略（反射、序列化、Unsafe）。

## 功能特性

- 单对象深度克隆
- 列表批量克隆
- 虚拟线程并行克隆
- CompletableFuture 异步克隆
- 多种策略：反射（默认）、序列化、Unsafe
- 注解驱动控制：`@CloneDeep`、`@CloneIgnore`、`@CloneReference`
- 自定义类型处理器：数组、集合、Map、Record
- 通过 SPI 可插拔克隆策略
- 可配置最大深度
- 不可变类型检测和注册
- 通过 CloneContext 检测循环引用
- 构建器 API 用于自定义克隆器配置
- 线程安全

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-deepclone</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

| 类名 | 说明 |
|------|------|
| `OpenClone` | 门面类——所有克隆操作的主入口 |
| `Cloner` | 深度克隆实现的克隆器接口 |
| `ClonerBuilder` | 构建器，用于创建自定义克隆器实例并选择策略 |
| `CloneContext` | 克隆上下文，支持循环引用跟踪和深度控制 |
| **克隆器实现** | |
| `ReflectiveCloner` | 默认克隆器，使用反射（处理大多数类型） |
| `SerializingCloner` | 使用 Java 序列化的克隆器（需要 Serializable） |
| `UnsafeCloner` | 使用 sun.misc.Unsafe 的高性能克隆器 |
| `AbstractCloner` | 克隆器实现的基类 |
| **注解** | |
| `@CloneDeep` | 标记字段进行深度克隆 |
| `@CloneIgnore` | 标记字段在克隆时跳过 |
| `@CloneReference` | 标记字段按引用复制（浅拷贝） |
| **类型处理器** | |
| `ArrayHandler` | 处理数组的深度克隆 |
| `CollectionHandler` | 处理 Collection 类型的深度克隆 |
| `MapHandler` | 处理 Map 类型的深度克隆 |
| `RecordHandler` | 处理 Java Record 的深度克隆 |
| `TypeHandler` | 类型处理器接口 |
| **策略** | |
| `CloneStrategy` | 克隆策略接口 |
| `FieldCloneStrategy` | 按字段的克隆策略 |
| `TypeCloneStrategy` | 按类型的克隆策略 |
| **SPI** | |
| `CloneStrategyProvider` | 可插拔克隆策略的 SPI |
| **契约** | |
| `DeepCloneable` | 提供自定义深度克隆逻辑的接口 |
| **异常** | |
| `OpenDeepCloneException` | 深度克隆运行时异常 |

## 快速开始

```java
import cloud.opencode.base.deepclone.OpenClone;

// 简单深度克隆
User cloned = OpenClone.clone(originalUser);

// 通过序列化克隆
User serialClone = OpenClone.cloneBySerialization(originalUser);

// 通过 Unsafe 克隆（高性能）
User unsafeClone = OpenClone.cloneByUnsafe(originalUser);

// 批量克隆
List<User> clonedList = OpenClone.cloneBatch(userList);

// 虚拟线程并行克隆
List<User> parallel = OpenClone.cloneBatchParallel(userList, 4);

// 异步克隆
CompletableFuture<User> future = OpenClone.cloneAsync(user);

// 通过构建器自定义克隆器
Cloner custom = OpenClone.builder()
    .reflective()
    .maxDepth(50)
    .build();
User result = custom.clone(user);
```

## 环境要求

- Java 25+

## 开源许可

Apache License 2.0

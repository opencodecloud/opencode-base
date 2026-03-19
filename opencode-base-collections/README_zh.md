# OpenCode Base Collections

现代集合工具库，提供特殊集合（BiMap、Multiset、Multimap、Table）、不可变集合、原始类型集合、并发集合、图结构和树数据结构，适用于 JDK 25+。

## 功能特性

- 特殊集合：BiMap、Multiset、Multimap、Table
- 不可变集合：ImmutableList、ImmutableSet、ImmutableMap 等
- 原始类型集合：IntList、LongList、DoubleList、IntSet、LongSet、IntIntMap 等
- 并发集合：LockFreeQueue、ConcurrentHashMultiset
- 图数据结构，带遍历算法
- 树结构：Trie、SkipList、TreeTraversal
- 集合工具：ListUtil、MapUtil、SetUtil、OpenCollection
- Stream 增强：OpenCollectors、OpenGatherers、Streams
- 集合代数运算（并集、交集、差集）
- 流式迭代：FluentIterable、PeekingIterator
- 区间类型：Range、RangeSet、RangeMap
- 集合转换和分组工具
- 排序和等价工具

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-collections</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API 概览

### 核心工具

| 类名 | 说明 |
|------|------|
| `OpenCollection` | 通用集合工具（空值安全检查、集合运算、过滤） |
| `OpenList` | List 创建和操作工具 |
| `OpenMap` | Map 创建和操作工具 |
| `OpenSet` | Set 创建和操作工具 |
| `ListUtil` | List 工具（分区、zip、去重、展平） |
| `MapUtil` | Map 工具（合并、反转、过滤、转换） |
| `SetUtil` | Set 工具（并集、交集、差集） |
| `CollectionFactory` | 带容量提示的集合创建工厂 |
| `ComparatorUtil` | 比较器构建工具 |

### 特殊集合

| 类名 | 说明 |
|------|------|
| `BiMap` | 双向映射接口（键到值和值到键） |
| `HashBiMap` | 基于哈希的 BiMap 实现 |
| `Multiset` | 允许重复元素并跟踪计数的集合 |
| `HashMultiset` | 基于哈希的 Multiset 实现 |
| `Multimap` | 每个键对应多个值的映射 |
| `AbstractMultimap` | Multimap 实现的抽象基类 |
| `ArrayListMultimap` | ArrayList 支持的 Multimap |
| `HashSetMultimap` | HashSet 支持的 Multimap |
| `Table` | 二维映射（行、列、值） |
| `HashBasedTable` | 基于哈希的 Table 实现 |
| `MapDifference` | 表示两个 Map 之间的差异 |
| `ValueDifference` | 表示 MapDifference 中的值差异 |
| `SetView` | 集合运算的不可修改视图 |
| `SetAlgebra` | 集合代数运算接口 |

### 特殊集合子包

| 类名 | 说明 |
|------|------|
| `ListMultimap` | 值为 List 的 Multimap |
| `SetMultimap` | 值为 Set 的 Multimap |
| `SortedSetMultimap` | 值为 SortedSet 的 Multimap |
| `TreeSetMultimap` | TreeSet 支持的 SortedSetMultimap |
| `MultimapBuilder` | Multimap 实例的构建器 |
| `ClassToInstanceMap` | 类型安全的 Class 到实例映射 |
| `MutableClassToInstanceMap` | 可变的 ClassToInstanceMap |
| `NavigableMultiset` | 带导航方法的 Multiset |
| `AbstractMultiset` | Multiset 实现的抽象基类 |
| `LinkedHashMultiset` | 保持插入顺序的 Multiset |
| `ConcurrentHashMultiset` | 线程安全的 Multiset |
| `TreeMultiset` | 有序 Multiset |
| `RangeSet` | 不重叠区间的集合 |
| `TreeRangeSet` | 基于树的 RangeSet 实现 |
| `RangeMap` | 区间到值的映射 |
| `TreeRangeMap` | 基于树的 RangeMap 实现 |
| `ArrayTable` | 由二维数组支持的固定大小 Table |
| `TreeBasedTable` | 行/列有序的 Table |
| `EvictingQueue` | 固定容量队列，溢出时淘汰最旧元素 |
| `MinMaxPriorityQueue` | 双端优先队列 |
| `Interner` | 对象驻留（规范实例）工具 |

### 不可变集合

| 类名 | 说明 |
|------|------|
| `ImmutableList` | 不可变列表 |
| `ImmutableSet` | 不可变集合 |
| `ImmutableMap` | 不可变映射 |
| `ImmutableBiMap` | 不可变双向映射 |
| `ImmutableMultimap` | 不可变多值映射 |
| `ImmutableListMultimap` | 不可变的 List 支持的多值映射 |
| `ImmutableSetMultimap` | 不可变的 Set 支持的多值映射 |
| `ImmutableMultiset` | 不可变多集 |
| `ImmutableSortedMap` | 不可变有序映射 |
| `ImmutableSortedSet` | 不可变有序集合 |
| `ImmutableTable` | 不可变表 |
| `ImmutableClassToInstanceMap` | 不可变类型安全实例映射 |
| `ImmutableCollectionUtil` | 创建不可变集合的工具 |

### 原始类型集合

| 类名 | 说明 |
|------|------|
| `IntList` | 原始 int 数组列表 |
| `LongList` | 原始 long 数组列表 |
| `DoubleList` | 原始 double 数组列表 |
| `IntSet` | 原始 int 集合 |
| `LongSet` | 原始 long 集合 |
| `IntIntMap` | 原始 int 到 int 的映射 |
| `IntObjectMap` | 原始 int 到 Object 的映射 |
| `LongObjectMap` | 原始 long 到 Object 的映射 |

### 并发

| 类名 | 说明 |
|------|------|
| `ConcurrentCollectionFactory` | 并发集合实例工厂 |
| `LockFreeQueue` | 无锁并发队列 |

### 图

| 类名 | 说明 |
|------|------|
| `Graph` | 图接口（节点和边） |
| `MutableGraph` | 可变图，支持添加/删除操作 |
| `GraphTraversalUtil` | 图遍历算法（BFS、DFS、拓扑排序） |

### 树

| 类名 | 说明 |
|------|------|
| `Trie` | 前缀树，用于字符串查找 |
| `SkipList` | 概率性跳表 |
| `TreeTraversalUtil` | 树遍历工具 |

### Stream 与迭代

| 类名 | 说明 |
|------|------|
| `OpenCollectors` | 自定义 Stream 收集器 |
| `OpenGatherers` | 自定义 Stream 聚集器（JDK 25） |
| `Streams` | Stream 创建和转换工具 |
| `FluentIterable` | 支持链式操作的流式迭代 |
| `PeekingIterator` | 带预览功能的迭代器 |
| `UnmodifiableIterator` | 不可修改迭代器基类 |

### 转换

| 类名 | 说明 |
|------|------|
| `CollectorUtil` | 收集器工具 |
| `GroupingUtil` | 分组和分类工具 |
| `MoreCollectorUtil` | 额外的收集器工具 |
| `PartitionUtil` | 集合分区工具 |

### 其他

| 类名 | 说明 |
|------|------|
| `Ordering` | 比较器构建器，支持链式调用和空值处理 |
| `Equivalence` | 自定义等价策略 |
| `EntryTransformer` | Map 条目转换接口 |
| `Range` | 不可变区间，支持开/闭端点 |
| `IterableUtil` | Iterable 工具 |
| `IteratorUtil` | Iterator 工具 |
| `OpenCollectionException` | 集合操作异常 |

## 快速开始

```java
import cloud.opencode.base.collections.*;

// BiMap - 双向映射
BiMap<String, Integer> biMap = HashBiMap.create();
biMap.put("one", 1);
String key = biMap.inverse().get(1);  // "one"

// Multimap - 每个键对应多个值
Multimap<String, String> multimap = ArrayListMultimap.create();
multimap.put("fruits", "apple");
multimap.put("fruits", "banana");
Collection<String> fruits = multimap.get("fruits");  // [apple, banana]

// Table - 二维映射
Table<String, String, Integer> table = HashBasedTable.create();
table.put("Alice", "Math", 95);
table.put("Alice", "English", 88);
int score = table.get("Alice", "Math");  // 95

// 不可变集合
ImmutableList<String> list = ImmutableList.of("a", "b", "c");
ImmutableMap<String, Integer> map = ImmutableMap.of("x", 1, "y", 2);

// 原始类型集合（无装箱）
IntList intList = IntList.of(1, 2, 3, 4, 5);
int sum = intList.sum();

// FluentIterable
List<String> result = FluentIterable.from(items)
    .filter(item -> item.isActive())
    .transform(Item::getName)
    .toList();

// 集合运算
Set<String> union = SetUtil.union(setA, setB);
Set<String> intersection = SetUtil.intersection(setA, setB);
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0

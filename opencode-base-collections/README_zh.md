# OpenCode Base Collections

现代集合工具库，提供特殊集合（BiMap、Multiset、Multimap、Table、LinkedHashMultimap）、不可变与持久化集合、原始类型集合、并发集合、图结构（含带权图与 Dijkstra 最短路径）、泛型元组、装饰器基类和树数据结构，适用于 JDK 25+。

> 153 个公开类 | @author Leon Soo | @since JDK 25

## 功能特性

- 特殊集合：BiMap、Multiset、Multimap、Table、LinkedHashMultimap
- 泛型元组：Pair（实现 Map.Entry）、Triple
- 不可变集合：ImmutableList、ImmutableSet、ImmutableMap、ImmutableRangeSet、ImmutableRangeMap 等
- 持久化集合：PersistentList（cons-cell）、PersistentMap（HAMT）、PersistentSet，支持结构共享
- 原始类型集合：IntList、LongList、DoubleList、FloatList、IntSet、LongSet、DoubleSet、IntIntMap、LongLongMap、ObjectIntMap、ObjectLongMap、ObjectDoubleMap 等
- 并发集合：LockFreeQueue、LockFreeStack（Treiber CAS）、ConcurrentHashMultiset
- 图数据结构，带遍历算法
- 带权图：ValueGraph / MutableValueGraph，支持 Dijkstra 最短路径
- 树结构：Trie、SkipList、TreeTraversal
- 集合工具：ListUtil、MapUtil、SetUtil、OpenCollection、ComparisonChain
- Stream 增强：OpenCollectors、OpenGatherers（zipWithIndex、takeWhileInclusive、interleave）、Streams
- 集合代数运算（并集、交集、差集）
- 流式迭代：FluentIterable、PeekingIterator、AbstractIterator
- 装饰器基类：ForwardingCollection、ForwardingList、ForwardingMap、ForwardingSet
- 区间类型：Range、RangeSet、RangeMap、IntInterval
- 集合转换和分组工具
- 排序和等价工具

## Maven 依赖

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-collections</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API 概览

### 核心工具

| 类名 | 说明 |
|------|------|
| `Pair` | 泛型二元组，实现 `Map.Entry` |
| `Triple` | 泛型三元组 |
| `ComparisonChain` | 链式比较器，用于多字段 `compareTo()`，支持短路 |
| `AbstractIterator` | 迭代器骨架类 — 只需实现 `computeNext()` |
| `IntInterval` | 零分配整数区间（类似 Python 的 `range()`） |
| `OpenCollection` | 通用集合工具（空值安全检查、集合运算、过滤） |
| `OpenList` | List 创建和操作工具 |
| `OpenMap` | Map 创建和操作工具 |
| `OpenSet` | Set 创建和操作工具 |
| `ListUtil` | List 工具（分区、zip、去重、展平） |
| `MapUtil` | Map 工具（合并、反转、过滤、转换） |
| `SetUtil` | Set 工具（并集、交集、差集） |
| `CollectionFactory` | 带容量提示的集合创建工厂 |
| `ComparatorUtil` | 比较器构建工具 |

### 装饰器基类

| 类名 | 说明 |
|------|------|
| `ForwardingCollection` | `Collection` 抽象装饰器 — 覆写 `delegate()` + 一个方法 |
| `ForwardingList` | `List` 抽象装饰器 |
| `ForwardingSet` | `Set` 抽象装饰器 |
| `ForwardingMap` | `Map` 抽象装饰器 |

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
| `LinkedHashMultimap` | 保持插入顺序的 Multimap（键和值均保持插入顺序） |
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
| `ImmutableRangeSet` | 不可变区间集合，支持补集、并集、交集 |
| `ImmutableRangeMap` | 不可变区间映射 |
| `ImmutableCollectionUtil` | 创建不可变集合的工具 |

### 持久化集合

| 类名 | 说明 |
|------|------|
| `PersistentList` | cons-cell 持久化链表，支持结构共享 |
| `PersistentMap` | HAMT 持久化映射，支持结构共享 |
| `PersistentSet` | HAMT 持久化集合，支持结构共享（包装 PersistentMap） |

### 原始类型集合

| 类名 | 说明 |
|------|------|
| `IntList` | 原始 int 数组列表 |
| `LongList` | 原始 long 数组列表 |
| `DoubleList` | 原始 double 数组列表 |
| `FloatList` | 原始 float 数组列表 |
| `IntSet` | 原始 int 集合 |
| `LongSet` | 原始 long 集合 |
| `DoubleSet` | 原始 double 集合 |
| `IntIntMap` | 原始 int 到 int 的映射 |
| `IntObjectMap` | 原始 int 到 Object 的映射 |
| `LongObjectMap` | 原始 long 到 Object 的映射 |
| `LongLongMap` | 原始 long 到 long 的映射 |
| `ObjectIntMap` | Object 到原始 int 的映射（无值装箱） |
| `ObjectLongMap` | Object 到原始 long 的映射（无值装箱） |
| `ObjectDoubleMap` | Object 到原始 double 的映射（无值装箱） |

### 并发

| 类名 | 说明 |
|------|------|
| `ConcurrentCollectionFactory` | 并发集合实例工厂 |
| `LockFreeQueue` | 无锁并发队列 |
| `LockFreeStack` | Treiber CAS 无锁并发栈 |

### 图

| 类名 | 说明 |
|------|------|
| `Graph` | 图接口（节点和边） |
| `MutableGraph` | 可变图，支持添加/删除操作 |
| `ValueGraph` | 带权图接口（节点、带值的边） |
| `MutableValueGraph` | 可变带权图，支持添加/删除和边值 |
| `GraphTraversalUtil` | 图遍历算法（BFS、DFS、拓扑排序、Dijkstra） |

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
| `OpenGatherers` | 自定义 Stream 聚集器（zipWithIndex、takeWhileInclusive、interleave） |
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

### ComparisonChain 链式比较器

```java
import cloud.opencode.base.collections.ComparisonChain;

// 在 compareTo() 中使用 — 第一个不等结果即短路
public int compareTo(Person other) {
    return ComparisonChain.start()
        .compare(this.lastName, other.lastName)
        .compare(this.firstName, other.firstName)
        .compare(this.age, other.age)
        .result();
}
```

### AbstractIterator 骨架迭代器

```java
import cloud.opencode.base.collections.AbstractIterator;

// 自定义迭代器 — 只需实现 computeNext()
Iterator<Integer> evens = new AbstractIterator<>() {
    private int next = 0;
    protected Integer computeNext() {
        if (next > 100) return endOfData();
        int result = next;
        next += 2;
        return result;
    }
};
```

### IntInterval 整数区间

```java
import cloud.opencode.base.collections.IntInterval;

IntInterval range = IntInterval.fromTo(1, 10);     // [1,2,3,...,10]
IntInterval evens = IntInterval.fromToBy(0, 20, 2); // [0,2,4,...,20]
IntInterval desc  = IntInterval.fromTo(10, 1);      // [10,9,8,...,1]

boolean has = range.contains(5);    // true，O(1)
int val = range.get(3);             // 4，O(1)
int[] arr = range.toArray();        // 原始 int[]
range.stream().sum();               // IntStream
```

### ObjectIntMap / ObjectLongMap / ObjectDoubleMap

```java
import cloud.opencode.base.collections.primitive.ObjectIntMap;

// 统计词频 — 无装箱开销
ObjectIntMap<String> wordCount = ObjectIntMap.create();
for (String word : words) {
    wordCount.addTo(word, 1);
}
int count = wordCount.getOrDefault("hello", 0);
```

### ForwardingCollection 装饰器

```java
import cloud.opencode.base.collections.ForwardingList;

// 记录所有添加操作的自定义列表
class LoggingList<E> extends ForwardingList<E> {
    private final List<E> delegate = new ArrayList<>();
    protected List<E> delegate() { return delegate; }

    @Override
    public boolean add(E element) {
        System.out.println("添加: " + element);
        return super.add(element);
    }
}
```

### 持久化集合（结构共享）

```java
import cloud.opencode.base.collections.immutable.*;

// PersistentSet — 基于 HAMT 的持久化集合
PersistentSet<String> set = PersistentSet.of("a", "b", "c");
PersistentSet<String> set2 = set.add("d");
// set 仍然是 {a, b, c} — 结构共享

// PersistentMap — HAMT 映射
PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty()
    .put("a", 1).put("b", 2);
PersistentMap<String, Integer> map2 = map.put("c", 3);
// map 仍然是 {a=1, b=2} — 结构共享
```

### ImmutableRangeSet / ImmutableRangeMap

```java
import cloud.opencode.base.collections.immutable.*;
import cloud.opencode.base.collections.Range;

ImmutableRangeSet<Integer> ranges = ImmutableRangeSet.<Integer>builder()
    .add(Range.closed(1, 10))
    .add(Range.closed(20, 30))
    .build();
boolean hit = ranges.contains(5);     // true
boolean miss = ranges.contains(15);   // false

ImmutableRangeMap<Integer, String> grading = ImmutableRangeMap.<Integer, String>builder()
    .put(Range.closed(90, 100), "A")
    .put(Range.closed(80, 89), "B")
    .put(Range.closed(70, 79), "C")
    .build();
String grade = grading.get(85);  // "B"
```

### LinkedHashMultimap 有序多值映射

```java
import cloud.opencode.base.collections.specialized.LinkedHashMultimap;

// 保持插入顺序的 Multimap
LinkedHashMultimap<String, String> mm = LinkedHashMultimap.create();
mm.put("fruits", "apple");
mm.put("vegs", "carrot");
mm.put("fruits", "banana");
// keySet 迭代顺序: fruits, vegs（插入顺序）
// get("fruits"): {apple, banana}（插入顺序，无重复）
```

### Gatherers（zipWithIndex、takeWhileInclusive、interleave）

```java
import cloud.opencode.base.collections.Pair;
import cloud.opencode.base.collections.OpenGatherers;
import java.util.List;

List<Pair<Long, String>> indexed = List.of("a", "b", "c").stream()
    .gather(OpenGatherers.zipWithIndex())
    .toList(); // [Pair(0, a), Pair(1, b), Pair(2, c)]

List<Integer> inclusive = List.of(1, 2, 3, 4, 5).stream()
    .gather(OpenGatherers.takeWhileInclusive(x -> x < 3))
    .toList(); // [1, 2, 3]
```

### ValueGraph + Dijkstra 最短路径

```java
import cloud.opencode.base.collections.graph.MutableValueGraph;
import cloud.opencode.base.collections.graph.GraphTraversalUtil;
import java.util.List;

MutableValueGraph<String, Double> graph = MutableValueGraph.directed();
graph.addNode("A");
graph.addNode("B");
graph.addNode("C");
graph.putEdgeValue("A", "B", 1.0);
graph.putEdgeValue("B", "C", 2.0);
graph.putEdgeValue("A", "C", 5.0);

List<String> path = GraphTraversalUtil.shortestWeightedPath(graph, "A", "C"); // [A, B, C]
```

### LockFreeStack 无锁栈

```java
import cloud.opencode.base.collections.concurrent.LockFreeStack;

LockFreeStack<String> stack = new LockFreeStack<>();
stack.push("first");
stack.push("second");
String top = stack.pop(); // "second"
```

### 原始类型集合（DoubleSet、LongLongMap、FloatList）

```java
import cloud.opencode.base.collections.primitive.DoubleSet;
import cloud.opencode.base.collections.primitive.LongLongMap;
import cloud.opencode.base.collections.primitive.FloatList;

DoubleSet doubles = DoubleSet.of(1.0, 2.0, 3.14);
boolean has = doubles.contains(3.14); // true

LongLongMap longMap = LongLongMap.create();
longMap.put(1L, 100L);
long val = longMap.get(1L); // 100

FloatList floats = FloatList.of(1.5f, 2.5f, 3.5f);
float sum = floats.sum(); // 7.5
```

## 环境要求

- Java 25+

## 许可证

Apache License 2.0

# Collections 组件方案

## 1. 组件概述

Collections 组件提供现代化、高性能的集合框架扩展，基于 JDK 25 原生 API 实现，零外部依赖。核心能力包括：

- **统一门面入口**：`OpenCollectors`/`OpenCollection`/`OpenList`/`OpenMap`/`OpenSet` 简化集合操作
- **特殊集合**：BiMap（双向映射）、Multiset（计数集合）、Multimap（多值映射）、Table（二维表）
- **不可变集合**：完整的 `ImmutableList`/`ImmutableSet`/`ImmutableMap`/`ImmutableBiMap`/`ImmutableTable` 等
- **原生类型集合**：`IntList`/`LongList`/`DoubleList`/`IntSet`/`LongSet`/`IntIntMap` 等，避免自动装箱
- **流式 API**：`FluentIterable`、`OpenGatherers`（JDK 25 Stream Gatherers）、`Streams` 增强工具
- **图数据结构**：`MutableGraph` + `GraphTraversalUtil`（BFS/DFS/拓扑排序/最短路径）
- **值域范围**：`Range`（连续范围）、`SetView`（惰性集合视图）
- **并发集合**：`ConcurrentCollectionFactory`、`LockFreeQueue`

## 2. 包结构

```
cloud.opencode.base.collections
├── OpenCollection                 # 通用集合工具门面
├── OpenCollectors                 # 流式 API 入口 + Collector 工具
├── OpenList / OpenMap / OpenSet   # 类型门面
├── OpenGatherers                  # JDK 25 Stream Gatherers 工具
├── FluentIterable                 # 流式可迭代 API
├── Ordering                       # 流式比较器构建器
├── Streams                        # Stream 增强工具
├── Range                          # 值域范围
├── CollectionFactory              # 集合工厂
├── ComparatorUtil                 # 比较器工具
├── ListUtil / SetUtil / MapUtil   # 类型工具类
├── IterableUtil / IteratorUtil    # 迭代工具
├── BiMap / HashBiMap              # 双向映射
├── Multiset / HashMultiset        # 计数集合
├── Multimap / ArrayListMultimap / HashSetMultimap  # 多值映射
├── Table / HashBasedTable         # 二维表
├── Equivalence                    # 等价策略
├── SetView / SetAlgebra           # 集合代数
├── immutable/                     # 不可变集合子包
│   ├── ImmutableList / ImmutableSet / ImmutableMap
│   ├── ImmutableBiMap / ImmutableSortedMap / ImmutableSortedSet
│   ├── ImmutableMultiset / ImmutableListMultimap / ImmutableSetMultimap
│   ├── ImmutableTable / ImmutableClassToInstanceMap
│   └── ImmutableCollectionUtil
├── primitive/                     # 原生类型集合子包
│   ├── IntList / LongList / DoubleList
│   ├── IntSet / LongSet
│   └── IntIntMap / IntObjectMap
├── graph/                         # 图数据结构子包
│   ├── Graph / MutableGraph
│   └── GraphTraversalUtil
├── concurrent/                    # 并发集合子包
│   ├── ConcurrentCollectionFactory
│   └── LockFreeQueue
└── exception/
    └── OpenCollectionException
```

## 3. 核心 API

### 3.1 OpenCollection

> 通用集合工具门面类，提供集合的判空、集合运算（并集/交集/差集）、查找、过滤、转换等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `isEmpty(Collection<?>)` | 判断集合是否为空或 null |
| `isNotEmpty(Collection<?>)` | 判断集合是否非空 |
| `isEmpty(Map<?, ?>)` | 判断 Map 是否为空或 null |
| `union(Iterable, Iterable)` | 计算两个集合的并集 |
| `intersection(Iterable, Iterable)` | 计算两个集合的交集 |
| `subtract(Iterable, Iterable)` | 计算两个集合的差集 |
| `disjunction(Iterable, Iterable)` | 计算两个集合的对称差集 |
| `containsAny(Collection, Object...)` | 判断集合是否包含任意给定元素 |
| `containsAll(Collection, Collection)` | 判断集合是否包含所有给定元素 |
| `isEqualCollection(Collection, Collection)` | 判断两个集合内容是否相等 |
| `isSubCollection(Collection, Collection)` | 判断是否为子集合 |
| `countMatches(Iterable, Predicate)` | 统计满足条件的元素数量 |
| `select(Iterable, Predicate)` | 筛选满足条件的元素 |
| `collect(Iterable, Function)` | 转换集合中的元素 |
| `find(Iterable, Predicate)` | 查找第一个满足条件的元素 |
| `extractSingleton(Collection)` | 提取唯一元素，非唯一时抛异常 |
| `permutations(Collection)` | 计算全排列 |
| `collate(Iterable, Iterable, Comparator)` | 归并排序两个有序集合 |
| `emptyIfNull(Collection)` | null 安全，返回空集合替代 null |

**示例：**

```java
// 集合运算
Collection<String> union = OpenCollection.union(list1, list2);
Collection<String> inter = OpenCollection.intersection(list1, list2);
Collection<String> diff = OpenCollection.subtract(list1, list2);

// 筛选和查找
Collection<String> filtered = OpenCollection.select(list, s -> s.startsWith("A"));
int count = OpenCollection.countMatches(list, s -> s.length() > 5);
String found = OpenCollection.find(list, s -> s.contains("test"));
```

### 3.2 OpenCollectors

> 统一流式 API 入口，提供 `CollectorFlow` 链式操作以及自定义 `Collector` 工厂方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `from(Iterable)` | 从 Iterable 创建 CollectorFlow |
| `of(E...)` | 从可变参数创建 CollectorFlow |
| `fromStream(Stream)` | 从 Stream 创建 CollectorFlow |
| `algebra(Set)` | 创建集合代数操作对象 |
| `toImmutableList()` | 收集为 ImmutableList 的 Collector |
| `toImmutableSet()` | 收集为 ImmutableSet 的 Collector |
| `toImmutableMap(Function, Function)` | 收集为 ImmutableMap 的 Collector |
| `toMultiset()` | 收集为 Multiset 的 Collector |
| `counting()` | 统计元素出现次数的 Collector |
| `onlyElement()` | 提取唯一元素的 Collector |
| `toOptional()` | 收集为 Optional 的 Collector |
| `leastK(int, Comparator)` | 取最小的 K 个元素 |
| `greatestK(int, Comparator)` | 取最大的 K 个元素 |
| `partitionBySize(int)` | 按大小分区的 Collector |

**CollectorFlow 链式方法：**

| 方法 | 描述 |
|------|------|
| `filter(Predicate)` | 过滤 |
| `map(Function)` | 映射 |
| `flatMap(Function)` | 扁平映射 |
| `distinct()` | 去重 |
| `sorted()` / `sorted(Comparator)` | 排序 |
| `limit(long)` / `skip(long)` | 限制/跳过 |
| `toList()` / `toSet()` | 收集为 List/Set |
| `toImmutableList()` / `toImmutableSet()` | 收集为不可变集合 |
| `groupBy(Function)` | 分组 |
| `partition(int)` | 按大小分区 |
| `count()` / `findFirst()` / `findAny()` | 终端操作 |

**示例：**

```java
// 流式操作
List<String> names = OpenCollectors.from(users)
    .filter(u -> u.isActive())
    .map(User::getName)
    .sorted()
    .toList();

// 分组
Map<String, List<User>> byCity = OpenCollectors.from(users)
    .groupBy(User::getCity);

// 自定义 Collector
ImmutableList<String> immutable = stream.collect(OpenCollectors.toImmutableList());
```

### 3.3 OpenList

> 列表门面工具类，简化 List 的创建、转换、分区、反转等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newArrayList()` | 创建空 ArrayList |
| `of(E...)` | 从可变参数创建 ArrayList |
| `from(Iterable)` | 从 Iterable 创建 ArrayList |
| `withCapacity(int)` | 创建指定初始容量的 ArrayList |
| `newLinkedList()` | 创建空 LinkedList |
| `reverse(List)` | 反转列表 |
| `partition(List, int)` | 按大小分区 |
| `transform(List, Function)` | 转换列表元素 |
| `charactersOf(String)` | 字符串转字符列表 |
| `getFirst(List)` / `getLast(List)` | 获取首个/最后一个元素 |
| `findFirst(List, Predicate)` | 查找第一个匹配元素 |
| `filter(List, Predicate)` | 过滤列表 |
| `cartesianProduct(List...)` | 计算笛卡尔积 |

**示例：**

```java
List<String> list = OpenList.of("a", "b", "c");
List<String> reversed = OpenList.reverse(list);
List<List<String>> partitions = OpenList.partition(list, 2);
List<Integer> lengths = OpenList.transform(list, String::length);
```

### 3.4 OpenMap

> Map 门面工具类，简化 Map 的创建、转换、过滤等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newHashMap()` | 创建空 HashMap |
| `of(K, V)` / `of(K, V, K, V)` / `of(K, V, K, V, K, V)` | 快速创建 HashMap |
| `from(Map)` | 从已有 Map 复制 |
| `withExpectedSize(int)` | 创建预估大小的 HashMap |
| `newLinkedHashMap()` / `newTreeMap()` / `newConcurrentMap()` | 创建不同类型的 Map |
| `transformValues(Map, Function)` | 转换 Map 的值 |
| `transformEntries(Map, EntryTransformer)` | 转换 Map 的键值对 |
| `filterKeys(Map, Predicate)` | 按键过滤 |
| `filterValues(Map, Predicate)` | 按值过滤 |
| `filterEntries(Map, Predicate)` | 按条目过滤 |
| `uniqueIndex(Iterable, Function)` | 创建唯一索引 Map |
| `getOrDefault(Map, K, V)` | 获取值或默认值 |
| `containsAllKeys(Map, K...)` | 判断是否包含所有键 |
| `immutableEntry(K, V)` | 创建不可变 Entry |

**示例：**

```java
Map<String, Integer> map = OpenMap.of("a", 1, "b", 2, "c", 3);
Map<String, Integer> filtered = OpenMap.filterKeys(map, k -> k.startsWith("a"));
Map<String, String> transformed = OpenMap.transformValues(map, Object::toString);
Map<String, User> byId = OpenMap.uniqueIndex(users, User::getId);
```

### 3.5 OpenSet

> Set 门面工具类，提供集合运算（并集/交集/差集/对称差）、幂集、组合等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newHashSet()` / `of(E...)` / `from(Iterable)` | 创建 HashSet |
| `newLinkedHashSet()` / `newTreeSet()` / `newConcurrentHashSet()` | 创建其他 Set |
| `union(Set, Set)` | 并集（返回 SetView） |
| `intersection(Set, Set)` | 交集（返回 SetView） |
| `difference(Set, Set)` | 差集（返回 SetView） |
| `symmetricDifference(Set, Set)` | 对称差集 |
| `powerSet(Set)` | 幂集 |
| `combinations(Set, int)` | 组合 |
| `cartesianProduct(Set...)` | 笛卡尔积 |
| `filter(Set, Predicate)` | 过滤 |
| `algebra(Set)` | 创建集合代数操作对象 |
| `disjoint(Set, Set)` | 判断是否不相交 |
| `isSubset(Set, Set)` / `equals(Set, Set)` | 子集/相等判断 |

**示例：**

```java
Set<String> set1 = OpenSet.of("a", "b", "c");
Set<String> set2 = OpenSet.of("b", "c", "d");
SetView<String> union = OpenSet.union(set1, set2);         // [a, b, c, d]
SetView<String> inter = OpenSet.intersection(set1, set2);  // [b, c]
SetView<String> diff = OpenSet.difference(set1, set2);     // [a]
Set<Set<String>> power = OpenSet.powerSet(set1);           // 8 个子集
```

### 3.6 OpenGatherers

> JDK 25 Stream Gatherers 工具类，提供窗口、去重、扫描、索引等中间操作 Gatherer。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `windowFixed(int)` | 固定大小窗口 |
| `windowSliding(int)` / `windowSliding(int, int)` | 滑动窗口（可指定步长） |
| `batch(int)` | 批处理 |
| `batchProcess(int, Function)` | 批处理并转换 |
| `distinctBy(Function)` | 按键去重 |
| `scan(R, BiFunction)` | 前缀扫描 |
| `takeWhileIndexed(BiPredicate)` | 带索引的 takeWhile |
| `dropWhileIndexed(BiPredicate)` | 带索引的 dropWhile |
| `filterIndexed(BiPredicate)` | 带索引的 filter |
| `changed()` | 去除连续重复元素 |
| `changedBy(Function)` | 按键去除连续重复 |
| `mapIndexed(BiFunction)` | 带索引的 map |
| `mapWithPrevious(BiFunction)` | 与前一个元素配对 map |
| `zipWithNext(BiFunction)` | 与下一个元素配对 |
| `groupRuns(Function)` | 按连续相同键分组 |
| `takeLast(int)` / `dropLast(int)` | 取/丢弃末尾 N 个 |
| `fold(R, BiFunction)` | 折叠 |
| `intersperse(T)` | 在元素间插入分隔符 |
| `indexed()` | 添加索引 |

**示例：**

```java
// 固定窗口
Stream<List<Integer>> windows = stream.gather(OpenGatherers.windowFixed(3));

// 滑动窗口
Stream<List<Integer>> sliding = stream.gather(OpenGatherers.windowSliding(3));

// 按键去重
Stream<User> unique = users.stream().gather(OpenGatherers.distinctBy(User::getEmail));

// 前缀和
Stream<Integer> sums = numbers.stream().gather(OpenGatherers.scan(0, Integer::sum));
```

### 3.7 FluentIterable

> 可迭代操作的流式 API，提供链式的 filter/transform/limit/skip 等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `from(Iterable)` | 从 Iterable 创建 |
| `of(E...)` | 从可变参数创建 |
| `empty()` | 空的 FluentIterable |
| `concat(Iterable...)` | 拼接多个 Iterable |
| `filter(Predicate)` / `filter(Class)` | 过滤 |
| `transform(Function)` | 转换 |
| `flatMap(Function)` | 扁平映射 |
| `limit(int)` / `skip(int)` | 限制/跳过 |
| `cycle()` | 循环迭代 |
| `distinct()` | 去重 |
| `append(Iterable)` | 追加 |
| `first()` / `last()` / `get(int)` | 获取元素 |
| `anyMatch(Predicate)` / `allMatch(Predicate)` | 匹配判断 |
| `toList()` / `toSet()` / `toImmutableList()` | 收集 |
| `toMap(Function, Function)` | 转为 Map |
| `join(String)` | 拼接为字符串 |
| `stream()` | 转为 Stream |

**示例：**

```java
List<Integer> lengths = FluentIterable.from(strings)
    .filter(s -> s.length() > 3)
    .transform(String::length)
    .toList();

Optional<String> first = FluentIterable.from(strings)
    .filter(s -> s.startsWith("A"))
    .first();
```

### 3.8 Ordering

> 流式比较器构建器，支持自然排序、null 处理、反转、复合、转换等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `natural()` | 自然排序 |
| `from(Comparator)` | 从 Comparator 创建 |
| `explicit(T...)` / `explicit(List)` | 显式排序 |
| `allEqual()` | 所有元素视为相等 |
| `arbitrary()` | 任意但一致的排序 |
| `usingToString()` | 按 toString 排序 |
| `reverse()` | 反转 |
| `nullsFirst()` / `nullsLast()` | null 处理 |
| `compound(Comparator)` | 复合排序 |
| `onResultOf(Function)` | 按转换结果排序 |
| `sortedCopy(Iterable)` | 返回排序后的副本 |
| `immutableSortedCopy(Iterable)` | 返回不可变的排序副本 |
| `isOrdered(Iterable)` / `isStrictlyOrdered(Iterable)` | 判断是否有序 |
| `min(Iterable)` / `max(Iterable)` | 最小/最大元素 |
| `leastOf(Iterable, int)` / `greatestOf(Iterable, int)` | 最小/最大的 K 个 |
| `binarySearch(List, T)` | 二分查找 |

**示例：**

```java
Ordering<String> natural = Ordering.natural();
Ordering<String> reversed = Ordering.natural().reverse();
Ordering<String> nullsFirst = Ordering.natural().nullsFirst();
Ordering<Person> byAge = Ordering.natural().onResultOf(Person::getAge);

List<String> sorted = Ordering.natural().sortedCopy(strings);
String min = Ordering.natural().min(strings);
```

### 3.9 Streams

> 增强的 Stream 工具类，提供 zip、findLast、mapWithIndex、interleave 等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `zip(Stream, Stream, BiFunction)` | 合并两个流 |
| `zipWithIndex(Stream)` | 为元素添加索引 |
| `findLast(Stream)` / `findLast(Iterable)` | 查找最后一个元素 |
| `mapWithIndex(Stream, BiFunction)` | 带索引的 map |
| `filterWithIndex(Stream, BiPredicate)` | 带索引的 filter |
| `forEachPair(Stream, BiConsumer)` | 相邻元素对遍历 |
| `mapPairs(Stream, BiFunction)` | 相邻元素对映射 |
| `concat(Stream...)` | 拼接多个流 |
| `interleave(Stream, Stream)` | 交替合并两个流 |
| `stream(Optional)` / `stream(Iterator)` / `stream(Iterable)` | 转为 Stream |

**示例：**

```java
Stream<String> result = Streams.zip(
    Stream.of("Alice", "Bob"),
    Stream.of(30, 25),
    (name, age) -> name + "=" + age
);

Streams.forEachPair(
    Stream.of(1, 2, 3, 4),
    (a, b) -> System.out.println(a + "->" + b)
);
```

### 3.10 Range

> 可比较值的连续范围，支持开/闭区间、包含判断、交集、跨越等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `closed(C, C)` | 闭区间 [lower, upper] |
| `open(C, C)` | 开区间 (lower, upper) |
| `closedOpen(C, C)` | 左闭右开 [lower, upper) |
| `openClosed(C, C)` | 左开右闭 (lower, upper] |
| `atMost(C)` / `lessThan(C)` | 上界范围 |
| `atLeast(C)` / `greaterThan(C)` | 下界范围 |
| `all()` | 全范围 |
| `singleton(C)` | 单值范围 |
| `contains(C)` | 判断是否包含值 |
| `encloses(Range)` | 判断是否包含另一个范围 |
| `isConnected(Range)` | 判断是否相连 |
| `intersection(Range)` | 交集 |
| `span(Range)` | 跨越（最小包含范围） |
| `isEmpty()` | 是否为空 |

**示例：**

```java
Range<Integer> closed = Range.closed(1, 10);    // [1, 10]
Range<Integer> open = Range.open(1, 10);        // (1, 10)
boolean contains = closed.contains(5);           // true
Range<Integer> inter = closed.intersection(Range.closed(5, 15)); // [5, 10]
```

### 3.11 BiMap / HashBiMap

> 双向映射接口及其基于哈希的实现，键和值都唯一，支持反向查找。

**BiMap 接口主要方法：**

| 方法 | 描述 |
|------|------|
| `put(K, V)` | 放入键值对，值已存在时抛异常 |
| `forcePut(K, V)` | 强制放入，覆盖已有映射 |
| `inverse()` | 获取反向视图 BiMap<V, K> |

**HashBiMap 工厂方法：**

| 方法 | 描述 |
|------|------|
| `create()` | 创建空 HashBiMap |
| `create(int)` | 创建指定初始容量的 HashBiMap |
| `create(Map)` | 从已有 Map 创建 |

**示例：**

```java
HashBiMap<String, Integer> bimap = HashBiMap.create();
bimap.put("one", 1);
bimap.put("two", 2);

Integer value = bimap.get("one");          // 1
BiMap<Integer, String> inverse = bimap.inverse();
String key = inverse.get(1);               // "one"
```

### 3.12 Multiset / HashMultiset

> 计数集合接口及其基于哈希的实现，记录每个元素的出现次数。

**Multiset 接口主要方法（通过 HashMultiset 实现）：**

| 方法 | 描述 |
|------|------|
| `create()` / `create(int)` / `create(Iterable)` | 工厂方法 |
| `count(Object)` | 获取元素出现次数 |
| `add(E, int)` | 添加指定次数的元素 |
| `remove(Object, int)` | 移除指定次数 |
| `setCount(E, int)` | 设置元素计数 |
| `setCount(E, int, int)` | CAS 方式设置计数 |
| `elementSet()` | 获取不重复元素集合 |
| `entrySet()` | 获取计数条目集合 |

**示例：**

```java
HashMultiset<String> multiset = HashMultiset.create();
multiset.add("apple", 3);
multiset.add("banana", 2);
int count = multiset.count("apple");       // 3
Set<String> elements = multiset.elementSet(); // [apple, banana]
```

### 3.13 Multimap / ArrayListMultimap / HashSetMultimap

> 多值映射接口及其实现，每个键可以关联多个值。

**ArrayListMultimap（值有序，允许重复）：**

| 方法 | 描述 |
|------|------|
| `create()` / `create(int, int)` / `create(Multimap)` | 工厂方法 |
| `put(K, V)` | 添加键值对 |
| `get(K)` | 获取键对应的值集合 |
| `getList(K)` | 获取键对应的 List |
| `removeAll(Object)` | 移除键的所有值 |
| `size()` | 总键值对数量 |
| `keySet()` / `keys()` / `values()` / `entries()` | 视图 |
| `asMap()` | 转为 Map<K, Collection<V>> |

**HashSetMultimap（值不重复）：**

| 方法 | 描述 |
|------|------|
| `create()` / `create(int, int)` / `create(Multimap)` | 工厂方法 |
| `getSet(K)` | 获取键对应的 Set |

**示例：**

```java
ArrayListMultimap<String, Integer> multimap = ArrayListMultimap.create();
multimap.put("a", 1);
multimap.put("a", 2);
multimap.put("a", 1);  // 允许重复
List<Integer> values = multimap.getList("a"); // [1, 2, 1]

HashSetMultimap<String, Integer> setMultimap = HashSetMultimap.create();
setMultimap.put("a", 1);
setMultimap.put("a", 1);  // 忽略重复
Set<Integer> unique = setMultimap.getSet("a"); // [1]
```

### 3.14 Table / HashBasedTable

> 二维表接口及其基于哈希的实现，通过行键和列键定位值。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `create()` / `create(int, int)` / `create(Table)` | 工厂方法 |
| `put(R, C, V)` | 放入值 |
| `get(Object, Object)` | 按行列键获取值 |
| `contains(Object, Object)` | 判断是否包含 |
| `containsRow(Object)` / `containsColumn(Object)` | 判断行/列是否存在 |
| `remove(Object, Object)` | 移除 |
| `row(R)` | 获取行视图 Map<C, V> |
| `column(C)` | 获取列视图 Map<R, V> |
| `rowKeySet()` / `columnKeySet()` | 行键/列键集合 |
| `rowMap()` / `columnMap()` | 行/列 Map 视图 |
| `cellSet()` | 获取所有 Cell 集合 |

**示例：**

```java
HashBasedTable<String, String, Integer> table = HashBasedTable.create();
table.put("row1", "col1", 100);
table.put("row1", "col2", 200);
table.put("row2", "col1", 300);

Integer value = table.get("row1", "col1");         // 100
Map<String, Integer> row = table.row("row1");      // {col1=100, col2=200}
Map<String, Integer> col = table.column("col1");   // {row1=100, row2=300}
```

### 3.15 ImmutableList

> 不可变列表实现，创建后不可修改，线程安全。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(E)` / `of(E, E)` / `of(E...)` | 创建不可变列表 |
| `copyOf(Collection)` / `copyOf(Iterable)` | 从已有集合复制 |
| `builder()` | 获取 Builder |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `add(E)` / `add(E...)` / `addAll(Iterable)` | 添加元素 |
| `build()` | 构建不可变列表 |

**示例：**

```java
ImmutableList<String> list = ImmutableList.of("a", "b", "c");
ImmutableList<String> list2 = ImmutableList.<String>builder()
    .add("x")
    .addAll(existingList)
    .build();
```

### 3.16 ImmutableMap

> 不可变映射实现，创建后不可修改，线程安全。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(K, V)` / `of(K, V, K, V)` ... | 创建不可变 Map |
| `copyOf(Map)` | 从已有 Map 复制 |
| `builder()` | 获取 Builder |

**示例：**

```java
ImmutableMap<String, Integer> map = ImmutableMap.of("a", 1, "b", 2);
ImmutableMap<String, Integer> map2 = ImmutableMap.<String, Integer>builder()
    .put("x", 10)
    .putAll(existingMap)
    .build();
```

### 3.17 ImmutableSet

> 不可变集合实现，创建后不可修改，线程安全。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(E)` / `of(E, E)` / `of(E...)` | 创建不可变 Set |
| `copyOf(Collection)` / `copyOf(Iterable)` | 从已有集合复制 |
| `builder()` | 获取 Builder |

**示例：**

```java
ImmutableSet<String> set = ImmutableSet.of("a", "b", "c");
ImmutableSet<String> set2 = ImmutableSet.<String>builder()
    .add("x")
    .addAll(existingSet)
    .build();
```

### 3.18 ImmutableBiMap

> 不可变双向映射实现，键和值都唯一。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(K, V)` / `of(K, V, K, V)` | 创建不可变 BiMap |
| `copyOf(Map)` | 从已有 Map 复制 |
| `builder()` | 获取 Builder |
| `inverse()` | 获取反向视图 |
| `getKey(V)` | 通过值查找键 |

**示例：**

```java
ImmutableBiMap<String, Integer> bimap = ImmutableBiMap.<String, Integer>builder()
    .put("one", 1)
    .put("two", 2)
    .build();
ImmutableBiMap<Integer, String> inverse = bimap.inverse();
```

### 3.19 ImmutableTable

> 不可变二维表实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(R, C, V)` | 创建不可变 Table |
| `builder()` | 获取 Builder |
| `get(Object, Object)` | 按行列键获取值 |
| `row(R)` / `column(C)` | 行/列视图 |
| `rowKeySet()` / `columnKeySet()` | 行键/列键集合 |
| `cellSet()` | 所有 Cell 集合 |

**示例：**

```java
ImmutableTable<String, String, Integer> table = ImmutableTable.<String, String, Integer>builder()
    .put("row1", "col1", 1)
    .put("row1", "col2", 2)
    .build();
```

### 3.20 ImmutableSortedMap

> 不可变有序映射实现，按键自然排序或自定义排序。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(K, V)` / `of(K, V, K, V)` | 创建（自然排序） |
| `copyOf(Map)` / `copyOf(Map, Comparator)` | 复制 |
| `naturalOrder()` | 自然排序 Builder |
| `orderedBy(Comparator)` | 自定义排序 Builder |
| `firstKey()` / `lastKey()` | 首个/最后键 |
| `subMap()` / `headMap()` / `tailMap()` | 子映射 |

**示例：**

```java
ImmutableSortedMap<String, Integer> map = ImmutableSortedMap.<String, Integer>naturalOrder()
    .put("c", 3)
    .put("a", 1)
    .put("b", 2)
    .build();
// 按键排序：a=1, b=2, c=3
```

### 3.21 ImmutableSortedSet

> 不可变有序集合实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(E)` / `of(E...)` | 创建（自然排序） |
| `copyOf(Collection)` / `copyOf(Collection, Comparator)` | 复制 |
| `naturalOrder()` / `orderedBy(Comparator)` | Builder |

**示例：**

```java
ImmutableSortedSet<String> set = ImmutableSortedSet.of("c", "a", "b"); // [a, b, c]
```

### 3.22 ImmutableMultiset

> 不可变多重集实现。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(E...)` | 创建 |
| `copyOf(Collection)` | 复制 |
| `builder()` | 获取 Builder |
| `count(Object)` | 获取计数 |
| `elementSet()` | 不重复元素集合 |
| `entrySet()` | 计数条目集合 |

**示例：**

```java
ImmutableMultiset<String> multiset = ImmutableMultiset.of("a", "b", "a", "c", "b", "a");
int count = multiset.count("a"); // 3
```

### 3.23 ImmutableListMultimap / ImmutableSetMultimap

> 不可变多值映射实现（List 版本允许重复值，Set 版本不允许）。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(K, V)` / `of(K, V, K, V)` | 创建 |
| `copyOf(Multimap)` | 复制 |
| `builder()` | 获取 Builder |
| `get(K)` | 获取值列表/集合 |
| `inverse()` | 反向多值映射 |

**示例：**

```java
ImmutableListMultimap<String, Integer> multimap = ImmutableListMultimap.<String, Integer>builder()
    .put("a", 1)
    .put("a", 2)
    .putAll("b", List.of(3, 4))
    .build();
ImmutableList<Integer> values = multimap.get("a"); // [1, 2]
```

### 3.24 ImmutableClassToInstanceMap

> 不可变类型实例映射，按 Class 类型安全地存取实例。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of()` / `of(Class, T)` | 创建 |
| `copyOf(Map)` | 复制 |
| `builder()` | 获取 Builder |
| `getInstance(Class)` | 类型安全地获取实例 |

**示例：**

```java
ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.builder()
    .put(String.class, "hello")
    .put(Integer.class, 42)
    .build();
String str = map.getInstance(String.class); // "hello"
```

### 3.25 ImmutableCollectionUtil

> 不可变集合工具类，提供转换、拼接、过滤等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `transform(ImmutableList, Function)` | 转换列表 |
| `concat(ImmutableList...)` | 拼接列表 |
| `reverse(ImmutableList)` | 反转列表 |
| `union(ImmutableSet...)` / `intersection(...)` / `difference(...)` | 集合运算 |
| `toBiMap(Map)` | 转为不可变 BiMap |
| `toTable(Map)` | 转为不可变 Table |
| `toSet(ImmutableList)` / `toList(ImmutableSet)` | 类型互转 |
| `filter(ImmutableList, Predicate)` | 过滤 |
| `isNullOrEmpty(Collection)` | 判空 |

### 3.26 Equivalence

> 自定义等价策略，可替换默认的 `equals`/`hashCode` 行为。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `equals()` | 基于 Object.equals 的等价策略 |
| `identity()` | 基于引用相等的等价策略 |
| `from(BiPredicate, ToIntFunction)` | 自定义等价策略 |
| `equivalent(T, T)` | 判断两个对象是否等价 |
| `hash(T)` | 计算等价哈希值 |

**示例：**

```java
Equivalence<String> caseInsensitive = Equivalence.from(
    String::equalsIgnoreCase, s -> s.toLowerCase().hashCode()
);
caseInsensitive.equivalent("Hello", "hello"); // true
```

### 3.27 ComparatorUtil

> 比较器工具类，提供 null 处理、字典序、最小/最大 K 个元素等。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `nullsFirst(Comparator)` / `nullsLast(Comparator)` | null 处理 |
| `isInOrder(Iterable, Comparator)` | 判断是否有序 |
| `isInStrictOrder(Iterable, Comparator)` | 判断是否严格有序 |
| `lexicographical()` / `lexicographical(Comparator)` | 字典序比较器 |
| `min(T, T, Comparator)` / `max(T, T, Comparator)` | 最小/最大值 |
| `least(int, Comparator)` / `greatest(int, Comparator)` | 最小/最大 K 个的 Collector |

**示例：**

```java
List<Integer> smallest = stream.collect(ComparatorUtil.least(5, Comparator.naturalOrder()));
boolean ordered = ComparatorUtil.isInOrder(list, Comparator.naturalOrder());
```

### 3.28 ListUtil

> 列表工具类，提供创建、分区、转换、反转、笛卡尔积等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newArrayList()` / `newArrayList(E...)` / `newArrayList(Iterable)` | 创建 ArrayList |
| `newArrayListWithCapacity(int)` / `newArrayListWithExpectedSize(int)` | 指定容量创建 |
| `newLinkedList()` / `newCopyOnWriteArrayList()` | 创建其他 List |
| `reverse(List)` | 反转 |
| `partition(List, int)` | 分区 |
| `transform(List, Function)` | 转换 |
| `charactersOf(String)` | 字符串转字符列表 |
| `cartesianProduct(List...)` | 笛卡尔积 |

### 3.29 SetUtil

> 集合工具类，提供集合运算、幂集、笛卡尔积、过滤、同步/不可修改包装等。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newHashSet()` / `newHashSet(E...)` / `newLinkedHashSet()` | 创建 Set |
| `union(Set, Set)` / `intersection(Set, Set)` / `difference(Set, Set)` | 集合运算 |
| `symmetricDifference(Set, Set)` | 对称差集 |
| `powerSet(Set)` | 幂集 |
| `combinations(Set, int)` | 组合 |
| `cartesianProduct(Set...)` | 笛卡尔积 |
| `filter(Set, Predicate)` | 过滤 |
| `synchronizedSet(Set)` / `unmodifiableSet(Set)` | 包装 |

### 3.30 MapUtil

> Map 工具类，提供创建、唯一索引、差异比较、转换、过滤等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newHashMap()` / `newLinkedHashMap()` / `newTreeMap()` / `newConcurrentMap()` | 创建 |
| `uniqueIndex(Iterable, Function)` | 创建唯一索引 |
| `difference(Map, Map)` | 比较两个 Map，返回 MapDifference |
| `transformValues(Map, Function)` | 转换值 |
| `transformEntries(Map, EntryTransformer)` | 转换键值对 |
| `filterKeys(Map, Predicate)` / `filterValues(Map, Predicate)` | 过滤 |
| `fromProperties(Properties)` | Properties 转 Map |
| `synchronizedBiMap(BiMap)` / `unmodifiableBiMap(BiMap)` | BiMap 包装 |

### 3.31 IterableUtil

> 可迭代对象工具类，提供拼接、分区、过滤、查找、转换等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `concat(Iterable...)` | 拼接 |
| `partition(Iterable, int)` / `paddedPartition(Iterable, int)` | 分区 |
| `filter(Iterable, Predicate)` / `filter(Iterable, Class)` | 过滤 |
| `any(Iterable, Predicate)` / `all(Iterable, Predicate)` | 匹配判断 |
| `tryFind(Iterable, Predicate)` | 查找 |
| `getOnlyElement(Iterable)` | 获取唯一元素 |
| `getFirst(Iterable, E)` / `getLast(Iterable)` / `get(Iterable, int)` | 按位置获取 |
| `transform(Iterable, Function)` | 转换 |
| `limit(Iterable, int)` / `skip(Iterable, int)` / `cycle(Iterable)` | 限制/跳过/循环 |
| `size(Iterable)` / `contains(Iterable, Object)` / `isEmpty(Iterable)` | 查询 |
| `elementsEqual(Iterable, Iterable)` | 比较 |

### 3.32 IteratorUtil

> 迭代器工具类，提供空迭代器、单元素迭代器、拼接、分区、PeekingIterator 等。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `emptyIterator()` | 空迭代器 |
| `singletonIterator(E)` | 单元素迭代器 |
| `unmodifiableIterator(Iterator)` | 不可修改迭代器 |
| `concat(Iterator...)` | 拼接 |
| `partition(Iterator, int)` | 分区 |
| `filter(Iterator, Predicate)` | 过滤 |
| `transform(Iterator, Function)` | 转换 |
| `peekingIterator(Iterator)` | PeekingIterator（可预览下一元素） |
| `advance(Iterator, int)` | 前进 N 步 |
| `limit(Iterator, int)` / `cycle(Iterable)` | 限制/循环 |

### 3.33 SetView

> 惰性集合运算的抽象视图，实际计算延迟到迭代时。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `copyInto(Set)` | 复制到可变 Set |
| `toSet()` | 转为普通 HashSet |

### 3.34 SetAlgebra

> 集合代数操作接口，提供并集、交集、差集、对称差、子集判断等。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `union(Set)` | 并集 |
| `intersection(Set)` | 交集 |
| `difference(Set)` | 差集 |
| `symmetricDifference(Set)` | 对称差集 |
| `isSubsetOf(Set)` / `isSupersetOf(Set)` | 子集/超集判断 |
| `isProperSubsetOf(Set)` / `isProperSupersetOf(Set)` | 真子集/真超集判断 |
| `isDisjoint(Set)` | 不相交判断 |
| `filter(Predicate)` | 过滤 |

**示例：**

```java
SetAlgebra<String> algebra = SetAlgebra.of(Set.of("a", "b", "c"));
Set<String> union = algebra.union(Set.of("b", "c", "d")); // [a, b, c, d]
boolean isSub = algebra.isSubsetOf(Set.of("a", "b", "c", "d")); // true
```

### 3.35 CollectionFactory

> 集合工厂，提供各种 JDK 集合类型的便捷创建方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newArrayList()` / `newLinkedList()` / `newCopyOnWriteArrayList()` | List 创建 |
| `newHashSet()` / `newLinkedHashSet()` / `newTreeSet()` / `newEnumSet(E, E...)` | Set 创建 |
| `newConcurrentHashSet()` / `newCopyOnWriteArraySet()` | 并发 Set 创建 |
| `newHashMap()` / `newLinkedHashMap()` / `newTreeMap()` / `newEnumMap(Class)` | Map 创建 |
| `newConcurrentHashMap()` / `newConcurrentSkipListMap()` | 并发 Map 创建 |
| `newIdentityHashMap()` / `newWeakHashMap()` | 特殊 Map 创建 |
| `newArrayDeque()` / `newPriorityQueue()` | Queue 创建 |
| `newLinkedBlockingQueue()` / `newArrayBlockingQueue(int)` | 阻塞队列创建 |
| `newConcurrentLinkedQueue()` / `newConcurrentLinkedDeque()` | 并发队列创建 |

### 3.36 MutableGraph

> 可变图实现，支持有向/无向图，可选自环。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `directed()` / `undirected()` | 创建有向/无向图 |
| `directedAllowingSelfLoops()` / `undirectedAllowingSelfLoops()` | 允许自环 |
| `addNode(N)` | 添加节点 |
| `putEdge(N, N)` | 添加边 |
| `removeNode(N)` / `removeEdge(N, N)` | 移除 |
| `nodes()` / `edges()` | 所有节点/边 |
| `successors(N)` / `predecessors(N)` / `adjacentNodes(N)` | 邻居 |
| `degree(N)` / `inDegree(N)` / `outDegree(N)` | 度数 |
| `hasEdge(N, N)` / `hasNode(N)` | 存在性 |

**示例：**

```java
MutableGraph<String> graph = MutableGraph.directed();
graph.addNode("A");
graph.putEdge("A", "B");
graph.putEdge("B", "C");
Set<String> neighbors = graph.successors("A"); // [B]
```

### 3.37 GraphTraversalUtil

> 图遍历工具类，提供 BFS、DFS、拓扑排序、最短路径等算法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `bfs(Graph, N)` | 广度优先遍历 |
| `bfs(Graph, N, Consumer)` | BFS 带访问回调 |
| `bfsUntil(Graph, N, Predicate)` | BFS 直到满足条件 |
| `dfs(Graph, N)` / `dfsIterative(Graph, N)` | 深度优先遍历 |
| `topologicalSort(Graph)` | 拓扑排序 |
| `topologicalSortKahn(Graph)` | Kahn 算法拓扑排序 |
| `hasCycle(Graph)` | 检测是否有环 |
| `shortestPath(Graph, N, N)` | 最短路径 |
| `hasPath(Graph, N, N)` | 是否可达 |
| `allPaths(Graph, N, N, int)` | 所有路径（限深度） |
| `connectedComponents(Graph)` | 连通分量 |
| `stronglyConnectedComponents(Graph)` | 强连通分量 |
| `reachableFrom(Graph, N)` | 从节点可达的所有节点 |
| `isConnected(Graph)` | 是否连通 |
| `isDag(Graph)` | 是否为 DAG |

**示例：**

```java
List<String> bfsOrder = GraphTraversalUtil.bfs(graph, "A");
List<String> topoOrder = GraphTraversalUtil.topologicalSort(graph);
Optional<List<String>> path = GraphTraversalUtil.shortestPath(graph, "A", "C");
```

### 3.38 ConcurrentCollectionFactory

> 线程安全集合工厂，提供并发 Map、Set、Queue 等的创建。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `newConcurrentMap()` / `newConcurrentMap(int)` | 并发 Map |
| `newConcurrentSortedMap()` | 并发有序 Map |
| `newConcurrentSet()` / `newConcurrentSortedSet()` | 并发 Set |
| `newConcurrentQueue()` / `newConcurrentDeque()` | 并发队列 |
| `newBlockingQueue(int)` / `newLinkedBlockingQueue()` | 阻塞队列 |
| `newDelayQueue()` / `newSynchronousQueue()` | 特殊队列 |
| `newCopyOnWriteList()` / `newCopyOnWriteSet()` | CopyOnWrite 集合 |
| `synchronizedMap(Map)` / `synchronizedSet(Set)` / `synchronizedList(List)` | 同步包装 |

### 3.39 LockFreeQueue

> 无锁队列实现，基于 CAS 操作的高性能并发队列。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `create()` | 创建空队列 |
| `create(Collection)` | 从集合创建 |
| `offer(E)` | 入队 |
| `poll()` | 出队 |
| `peek()` | 查看队首 |
| `size()` / `isEmpty()` | 查询 |

**示例：**

```java
LockFreeQueue<String> queue = LockFreeQueue.create();
queue.offer("item1");
queue.offer("item2");
String item = queue.poll(); // "item1"
```

### 3.40 原生类型集合 (primitive)

> 避免自动装箱的原生类型集合，包括 IntList、LongList、DoubleList、IntSet、LongSet、IntIntMap、IntObjectMap。

**IntList 主要方法：**

| 方法 | 描述 |
|------|------|
| `create()` / `create(int)` / `of(int...)` / `range(int, int)` | 创建 |
| `get(int)` / `set(int, int)` | 按索引访问 |
| `add(int)` / `addAll(int...)` | 添加 |
| `removeAt(int)` / `remove(int)` | 移除 |
| `contains(int)` / `indexOf(int)` | 查找 |
| `sum()` / `min()` / `max()` / `average()` | 统计 |
| `sort()` / `reverse()` | 排序/反转 |
| `toArray()` / `stream()` | 转换 |

**IntIntMap 主要方法：**

| 方法 | 描述 |
|------|------|
| `create()` / `create(int)` | 创建 |
| `put(int, int)` / `get(int)` / `getOrDefault(int, int)` | 存取 |
| `remove(int)` / `containsKey(int)` / `containsValue(int)` | 操作 |
| `keySet()` / `valuesToArray()` | 视图 |
| `forEach(IntIntConsumer)` | 遍历 |

**DoubleList 额外统计方法：**

| 方法 | 描述 |
|------|------|
| `variance()` | 方差 |
| `standardDeviation()` | 标准差 |

**示例：**

```java
IntList list = IntList.of(1, 2, 3, 4, 5);
long sum = list.sum();       // 15
int max = list.max();        // 5
double avg = list.average(); // 3.0

IntIntMap map = IntIntMap.create();
map.put(1, 100);
map.put(2, 200);
int value = map.get(1); // 100
```

### 3.41 MapDifference / ValueDifference

> Map 比较结果接口，表示两个 Map 之间的差异。

**MapDifference 主要方法：**

| 方法 | 描述 |
|------|------|
| `areEqual()` | 两个 Map 是否相等 |
| `entriesOnlyOnLeft()` | 仅在左侧存在的条目 |
| `entriesOnlyOnRight()` | 仅在右侧存在的条目 |
| `entriesInCommon()` | 共同的条目 |
| `entriesDiffering()` | 值不同的条目 |

**示例：**

```java
MapDifference<String, Integer> diff = MapUtil.difference(map1, map2);
Map<String, Integer> onlyLeft = diff.entriesOnlyOnLeft();
Map<String, Integer> common = diff.entriesInCommon();
```

### 3.42 OpenCollectionException

> 集合操作异常类，提供多种工厂方法创建语义化异常。

**工厂方法：**

| 方法 | 描述 |
|------|------|
| `emptyCollection(String)` | 集合为空异常 |
| `indexOutOfBounds(int, int)` | 索引越界异常 |
| `duplicateKey(Object)` / `duplicateValue(Object)` | 重复键/值异常 |
| `nullElement()` / `nullKey()` / `nullValue()` | null 异常 |
| `immutableCollection()` | 不可变集合修改异常 |
| `elementNotFound(Object)` / `keyNotFound(Object)` | 未找到异常 |
| `illegalCapacity(int)` / `negativeSize(int)` | 非法参数异常 |
| `unsupportedOperation(String)` | 不支持的操作异常 |

### 3.43 EntryTransformer

> Map 条目转换函数接口，用于 `MapUtil.transformEntries()` 等方法。

### 3.44 PeekingIterator

> 可查看下一个元素的迭代器接口，通过 `IteratorUtil.peekingIterator()` 创建。

### 3.45 UnmodifiableIterator

> 不支持 `remove()` 操作的迭代器抽象类。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `of(Iterator)` | 从已有迭代器创建不可修改迭代器 |

# OpenCode Base Collections

Modern collection utilities providing specialized collections (BiMap, Multiset, Multimap, Table), immutable and persistent collections, primitive collections, concurrent collections, graph structures (including weighted graphs with Dijkstra), generic tuples, decorator base classes, and tree data structures for JDK 25+.

> 153 public classes | @author Leon Soo | @since JDK 25

## Features

- Specialized collections: BiMap, Multiset, Multimap, Table, LinkedHashMultimap
- Generic tuples: Pair (implements Map.Entry), Triple
- Immutable collections: ImmutableList, ImmutableSet, ImmutableMap, ImmutableRangeSet, ImmutableRangeMap
- Persistent collections: PersistentList (cons-cell), PersistentMap (HAMT), PersistentSet with structural sharing
- Primitive collections: IntList, LongList, DoubleList, FloatList, IntSet, LongSet, DoubleSet, IntIntMap, LongLongMap, ObjectIntMap, ObjectLongMap, ObjectDoubleMap, etc.
- Concurrent collections: LockFreeQueue, LockFreeStack (Treiber CAS), ConcurrentHashMultiset
- Graph data structures with traversal algorithms
- Weighted graphs: ValueGraph / MutableValueGraph with Dijkstra shortest path
- Tree structures: Trie, SkipList, TreeTraversal
- Collection utilities: ListUtil, MapUtil, SetUtil, OpenCollection, ComparisonChain
- Stream enhancements: OpenCollectors, OpenGatherers (zipWithIndex, takeWhileInclusive, interleave), Streams
- Set algebra operations (union, intersection, difference)
- Fluent iteration: FluentIterable, PeekingIterator, AbstractIterator
- Decorator base classes: ForwardingCollection, ForwardingList, ForwardingMap, ForwardingSet
- Range types: Range, RangeSet, RangeMap, IntInterval
- Collection transformation and grouping utilities
- Ordering and equivalence utilities

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-collections</artifactId>
    <version>1.0.3</version>
</dependency>
```

## API Overview

### Core Utilities

| Class | Description |
|-------|-------------|
| `Pair` | Generic pair (2-tuple), implements `Map.Entry` |
| `Triple` | Generic triple (3-tuple) |
| `ComparisonChain` | Fluent comparator chain for multi-field `compareTo()` with short-circuit |
| `AbstractIterator` | Skeletal Iterator — just implement `computeNext()` |
| `IntInterval` | Zero-allocation integer range (like Python's `range()`) |
| `OpenCollection` | General collection utility (null-safe checks, set operations, filtering) |
| `OpenList` | List creation and manipulation utilities |
| `OpenMap` | Map creation and manipulation utilities |
| `OpenSet` | Set creation and manipulation utilities |
| `ListUtil` | List utilities (partition, zip, distinct, flatten) |
| `MapUtil` | Map utilities (merge, invert, filter, transform) |
| `SetUtil` | Set utilities (union, intersection, difference) |
| `CollectionFactory` | Factory for creating collections with sizing hints |
| `ComparatorUtil` | Comparator building utilities |

### Decorator Base Classes

| Class | Description |
|-------|-------------|
| `ForwardingCollection` | Abstract decorator for `Collection` — override `delegate()` + one method |
| `ForwardingList` | Abstract decorator for `List` |
| `ForwardingSet` | Abstract decorator for `Set` |
| `ForwardingMap` | Abstract decorator for `Map` |

### Specialized Collections

| Class | Description |
|-------|-------------|
| `BiMap` | Bidirectional map interface (key-to-value and value-to-key) |
| `HashBiMap` | Hash-based BiMap implementation |
| `Multiset` | Collection allowing duplicate elements with count tracking |
| `HashMultiset` | Hash-based Multiset implementation |
| `Multimap` | Map with multiple values per key |
| `AbstractMultimap` | Abstract base for Multimap implementations |
| `ArrayListMultimap` | ArrayList-backed Multimap |
| `HashSetMultimap` | HashSet-backed Multimap |
| `Table` | Two-dimensional map (row, column, value) |
| `HashBasedTable` | Hash-based Table implementation |
| `MapDifference` | Represents the difference between two maps |
| `ValueDifference` | Represents a value difference within MapDifference |
| `SetView` | Unmodifiable view of set operations |
| `SetAlgebra` | Set algebra operations interface |

### Specialized Sub-package

| Class | Description |
|-------|-------------|
| `LinkedHashMultimap` | Insertion-ordered Multimap (preserves key and value order) |
| `ListMultimap` | Multimap with List values |
| `SetMultimap` | Multimap with Set values |
| `SortedSetMultimap` | Multimap with SortedSet values |
| `TreeSetMultimap` | TreeSet-backed SortedSetMultimap |
| `MultimapBuilder` | Builder for creating Multimap instances |
| `ClassToInstanceMap` | Type-safe map from Class to instance |
| `MutableClassToInstanceMap` | Mutable ClassToInstanceMap |
| `NavigableMultiset` | Multiset with navigation methods |
| `AbstractMultiset` | Abstract base for Multiset implementations |
| `LinkedHashMultiset` | Insertion-ordered Multiset |
| `ConcurrentHashMultiset` | Thread-safe Multiset |
| `TreeMultiset` | Sorted Multiset |
| `RangeSet` | Set of non-overlapping ranges |
| `TreeRangeSet` | Tree-based RangeSet implementation |
| `RangeMap` | Map from ranges to values |
| `TreeRangeMap` | Tree-based RangeMap implementation |
| `ArrayTable` | Fixed-size table backed by a 2D array |
| `TreeBasedTable` | Sorted row/column Table |
| `EvictingQueue` | Fixed-capacity queue that evicts oldest elements |
| `MinMaxPriorityQueue` | Double-ended priority queue |
| `Interner` | Object interning (canonical instance) utility |

### Immutable Collections

| Class | Description |
|-------|-------------|
| `ImmutableList` | Immutable list |
| `ImmutableSet` | Immutable set |
| `ImmutableMap` | Immutable map |
| `ImmutableBiMap` | Immutable bidirectional map |
| `ImmutableMultimap` | Immutable multimap |
| `ImmutableListMultimap` | Immutable list-backed multimap |
| `ImmutableSetMultimap` | Immutable set-backed multimap |
| `ImmutableMultiset` | Immutable multiset |
| `ImmutableSortedMap` | Immutable sorted map |
| `ImmutableSortedSet` | Immutable sorted set |
| `ImmutableTable` | Immutable table |
| `ImmutableClassToInstanceMap` | Immutable type-safe instance map |
| `ImmutableRangeSet` | Immutable range set with complement, union, intersection |
| `ImmutableRangeMap` | Immutable range map |
| `ImmutableCollectionUtil` | Utilities for creating immutable collections |

### Persistent Collections

| Class | Description |
|-------|-------------|
| `PersistentList` | Cons-cell persistent linked list with structural sharing |
| `PersistentMap` | HAMT persistent map with structural sharing |
| `PersistentSet` | HAMT persistent set with structural sharing (wraps PersistentMap) |

### Primitive Collections

| Class | Description |
|-------|-------------|
| `IntList` | Primitive int array list |
| `LongList` | Primitive long array list |
| `DoubleList` | Primitive double array list |
| `FloatList` | Primitive float array list |
| `IntSet` | Primitive int set |
| `LongSet` | Primitive long set |
| `DoubleSet` | Primitive double set |
| `IntIntMap` | Primitive int-to-int map |
| `IntObjectMap` | Primitive int-to-Object map |
| `LongObjectMap` | Primitive long-to-Object map |
| `LongLongMap` | Primitive long-to-long map |
| `ObjectIntMap` | Object-to-primitive int map (no value boxing) |
| `ObjectLongMap` | Object-to-primitive long map (no value boxing) |
| `ObjectDoubleMap` | Object-to-primitive double map (no value boxing) |

### Concurrent

| Class | Description |
|-------|-------------|
| `ConcurrentCollectionFactory` | Factory for concurrent collection instances |
| `LockFreeQueue` | Lock-free concurrent queue |
| `LockFreeStack` | Treiber CAS lock-free concurrent stack |

### Graph

| Class | Description |
|-------|-------------|
| `Graph` | Graph interface (nodes and edges) |
| `MutableGraph` | Mutable graph with add/remove operations |
| `ValueGraph` | Weighted graph interface (nodes, edges with values) |
| `MutableValueGraph` | Mutable weighted graph with add/remove and edge values |
| `GraphTraversalUtil` | Graph traversal algorithms (BFS, DFS, topological sort, Dijkstra) |

### Tree

| Class | Description |
|-------|-------------|
| `Trie` | Prefix tree (trie) for string lookups |
| `SkipList` | Probabilistic skip list |
| `TreeTraversalUtil` | Tree traversal utilities |

### Stream & Iteration

| Class | Description |
|-------|-------------|
| `OpenCollectors` | Custom Stream collectors |
| `OpenGatherers` | Custom Stream gatherers (zipWithIndex, takeWhileInclusive, interleave) |
| `Streams` | Stream creation and transformation utilities |
| `FluentIterable` | Fluent iterable with chained operations |
| `PeekingIterator` | Iterator with peek-ahead capability |
| `UnmodifiableIterator` | Unmodifiable iterator base class |

### Transform

| Class | Description |
|-------|-------------|
| `CollectorUtil` | Collector utilities |
| `GroupingUtil` | Grouping and classification utilities |
| `MoreCollectorUtil` | Additional collector utilities |
| `PartitionUtil` | Collection partitioning utilities |

### Other

| Class | Description |
|-------|-------------|
| `Ordering` | Comparator builder with chaining and null handling |
| `Equivalence` | Custom equality strategy |
| `EntryTransformer` | Map entry transformation interface |
| `Range` | Immutable range with open/closed endpoints |
| `IterableUtil` | Iterable utilities |
| `IteratorUtil` | Iterator utilities |
| `OpenCollectionException` | Collection operation exception |

## Quick Start

```java
import cloud.opencode.base.collections.*;

// BiMap - bidirectional map
BiMap<String, Integer> biMap = HashBiMap.create();
biMap.put("one", 1);
String key = biMap.inverse().get(1);  // "one"

// Multimap - multiple values per key
Multimap<String, String> multimap = ArrayListMultimap.create();
multimap.put("fruits", "apple");
multimap.put("fruits", "banana");
Collection<String> fruits = multimap.get("fruits");  // [apple, banana]

// Table - two-dimensional map
Table<String, String, Integer> table = HashBasedTable.create();
table.put("Alice", "Math", 95);
table.put("Alice", "English", 88);
int score = table.get("Alice", "Math");  // 95

// Immutable collections
ImmutableList<String> list = ImmutableList.of("a", "b", "c");
ImmutableMap<String, Integer> map = ImmutableMap.of("x", 1, "y", 2);

// Primitive collections (no boxing)
IntList intList = IntList.of(1, 2, 3, 4, 5);
int sum = intList.sum();

// FluentIterable
List<String> result = FluentIterable.from(items)
    .filter(item -> item.isActive())
    .transform(Item::getName)
    .toList();

// Set operations
Set<String> union = SetUtil.union(setA, setB);
Set<String> intersection = SetUtil.intersection(setA, setB);
```

### ComparisonChain

```java
import cloud.opencode.base.collections.ComparisonChain;

// Inside compareTo() — short-circuits on first non-zero
public int compareTo(Person other) {
    return ComparisonChain.start()
        .compare(this.lastName, other.lastName)
        .compare(this.firstName, other.firstName)
        .compare(this.age, other.age)
        .result();
}
```

### AbstractIterator

```java
import cloud.opencode.base.collections.AbstractIterator;

// Custom iterator — just implement computeNext()
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

### IntInterval

```java
import cloud.opencode.base.collections.IntInterval;

IntInterval range = IntInterval.fromTo(1, 10);     // [1,2,3,...,10]
IntInterval evens = IntInterval.fromToBy(0, 20, 2); // [0,2,4,...,20]
IntInterval desc  = IntInterval.fromTo(10, 1);      // [10,9,8,...,1]

boolean has = range.contains(5);    // true, O(1)
int val = range.get(3);             // 4, O(1)
int[] arr = range.toArray();        // primitive int[]
range.stream().sum();               // IntStream
```

### ObjectIntMap / ObjectLongMap / ObjectDoubleMap

```java
import cloud.opencode.base.collections.primitive.ObjectIntMap;

// Count word frequencies — no boxing overhead
ObjectIntMap<String> wordCount = ObjectIntMap.create();
for (String word : words) {
    wordCount.addTo(word, 1);
}
int count = wordCount.getOrDefault("hello", 0);
```

### ForwardingCollection

```java
import cloud.opencode.base.collections.ForwardingList;

// Custom list that logs all additions
class LoggingList<E> extends ForwardingList<E> {
    private final List<E> delegate = new ArrayList<>();
    protected List<E> delegate() { return delegate; }

    @Override
    public boolean add(E element) {
        System.out.println("Adding: " + element);
        return super.add(element);
    }
}
```

### Persistent Collections (Structural Sharing)

```java
import cloud.opencode.base.collections.immutable.*;

// PersistentSet — HAMT with structural sharing
PersistentSet<String> set = PersistentSet.of("a", "b", "c");
PersistentSet<String> set2 = set.add("d");
// set is still {a, b, c} — structural sharing

// PersistentMap — HAMT map
PersistentMap<String, Integer> map = PersistentMap.<String, Integer>empty()
    .put("a", 1).put("b", 2);
PersistentMap<String, Integer> map2 = map.put("c", 3);
// map is still {a=1, b=2} — structural sharing
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

### LinkedHashMultimap

```java
import cloud.opencode.base.collections.specialized.LinkedHashMultimap;

// Insertion-ordered multimap
LinkedHashMultimap<String, String> mm = LinkedHashMultimap.create();
mm.put("fruits", "apple");
mm.put("vegs", "carrot");
mm.put("fruits", "banana");
// keySet iteration: fruits, vegs (insertion order)
// get("fruits"): {apple, banana} (insertion order, no duplicates)
```

### Gatherers (zipWithIndex, takeWhileInclusive, interleave)

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

### ValueGraph + Dijkstra Shortest Path

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

### LockFreeStack

```java
import cloud.opencode.base.collections.concurrent.LockFreeStack;

LockFreeStack<String> stack = new LockFreeStack<>();
stack.push("first");
stack.push("second");
String top = stack.pop(); // "second"
```

### Primitive Collections (DoubleSet, LongLongMap, FloatList)

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

## Requirements

- Java 25+

## License

Apache License 2.0

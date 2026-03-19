# OpenCode Base Collections

Modern collection utilities providing specialized collections (BiMap, Multiset, Multimap, Table), immutable collections, primitive collections, concurrent collections, graph structures, and tree data structures for JDK 25+.

## Features

- Specialized collections: BiMap, Multiset, Multimap, Table
- Immutable collections: ImmutableList, ImmutableSet, ImmutableMap, and more
- Primitive collections: IntList, LongList, DoubleList, IntSet, LongSet, IntIntMap, etc.
- Concurrent collections: LockFreeQueue, ConcurrentHashMultiset
- Graph data structures with traversal algorithms
- Tree structures: Trie, SkipList, TreeTraversal
- Collection utilities: ListUtil, MapUtil, SetUtil, OpenCollection
- Stream enhancements: OpenCollectors, OpenGatherers, Streams
- Set algebra operations (union, intersection, difference)
- Fluent iteration: FluentIterable, PeekingIterator
- Range types: Range, RangeSet, RangeMap
- Collection transformation and grouping utilities
- Ordering and equivalence utilities

## Maven

```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-collections</artifactId>
    <version>1.0.0</version>
</dependency>
```

## API Overview

### Core Utilities

| Class | Description |
|-------|-------------|
| `OpenCollection` | General collection utility (null-safe checks, set operations, filtering) |
| `OpenList` | List creation and manipulation utilities |
| `OpenMap` | Map creation and manipulation utilities |
| `OpenSet` | Set creation and manipulation utilities |
| `ListUtil` | List utilities (partition, zip, distinct, flatten) |
| `MapUtil` | Map utilities (merge, invert, filter, transform) |
| `SetUtil` | Set utilities (union, intersection, difference) |
| `CollectionFactory` | Factory for creating collections with sizing hints |
| `ComparatorUtil` | Comparator building utilities |

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
| `ImmutableCollectionUtil` | Utilities for creating immutable collections |

### Primitive Collections

| Class | Description |
|-------|-------------|
| `IntList` | Primitive int array list |
| `LongList` | Primitive long array list |
| `DoubleList` | Primitive double array list |
| `IntSet` | Primitive int set |
| `LongSet` | Primitive long set |
| `IntIntMap` | Primitive int-to-int map |
| `IntObjectMap` | Primitive int-to-Object map |
| `LongObjectMap` | Primitive long-to-Object map |

### Concurrent

| Class | Description |
|-------|-------------|
| `ConcurrentCollectionFactory` | Factory for concurrent collection instances |
| `LockFreeQueue` | Lock-free concurrent queue |

### Graph

| Class | Description |
|-------|-------------|
| `Graph` | Graph interface (nodes and edges) |
| `MutableGraph` | Mutable graph with add/remove operations |
| `GraphTraversalUtil` | Graph traversal algorithms (BFS, DFS, topological sort) |

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
| `OpenGatherers` | Custom Stream gatherers (JDK 25) |
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

## Requirements

- Java 25+

## License

Apache License 2.0

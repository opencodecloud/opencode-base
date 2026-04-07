# OpenCode Base Tree

**Tree data structure utilities for Java 25+**

`opencode-base-tree` provides comprehensive tree data structure support including tree building from flat lists, multiple traversal algorithms, balanced trees (AVL, Red-Black), diff comparison, merge, serialization, virtual trees with lazy loading, LCA, and more.

## Features

### Core Features
- **Tree Building**: Convert flat lists to tree hierarchies with ID/parentID mapping
- **Multiple Traversal**: Pre-order, post-order, breadth-first, depth-limited, level-order
- **Search & Filter**: Find by ID, find by predicate, path finding, leaf extraction
- **Tree Operations**: Flatten, sort, depth calculation, node counting, filtering with ancestor preservation

### Advanced Features
- **Balanced Trees**: AVL tree and Red-Black tree implementations
- **Virtual Tree**: Lazy child loading with on-demand expansion and preloading
- **Tree Diff**: Compare two trees and produce diff results (added/removed/modified)
- **Tree Merge**: Merge two forests with configurable conflict resolution
- **Serialization**: Serialize trees to JSON, XML, and Map representations
- **Concurrent Building**: Thread-safe tree construction
- **Path Finding**: Root-to-node path extraction, lowest common ancestor (LCA)
- **Tree Statistics**: Single-pass comprehensive metrics (depth, width, branching factor)
- **Cycle Detection**: Detect cycles in tree structures

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-tree</artifactId>
    <version>1.0.3</version>
</dependency>
```

### Implement Treeable Interface

```java
import cloud.opencode.base.tree.Treeable;
import java.util.List;

public class Menu implements Treeable<Menu, Long> {
    private Long id;
    private Long parentId;
    private String name;
    private List<Menu> children;

    @Override public Long getId() { return id; }
    @Override public Long getParentId() { return parentId; }
    @Override public List<Menu> getChildren() { return children; }
    @Override public void setChildren(List<Menu> children) { this.children = children; }
    public String getName() { return name; }
    // ... constructors, setters ...
}
```

### Build Tree from Flat List

```java
import cloud.opencode.base.tree.OpenTree;

// Automatic root detection (parentId=null/0/"" treated as root)
List<Menu> flatList = menuService.findAll();
List<Menu> tree = OpenTree.buildTree(flatList);

// Explicit root ID
List<Menu> tree = OpenTree.buildTree(flatList, 0L);

// Build sorted tree
List<Menu> tree = OpenTree.buildTreeSorted(flatList, 0L,
    Comparator.comparing(Menu::getSort));
```

### Traversal

```java
// Pre-order (parent before children)
OpenTree.traversePreOrder(tree, node -> System.out.println(node.getName()));

// Post-order (children before parent)
OpenTree.traversePostOrder(tree, node -> cleanup(node));

// Breadth-first (level by level)
OpenTree.traverseBreadthFirst(tree, node -> process(node));

// With depth information
OpenTree.traverseWithDepth(tree, (node, depth) -> indent(depth, node));
```

### Search

```java
// Find by ID
Menu found = OpenTree.find(tree, menuId);

// Find all matching nodes
List<Menu> matches = OpenTree.findAll(tree, m -> m.isEnabled());

// Get leaf nodes
List<Menu> leaves = OpenTree.getLeaves(tree);

// Get path from root to node
List<Menu> path = OpenTree.getPath(tree, targetId);
```

### Filter

```java
// Filter keeping ancestors (matching nodes + their ancestor chain)
List<Menu> filtered = OpenTree.filter(tree, m -> m.isVisible());

// Flatten tree to list
List<Menu> flat = OpenTree.flattenTree(tree);
```

### Merge (V1.0.3)

```java
import cloud.opencode.base.tree.operation.TreeMerger;

// Merge two forests — keep left node on ID conflict
List<Menu> merged = OpenTree.mergeKeepLeft(tree1, tree2);

// Keep right node on conflict
List<Menu> merged = OpenTree.mergeKeepRight(tree1, tree2);

// Custom conflict resolution
List<Menu> merged = OpenTree.merge(tree1, tree2, (left, right) -> {
    left.setName(right.getName()); // take name from right
    return left;
});
```

### Sort (V1.0.3)

```java
// Recursive sort at every level
OpenTree.sort(tree, Comparator.comparing(Menu::getSort));

// Sort by extracted key
OpenTree.sortBy(tree, Menu::getName);

// Check if tree is sorted at all levels
boolean sorted = OpenTree.isSorted(tree, Comparator.comparing(Menu::getSort));
```

### Statistics (V1.0.3)

```java
import cloud.opencode.base.tree.operation.TreeStatistics;

TreeStatistics stats = OpenTree.statistics(tree);
stats.nodeCount();          // total nodes
stats.leafCount();          // leaf nodes
stats.maxDepth();           // max tree depth
stats.maxWidth();           // widest level
stats.avgBranchingFactor(); // avg children per internal node
stats.widthByLevel();       // Map<Integer, Integer>
stats.leafRatio();          // leafCount / nodeCount
stats.summary();            // human-readable summary
```

### Lowest Common Ancestor (V1.0.3)

```java
import cloud.opencode.base.tree.path.PathFinder;

// Find LCA by ID
Optional<Menu> lca = OpenTree.findLCA(tree, id1, id2);

// Find LCA by predicate
Optional<Menu> lca = PathFinder.findLowestCommonAncestor(tree,
    m -> "Finance".equals(m.getName()),
    m -> "HR".equals(m.getName()));
```

### Subtree & Siblings (V1.0.3)

```java
// Extract subtree rooted at node
Optional<Menu> subtree = OpenTree.extractSubtree(tree, nodeId);

// Get siblings (same-parent nodes, excluding self)
List<Menu> siblings = OpenTree.getSiblings(tree, nodeId);
```

### Serialization

```java
String json = OpenTree.toJson(tree);
String xml = OpenTree.toXml(tree);
List<Map<String, Object>> maps = OpenTree.toMaps(tree);
List<Map<String, Object>> flatMaps = OpenTree.toFlatMaps(tree);
```

### Virtual Tree (Lazy Loading)

```java
import cloud.opencode.base.tree.virtual.VirtualTree;

// Create root with lazy child loader
VirtualTree<Dept, Long> vTree = OpenTree.virtualTree(
    1L, rootDept, id -> deptService.findChildren(id));

// Children loaded on first access
List<VirtualTree<Dept, Long>> children = vTree.getChildren();

// Preload to depth 3
OpenTree.preloadVirtualTree(vTree, 3);
```

### TreeNode (Generic Tree)

```java
import cloud.opencode.base.tree.TreeNode;

// Build tree with generic TreeNode
TreeNode<String> root = OpenTree.node("Root");
root.addChild("Child1");
root.addChild("Child2");

// Build from flat list with extractors
List<TreeNode<Item>> tree = OpenTree.build(items, Item::getId, Item::getParentId);

// Print
OpenTree.printToConsole(root);
```

### Balanced Trees

```java
import cloud.opencode.base.tree.balanced.*;

// AVL Tree (self-balancing, O(log n) operations)
AvlTree<Integer> avl = new AvlTree<>();
avl.insert(5); avl.insert(3); avl.insert(7);
boolean found = avl.contains(3); // true
avl.delete(3);

// Red-Black Tree
RedBlackTree<String> rbt = new RedBlackTree<>();
rbt.insert("B"); rbt.insert("A"); rbt.insert("C");

// Create from collection
AvlTree<Integer> avl = BalancedTreeUtil.avlTreeFrom(List.of(5, 3, 7, 1, 9));
```

## Class Reference

### Root Package (`cloud.opencode.base.tree`)
| Class | Description |
|-------|-------------|
| `OpenTree` | Main facade — building, traversal, search, filter, merge, sort, LCA, statistics, serialization, virtual trees |
| `TreeNode<T>` | Generic tree node with data and children list |
| `TreeBuilder` | Builds tree from flat collections using ID/parentID mapping |
| `Treeable<T, ID>` | Interface for tree-capable entities (getId, getParentId, getChildren, setChildren) |
| `DefaultTreeNode<ID>` | Default Treeable implementation with id, parentId, name, extra fields |
| `LightTreeNode<ID>` | Lightweight immutable tree node (record) |
| `TreePrinter` | Pretty-print tree structures to string |
| `TreeTraverser` | Configurable tree traversal engine with streams, iterators, reduction |

### Balanced Trees (`tree.balanced`)
| Class | Description |
|-------|-------------|
| `AvlTree<T>` | Self-balancing AVL tree with insert, delete, search |
| `RedBlackTree<T>` | Red-Black tree with guaranteed O(log n) operations |
| `BalancedTreeUtil` | Factory methods for balanced trees |

### Builder (`tree.builder`)
| Class | Description |
|-------|-------------|
| `ConcurrentTreeBuilder` | Thread-safe tree building for concurrent scenarios |
| `ListToTreeConverter` | Generic flat list to tree converter with duplicate rejection |

### Diff (`tree.diff`)
| Class | Description |
|-------|-------------|
| `TreeDiff` | Compare two trees and produce diff results |
| `TreeDiffResult<T>` | Result: added, removed, modified, unchanged nodes |

### Exception (`tree.exception`)
| Class | Description |
|-------|-------------|
| `TreeException` | Base exception (extends `OpenException`, component="Tree") |
| `CycleDetectedException` | Thrown when a cycle is detected, with cycle path |
| `TreeErrorCode` | Error codes: BUILD_FAILED, CYCLE_DETECTED, MAX_DEPTH_EXCEEDED, etc. |

### Operation (`tree.operation`)
| Class | Description |
|-------|-------------|
| `TreeFilter` | Filter with ancestor preservation, flat filter, depth filter |
| `TreeMapper` | Map tree nodes to different types, extract values |
| `TreeMerger` | Merge two forests with conflict resolution (keepLeft/keepRight/custom) |
| `TreeSorter` | Recursive sort at every level, isSorted check |
| `TreeStatistics` | Comprehensive metrics in a single BFS pass (record) |
| `TreeUtil` | Find, flatten, count, getLeaves, extractSubtree, getSiblings |

### Path (`tree.path`)
| Class | Description |
|-------|-------------|
| `PathFinder` | Find paths to nodes, LCA (lowest common ancestor) |
| `TreePath<T>` | Immutable path from root to target node |

### Result (`tree.result`)
| Class | Description |
|-------|-------------|
| `TreeResult<T>` | Sealed result type: Success, Failure, Empty, Validation |

### Serialization (`tree.serialization`)
| Class | Description |
|-------|-------------|
| `TreeSerializer` | Serialize to JSON, XML, Map, flat Map (with config) |

### Traversal (`tree.traversal`)
| Class | Description |
|-------|-------------|
| `TreeTraversal` | Base traversal interface |
| `PreOrderTraversal` | Pre-order (root-first) singleton |
| `PostOrderTraversal` | Post-order (children-first) singleton |
| `LevelOrderTraversal` | Breadth-first singleton |
| `DepthLimitedTraversal` | With maximum depth |
| `IterativeTraversal` | Stack-based non-recursive |
| `TreeVisitor` | Visitor interface with factory methods |

### Validation (`tree.validation`)
| Class | Description |
|-------|-------------|
| `CycleDetector` | Cycle detection with path extraction (O(n)) |
| `TreeNodeValidator` | Null ID, duplicate ID, structure validation |

### Virtual (`tree.virtual`)
| Class | Description |
|-------|-------------|
| `VirtualTree<T, ID>` | Lazy-loading tree with LRU cache, thread-safe |
| `LazyChildLoader<T, ID>` | Functional interface for lazy child loading |

## Security

- All recursive operations have depth protection (max 1000) or use iterative algorithms
- XML serialization sanitizes element names against injection
- JSON output escapes all special characters
- No external dependencies — zero CVE surface

## Requirements

- Java 25+
- No external dependencies (only opencode-base-core)

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

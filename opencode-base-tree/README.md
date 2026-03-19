# OpenCode Base Tree

**Tree data structure utilities for Java 25+**

`opencode-base-tree` provides comprehensive tree data structure support including tree building from flat lists, multiple traversal algorithms, balanced trees (AVL, Red-Black), diff comparison, serialization, virtual trees with lazy loading, and more.

## Features

### Core Features
- **Tree Building**: Convert flat lists to tree hierarchies with ID/parentID mapping
- **Multiple Traversal**: Pre-order, post-order, breadth-first, depth-limited, level-order
- **Search & Filter**: Find by ID, find by predicate, path finding, leaf extraction
- **Tree Operations**: Flatten, sort, depth calculation, node counting, filtering with ancestor preservation

### Advanced Features
- **Balanced Trees**: AVL tree and Red-Black tree implementations
- **Virtual Tree**: Lazy child loading with on-demand expansion and preloading
- **Tree Diff**: Compare two trees and produce diff results
- **Serialization**: Serialize trees to JSON, XML, and Map representations
- **Concurrent Building**: Thread-safe tree construction
- **List-to-Tree Conversion**: Generic flat-to-tree converter with duplicate detection
- **Path Finding**: Root-to-node path extraction
- **Cycle Detection**: Detect cycles in tree structures

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-tree</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.tree.*;

// Build tree from flat list using Treeable interface
List<Menu> menus = menuService.findAll();
List<Menu> tree = OpenTree.buildTree(menus);

// Build tree with custom ID/parentID extractors
List<TreeNode<Dept>> tree = OpenTree.build(depts,
    Dept::getId, Dept::getParentId);

// Traversal
OpenTree.traversePreOrder(tree, node -> System.out.println(node.getName()));
OpenTree.traverseBreadthFirst(tree, node -> process(node));
OpenTree.traverseWithDepth(tree, (node, depth) -> indent(depth, node));

// Search
Menu found = OpenTree.find(tree, menuId);
List<Menu> matches = OpenTree.findAll(tree, m -> m.isEnabled());
List<Menu> leaves = OpenTree.getLeaves(tree);
List<Menu> path = OpenTree.getPath(tree, targetId);

// Operations
List<Menu> filtered = OpenTree.filter(tree, m -> m.isVisible());
List<Menu> flat = OpenTree.flattenTree(tree);
int depth = OpenTree.depth(tree);
int size = OpenTree.size(tree);

// Serialization
String json = OpenTree.toJson(tree);
String xml = OpenTree.toXml(tree);

// Virtual tree with lazy loading
VirtualTree<Dept, Long> vTree = OpenTree.virtualTree(
    1L, rootDept, id -> deptService.findChildren(id));
OpenTree.preloadVirtualTree(vTree, 3);

// Print tree
OpenTree.printToConsole(treeNode);
```

## Class Reference

### Root Package (`cloud.opencode.base.tree`)
| Class | Description |
|-------|-------------|
| `OpenTree` | Main facade: building, traversal, search, filter, serialization, virtual trees |
| `TreeNode<T>` | Generic tree node with data and children list |
| `TreeBuilder` | Builds tree from flat collections using ID/parentID mapping |
| `Treeable<T, ID>` | Interface for tree-capable entities with getId, getParentId, getChildren, setChildren |
| `DefaultTreeNode<T>` | Default Treeable implementation with id, parentId, name, extra fields |
| `LightTreeNode` | Lightweight tree node for minimal memory footprint |
| `TreePrinter` | Pretty-print tree structures to string |
| `TreeTraverser` | Configurable tree traversal engine |

### Balanced Trees (`tree.balanced`)
| Class | Description |
|-------|-------------|
| `AvlTree<T>` | Self-balancing AVL tree with insert, delete, search |
| `BalancedTreeUtil` | Utility methods for balanced tree operations |
| `RedBlackTree<T>` | Red-Black tree implementation with guaranteed O(log n) operations |

### Builder (`tree.builder`)
| Class | Description |
|-------|-------------|
| `ConcurrentTreeBuilder` | Thread-safe tree building for concurrent scenarios |
| `ListToTreeConverter` | Generic flat list to tree converter with duplicate rejection |

### Diff (`tree.diff`)
| Class | Description |
|-------|-------------|
| `TreeDiff` | Compare two trees and produce diff results |
| `TreeDiffResult` | Result of tree comparison with added, removed, modified nodes |

### Exception (`tree.exception`)
| Class | Description |
|-------|-------------|
| `CycleDetectedException` | Thrown when a cycle is detected in tree structure |
| `TreeErrorCode` | Error codes for tree exceptions |
| `TreeException` | Base exception for tree operations |

### Operation (`tree.operation`)
| Class | Description |
|-------|-------------|
| `TreeFilter` | Tree filtering with ancestor preservation |
| `TreeMapper` | Transform tree nodes with mapping functions |
| `TreeUtil` | General tree utility operations |

### Path (`tree.path`)
| Class | Description |
|-------|-------------|
| `PathFinder` | Find paths between nodes in a tree |
| `TreePath` | Represents a path from root to a target node |

### Result (`tree.result`)
| Class | Description |
|-------|-------------|
| `TreeResult` | Generic tree operation result wrapper |

### Serialization (`tree.serialization`)
| Class | Description |
|-------|-------------|
| `TreeSerializer` | Serialize trees to JSON, XML, Map, and flat Map representations |

### Traversal (`tree.traversal`)
| Class | Description |
|-------|-------------|
| `TreeTraversal` | Base traversal interface |
| `PreOrderTraversal` | Pre-order (root-first) depth traversal |
| `PostOrderTraversal` | Post-order (children-first) depth traversal |
| `LevelOrderTraversal` | Level-order (breadth-first) traversal |
| `DepthLimitedTraversal` | Depth-limited traversal with maximum depth |
| `IterativeTraversal` | Stack-based iterative traversal (no recursion) |
| `TreeVisitor` | Visitor interface for tree traversal |

### Validation (`tree.validation`)
| Class | Description |
|-------|-------------|
| `CycleDetector` | Detect cycles in tree/graph structures |
| `TreeNodeValidator` | Validate tree node constraints |

### Virtual (`tree.virtual`)
| Class | Description |
|-------|-------------|
| `VirtualTree<T, ID>` | Virtual tree with lazy child loading and on-demand expansion |
| `LazyChildLoader<T, ID>` | Interface for lazy child loading |

## Requirements

- Java 25+
- No external dependencies

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

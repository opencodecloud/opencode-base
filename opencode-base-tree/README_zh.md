# OpenCode Base Tree

**适用于 Java 25+ 的树数据结构工具库**

`opencode-base-tree` 提供了完整的树数据结构支持，包括从扁平列表构建树、多种遍历算法、平衡树（AVL、红黑树）、差异比较、序列化、支持懒加载的虚拟树等功能。

## 功能特性

### 核心功能
- **树构建**：通过 ID/parentID 映射将扁平列表转换为树形层级
- **多种遍历**：前序、后序、广度优先、深度限制、层序遍历
- **搜索与过滤**：按 ID 查找、按条件查找、路径查找、叶子节点提取
- **树操作**：扁平化、排序、深度计算、节点计数、保留祖先的过滤

### 高级功能
- **平衡树**：AVL 树和红黑树实现
- **虚拟树**：懒加载子节点，按需展开和预加载
- **树差异比较**：比较两棵树并生成差异结果
- **序列化**：将树序列化为 JSON、XML 和 Map 表示
- **并发构建**：线程安全的树构建
- **列表转树**：通用扁平列表转树转换器，支持重复检测
- **路径查找**：从根到节点的路径提取
- **循环检测**：检测树结构中的循环

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-tree</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.tree.*;

// 使用 Treeable 接口从扁平列表构建树
List<Menu> menus = menuService.findAll();
List<Menu> tree = OpenTree.buildTree(menus);

// 使用自定义 ID/parentID 提取器构建树
List<TreeNode<Dept>> tree = OpenTree.build(depts,
    Dept::getId, Dept::getParentId);

// 遍历
OpenTree.traversePreOrder(tree, node -> System.out.println(node.getName()));
OpenTree.traverseBreadthFirst(tree, node -> process(node));
OpenTree.traverseWithDepth(tree, (node, depth) -> indent(depth, node));

// 搜索
Menu found = OpenTree.find(tree, menuId);
List<Menu> matches = OpenTree.findAll(tree, m -> m.isEnabled());
List<Menu> leaves = OpenTree.getLeaves(tree);
List<Menu> path = OpenTree.getPath(tree, targetId);

// 操作
List<Menu> filtered = OpenTree.filter(tree, m -> m.isVisible());
List<Menu> flat = OpenTree.flattenTree(tree);
int depth = OpenTree.depth(tree);
int size = OpenTree.size(tree);

// 序列化
String json = OpenTree.toJson(tree);
String xml = OpenTree.toXml(tree);

// 虚拟树与懒加载
VirtualTree<Dept, Long> vTree = OpenTree.virtualTree(
    1L, rootDept, id -> deptService.findChildren(id));
OpenTree.preloadVirtualTree(vTree, 3);

// 打印树
OpenTree.printToConsole(treeNode);
```

## 类参考

### 根包 (`cloud.opencode.base.tree`)
| 类 | 说明 |
|----|------|
| `OpenTree` | 主门面：构建、遍历、搜索、过滤、序列化、虚拟树 |
| `TreeNode<T>` | 通用树节点，包含数据和子节点列表 |
| `TreeBuilder` | 通过 ID/parentID 映射从扁平集合构建树 |
| `Treeable<T, ID>` | 树能力实体接口：getId、getParentId、getChildren、setChildren |
| `DefaultTreeNode<T>` | 默认 Treeable 实现，包含 id、parentId、name、extra 字段 |
| `LightTreeNode` | 轻量级树节点，最小化内存占用 |
| `TreePrinter` | 将树结构美化打印为字符串 |
| `TreeTraverser` | 可配置的树遍历引擎 |

### 平衡树 (`tree.balanced`)
| 类 | 说明 |
|----|------|
| `AvlTree<T>` | 自平衡 AVL 树，支持插入、删除、搜索 |
| `BalancedTreeUtil` | 平衡树操作工具方法 |
| `RedBlackTree<T>` | 红黑树实现，保证 O(log n) 操作 |

### 构建器 (`tree.builder`)
| 类 | 说明 |
|----|------|
| `ConcurrentTreeBuilder` | 线程安全的树构建器 |
| `ListToTreeConverter` | 通用扁平列表转树转换器，支持重复拒绝 |

### 差异 (`tree.diff`)
| 类 | 说明 |
|----|------|
| `TreeDiff` | 比较两棵树并生成差异结果 |
| `TreeDiffResult` | 树比较结果，包含新增、删除、修改的节点 |

### 异常 (`tree.exception`)
| 类 | 说明 |
|----|------|
| `CycleDetectedException` | 检测到树结构循环时抛出 |
| `TreeErrorCode` | 树异常错误码 |
| `TreeException` | 树操作基础异常 |

### 操作 (`tree.operation`)
| 类 | 说明 |
|----|------|
| `TreeFilter` | 树过滤，保留祖先节点 |
| `TreeMapper` | 使用映射函数转换树节点 |
| `TreeUtil` | 通用树工具操作 |

### 路径 (`tree.path`)
| 类 | 说明 |
|----|------|
| `PathFinder` | 在树中查找节点间路径 |
| `TreePath` | 表示从根到目标节点的路径 |

### 结果 (`tree.result`)
| 类 | 说明 |
|----|------|
| `TreeResult` | 通用树操作结果包装器 |

### 序列化 (`tree.serialization`)
| 类 | 说明 |
|----|------|
| `TreeSerializer` | 将树序列化为 JSON、XML、Map 和扁平 Map 表示 |

### 遍历 (`tree.traversal`)
| 类 | 说明 |
|----|------|
| `TreeTraversal` | 遍历基础接口 |
| `PreOrderTraversal` | 前序（根优先）深度遍历 |
| `PostOrderTraversal` | 后序（子节点优先）深度遍历 |
| `LevelOrderTraversal` | 层序（广度优先）遍历 |
| `DepthLimitedTraversal` | 带最大深度限制的遍历 |
| `IterativeTraversal` | 基于栈的迭代遍历（非递归） |
| `TreeVisitor` | 树遍历访问者接口 |

### 验证 (`tree.validation`)
| 类 | 说明 |
|----|------|
| `CycleDetector` | 检测树/图结构中的循环 |
| `TreeNodeValidator` | 验证树节点约束 |

### 虚拟化 (`tree.virtual`)
| 类 | 说明 |
|----|------|
| `VirtualTree<T, ID>` | 虚拟树，支持懒加载子节点和按需展开 |
| `LazyChildLoader<T, ID>` | 懒加载子节点接口 |

## 环境要求

- Java 25+
- 无外部依赖

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

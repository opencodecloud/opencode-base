# OpenCode Base Tree

**适用于 Java 25+ 的树数据结构工具库**

`opencode-base-tree` 提供完整的树数据结构支持，包括扁平列表转树、多种遍历算法、平衡树（AVL、红黑树）、差异比较、树合并、序列化、懒加载虚拟树、最近公共祖先等功能。

## 功能特性

### 核心功能
- **树构建**：通过 ID/parentID 映射将扁平列表转换为树形层级
- **多种遍历**：前序、后序、广度优先、深度限制、层序遍历
- **搜索与过滤**：按 ID 查找、按条件查找、路径查找、叶子节点提取
- **树操作**：扁平化、排序、深度计算、节点计数、保留祖先链的过滤

### 高级功能
- **平衡树**：AVL 树和红黑树实现
- **虚拟树**：懒加载子节点，按需展开和预加载
- **树差异比较**：比较两棵树，生成新增/删除/修改差异结果
- **树合并**：支持冲突解决策略合并两个树森林
- **序列化**：将树序列化为 JSON、XML 和 Map
- **并发构建**：线程安全的树构建
- **路径查找**：根到节点路径提取、最近公共祖先（LCA）
- **树统计**：单次遍历收集全面指标（深度、宽度、分支因子）
- **循环检测**：检测树结构中的循环

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-tree</artifactId>
    <version>1.0.3</version>
</dependency>
```

### 实现 Treeable 接口

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
    // ... 构造器、setter ...
}
```

### 从扁平列表构建树

```java
import cloud.opencode.base.tree.OpenTree;

// 自动根节点检测（parentId=null/0/"" 视为根节点）
List<Menu> flatList = menuService.findAll();
List<Menu> tree = OpenTree.buildTree(flatList);

// 指定根节点ID
List<Menu> tree = OpenTree.buildTree(flatList, 0L);

// 构建排序树
List<Menu> tree = OpenTree.buildTreeSorted(flatList, 0L,
    Comparator.comparing(Menu::getSort));
```

### 遍历

```java
// 前序遍历（先父后子）
OpenTree.traversePreOrder(tree, node -> System.out.println(node.getName()));

// 后序遍历（先子后父）
OpenTree.traversePostOrder(tree, node -> cleanup(node));

// 广度优先（逐层遍历）
OpenTree.traverseBreadthFirst(tree, node -> process(node));

// 带深度信息
OpenTree.traverseWithDepth(tree, (node, depth) -> indent(depth, node));
```

### 搜索

```java
Menu found = OpenTree.find(tree, menuId);                          // 按ID查找
List<Menu> matches = OpenTree.findAll(tree, m -> m.isEnabled());   // 按条件查找
List<Menu> leaves = OpenTree.getLeaves(tree);                      // 叶子节��
List<Menu> path = OpenTree.getPath(tree, targetId);                // 根到节点路径
```

### 过滤

```java
// 过滤并保留祖先链
List<Menu> filtered = OpenTree.filter(tree, m -> m.isVisible());

// 扁平化为列表
List<Menu> flat = OpenTree.flattenTree(tree);
```

### 树合并 (V1.0.3)

```java
// 冲突时保留左侧节点
List<Menu> merged = OpenTree.mergeKeepLeft(tree1, tree2);

// 冲突时保留右侧节点
List<Menu> merged = OpenTree.mergeKeepRight(tree1, tree2);

// 自定义冲突解决
List<Menu> merged = OpenTree.merge(tree1, tree2, (left, right) -> {
    left.setName(right.getName()); // 取右侧名称
    return left;
});
```

### 递归排序 (V1.0.3)

```java
OpenTree.sort(tree, Comparator.comparing(Menu::getSort));   // 每层递归排序
OpenTree.sortBy(tree, Menu::getName);                        // 按提取键排序
boolean sorted = OpenTree.isSorted(tree, comparator);        // 检查是否已排序
```

### 树统计 (V1.0.3)

```java
import cloud.opencode.base.tree.operation.TreeStatistics;

TreeStatistics stats = OpenTree.statistics(tree);
stats.nodeCount();          // 总节点数
stats.leafCount();          // 叶子节点数
stats.maxDepth();           // 最大深度
stats.maxWidth();           // 最大宽度
stats.avgBranchingFactor(); // 平均分支因子
stats.widthByLevel();       // 每层宽度 Map<Integer, Integer>
stats.leafRatio();          // 叶子比率
stats.summary();            // 可读摘要
```

### 最近公共祖先 (V1.0.3)

```java
Optional<Menu> lca = OpenTree.findLCA(tree, id1, id2);
```

### 子树提取与兄弟节点 (V1.0.3)

```java
Optional<Menu> subtree = OpenTree.extractSubtree(tree, nodeId); // 提取子树
List<Menu> siblings = OpenTree.getSiblings(tree, nodeId);        // 兄弟节点
```

### 序列化

```java
String json = OpenTree.toJson(tree);
String xml = OpenTree.toXml(tree);
List<Map<String, Object>> maps = OpenTree.toMaps(tree);
```

### 虚拟树（懒加载）

```java
import cloud.opencode.base.tree.virtual.VirtualTree;

VirtualTree<Dept, Long> vTree = OpenTree.virtualTree(
    1L, rootDept, id -> deptService.findChildren(id));

// 子节点在首次访问时按需加载
List<VirtualTree<Dept, Long>> children = vTree.getChildren();

// 预加载到深度3
OpenTree.preloadVirtualTree(vTree, 3);
```

### 平衡树

```java
import cloud.opencode.base.tree.balanced.*;

// AVL 树（自平衡，O(log n)）
AvlTree<Integer> avl = new AvlTree<>();
avl.insert(5); avl.insert(3); avl.insert(7);

// 红黑树
RedBlackTree<String> rbt = new RedBlackTree<>();
rbt.insert("B"); rbt.insert("A"); rbt.insert("C");

// 从集合创建
AvlTree<Integer> avl = BalancedTreeUtil.avlTreeFrom(List.of(5, 3, 7, 1, 9));
```

## 类参考

### 根包 (`cloud.opencode.base.tree`)
| 类 | 说明 |
|----|------|
| `OpenTree` | 主门面 — 构建、遍历、搜索、过滤、合并、排序、LCA、统计、序列化、虚拟树 |
| `TreeNode<T>` | 通用树节点 |
| `TreeBuilder` | 通过 ID/parentID 从扁平集合构建树 |
| `Treeable<T, ID>` | 树能力接口 |
| `DefaultTreeNode<ID>` | 默认 Treeable 实现 |
| `LightTreeNode<ID>` | 轻量级不可变树节点（record） |
| `TreePrinter` | 树结构美化打印 |
| `TreeTraverser` | 可配置遍历引擎（流、迭代器、归约） |

### 操作 (`tree.operation`)
| 类 | 说明 |
|----|------|
| `TreeFilter` | 过滤（保留祖先链/扁平/按深度） |
| `TreeMapper` | 节点类型映射、值提取 |
| `TreeMerger` | 树森林合并（keepLeft/keepRight/自定义） |
| `TreeSorter` | 每层递归排序、排序检查 |
| `TreeStatistics` | 单次 BFS 全面统计指标（record） |
| `TreeUtil` | 查找、扁平化、计数、子树提取、兄弟节点 |

### 其他包
| 包 | 主要类 | 说明 |
|----|--------|------|
| `tree.balanced` | `AvlTree`, `RedBlackTree`, `BalancedTreeUtil` | 平衡树 |
| `tree.builder` | `ConcurrentTreeBuilder`, `ListToTreeConverter` | 构建器 |
| `tree.diff` | `TreeDiff`, `TreeDiffResult` | 树差异比较 |
| `tree.exception` | `TreeException`, `CycleDetectedException`, `TreeErrorCode` | 异常（继承 OpenException） |
| `tree.path` | `PathFinder`, `TreePath` | 路径查找、LCA |
| `tree.result` | `TreeResult` | 密封结果类型 |
| `tree.serialization` | `TreeSerializer` | JSON/XML/Map 序列化 |
| `tree.traversal` | `PreOrderTraversal`, `PostOrderTraversal`, `LevelOrderTraversal`, ... | 遍历策略 |
| `tree.validation` | `CycleDetector`, `TreeNodeValidator` | 验证 |
| `tree.virtual` | `VirtualTree`, `LazyChildLoader` | 懒加载虚拟树 |

## 安全性

- 所有递归操作均有深度保护（最大1000层）或使用迭代算法
- XML 序列化清理元素名防注入
- JSON 输出转义所有特殊字符
- 零外部依赖 — 零 CVE 攻击面

## 环境要求

- Java 25+
- 无外部依赖（仅依赖 opencode-base-core）

## 开源协议

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

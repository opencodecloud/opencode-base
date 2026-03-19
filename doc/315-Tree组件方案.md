# Tree 组件方案

## 1. 组件概述

`opencode-base-tree` 模块提供完整的树形结构处理能力，涵盖通用树节点模型、列表转树、多种遍历算法、路径查找、树过滤映射、差异计算、序列化、平衡二叉搜索树（AVL / 红黑树）、虚拟懒加载树以及树验证等功能。零外部依赖，纯 JDK 25 实现。

**核心特性：**
- 通用树结构：`Treeable` 接口 + `DefaultTreeNode` / `LightTreeNode` 开箱即用
- 列表转树：一行代码完成 `List -> Tree` 转换
- 遍历算法：前序、后序、层序、带深度、迭代式（防栈溢出）、深度限制
- 路径查找：根到节点路径、叶子路径
- 树操作：过滤（保留祖先链）、映射、扁平化、排序、统计
- 树差异计算：diff 对比，输出新增/删除/修改节点
- 序列化：树转 JSON / XML / Map
- 平衡树：AVL 树、红黑树
- 虚拟树：懒加载子节点，按需加载
- 验证：循环检测、节点校验
- 操作结果：`TreeResult` 密封接口，支持模式匹配

## 2. 包结构

```
cloud.opencode.base.tree
├── Treeable.java                    # 可树化接口
├── TreeNode.java                    # 通用树节点
├── DefaultTreeNode.java             # 默认树节点实现
├── LightTreeNode.java               # 轻量级树节点 (Record)
├── OpenTree.java                    # 树工具门面类
├── TreePrinter.java                 # 树形打印工具
├── TreeTraverser.java               # 高级遍历工具（Stream/Iterator/reduce/map）
├── TreeBuilder.java                 # 通用树构建器
│
├── builder/                         # 树构建
│   ├── ListToTreeConverter.java     # 列表转树转换器
│   └── ConcurrentTreeBuilder.java   # 并发树构建器
│
├── traversal/                       # 遍历算法
│   ├── TreeTraversal.java           # 遍历策略接口
│   ├── TreeVisitor.java             # 访问者接口
│   ├── PreOrderTraversal.java       # 先序遍历
│   ├── PostOrderTraversal.java      # 后序遍历
│   ├── LevelOrderTraversal.java     # 层序遍历
│   ├── IterativeTraversal.java      # 迭代式遍历（防栈溢出）
│   └── DepthLimitedTraversal.java   # 深度限制遍历
│
├── operation/                       # 树操作
│   ├── TreeUtil.java                # 树操作工具
│   ├── TreeFilter.java              # 树过滤器
│   └── TreeMapper.java              # 树映射器
│
├── path/                            # 路径查找
│   ├── PathFinder.java              # 路径查找器
│   └── TreePath.java                # 树路径 (Record)
│
├── diff/                            # 树差异
│   ├── TreeDiff.java                # 树差异计算
│   └── TreeDiffResult.java          # 差异结果 (Record)
│
├── serialization/                   # 序列化
│   └── TreeSerializer.java          # 树序列化器（JSON/XML/Map）
│
├── balanced/                        # 平衡树
│   ├── AvlTree.java                 # AVL 自平衡二叉搜索树
│   ├── RedBlackTree.java            # 红黑树
│   └── BalancedTreeUtil.java        # 平衡树工具
│
├── virtual/                         # 虚拟树
│   ├── VirtualTree.java             # 懒加载虚拟树
│   └── LazyChildLoader.java         # 懒加载子节点接口
│
├── result/                          # 操作结果
│   └── TreeResult.java              # 密封结果接口（Success/Failure/Empty/Validation）
│
├── validation/                      # 验证
│   ├── TreeNodeValidator.java       # 节点验证器
│   └── CycleDetector.java           # 循环检测器
│
└── exception/                       # 异常
    ├── TreeException.java           # 树异常基类
    ├── TreeErrorCode.java           # 错误码枚举
    └── CycleDetectedException.java  # 循环引用异常
```

---

## 3. 核心 API

### 3.1 Treeable

> 可树化接口，所有需要组织成树形结构的对象应实现此接口。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `ID getId()` | 获取节点 ID |
| `ID getParentId()` | 获取父节点 ID |
| `List<T> getChildren()` | 获取子节点列表 |
| `void setChildren(List<T> children)` | 设置子节点列表 |

**示例：**

```java
public class Menu implements Treeable<Menu, Long> {
    private Long id;
    private Long parentId;
    private String name;
    private List<Menu> children;

    @Override
    public Long getId() { return id; }
    @Override
    public Long getParentId() { return parentId; }
    @Override
    public List<Menu> getChildren() { return children; }
    @Override
    public void setChildren(List<Menu> children) { this.children = children; }
}
```

### 3.2 DefaultTreeNode

> 默认树节点实现，内置 `id`、`parentId`、`name`、`sort`、`extra` 扩展字段，开箱即用。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `DefaultTreeNode()` | 无参构造 |
| `DefaultTreeNode(ID id)` | 指定 ID 构造 |
| `DefaultTreeNode(ID id, ID parentId, String name)` | 指定 ID、父 ID、名称构造 |
| `ID getId()` | 获取节点 ID |
| `void setId(ID id)` | 设置节点 ID |
| `ID getParentId()` | 获取父节点 ID |
| `void setParentId(ID parentId)` | 设置父节点 ID |
| `String getName()` | 获取节点名称 |
| `void setName(String name)` | 设置节点名称 |
| `int getSort()` | 获取排序值 |
| `void setSort(int sort)` | 设置排序值 |
| `Map<String, Object> getExtra()` | 获取扩展属性 |
| `void setExtra(Map<String, Object> extra)` | 设置扩展属性 |
| `DefaultTreeNode<ID> put(String key, Object value)` | 添加扩展属性（链式） |
| `<V> V get(String key)` | 获取扩展属性值 |
| `<V> V get(String key, V defaultValue)` | 获取扩展属性值（带默认值） |
| `boolean isRoot()` | 判断是否根节点 |
| `boolean isLeaf()` | 判断是否叶子节点 |
| `DefaultTreeNode<ID> addChild(DefaultTreeNode<ID> child)` | 添加子节点 |
| `List<DefaultTreeNode<ID>> getChildren()` | 获取子节点列表 |
| `void setChildren(List<DefaultTreeNode<ID>> children)` | 设置子节点列表 |

**示例：**

```java
DefaultTreeNode<Long> root = new DefaultTreeNode<>(1L, null, "根节点");
DefaultTreeNode<Long> child = new DefaultTreeNode<>(2L, 1L, "子节点");
child.put("icon", "folder").put("url", "/system");
root.addChild(child);

String icon = child.get("icon"); // "folder"
```

### 3.3 LightTreeNode

> 轻量级树节点，使用 Record 实现，不可变。适用于只需基本树结构而无需扩展属性的场景。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `LightTreeNode(ID id, ID parentId, String name, List<LightTreeNode<ID>> children)` | 规范化构造 |
| `static <ID> LightTreeNode<ID> of(ID id, ID parentId, String name)` | 创建节点 |
| `static <ID> LightTreeNode<ID> root(ID id, String name)` | 创建根节点 |
| `boolean isRoot()` | 判断是否根节点 |
| `boolean isLeaf()` | 判断是否叶子节点 |
| `LightTreeNode<ID> withChild(LightTreeNode<ID> child)` | 添加子节点，返回新实例 |

**示例：**

```java
LightTreeNode<Long> root = LightTreeNode.root(1L, "系统管理");
LightTreeNode<Long> child = LightTreeNode.of(2L, 1L, "用户管理");
LightTreeNode<Long> tree = root.withChild(child);
```

### 3.4 TreeNode

> 通用树节点，可包裹任意数据类型，支持父子关系、遍历、查找、映射、过滤等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `TreeNode(T data)` | 使用数据创建节点 |
| `T getData()` | 获取节点数据 |
| `void setData(T data)` | 设置节点数据 |
| `TreeNode<T> getParent()` | 获取父节点 |
| `List<TreeNode<T>> getChildren()` | 获取子节点列表（不可变） |
| `TreeNode<T> addChild(T data)` | 添加子节点（数据） |
| `TreeNode<T> addChild(TreeNode<T> child)` | 添加子节点 |
| `boolean removeChild(TreeNode<T> child)` | 移除子节点 |
| `void clearChildren()` | 清空子节点 |
| `boolean isRoot()` | 判断是否根节点 |
| `boolean isLeaf()` | 判断是否叶子节点 |
| `int getChildCount()` | 获取子节点数量 |
| `boolean hasChildren()` | 是否有子节点 |
| `int getDepth()` | 获取节点深度（根为 0） |
| `int getHeight()` | 获取节点高度 |
| `TreeNode<T> getRoot()` | 获取根节点 |
| `List<TreeNode<T>> getSiblings()` | 获取兄弟节点 |
| `List<TreeNode<T>> getAncestors()` | 获取所有祖先节点 |
| `List<TreeNode<T>> getDescendants()` | 获取所有后代节点 |
| `List<TreeNode<T>> getLeaves()` | 获取所有叶子节点 |
| `Optional<TreeNode<T>> find(Predicate<T> predicate)` | 查找匹配节点 |
| `List<TreeNode<T>> findAll(Predicate<T> predicate)` | 查找所有匹配节点 |
| `void forEachPreOrder(Consumer<TreeNode<T>> action)` | 前序遍历 |
| `void forEachPostOrder(Consumer<TreeNode<T>> action)` | 后序遍历 |
| `void forEachBreadthFirst(Consumer<TreeNode<T>> action)` | 广度优先遍历 |
| `<R> TreeNode<R> map(Function<T, R> mapper)` | 映射为新树 |
| `TreeNode<T> filter(Predicate<T> predicate)` | 过滤节点 |
| `int size()` | 获取子树节点总数 |

**示例：**

```java
TreeNode<String> root = new TreeNode<>("公司");
root.addChild("技术部").addChild("前端组");
root.addChild("市场部");

root.forEachPreOrder(node -> System.out.println(node.getData()));
TreeNode<String> found = root.find(s -> s.equals("前端组")).orElse(null);
int total = root.size(); // 4
```

### 3.5 OpenTree

> 树工具门面类，提供构建、遍历、查找、过滤、扁平化、排序、统计、虚拟树、序列化等一站式静态方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> TreeNode<T> node(T data)` | 创建 TreeNode 节点 |
| `static <T, ID> List<TreeNode<T>> build(...)` | 从列表构建 TreeNode 树 |
| `static <T, ID> TreeNode<T> buildSingle(...)` | 构建单根 TreeNode 树 |
| `static TreeNode<Map<String, Object>> fromMap(Map, String)` | 从 Map 构建 TreeNode |
| `static <T> List<T> flatten(TreeNode<T> root)` | 扁平化 TreeNode 树 |
| `static <T> List<TreeBuilder.NodeWithDepth<T>> flattenWithDepth(TreeNode<T> root)` | 带深度扁平化 |
| `static <T> String print(TreeNode<T> root)` | 打印 TreeNode 树 |
| `static <T> void printToConsole(TreeNode<T> root)` | 打印到控制台 |
| `static <T extends Treeable<T, ID>, ID> List<T> buildTree(List<T> nodes)` | 列表转树（自动识别根节点） |
| `static <T extends Treeable<T, ID>, ID> List<T> buildTree(List<T> nodes, ID rootId)` | 列表转树（指定根 ID） |
| `static <T extends Treeable<T, ID>, ID> List<T> buildTreeSorted(List<T>, ID, Comparator<T>)` | 带排序的列表转树 |
| `static List<DefaultTreeNode<Object>> buildTreeFromMaps(List<Map>, String, String, String)` | 从 Map 列表构建树 |
| `static <T extends Treeable<T, ?>> void traversePreOrder(List<T> roots, Consumer<T>)` | 前序遍历 |
| `static <T extends Treeable<T, ?>> void traversePostOrder(List<T> roots, Consumer<T>)` | 后序遍历 |
| `static <T extends Treeable<T, ?>> void traverseBreadthFirst(List<T> roots, Consumer<T>)` | 广度优先遍历 |
| `static <T extends Treeable<T, ?>> void traverseWithDepth(List<T> roots, BiConsumer<T, Integer>)` | 带深度遍历 |
| `static <T extends Treeable<T, ID>, ID> T find(List<T> roots, ID id)` | 按 ID 查找节点 |
| `static <T extends Treeable<T, ?>> List<T> findAll(List<T> roots, Predicate<T>)` | 查找所有匹配节点 |
| `static <T extends Treeable<T, ID>, ID> List<T> getPath(List<T> roots, ID id)` | 获取根到节点路径 |
| `static <T extends Treeable<T, ?>> List<T> getLeaves(List<T> roots)` | 获取所有叶子节点 |
| `static <T extends Treeable<T, ID>, ID> List<T> filter(List<T> roots, Predicate<T>)` | 过滤（保留祖先链） |
| `static <T extends Treeable<T, ?>> List<T> flattenTree(List<T> roots)` | 扁平化树 |
| `static <T extends Treeable<T, ?>> int depth(List<T> roots)` | 计算树深度 |
| `static <T extends Treeable<T, ?>> int size(List<T> roots)` | 计算节点总数 |
| `static <T extends Treeable<T, ?>> void sortTree(List<T>, Comparator<T>)` | 递归排序树 |
| `static <T, ID> VirtualTree<T, ID> virtualTree(...)` | 创建虚拟树 |
| `static <T, ID> VirtualTree.Builder<T, ID> virtualTreeBuilder()` | 虚拟树构建器 |
| `static <T, ID> void preloadVirtualTree(VirtualTree<T, ID>, int depth)` | 预加载虚拟树 |
| `static <T extends Treeable<T, ID>, ID> String toJson(List<T> roots)` | 树转 JSON |
| `static <T extends Treeable<T, ID>, ID> String toXml(List<T> roots)` | 树转 XML |
| `static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toMaps(List<T> roots)` | 树转 Map 列表 |
| `static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toFlatMaps(List<T> roots)` | 树转扁平 Map 列表 |
| `static <T> String treeNodeToJson(TreeNode<T>, Function)` | TreeNode 转 JSON |
| `static <T> String treeNodeToXml(TreeNode<T>, Function)` | TreeNode 转 XML |

**示例：**

```java
// 列表转树
List<Menu> menus = menuService.listAll();
List<Menu> tree = OpenTree.buildTree(menus);

// 带排序构建
List<Menu> sorted = OpenTree.buildTreeSorted(menus, 0L,
    Comparator.comparingInt(Menu::getSort));

// 遍历
OpenTree.traversePreOrder(tree, m -> System.out.println(m.getName()));

// 带深度遍历
OpenTree.traverseWithDepth(tree, (m, depth) ->
    System.out.println("  ".repeat(depth) + m.getName()));

// 查找
Menu found = OpenTree.find(tree, 5L);
List<Menu> matches = OpenTree.findAll(tree, m -> m.getName().contains("管理"));

// 路径
List<Menu> path = OpenTree.getPath(tree, 5L);

// 过滤（保留匹配节点及其祖先链）
List<Menu> filtered = OpenTree.filter(tree, m -> m.getName().contains("系统"));

// 统计
int depth = OpenTree.depth(tree);
int size = OpenTree.size(tree);

// 序列化
String json = OpenTree.toJson(tree);
```

### 3.6 TreeBuilder

> 通用树构建器，将扁平数据通过 ID 提取器和父 ID 提取器构建为 `TreeNode` 树。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T, ID> List<TreeNode<T>> build(List<T>, Function<T, ID>, Function<T, ID>)` | 列表构建 TreeNode 森林 |
| `static <T, ID> TreeNode<T> buildSingle(List<T>, Function<T, ID>, Function<T, ID>)` | 列表构建单根 TreeNode |
| `static TreeNode<Map<String, Object>> buildFromMap(Map, String)` | 从嵌套 Map 构建 |
| `static TreeNode<Map<String, Object>> buildFromMap(Map, String, int)` | 从嵌套 Map 构建（限制深度） |
| `static <T> List<T> flatten(TreeNode<T> root)` | 扁平化 TreeNode |
| `static <T> List<NodeWithDepth<T>> flattenWithDepth(TreeNode<T> root)` | 带深度扁平化 |

**示例：**

```java
List<TreeNode<Department>> tree = TreeBuilder.build(
    departments,
    Department::getId,
    Department::getParentId
);
```

### 3.7 ListToTreeConverter

> 列表转树转换器，将 `Treeable` 列表转换为树形结构。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> List<T> convert(List<T> nodes)` | 列表转树（自动识别根节点） |
| `static <T extends Treeable<T, ID>, ID> List<T> convert(List<T> nodes, ID rootId)` | 列表转树（指定根 ID） |
| `static <T extends Treeable<T, ID>, ID> List<T> convertSorted(List<T>, ID, Comparator<T>)` | 带排序转换 |
| `static <T, N extends Treeable<N, ID>, ID> List<N> convert(List<T>, Function, Function, Function)` | 通用转换（自定义提取器） |

**示例：**

```java
List<Menu> tree = ListToTreeConverter.convert(menus);
List<Menu> tree = ListToTreeConverter.convert(menus, 0L);
```

### 3.8 ConcurrentTreeBuilder

> 并发树构建器，使用 `ConcurrentHashMap` 和并行流提升大数据量下的构建性能。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> List<T> build(List<T> nodes)` | 并发构建树 |
| `static <T extends Treeable<T, ID>, ID> List<T> build(List<T> nodes, ID rootId)` | 并发构建树（指定根 ID） |
| `static <T extends Treeable<T, ID>, ID> List<T> buildLarge(List<T>, ID, int)` | 大数据量并发构建 |

**示例：**

```java
// 10万+节点时使用并发构建
List<Category> tree = ConcurrentTreeBuilder.build(categories, 0L);
```

### 3.9 TreeTraverser

> 高级遍历工具，提供 Stream、Iterator、map、reduce、foldBottomUp、最近公共祖先等功能。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> boolean traverse(TreeNode<T>, ControlledVisitor<T>)` | 可控遍历（支持 CONTINUE / SKIP_CHILDREN / STOP） |
| `static <T extends Treeable<T, ID>, ID> boolean traverse(List<T>, ControlledVisitor)` | Treeable 可控遍历 |
| `static <T> Stream<TreeNode<T>> stream(TreeNode<T> root)` | 前序 Stream |
| `static <T extends Treeable<T, ID>, ID> Stream<T> stream(List<T> roots)` | Treeable Stream |
| `static <T> Stream<TreeNode<T>> parallelStream(TreeNode<T> root)` | 并行 Stream |
| `static <T> Stream<TreeNode<T>> breadthFirstStream(TreeNode<T> root)` | 广度优先 Stream |
| `static <T> Stream<TreeNode<T>> postOrderStream(TreeNode<T> root)` | 后序 Stream |
| `static <T> Iterator<TreeNode<T>> preOrderIterator(TreeNode<T> root)` | 前序迭代器 |
| `static <T> Iterator<TreeNode<T>> breadthFirstIterator(TreeNode<T> root)` | 广度优先迭代器 |
| `static <T> Iterator<TreeNode<T>> postOrderIterator(TreeNode<T> root)` | 后序迭代器 |
| `static <T extends Treeable<T, ID>, ID> Iterator<T> preOrderIterator(List<T> roots)` | Treeable 前序迭代器 |
| `static <T, R> TreeNode<R> map(TreeNode<T>, Function<T, R>)` | 映射为新树 |
| `static <T, R> TreeNode<R> mapNode(TreeNode<T>, Function<TreeNode<T>, R>)` | 节点映射为新树 |
| `static <T, R> Stream<R> flatMap(TreeNode<T>, Function<TreeNode<T>, Stream<R>>)` | 扁平映射 |
| `static <T, R> R reduce(TreeNode<T>, R, BiFunction<R, T, R>)` | 归约 |
| `static <T, R> R foldBottomUp(TreeNode<T>, Function<TreeNode<T>, R>, BiFunction)` | 自底向上折叠 |
| `static <T> List<TreeNode<T>> getDescendants(TreeNode<T>)` | 获取所有后代 |
| `static <T> List<TreeNode<T>> getSiblings(TreeNode<T>)` | 获取兄弟节点 |
| `static <T> List<TreeNode<T>> getAncestors(TreeNode<T>)` | 获取所有祖先 |
| `static <T> int getDepth(TreeNode<T>)` | 获取节点深度 |
| `static <T> TreeNode<T> findLowestCommonAncestor(TreeNode<T>, TreeNode<T>)` | 最近公共祖先 |

**示例：**

```java
// Stream 操作
TreeTraverser.stream(root)
    .filter(node -> node.getData().startsWith("A"))
    .map(TreeNode::getData)
    .forEach(System.out::println);

// 可控遍历
TreeTraverser.traverse(root, node -> {
    if (node.getData().equals("skip")) {
        return TraversalControl.SKIP_CHILDREN;
    }
    System.out.println(node.getData());
    return TraversalControl.CONTINUE;
});

// 归约
int total = TreeTraverser.reduce(root, 0, (sum, data) -> sum + 1);

// 最近公共祖先
TreeNode<String> lca = TreeTraverser.findLowestCommonAncestor(node1, node2);
```

### 3.10 TreePrinter

> 树形打印工具，输出 ASCII 树形图、缩进格式或统计信息。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> String print(List<T> roots)` | 打印 ASCII 树形图（默认用 getName） |
| `static <T extends Treeable<T, ID>, ID> String print(List<T>, Function)` | 打印 ASCII 树形图（自定义标签） |
| `static <T extends Treeable<T, ID>, ID> void printToConsole(List<T> roots)` | 打印到控制台 |
| `static <T extends Treeable<T, ID>, ID> void printToConsole(List<T>, Function)` | 打印到控制台（自定义标签） |
| `static <T extends Treeable<T, ID>, ID> void printToStream(List<T>, Function, PrintStream)` | 打印到输出流 |
| `static <T extends Treeable<T, ID>, ID> String printSingle(T root)` | 打印单棵树 |
| `static <T extends Treeable<T, ID>, ID> String printSimple(List<T>, Function)` | 简单缩进格式 |
| `static <T extends Treeable<T, ID>, ID> String getStats(List<T> roots)` | 获取统计信息 |

**示例：**

```java
String treeStr = TreePrinter.print(tree);
// ├── 系统管理
// │   ├── 用户管理
// │   └── 角色管理
// └── 业务管理
//     ├── 订单管理
//     └── 商品管理

TreePrinter.printToConsole(tree, Menu::getName);
String stats = TreePrinter.getStats(tree);
```

### 3.11 遍历策略

#### 3.11.1 TreeTraversal

> 遍历策略接口，可传入不同遍历实现。

| 方法 | 描述 |
|------|------|
| `<T extends Treeable<T, ID>, ID> void traverse(List<T> roots, TreeVisitor<T> visitor)` | 执行遍历 |

#### 3.11.2 TreeVisitor

> 访问者接口，遍历时回调。

| 方法 | 描述 |
|------|------|
| `void visit(T node)` | 访问节点 |

#### 3.11.3 PreOrderTraversal

> 先序遍历（根 -> 左 -> 右）。

| 方法 | 描述 |
|------|------|
| `static PreOrderTraversal getInstance()` | 获取单例 |
| `void traverse(List<T> roots, TreeVisitor<T> visitor)` | 先序遍历 |

#### 3.11.4 PostOrderTraversal

> 后序遍历（左 -> 右 -> 根）。

| 方法 | 描述 |
|------|------|
| `static PostOrderTraversal getInstance()` | 获取单例 |
| `void traverse(List<T> roots, TreeVisitor<T> visitor)` | 后序遍历 |

#### 3.11.5 LevelOrderTraversal

> 层序遍历（广度优先）。

| 方法 | 描述 |
|------|------|
| `static LevelOrderTraversal getInstance()` | 获取单例 |
| `void traverse(List<T> roots, TreeVisitor<T> visitor)` | 层序遍历 |

#### 3.11.6 IterativeTraversal

> 迭代式遍历，使用显式栈避免深层递归导致的栈溢出。

| 方法 | 描述 |
|------|------|
| `static IterativeTraversal getInstance()` | 获取单例 |
| `void traverse(List<T> roots, TreeVisitor<T> visitor)` | 迭代式遍历 |

#### 3.11.7 DepthLimitedTraversal

> 深度限制遍历，只遍历到指定深度。

| 方法 | 描述 |
|------|------|
| `DepthLimitedTraversal(int maxDepth)` | 构造（指定最大深度） |
| `static DepthLimitedTraversal of(int maxDepth)` | 创建实例 |
| `void traverse(List<T> roots, TreeVisitor<T> visitor)` | 深度限制遍历 |
| `int getMaxDepth()` | 获取最大深度 |

**示例：**

```java
// 策略模式遍历
TreeTraversal traversal = PreOrderTraversal.getInstance();
traversal.traverse(tree, node -> System.out.println(node.getName()));

// 深度限制
DepthLimitedTraversal.of(3).traverse(tree, node -> process(node));
```

### 3.12 TreeUtil

> 树操作工具类，提供查找、扁平化、统计等常用工具方法。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> Optional<T> findById(List<T>, ID)` | 按 ID 查找节点 |
| `static <T extends Treeable<T, ID>, ID> Optional<T> find(List<T>, Predicate<T>)` | 按条件查找节点 |
| `static <T extends Treeable<T, ID>, ID> List<T> findAll(List<T>, Predicate<T>)` | 查找所有匹配节点 |
| `static <T extends Treeable<T, ID>, ID> List<T> flatten(List<T>)` | 扁平化树 |
| `static <T extends Treeable<T, ID>, ID> int count(List<T>)` | 节点计数 |
| `static <T extends Treeable<T, ID>, ID> int getMaxDepth(List<T>)` | 获取最大深度 |
| `static <T extends Treeable<T, ID>, ID> List<T> getLeaves(List<T>)` | 获取叶子节点 |
| `static <T extends Treeable<T, ID>, ID> boolean contains(List<T>, ID)` | 判断是否包含指定 ID |

### 3.13 TreeFilter

> 树过滤器，支持保留祖先链的过滤、扁平过滤、按深度过滤。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> List<T> filter(List<T>, Predicate<T>)` | 过滤（匹配节点保留祖先链） |
| `static <T extends Treeable<T, ID>, ID> List<T> filterWithAncestors(List<T>, Predicate<T>)` | 过滤并保留所有祖先 |
| `static <T extends Treeable<T, ID>, ID> List<T> filterFlat(List<T>, Predicate<T>)` | 扁平过滤（返回匹配节点列表） |
| `static <T extends Treeable<T, ID>, ID> List<T> filterByDepth(List<T>, int)` | 按深度过滤 |

**示例：**

```java
// 过滤包含"管理"的节点，保留其祖先链
List<Menu> filtered = TreeFilter.filter(tree, m -> m.getName().contains("管理"));

// 只返回匹配节点（不含祖先）
List<Menu> flat = TreeFilter.filterFlat(tree, m -> m.isEnabled());

// 只保留前 2 层
List<Menu> top2 = TreeFilter.filterByDepth(tree, 2);
```

### 3.14 TreeMapper

> 树映射器，将树从一种类型映射到另一种类型。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <S extends Treeable<S, ID>, T extends Treeable<T, ID>, ID> List<T> map(List<S>, Function<S, T>)` | 映射为同接口的新树 |
| `static <S extends Treeable<S, ID>, T, ID> List<T> mapToAny(List<S>, Function<S, T>)` | 映射为任意类型列表 |
| `static <T extends Treeable<T, ID>, ID, R> List<R> extractAll(List<T>, Function<T, R>)` | 提取所有节点属性 |

### 3.15 PathFinder

> 路径查找器，提供根到节点的路径查找、所有叶子路径等功能。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> Optional<TreePath<T>> findPathById(List<T>, ID)` | 按 ID 查找路径 |
| `static <T extends Treeable<T, ID>, ID> Optional<TreePath<T>> findPath(List<T>, Predicate<T>)` | 按条件查找路径 |
| `static <T extends Treeable<T, ID>, ID> List<TreePath<T>> findAllPaths(List<T>, Predicate<T>)` | 查找所有匹配路径 |
| `static <T extends Treeable<T, ID>, ID> List<TreePath<T>> findAllLeafPaths(List<T>)` | 查找所有叶子路径 |
| `static <T extends Treeable<T, ID>, ID> List<ID> getAncestorIds(List<T>, ID)` | 获取祖先 ID 列表 |
| `static <T extends Treeable<T, ID>, ID> int getDepth(List<T>, ID)` | 获取节点深度 |

**示例：**

```java
Optional<TreePath<Menu>> path = PathFinder.findPathById(tree, 5L);
path.ifPresent(p -> {
    System.out.println(p.toString(" > ")); // 根 > 系统管理 > 用户管理
    Menu target = p.getTarget();
    Menu root = p.getRoot();
});
```

### 3.16 TreePath

> 树路径 Record，表示从根到目标节点的路径。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T> TreePath<T> empty()` | 空路径 |
| `static <T> TreePath<T> of(T... nodes)` | 从节点创建路径 |
| `static <T> TreePath<T> of(List<T> nodes)` | 从列表创建路径 |
| `boolean isEmpty()` | 是否为空 |
| `int length()` | 路径长度 |
| `T getRoot()` | 获取根节点 |
| `T getTarget()` | 获取目标节点 |
| `T get(int index)` | 按索引获取节点 |
| `T getParent()` | 获取目标的父节点 |
| `TreePath<T> subPath(int start, int end)` | 子路径 |
| `TreePath<T> append(T node)` | 追加节点 |
| `TreePath<T> reverse()` | 反转路径 |
| `boolean contains(T node)` | 是否包含节点 |
| `String toString(String separator)` | 用分隔符连接 |

### 3.17 TreeDiff

> 树差异计算工具，对比两棵树并输出差异。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> TreeDiffResult<T> diff(List<T> oldRoots, List<T> newRoots)` | 按 ID 对比差异 |
| `static <T extends Treeable<T, ID>, ID> TreeDiffResult<T> diff(List<T>, List<T>, BiPredicate)` | 按 ID 对比差异（自定义相等判断） |
| `static <T extends Treeable<T, ID>, ID, K> TreeDiffResult<T> diffByKey(List<T>, List<T>, Function<T, K>)` | 按自定义 Key 对比差异 |

### 3.18 TreeDiffResult

> 差异结果 Record，包含新增、删除、修改的节点列表。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `List<T> added()` | 新增节点 |
| `List<T> removed()` | 删除节点 |
| `List<ModifiedNode<T>> modified()` | 修改节点 |
| `static <T> TreeDiffResult<T> empty()` | 空结果 |
| `boolean isEqual()` | 两棵树是否相同 |
| `boolean hasChanges()` | 是否有变更 |
| `int getTotalChanges()` | 总变更数 |
| `int getAddedCount()` | 新增数 |
| `int getRemovedCount()` | 删除数 |
| `int getModifiedCount()` | 修改数 |
| `String getSummary()` | 变更摘要 |

**示例：**

```java
TreeDiffResult<Menu> diff = TreeDiff.diff(oldTree, newTree);
if (diff.hasChanges()) {
    System.out.println(diff.getSummary());
    diff.added().forEach(m -> System.out.println("新增: " + m.getName()));
    diff.removed().forEach(m -> System.out.println("删除: " + m.getName()));
}
```

### 3.19 TreeSerializer

> 树序列化工具，支持将树转为 JSON、XML、Map。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> String toJson(List<T> roots)` | 树转 JSON |
| `static <T extends Treeable<T, ID>, ID> String toJson(List<T>, SerializerConfig)` | 树转 JSON（自定义配置） |
| `static <T extends Treeable<T, ID>, ID> String toJsonSingle(T root)` | 单棵树转 JSON |
| `static <T extends Treeable<T, ID>, ID> String toJsonSingle(T, SerializerConfig)` | 单棵树转 JSON（自定义配置） |
| `static <T> String treeNodeToJson(TreeNode<T>, Function)` | TreeNode 转 JSON |
| `static <T> String treeNodeToJson(TreeNode<T>, Function, SerializerConfig)` | TreeNode 转 JSON（自定义配置） |
| `static <T extends Treeable<T, ID>, ID> String toXml(List<T> roots)` | 树转 XML |
| `static <T extends Treeable<T, ID>, ID> String toXml(List<T>, SerializerConfig)` | 树转 XML（自定义配置） |
| `static <T extends Treeable<T, ID>, ID> String toXmlSingle(T root)` | 单棵树转 XML |
| `static <T> String treeNodeToXml(TreeNode<T>, Function)` | TreeNode 转 XML |
| `static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toMaps(List<T>)` | 树转 Map（保留层级） |
| `static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toMaps(List<T>, SerializerConfig)` | 树转 Map（自定义配置） |
| `static <T extends Treeable<T, ID>, ID> Map<String, Object> toMap(T node)` | 单节点转 Map |
| `static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toFlatMaps(List<T>)` | 树转扁平 Map 列表 |
| `static <T extends Treeable<T, ID>, ID> List<Map<String, Object>> toFlatMaps(List<T>, SerializerConfig)` | 树转扁平 Map（自定义配置） |

**SerializerConfig 配置项：**

| 字段 | 描述 | 默认值 |
|------|------|--------|
| `idField` | ID 字段名 | `"id"` |
| `parentIdField` | 父 ID 字段名 | `"parentId"` |
| `childrenField` | 子节点字段名 | `"children"` |
| `nodeElement` | XML 节点元素名 | `"node"` |
| `rootElement` | XML 根元素名 | `"tree"` |
| `prettyPrint` | 是否美化输出 | `false` |
| `indentSize` | 缩进大小 | `2` |
| `includeParentId` | 是否包含父 ID | `true` |
| `includeEmptyChildren` | 是否包含空 children | `false` |
| `includeXmlDeclaration` | 是否包含 XML 声明 | `true` |

**示例：**

```java
String json = TreeSerializer.toJson(tree);
String xml = TreeSerializer.toXml(tree, SerializerConfig.builder()
    .prettyPrint(true)
    .childrenField("items")
    .build());
```

### 3.20 AvlTree

> AVL 自平衡二叉搜索树，通过旋转保持高度平衡，保证 O(log n) 的查找、插入、删除性能。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `AvlTree()` | 自然序构造 |
| `AvlTree(Comparator<? super T> comparator)` | 自定义比较器构造 |
| `int size()` | 获取元素数量 |
| `boolean isEmpty()` | 是否为空 |
| `void clear()` | 清空树 |
| `boolean insert(T data)` | 插入元素 |
| `void insert(T... elements)` | 批量插入 |
| `void insertAll(Collection<? extends T> elements)` | 集合插入 |
| `boolean delete(T data)` | 删除元素 |
| `Optional<T> deleteMin()` | 删除并返回最小元素 |
| `Optional<T> deleteMax()` | 删除并返回最大元素 |
| `boolean contains(T data)` | 是否包含元素 |
| `Optional<T> search(T data)` | 搜索元素 |
| `Optional<T> min()` | 获取最小值 |
| `Optional<T> max()` | 获取最大值 |
| `void inOrderTraversal(Consumer<T> action)` | 中序遍历 |
| `void preOrderTraversal(Consumer<T> action)` | 前序遍历 |
| `void postOrderTraversal(Consumer<T> action)` | 后序遍历 |
| `void levelOrderTraversal(Consumer<T> action)` | 层序遍历 |
| `List<T> toSortedList()` | 转有序列表 |
| `List<T> range(T from, T to)` | 范围查询 |
| `int height()` | 获取树高度 |
| `boolean isBalanced()` | 是否平衡 |

**示例：**

```java
AvlTree<Integer> tree = new AvlTree<>();
tree.insert(5, 3, 7, 1, 4, 6, 8);

tree.contains(4); // true
tree.min();       // Optional[1]
tree.max();       // Optional[8]
tree.range(3, 6); // [3, 4, 5, 6]
tree.toSortedList(); // [1, 3, 4, 5, 6, 7, 8]
```

### 3.21 RedBlackTree

> 红黑树，另一种自平衡二叉搜索树，通过颜色规则保持近似平衡。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `RedBlackTree()` | 自然序构造 |
| `RedBlackTree(Comparator<? super T> comparator)` | 自定义比较器构造 |
| `int size()` | 获取元素数量 |
| `boolean isEmpty()` | 是否为空 |
| `void clear()` | 清空树 |
| `boolean insert(T data)` | 插入元素 |
| `void insert(T... elements)` | 批量插入 |
| `void insertAll(Collection<? extends T> elements)` | 集合插入 |
| `boolean delete(T data)` | 删除元素 |
| `boolean contains(T data)` | 是否包含元素 |
| `Optional<T> search(T data)` | 搜索元素 |
| `Optional<T> min()` | 获取最小值 |
| `Optional<T> max()` | 获取最大值 |
| `void inOrderTraversal(Consumer<T> action)` | 中序遍历 |
| `void preOrderTraversal(Consumer<T> action)` | 前序遍历 |
| `void postOrderTraversal(Consumer<T> action)` | 后序遍历 |
| `List<T> toSortedList()` | 转有序列表 |
| `List<T> range(T from, T to)` | 范围查询 |
| `int height()` | 获取树高度 |
| `int blackHeight()` | 获取黑色高度 |

**示例：**

```java
RedBlackTree<String> tree = new RedBlackTree<>();
tree.insert("cherry", "apple", "banana");
tree.toSortedList(); // [apple, banana, cherry]
```

### 3.22 BalancedTreeUtil

> 平衡树工具类，提供便捷的创建、合并、统计、集合运算等操作。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Comparable<T>> AvlTree<T> avlTreeOf(T... elements)` | 快速创建 AVL 树 |
| `static <T extends Comparable<T>> AvlTree<T> avlTreeFrom(Collection<T>)` | 从集合创建 AVL 树 |
| `static <T> AvlTree<T> avlTreeOf(Comparator<T>, T...)` | 自定义比较器创建 AVL 树 |
| `static <T extends Comparable<T>> RedBlackTree<T> redBlackTreeOf(T...)` | 快速创建红黑树 |
| `static <T extends Comparable<T>> RedBlackTree<T> redBlackTreeFrom(Collection<T>)` | 从集合创建红黑树 |
| `static <T> RedBlackTree<T> redBlackTreeOf(Comparator<T>, T...)` | 自定义比较器创建红黑树 |
| `static <T extends Comparable<T>> AvlTree<T> fromSortedArray(T[])` | 从有序数组构建最优 AVL 树 |
| `static <T extends Comparable<T>> AvlTree<T> fromSortedList(List<T>)` | 从有序列表构建最优 AVL 树 |
| `static <T extends Comparable<T>> AvlTree<T> merge(AvlTree<T>, AvlTree<T>)` | 合并两棵 AVL 树 |
| `static <T extends Comparable<T>> RedBlackTree<T> merge(RedBlackTree<T>, RedBlackTree<T>)` | 合并两棵红黑树 |
| `static <T> Optional<T> kthSmallest(AvlTree<T>, int k)` | 第 k 小元素 |
| `static <T> Optional<T> kthLargest(AvlTree<T>, int k)` | 第 k 大元素 |
| `static <T> Optional<T> median(AvlTree<T>)` | 中位数 |
| `static <T extends Comparable<T>> Optional<T> floor(AvlTree<T>, T)` | 不大于给定值的最大元素 |
| `static <T extends Comparable<T>> Optional<T> ceiling(AvlTree<T>, T)` | 不小于给定值的最小元素 |
| `static <T extends Comparable<T>> AvlTree<T> intersection(AvlTree<T>, AvlTree<T>)` | 交集 |
| `static <T extends Comparable<T>> AvlTree<T> union(AvlTree<T>, AvlTree<T>)` | 并集 |
| `static <T extends Comparable<T>> AvlTree<T> difference(AvlTree<T>, AvlTree<T>)` | 差集 |
| `static <T extends Comparable<T>> boolean isValidBst(AvlTree<T>)` | 验证是否有效 BST |
| `static <T> TreeStats stats(AvlTree<T>)` | AVL 树统计 |
| `static <T> TreeStats stats(RedBlackTree<T>)` | 红黑树统计 |

**TreeStats Record：**

| 字段/方法 | 描述 |
|-----------|------|
| `int size` | 节点数 |
| `int height` | 高度 |
| `boolean balanced` | 是否平衡 |
| `int minPossibleHeight()` | 理论最小高度 |
| `double balanceEfficiency()` | 平衡效率 |

**示例：**

```java
AvlTree<Integer> tree = BalancedTreeUtil.avlTreeOf(5, 3, 7, 1, 4);
Optional<Integer> floor = BalancedTreeUtil.floor(tree, 6);   // Optional[5]
Optional<Integer> median = BalancedTreeUtil.median(tree);     // Optional[4]
AvlTree<Integer> merged = BalancedTreeUtil.merge(tree1, tree2);
TreeStats stats = BalancedTreeUtil.stats(tree);
```

### 3.23 VirtualTree

> 虚拟懒加载树，子节点按需加载，适合大数据量或远程数据源场景。支持节点缓存、预加载、缓存统计。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `VirtualTree(ID id, ID parentId, T data, LazyChildLoader)` | 构造 |
| `VirtualTree(ID id, ID parentId, T data, LazyChildLoader, Map, int, boolean)` | 全参构造（含缓存配置） |
| `static <T, ID> VirtualTree<T, ID> root(ID id, T data, LazyChildLoader)` | 创建根节点 |
| `static <T, ID> Builder<T, ID> builder()` | 构建器 |
| `ID getId()` | 获取 ID |
| `ID getParentId()` | 获取父 ID |
| `List<VirtualTree<T, ID>> getChildren()` | 获取子节点（触发懒加载） |
| `void setChildren(List<VirtualTree<T, ID>> children)` | 设置子节点 |
| `boolean isChildrenLoaded()` | 子节点是否已加载 |
| `void reloadChildren()` | 重新加载子节点 |
| `void preload(int depth)` | 预加载指定深度 |
| `void unloadChildren()` | 卸载子节点 |
| `T getData()` | 获取节点数据 |
| `boolean isRoot()` | 是否根节点 |
| `boolean isLeaf()` | 是否叶子节点 |
| `int getChildCount()` | 子节点数量 |
| `Optional<VirtualTree<T, ID>> findInCache(ID nodeId)` | 从缓存查找 |
| `Optional<VirtualTree<T, ID>> find(ID nodeId)` | 递归查找 |
| `List<VirtualTree<T, ID>> findAll(Predicate<T>)` | 查找所有匹配 |
| `void traversePreOrder(Consumer)` | 前序遍历 |
| `void traversePostOrder(Consumer)` | 后序遍历 |
| `void traverseBreadthFirst(Consumer)` | 广度优先遍历 |
| `void traverseWithDepthLimit(Consumer, int)` | 深度限制遍历 |
| `CacheStats getCacheStats()` | 获取缓存统计 |
| `void clearCache()` | 清空缓存 |
| `int getLoadedNodeCount()` | 已加载节点数 |

**Builder 方法：**

| 方法 | 描述 |
|------|------|
| `Builder<T, ID> id(ID id)` | 设置 ID |
| `Builder<T, ID> parentId(ID parentId)` | 设置父 ID |
| `Builder<T, ID> data(T data)` | 设置数据 |
| `Builder<T, ID> childLoader(LazyChildLoader)` | 设置加载器 |
| `Builder<T, ID> nodeCache(Map)` | 设置缓存 |
| `Builder<T, ID> maxCacheSize(int)` | 最大缓存容量 |
| `Builder<T, ID> cacheEnabled(boolean)` | 是否启用缓存 |
| `VirtualTree<T, ID> build()` | 构建 |

**示例：**

```java
VirtualTree<String, Long> root = VirtualTree.<String, Long>builder()
    .id(1L)
    .data("根节点")
    .childLoader((id) -> loadChildrenFromDatabase(id))
    .maxCacheSize(1000)
    .cacheEnabled(true)
    .build();

// 按需加载
root.getChildren(); // 首次访问触发加载
root.preload(3);    // 预加载 3 层
```

### 3.24 TreeResult

> 密封接口，表示树操作结果。支持 JDK 25 模式匹配。

**子类型：**

| 类型 | 描述 |
|------|------|
| `Success<T>(T data)` | 成功结果 |
| `Failure<T>(String message, Throwable cause)` | 失败结果 |
| `Empty<T>()` | 空结果 |
| `Validation<T>(List<Violation> violations)` | 验证结果 |

**主要方法：**

| 方法 | 描述 |
|------|------|
| `boolean isSuccess()` | 是否成功 |
| `boolean isFailed()` | 是否失败 |
| `boolean isEmpty()` | 是否为空 |

**Violation Record：**

| 方法 | 描述 |
|------|------|
| `static Violation of(String message)` | 创建违规 |
| `static Violation error(String field, String message)` | 错误级别违规 |
| `static Violation warning(String field, String message)` | 警告级别违规 |
| `static Violation info(String field, String message)` | 信息级别违规 |
| `boolean isError()` | 是否错误 |
| `boolean isWarning()` | 是否警告 |

**示例：**

```java
String message = switch (result) {
    case TreeResult.Success(var data) -> "找到: " + data;
    case TreeResult.Failure(var msg, var cause) -> "错误: " + msg;
    case TreeResult.Empty() -> "未找到数据";
    case TreeResult.Validation(var violations) -> "验证失败: " + violations;
};
```

### 3.25 CycleDetector

> 循环检测器，用于检测树结构中的循环引用。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> boolean hasCycle(List<T>)` | 是否存在循环 |
| `static <T extends Treeable<T, ID>, ID> Optional<List<ID>> findCyclePath(List<T>)` | 查找循环路径 |
| `static <T extends Treeable<T, ID>, ID> void checkNoCycle(List<T>)` | 无循环断言（有则抛异常） |
| `static <T extends Treeable<T, ID>, ID> boolean hasPotentialCycle(List<T>)` | 是否存在潜在循环 |

### 3.26 TreeNodeValidator

> 节点验证器，验证树结构的完整性。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `static <T extends Treeable<T, ID>, ID> ValidationResult validate(List<T>)` | 验证节点列表 |
| `static <T extends Treeable<T, ID>, ID> ValidationResult validateStructure(List<T>)` | 验证树结构 |
| `static <T extends Treeable<T, ID>, ID> void validateOrThrow(List<T>)` | 验证（失败抛异常） |

**ValidationResult Record：**

| 方法 | 描述 |
|------|------|
| `boolean valid()` | 是否有效 |
| `List<String> errors()` | 错误列表 |
| `boolean hasErrors()` | 是否有错误 |
| `String getErrorMessage()` | 错误消息 |

### 3.27 TreeException

> 树异常基类，包含错误码。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `TreeException(String message)` | 消息构造 |
| `TreeException(String code, String message)` | 编码+消息构造 |
| `TreeException(TreeErrorCode errorCode)` | 错误码构造 |
| `TreeException(String message, Throwable cause)` | 消息+原因构造 |
| `String getCode()` | 获取错误码 |
| `static TreeException buildFailed(String)` | 构建失败异常 |
| `static TreeException invalidNode(String)` | 无效节点异常 |
| `static TreeException duplicateId(Object)` | 重复 ID 异常 |
| `static TreeException parentNotFound(Object)` | 父节点不存在异常 |
| `static TreeException nodeNotFound(Object)` | 节点不存在异常 |
| `static TreeException maxDepthExceeded(int)` | 超过最大深度异常 |

### 3.28 CycleDetectedException

> 循环检测异常，继承 `TreeException`。

**主要方法：**

| 方法 | 描述 |
|------|------|
| `CycleDetectedException(String message)` | 消息构造 |
| `CycleDetectedException(List<?> cyclePath)` | 循环路径构造 |
| `CycleDetectedException(String message, List<?> cyclePath)` | 消息+路径构造 |
| `List<?> getCyclePath()` | 获取循环路径 |

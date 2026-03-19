# Graph 组件方案

## 1. 组件概述

`opencode-base-graph` 模块提供完整的图数据结构与算法支持，包括有向图/无向图/加权图、图遍历、最短路径、拓扑排序、最小生成树、网络流、社区检测、中心性分析、子图操作、图布局、图序列化等功能。基于 JDK 25，零第三方依赖。

## 2. 包结构

```
cloud.opencode.base.graph
├── OpenGraph.java                         # 门面入口类
├── Graph.java                             # 图接口
├── DirectedGraph.java                     # 有向图实现
├── UndirectedGraph.java                   # 无向图实现
├── WeightedGraph.java                     # 加权图接口及实现
│
├── node/                                  # 节点与边
│   ├── Node.java                          # 节点接口 + SimpleNode 实现
│   └── Edge.java                          # 边 Record
│
├── algorithm/                             # 图算法
│   ├── GraphTraversalUtil.java            # BFS/DFS 遍历
│   ├── SafeGraphTraversalUtil.java        # 迭代式安全遍历
│   ├── ShortestPathUtil.java              # Dijkstra 最短路径
│   ├── AStarUtil.java                     # A* 算法
│   ├── BidirectionalBfsUtil.java          # 双向 BFS
│   ├── TopologicalSortUtil.java           # 拓扑排序
│   ├── CycleDetectionUtil.java            # 环检测
│   ├── ConnectedComponentsUtil.java       # 连通分量
│   ├── MinimumSpanningTreeUtil.java       # 最小生成树 (Prim/Kruskal)
│   ├── NetworkFlowUtil.java               # 网络流 (最大流/最小割)
│   ├── CentralityUtil.java               # 中心性算法 (PageRank/度/接近/中介)
│   ├── CommunityDetectionUtil.java        # 社区检测 (Louvain/标签传播)
│   └── SubgraphUtil.java                  # 子图操作 (诱导/过滤/集合运算)
│
├── builder/                               # 构建器
│   └── GraphBuilder.java                  # 图构建器
│
├── validation/                            # 验证
│   ├── GraphValidator.java                # 图验证器
│   └── ValidationResult.java             # 验证结果
│
├── security/                              # 安全
│   └── SafeGraphOperations.java           # 安全图操作 (资源限制/超时)
│
├── serializer/                            # 序列化
│   ├── GraphSerializer.java               # DOT/邻接表/边列表序列化
│   ├── GraphMLUtil.java                   # GraphML 格式导入导出
│   └── GexfUtil.java                      # GEXF 格式导入导出
│
├── layout/                                # 布局算法
│   └── LayoutUtil.java                    # 力导向/弹簧/环形/网格/层次/随机布局
│
└── exception/                             # 异常
    ├── GraphException.java                # 图异常基类
    ├── GraphErrorCode.java                # 错误码枚举
    ├── VertexNotFoundException.java        # 顶点不存在
    ├── EdgeNotFoundException.java          # 边不存在
    ├── CycleDetectedException.java         # 检测到环
    ├── NoPathException.java               # 无路径
    ├── InvalidVertexException.java         # 无效顶点
    ├── InvalidEdgeException.java           # 无效边
    ├── GraphLimitExceededException.java    # 超出限制
    └── GraphTimeoutException.java          # 计算超时
```

## 3. 核心 API

### 3.1 OpenGraph

> 图组件门面入口类，提供图创建、遍历、最短路径、拓扑排序、连通性、最小生成树、网络流等常用操作的静态便捷方法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> Graph<V> directed()` | 创建有向图 |
| `static <V> Graph<V> undirected()` | 创建无向图 |
| `static <V> WeightedGraph<V> directedWeighted()` | 创建加权有向图 |
| `static <V> WeightedGraph<V> undirectedWeighted()` | 创建加权无向图 |
| `static <V> GraphBuilder<V> directedBuilder()` | 创建有向图构建器 |
| `static <V> GraphBuilder<V> undirectedBuilder()` | 创建无向图构建器 |
| `static <V> List<V> bfs(Graph<V> graph, V start)` | 广度优先搜索 |
| `static <V> List<V> dfs(Graph<V> graph, V start)` | 深度优先搜索 |
| `static <V> List<V> dfsIterative(Graph<V> graph, V start)` | 迭代式深度优先搜索（防栈溢出） |
| `static <V> Map<V, Double> dijkstra(Graph<V> graph, V source)` | Dijkstra 单源最短路径 |
| `static <V> List<V> shortestPath(Graph<V> graph, V source, V target)` | 获取两点间最短路径 |
| `static <V> List<V> aStar(Graph<V> graph, V source, V target, BiFunction<V, V, Double> heuristic)` | A* 搜索 |
| `static <V> List<V> bidirectionalBfs(Graph<V> graph, V source, V target)` | 双向 BFS 搜索 |
| `static <V> List<V> topologicalSort(Graph<V> graph)` | 拓扑排序 |
| `static <V> boolean canTopologicalSort(Graph<V> graph)` | 判断是否可拓扑排序 |
| `static <V> boolean hasCycle(Graph<V> graph)` | 检测图中是否存在环 |
| `static <V> List<V> findCycle(Graph<V> graph)` | 查找图中的环 |
| `static <V> List<Set<V>> connectedComponents(Graph<V> graph)` | 获取连通分量 |
| `static <V> boolean isConnected(Graph<V> graph, V v1, V v2)` | 判断两个顶点是否连通 |
| `static <V> boolean isFullyConnected(Graph<V> graph)` | 判断图是否全连通 |
| `static <V> int connectedComponentCount(Graph<V> graph)` | 获取连通分量数量 |
| `static <V> Set<Edge<V>> prim(Graph<V> graph)` | Prim 最小生成树 |
| `static <V> Set<Edge<V>> prim(Graph<V> graph, V start)` | Prim 最小生成树（指定起点） |
| `static <V> Set<Edge<V>> kruskal(Graph<V> graph)` | Kruskal 最小生成树 |
| `static <V> double mstWeight(Graph<V> graph)` | 最小生成树权重 |
| `static <V> boolean hasSpanningTree(Graph<V> graph)` | 判断是否存在生成树 |
| `static <V> double maxFlow(Graph<V> graph, V source, V sink)` | 最大流 |
| `static <V> Map<Edge<V>, Double> getFlows(Graph<V> graph, V source, V sink)` | 获取各边流量 |
| `static <V> Set<Edge<V>> minCut(Graph<V> graph, V source, V sink)` | 最小割 |
| `static <V> NetworkFlowUtil.FlowResult<V> computeFlow(Graph<V> graph, V source, V sink)` | 完整流计算结果 |

**示例:**

```java
// 创建有向图并添加边
Graph<String> graph = OpenGraph.directed();
graph.addEdge("A", "B", 1.0);
graph.addEdge("A", "C", 4.0);
graph.addEdge("B", "C", 2.0);
graph.addEdge("C", "D", 1.0);

// BFS 遍历
List<String> bfsResult = OpenGraph.bfs(graph, "A");

// 最短路径
List<String> path = OpenGraph.shortestPath(graph, "A", "D");

// 拓扑排序
List<String> order = OpenGraph.topologicalSort(graph);

// 最大流
double flow = OpenGraph.maxFlow(graph, "A", "D");
```

### 3.2 Graph

> 图接口，定义图的基本操作，包括顶点/边的增删查、邻接关系查询、权重获取等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `void addVertex(V vertex)` | 添加顶点 |
| `void addEdge(V from, V to)` | 添加默认权重边 |
| `void addEdge(V from, V to, double weight)` | 添加加权边 |
| `void removeVertex(V vertex)` | 移除顶点及关联边 |
| `void removeEdge(V from, V to)` | 移除边 |
| `Set<V> vertices()` | 获取所有顶点 |
| `Set<Edge<V>> edges()` | 获取所有边 |
| `Set<V> neighbors(V vertex)` | 获取邻接顶点 |
| `Set<Edge<V>> outEdges(V vertex)` | 获取出边 |
| `Set<Edge<V>> inEdges(V vertex)` | 获取入边 |
| `int vertexCount()` | 获取顶点数 |
| `int edgeCount()` | 获取边数 |
| `boolean containsVertex(V vertex)` | 判断是否包含顶点 |
| `boolean containsEdge(V from, V to)` | 判断是否包含边 |
| `double getWeight(V from, V to)` | 获取边权重 |
| `boolean isDirected()` | 是否为有向图 |
| `void clear()` | 清空图 |

### 3.3 DirectedGraph

> 有向图实现，边有方向，适用于依赖关系、工作流等场景。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `DirectedGraph()` | 创建有向图 |
| `void addVertex(V vertex)` | 添加顶点 |
| `void addEdge(V from, V to, double weight)` | 添加有向加权边 |
| `void removeVertex(V vertex)` | 移除顶点及所有关联边 |
| `void removeEdge(V from, V to)` | 移除有向边 |
| `Set<V> neighbors(V vertex)` | 获取后继顶点 |
| `Set<Edge<V>> outEdges(V vertex)` | 获取出边集合 |
| `Set<Edge<V>> inEdges(V vertex)` | 获取入边集合 |
| `boolean isDirected()` | 返回 true |

**示例:**

```java
Graph<String> graph = new DirectedGraph<>();
graph.addEdge("A", "B", 1.0);
graph.addEdge("B", "C", 2.0);
Set<String> neighbors = graph.neighbors("A");  // ["B"]
```

### 3.4 UndirectedGraph

> 无向图实现，边无方向，适用于社交网络、路网等场景。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `UndirectedGraph()` | 创建无向图 |
| `void addEdge(V from, V to, double weight)` | 添加无向加权边（双向） |
| `void removeVertex(V vertex)` | 移除顶点 |
| `void removeEdge(V from, V to)` | 移除无向边（双向） |
| `boolean isDirected()` | 返回 false |

**示例:**

```java
Graph<String> graph = new UndirectedGraph<>();
graph.addEdge("A", "B");
Set<String> aNeighbors = graph.neighbors("A");  // ["B"]
Set<String> bNeighbors = graph.neighbors("B");  // ["A"]
```

### 3.5 WeightedGraph

> 加权图接口，扩展 Graph 接口，支持设置边权重和计算总权重。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `boolean setWeight(V from, V to, double weight)` | 设置已有边的权重 |
| `double totalWeight()` | 计算图所有边的总权重 |
| `static <V> WeightedGraph<V> directed()` | 创建加权有向图 |
| `static <V> WeightedGraph<V> undirected()` | 创建加权无向图 |

**示例:**

```java
WeightedGraph<String> graph = WeightedGraph.directed();
graph.addEdge("A", "B", 3.0);
graph.setWeight("A", "B", 5.0);
double total = graph.totalWeight();
```

### 3.6 Edge

> 边 Record，表示图中两个顶点之间的连接，包含起点、终点和权重。不可变对象。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `Edge(V from, V to)` | 创建默认权重(1.0)的边 |
| `Edge(V from, V to, double weight)` | 创建加权边 |
| `V from()` | 获取起点 |
| `V to()` | 获取终点 |
| `double weight()` | 获取权重 |
| `Edge<V> reversed()` | 返回反向边 |
| `Edge<V> withWeight(double newWeight)` | 返回新权重的边 |
| `boolean isSelfLoop()` | 判断是否为自环 |

**示例:**

```java
Edge<String> edge = new Edge<>("A", "B", 5.0);
String from = edge.from();    // "A"
Edge<String> rev = edge.reversed();  // B -> A, 权重 5.0
```

### 3.7 Node

> 节点接口及 SimpleNode 实现，用于图中的顶点包装。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `V getValue()` | 获取节点值 |

### 3.8 GraphBuilder

> 图构建器，支持链式构建图实例，可批量添加顶点和边，支持验证。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> GraphBuilder<V> directed()` | 创建有向图构建器 |
| `static <V> GraphBuilder<V> undirected()` | 创建无向图构建器 |
| `GraphBuilder<V> initialCapacity(int capacity)` | 设置初始容量 |
| `GraphBuilder<V> addVertex(V vertex)` | 添加顶点 |
| `GraphBuilder<V> addVertices(V... vertices)` | 批量添加顶点 |
| `GraphBuilder<V> addVertices(Collection<V> vertices)` | 批量添加顶点集合 |
| `GraphBuilder<V> addEdge(V from, V to)` | 添加边 |
| `GraphBuilder<V> addEdge(V from, V to, double weight)` | 添加加权边 |
| `GraphBuilder<V> addEdge(Edge<V> edge)` | 添加 Edge 对象 |
| `GraphBuilder<V> addEdges(Collection<Edge<V>> edges)` | 批量添加边 |
| `GraphBuilder<V> configure(Consumer<GraphBuilder<V>> configurer)` | 配置回调 |
| `Graph<V> build()` | 构建图 |
| `Graph<V> buildAndValidate()` | 构建并验证图 |

**示例:**

```java
Graph<String> graph = GraphBuilder.<String>directed()
    .initialCapacity(100)
    .addEdge("A", "B", 1.0)
    .addEdge("B", "C", 2.0)
    .addEdge("C", "D", 3.0)
    .buildAndValidate();
```

### 3.9 GraphTraversalUtil

> 图遍历工具类，提供 BFS 和 DFS 遍历算法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> List<V> bfs(Graph<V> graph, V start)` | 广度优先搜索 |
| `static <V> void bfs(Graph<V> graph, V start, Consumer<V> visitor)` | BFS 带访问回调 |
| `static <V> List<V> dfs(Graph<V> graph, V start)` | 深度优先搜索 |
| `static <V> void dfs(Graph<V> graph, V start, Consumer<V> visitor)` | DFS 带访问回调 |
| `static <V> List<V> bfsAll(Graph<V> graph)` | BFS 遍历全图（含不连通分量） |
| `static <V> List<V> dfsAll(Graph<V> graph)` | DFS 遍历全图（含不连通分量） |

**示例:**

```java
List<String> bfsResult = GraphTraversalUtil.bfs(graph, "A");
GraphTraversalUtil.bfs(graph, "A", vertex -> System.out.println(vertex));
```

### 3.10 SafeGraphTraversalUtil

> 安全图遍历工具类，使用迭代方式替代递归避免栈溢出，支持深度/距离限制。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> List<V> dfsIterative(Graph<V> graph, V start)` | 迭代式 DFS |
| `static <V> void dfsIterative(Graph<V> graph, V start, Consumer<V> visitor)` | 迭代式 DFS 带回调 |
| `static <V> List<V> dfsWithLimit(Graph<V> graph, V start, int maxDepth)` | 带深度限制的 DFS |
| `static <V> List<V> dfsIterativeWithLimit(Graph<V> graph, V start, int maxDepth)` | 迭代式带深度限制 DFS |
| `static <V> List<V> bfsWithLimit(Graph<V> graph, V start, int maxDistance)` | 带距离限制的 BFS |

**示例:**

```java
// 大图安全遍历（不会栈溢出）
List<String> result = SafeGraphTraversalUtil.dfsIterative(graph, "A");
// 限制搜索深度
List<String> limited = SafeGraphTraversalUtil.dfsWithLimit(graph, "A", 100);
```

### 3.11 ShortestPathUtil

> 最短路径工具类，基于 Dijkstra 算法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> Map<V, Double> dijkstra(Graph<V> graph, V source)` | Dijkstra 单源最短距离 |
| `static <V> List<V> shortestPath(Graph<V> graph, V source, V target)` | 两点间最短路径 |
| `static <V> double shortestDistance(Graph<V> graph, V source, V target)` | 两点间最短距离 |
| `static <V> boolean hasPath(Graph<V> graph, V source, V target)` | 判断两点间是否存在路径 |

**示例:**

```java
Map<String, Double> distances = ShortestPathUtil.dijkstra(graph, "A");
List<String> path = ShortestPathUtil.shortestPath(graph, "A", "D");
```

### 3.12 AStarUtil

> A* 算法工具类，支持启发式搜索，适用于有启发函数的场景。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> List<V> findPath(Graph<V> graph, V source, V target, BiFunction<V, V, Double> heuristic)` | A* 搜索 |
| `static <V> List<V> findPath(Graph<V> graph, V source, V target)` | A* 搜索（零启发函数，等价于 Dijkstra） |
| `static <V> List<V> findPathWithCostLimit(Graph<V> graph, V source, V target, BiFunction<V, V, Double> heuristic, double costLimit)` | 带代价限制的 A* 搜索 |
| `static <V> PathResult<V> findPathDetailed(Graph<V> graph, V source, V target, BiFunction<V, V, Double> heuristic)` | 详细结果（含代价和扩展节点数） |

**示例:**

```java
BiFunction<String, String, Double> heuristic = (a, b) -> 0.0;
List<String> path = AStarUtil.findPath(graph, "A", "Z", heuristic);
AStarUtil.PathResult<String> result = AStarUtil.findPathDetailed(graph, "A", "Z", heuristic);
boolean found = result.hasPath();
```

### 3.13 BidirectionalBfsUtil

> 双向 BFS 工具类，从起点和终点同时搜索，适用于大图无权路径查找。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> List<V> findPath(Graph<V> graph, V source, V target)` | 双向 BFS 查找路径 |
| `static <V> boolean hasPath(Graph<V> graph, V source, V target)` | 判断是否有路径 |
| `static <V> int shortestPathLength(Graph<V> graph, V source, V target)` | 最短路径长度 |
| `static <V> Set<V> findVerticesOnPath(Graph<V> graph, V source, V target, int maxDistance)` | 查找路径上的顶点 |

### 3.14 TopologicalSortUtil

> 拓扑排序工具类，适用于有向无环图(DAG)。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> List<V> sort(Graph<V> graph)` | Kahn 算法拓扑排序 |
| `static <V> List<V> sortDfs(Graph<V> graph)` | DFS 拓扑排序 |
| `static <V> boolean canSort(Graph<V> graph)` | 判断是否可拓扑排序 |
| `static <V> Set<V> getSourceVertices(Graph<V> graph)` | 获取所有源顶点（入度为 0） |
| `static <V> Set<V> getSinkVertices(Graph<V> graph)` | 获取所有汇顶点（出度为 0） |
| `static <V> Map<V, Integer> getDependencyDepths(Graph<V> graph)` | 获取依赖深度映射 |

**示例:**

```java
Graph<String> dag = OpenGraph.directed();
dag.addEdge("compile", "test");
dag.addEdge("test", "package");
List<String> order = TopologicalSortUtil.sort(dag);  // [compile, test, package]
```

### 3.15 CycleDetectionUtil

> 环检测工具类，支持有向图和无向图。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> boolean hasCycle(Graph<V> graph)` | 检测是否存在环 |
| `static <V> List<V> findCycle(Graph<V> graph)` | 查找环（返回环上顶点） |
| `static <V> boolean wouldCreateCycle(Graph<V> graph, V from, V to)` | 判断添加边是否会产生环 |

### 3.16 ConnectedComponentsUtil

> 连通分量工具类，查找图中的连通子图。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> List<Set<V>> find(Graph<V> graph)` | 查找所有连通分量 |
| `static <V> boolean isConnected(Graph<V> graph, V v1, V v2)` | 判断两顶点是否连通 |
| `static <V> boolean isFullyConnected(Graph<V> graph)` | 判断图是否全连通 |
| `static <V> int count(Graph<V> graph)` | 获取连通分量数量 |
| `static <V> Set<V> getLargestComponent(Graph<V> graph)` | 获取最大连通分量 |
| `static <V> Set<V> getSmallestComponent(Graph<V> graph)` | 获取最小连通分量 |
| `static <V> Set<V> getComponentContaining(Graph<V> graph, V vertex)` | 获取包含指定顶点的连通分量 |

### 3.17 MinimumSpanningTreeUtil

> 最小生成树工具类，提供 Prim 和 Kruskal 算法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> Set<Edge<V>> prim(Graph<V> graph, V start)` | Prim 算法（指定起点） |
| `static <V> Set<Edge<V>> prim(Graph<V> graph)` | Prim 算法（自动选择起点） |
| `static <V> Set<Edge<V>> kruskal(Graph<V> graph)` | Kruskal 算法 |
| `static <V> double mstWeight(Graph<V> graph)` | 最小生成树总权重 |
| `static <V> double totalWeight(Set<Edge<V>> edges)` | 边集总权重 |
| `static <V> boolean hasSpanningTree(Graph<V> graph)` | 判断是否存在生成树 |
| `static <V> Set<Edge<V>> minimumSpanningForest(Graph<V> graph)` | 最小生成森林 |
| `static <V> int componentCount(Graph<V> graph)` | 连通分量数（通过并查集） |

**示例:**

```java
Set<Edge<String>> mst = MinimumSpanningTreeUtil.prim(graph, "A");
Set<Edge<String>> mst2 = MinimumSpanningTreeUtil.kruskal(graph);
double weight = MinimumSpanningTreeUtil.mstWeight(graph);
```

### 3.18 NetworkFlowUtil

> 网络流工具类，提供最大流（Edmonds-Karp BFS 和 DFS 增广）和最小割算法。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> double maxFlow(Graph<V> graph, V source, V sink)` | 最大流（BFS 增广） |
| `static <V> double maxFlowDfs(Graph<V> graph, V source, V sink)` | 最大流（DFS 增广） |
| `static <V> Map<Edge<V>, Double> getFlows(Graph<V> graph, V source, V sink)` | 获取各边流量 |
| `static <V> FlowResult<V> computeFlow(Graph<V> graph, V source, V sink)` | 计算完整流结果 |
| `static <V> Set<Edge<V>> minCut(Graph<V> graph, V source, V sink)` | 最小割 |
| `static <V> double minCutCapacity(Graph<V> graph, V source, V sink)` | 最小割容量 |

**示例:**

```java
double flow = NetworkFlowUtil.maxFlow(graph, "source", "sink");
Map<Edge<String>, Double> flows = NetworkFlowUtil.getFlows(graph, "source", "sink");
Set<Edge<String>> cut = NetworkFlowUtil.minCut(graph, "source", "sink");
```

### 3.19 CentralityUtil

> 中心性算法工具类，提供度中心性、接近中心性、中介中心性、PageRank、特征向量中心性、Katz 中心性等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> Map<V, Double> degreeCentrality(Graph<V> graph)` | 度中心性 |
| `static <V> Map<V, Double> inDegreeCentrality(Graph<V> graph)` | 入度中心性 |
| `static <V> Map<V, Double> outDegreeCentrality(Graph<V> graph)` | 出度中心性 |
| `static <V> Map<V, Double> closenessCentrality(Graph<V> graph)` | 接近中心性 |
| `static <V> Map<V, Double> betweennessCentrality(Graph<V> graph)` | 中介中心性 |
| `static <V> Map<V, Double> betweennessCentrality(Graph<V> graph, boolean normalized)` | 中介中心性（可选归一化） |
| `static <V> Map<V, Double> pageRank(Graph<V> graph)` | PageRank（默认参数） |
| `static <V> Map<V, Double> pageRank(Graph<V> graph, double dampingFactor, int maxIterations)` | PageRank（自定义参数） |
| `static <V> Map<V, Double> eigenvectorCentrality(Graph<V> graph)` | 特征向量中心性 |
| `static <V> Map<V, Double> katzCentrality(Graph<V> graph, double alpha, double beta)` | Katz 中心性 |
| `static <V> List<V> topK(Map<V, Double> centrality, int k)` | 取中心性 Top-K 顶点 |
| `static <V> Map<V, Double> normalize(Map<V, Double> centrality)` | 归一化中心性值 |
| `static <V> CentralityStats getStats(Map<V, Double> centrality)` | 中心性统计信息 |

**示例:**

```java
Map<String, Double> pageRank = CentralityUtil.pageRank(graph, 0.85, 100);
Map<String, Double> betweenness = CentralityUtil.betweennessCentrality(graph);
List<String> topVertices = CentralityUtil.topK(pageRank, 5);
```

### 3.20 CommunityDetectionUtil

> 社区检测工具类，提供 Louvain 和标签传播算法，用于发现图中的聚类结构。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> CommunityResult<V> louvain(Graph<V> graph)` | Louvain 社区检测（默认参数） |
| `static <V> CommunityResult<V> louvain(Graph<V> graph, double resolution, int maxIterations)` | Louvain（自定义参数） |
| `static <V> CommunityResult<V> labelPropagation(Graph<V> graph)` | 标签传播算法（默认参数） |
| `static <V> CommunityResult<V> labelPropagation(Graph<V> graph, int maxIterations)` | 标签传播（自定义迭代数） |
| `static <V> double calculateModularity(Graph<V> graph, List<Set<V>> communities)` | 计算模块度 |
| `static <V> Set<V> getCommunity(CommunityResult<V> result, V vertex)` | 获取顶点所在社区 |
| `static <V> boolean inSameCommunity(CommunityResult<V> result, V v1, V v2)` | 判断两顶点是否在同一社区 |
| `static <V> List<Set<V>> getSortedBySize(CommunityResult<V> result)` | 按大小排序社区 |

**示例:**

```java
CommunityResult<String> result = CommunityDetectionUtil.louvain(graph);
List<Set<String>> communities = result.communities();
Set<String> community = CommunityDetectionUtil.getCommunity(result, "A");
```

### 3.21 SubgraphUtil

> 子图操作工具类，提供诱导子图、过滤、邻域、图集合运算（并/交/差/对称差）、翻转、补图、采样等。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> Graph<V> induced(Graph<V> graph, Set<V> vertices)` | 顶点诱导子图 |
| `static <V> Graph<V> edgeInduced(Graph<V> graph, Set<Edge<V>> edges)` | 边诱导子图 |
| `static <V> Graph<V> filterVertices(Graph<V> graph, Predicate<V> predicate)` | 按条件过滤顶点 |
| `static <V> Graph<V> filterEdges(Graph<V> graph, Predicate<Edge<V>> predicate)` | 按条件过滤边 |
| `static <V> Graph<V> filterByWeight(Graph<V> graph, double minWeight, double maxWeight)` | 按权重范围过滤 |
| `static <V> Graph<V> neighborhood(Graph<V> graph, V center, int k)` | K 跳邻域子图 |
| `static <V> Graph<V> egoNetwork(Graph<V> graph, V ego)` | 自我网络 |
| `static <V> Graph<V> egoNetwork(Graph<V> graph, V ego, int radius)` | 指定半径的自我网络 |
| `static <V> Graph<V> union(Graph<V> g1, Graph<V> g2)` | 图并集 |
| `static <V> Graph<V> intersection(Graph<V> g1, Graph<V> g2)` | 图交集 |
| `static <V> Graph<V> difference(Graph<V> g1, Graph<V> g2)` | 图差集 |
| `static <V> Graph<V> symmetricDifference(Graph<V> g1, Graph<V> g2)` | 图对称差 |
| `static <V> Graph<V> reverse(Graph<V> graph)` | 翻转有向图 |
| `static <V> Graph<V> copy(Graph<V> graph)` | 深拷贝图 |
| `static <V> Graph<V> complement(Graph<V> graph)` | 补图 |
| `static <V> Graph<V> removeIsolated(Graph<V> graph)` | 移除孤立顶点 |
| `static <V> Graph<V> sampleVertices(Graph<V> graph, int numVertices, Random random)` | 随机采样顶点子图 |
| `static <V> Graph<V> sampleEdges(Graph<V> graph, int numEdges, Random random)` | 随机采样边子图 |

**示例:**

```java
Graph<String> sub = SubgraphUtil.induced(graph, Set.of("A", "B", "C"));
Graph<String> filtered = SubgraphUtil.filterVertices(graph, v -> v.startsWith("node"));
Graph<String> neighborhood = SubgraphUtil.neighborhood(graph, "A", 2);
Graph<String> union = SubgraphUtil.union(graph1, graph2);
```

### 3.22 GraphValidator

> 图验证器，验证顶点、边、图结构的合法性。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> void validateVertex(V vertex)` | 验证顶点非空 |
| `static <V> void validateEdge(V from, V to)` | 验证边的两端非空 |
| `static <V> void validateEdge(V from, V to, double weight)` | 验证边及权重 |
| `static void validateWeight(double weight)` | 验证权重有效（非 NaN/Infinite） |
| `static <V> ValidationResult validateGraph(Graph<V> graph)` | 验证图结构（检测自环、孤立顶点、负权边） |
| `static <V> void validateGraphStructure(Graph<V> graph)` | 验证图结构（抛异常） |
| `static <V> ValidationResult validateDAG(Graph<V> graph)` | 验证是否为 DAG |
| `static <V> boolean vertexExists(Graph<V> graph, V vertex)` | 判断顶点是否存在 |
| `static <V> boolean edgeExists(Graph<V> graph, V from, V to)` | 判断边是否存在 |

### 3.23 ValidationResult

> 验证结果类，包含警告和错误信息。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static ValidationResult success()` | 创建成功结果 |
| `static ValidationResult error(String error)` | 创建包含错误的结果 |
| `static ValidationResult warning(String warning)` | 创建包含警告的结果 |
| `boolean isValid()` | 是否有效（无错误） |
| `boolean hasErrors()` | 是否有错误 |
| `boolean hasWarnings()` | 是否有警告 |
| `List<String> warnings()` | 获取警告列表 |
| `List<String> errors()` | 获取错误列表 |
| `ValidationResult merge(ValidationResult other)` | 合并两个验证结果 |

### 3.24 SafeGraphOperations

> 安全图操作工具类，提供资源限制（顶点/边数量上限）和超时控制。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static void setMaxVertices(int max)` | 设置最大顶点数（默认 100,000） |
| `static void setMaxEdges(int max)` | 设置最大边数（默认 1,000,000） |
| `static void setMaxDepth(int max)` | 设置最大深度（默认 10,000） |
| `static void setTimeout(Duration duration)` | 设置超时时间（默认 30 秒） |
| `static <V> void safeAddVertex(Graph<V> graph, V vertex)` | 安全添加顶点（检查上限） |
| `static <V> void safeAddEdge(Graph<V> graph, V from, V to, double weight)` | 安全添加边（检查上限和权重） |
| `static <V> List<V> safeShortestPath(Graph<V> graph, V source, V target)` | 带超时的最短路径 |
| `static <V> Map<V, Double> safeDijkstra(Graph<V> graph, V source)` | 带超时的 Dijkstra |
| `static <T> T executeWithTimeout(Callable<T> callable, String operationName)` | 通用超时执行 |
| `static void resetToDefaults()` | 重置为默认限制 |

### 3.25 GraphSerializer

> 图序列化工具类，支持 DOT、邻接表、边列表格式。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> String toDot(Graph<V> graph)` | 导出为 DOT 格式 |
| `static <V> String toDot(Graph<V> graph, String graphName)` | 导出为 DOT 格式（自定义名称） |
| `static <V> String toAdjacencyList(Graph<V> graph)` | 导出为邻接表 |
| `static Graph<String> fromAdjacencyList(String input, boolean directed)` | 从邻接表导入 |
| `static <V> String toEdgeList(Graph<V> graph)` | 导出为边列表 |
| `static Graph<String> fromEdgeList(String input, boolean directed)` | 从边列表导入 |
| `static <V> String getStatistics(Graph<V> graph)` | 获取图统计信息 |

### 3.26 GraphMLUtil

> GraphML 格式工具类，支持 GraphML XML 格式的导入导出和文件读写。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> String toGraphML(Graph<V> graph)` | 导出为 GraphML 字符串 |
| `static <V> String toGraphML(Graph<V> graph, Map<String, Map<String, String>> vertexAttrs, Map<String, Map<String, String>> edgeAttrs)` | 导出为 GraphML（带属性） |
| `static <V> void writeToFile(Graph<V> graph, Path path)` | 写入 GraphML 文件 |
| `static Graph<String> fromGraphML(String graphml)` | 从 GraphML 字符串导入 |
| `static Graph<String> readFromFile(Path path)` | 从 GraphML 文件导入 |

### 3.27 GexfUtil

> GEXF 格式工具类，支持 GEXF（Gephi 兼容）格式的导入导出，支持可视化数据。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> String toGexf(Graph<V> graph)` | 导出为 GEXF 字符串 |
| `static <V> String toGexf(Graph<V> graph, Map<String, Map<String, String>> attrs, Map<String, VisualData> visuals)` | 导出为 GEXF（带属性和可视化数据） |
| `static <V> void writeToFile(Graph<V> graph, Path path)` | 写入 GEXF 文件 |
| `static Graph<String> fromGexf(String gexf)` | 从 GEXF 字符串导入 |
| `static Graph<String> readFromFile(Path path)` | 从 GEXF 文件导入 |

### 3.28 LayoutUtil

> 图布局算法工具类，计算图顶点的二维坐标位置，适用于可视化。

**主要方法:**

| 方法 | 描述 |
|------|------|
| `static <V> Map<V, Point2D> forceDirected(Graph<V> graph, double width, double height)` | 力导向布局 |
| `static <V> Map<V, Point2D> forceDirected(Graph<V> graph, double width, double height, int iterations, double cooling)` | 力导向布局（自定义参数） |
| `static <V> Map<V, Point2D> spring(Graph<V> graph, double width, double height, double springLength, double springK)` | 弹簧布局 |
| `static <V> Map<V, Point2D> circular(Graph<V> graph, double centerX, double centerY, double radius)` | 环形布局 |
| `static <V> Map<V, Point2D> circular(Graph<V> graph, double width, double height)` | 环形布局（自适应） |
| `static <V> Map<V, Point2D> grid(Graph<V> graph, double width, double height)` | 网格布局 |
| `static <V> Map<V, Point2D> hierarchical(Graph<V> graph, double width, double height)` | 层次布局 |
| `static <V> Map<V, Point2D> random(Graph<V> graph, double width, double height)` | 随机布局 |
| `static <V> Map<V, Point2D> center(Map<V, Point2D> positions, double width, double height)` | 居中布局 |
| `static <V> Map<V, Point2D> scale(Map<V, Point2D> positions, double width, double height, double margin)` | 缩放布局 |

**示例:**

```java
Map<String, Point2D> positions = LayoutUtil.forceDirected(graph, 800, 600);
Map<String, Point2D> circle = LayoutUtil.circular(graph, 400, 300, 250);
Map<String, Point2D> tree = LayoutUtil.hierarchical(graph, 800, 600);
```

### 3.29 异常类

> 图操作异常体系，所有异常继承自 GraphException。

| 异常类 | 描述 |
|--------|------|
| `GraphException` | 图异常基类，包含 GraphErrorCode |
| `GraphErrorCode` | 错误码枚举（结构/算法/验证/资源错误） |
| `VertexNotFoundException` | 顶点不存在异常 |
| `EdgeNotFoundException` | 边不存在异常 |
| `CycleDetectedException` | 检测到环异常，可通过 `getCycle()` 获取环 |
| `NoPathException` | 两点间无路径异常 |
| `InvalidVertexException` | 无效顶点异常 |
| `InvalidEdgeException` | 无效边异常 |
| `GraphLimitExceededException` | 超出资源限制异常 |
| `GraphTimeoutException` | 计算超时异常 |

# OpenCode Base Graph

**图数据结构与算法库，适用于 Java 25+**

`opencode-base-graph` 提供全面的图库，包括有向/无向/加权图、经典图算法（BFS、DFS、Dijkstra、A*、拓扑排序）、最小生成树、网络流、环检测、社区发现和图序列化。

## 功能特性

### 图类型
- **DirectedGraph**：基于邻接表的有向图
- **UndirectedGraph**：对称边存储的无向图
- **WeightedGraph**：支持有向和无向模式的加权图

### 算法
- **遍历**：BFS、DFS（递归和迭代栈安全版本）
- **最短路径**：Dijkstra、自定义启发式的 A*、双向 BFS
- **拓扑排序**：带环检测的 Kahn 算法
- **环检测**：有向/无向图中的环检测和环查找
- **连通性**：连通分量、可达性、完全连通检查
- **最小生成树**：Prim 和 Kruskal 算法
- **网络流**：Ford-Fulkerson（Edmonds-Karp）、最大流、最小割
- **中心性**：度中心性、介数中心性、接近中心性和 PageRank
- **社区发现**：图分区和聚类识别
- **子图**：导出子图提取

### 高级功能
- **图构建器**：流式 API 构建图
- **图验证**：结构和约束验证
- **图序列化**：GraphML 和 GEXF 格式导出
- **布局**：力导向图布局算法
- **安全性**：栈安全遍历、超时保护、大小限制

## 快速开始

### Maven 依赖
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-graph</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 基本用法

```java
import cloud.opencode.base.graph.*;

// 创建有向图
Graph<String> graph = OpenGraph.directed();
graph.addEdge("A", "B", 1.0);
graph.addEdge("B", "C", 2.0);
graph.addEdge("A", "C", 4.0);

// BFS 遍历
List<String> bfs = OpenGraph.bfs(graph, "A");  // [A, B, C]

// DFS 遍历
List<String> dfs = OpenGraph.dfs(graph, "A");  // [A, B, C]

// 最短路径
List<String> path = OpenGraph.shortestPath(graph, "A", "C");  // [A, B, C]
```

### 图构建器

```java
// 流式图构建
Graph<String> graph = OpenGraph.<String>directedBuilder()
    .edge("A", "B", 1.0)
    .edge("B", "C", 2.0)
    .edge("A", "C", 4.0)
    .build();
```

### 最短路径算法

```java
// Dijkstra - 从源点出发的所有最短距离
Map<String, Double> distances = OpenGraph.dijkstra(graph, "A");

// A* 带启发式函数
List<String> path = OpenGraph.aStar(graph, "A", "Z",
    (a, b) -> estimateDistance(a, b));

// 双向 BFS（适合大型无权图）
List<String> path = OpenGraph.bidirectionalBfs(graph, "A", "Z");
```

### 拓扑排序与环检测

```java
// 拓扑排序（仅限 DAG）
List<String> order = OpenGraph.topologicalSort(graph);

// 检查是否可排序（是否为 DAG）
boolean isDAG = OpenGraph.canTopologicalSort(graph);

// 环检测
boolean hasCycle = OpenGraph.hasCycle(graph);
List<String> cycle = OpenGraph.findCycle(graph);
```

### 最小生成树

```java
Graph<String> graph = OpenGraph.undirected();
// ... 添加边 ...

// Prim 算法
Set<Edge<String>> mst = OpenGraph.prim(graph);

// Kruskal 算法
Set<Edge<String>> mst = OpenGraph.kruskal(graph);

// 最小生成树总权重
double weight = OpenGraph.mstWeight(graph);
```

### 网络流

```java
// 最大流
double maxFlow = OpenGraph.maxFlow(graph, source, sink);

// 每条边上的流量
Map<Edge<String>, Double> flows = OpenGraph.getFlows(graph, source, sink);

// 最小割
Set<Edge<String>> minCut = OpenGraph.minCut(graph, source, sink);

// 完整流结果
NetworkFlowUtil.FlowResult<String> result = OpenGraph.computeFlow(graph, source, sink);
```

### 连通性

```java
// 连通分量
List<Set<String>> components = OpenGraph.connectedComponents(graph);

// 检查连通性
boolean connected = OpenGraph.isConnected(graph, "A", "B");
boolean fullyConnected = OpenGraph.isFullyConnected(graph);
int componentCount = OpenGraph.connectedComponentCount(graph);
```

## 类参考

### 根包 (`cloud.opencode.base.graph`)
| 类 | 说明 |
|---|------|
| `OpenGraph` | 主门面类，提供图创建和算法访问的静态方法 |
| `Graph` | 图数据结构操作的核心接口 |
| `DirectedGraph` | 基于邻接表的有向图实现 |
| `UndirectedGraph` | 对称边的无向图实现 |
| `WeightedGraph` | 支持有向和无向模式的加权图 |

### 算法包 (`cloud.opencode.base.graph.algorithm`)
| 类 | 说明 |
|---|------|
| `GraphTraversalUtil` | BFS 和 DFS 遍历算法 |
| `SafeGraphTraversalUtil` | 栈安全的迭代式 DFS，避免栈溢出 |
| `ShortestPathUtil` | Dijkstra 最短路径算法 |
| `AStarUtil` | 带自定义启发式函数的 A* 寻路 |
| `BidirectionalBfsUtil` | 双向 BFS，适用于大图的高效路径查找 |
| `TopologicalSortUtil` | Kahn 拓扑排序算法 |
| `CycleDetectionUtil` | 图中的环检测和环查找 |
| `ConnectedComponentsUtil` | 连通分量分析和可达性 |
| `MinimumSpanningTreeUtil` | Prim 和 Kruskal 最小生成树算法 |
| `NetworkFlowUtil` | Ford-Fulkerson 最大流、最小割和边流量计算 |
| `CentralityUtil` | 度、介数、接近和 PageRank 中心性 |
| `CommunityDetectionUtil` | 图分区和社区识别 |
| `SubgraphUtil` | 导出子图提取 |

### 构建器包 (`cloud.opencode.base.graph.builder`)
| 类 | 说明 |
|---|------|
| `GraphBuilder` | 有向和无向图构建的流式构建器 |

### 节点包 (`cloud.opencode.base.graph.node`)
| 类 | 说明 |
|---|------|
| `Node` | 图顶点表示 |
| `Edge` | 带源、目标和权重的图边 |

### 序列化包 (`cloud.opencode.base.graph.serializer`)
| 类 | 说明 |
|---|------|
| `GraphSerializer` | 图序列化/反序列化接口 |
| `GraphMLUtil` | GraphML 格式序列化工具 |
| `GexfUtil` | GEXF 格式序列化工具 |

### 布局包 (`cloud.opencode.base.graph.layout`)
| 类 | 说明 |
|---|------|
| `LayoutUtil` | 力导向和其他图布局算法 |

### 验证包 (`cloud.opencode.base.graph.validation`)
| 类 | 说明 |
|---|------|
| `GraphValidator` | 图结构和约束验证 |
| `ValidationResult` | 带错误详情的验证结果 |

### 安全包 (`cloud.opencode.base.graph.security`)
| 类 | 说明 |
|---|------|
| `SafeGraphOperations` | 大小限制和超时保护的图操作 |

### 异常包 (`cloud.opencode.base.graph.exception`)
| 类 | 说明 |
|---|------|
| `GraphException` | 图操作的基础异常 |
| `CycleDetectedException` | 在预期 DAG 中发现环 |
| `EdgeNotFoundException` | 引用的边未找到 |
| `VertexNotFoundException` | 引用的顶点未找到 |
| `InvalidEdgeException` | 无效的边定义 |
| `InvalidVertexException` | 无效的顶点定义 |
| `NoPathException` | 源和目标之间不存在路径 |
| `GraphLimitExceededException` | 图大小或操作限制超出 |
| `GraphTimeoutException` | 图操作超时 |
| `GraphErrorCode` | 图错误码枚举 |

## 环境要求

- Java 25+（使用 record、密封接口、模式匹配）
- 核心功能无外部依赖

## 许可证

Apache License 2.0

## 作者

Leon Soo - [OpenCode.cloud](https://opencode.cloud)

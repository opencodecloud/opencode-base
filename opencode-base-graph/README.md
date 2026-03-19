# OpenCode Base Graph

**Graph data structures and algorithms for Java 25+**

`opencode-base-graph` provides a comprehensive graph library with directed/undirected/weighted graphs, classic graph algorithms (BFS, DFS, Dijkstra, A*, topological sort), minimum spanning trees, network flow, cycle detection, community detection, and graph serialization.

## Features

### Graph Types
- **DirectedGraph**: Directed graph with adjacency list representation
- **UndirectedGraph**: Undirected graph with symmetric edge storage
- **WeightedGraph**: Weighted graph supporting both directed and undirected modes

### Algorithms
- **Traversal**: BFS, DFS (recursive and iterative stack-safe)
- **Shortest Path**: Dijkstra, A* with custom heuristics, Bidirectional BFS
- **Topological Sort**: Kahn's algorithm with cycle detection
- **Cycle Detection**: Detect and find cycles in directed/undirected graphs
- **Connectivity**: Connected components, reachability, full connectivity check
- **Minimum Spanning Tree**: Prim's and Kruskal's algorithms
- **Network Flow**: Ford-Fulkerson (Edmonds-Karp), max flow, min cut
- **Centrality**: Degree, betweenness, closeness, and PageRank centrality
- **Community Detection**: Graph partitioning and cluster identification
- **Subgraph**: Extract induced subgraphs

### Advanced Features
- **Graph Builder**: Fluent API for graph construction
- **Graph Validation**: Structure and constraint validation
- **Graph Serialization**: GraphML and GEXF format export
- **Layout**: Force-directed graph layout algorithms
- **Safety**: Stack-safe traversal, timeout protection, size limits

## Quick Start

### Maven Dependency
```xml
<dependency>
    <groupId>cloud.opencode.base</groupId>
    <artifactId>opencode-base-graph</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Basic Usage

```java
import cloud.opencode.base.graph.*;

// Create a directed graph
Graph<String> graph = OpenGraph.directed();
graph.addEdge("A", "B", 1.0);
graph.addEdge("B", "C", 2.0);
graph.addEdge("A", "C", 4.0);

// BFS traversal
List<String> bfs = OpenGraph.bfs(graph, "A");  // [A, B, C]

// DFS traversal
List<String> dfs = OpenGraph.dfs(graph, "A");  // [A, B, C]

// Shortest path
List<String> path = OpenGraph.shortestPath(graph, "A", "C");  // [A, B, C]
```

### Graph Builder

```java
// Fluent graph construction
Graph<String> graph = OpenGraph.<String>directedBuilder()
    .edge("A", "B", 1.0)
    .edge("B", "C", 2.0)
    .edge("A", "C", 4.0)
    .build();
```

### Shortest Path Algorithms

```java
// Dijkstra - all shortest distances from source
Map<String, Double> distances = OpenGraph.dijkstra(graph, "A");

// A* with heuristic
List<String> path = OpenGraph.aStar(graph, "A", "Z",
    (a, b) -> estimateDistance(a, b));

// Bidirectional BFS (efficient for large unweighted graphs)
List<String> path = OpenGraph.bidirectionalBfs(graph, "A", "Z");
```

### Topological Sort & Cycle Detection

```java
// Topological sort (DAG only)
List<String> order = OpenGraph.topologicalSort(graph);

// Check if sortable (is DAG)
boolean isDAG = OpenGraph.canTopologicalSort(graph);

// Cycle detection
boolean hasCycle = OpenGraph.hasCycle(graph);
List<String> cycle = OpenGraph.findCycle(graph);
```

### Minimum Spanning Tree

```java
Graph<String> graph = OpenGraph.undirected();
// ... add edges ...

// Prim's algorithm
Set<Edge<String>> mst = OpenGraph.prim(graph);

// Kruskal's algorithm
Set<Edge<String>> mst = OpenGraph.kruskal(graph);

// MST total weight
double weight = OpenGraph.mstWeight(graph);
```

### Network Flow

```java
// Maximum flow
double maxFlow = OpenGraph.maxFlow(graph, source, sink);

// Flow on each edge
Map<Edge<String>, Double> flows = OpenGraph.getFlows(graph, source, sink);

// Minimum cut
Set<Edge<String>> minCut = OpenGraph.minCut(graph, source, sink);

// Full flow result
NetworkFlowUtil.FlowResult<String> result = OpenGraph.computeFlow(graph, source, sink);
```

### Connectivity

```java
// Connected components
List<Set<String>> components = OpenGraph.connectedComponents(graph);

// Check connectivity
boolean connected = OpenGraph.isConnected(graph, "A", "B");
boolean fullyConnected = OpenGraph.isFullyConnected(graph);
int componentCount = OpenGraph.connectedComponentCount(graph);
```

## Class Reference

### Root Package (`cloud.opencode.base.graph`)
| Class | Description |
|-------|-------------|
| `OpenGraph` | Main facade with static methods for graph creation and algorithm access |
| `Graph` | Core interface for graph data structure operations |
| `DirectedGraph` | Directed graph implementation with adjacency list |
| `UndirectedGraph` | Undirected graph implementation with symmetric edges |
| `WeightedGraph` | Weighted graph supporting directed and undirected modes |

### Algorithm Package (`cloud.opencode.base.graph.algorithm`)
| Class | Description |
|-------|-------------|
| `GraphTraversalUtil` | BFS and DFS traversal algorithms |
| `SafeGraphTraversalUtil` | Stack-safe iterative DFS to avoid stack overflow |
| `ShortestPathUtil` | Dijkstra's shortest path algorithm |
| `AStarUtil` | A* pathfinding with custom heuristic functions |
| `BidirectionalBfsUtil` | Bidirectional BFS for efficient path finding in large graphs |
| `TopologicalSortUtil` | Kahn's algorithm for topological ordering |
| `CycleDetectionUtil` | Cycle detection and cycle finding in graphs |
| `ConnectedComponentsUtil` | Connected component analysis and reachability |
| `MinimumSpanningTreeUtil` | Prim's and Kruskal's MST algorithms |
| `NetworkFlowUtil` | Ford-Fulkerson max flow, min cut, and edge flow computation |
| `CentralityUtil` | Degree, betweenness, closeness, and PageRank centrality |
| `CommunityDetectionUtil` | Graph partitioning and community identification |
| `SubgraphUtil` | Induced subgraph extraction |

### Builder Package (`cloud.opencode.base.graph.builder`)
| Class | Description |
|-------|-------------|
| `GraphBuilder` | Fluent builder for directed and undirected graph construction |

### Node Package (`cloud.opencode.base.graph.node`)
| Class | Description |
|-------|-------------|
| `Node` | Graph vertex representation |
| `Edge` | Graph edge with source, target, and weight |

### Serializer Package (`cloud.opencode.base.graph.serializer`)
| Class | Description |
|-------|-------------|
| `GraphSerializer` | Interface for graph serialization/deserialization |
| `GraphMLUtil` | GraphML format serialization utility |
| `GexfUtil` | GEXF format serialization utility |

### Layout Package (`cloud.opencode.base.graph.layout`)
| Class | Description |
|-------|-------------|
| `LayoutUtil` | Force-directed and other graph layout algorithms |

### Validation Package (`cloud.opencode.base.graph.validation`)
| Class | Description |
|-------|-------------|
| `GraphValidator` | Graph structure and constraint validation |
| `ValidationResult` | Validation result with error details |

### Security Package (`cloud.opencode.base.graph.security`)
| Class | Description |
|-------|-------------|
| `SafeGraphOperations` | Size-limited and timeout-protected graph operations |

### Exception Package (`cloud.opencode.base.graph.exception`)
| Class | Description |
|-------|-------------|
| `GraphException` | Base exception for graph operations |
| `CycleDetectedException` | Cycle found where DAG was expected |
| `EdgeNotFoundException` | Referenced edge not found |
| `VertexNotFoundException` | Referenced vertex not found |
| `InvalidEdgeException` | Invalid edge definition |
| `InvalidVertexException` | Invalid vertex definition |
| `NoPathException` | No path exists between source and target |
| `GraphLimitExceededException` | Graph size or operation limit exceeded |
| `GraphTimeoutException` | Graph operation timed out |
| `GraphErrorCode` | Enumeration of graph error codes |

## Requirements

- Java 25+ (uses records, sealed interfaces, pattern matching)
- No external dependencies for core functionality

## License

Apache License 2.0

## Author

Leon Soo - [OpenCode.cloud](https://opencode.cloud)
